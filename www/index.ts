declare global {
    interface Window {
        plugins?: {
            Reteno?: import("./index").RetenoPlugin;
        };
    }
}

import { LogEvent, SetUserAttributesPayload, AnonymousUserAttributes } from './types/index';

import Notifications from "./Notifications";

export {
    LogEvent,
    SetUserAttributesPayload,
    AnonymousUserAttributes
} from './types/index';

export const PLUGIN_NAME = 'RetenoPlugin';

export class RetenoPlugin{

    private _apiKey: string = "";

    Notifications: Notifications = new Notifications();

    initialize(apiKey: string, isDebugMode: boolean = false): Promise<boolean> {

        this._apiKey = apiKey;

        return new Promise<boolean>((resolve, reject) => {

            window.cordova.exec(
                resolve,
                reject,
                PLUGIN_NAME,
                "start",
                [{
                    apiKey: this._apiKey,
                    isDebugMode: isDebugMode
                }],
            );

        });

    }

    logEvent(payload: LogEvent): Promise<boolean> {

        return new Promise<boolean>((resolve, reject) => {

            window.cordova.exec(
                resolve,
                reject,
                PLUGIN_NAME,
                "logEvent",
                [payload],
            );

        });

    }


    setUserAttributes(payload: SetUserAttributesPayload): Promise<boolean> {

        return new Promise<boolean>((resolve, reject) => {

            window.cordova.exec(
                resolve,
                reject,
                PLUGIN_NAME,
                "setUserAttributes",
                [payload],
            );

        });

    }

    setAnonymousUserAttributes(payload: AnonymousUserAttributes): Promise<boolean> {

        return new Promise<boolean>((resolve, reject) => {

            window.cordova.exec(
                resolve,
                reject,
                PLUGIN_NAME,
                "setAnonymousUserAttributes",
                [payload],
            );

        });

    }


}


const Reteno = new RetenoPlugin();

if (!window.plugins) {
    window.plugins = {};
}
if (!window.plugins.Reteno) {
    window.plugins.Reteno = Reteno;
}


export default Reteno;
