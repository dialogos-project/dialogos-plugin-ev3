package com.clt.dialogos.lego.ev3;

import java.io.IOException;
import java.util.Map;

import com.clt.dialogos.plugin.PluginRuntime;
import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.sensor.Port;

/**
 * @author dabo
 *
 */
public class Ev3Runtime implements PluginRuntime {

    private EV3 brick;
    private Map<Port, SensorType> sensorTypes;

    public Ev3Runtime(EV3 brick, Map<Port, SensorType> sensorTypes) {

        this.brick = brick;
        this.sensorTypes = sensorTypes;

        if (brick != null) {
            Thread t = new Thread(new Runnable() {

                public void run() {

                    try {
                        long delay = Ev3Runtime.this.keepAlive();
                        while (delay != 0) {
                            // ping the brick 10s before it wants to sleep
                            if (delay < 0) {
                                Thread.sleep(10000);
                            } else {
                                Thread.sleep(delay - 10000);
                            }
                            delay = Ev3Runtime.this.keepAlive();
                        }
                    } catch (Exception exn) {
                        // ignore
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    private synchronized long keepAlive()
            throws IOException {

        if (this.brick == null) {
            return 0;
        } else {
            // TODO implement me
            return 0;
//            return this.brick.keepAlive();
        }
    }

    public synchronized void dispose() {

        if (this.brick != null) {
            try {
                this.brick.close();
            } catch (Exception exn) {
                // ignore
            }
            this.brick = null;
        }
    }

    public EV3 getBrick() {
        return this.brick;
    }

    public SensorType getSensorType(Port port) {
        return this.sensorTypes.get(port);
    }
}
