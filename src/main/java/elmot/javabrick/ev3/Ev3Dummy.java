/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3;

import elmot.javabrick.ev3.sensor.Mode;
import elmot.javabrick.ev3.sensor.Port;
import elmot.javabrick.ev3.sensor.SensorFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A dummy implementation of EV3. This class does nothing when
 * motors are controlled and programs are started, always returns
 * a zero RAW value for all sensors, and returns a brick name of "(Dummy)".
 * 
 * @author Alexander
 */
public class Ev3Dummy extends EV3 {

    @Override
    public void stopProgram() throws IOException {
    }

    @Override
    public void waitUntilProgramTermination() throws IOException {
    }

    @Override
    public void startProgram(String absoluteFilename) throws IOException {
    }

    @Override
    public List<FileSystem.Ev3File> findPrograms() throws IOException {
        return new ArrayList<>();
    }

    @Override
    public void stopMotor(MotorFactory.MOTORSET motors, MotorFactory.BRAKE brake) throws IOException {
    }

    @Override
    public void startMotor(MotorFactory.MOTORSET motors) throws IOException {
    }

    @Override
    public void setMotorDirection(MotorFactory.MOTORSET motors, MotorFactory.DIR direction) throws IOException {
    }

    @Override
    public void setMotorSpeed(MotorFactory.MOTORSET motors, int speed) throws IOException {
    }

    @Override
    public SensorFactory getUltrasonicSensor() {
        return new DummySensorFactory(this);
    }

    @Override
    public SensorFactory getSoundSensor() {
        return new DummySensorFactory(this);
    }

    @Override
    public SensorFactory getColorSensor() {
        return new DummySensorFactory(this);
    }

    @Override
    public SensorFactory getTouchSensor() {
        return new DummySensorFactory(this);
    }

    @Override
    public String getBrickname() throws IOException {
        return "(Dummy)";
    }

    @Override
    public void ensureOpen() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public ByteBuffer dataExchange(ByteBuffer bytes, int expectedSeqNo) throws IOException {
        // this should never be called
        return null;
    }

    public static class DummySensorFactory extends SensorFactory {

        public DummySensorFactory(EV3 ev3) {
            super(ev3);
        }

        public void setMode(int daisyChainLevel, Port port, int mode) throws IOException {
        }

        @Override
        public List<? extends Mode> getModes() {
            return Arrays.asList(MODE.values());
        }

        @Override
        public Mode decodeMode(String modename) {
            return MODE.valueOf(modename);
        }

        @Override
        public Object readValue(Port port) throws IOException {
            return 0;
        }

        public enum MODE implements Mode {
            RAW(0);

            private final int val;

            private MODE(int val) {
                this.val = val;
            }

            @Override
            public int getId() {
                return val;
            }

            @Override
            public String getName() {
                return toString();
            }
        }

    }
}
