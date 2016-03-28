package com.heytz.ESPWrapper;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.heytz.ESPWrapper.esptouch.protocol.EsptouchGenerator;
import com.heytz.ESPWrapper.esptouch.task.IEsptouchGenerator;
import com.heytz.ESPWrapper.esptouch.util.EspNetUtil;
import com.heytz.ESPWrapper.esptouch.udp.UDPSocketClient;
import com.heytz.ESPWrapper.esptouch.udp.UDPSocketServer;
import com.heytz.ESPWrapper.esptouch.task.IEsptouchTaskParameter;
import com.heytz.ESPWrapper.esptouch.task.EsptouchTaskParameter;
import com.heytz.ESPWrapper.esptouch.util.ByteUtil;
import com.heytz.ESPWrapper.esptouch.IEsptouchResult;
import com.heytz.ESPWrapper.esptouch.EsptouchResult;
import com.heytz.ESPWrapper.esptouch.IEsptouchListener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.InetAddress;
import java.util.*;
//import android.os;
//import com.espressif.iot.esptouch.EsptouchTask;
//import com.espressif.iot.esptouch.IEsptouchListener;
//import com.espressif.iot.esptouch.IEsptouchResult;
//import com.espressif.iot.esptouch.IEsptouchTask;
//import com.espressif.iot.esptouch.task.__IEsptouchTask;
//import com.espressif.iot_esptouch_demo.R;
//import esptouch.udp.UDPSocketClient;
//import com.lsd.easy.joine.test.R;

/**
 * This class starts transmit to activation
 */
public class ESPWrapper extends CordovaPlugin {

    private static String TAG = "=====ESPWrapper.class====";

    private Context context;
    private String userName;
    private String deviceLoginID;
    private String devicePassword;
    private int activateTimeout;
    private String activatePort;
    private CallbackContext ESPCallbackContext;
    private String wifiSSID;
    private String wifiKey;
    private UDPSocketClient mSocketClient;
    private UDPSocketServer mSocketServer;
    private IEsptouchTaskParameter mParameter;
    private volatile boolean mIsInterrupt = false;
    private volatile List<IEsptouchResult> mEsptouchResultList;
    private volatile boolean mIsSuc = false;
    private volatile Map<String, Integer> mBssidTaskSucCountMap;
    private IEsptouchListener mEsptouchListener;


    private static int[][] desTables = new int[][]{{15, 12, 8, 2}, {13, 8, 10, 1}, {1, 10, 13, 0}, {3, 15, 0, 6}, {11, 8, 12, 7}, {4, 3, 2, 12}, {6, 11, 13, 8}, {2, 1, 14, 7}};
    private Handler mHandler;
//    private final UDPSocketClient mSocketClient;

    private static boolean sendFlag = true;
    public static int CODE_INTERVAL_TIMES = 8;
    public static int CODE_INTERVAL_TIME = 500;
    public static int CODE_TIME = 20;
    public static int CODE_TIMES = 5;
    private String broadcastIp = "255.255.255.255";
    private Set<String> successMacSet = new HashSet();


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        context = cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("setDeviceWifi")) {

            wifiSSID = args.getString(0);
            wifiKey = args.getString(1);
            userName = args.getString(2);
            //easylinkVersion = args.getInt(3);
            activateTimeout = args.getInt(4);
            activatePort = args.getString(5);
            deviceLoginID = args.getString(6);
            devicePassword = args.getString(7);
            String isSsidHiddenStr = "NO";
            String taskResultCountStr = "1";

            if (wifiSSID == null || wifiSSID.length() == 0 ||
                    wifiKey == null || wifiKey.length() == 0 ||
                    userName == null || userName.length() == 0 ||
                    activatePort == null || activatePort.length() == 0 ||
                    devicePassword == null || devicePassword.length() == 0 ||
                    deviceLoginID == null || deviceLoginID.length() == 0
                    ) {
                Log.e(TAG, "arguments error ===== empty");
                return false;
            }

            InetAddress localInetAddress = EspNetUtil.getLocalInetAddress(context);

            mParameter = new EsptouchTaskParameter();
            mEsptouchResultList = new ArrayList<IEsptouchResult>();
            mSocketClient = new UDPSocketClient();
            mSocketServer = new UDPSocketServer(mParameter.getPortListening(),
                    mParameter.getWaitUdpTotalMillisecond(), context);

            IEsptouchGenerator generator = new EsptouchGenerator(wifiSSID, wifiSSID,
                    wifiKey, localInetAddress, true);
            listenAsyn(mParameter.getEsptouchResultTotalLen());

            execute(generator);

//            byte[][] gcBytes2;
//            byte[][] dcBytes2;
//            GuideCode gc = new GuideCode();
//            char[] gcU81 = gc.getU8s();
//            gcBytes2 = new byte[gcU81.length][];
//
//            for (int i = 0; i < gcBytes2.length; i++) {
//                gcBytes2[i] = ByteUtil.genSpecBytes(gcU81[i]);
//            }
//
//            // generate data code
//            DatumCode dc = new DatumCode(apSsid, apBssid, apPassword, inetAddress,
//                    isSsidHiden);
//            char[] dcU81 = dc.getU8s();
//            dcBytes2 = new byte[dcU81.length][];
//
//            for (int i = 0; i < dcBytes2.length; i++) {
//                dcBytes2[i] = ByteUtil.genSpecBytes(dcU81[i]);
//            }
//
//
//            mSocketClient.sendData(dcBytes2, 0, dcBytes2.length,
//                    mParameter.getTargetHostname(),
//                    mParameter.getTargetPort(),
//                    mParameter.getIntervalDataCodeMillisecond());
//
//
//            mHandler.postDelayed(timeoutRun, 40000L);
//            send(wifiSSID, wifiKey);
//            // todo: replace with EasylinkAPI
//            //ftcService = new FTC_Service();
////            SmartConfigActivity.onConfigResult = callbackContext;
//            ESPCallbackContext = callbackContext;
//            //ftcListener = new FTCLisenerExtension(callbackContext);
////            this.transmitSettings(wifiSSID, wifiKey);
            return true;
        }
