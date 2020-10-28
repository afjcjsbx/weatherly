package com.afjcjsbx.iotsensor;


import com.afjcjsbx.iotsensor.util.CoordinatesUtils;

import java.util.Arrays;

public class TestSensors {

    public static String OPENWEATHER_API_KEY = "52cb6ac6b2056c8f84b92bffc187beac";
    public static double LATITUDE_ROME = 41.90278;
    public static double LONGITUDE_ROME = 12.49636;
    public static double RADIUS_IN_METERS = 30000;
    public static String BROKER_ENDPOINT = "35.158.147.153";
    public static String TOPIC_NAME = "weather/Rome";

    public static void main(String[] args) {

        int num = 1;
        for(int i = 0; i < num; i++ ){

            Thread thread = new Thread("New Thread") {
                public void run(){
                    try {
                        CoordinatesUtils.Location currentLocation = new CoordinatesUtils.Location(LATITUDE_ROME, LONGITUDE_ROME);
                        CoordinatesUtils.Location randomLocation = CoordinatesUtils.getLocationInLatLngRad(RADIUS_IN_METERS, currentLocation);

                        System.out.println(randomLocation);
                        SensorPython sensor = new SensorPython(randomLocation.getLatitude(),
                                randomLocation.getLongitude(), BROKER_ENDPOINT, TOPIC_NAME, OPENWEATHER_API_KEY);
                                sensor.init();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();


        }


    }
}
