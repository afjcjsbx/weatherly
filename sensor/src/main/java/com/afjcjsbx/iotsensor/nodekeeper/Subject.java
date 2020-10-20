package com.afjcjsbx.iotsensor.nodekeeper;

import com.afjcjsbx.iotsensor.nodekeeper.interfaces.AttachNode;
import com.afjcjsbx.iotsensor.nodekeeper.interfaces.FindBestNode;
import com.afjcjsbx.iotsensor.nodekeeper.interfaces.Observer;
import com.afjcjsbx.iotsensor.util.MqttException;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Subject implements Serializable {

    private static long HERATHBEAT_TIME = 2000;

    private List<Node> nodes = new ArrayList<>();
    public List<Node> getNodes() {
        return nodes;
    }


    public void attach(Node node) {
        if(nodes.contains(node)){
            throw new MqttException("Node already exist!!");
        }
        nodes.add(node);
        System.err.println("Node added");
    }



    public void getNodesLoad(){
        if(nodes.isEmpty()){
            System.out.println("No nodes");
        }

        for(Node n: nodes){
            System.out.println(n.toString());
        }
    }


    public void garbageCollector(){
        nodes.removeIf(node -> System.currentTimeMillis() - node.getHearthbeat() > HERATHBEAT_TIME);
    }

}