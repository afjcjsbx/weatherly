package com.afjcjsbx.iotsensor;

import com.afjcjsbx.iotsensor.nodekeeper.interfaces.AttachNode;
import com.afjcjsbx.iotsensor.nodekeeper.interfaces.CheckNode;
import com.afjcjsbx.iotsensor.util.MyWeather;
import com.afjcjsbx.iotsensor.util.NodeException;
import com.afjcjsbx.iotsensor.util.StringUtils;
import com.google.gson.Gson;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.server.Server;
import io.moquette.server.config.ClasspathConfig;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBIOException;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class FogNode {

    private String nodeId;
    private String address;
    private int port;

    private String rmiAddress;
    private String rmiPort;

    private InfluxDB influxDB;


    public FogNode(String address, int port) throws InterruptedException, RemoteException, NotBoundException, MalformedURLException {
        this.address = address;
        this.port = port;
        this.nodeId = StringUtils.getAlphaNumericString(32);

        rmiAddress = System.getenv("RMI_ADDRESS");
        if (rmiAddress == null) {
            throw new NodeException("Environment variable RMI_ADDRESS not set");
        }

        rmiPort = System.getenv("RMI_PORT");
        if (rmiPort == null) {
            throw new NodeException("Environment variable RMI_PORT not set");
        }

        System.err.println("Fog node ready on " + address + ":" + port);

        try {
            // lookup method to find reference of remote object
            AttachNode access = (AttachNode) Naming.lookup("rmi://" + rmiAddress + ":" + rmiPort +
                    "/attachNode");

            boolean response = access.addNode(nodeId, address, port);
        } catch (NotBoundException | RemoteException e) {
            System.err.println("Exception in RMI invoke");
            e.printStackTrace();
        }

        TimerTask timerTask = new HearthbeatTask();
        //running timer task as daemon thread
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private class PublisherListener extends AbstractInterceptHandler {
        @Override
        public void onPublish(InterceptPublishMessage message) {
            System.out.println("moquette mqtt broker message intercepted, topic: " + message.getTopicName()
                    + ", content: " + new String(message.getPayload().array()));

            String payload = new String(message.getPayload().array());
            Gson gson = new Gson();
            MyWeather event = gson.fromJson(payload, MyWeather.class);

            insertInDb(event);
        }
    }


    private boolean initInfluxDb() {

        String influxdbAddress = System.getenv("INFLUXDB_ADDRESS");
        if (influxdbAddress == null) {
            throw new NodeException("Environment variable INFLUXDB_ADDRESS not set");
        }

        try {
            // Create an object to handle the communication with InfluxDB.
            // (best practice tip: reuse the 'influxDB' instance when possible)
            final String serverURL = "http://" + influxdbAddress + ":8086", username = "root", password = "root";
            influxDB = InfluxDBFactory.connect(serverURL, username, password);

            // Create a database...
            String databaseName = "home";
            influxDB.query(new Query("CREATE DATABASE " + databaseName));
            influxDB.setDatabase(databaseName);

            // ... and a retention policy, if necessary.
            String retentionPolicyName = "one_day_only";
            influxDB.query(new Query("CREATE RETENTION POLICY " + retentionPolicyName
                    + " ON " + databaseName + " DURATION 1d REPLICATION 1 DEFAULT"));
            influxDB.setRetentionPolicy(retentionPolicyName);

            // Enable batch writes to get better performance.
            influxDB.enableBatch(BatchOptions.DEFAULTS);

            return true;
        }catch (InfluxDBIOException e){
            System.err.println("Exception in influxDB connection");
            e.printStackTrace();
            return false;
        }
    }


    private void insertInDb(MyWeather point) {
        influxDB.write(Point.measurement("weather")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("zone", point.getZone())
                .addField("locality_name", point.getLocality_name())
                .addField("country", point.getCountry())
                .addField("temp", point.getTemp())
                .addField("humidity", point.getHumidity())
                .addField("pressure", point.getPressure())
                .addField("wind_speed", point.getWind_speed())
                .addField("wind_deg", point.getWind_deg())
                .addField("sunrise", point.getSunrise())
                .addField("sunset", point.getSunset())
                .addField("lat", point.getLat())
                .addField("lon", point.getLon())
                .addField("country", point.getCountry())
                .addField("country", point.getCountry())
                .build());
    }


    public class HearthbeatTask extends TimerTask {

        @Override
        public void run() {
            System.out.println("Sending hearhbeat: " + nodeId);
            //System.out.println("Timer task started at:"+new Date());
            completeTask();
            //System.out.println("Timer task finished at:"+new Date());
        }

        private void completeTask() {
            try {
                CheckNode access = (CheckNode) Naming.lookup("rmi://" + rmiAddress + ":" + rmiPort + "/sendHearthbeat");
                try {
                    access.sendPing(nodeId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) throws IOException {


        String address = System.getenv("ADDRESS");
        if (address == null) {
            throw new NodeException("Environment variable ADDRESS not set");
        }

        String port = System.getenv("PORT");
        if (port == null) {
            throw new NodeException("Environment variable PORT not set");
        }


        FogNode fogNode;
        try {
            fogNode = new FogNode(address, Integer.parseInt(port));
            fogNode.init();

        } catch (NotBoundException | InterruptedException e) {
            e.printStackTrace();
        }


    }


    private void init() throws IOException, InterruptedException {
        // Initialize influxDB

        while(!initInfluxDb()){
            TimeUnit.SECONDS.sleep(3);
        }

        // Creating a MQTT Broker using Moquette
        //final IConfig classPathConfig = new ClasspathConfig();

        MemoryConfig config = new MemoryConfig(new Properties());
        config.setProperty("host", address);
        config.setProperty("port", Integer.toString(port));

        final Server mqttBroker = new Server();
        final List<? extends InterceptHandler> userHandlers = Collections.singletonList(new PublisherListener());
        mqttBroker.startServer(config, userHandlers);

        System.out.println("moquette mqtt broker started, press ctrl-c to shutdown..");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("stopping moquette mqtt broker..");
                mqttBroker.stopServer();
                System.out.println("moquette mqtt broker stopped");
            }
        });

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}