package com.clt.lego;

import java.awt.Component;
import java.io.IOException;

import com.clt.util.UserCanceledException;

/**
 * @author dabo
 *
 */
public interface BrickFactory {

    public String[] getAvailablePorts() throws IOException;

    public BrickDescription getBrickInfo(Component parent, String port)
            throws IOException, UserCanceledException;
}
