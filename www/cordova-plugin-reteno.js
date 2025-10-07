/*
didReceiveNotificationResponseHandler
willPresentNotificationHandler


Reteno.userNotificationService.didReceiveNotificationResponseHandler = { [weak self] response in
            switch response.actionIdentifier {
            case UNNotificationDefaultActionIdentifier:
                print("Default action")

            case UNNotificationDismissActionIdentifier:
                print("Dismiss action")

            default:
                print(response.actionIdentifier)
            }

            let alert = Informati



func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {


Reteno.userNotificationService.processRemoteNotificationsToken(token)

 */

var exec = require("cordova/exec");

var PLUGIN_NAME = "RetenoPlugin";

var onApplicationDidBecomeActiveCallback = function () {};
var onApplicationDidEnterBackgroundCallback = function () {};

/***********************
 * Protected internals
 ***********************/
// iOS only
exports._applicationDidBecomeActive = function () {
    onApplicationDidBecomeActiveCallback();
};

exports._applicationDidEnterBackground = function () {
    onApplicationDidEnterBackgroundCallback();
};

/**
 * @deprecated this method of Reteno initialization is deprecated, becuase it led to incorrect initializations of native libraries,
 * please refer to new documentation https://docs.reteno.com/reference/cordova-sdk-setup, IOS and Android SDK Setup sections
 */
exports.setApiKey = function (arg0, success, error) {};


exports.start = function (arg0, success, error) {

    return new Promise((resolve, reject) => {

        exec(resolve, reject, PLUGIN_NAME, "start", [arg0]);

    });


};

exports.logEvent = function (arg0, success, error) {

    return new Promise((resolve, reject) => {

        exec(resolve, reject, PLUGIN_NAME, "logEvent", [arg0]);

    });

};

exports.setUserAttributes = function (arg0, success, error) {
    exec(success, error, PLUGIN_NAME, "setUserAttributes", [arg0]);
};

exports.setAnonymousUserAttributes = function (arg0, success, error) {
    exec(success, error, PLUGIN_NAME, "setAnonymousUserAttributes", [arg0]);
};

exports.getInitialNotification = function (arg0, success, error) {
    exec(success, error, PLUGIN_NAME, "getInitialNotification", [arg0]);
};

exports.performRemoteToken = function (arg0, success, error) {
    exec(success, error, PLUGIN_NAME, "performRemoteToken", [arg0]);
};

exports.setOnRetenoPushReceivedListener = function (success, error) {
    exec(success, error, PLUGIN_NAME, "setOnRetenoPushReceivedListener", []);
};

exports.setDeviceToken = function (arg0, success, error) {
    exec(success, error, PLUGIN_NAME, "setDeviceToken", [arg0]);
};
