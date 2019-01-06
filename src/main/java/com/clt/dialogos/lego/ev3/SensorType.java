package com.clt.dialogos.lego.ev3;

public enum SensorType {
    NONE,
    TOUCH,
    LIGHT,
    SOUND,
    ULTRASONIC;

    @Override
    public String toString() {

        return Resources.getString("SENSORTYPE_" + this.name());
    }
}
