package com.immichframe.immichframe.sensors;

import android.util.Log;

public class SensorPoller {
    public static boolean sleepModeActive = false;
    long durationBeforeSleep;
    protected boolean disabled = false;
    public long idleDuration = 0;

    private final HardwareSensor hardwareSensor;

    public SensorPoller(HardwareSensor hardwareSensor, int wakeLockMinutes) {
        this.hardwareSensor = hardwareSensor;
        updateSettings(wakeLockMinutes);
    }

    public boolean isSleepModeActive() {
        return sleepModeActive;
    }

    public void checkIfActivityDetected(SensorServiceCallback callback) {
        if (this.disabled || this.durationBeforeSleep < 1) {
            return;
        }
        if (!hardwareSensor.isActivityDetected()) {
            if (this.idleDuration % 30000 == 0) {
                Log.d("SensorPoller", String.format("No activity detected for %d minutes, already in sleep mode: %b", this.idleDuration / 1000 / 60, sleepModeActive));
            }
            this.idleDuration += 1000;

            boolean noMotionForFullDuration = this.idleDuration > this.durationBeforeSleep;
            if (noMotionForFullDuration) {
                if(!sleepModeActive) {
                    Log.d("SensorPoller", "-------Going to sleep--------");
                }
                callback.sleep();
                sleepModeActive = true;
                return;
            }
            return;
        }
        this.idleDuration = 0L;
        callback.wakeUp();
    }

    public synchronized void updateSettings(int wakeLockMinutes) {
        try {
            this.durationBeforeSleep = wakeLockMinutes * 60 * 1000L;
        } catch (Exception e) {
            Log.e("LogPoller", "Error updating settings", e);
        }
    }

    public void resetMotionSensor() {
        this.idleDuration = 0L;
        sleepModeActive = false;
    }
}
