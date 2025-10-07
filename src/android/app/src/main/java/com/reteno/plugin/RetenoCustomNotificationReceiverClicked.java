package com.reteno.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class RetenoCustomNotificationReceiverClicked extends BroadcastReceiver {

    private static final String TAG = "RetenoPlugin";

    @Override
    public void onReceive(Context context, Intent intent) {

//        dumpIntent(TAG, intent);

        if(RetenoPlugin.pluginInited){
            RetenoPlugin.sendCallback(intent);
        }else{
            RetenoPlugin.initialPush = intent;
        }

    }

    static void dumpIntent(String tag, Intent i) {
        if (i == null) { Log.d(tag, "Intent=null"); return; }
        Log.d(tag, "---- INTENT ----");
        Log.d(tag, "action=" + i.getAction());
        Log.d(tag, "data=" + i.getDataString());
        Log.d(tag, "component=" + i.getComponent());
        Log.d(tag, "categories=" + i.getCategories());
        Log.d(tag, "flags=0x" + Integer.toHexString(i.getFlags()));

        // На случай кастомных parcelables
//        i.setExtrasClassLoader(PushReceiver.class.getClassLoader());

        Bundle b = i.getExtras();
        if (b == null) { Log.d(tag, "extras=null"); return; }

        for (String k : b.keySet()) {
            Object v = b.get(k);
            String val;
            if (v instanceof Bundle)               val = "Bundle" + bundleToString((Bundle) v);
            else if (v instanceof String[])        val = java.util.Arrays.toString((String[]) v);
            else if (v instanceof int[])           val = java.util.Arrays.toString((int[]) v);
            else if (v instanceof java.util.ArrayList) val = "ArrayList(size=" + ((java.util.ArrayList<?>) v).size() + ")";
            else if (v == null)                    val = "null";
            else                                   val = v + " (" + v.getClass().getSimpleName() + ")";
            Log.d(tag, "extra[" + k + "] = " + val);
        }
    }

    static String bundleToString(Bundle b) {
        if (b == null) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (String k : b.keySet()) {
            if (!first) sb.append(", ");
            Object v = b.get(k);
            sb.append(k).append("=");
            sb.append(v instanceof Bundle ? bundleToString((Bundle) v) : String.valueOf(v));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

}
