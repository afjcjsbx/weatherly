package com.afjcjsbx.iotsensor.nodekeeper.listener;

import com.afjcjsbx.iotsensor.nodekeeper.Node;
import com.afjcjsbx.iotsensor.nodekeeper.SingletonSubjects;
import com.afjcjsbx.iotsensor.nodekeeper.interfaces.AttachNode;
import com.afjcjsbx.iotsensor.nodekeeper.interfaces.FindBestNode;
import com.afjcjsbx.iotsensor.util.MqttException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class FindBestNodeImpl extends UnicastRemoteObject implements FindBestNode {

    public FindBestNodeImpl() throws RemoteException {
        super();
    }


    @Override
    public synchronized String findBestNode() throws InterruptedException {
        List<Node> nodes = SingletonSubjects.getInstance().subject.getNodes();
        if (nodes.size() == 0) {
            throw new MqttException("No fog nodes available");
        }

        Node node = null;
        int bigInt = Integer.MAX_VALUE;
        for(Node n: nodes){
            if(n.getSensorsCount() <= bigInt){
                node = n;
                node.addSensor();
                bigInt = n.getSensorsCount();
            }
        }

        return node.getAddress() + ":" + node.getPort();
    }
}