package com.immichframe.immichframe.sensors;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.kitesystems.nix.frame.MotionSensor;

import java.io.File;


public class ActivitySensor {
    private final SensorPoller instance;
    private final PowerUtils powerUtils;

    public ActivitySensor(Context context, int activitySensorTimeout) {
        powerUtils = new PowerUtils(context);
        if(new File("/etc/nix.model").exists()) {
            instance = new SensorPoller(new MotionSensor(), activitySensorTimeout);
        } else {
            instance = new SensorPoller(() -> true, activitySensorTimeout);
        }
    }

    SensorServiceCallback sensorServiceCallback = new SensorServiceCallback() {
        @Override
        public void sleep() {
            powerUtils.goToSleep();
        }

        @Override
        public void wakeUp() {
            instance.resetMotionSensor();
            powerUtils.wakeUp();
        }
    };

    public void checkSensors() {
        try {
            Thread.currentThread().setPriority(10);
            instance.checkIfActivityDetected(sensorServiceCallback);
        } catch (SecurityException e2) {
            Log.e("ImmichFrame", e2.toString());
        }
    }

    public void powerButtonPressed() {
        if(instance.isSleepModeActive()) {
            instance.resetMotionSensor();
            powerUtils.wakeUp();
        } else {
            powerUtils.goToSleep();
        }
    }

    public void resetMotionSensor() {
        instance.resetMotionSensor();
    }
}
