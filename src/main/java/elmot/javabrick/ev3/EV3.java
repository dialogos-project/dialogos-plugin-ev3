package elmot.javabrick.ev3;

import elmot.javabrick.ev3.sensor.HTIRSeeker;
import elmot.javabrick.ev3.sensor.CompassSensorFactory;
import elmot.javabrick.ev3.sensor.IRSensorFactory;
import elmot.javabrick.ev3.sensor.UltrasonicSensorFactory;
import elmot.javabrick.ev3.sensor.GyroSensorFactory;
import elmot.javabrick.ev3.sensor.TemperatureSensorFactory;
import elmot.javabrick.ev3.sensor.ColorSensorFactory;
import elmot.javabrick.ev3.sensor.TouchSensorFactory;
import elmot.javabrick.ev3.sensor.HTAngleSensor;
import elmot.javabrick.ev3.sensor.SoundSensorFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author elmot
 */
public abstract class EV3 implements AutoCloseable {
    public final MotorFactory MOTOR;
    public final ColorSensorFactory COLOR;

    public final TouchSensorFactory TOUCH;
    public final UltrasonicSensorFactory ULTRASONIC;
    public final IRSensorFactory IR;
    public final CompassSensorFactory COMPASS;
    public final SoundSensorFactory SOUND;
    public final TemperatureSensorFactory TEMP;
    public final HTIRSeeker HT_IR_SEEKER;
    public final GyroSensorFactory GYRO;
    public final HTAngleSensor HT_ANGLE;
    public final FileSystem FILE;

    public final SystemFactory SYSTEM;

    public EV3() {
        MOTOR = new MotorFactory(this);
        COLOR = new ColorSensorFactory(this);
        SYSTEM = new SystemFactory(this);
        TOUCH = new TouchSensorFactory(this);
        ULTRASONIC = new UltrasonicSensorFactory(this);
        IR = new IRSensorFactory(this);
        COMPASS = new CompassSensorFactory(this);
        SOUND = new SoundSensorFactory(this);
        TEMP = new TemperatureSensorFactory(this);
        HT_ANGLE = new HTAngleSensor(this);
        HT_IR_SEEKER = new HTIRSeeker(this);
        GYRO = new GyroSensorFactory(this);
        FILE = new FileSystem(this);
    }

    public abstract void ensureOpen() throws IOException;

    @Override
    public abstract void close() throws IOException;

    public synchronized Response run(CommandBlock commandBlock, Class<?>[] commandParameters) throws IOException {
        ensureOpen();
        return commandBlock.run(this, commandParameters);
    }

    public abstract ByteBuffer dataExchange(ByteBuffer bytes, int expectedSeqNo) throws IOException;
}
