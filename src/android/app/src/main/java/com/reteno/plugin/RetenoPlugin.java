package com.reteno.plugin;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.reteno.core.Reteno;
import com.reteno.core.RetenoApplication;
import com.reteno.core.RetenoConfig;
import com.reteno.core.RetenoInternalImpl;

import com.reteno.core.domain.model.event.Event;
import com.reteno.core.domain.model.user.User;
import com.reteno.core.domain.model.user.UserAttributesAnonymous;
import com.reteno.push.Constants;
import com.reteno.push.RetenoNotifications;
import com.reteno.push.permission.NotificationStatus;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
//import org.apache.cordova.firebase.FirebasePlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

public class RetenoPlugin extends CordovaPlugin {
    protected static final String TAG = "RetenoPlugin";


    public static Boolean pluginInited = false;
    public static Intent initialPush = null;

    public static ConcurrentLinkedQueue<CallbackContext> clickCallbackContexts = new ConcurrentLinkedQueue<>();

    private static java.lang.ref.WeakReference<RetenoPlugin> sInstance = new java.lang.ref.WeakReference<>(null);

    private Reteno retenoInstance;

    public RetenoPlugin(){



    }

    @Override
    public void pluginInitialize() {

        pluginInited = true;

        sInstance = new java.lang.ref.WeakReference<>(this);

    }

    public Reteno getRetenoInstance() {

        return Reteno.getInstance();

    }


