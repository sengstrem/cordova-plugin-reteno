import Cordova
import Cordova
import Cordova
import Cordova
import Cordova
import Cordova
import Cordova
import Cordova
import Cordova
import Cordova
import Cordova
import Foundation
import Reteno
import SwiftUI
import UserNotifications

@objc(RetenoPlugin) class RetenoPlugin: CDVPlugin {

    override func pluginInitialize() {

    }

    @objc(start:)
    func start(command: CDVInvokedUrlCommand) {

        print("RetenoPlugin: start")

        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK
        )

        let payload = command.arguments[0] as? [String: Any]

        let isDebugMode = payload?["isDebugMode"] as? Bool ?? false
        let apiKey = payload?["apiKey"] as? String ?? ""

        //let configuration: RetenoConfiguration = .init(isDebugMode: isDebugMode)
        //Reteno.start(apiKey: apiKey, configuration: configuration)

        let configuration: RetenoConfiguration = .init(isAutomaticScreenReportingEnabled: true, isAutomaticAppLifecycleReportingEnabled: true, isDebugMode: isDebugMode)

        Reteno.delayedSetup(apiKey: apiKey, configuration: configuration)


        self.commandDelegate!.send(
            pluginResult,
            callbackId: command.callbackId
        )

    }

    @objc(logEvent:)
    func logEvent(command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR
        )
        self.commandDelegate.run(inBackground: { [self] in

            pluginResult = logEventAsync(command: command)

            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            )

        })


    }

    func logEventAsync(command: CDVInvokedUrlCommand) -> CDVPluginResult {
        var pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR
        )
        let payload = command.arguments[0] as? NSDictionary ?? NSDictionary()
        var res = true
        do {
            let requestPayload = try RetenoEvent.buildEventPayload(payload: payload)
            Reteno.logEvent(
                eventTypeKey: requestPayload.eventName,
                date: requestPayload.date,
                parameters: requestPayload.parameters,
                forcePush: requestPayload.forcePush
            )
        } catch {
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: error.localizedDescription
            )
            res = false
        }

        if res {
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK
            )
        }
        return pluginResult ?? CDVPluginResult(status: CDVCommandStatus_ERROR)
    }

    @objc(setUserAttributes:)
    func setUserAttributes(command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR
        )
        self.commandDelegate.run(inBackground: { [self] in

            pluginResult = setUserAttributesAsync(command: command)

            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            )

        })



    }

    func setUserAttributesAsync(command: CDVInvokedUrlCommand) -> CDVPluginResult {
        var pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR
        )
        let payload = command.arguments[0] as? NSDictionary ?? NSDictionary()

        let externalUserId = payload["externalUserId"] as? String
        var res = true

        do {
            let requestPayload = try RetenoUserAttributes.buildSetUserAttributesPayload( payload: payload)

            Reteno.updateUserAttributes(
                externalUserId: externalUserId,
                userAttributes: requestPayload.userAttributes,
                subscriptionKeys: requestPayload.subscriptionKeys,
                groupNamesInclude: requestPayload.groupNamesInclude,
                groupNamesExclude: requestPayload.groupNamesExclude
            )

        } catch {

            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: error.localizedDescription
            )
            res = false

        }

        if res {
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK
            )
        }
        self.commandDelegate!.send(
            pluginResult,
            callbackId: command.callbackId
        )
        return pluginResult ?? CDVPluginResult(status: CDVCommandStatus_ERROR)
    }

    @objc(setAnonymousUserAttributes:)
    func setAnonymousUserAttributes(command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR
        )
        self.commandDelegate.run(inBackground: { [self] in

            pluginResult = setAnonymousUserAttributesAsync(command: command)

            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            )

        })


    }

    func setAnonymousUserAttributesAsync(command: CDVInvokedUrlCommand) -> CDVPluginResult {
        var pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR
        )
        let payload = command.arguments[0] as? NSDictionary ?? NSDictionary()

        var res = true

        do {
            let userAttributes = try RetenoUserAttributes.buildAnonymousUserAttributesPayload(
                payload: payload)
            Reteno.updateAnonymousUserAttributes(userAttributes: userAttributes)

        } catch {
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: error.localizedDescription
            )
            res = false
        }

        if res {
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK
            )
        }
        self.commandDelegate!.send(
            pluginResult,
            callbackId: command.callbackId
        )
        return pluginResult ?? CDVPluginResult(status: CDVCommandStatus_ERROR)
    }

    @objc(getInitialNotification:)
    func getInitialNotification(command: CDVInvokedUrlCommand) {
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR
        )

        self.commandDelegate!.send(
            pluginResult,
            callbackId: command.callbackId
        )
    }

    @objc(performRemoteToken:)
    func performRemoteToken(command: CDVInvokedUrlCommand) {
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR
        )
        let fcmToken = command.arguments[0] as? String ?? ""
        Reteno.userNotificationService.processRemoteNotificationsToken(fcmToken)
        self.commandDelegate!.send(
            pluginResult,
            callbackId: command.callbackId
        )
    }

    @objc(setOnRetenoPushReceivedListener:)
    func setOnRetenoPushReceivedListener(command: CDVInvokedUrlCommand) {
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR
        )

        self.commandDelegate!.send(
            pluginResult,
            callbackId: command.callbackId
        )
    }

    @objc(requestPermission:)
    func requestPermission(command: CDVInvokedUrlCommand) {


        // Register for receiving push notifications
        // registerForRemoteNotifications will show the native iOS notification permission prompt
        // Provide UNAuthorizationOptions or use default
        Reteno.userNotificationService.registerForRemoteNotifications(with: [.sound, .alert, .badge]) { granted in

            print("Request Permission: " + (granted ? "TRUE" : "FALSE"));

            // granted == true if user allowed receiving Remote notifications
            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: granted
            )

            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            )

        }

    }

    var notificationClickedCallbackId: String = "";
    var notificationWillShowInForegoundCallbackId: String = "";

    @objc(addNotificationClickListener:)
    func addNotificationClickListener(command: CDVInvokedUrlCommand) {

        print("RetenoPlugin: addNotificationClickListener")

        let handlerNotSet = self.notificationClickedCallbackId == "";

        self.notificationClickedCallbackId = command.callbackId;

        if (handlerNotSet) {
            Reteno.userNotificationService.didReceiveNotificationResponseHandler = { [self] response in

                print("Reteno Plugin didReceiveNotificationResponseHandler");
                print(response.notification);
                print("response.actionIdentifier");
                print(response.actionIdentifier);

                let notification = response.notification;

                var payload: [String: Any] = [
                    "id": notification.request.identifier,
                    "title": notification.request.content.title,
                    "body": notification.request.content.body,
                    "subtitle": notification.request.content.subtitle,
                    "badge": notification.request.content.badge ?? 0,
                    "sound": notification.request.content.sound?.description ?? "",
                    "launchImageName": notification.request.content.launchImageName
                ]




                if let userInfo = notification.request.content.userInfo as? [String: Any] {
                    payload["userInfo"] = userInfo
                }

//                response.actionIdentifier


                let action = response.actionIdentifier != UNNotificationDefaultActionIdentifier ? response.actionIdentifier : "";

                let notificationClickResponse = [
                    "result" : [
                        "actionId": action,
                        "url": ""
                    ],
                    "notification": payload
                ];

                let pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: notificationClickResponse
                )
                pluginResult?.setKeepCallbackAs(true)

                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: self.notificationClickedCallbackId
                )

            }
        }

    }


    @objc(addForegroundLifecycleListener:)
    func addForegroundLifecycleListener(command: CDVInvokedUrlCommand) {

        let handlerNotSet = self.notificationWillShowInForegoundCallbackId == "";
        self.notificationWillShowInForegoundCallbackId = command.callbackId;
        if (handlerNotSet) {
            Reteno.userNotificationService.willPresentNotificationHandler = { notification in

                print("Reteno Plugin addForegroundLifecycleListener");
                print(notification.request.content.title)
                print(self.notificationWillShowInForegoundCallbackId);

                var payload: [String: Any] = [
                    "id": notification.request.identifier,
                    "title": notification.request.content.title,
                    "body": notification.request.content.body,
                    "subtitle": notification.request.content.subtitle,
                    "badge": notification.request.content.badge ?? 0,
                    "sound": notification.request.content.sound?.description ?? "",
                    "launchImageName": notification.request.content.launchImageName
                ]


                if let userInfo = notification.request.content.userInfo as? [String: Any] {
                    payload["userInfo"] = userInfo
                }

                let pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: payload
                )

                pluginResult?.setKeepCallbackAs(true)

                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: self.notificationWillShowInForegoundCallbackId
                )

                let authOptions: UNNotificationPresentationOptions
                if #available(iOS 14.0, *) {
                    authOptions = [.badge, .sound, .banner]
                } else {
                    authOptions = [.badge, .sound, .alert]
                }

//                 return [];

                return authOptions

            }
        }

    }

    @objc(proceedWithWillDisplay:)
    func proceedWithWillDisplay(command: CDVInvokedUrlCommand) {



    }


}
