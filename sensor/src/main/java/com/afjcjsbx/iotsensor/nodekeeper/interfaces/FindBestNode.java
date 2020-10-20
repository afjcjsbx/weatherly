package com.afjcjsbx.iotsensor.nodekeeper.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FindBestNode extends Remote {
    public String findBestNode() throws RemoteException, InterruptedException;
}  