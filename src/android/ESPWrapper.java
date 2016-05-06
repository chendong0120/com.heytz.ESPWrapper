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

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        context = cordova.getActivity();//.getApplication();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(SET_DEVICE_WIFI)) {
            setDeviceWifi(args.getString(0), args.getString(1), args.getString(2), args.getBoolean(3), null);
            return true;
        }
        if (action.equals(DEALLOC)) {
            dealloc();
            return true;
        }
        return false;
    }

    private void setDeviceWifi(String apSsid, String apPassword, String apBssid, Boolean isSsidHidden, String taskResultCountStr) {

        int taskResultCount = -1;
        if (__IEsptouchTask.DEBUG) {
            Log.d(TAG, "mBtnConfirm is clicked, mEdtApSsid = " + apSsid
                    + ", " + " mEdtApPassword = " + apPassword);
        }
        if (null != taskResultCountStr) {
            taskResultCount = Integer.parseInt(taskResultCountStr);
        }

        mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
                isSsidHidden, context);
        mEsptouchTask.setEsptouchListener(myListener);
        List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
        Log.d(TAG, resultList.toString());
    }

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
//            onEsptoucResultAddedPerform(result);
        }
    };

    private void dealloc() {
        //取消配对
        if (mEsptouchTask != null) {
            mEsptouchTask.interrupt();
        }
    }
}