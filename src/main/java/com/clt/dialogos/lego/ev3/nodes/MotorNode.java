package com.clt.dialogos.lego.ev3.nodes;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.xml.sax.SAXException;

import com.clt.dialogos.lego.ev3.Ev3Node;
import com.clt.dialogos.lego.ev3.Ev3Runtime;
import com.clt.dialogos.lego.ev3.Plugin;
import com.clt.dialogos.lego.ev3.Resources;
import com.clt.diamant.IdMap;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.MotorFactory;

/**
 * @author dabo
 *
 */
public class MotorNode extends Ev3Node {

    private static final String MOTOR = "motor";
    private static final String STATE = "state";
    private static final String POWER = "power";

    // Do not change names! These are written to XML.
    enum State {
        FORWARD,
        BACKWARD,
        STOP,
        DRIFT;

        @Override
        public String toString() {
            return Resources.getString("MOTORSTATE_" + this.name());
        }
    };

    public MotorNode() {

        this.setColor(new Color(255, 255, 153));

        this.setProperty(MotorNode.MOTOR, MotorFactory.MOTORSET.A); //  Motor.Port.A);
        this.setProperty(MotorNode.STATE, State.FORWARD);
        this.setProperty(MotorNode.POWER, "80");
        

        this.addEdge();
    }
    
    @Override
    protected int executeEv3(WozInterface comm) {
        try {
            Ev3Runtime runtime = (Ev3Runtime) this.getPluginRuntime(Plugin.class, comm);
            EV3 brick = runtime.getBrick();
            
            if (brick == null) {
                throw new NodeExecutionException(this, Resources.getString("NoNxtBrickSelected"));
            }
            
            MotorFactory.MOTORSET motorSet = (MotorFactory.MOTORSET) getProperty(MOTOR);
            if (motorSet == null) {
                throw new NodeExecutionException(this, Resources.getString("NoMotorSelected"));
            }
            
            State state = (State) getProperty(STATE);
            if (state == null) {
                throw new NodeExecutionException(this, Resources.getString("NoMotorStateSelected"));
            }
            
            int power = Integer.parseInt((String) getProperty(POWER));
            brick.MOTOR.speed(motorSet, power);
            
            switch(state) {
                case FORWARD:
                    brick.MOTOR.direction(motorSet, MotorFactory.DIR.FORWARD);
                    brick.MOTOR.start(motorSet);
                    break;
                    
                    
                case BACKWARD:
                    brick.MOTOR.direction(motorSet, MotorFactory.DIR.BACK);
                    brick.MOTOR.start(motorSet);
                    break;
                    
                case STOP:
                    brick.MOTOR.stop(motorSet, MotorFactory.BRAKE.BRAKE);
                    break;
                    
                case DRIFT:
                    brick.MOTOR.stop(motorSet, MotorFactory.BRAKE.COAST);
                    break;                    
            }
        } catch (NodeExecutionException exn) {
            throw exn;
        } catch (Exception exn) {
            throw new NodeExecutionException(this, Resources.getString("CouldNotControlMotor"), exn);
        }

        return 0;
    }

    @Override
    protected JComponent createEditorComponentImpl(Map<String, Object> properties) {

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);

        p.add(new JLabel(Resources.getString("MotorPort") + ':'), gbc);
        gbc.gridx++;
        
        final JComboBox motor = NodePropertiesDialog.createComboBox(properties, MOTOR, MotorFactory.MOTORSET.values());
        p.add(motor, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        p.add(new JLabel(Resources.getString("MotorState") + ':'), gbc);
        gbc.gridx++;
        final JComboBox state = NodePropertiesDialog.createComboBox(properties, MotorNode.STATE, State.values());
        p.add(state, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        p.add(new JLabel(Resources.getString("MotorPower") + ':'), gbc);
        gbc.gridx++;
        final JTextField power = NodePropertiesDialog.createTextField(properties, MotorNode.POWER);
        p.add(power, gbc);

        ItemListener l = new ItemListener() {

            public void itemStateChanged(ItemEvent e) {

                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Object item = e.getItem();

                    power.setEnabled((item == State.FORWARD) || (item == State.BACKWARD));
                }
            }
        };
        state.addItemListener(l);
        l.itemStateChanged(new ItemEvent(state, ItemEvent.ITEM_STATE_CHANGED,
                properties.get(MotorNode.STATE), ItemEvent.SELECTED));

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        p.add(Box.createVerticalGlue(), gbc);

        return p;
    }
    
    private MotorFactory.MOTORSET getMotorset() {
        return (MotorFactory.MOTORSET) getProperty(MOTOR);
    }
    
    private State getState() {
        return (State) getProperty(STATE);
    }
    
    private String getPower() {
        return (String) getProperty(POWER);
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map) throws SAXException {
        if (name.equals(MotorNode.MOTOR)) {
            MotorFactory.MOTORSET motorset = MotorFactory.MOTORSET.valueOf(value);
            setProperty(MOTOR, motorset);
            
            if (this.getProperty(MotorNode.MOTOR) == null) {
                r.raiseException(Resources.format("UnknownMotor", value));
            }
        } else if (name.equals(MotorNode.STATE)) {
            State state = State.valueOf(value);
            setProperty(STATE, state);

            if (this.getProperty(MotorNode.STATE) == null) {
                r.raiseException(Resources.format("UnknownMotorState", value));
            }
        } else if (name.equals(MotorNode.POWER)) {
            this.setProperty(MotorNode.POWER, value);
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {
        super.writeAttributes(out, uid_map);
        
        Graph.printAtt(out, MOTOR, getMotorset().name());
        Graph.printAtt(out, STATE, getState().name());
        Graph.printAtt(out, POWER, getPower());
    }
}
