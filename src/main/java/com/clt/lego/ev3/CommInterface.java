/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.lego.ev3;

import java.io.IOException;

/**
 *
 * @author koller
 */
public interface CommInterface {
    public void send(byte[] data) throws IOException;
    public byte[] read(int expectedLength) throws IOException;
}
