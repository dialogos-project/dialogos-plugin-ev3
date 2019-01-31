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


import com.clt.dialogos.plugin.PluginSettings;
import com.clt.diamant.IdMap;
import com.clt.diamant.graph.Graph;
import com.clt.event.ProgressListener;
import com.clt.gui.OptionPane;
import com.clt.gui.ProgressDialog;
import com.clt.properties.DefaultEnumProperty;
import com.clt.properties.EnumProperty;
import com.clt.properties.Property;
import com.clt.properties.PropertySet;
import com.clt.util.AbstractLongAction;
import com.clt.util.UserCanceledException;
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
    private EV3 brick = null;

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

        ev3.addChangeListener(e -> {
            resetBrick();
        });

        this.sensorTypes = new LinkedHashMap<Port, DefaultEnumProperty<SensorType>>();
        for (Port port : Port.values()) {
            DefaultEnumProperty<SensorType> p = new DefaultEnumProperty<SensorType>(port.name(), port.toString(), null, SensorType.values(), SensorType.NONE);
            this.sensorTypes.put(port, p);
        }

        // updateBrickList(null, true);
    }

    private void addBrick(Ev3Descriptor desc) {
        Settings.availablePorts.add(desc);
        this.ev3.setPossibleValues(this.getAvailablePorts());
        this.ev3.setValue(desc);
    }

    /**
     * If brick is changed in the dropdown menu, set the brick variable to null,
     * so the next call to {@link #createBrick(java.awt.Component) }
     * will open a new brick connection. If we had an active brick connection
     * before, close it before forgetting it.
     *
     */
    private void resetBrick() {
        if (brick != null) {
            try {
                brick.close();
            } catch (IOException ex) {
            }
        }

        brick = null;
    }

    /**
     * Returns a brick connection based on the user's choice in the dropdown
     * menu.
     *
     * @param parent
     * @return
     * @throws IOException
     * @throws UserCanceledException
     */
    public EV3 createBrick(Component parent) throws IOException, UserCanceledException {
        // no brick selected => return null
        if (ev3.getValue() == null) {
            return null;
        }

        if (brick != null) {
//            System.err.println("reuse previous connection");
            
            // Check that connection is still available. If not, reconnect.
            try {
                brick.SYSTEM.getBrickName();
            } catch (Exception e) {
//                System.err.println("connection went away, reconnect");

                try {
                    brick.close();
                } catch (Exception e2) {
//                    System.err.println("Additional exception during brick close: ");
                    e2.printStackTrace();
                }

                brick = ev3.getValue().instantiateWithRetries(3);
            }
        } else {
            // If no brick was previously allocated (e.g. because resetBrick was
            // called), create a new brick connection and cache it.
//            System.err.println("instantiate brick after reset");
            brick = ev3.getValue().instantiateWithRetries(3);
        }

        return brick;
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
    protected void readAttribute(final XMLReader r, String name, String value, IdMap uid_map) {
        if( name.equals("ev3")) {
            Ev3Descriptor desc = Ev3Descriptor.deserialize(value);
            
            if( desc != null ) {
                try {
                    // try to reconnect
                    EV3 brick = desc.instantiate();
                    brick.SYSTEM.getBrickName();
                    
                    // reconnection worked, add to brick list
                    availablePorts.clear();
                    availablePorts.add(desc);
                    
                    Ev3Descriptor[] available = getAvailablePorts();                    
                    ev3.setPossibleValues(available);                    
                    ev3.setValue(desc);                    
                } catch (Exception ex) {
                    // reconnecting didn't work => just stay with "Dummy"
                }                
            }
        } else {
            Port port = Port.valueOf(name);
            SensorType type = SensorType.valueOf(value);
            setSensorType(port, type);
        }
    }

    @Override
    public void writeAttributes(XMLWriter out, IdMap uidMap) {
        Ev3Descriptor desc = ev3.getValue();
        
        if( desc != null ) {
            Graph.printAtt(out, "ev3", desc.serialize());
            for( Port port : Port.values() ) {
                Graph.printAtt(out, port.name(), getSensorType(port).name());
            }
        }
    }

    public SensorType getSensorType(Port port) {
        return sensorTypes.get(port).getValue();
    }
    
    public void setSensorType(Port port, SensorType type) {
        EnumProperty<SensorType> prop = sensorTypes.get(port);
        prop.setValue(type);
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
