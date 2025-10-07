import {NotificationClickEvent, NotificationEventName, NotificationEventTypeMap} from "./types/NotificationEvents";
import {NotificationWillDisplayEvent} from "./NotificationReceived";
import {OSNotification} from "./OSNotification";
import {PLUGIN_NAME} from "./index";

export default class Notifications
{

    private _permissionObserverList: ((event: boolean) => void)[] = [];
    private _notificationClickedListeners: ((
        event: NotificationClickEvent,
    ) => void)[] = [];
    private _notificationWillDisplayListeners: ((
        event: NotificationWillDisplayEvent,
    ) => void)[] = [];

    private _processFunctionList<T>(
        array: ((event: T) => void)[],
        param: T,
    ): void {
        for (let i = 0; i < array.length; i++) {
            array[i](param);
        }
    }

    // private _permission?: boolean;

    requestPermission(fallbackToSettings?: boolean): Promise<boolean> {

        let fallback = fallbackToSettings ?? false;

        return new Promise<boolean>((resolve, reject) => {
            window.cordova.exec(
                resolve,
                reject,
                PLUGIN_NAME,
                "requestPermission",
                [fallback],
            );
        });

    }


    addEventListener<K extends NotificationEventName>(
        event: K,
        listener: (event: NotificationEventTypeMap[K]) => void,
    ): void {
        if (event === "click") {
            this._notificationClickedListeners.push(
                listener as (event: NotificationClickEvent) => void,
            );
            const clickParsingHandler = (json: NotificationClickEvent) => {

                console.log('RETENO PLUGIN: CLICK EVENT', json);

                this._processFunctionList(this._notificationClickedListeners, json);
            };
            window.cordova.exec(
                clickParsingHandler,
                function () {},
                PLUGIN_NAME,
                "addNotificationClickListener",
                [],
            );
        } else if (event === "foregroundWillDisplay") {
            this._notificationWillDisplayListeners.push(
                listener as (event: NotificationWillDisplayEvent) => void,
            );
            const foregroundParsingHandler = (notification: OSNotification) => {

                console.log('RETENO PLUGIN: FOREGROUND WILL DISPLAY EVENT', notification);

                this._notificationWillDisplayListeners.forEach((listener) => {
                    listener(new NotificationWillDisplayEvent(notification));
                });
                window.cordova.exec(
                    function () {},
                    function () {},
                    PLUGIN_NAME,
                    "proceedWithWillDisplay",
                    [notification.notificationId],
                );
            };
            window.cordova.exec(
                foregroundParsingHandler,
                function () {},
                PLUGIN_NAME,
                "addForegroundLifecycleListener",
                [],
            );
        } else if (event === "permissionChange") {
            this._permissionObserverList.push(listener as (event: boolean) => void);
            const permissionCallBackProcessor = (state: boolean) => {
                this._processFunctionList(this._permissionObserverList, state);
            };
            window.cordova.exec(
                permissionCallBackProcessor,
                function () {},
                PLUGIN_NAME,
                "addPermissionObserver",
                [],
            );
        } else {
            return;
        }
    }

}
