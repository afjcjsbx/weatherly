package com.afjcjsbx.iotsensor.nodekeeper;

public class Node {

    private String nodeId;
    private int sensorsCount;
    private String address;
    private int port;
    private long hearthbeat;

    public Node(String nodeId, String address, int port) {
        this.nodeId = nodeId;
        this.sensorsCount = 0;
        this.address = address;
        this.port = port;
        this.hearthbeat = System.currentTimeMillis();
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setHearthbeat(long hearthbeat) {
        this.hearthbeat = hearthbeat;
    }

    public long getHearthbeat() {
        return hearthbeat;
    }

    public void addSensor(){
        sensorsCount+=1;
    }
    public int getSensorsCount() {
        return sensorsCount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Node{" +
                "nodeId='" + nodeId + '\'' +
                ", sensorsCount=" + sensorsCount +
                ", address='" + address + '\'' +
                ", port=" + port +
                '}';
    }
}
