package com.example.valdizz.busstation.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ReminderBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            //Set reminders after startup

        }
    }
}
