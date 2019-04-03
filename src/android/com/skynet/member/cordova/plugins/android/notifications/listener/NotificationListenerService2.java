package com.skynet.member.cordova.plugins.android.notifications.listener;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import org.apache.cordova.PluginResult;

import android.service.notification.StatusBarNotification;
import android.os.Bundle;

/**
 * Cordova 插件入口。
 */
public class NotificationListenerService2 extends CordovaPlugin {
  private static final String TAG = AndroidNotificationListenerPluginEntry.class.getSimpleName();
  private static final String LISTEN = "listen";

  private static CallbackContext listener;

  private boolean isPaused;

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    Log.i(TAG, "收到动作: " + action);

    if (LISTEN.equals(action)) {
      setListener(callbackContext);
      return true;
    } else {
      callbackContext.error(TAG + ". " + action + " is not a supported function.");
      return false;
    }
  }

  @Override
  public void onPause(boolean multitasking) {
    this.isPaused = true;
  }

  @Override
  public void onResume(boolean multitasking) {
    this.isPaused = false;
  }

  /**
   * 初始化监听器。
   */
  public void setListener(CallbackContext callbackContext) {
    Log.i(TAG, "Attaching callback context listener " + callbackContext);
    listener = callbackContext;

    PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
    result.setKeepCallback(true);
    callbackContext.sendPluginResult(result);
  }

  /**
   * 调用JS通知。
   */
  static void notifyListener(StatusBarNotification n) {
    try {
      if (listener == null) {
        Log.e(TAG, "You must define listener first in javascript. Call window.androidNotificationListener.listen(success, error) first");
        return;
      }

      JSONObject json = parse(n);
      PluginResult result = new PluginResult(PluginResult.Status.OK, json);

      Log.i(TAG, "发送通知给JS监听: " + json.toString());
      result.setKeepCallback(true);

      listener.sendPluginResult(result);
    } catch (Exception e) {
      Log.e(TAG, "发送通知给JS监听失败: " + e);

      listener.error(TAG + ". 发送通知给JS监听失败: " + e.getMessage());
    }
  }

  private static JSONObject parse(StatusBarNotification n) throws JSONException {
    JSONObject json = new JSONObject();

    Bundle extras = n.getNotification().extras;

    json.put("title", getExtra(extras, "android.title"));
    json.put("package", n.getPackageName());
    json.put("text", getExtra(extras, "android.text"));
    json.put("textLines", getExtraLines(extras, "android.textLines"));

    return json;
  }

  private static String getExtraLines(Bundle extras, String extra) {
    try {
      CharSequence[] lines = extras.getCharSequenceArray(extra);
      return lines[lines.length - 1].toString();
    } catch (Exception e) {
      Log.d(TAG, "Unable to get extra lines " + extra);
      return "";
    }
  }

  private static String getExtra(Bundle extras, String extra) {
    try {
      return extras.get(extra).toString();
    } catch (Exception e) {
      return "";
    }
  }
}
