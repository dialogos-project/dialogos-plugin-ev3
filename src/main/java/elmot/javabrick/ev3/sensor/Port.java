package elmot.javabrick.ev3.sensor;

/**
 * A port for attaching a sensor.
 * 
*/
public enum Port {
    P1(0), P2(1), P3(2), P4(3);

    public final int portNum;

    Port(int portNum) {
        this.portNum = portNum;
    }
    
    
}
