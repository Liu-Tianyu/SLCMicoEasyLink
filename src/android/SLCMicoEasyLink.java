package com.siemens.dolphin.SLCMicoEasyLink;

import android.content.Context;
import android.util.Log;

import io.fogcloud.sdk.easylink.api.EasyLink;
import io.fogcloud.sdk.easylink.helper.EasyLinkCallBack;
import io.fogcloud.sdk.easylink.helper.EasyLinkParams;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

public class SLCMicoEasyLink extends CordovaPlugin {

    private static final String TAG = "SLCMicoEasyLink";

    private static final String ACTION_WIFI_SSID = "wifiSSID";
    private static final String ACTION_START_WIFI_CONFIG_WITH_PWD = "startWifiConfigWithPwd";
    private static final String ACTION_STOP_WIFI_CONFIG = "stopWifiConfig";
    private static final int RUN_SECOND = 35000;
    private static final int SLEEP_TIME = 20;

    private EasyLink easyLink;
    private Context context;

    CallbackContext discoverCallback;

    /**
     * Constructor.
     */
    public SLCMicoEasyLink() {

    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        context = this.cordova.getActivity().getApplicationContext();
        easyLink = new EasyLink(context);
    }

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) {
        if (action.equals(ACTION_WIFI_SSID)) {
            callbackContext.success(getWifiSSID());
            return true;
        }

        if (action.equals(ACTION_START_WIFI_CONFIG_WITH_PWD)) {
            this.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        startWifiConfigWithPwd(callbackContext, args.getString(0));
                    } catch (JSONException e) {
                        Thread t = Thread.currentThread();
                        t.getUncaughtExceptionHandler().uncaughtException(t, e);
                    }
                }
            });
            return true;
        }

        if (action.equals(ACTION_STOP_WIFI_CONFIG)) {
            stopWifiConfig();
            return true;
        }

        return false;
    }

    public String getWifiSSID() {
        String ssid = easyLink.getSSID();
        return (ssid.length() > 0 ? ssid : "No ssid found.");
    }

    public void startWifiConfigWithPwd(CallbackContext callbackContext, String password) {
        discoverCallback = callbackContext;

        EasyLinkParams easylinkPara = new EasyLinkParams();
        easylinkPara.ssid = easyLink.getSSID();
        easylinkPara.password = password;
        easylinkPara.runSecond = RUN_SECOND;
        easylinkPara.sleeptime = SLEEP_TIME;

        easyLink.startEasyLink(easylinkPara, new EasyLinkCallBack() {
            @Override
            public void onSuccess(int code, String message) {
                Log.d(TAG, code + message);
                discoverCallback.success(code);
            }

            @Override
            public void onFailure(int code, String message) {
                Log.d(TAG, code + message);
                //todo change to error, error is not working now.
                discoverCallback.success(code);
            }
        });
    }

    public void stopWifiConfig() {
        easyLink.stopEasyLink(new EasyLinkCallBack() {
            @Override
            public void onSuccess(int code, String message) {
                Log.d(TAG, "stopEasyLink onSuccess: " + code + message);
            }

            @Override
            public void onFailure(int code, String message) {
                Log.d(TAG, "stopEasyLink onFailure: " + code + message);
            }
        });
    }
}
