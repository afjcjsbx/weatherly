package com.afjcjsbx.iotsensor;

import com.afjcjsbx.iotsensor.util.*;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Random;


public class SensorPython {
    private static final float MIN_TEMPERATURE_IN_KELVIN = 276.15f;
    private static final float MAX_TEMPERATURE_IN_KELVIN = 293.15f;

    private static final String EMQ_USERNAME = "afjcjsbx";
    private static final String EMQ_PASSWORD = "qwerty";

    private static String clientId;
    private static String lat;
    private static String lon;
    private static String zone = "unassigned-zone";

    private static String endpoint;
    private static String topic;
    private static String apiKey;


    private static boolean showHelp = false;

    // Configuration parameters
    private static final int qos = 0;
    private static final int PUBLISH_DATA_TIME_INTERVAL = 1000 ; // 30 seconds
    private static final int RETRY_RECONNECT_INTERVAL = 1000 * 30; // 30 seconds

    private static final int NEW_PORT = 1883;

    // Weather api
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather";

    private final CloseableHttpClient httpClient = HttpClients.createDefault();


    public SensorPython(double lat, double lon, String endpoint, String topic, String apikey) {
        clientId = StringUtils.getAlphaNumericString(32);
        this.lat = String.valueOf(lat);
        this.lon = String.valueOf(lon);
        this.endpoint = endpoint;
        this.topic = topic;
        this.apiKey = apikey;
    }



    static void printUsage() {
        System.out.println(
                "Usage:\n" +
                        "  --help        This message\n" +
                        "  --clientId    Client ID to use when connecting (optional)\n" +
                        "  -z|--zone Weather locality or fog node name\n" +
                        "  --lat Weather locality latitude in coordinates\n" +
                        "  --lon Weather locality longitude in coordinates\n" +
                        "  -e|--endpoint AWS Greengrass Core endpoint hostname\n" +
                        "  -r|--rootca   Path to the root certificate\n" +
                        "  -c|--cert     Path to the IoT thing certificate\n" +
                        "  -k|--key      Path to the IoT thing private key\n" +
                        "  -apikey|--apikey      Openweather private api key\n" +
                        "  -t|--topic    topic to subscribe\n"
        );
    }

    static void parseCommandLine(String[] args) {
        for (int idx = 0; idx < args.length; ++idx) {
            switch (args[idx]) {
                case "--help":
                    showHelp = true;
                    break;
                case "-id":
                case "--clientId":
                    if (idx + 1 < args.length) {
                        clientId = args[++idx];
                    }
                    break;
                case "-z":
                case "--zone":
                    if (idx + 1 < args.length) {
                        zone = args[++idx];
                    }
                    break;
                case "--lat":
                    if (idx + 1 < args.length) {
                        lat = args[++idx];
                    }
                    break;
                case "--lon":
                    if (idx + 1 < args.length) {
                        lon = args[++idx];
                    }
                    break;
                case "-e":
                case "--endpoint":
                    if (idx + 1 < args.length) {
                        endpoint = args[++idx];
                    }
                    break;
                case "-t":
                case "--topic":
                    if (idx + 1 < args.length) {
                        topic = args[++idx];
                    }
                    break;
                case "-apikey":
                case "--apikey":
                    if (idx + 1 < args.length) {
                        apiKey = args[++idx];
                    }
                    break;
                default:
                    System.out.println("Unrecognized argument: " + args[idx]);
            }
        }
    }

    public static void main(String[] args) throws Exception {

        parseCommandLine(args);
        if (showHelp) {
            printUsage();
            return;
        }

    }

    public void init() throws InterruptedException {
        if (endpoint == null) {
            throw new MqttException("must provide a valid endpoint");
        } else if (lat == null || lon == null) {
            throw new MqttException("must provide a locality");
        } else if (clientId == null) {
            throw new MqttException("mqttClient must not be null");
        }



        while (true) {
            try {

                MqttClient client = new MqttClient("tcp://"+endpoint + ":" + NEW_PORT, clientId,  new MqttDefaultFilePersistence("/tmp"));
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setUserName(EMQ_USERNAME);
                connOpts.setPassword(EMQ_PASSWORD.toCharArray());
                client.setCallback(new SimpleMqttCallBack());

                //options.setConnectionTimeout(60);
                //options.setKeepAliveInterval(60);
                connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);


                System.out.println("starting connect the server...");
                client.connect(connOpts);
                System.out.println("connected!");


                while (true) {

                    try {

                        float temperature = generateRandomTemperature(MIN_TEMPERATURE_IN_KELVIN, MAX_TEMPERATURE_IN_KELVIN);
                        String message = "{\"coord\":{\"lon\":12.38,\"lat\":41.76},\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"clear sky\",\"icon\":\"01d\"}],\"base\":\"stations\",\"main\":{\"temp\":" + temperature + ",\"feels_like\":287.16,\"temp_min\":292.04,\"temp_max\":292.59,\"pressure\":1017,\"humidity\":55},\"visibility\":10000,\"wind\":{\"speed\":7.2,\"deg\":290},\"clouds\":{\"all\":0},\"dt\":1603897442,\"sys\":{\"type\":1,\"id\":6795,\"country\":\"IT\",\"sunrise\":1603863482,\"sunset\":1603901414},\"timezone\":3600,\"id\":6698334,\"name\":\"Vitinia\",\"cod\":200}";
                        //String message = sendGet();
                        /**
                         * Read JSON from a file into a Map
                         */
                        System.out.println("message: " +message);

                        Gson gson = new Gson();
                        WeatherObject event = gson.fromJson(message, WeatherObject.class);

                        MyWeather weather = MyWeather.newBuilder()
                                .sensor_name(clientId)
                                .zone(zone)
                                .locality_name(event.getName())
                                .country(event.getSys().getCountry())
                                .temp(event.getMain().getTemp())
                                .humidity(event.getMain().getHumidity())
                                .pressure(event.getMain().getPressure())
                                .wind_speed(event.getWind().getSpeed())
                                .wind_deg(event.getWind().getDeg())
                                .sunrise(event.getSys().getSunrise())
                                .sunset(event.getSys().getSunset())
                                .lat(event.getCoord().getLat())
                                .lon(event.getCoord().getLon())
                                .build();

                        System.out.println("Publishing message: " + new Gson().toJson(weather));
                        MqttMessage mqttMessage = new MqttMessage(new Gson().toJson(weather).getBytes());
                        mqttMessage.setQos(qos);

                        client.publish(topic, mqttMessage);
                        Thread.sleep(PUBLISH_DATA_TIME_INTERVAL);

                    } finally {
                        //obj.close();
                    }

                }


            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(RETRY_RECONNECT_INTERVAL);
            }
        }
    }

    private void close() throws IOException {
        httpClient.close();
    }


    private String sendGet() throws Exception {

        URIBuilder b = new URIBuilder(API_URL);
        b.addParameter("lat", lat);
        b.addParameter("lon", lon);
        b.addParameter("appid", apiKey);

        HttpGet request = new HttpGet(b.build());
        // add request headers
        request.addHeader(HttpHeaders.USER_AGENT, "WeatherSensor");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            // Get HttpResponse Status
            System.out.println(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            // return it as a String
            String result = EntityUtils.toString(entity);
            //System.out.println(result);
            return result;
        }

    }


    private float generateRandomTemperature(float min, float max){
        return (float) Math.random() * (max - min + 1) + min;
    }

}