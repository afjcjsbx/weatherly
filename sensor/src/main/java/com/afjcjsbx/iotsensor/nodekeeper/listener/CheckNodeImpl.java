package com.afjcjsbx.iotsensor.nodekeeper.listener;

import com.afjcjsbx.iotsensor.nodekeeper.Node;
import com.afjcjsbx.iotsensor.nodekeeper.SingletonSubjects;
import com.afjcjsbx.iotsensor.nodekeeper.interfaces.AttachNode;
import com.afjcjsbx.iotsensor.nodekeeper.interfaces.CheckNode;
import com.afjcjsbx.iotsensor.nodekeeper.interfaces.FindBestNode;
import com.afjcjsbx.iotsensor.util.MqttException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class CheckNodeImpl extends UnicastRemoteObject implements CheckNode {

    public CheckNodeImpl() throws RemoteException {
        super();
    }


    @Override
    public void sendPing(String nodeId) throws InterruptedException {
        List<Node> nodes = SingletonSubjects.getInstance().subject.getNodes();
        if (nodes.size() == 0) {
            throw new MqttException("No fog nodes available");
        }

        for(Node n: nodes){
            if(n.getNodeId().equals(nodeId)){
                n.setHearthbeat(System.currentTimeMillis());
            }
        }

    }
}