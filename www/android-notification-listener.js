"use strict";

module.exports = {
  // value must be an ArrayBuffer
  listen: function(success, failure) {
    console.log("Initializing AndroidNotificationListener cordova plugin...");
    
    cordova.exec(success, failure, "AndroidNotificationListenerPluginEntry", "listen", ['OK']);
  }
};
