package com.afjcjsbx.iotsensor.nodekeeper.interfaces;

import java.rmi.*;

public interface CheckNode extends Remote {
    public void sendPing(String nodeId) throws RemoteException, InterruptedException;
}