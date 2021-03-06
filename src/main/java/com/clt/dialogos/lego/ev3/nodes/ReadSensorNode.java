package com.clt.dialogos.lego.ev3.nodes;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xml.sax.SAXException;

import com.clt.dialogos.lego.ev3.Ev3Node;
import com.clt.dialogos.lego.ev3.Ev3Runtime;
import com.clt.dialogos.lego.ev3.Plugin;
import com.clt.dialogos.lego.ev3.Resources;
import com.clt.dialogos.lego.ev3.SensorType;
import com.clt.dialogos.lego.ev3.Settings;
import com.clt.diamant.IdMap;
import com.clt.diamant.Slot;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.graph.search.SearchResult;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.script.exp.Value;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.Ev3Dummy;
import elmot.javabrick.ev3.sensor.Mode;
import elmot.javabrick.ev3.sensor.Port;
import elmot.javabrick.ev3.sensor.RawOnlySensorFactory;
import elmot.javabrick.ev3.sensor.SensorFactory;

/**
 * @author dabo
 *
 */
public class ReadSensorNode extends Ev3Node {

    private static final String SENSOR = "sensor";
    private static final String MODE = "mode";
    private static final String ACTIVATE = "activate";
    private static final String VARIABLE = "variable";

    // Don't change names. They are written to XML
    public ReadSensorNode() {
        this.setProperty(ReadSensorNode.MODE, 0);
        this.setProperty(ReadSensorNode.SENSOR, new SensorPort(Port.P1));
        this.addEdge();
    }

    public static Color getDefaultColor() {
        return new Color(255, 255, 153);
    }
    
    private Settings getSettings() {
        return (Settings) getPluginSettings(Plugin.class);
    }
    
    private EV3 getBrick() {
        return new Ev3Dummy();
    }
    
    @Override
    protected JComponent createEditorComponentImpl(final Map<String, Object> properties) {
        final JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);

        
        
        // dropdown for selecting sensor port
        
        SensorPort ports[] = new SensorPort[Port.values().length];
        int i = 0;
        for (Port port : Port.values()) {
            ports[i++] = new SensorPort(port);
        }

        p.add(new JLabel(Resources.getString("SensorPort") + ':'), gbc);
        gbc.gridx++;
        final JComboBox sensor = NodePropertiesDialog.createComboBox(properties, ReadSensorNode.SENSOR, ports);
//        this.setProperty(ReadSensorNode.SENSOR, ports[0]); // don't overwrite previously selected port
        // sensor.setSelectedItem(ports[0]);

        p.add(sensor, gbc);
        
        
        
        // dropdown for selecting sensor mode
        
        SensorPort sp = (SensorPort) properties.get(SENSOR);
        SensorType sensorType = getSettings().getSensorType(sp.getPort());
        EV3 brick = getBrick();
        Collection<? extends Mode> modes = sensorType.getModes(brick);
        final JComboBox sensorMode = NodePropertiesDialog.createIntComboBox(properties, ReadSensorNode.MODE, modes);
        final JPanel options = new JPanel(new GridLayout(1, 1));
        
        
        
        // if sensor port changes, update list of possible sensor modes
        
