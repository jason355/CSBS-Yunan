package com.hfad.swbs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class BootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            Intent serviceIntent = new Intent(context, MyForegroundWebsocketService.class);
            context.startForegroundService(serviceIntent);
            serviceIntent = new Intent(context, MyForegroundTimerService.class);
            context.startService(serviceIntent);
        }
    }
}