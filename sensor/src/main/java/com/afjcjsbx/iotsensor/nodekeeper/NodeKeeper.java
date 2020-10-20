package com.afjcjsbx.iotsensor.nodekeeper;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import com.afjcjsbx.iotsensor.nodekeeper.interfaces.AttachNode;
import com.afjcjsbx.iotsensor.nodekeeper.interfaces.CheckNode;
import com.afjcjsbx.iotsensor.nodekeeper.interfaces.FindBestNode;
import com.afjcjsbx.iotsensor.nodekeeper.listener.AttachNodeImpl;
import com.afjcjsbx.iotsensor.nodekeeper.listener.CheckNodeImpl;
import com.afjcjsbx.iotsensor.nodekeeper.listener.FindBestNodeImpl;
import com.afjcjsbx.iotsensor.util.MqttException;

public class NodeKeeper extends UnicastRemoteObject {

    private static final long serialVersionUID = 1L;

    protected NodeKeeper() throws RemoteException {
        super();
    }



    public static void main(String[] args) throws InterruptedException {
        SingletonSubjects.getInstance();

        try {

            // rmiregistry within the server JVM with
            // port number 1900
            LocateRegistry.createRegistry(1900);
            AttachNode serverOperation = new AttachNodeImpl();
            FindBestNode findBestNodeOperation = new FindBestNodeImpl();
            CheckNode sendHearthbeatOperation = new CheckNodeImpl();

            // Binds the remote object by the name
            Naming.rebind("rmi://localhost:1900"+
                    "/attachNode", serverOperation);

            Naming.rebind("rmi://localhost:1900"+
                    "/findBestNode", findBestNodeOperation);

            Naming.rebind("rmi://localhost:1900"+
                    "/sendHearthbeat", sendHearthbeatOperation);

            System.err.println("Server ready");

            while (true) {
                try {
                    SingletonSubjects.getInstance().subject.getNodesLoad();
                    SingletonSubjects.getInstance().subject.garbageCollector();
                } catch (MqttException e) {
                    System.out.println("InterruptedException Exception" + e.getMessage());
                }
                Thread.sleep(2000);
            }


        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

    }




}