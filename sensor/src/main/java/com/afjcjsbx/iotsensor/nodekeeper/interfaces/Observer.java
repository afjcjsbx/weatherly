package com.afjcjsbx.iotsensor.nodekeeper.interfaces;

import com.afjcjsbx.iotsensor.nodekeeper.Subject;

import java.util.List;

public abstract class Observer {
   protected int sensorCount;
   public abstract void update();
}