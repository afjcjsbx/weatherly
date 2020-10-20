package com.afjcjsbx.iotsensor.nodekeeper;

import com.afjcjsbx.iotsensor.nodekeeper.interfaces.Observer;

public class NodeObserver extends Observer {

   private String address;
   private int port;

   public NodeObserver(String address, int port){
      this.address = address;
      this.port = port;
   }

   @Override
   public void update() {
      System.out.println( "My address: " + address + ":" + port);
   }
}