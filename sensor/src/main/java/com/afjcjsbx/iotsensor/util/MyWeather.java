package com.afjcjsbx.iotsensor.util;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder(builderMethodName = "newBuilder")
public class MyWeather {

    private final String sensor_name;
    private final String zone;
    private final String locality_name;
    private final String country;
    private final float temp;
    private final float pressure;
    private final float humidity;
    private final float wind_speed;
    private final float wind_deg;
    private final float sunrise;
    private final float sunset;
    private final float lat;
    private final float lon;

}
