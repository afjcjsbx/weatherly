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
import com.afjcjsbx.iotsensor.util.NodeException;

public class NodeKeeper extends UnicastRemoteObject {

    private static final long serialVersionUID = 1L;

    protected NodeKeeper() throws RemoteException {
        super();
    }



    public static void main(String[] args) throws InterruptedException {
        SingletonSubjects.getInstance();

        try {

            String address = System.getenv("ADDRESS");
            if(address == null){
                throw new NodeException("Environment variable ADDRESS not set");
            }

            String port = System.getenv("PORT");
            if(port == null){
                throw new NodeException("Environment variable PORT not set");
            }
            // rmiregistry within the server JVM with
            // port number 1900
            LocateRegistry.createRegistry(Integer.parseInt(port));
            AttachNode serverOperation = new AttachNodeImpl();
            FindBestNode findBestNodeOperation = new FindBestNodeImpl();
            CheckNode sendHearthbeatOperation = new CheckNodeImpl();

            // Binds the remote object by the name
            Naming.rebind("rmi://" + address + ":" + port +
                    "/attachNode", serverOperation);

            Naming.rebind("rmi://" + address + ":" + port +
                    "/findBestNode", findBestNodeOperation);

            Naming.rebind("rmi://" + address + ":" + port +
                    "/sendHearthbeat", sendHearthbeatOperation);

            System.err.println("Server ready on " + address + ":" + port);

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