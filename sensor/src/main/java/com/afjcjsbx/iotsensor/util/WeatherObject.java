package com.afjcjsbx.iotsensor.util;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


import java.util.ArrayList;

@Getter
@Setter
@ToString
public class WeatherObject {

    private Coord coord;
    private ArrayList<Object> weather = new ArrayList<>();
    private String base;
    private Main main;
    private float visibility;
    private Wind wind;
    private Clouds clouds;
    private float dt;
    private Sys sys;
    private float timezone;
    private float id;
    private String name;
    private float cod;

    @Getter
    @Setter
    @ToString
    public class Sys {
        private float type;
        private float id;
        private String country;
        private float sunrise;
        private float sunset;
    }

    @Getter
    @Setter
    @ToString
    public class Clouds {
        private float all;
    }

    @Getter
    @Setter
    @ToString
    public class Wind {
        private float speed;
        private float deg;
    }

    @Getter
    @Setter
    @ToString
    public class Main {
        private float temp;
        private float feels_like;
        private float temp_min;
        private float temp_max;
        private float pressure;
        private float humidity;
    }

    @Getter
    @Setter
    @ToString
    public class Coord {
        private float lon;
        private float lat;
    }

}

