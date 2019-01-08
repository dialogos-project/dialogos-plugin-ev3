/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.lego.ev3;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.EV3FactoryUsb;
import elmot.javabrick.ev3.Ev3FactoryBluetooth;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author koller
 */
public class Ev3Descriptor implements Comparable<Ev3Descriptor> {

    public static enum ConnectionTypes {
        USB, BLUETOOTH, WIFI, DUMMY
    }
    
    private ConnectionTypes connectionType;
    private String port;
    
    private static List<Ev3Descriptor> allDescriptors = new ArrayList<>();

    public Ev3Descriptor(ConnectionTypes connectionType, String port) {
        this.connectionType = connectionType;
        this.port = port;
    }

    public ConnectionTypes getConnectionType() {
        return connectionType;
    }

    public String getPort() {
        return port;
    }
    
    public EV3 instantiate() throws IOException {
        switch (connectionType) {
            case USB:
                return EV3FactoryUsb.instantiate(this);
            case BLUETOOTH:
                return Ev3FactoryBluetooth.instantiate(this);
            default:
                return null;
        }
    }
    
    public static void discoverAll() {
        allDescriptors.clear();        
        EV3FactoryUsb.discoverDevices(allDescriptors);
        Ev3FactoryBluetooth.discoverDevices(allDescriptors);
    }
    
    public static List<Ev3Descriptor> getAllDescriptors() {
        return allDescriptors;
    }

    @Override
    public String toString() {
        return String.format("%s://%s", connectionType.toString().toLowerCase(), port);
    }
    


    @Override
    public int compareTo(Ev3Descriptor t) {
        return Comparator.comparing(Ev3Descriptor::getConnectionType).thenComparing(Ev3Descriptor::getPort).compare(this, t);
    }    
}
