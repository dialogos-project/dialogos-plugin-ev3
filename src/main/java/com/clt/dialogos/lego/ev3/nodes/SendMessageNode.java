package com.clt.dialogos.lego.ev3.nodes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xml.sax.SAXException;

import com.clt.dialogos.lego.ev3.Node;
import com.clt.dialogos.lego.ev3.NxtRuntime;
import com.clt.dialogos.lego.ev3.Plugin;
import com.clt.dialogos.lego.ev3.Resources;
import com.clt.diamant.IdMap;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.diamant.gui.NodePropertiesDialog;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.StringValue;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;
import com.clt.lego.ev3.Ev3;

/**
 * @author Daniel Bobbert
 *
 */
public class SendMessageNode extends Node {

    private static final String MAILBOX = "mailbox";
    private static final String MESSAGE = "message";

    public SendMessageNode() {

        this.addEdge();
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

        p.add(new JLabel(Resources.getString("Mailbox") + ':'), gbc);
        gbc.gridx++;
        JComboBox mailbox
                = NodePropertiesDialog.createComboBox(properties, SendMessageNode.MAILBOX,
                        new Integer[]{
                            1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        p.add(mailbox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        p.add(new JLabel(Resources.getString("Message") + ':'), gbc);
        gbc.gridx++;
        p.add(NodePropertiesDialog.createTextField(properties,
                SendMessageNode.MESSAGE), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        p.add(Box.createVerticalGlue(), gbc);

        return p;
    }

    @Override
    protected int executeNXT(WozInterface comm) {

        NxtRuntime runtime = (NxtRuntime) this.getPluginRuntime(Plugin.class, comm);
        Ev3 brick = runtime.getBrick();
        if (brick == null) {
            throw new NodeExecutionException(this, Resources
                    .getString("NoNxtBrickSelected"));
        }

        Integer mailbox = (Integer) this.properties.get(SendMessageNode.MAILBOX);
        if (mailbox == null) {
            throw new NodeExecutionException(this, Resources
                    .getString("NoMailboxSelected"));
        }

        try {
            String message = (String) this.getProperty(SendMessageNode.MESSAGE);

            Value msg = this.parseExpression(message).evaluate(comm);
            if (msg instanceof StringValue) {
                message = ((StringValue) msg).getString();
            } else if (msg instanceof IntValue) {
                long value = ((IntValue) msg).getInt();
                char[] i2s = new char[4];
                for (int i = 0; i < i2s.length; i++) {
                    i2s[i] = (char) (value & 0xFF);
                    value >>>= 8;
                }
                message = String.valueOf(i2s);
            } else {
                message = msg.toString();
            }

            brick.sendMessage(mailbox.intValue() - 1, message);
        } catch (NodeExecutionException exn) {
            throw exn;
        } catch (Exception exn) {
            throw new NodeExecutionException(this, Resources
                    .getString("CouldNotSendMessage"), exn);
        }

        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void readAttribute(XMLReader r, String name, String value,
            IdMap uid_map)
            throws SAXException {

        if (name.equals(SendMessageNode.MAILBOX)) {
            try {
                int mailbox = Integer.parseInt(value);
                if ((mailbox < 1) || (mailbox > 10)) {
                    throw new NumberFormatException("Value out of range");
                } else {
                    this.setProperty(SendMessageNode.MAILBOX, new Integer(mailbox));
                }
            } catch (NumberFormatException exn) {
                r.raiseException(Resources.format("IllegalMailboxValue", value), exn);
            }
        } else if (name.equals(SendMessageNode.MESSAGE)) {
            this.setProperty(SendMessageNode.MESSAGE, value);
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

        super.writeAttributes(out, uid_map);

        if (this.getProperty(SendMessageNode.MAILBOX) != null) {
            Graph.printAtt(out, SendMessageNode.MAILBOX, (Integer) this
                    .getProperty(SendMessageNode.MAILBOX));
        }

        if (this.getProperty(SendMessageNode.MESSAGE) != null) {
            Graph.printAtt(out, SendMessageNode.MESSAGE, (String) this
                    .getProperty(SendMessageNode.MESSAGE));
        }
    }
}
