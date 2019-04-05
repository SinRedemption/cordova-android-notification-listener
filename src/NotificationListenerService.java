package com.skynet.member.cordova.plugins.android.notifications.listener;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 安卓消息中心监听服务。注意，由于安卓本身的缓存限制，可能会导致监听代码无效，方法是重命名这个监听服务类。参见：
 * https://stackoverflow.com/questions/33530807/why-is-this-notificationlistenerservice-not-working
 *
 * Android
 * 消息监听服务参见：//http://developer.android.com/reference/android/service/notification/NotificationListenerService.html
 */
public class NotificationListenerService extends NotificationListenerService {
  private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

  private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
  private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

  private static final String TAG = NotificationListenerService.class.getSimpleName();

  private static final String PKG_WHITE_LIST = "com.google.android.googlequicksearchbox";

  private static List<StatusBarNotification> notifications;
  public static boolean enabled = false;
  private static Context context;

  @Override
  public void onCreate() {
    super.onCreate();

    enabled = true;
    context = this;
    notifications = new ArrayList<>();
  }

  @Override
  public void onDestroy() {
    Log.i(TAG, "onDestroy");

    enabled = false;
  }

  /**
   * 监听消息栏中的消息投递消息，即收到消息。
   */
  @Override
  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  public void onNotificationPosted(StatusBarNotification sbn) {
    Log.d(TAG, "notification package name " + sbn.getPackageName());

    String pk = sbn.getPackageName();
    if (pk.equals("android") || ignorePkg(pk) || sbn.isOngoing()) {
      Log.d(TAG, "Ignore notification for pkg: " + pk);

      return;
    }

    Bundle extras = sbn.getNotification().extras;
    final String title = extras.getString(NotificationCompat.EXTRA_TITLE);
    final String content = extras.getString(NotificationCompat.EXTRA_TEXT);

    if (title == null || content == null) return;

    AndroidNotificationListenerPluginEntry.notifyListener(sbn);
    // notifications.add(sbn);
  }

  /**
   * 过滤掉不需要的包。
   */
  private boolean ignorePkg(String pk) {
    for (String s : PKG_WHITE_LIST.split(",")) {
      if (pk.contains(s)) return true;
    }

    return false;
  }

  @Override
  public void onNotificationRemoved(StatusBarNotification sbn) {
    // debugNotification(sbn);
  }

  public static void removeAll() {
    try {
      for (StatusBarNotification n : notifications) {
        remove(n);
      }

      notifications.clear();
    } catch (Exception e) {
      Log.e(TAG, "Unable to remove notifications", e);
    }
  }

  private static void remove(StatusBarNotification n) {
    NotificationManager nMgr = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

    int id = n.getId();

    String tag = n.getTag();
    Log.i(TAG, tag + ", " + id);

    nMgr.cancel(tag, id);
  }

  /**
   * 通知服务授权。
   */
  public static void acquireService(Context context) {
    // 开启通知权限
    if (!isNotificationEnabled(context)) {
      Intent intent = new Intent(Settings.ACTION_SETTINGS);
      context.startActivity(intent);
    }

    // 消息监听是否已连接
    if (!isNotificationListenerConnected(context)) {
      context.startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    // 切换消息监听状态
    toggleNotificationListenerService(context);
  }

  /**
   * 判断通知是否已启用。
   *
   * @param context 上下文
   */
  @SuppressLint("NewApi")
  static boolean isNotificationEnabled(Context context) {
    AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
    ApplicationInfo appInfo = context.getApplicationInfo();
    String pkg = context.getApplicationContext().getPackageName();

    try {
      int uid = appInfo.uid;
      Class appOpsClass;

      appOpsClass = Class.forName(AppOpsManager.class.getName());
      Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);
      Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);

      int value = (Integer) opPostNotificationValue.get(Integer.class);
      return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
    } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | InvocationTargetException
        | IllegalAccessException e) {
      Log.e("NotificationMonitorSvr", e.getMessage());
    }

    return false;
  }

  /**
   * 切换通知消息监听状态。因为系统有个bug，需要开关一次。
   */
  static void toggleNotificationListenerService(Context context) {
    PackageManager pm = context.getPackageManager();
    ComponentName thisComponent = new ComponentName(context, NotificationListenerService.class);

    pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP);
    pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP);
  }

  /**
   * 判断当前应用是否被允许消息通知监听。
   */
  static boolean isNotificationListenerConnected(Context context) {
    return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.getPackageName());
  }
}
