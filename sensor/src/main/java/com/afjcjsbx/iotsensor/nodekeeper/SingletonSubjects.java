package com.afjcjsbx.iotsensor.nodekeeper;

import java.rmi.RemoteException;

// Java program implementing Singleton class
// with getInstance() method 
public class SingletonSubjects {
    // static variable single_instance of type Singleton 
    private static SingletonSubjects single_instance = null;
  
    // variable of type String 
    public Subject subject;
  
    // private constructor restricted to this class itself 
    private SingletonSubjects() throws InterruptedException {
        subject = new Subject();
    } 
  
    // static method to create instance of Singleton class 
    public static SingletonSubjects getInstance() throws InterruptedException {
        if (single_instance == null) 
            single_instance = new SingletonSubjects();
  
        return single_instance; 
    } 
} 