    public static void sendCallback(Intent intent) {

        RetenoPlugin plugin = sInstance.get();

        clickCallbackContexts.forEach(iterContext -> {

            //iterContext.sendPluginResult();

            plugin.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {

                    //iterContext.sendPluginResult(logEvent(args));

                    JSONObject notificationData = buildNotificationPayloadFromIntent(intent);


                    JSONObject payload = new JSONObject();

                    try {

                        payload.put("notification", notificationData);

                        payload.put(
                            "result",
                            new JSONObject()
                                .put("actionId", notificationData.get("actionId"))
                        );

                    }catch (org.json.JSONException ignore) {}

                    PluginResult pr = new PluginResult(PluginResult.Status.OK, payload);
                    pr.setKeepCallback(true);
                    iterContext.sendPluginResult(pr);

                }
            });

        });

    }

    @Override
    public boolean execute(
        String action, JSONArray args, CallbackContext callbackContext
    ) throws JSONException {

        if ("logEvent".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    callbackContext.sendPluginResult(logEvent(args));
                }
            });
            return true;
        }
        if ("setUserAttributes".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    callbackContext.sendPluginResult(createUser(args));
                }
            });
            return true;
        }
        if ("setAnonymousUserAttributes".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    callbackContext.sendPluginResult(createAnonymousUser(args));
                }
            });
            return true;
        }
        if ("getInitialNotification".equals(action)) {
            echo(action + "\n" + args.toString(), callbackContext);
            return true;
        }
        if ("setDeviceToken".equals(action)) {
            setDeviceToken(args, callbackContext);
            return true;
        }
        if ("performRemoteToken".equals(action)) {
            echo(action + "\n" + args.toString(), callbackContext);
            return true;
        }
        if ("start".equals(action)) {

            JSONObject payload = (JSONObject) args.get(0);

            String apiKey = RetenoUtil.getStringOrNull(payload, "apiKey");
            Boolean isDebugMode = Boolean.parseBoolean(RetenoUtil.getStringOrNull(payload, "isDebugMode"));


            if(apiKey != null) {

                cordova.getActivity().runOnUiThread(() -> {

                    Reteno.initWithConfig(
                        new RetenoConfig.Builder()
                            .accessKey(apiKey)
                            .setDebug(isDebugMode)
                            .build()
                    );

                    cordova.getThreadPool().execute(new Runnable() {
                        @Override
                        public void run() {

                            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));

                        }
                    });

                });





            }else{

                cordova.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {

                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "No apiKey was set."));

                    }
                });

            }

            return true;
        }

        if ("requestPermission".equals(action)) {

            Log.e("RetenoPlugin", "PERMISSION: REQUEST 1...");

            boolean fallbackToSettings = args.getBoolean(0);

            try {
                Future<NotificationStatus> status = RetenoNotifications.getNotificationPermissionStatusFuture();
                NotificationStatus value = status.get();
                switch (value) {
                    case DENIED:
                        Log.e("RetenoPlugin", "PERMISSION: DENIED");
                        //Permission denied but can be requested again
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
                        break;
                    case ALLOWED:
                        //Permission allowed
                        Log.e("RetenoPlugin", "PERMISSION: ALLOWED");
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
                        break;
                    case PERMANENTLY_DENIED:
                        //Permission permanently denied. Show navigate to settings dialog
                        Log.e("RetenoPlugin", "PERMISSION: PERMANENTLY_DENIED");
                        if(fallbackToSettings){
                            openNotificationSettings(callbackContext);
                        }else{
                            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
                        }
                        break;
                    default: break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
            }


            return true;

        }


        if("addNotificationClickListener".equals(action)){

            clickCallbackContexts.add(callbackContext);

            if(initialPush != null){
                sendCallback(initialPush);
            }

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {

                    PluginResult pr = new PluginResult(PluginResult.Status.NO_RESULT);
                    pr.setKeepCallback(true);
                    callbackContext.sendPluginResult(pr);

                }
            });

            return true;

        }

        return false;
    }

    private void openNotificationSettings(CallbackContext callbackContext) {
        try {
            Intent intent = new Intent();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ - настройки уведомлений приложения
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, cordova.getActivity().getPackageName());
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Android 5.0+ - настройки приложения
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + cordova.getActivity().getPackageName()));
            } else {
                // Старые версии Android
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + cordova.getActivity().getPackageName()));
            }

            cordova.getActivity().startActivity(intent);
            callbackContext.success("Настройки уведомлений открыты");

        } catch (Exception e) {
            callbackContext.error("Ошибка при открытии настроек: " + e.getMessage());
        }
    }

    private void echo(String msg, CallbackContext callbackContext) {
        if (msg == null || msg.length() == 0) {
            callbackContext.error("Empty message!");
        } else {
            Toast.makeText(webView.getContext(), msg, Toast.LENGTH_LONG)
                .show();
            callbackContext.success(msg);
        }
    }

    private PluginResult logEvent(JSONArray args) {
        if (args == null || args.length() == 0) {
            return new PluginResult(PluginResult.Status.ERROR, "Empty event");
        } else {
            try {
                Event event = RetenoEvent.buildEventFromPayload((JSONObject) args.get(0));

                Reteno.getInstance().logEvent(event);
                Log.d("RetenoPlugin", "TRIGGERED LOG EVENT");
//                getRetenoInstance().logEvent(event);
            } catch (Exception e) {
                Log.e("RetenoPlugin", e.getLocalizedMessage(), e);
                return new PluginResult(PluginResult.Status.ERROR, ("Reteno Android SDK Error " + e.getLocalizedMessage()));
            }
            return new PluginResult(PluginResult.Status.OK);
        }
    }

    public void setDeviceToken(JSONArray args, CallbackContext callbackContext) {

    }

    public PluginResult createUser(JSONArray args) {
        try {
            JSONObject payload = (JSONObject) args.get(0);
            String externalId = payload.getString("externalUserId");
            JSONObject userJSON = payload.getJSONObject("user");
            User user = RetenoUserAttributes.buildUserFromPayload(userJSON);
            //getRetenoInstance().setUserAttributes(externalId, user);
            Reteno.getInstance().setUserAttributes(externalId, user);

            Log.d("RetenoPlugin", "createUser");
            Log.d("RetenoPlugin", externalId);
            Log.d("RetenoPlugin", user.toString());
        } catch (Exception e) {
            Log.e("RetenoPlugin", e.getLocalizedMessage(), e);
            return new PluginResult(PluginResult.Status.ERROR, "Reteno Android SDK Error: " + e.getLocalizedMessage());
        }
        return new PluginResult(PluginResult.Status.OK);
    }

    public PluginResult createAnonymousUser(JSONArray args) {
        try {
            JSONObject payload = (JSONObject) args.get(0);
            JSONObject userJSON = payload.getJSONObject("user");
            UserAttributesAnonymous userAttributes = RetenoUserAttributes.buildAnonymousUserFromPayload(userJSON);
            getRetenoInstance().setAnonymousUserAttributes(userAttributes);
        } catch (Exception e) {
            Log.e("RetenoPlugin", e.getLocalizedMessage(), e);
            return new PluginResult(PluginResult.Status.ERROR, "Reteno Android SDK Error: " + e.getLocalizedMessage());
        }
        return new PluginResult(PluginResult.Status.OK);
    }

    static Map<String, String> bundleToMap(Bundle extras) {
        Map<String, String> map = new HashMap<String, String>();

        Set<String> ks = extras.keySet();
        Iterator<String> iterator = ks.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            map.put(key, extras.getString(key));
        }
        return map;
    }

    public void getInitialNotification(CallbackContext callbackContext) {
        Activity activity = this.cordova.getActivity();
        if (activity == null) {
            callbackContext.error("No activity");
            return;
        }
    }



    private static org.json.JSONObject buildNotificationPayloadFromIntent(android.content.Intent i) {
        org.json.JSONObject p = new org.json.JSONObject();
        try {
            // id сообщения
            putString(p, "id", firstNonEmpty(i,
                "google.message_id", "es_interaction_id", "message_id", "m_id", "id"));


            // заголовок
            putString(p, "title", firstNonEmpty(i,
                "title", "es_title", "gcm.notification.title", "notification_title",
                "android.title", "content_title"));

            // текст/тело
            putString(p, "body", firstNonEmpty(i,
                "body", "es_content", "gcm.notification.body", "message", "alert",
                "android.text", "content_text"));

            // подзаголовок (в Android это чаще subText — может отсутствовать)
            putString(p, "subtitle", firstNonEmpty(i,
                "subtitle", "es_subtitle", "gcm.notification.subtitle", "sub_text", "android.subText"));

            // бейдж (в Android не стандартизован — если нет, будет 0)
            putInt(p, "badge", firstInt(i, 0, "badge", "gcm.notification.badge", "notification_count"));

            // звук
            putString(p, "sound", firstNonEmpty(i,
                "sound", "gcm.notification.sound2", "gcm.notification.sound"));

            // аналог launchImageName (в Android прямого аналога нет,
            // но иногда присылают image/picture/large_icon)
            putString(p, "launchImageName", firstNonEmpty(i,
                "launchImageName", "es_notification_image", "image", "gcm.notification.image", "picture", "large_icon"));

            putString(p, "bigPicture", firstNonEmpty(i,
                                        "launchImageName", "es_notification_image", "image", "gcm.notification.image", "picture", "large_icon"));

            putString(p,"actionId", firstNonEmpty(i, "es_btn_action_id"));

            // необязательно, но полезно:
            putString(p, "action", i.getAction());
            p.put("extras", bundleToJson(i.getExtras())); // все оригинальные extras
            p.put("additionalData", bundleToJson(i.getExtras()));
        } catch (org.json.JSONException ignore) { }
        return p;
    }

    // ---------- helpers ----------
    private static String firstNonEmpty(android.content.Intent i, String... keys) {
        if (i == null || i.getExtras() == null) return "";
        android.os.Bundle b = i.getExtras();
        for (String k : keys) {
            if (b.containsKey(k)) {
                Object v = b.get(k);
                if (v != null) {
                    String s = String.valueOf(v);
                    if (!s.isEmpty()) return s;
                }
            }
        }
        return "";
    }

    private static int firstInt(android.content.Intent i, int def, String... keys) {
        if (i == null || i.getExtras() == null) return def;
        android.os.Bundle b = i.getExtras();
        for (String k : keys) {
            if (!b.containsKey(k)) continue;
            Object v = b.get(k);
            if (v instanceof Number) return ((Number) v).intValue();
            if (v != null) {
                try { return Integer.parseInt(String.valueOf(v)); } catch (Exception ignored) {}
            }
        }
        return def;
    }

    private static void putString(org.json.JSONObject o, String k, String v) throws org.json.JSONException {
        o.put(k, v == null ? "" : v);
    }
    private static void putInt(org.json.JSONObject o, String k, int v) throws org.json.JSONException {
        o.put(k, v);
    }

    private static org.json.JSONObject bundleToJson(android.os.Bundle b) {
        org.json.JSONObject j = new org.json.JSONObject();
        if (b == null) return j;
        for (String k : b.keySet()) {
            Object v = b.get(k);
            try {
                if (v instanceof android.os.Bundle) {
                    j.put(k, bundleToJson((android.os.Bundle) v));
                } else if (v != null) {
                    j.put(k, String.valueOf(v)); // безопасно приводим к строке
                } else {
                    j.put(k, org.json.JSONObject.NULL);
                }
            } catch (org.json.JSONException ignore) { }
        }
        return j;
    }

}
