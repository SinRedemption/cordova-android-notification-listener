"use strict";

module.exports = {
  // value must be an ArrayBuffer
  listen: function(success, failure) {
    console.log("初始化 AndroidNotificationListener");
    
    cordova.exec(success, failure, "com.skynet.member.cordova.plugins.android.notifications.listener.AndroidNotificationListenerPluginEntry", "listen", []);
  }
};
