"use strict";

module.exports = {
  // value must be an ArrayBuffer
  listen: function(success, error) {
    console.log("Initializing AndroidNotificationListener cordova plugin...");
    
    cordova.exec(success, error, "AndroidNotificationListenerPluginEntry", "listen", []);
  }
};
