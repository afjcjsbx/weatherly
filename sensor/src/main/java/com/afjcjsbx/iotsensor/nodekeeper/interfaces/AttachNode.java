package com.afjcjsbx.iotsensor.nodekeeper.interfaces;

import java.rmi.*;

public interface AttachNode extends Remote {
    public boolean addNode(String nodeId, String address, int port) throws RemoteException, InterruptedException;
}