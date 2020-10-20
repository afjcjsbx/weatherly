package com.afjcjsbx.iotsensor.nodekeeper.listener;

import com.afjcjsbx.iotsensor.nodekeeper.Node;
import com.afjcjsbx.iotsensor.nodekeeper.NodeObserver;
import com.afjcjsbx.iotsensor.nodekeeper.SingletonSubjects;
import com.afjcjsbx.iotsensor.nodekeeper.interfaces.AttachNode;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AttachNodeImpl extends UnicastRemoteObject implements AttachNode {

    public AttachNodeImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean addNode(String nodeId, String address, int port) throws InterruptedException {
        SingletonSubjects.getInstance().subject.attach(new Node(nodeId, address, port));
        return true;
    }

}