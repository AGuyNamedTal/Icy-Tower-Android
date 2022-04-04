package com.talv.icytower.batteryChange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryChangeReceiver extends BroadcastReceiver {

    private final double batteryLow;
    public final BatteryChangeListener batteryChangeListener;

    private double previousBattery = -1;

    public BatteryChangeReceiver(double batterLow, BatteryChangeListener batteryChangeListener) {
        this.batteryLow = batterLow;
        this.batteryChangeListener = batteryChangeListener;
    }


    private double getBatteryPct(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (double) level / scale;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        double batteryPct = getBatteryPct(intent);
        if (batteryPct <= batteryLow && previousBattery > batteryLow) {
            batteryChangeListener.onBatteryLow(batteryPct);
        } else if (batteryPct > batteryLow && previousBattery <= batteryLow) {
            batteryChangeListener.onBatteryNotLow(batteryPct);
        }
        Log.d("battery", String.valueOf(batteryPct));
        previousBattery = batteryPct;
    }
}
