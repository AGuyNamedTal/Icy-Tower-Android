package com.talv.icytower.smsListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private final SmsListener smsListener;

    public SmsBroadcastReceiver(SmsListener smsListener) {
        this.smsListener = smsListener;
    }

    public SmsBroadcastReceiver() {
        smsListener = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            if (Telephony.Sms.Intents.getMessagesFromIntent(intent).length > 0) {
                if (smsListener != null)
                    smsListener.onReceive();
            }
        }
    }
}
