package com.clt.dialogos.lego.ev3;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.IdMap;
import com.clt.event.ProgressListener;
import com.clt.gui.OptionPane;
import com.clt.gui.ProgressDialog;
import com.clt.io.InterfaceType;
import com.clt.properties.DefaultEnumProperty;
import com.clt.properties.Property;
import com.clt.properties.PropertySet;
import com.clt.util.AbstractLongAction;
import com.clt.util.UserCanceledException;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import java.util.ArrayList;
import java.util.List;
import elmot.javabrick.ev3.Ev3Descriptor;
import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.sensor.Port;
import java.util.EnumMap;

/**
 * @author dabo, koller
 */
public class Settings extends PluginSettings {

    private static Collection<Ev3Descriptor> availablePorts = new TreeSet<Ev3Descriptor>();
    private DefaultEnumProperty<Ev3Descriptor> ev3;
    private Map<Port, DefaultEnumProperty<SensorType>> sensorTypes;
    
    static {
        Settings.availablePorts.add(new Ev3Descriptor(Ev3Descriptor.ConnectionTypes.DUMMY, "--", "--"));
    }

    @SuppressWarnings("unchecked")
    private Ev3Descriptor[] getAvailablePorts() {
        return Settings.availablePorts.toArray(new Ev3Descriptor[Settings.availablePorts.size()]);
    }

    public Settings() {

        this.ev3 = new DefaultEnumProperty<Ev3Descriptor>("ev3", Resources.getString("NxtBrick"), null, this.getAvailablePorts()) {
            @Override
            public String getName() {
                return Resources.getString("NxtBrick");
            }
        };

        if (!Settings.availablePorts.isEmpty()) {
            this.ev3.setValue(Settings.availablePorts.iterator().next());
        }

        this.sensorTypes = new LinkedHashMap<Port, DefaultEnumProperty<SensorType>>();
        for (Port port : Port.values()) {
            DefaultEnumProperty<SensorType> p = new DefaultEnumProperty<SensorType>(port.name(), port.toString(), null, SensorType.values(), SensorType.NONE);
            this.sensorTypes.put(port, p);
        }

        // updateBrickList(null, true);
    }

    private void addBrick(Ev3Descriptor  desc) {
        Settings.availablePorts.add(desc);
        this.ev3.setPossibleValues(this.getAvailablePorts());
        this.ev3.setValue(desc);
    }

    private void updateBrickList(Component parent, boolean search) {
        try {
            if (search) {
                final ProgressDialog d = new ProgressDialog(parent);
                try {
                    d.run(new AbstractLongAction() {
                        private AtomicBoolean cancel = new AtomicBoolean(false);

                        @Override
                        public void cancel() {
                            this.cancel.set(true);
                        }

                        @Override
                        public boolean canCancel() {
                            return true;
                        }

                        @Override
                        protected void run(ProgressListener progress) throws Exception {
                            StringWriter log = new StringWriter();
                            PrintWriter pw = new PrintWriter(log, true);

                            boolean foundNewBrick = false;
                            
                            Ev3Descriptor.discoverAll();
                            List<Ev3Descriptor> availableBricks = Ev3Descriptor.getAllDescriptors();

                            // remove bricks that are no longer connected
                            List<Ev3Descriptor> availablePortsCopy = new ArrayList<>(Settings.availablePorts); // to avoid ConcurrentModificationException
                            for (Ev3Descriptor x : availablePortsCopy) {
                                if (!availableBricks.contains(x)) {
                                    Settings.availablePorts.remove(x);
                                }
                            }

                            // add bricks that were newly connected
                            for (Ev3Descriptor x : availableBricks) {
                                if (!Settings.availablePorts.contains(x)) {
                                    foundNewBrick = true;
                                    addBrick(x);
                                }
                            }

                            if (!foundNewBrick) {
                                pw.println(Resources.getString("NoNewBrickFound"));
                            }

                            pw.close();

                            if (log.getBuffer().length() > 0) {
                                OptionPane.warning(d, log.toString());
                            }
                        }

                        @Override
                        public String getDescription() {
                            return Resources.getString("SearchingForBricks");
                        }
                    });
                } catch (InvocationTargetException exn) {
                    exn.getTargetException().printStackTrace();
                    OptionPane.error(parent, exn.getTargetException());
                }
            }
        } catch (Exception exn) {
            System.err.println(exn);
        }

        Ev3Descriptor[] available = getAvailablePorts();
        this.ev3.setPossibleValues(available);

        // display the first of the newly found bricks in
        // the Settings-UI
        if (search && (this.ev3.getPossibleValues().length > 1)) {
            this.ev3.setValue(this.ev3.getPossibleValues()[1]);
        }
    }

