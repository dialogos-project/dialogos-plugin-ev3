package com.clt.dialogos.lego.ev3.nodes;

import java.awt.FlowLayout;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;


import com.clt.dialogos.lego.ev3.Ev3Node;
import com.clt.dialogos.lego.ev3.Ev3Runtime;
import com.clt.dialogos.lego.ev3.Plugin;
import com.clt.dialogos.lego.ev3.Resources;
import com.clt.diamant.WozInterface;
import com.clt.diamant.graph.nodes.NodeExecutionException;
import com.clt.script.cmd.ExecutionException;

/**
 * @author dabo
 *
 */
public class StopProgramNode extends Ev3Node {

//    private static final String CHECK_RUNNING = "checkRunning";

    public StopProgramNode() {

//        this.setProperty(StopProgramNode.CHECK_RUNNING, Boolean.FALSE);
        this.addEdge(Resources.getString("ProgramStopped"));
    }

    @Override
    protected JComponent createEditorComponentImpl(Map<String, Object> properties) {

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));

//        p.add(NodePropertiesDialog.createCheckBox(properties,
//                StopProgramNode.CHECK_RUNNING,
//                Resources.getString("CheckIfRunning")));

        return p;
    }

    /*
    @Override
    public boolean editProperties(Component parent) {
        boolean oldCheck = this.getBooleanProperty(StopProgramNode.CHECK_RUNNING);
        if (super.editProperties(parent)) {
            boolean newCheck = this.getBooleanProperty(StopProgramNode.CHECK_RUNNING);
            if (!oldCheck && newCheck) {
                this.getEdge(0).setCondition(Resources.getString("ProgramStopped"));
                this.addEdge(Resources.getString("NoProgramRunning"));
            } else if (oldCheck && !newCheck) {
                this.removeEdge(1);
            }

            return true;
        } else {
            return false;
        }
    }
*/

    @Override
    protected int executeEv3(WozInterface comm) {

        try {
            Ev3Runtime runtime = (Ev3Runtime) this.getPluginRuntime(Plugin.class, comm);
            if (runtime.getBrick() == null) {
                throw new ExecutionException(Resources.getString("NoNxtBrickSelected"));
            }
            
            runtime.getBrick().stopProgram();
            
            /*
            boolean check = this.getBooleanProperty(StopProgramNode.CHECK_RUNNING);
            
            if( check ) {
                byte status = runtime.getBrick().FILE.checkStatus();
                if( status == 1 ) { // still busy
                    return 1;
                }
            }
*/
            
            return 0;
        } catch (Exception exn) {
            throw new NodeExecutionException(this, Resources.getString("CouldNotStopProgram"), exn);
        }
    }

    /*
    @Override
    protected void readAttribute(XMLReader r, String name, String value, IdMap uid_map)            throws SAXException {

        if (name.equals(StopProgramNode.CHECK_RUNNING)) {
            boolean check = value.equalsIgnoreCase("1");
            this.setProperty(StopProgramNode.CHECK_RUNNING, check ? Boolean.TRUE
                    : Boolean.FALSE);
        } else {
            super.readAttribute(r, name, value, uid_map);
        }
    }

    @Override
    protected void writeAttributes(XMLWriter out, IdMap uid_map) {

        if (this.getBooleanProperty(StopProgramNode.CHECK_RUNNING)) {
            Graph.printAtt(out, StopProgramNode.CHECK_RUNNING, true);
        }
    }
*/

}
