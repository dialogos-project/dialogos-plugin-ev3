package com.clt.dialogos.lego.ev3;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.sensor.Mode;
import elmot.javabrick.ev3.sensor.RawOnlySensorFactory;
import elmot.javabrick.ev3.sensor.SensorFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public enum SensorType {
    NONE(brick -> null),
    TOUCH(brick -> brick.TOUCH),
    COLOR(brick -> brick.COLOR),
    SOUND(brick -> brick.SOUND),
    ULTRASONIC(brick -> brick.ULTRASONIC);
    
    // TODO add other sensors
    
    private final Function<EV3,SensorFactory> sensor;

    private SensorType( Function<EV3, SensorFactory> sensor) {
        this.sensor = sensor;
    }
    
    public SensorFactory getSensor(EV3 brick) {
        return sensor.apply(brick);
    }
    
    public Collection<? extends Mode> getModes(EV3 brick) {
        SensorFactory sf = getSensor(brick);
        
        if( sf == null ) {
            return Collections.singletonList(RawOnlySensorFactory.MODE.RAW);
        } else {
            return sf.getModes();
        }
    }
    
    @Override
    public String toString() {
        return Resources.getString("SENSORTYPE_" + this.name());
    }
}
