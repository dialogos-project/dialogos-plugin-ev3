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
import elmot.javabrick.ev3.sensor.SensorFactory;
import elmot.javabrick.ev3.sensor.SoundSensorFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author elmot
 */
public abstract class EV3 implements AutoCloseable {

    protected final MotorFactory MOTOR;
    protected final ColorSensorFactory COLOR;
    protected final TouchSensorFactory TOUCH;
    protected final UltrasonicSensorFactory ULTRASONIC;
    protected final IRSensorFactory IR;
    protected final CompassSensorFactory COMPASS;
    protected final SoundSensorFactory SOUND;
    protected final TemperatureSensorFactory TEMP;
    protected final HTIRSeeker HT_IR_SEEKER;
    protected final GyroSensorFactory GYRO;
    protected final HTAngleSensor HT_ANGLE;
    protected final FileSystem FILE;
    protected final SystemFactory SYSTEM;
    
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

    public synchronized Response run(CommandBlock commandBlock, Class<?>[] commandParameters) throws IOException {
        ensureOpen();
        return commandBlock.run(this, commandParameters);
    }
    
    abstract public void ensureOpen() throws IOException;
    abstract public void close() throws IOException;
    abstract public ByteBuffer dataExchange(ByteBuffer bytes, int expectedSeqNo) throws IOException;

    public String getBrickname() throws IOException {
        return SYSTEM.getBrickName();
    }
    
    public SensorFactory getTouchSensor() {
        return TOUCH;
    }
    
    public SensorFactory getColorSensor() {
        return COLOR;
    }
    
    public SensorFactory getSoundSensor() {
        return SOUND;
    }
    
    public SensorFactory getUltrasonicSensor() {
        return ULTRASONIC;
    }
    
    public void setMotorSpeed(MotorFactory.MOTORSET motors, int speed) throws IOException {
        MOTOR.speed(motors, speed);
    }
    
    public void setMotorDirection(MotorFactory.MOTORSET motors, MotorFactory.DIR direction) throws IOException {
        MOTOR.direction(motors, direction);
    }
    
    public void startMotor(MotorFactory.MOTORSET motors) throws IOException {
        MOTOR.start(motors);
    }
    
    public void stopMotor(MotorFactory.MOTORSET motors, MotorFactory.BRAKE brake) throws IOException {
        MOTOR.stop(motors, brake);
    }
    
    public List<FileSystem.Ev3File> findPrograms() throws IOException {
        return FILE.findFiles(FileSystem.PROJECT_ROOT, file -> file.getName().toLowerCase().endsWith(".rbf"));
    }
    
    public void startProgram(String absoluteFilename) throws IOException {
        FILE.startProgram(FileSystem.Ev3File.makeFilenameRelativeToProjectRoot(absoluteFilename));
    }
    
    public void waitUntilProgramTermination() throws IOException {
        FILE.waitUntilProgramTermination();
    }
    
    public void stopProgram() throws IOException {
        FILE.stopProgram();
    }
}

