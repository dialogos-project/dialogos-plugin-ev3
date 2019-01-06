package com.clt.dialogos.lego.ev3;

import java.util.Arrays;

import javax.swing.Icon;

import com.clt.dialogos.lego.ev3.nodes.MotorNode;
import com.clt.dialogos.lego.ev3.nodes.ProgramNode;
import com.clt.dialogos.lego.ev3.nodes.ReadSensorNode;
import com.clt.dialogos.lego.ev3.nodes.StopProgramNode;
import com.clt.dialogos.plugin.PluginSettings;
import static com.clt.diamant.graph.Node.registerNodeTypes;
import com.clt.gui.Images;

/**
 * @author dabo
 *
 */
public class Plugin implements com.clt.dialogos.plugin.Plugin {

    @Override
    public void initialize() {
        registerNodeTypes(this.getName(), Arrays.asList(new Class<?>[]{
//            ProgramNode.class, 
//            StopProgramNode.class, 
//            ReadSensorNode.class, 
            MotorNode.class
        }));
    }

    @Override
    public String getId() {
        return "dialogos.plugin.lego.ev3";
    }

    @Override
    public String getName() {
        return "Lego Mindstorms EV3";
    }

    @Override
    public Icon getIcon() {
        return Images.load(this, "LegoEv3.png");
    }

    @Override
    public String getVersion() { return "2.0";  }  // DO NOT EDIT - This line is updated automatically by the make-release script.

    @Override
    public PluginSettings createDefaultSettings() {
        return new Settings();
    }
}