    @Override
    public JComponent createEditor() {

        final JPanel p = new JPanel(new BorderLayout());

        // update ports
        this.updateBrickList(null, false);

        PropertySet<Property<?>> ps = new PropertySet<Property<?>>();
        ps.add(this.ev3);
        for (DefaultEnumProperty<SensorType> sensorType : this.sensorTypes.values()) {
            ps.add(sensorType);
        }

        p.add(ps.createPropertyPanel(true), BorderLayout.NORTH);

        JButton update = new JButton(Resources.getString("FindBricks"));
        update.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                Settings.this.updateBrickList(null, true);
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(update);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    @Override
    protected void readAttribute(final XMLReader r, String name, String value,
            IdMap uid_map) {

        if (name.equals("nxt")) {
            r.setHandler(new AbstractHandler("att") {

                private Class<?> factory = null;
                private String brickName = null;
                private String uri = null;
                private InterfaceType type = null;
                private String port = null;

                @Override
                protected void start(String name, Attributes atts) {

                    if (name.equals("att")) {
                        r.setHandler(new AbstractHandler("att"));

                        String att = atts.getValue("name");
                        String value = atts.getValue("value");
                        if (att.equals("factory")) {
                            try {
                                this.factory = Settings.class.getClassLoader().loadClass(value);
                            } catch (ClassNotFoundException exn) {
                                // ignore
                            }
                        } else if (att.equals("name")) {
                            this.brickName = value;
                        } else if (att.equals("type")) {
                            this.type = InterfaceType.valueOf(value);
                        } else if (att.equals("port")) {
                            this.port = value;
                        } else if (att.equals("uri")) {
                            this.uri = value;
                        }
                    }
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void end(String name) throws SAXException {
                    // TODO implement me
//
//                    try {
//                        BrickDescription desc
//                                = (BrickDescription) this.factory.getConstructor(
//                                        new Class[]{String.class, NxtDeviceInfo.class,
//                                            InterfaceType.class, String.class}).newInstance(
//                                                new Object[]{this.uri,
//                                                    new NxtDeviceInfo(this.brickName, null, null, 0, 0, 0),
//                                                    this.type,
//                                                    this.port});
//                        Settings.this.addBrick(desc);
//                        Settings.this.ev3.setValue(desc);
//                    } catch (Exception exn) {
//                        r.raiseException(exn);
//                    }
                }
            });
        } else if (name.equals("sensor")) {
            r.setHandler(new AbstractHandler("att") {

                @Override
                protected void start(String name, Attributes atts) throws SAXException {

                    if (name.equals("att")) {
                        r.setHandler(new AbstractHandler("att"));

                        String sensor = atts.getValue("name");
                        String value = atts.getValue("value");

                        int sensorID = -1;
                        try {
                            sensorID = Integer.parseInt(sensor);
                        } catch (Exception exn) {
                            r.raiseException(exn);
                        }

                        Port port = null;
                        for (Port p : Port.values()) {
                            if (p.portNum == sensorID) {
                                port = p;
                            }
                        }
                        SensorType type = null;
                        for (SensorType t : SensorType.values()) {
                            if (t.name().equals(value)) {
                                type = t;
                            }
                        }
                        if ((port != null) && (type != null)) {
                            Settings.this.sensorTypes.get(port).setValue(type);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void writeAttributes(XMLWriter out, IdMap uidMap) {
        // TODO implement me

        /*
        BrickDescription nxt = this.ev3.getValue();
        if ((nxt != null) && (nxt.getInterfaceType() != null)) {
            Graph.printAtt(out, "nxt", "nxt", null);
            Graph.printAtt(out, "factory", nxt.getClass().getName());
            Graph.printAtt(out, "name", nxt.getBrickName());
            Graph.printAtt(out, "type", nxt.getInterfaceType().name());
            if (nxt.getPort() != null) {
                Graph.printAtt(out, "port", nxt.getPort());
            }
            Graph.printAtt(out, "uri", nxt.getURI());
            out.closeElement("att");

            Graph.printAtt(out, "sensor", "sensor", null);
            for (Sensor.Port port : this.sensorTypes.keySet()) {
                Graph.printAtt(out, String.valueOf(port.getID()), this.sensorTypes.get(
                        port).getValue()
                        .name());
            }
            out.closeElement("att");
        }
*/
    }

    public SensorType getSensorType(Port port) {

        return this.sensorTypes.get(port).getValue();
    }

    public EV3 createBrick(Component parent) throws IOException, UserCanceledException {

        if (this.ev3.getValue() != null) {
            return this.ev3.getValue().instantiate();
        } else {
            return null;
        }
    }

    @Override
    public Ev3Runtime createRuntime(Component parent) throws Exception {
        Map<Port, SensorType> sensorTypes = new EnumMap<Port, SensorType>(Port.class);
        
        for (Port port : this.sensorTypes.keySet()) {
            sensorTypes.put(port, this.getSensorType(port));
        }
        
        return new Ev3Runtime(this.createBrick(parent), sensorTypes);
    }
}
