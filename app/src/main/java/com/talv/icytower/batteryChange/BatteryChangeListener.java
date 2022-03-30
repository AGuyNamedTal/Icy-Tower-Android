package com.talv.icytower.batteryChange;

public interface BatteryChangeListener {
    void onBatteryLow(double battery);
    void onBatteryNotLow(double battery);

}
