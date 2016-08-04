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
import org.json.JSONObject;

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

        easyLinkCallbackContext = callbackContext;
        if (action.equals(SET_DEVICE_WIFI)) {
            String ssid = args.getString(0);
            String apPwd = args.getString(1);
            uid = args.getString(2);
            APPId = args.getString(3);
            productKey = args.getString(4);
            token = args.getString(5);
            deviceLoginID = args.getString(6);
            devicePassword = args.getString(7);
            setDeviceWifi(ssid, apPwd, mWifiAdmin.getWifiConnectedBssid(), false, -1);
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

                if (result.isSuc()) {
                    try {

                        JSONObject deviceInfo = new JSONObject();
                        deviceInfo.put("mac", result.getBssid());
                        deviceInfo.put("ip", result.getInetAddress());
                        easyLinkCallbackContext.success(deviceInfo);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //                List<IEsptouchResult> result = mEsptouchTask.executeForResults(taskResultCountStr);
            }
        }).start();
    }

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            Log.d(TAG, "IEsptouchListener:" + result.getInetAddress().getHostAddress());
            Log.d(TAG, "IEsptouchListener:" + result.getBssid());//macAddress;
        }
    };

    private void dealloc() {
        //取消配对
        if (mEsptouchTask != null) {
            mEsptouchTask.interrupt();
        }
    }
}
