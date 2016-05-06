package com.heytz.ESPWrapper;

 import android.content.Context;
 import android.util.Log;
 import com.espressif.iot.esptouch.EsptouchTask;
 import com.espressif.iot.esptouch.IEsptouchListener;
 import com.espressif.iot.esptouch.IEsptouchResult;
 import com.espressif.iot.esptouch.IEsptouchTask;
 import com.espressif.iot.esptouch.task.__IEsptouchTask;
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.CordovaInterface;
 import org.apache.cordova.CordovaPlugin;
 import org.apache.cordova.CordovaWebView;
 import org.json.JSONArray;
 import org.json.JSONException;

 import java.util.List;
/**
 * This class starts transmit to activation
 */
public class ESPWrapper extends CordovaPlugin {
  private final static String TAG = "HeytzEsptouch";

     private final static String SET_DEVICE_WIFI = "setDeviceWifi";
     private final static String DEALLOC = "dealloc";
     private Context context;
     private IEsptouchTask mEsptouchTask;
     private EspWifiAdminSimple mWifiAdmin;
     private Socket socket;

     private String mac;
     private CallbackContext easyLinkCallbackContext;
     private String uid;
     private String token;
     private String APPId;
     private String productKey;
     private String deviceLoginID;
     private String devicePassword;

     @Override
     public void initialize(CordovaInterface cordova, CordovaWebView webView) {
         context = cordova.getActivity();//.getApplication();
         mWifiAdmin = new EspWifiAdminSimple(context);
     }

     @Override
     public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
         if (action.equals(SET_DEVICE_WIFI)) {
             easyLinkCallbackContext = callbackContext;
 //            String ssid = args.getString(0);
 //            String apPwd = args.getString(1);
 //            uid = args.getString(1);
 //            APPId = args.getString(2);
 //            productKey = args.getString(3);
 //            token = args.getString(4);
 //            deviceLoginID = args.getString(6);
 //            devicePassword = args.getString(7);
             setDeviceWifi("Heytz", "523618++", mWifiAdmin.getWifiConnectedBssid(), false, -1);
             return true;
         }
         if (action.equals(DEALLOC)) {
             dealloc();
             return true;
         }
         return false;
     }

     private void setDeviceWifi(final String apSsid, final String apPassword, final String apBssid, final Boolean isSsidHidden, final int taskResultCountStr) {


         if (__IEsptouchTask.DEBUG) {
             Log.d(TAG, "mBtnConfirm is clicked, mEdtApSsid = " + apSsid
                     + ", " + " mEdtApPassword = " + apPassword);
         }
         new Thread(new Runnable() {
             @Override
             public void run() {
                 mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
                         isSsidHidden, context);
                 mEsptouchTask.setEsptouchListener(myListener);
                 IEsptouchResult result = mEsptouchTask.executeForResult();
 //                List<IEsptouchResult> result = mEsptouchTask.executeForResults(taskResultCountStr);
             }
         }).start();
     }

     private IEsptouchListener myListener = new IEsptouchListener() {

         @Override
         public void onEsptouchResultAdded(final IEsptouchResult result) {
 //            onEsptoucResultAddedPerform(result);
             final String deviceIP = result.getInetAddress().getHostAddress();
             mac = result.getBssid();//macAddress;
 //            result.getInetAddress().getAddress();
             if (result.isSuc()) {
                 Log.d(TAG, result.getBssid());
                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                         boolean isReady = false;
                         int timeoutValue = 30;
                         while (!isReady && !(timeoutValue == 0)) {
                             try {
                                 Thread.sleep(1000L);
                                 timeoutValue--;
                             } catch (InterruptedException e) {
                                 Log.e(TAG, e.getMessage());
                             }

                             try {
                                 try {
                                     while (!isReady) {
                                         socket = new Socket(deviceIP, 8000);
                                         isReady = true;
                                     }
                                 } catch (Exception se) {
                                     Log.e(TAG, se.toString());
                                 }
                                 if (isReady) {
                                     final OutputStream os = socket.getOutputStream();
                                     String cmd = "{" + "\"app_id\":\"" + APPId + "\"," +
                                             "\"product_key\":\"" + productKey + "\"," +
                                             "\"user_token\":\"" + token + "\"," +
                                             "\"uid\":\"" + uid +
                                             "\"}";
                                     os.write(cmd.getBytes());
                                     Log.i(TAG, cmd);

                                     InputStream is = socket.getInputStream();
                                     byte[] reply = new byte[0];
                                     try {
                                         reply = readStream(is);
                                     } catch (Exception e) {
                                         Log.e(TAG, e.toString());
                                         e.printStackTrace();
                                     }

                                     final String replyMessages = new String(reply);
                                     JSONObject activeJSON = new JSONObject();

                                     String stringResult = "{\"did\": \"" + replyMessages + "\", \"mac\": \"" + mac + "\"}";
                                     activeJSON.put("device_id", replyMessages);
                                     activeJSON.put("mac", mac);
 //                                activeJSON = new JSONObject(replyMessages);

                                     easyLinkCallbackContext.success(activeJSON);
                                     if (reply.length > 0) {
                                         break;
                                     }
                                 }
                             } catch (Exception e) {
                                 Log.e(TAG, e.getMessage());
                                 try {
                                     Thread.sleep(3 * 1000L);
                                     timeoutValue = timeoutValue - 3;
                                 } catch (InterruptedException e1) {

                                     Log.e(TAG, e1.getMessage());

                                 }
                             }
                         }
                     }
                 }).start();
             }
         }
     };

     private void dealloc() {
         //取消配对
         if (mEsptouchTask != null) {
             mEsptouchTask.interrupt();
         }
     }

     private static byte[] readStream(InputStream inStream) throws Exception {
         int count = 0;
         while (count == 0) {
             count = inStream.available();
             Log.i(TAG, String.valueOf(count));
         }
         byte[] b = new byte[count];
         inStream.read(b);
         return b;
     }

     private void sendDidVerification(String did) {
         try {
             final OutputStream os = socket.getOutputStream();
             String cmd = "{" + "\"device_id\":\"" + did +
                     "\"}";
             os.write(cmd.getBytes());
             easyLinkCallbackContext.success("OK");
             if (socket != null) {
                 try {
                     socket.close();
                     Log.i(TAG, "Socket closed.");
                 } catch (Exception e) {
                     Log.e(TAG, e.toString());
                     e.printStackTrace();
                 }
             }
         } catch (Exception e) {
             easyLinkCallbackContext.error("Device activate failed.");
             Log.e(TAG, e.getMessage());
         }
     }
 }