        ItemListener typeListener = (ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                SensorPort port = (SensorPort) e.getItem();
                SensorType value = null;
                if (port != null) {
                    value = port.getType();

                    // replace possible modes in mode list with the ones that are appropriate for this type of sensor
                    SensorType typeAtPort = getSettings().getSensorType(port.getPort());
                    sensorMode.removeAllItems();

                    if (typeAtPort == null) {
                        sensorMode.addItem(RawOnlySensorFactory.MODE.RAW);
                    } else {
                        for (Mode mode : typeAtPort.getModes(brick)) {
                            sensorMode.addItem(mode);
                        }
                    }

                    sensorMode.setSelectedIndex(0);
                }

                if (p.isShowing()) {
                    p.revalidate();
                    p.repaint();
                }
            }
        };

        
        sensor.addItemListener(typeListener);
        typeListener.itemStateChanged(new ItemEvent(sensor,
                ItemEvent.ITEM_STATE_CHANGED,
                properties.get(ReadSensorNode.SENSOR), ItemEvent.SELECTED));

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        // gbc.weightx = 1.0;
        p.add(options, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        p.add(new JLabel(Resources.getString("SensorMode") + ':'), gbc);
        gbc.gridx++;
        p.add(sensorMode, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        p.add(new JLabel(Resources.getString("SaveInVariable") + ':'), gbc);
        gbc.gridx++;
        p.add(NodePropertiesDialog.createComboBox(properties,
                ReadSensorNode.VARIABLE, this.getGraph().getAllVariables(
                        Graph.LOCAL)), gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        p.add(new JPanel(), gbc);

        return p;
    }

    @Override
    protected int executeEv3(WozInterface comm) {

        try {
            Ev3Runtime runtime = (Ev3Runtime) this.getPluginRuntime(Plugin.class, comm);
            EV3 brick = runtime.getBrick();

            if (brick == null) {
                throw new NodeExecutionException(this, Resources.getString("NoNxtBrickSelected"));
            }

            SensorPort sensorPort = (SensorPort) this.getProperty(ReadSensorNode.SENSOR);
            if (sensorPort == null) {
                throw new NodeExecutionException(this, Resources.getString("NoSensorSelected"));
            }

            Slot v = (Slot) this.getProperty(ReadSensorNode.VARIABLE);
            if (v == null) {
                throw new NodeExecutionException(this, com.clt.diamant.Resources.getString("NoVariableAssigned"));
            }

            Port port = sensorPort.getPort();
            int mode = getModeId();
            SensorType type = runtime.getSensorType(sensorPort.getPort());
            SensorFactory factory = type.getSensor(brick);
            factory.setMode(0, port, mode);
            Object value = factory.readValue(port);
            
            v.setValue(Value.of(value));
        } catch (NodeExecutionException exn) {
            throw exn;
        } catch (Exception exn) {
            throw new NodeExecutionException(this, Resources.getString("CouldNotReadSensor"), exn);
        }
        return 0;
    }

    @Override
    public void validate(Collection<SearchResult> errors) {

        super.validate(errors);

        SensorPort sensor = (SensorPort) this.getProperty(ReadSensorNode.SENSOR);
        if (sensor == null) {
            this.reportError(errors, false, Resources.getString("NoSensorSelected"));
        } else {
            SensorType type = sensor.getType();
            if ((type == null) || (type == SensorType.NONE)) {
                reportError(errors, false, Resources.getString("SensorTypeNotSet"));
            }
        }

        Slot v = (Slot) this.getProperty(ReadSensorNode.VARIABLE);
        if (v != null) {
            if (!this.getGraph().getAllVariables(Graph.LOCAL).contains(v)) {
                this.reportError(errors, false, com.clt.diamant.Resources.format(
                        "referencesInaccessibleVariable", v.getName()));
            }

            if (v.getType() != com.clt.script.exp.Type.Int) {
                this.reportError(errors, false, Resources.format("usesNonIntVariable",
                        v.getName()));
            }
        } else {
            this.reportError(errors, false, com.clt.diamant.Resources
                    .getString("hasNoVariableAssigned"));
        }

    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {
        if (name.equals(ReadSensorNode.SENSOR)) {
            for (Port s : Port.values()) {
                if (String.valueOf(s.portNum).equals(value)) {
                    this.setProperty(ReadSensorNode.SENSOR, new SensorPort(s));
                    break;
                }
            }
            if (this.getProperty(ReadSensorNode.SENSOR) == null) {
                r.raiseException(Resources.format("UnknownSensor", value));
            }
        } else if (name.equals(ReadSensorNode.MODE)) {
            setProperty(MODE, Integer.parseInt(value));

            if (this.getProperty(ReadSensorNode.SENSOR) == null) {
                r.raiseException(Resources.format("UnknownSensor", value));
            }
        } else if (name.equals(ReadSensorNode.VARIABLE) && (value != null)) {
            try {
                this.setProperty(ReadSensorNode.VARIABLE, uid_map.variables.get(value));
            } catch (Exception exn) {
                r.raiseException(com.clt.diamant.Resources.format("UnknownVariable", "ID " + value));
            }
        } else if (name.equals(ReadSensorNode.ACTIVATE)) {
            this.setProperty(name, value.equals("1") ? Boolean.TRUE : Boolean.FALSE);
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

        super.writeAttributes(out, uid_map);

        SensorPort sensor = (SensorPort) this.getProperty(ReadSensorNode.SENSOR);
        if (sensor != null) {
            Graph.printAtt(out, ReadSensorNode.SENSOR, sensor.getPort().portNum);
        }

        Slot v = (Slot) this.getProperty(ReadSensorNode.VARIABLE);
        if (v != null) {
            try {
                String uid = uid_map.variables.getKey(v);
                Graph.printAtt(out, ReadSensorNode.VARIABLE, uid);
            } catch (Exception exn) {
            } // variable deleted
        }

        if (this.getProperty(ReadSensorNode.MODE) != null) {
            Graph.printAtt(out, ReadSensorNode.MODE, getModeId());
        }

        if (this.getBooleanProperty(ReadSensorNode.ACTIVATE)) {
            Graph.printAtt(out, ReadSensorNode.ACTIVATE, true);
        }
    }

    // don't call from within createEditorComponentImpl; it will
    // read from the wrong properties map
    private int getModeId() {
        return (Integer) getProperty(MODE);
    }
    
    private class SensorPort {

        private Port port;

        public SensorPort(Port port) {
            this.port = port;
        }

        public Port getPort() {
            return this.port;
        }

        public SensorType getType() {
            return ((Settings) ReadSensorNode.this.getPluginSettings(Plugin.class)).getSensorType(this.port);
        }

        @Override
        public int hashCode() {
            return this.port.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof SensorPort) {
                return ((SensorPort) o).getPort().equals(this.getPort());
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return this.port + " (" + this.getType() + ")";
        }
    }
}