//        if (action.equals("dealloc")) {
//            stopSend();
//            return true;
//        }
        return false;
    }

    private boolean execute(IEsptouchGenerator generator) {

        byte[][] gcBytes2 = generator.getGCBytes2();
        byte[][] dcBytes2 = generator.getDCBytes2();

        int index = 0;

        mSocketClient.sendData(gcBytes2,
                mParameter.getTargetHostname(),
                mParameter.getTargetPort(),
                mParameter.getIntervalGuideCodeMillisecond());
//        mSocketClient.sendData(dcBytes2, index, dcBytes2.length,
//                mParameter.getTargetHostname(),
//                mParameter.getTargetPort(),
//                mParameter.getIntervalDataCodeMillisecond());

        return true;
    }

    private void listenAsyn(final int expectDataLen) {
        new Thread() {
            public void run() {

                long startTimestamp = System.currentTimeMillis();
                byte[] apSsidAndPassword = ByteUtil.getBytesByString(wifiSSID
                        + wifiKey);
                byte expectOneByte = (byte) (apSsidAndPassword.length + 9);

                byte receiveOneByte = -1;
                byte[] receiveBytes = null;
                while (mEsptouchResultList.size() < mParameter
                        .getExpectTaskResultCount() && !mIsInterrupt) {
                    receiveBytes = mSocketServer
                            .receiveSpecLenBytes(expectDataLen);
                    if (receiveBytes != null) {
                        receiveOneByte = receiveBytes[0];
                    } else {
                        receiveOneByte = -1;
                    }
                    if (receiveOneByte == expectOneByte) {

                        // change the socket's timeout
                        long consume = System.currentTimeMillis()
                                - startTimestamp;
                        int timeout = (int) (mParameter
                                .getWaitUdpTotalMillisecond() - consume);
                        if (timeout < 0) {

                            break;
                        } else {

                            mSocketServer.setSoTimeout(timeout);

                            if (receiveBytes != null) {
                                String bssid = ByteUtil.parseBssid(
                                        receiveBytes,
                                        mParameter.getEsptouchResultOneLen(),
                                        mParameter.getEsptouchResultMacLen());
                                InetAddress inetAddress = EspNetUtil
                                        .parseInetAddr(
                                                receiveBytes,
                                                mParameter.getEsptouchResultOneLen()
                                                        + mParameter.getEsptouchResultMacLen(),
                                                mParameter.getEsptouchResultIpLen());
                                __putEsptouchResult(true, bssid, inetAddress);
                            }
                        }
                    } else {

                    }
                }
                mIsSuc = mEsptouchResultList.size() >= mParameter
                        .getExpectTaskResultCount();
                interrupt();

            }
        }.start();
    }
    private void __putEsptouchResult(boolean isSuc, String bssid,
                                     InetAddress inetAddress) {
        synchronized (mEsptouchResultList) {
            // check whether the result receive enough UDP response
            boolean isTaskSucCountEnough = false;
            Integer count = mBssidTaskSucCountMap.get(bssid);
            if (count == null) {
                count = 0;
            }
            ++count;

            mBssidTaskSucCountMap.put(bssid, count);
            isTaskSucCountEnough = count >= mParameter
                    .getThresholdSucBroadcastCount();
            if (!isTaskSucCountEnough) {

                return;
            }
            // check whether the result is in the mEsptouchResultList already
            boolean isExist = false;
            for (IEsptouchResult esptouchResultInList : mEsptouchResultList) {
                if (esptouchResultInList.getBssid().equals(bssid)) {
                    isExist = true;
                    break;
                }
            }
            // only add the result who isn't in the mEsptouchResultList
            if (!isExist) {

                final IEsptouchResult esptouchResult = new EsptouchResult(isSuc,
                        bssid, inetAddress);
                mEsptouchResultList.add(esptouchResult);
                if (mEsptouchListener != null) {
                    mEsptouchListener.onEsptouchResultAdded(esptouchResult);
                }
            }
        }
    }

    public synchronized void interrupt() {
        if (!mIsInterrupt) {
            mIsInterrupt = true;
            mSocketClient.interrupt();
            mSocketServer.interrupt();
            // interrupt the current Thread which is used to wait for udp response
            Thread.currentThread().interrupt();
        }
    }
}
