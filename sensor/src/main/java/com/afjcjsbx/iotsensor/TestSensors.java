package com.afjcjsbx.iotsensor;


import com.afjcjsbx.iotsensor.util.CoordinatesUtils;

import java.util.Arrays;

public class TestSensors {

    public static void main(String[] args) {

        String[] arguments = "-z Rome --lat 42.12 --lon 13.12 -e nodekeeper -p 1900 -t weather --apikey 52cb6ac6b2056c8f84b92bffc187beac".split(" ");
        String OPENWEATHER_API_KEY = "52cb6ac6b2056c8f84b92bffc187beac";
        double LATITUDE_ROME = 41.90278;
        double LONGITUDE_ROME = 12.49636;
        double RADIUS_IN_METERS = 30000;

        int num = 10;
        for(int i = 0; i < num; i++ ){

            Thread thread = new Thread("New Thread") {
                public void run(){
                    try {
                        CoordinatesUtils.Location currentLocation = new CoordinatesUtils.Location(LATITUDE_ROME, LONGITUDE_ROME);
                        CoordinatesUtils.Location randomLocation = CoordinatesUtils.getLocationInLatLngRad(RADIUS_IN_METERS, currentLocation);

                        System.out.println(randomLocation);
                        Sensor sensor = new Sensor("Rome", randomLocation.getLatitude(),
                                randomLocation.getLongitude(), "nodekeeper", 1900, "weather", OPENWEATHER_API_KEY);
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
