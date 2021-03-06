# AndroidNotificationServiceListener plugin for Cordova

This is an implementation of the
[NotificationListenerService in Android](https://developer.android.com/reference/android/service/notification/NotificationListenerService.html)
for Cordova.

    A service that receives calls from the system when new notifications are posted or removed, or their ranking changed.

Note: This plugin doesn't work for IOS or Windows Phone, feel free to create a pull request if you want to add that functionality to this project.

## How to install

    cordova plugin add https://github.com/SinRedemption/cordova-android-notification-listener

## Enable notification listener service

This service requires an special permission that must be enabled from settings on Android (Settings > Notifications > Notification access)

Note: The app requires the following permission in your Manifest file on Android, which will be added automatically:

    android.permission.BIND_NOTIFICATION_LISTENER_SERVICE

## How to use

On Cordova initialization, add the callback for your notification-listener.
Then everytime you get a notification in your phone that callback in JS will be triggered with the notification data.

```
var app = {
    initialize: function() {
       console.log("Initializing app");
       this.bindEvents();
    },
    bindEvents: function() {
        console.log("Binding events");
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    onDeviceReady: function() {
       console.log("Device ready");

       window.androidNotificationListener.listen(function(n){
         console.log("Received notification " + JSON.stringify(n) );
       }, function(e){
         console.log("Notification Error " + e);
       });
    }
};
app.initialize();
```

And in the android activity, you have to do like this

```
// start service
startService(new Intent(this, NotificationListenerSvr.class));

// acquire service permission
NotificationListenerSvr.acquireService(this);
```

## Sample output
```
Received notification
{
  "title": "飞迈",
  "package": "com.ja.xman",
  "text": "Bla...Bla...",
  "textLines": ""
}
```

## Notification response format

The notification response received by Javascript is a simplified object from the
[StatusBarNotification class](https://developer.android.com/reference/android/service/notification/StatusBarNotification.html)
in Android.
