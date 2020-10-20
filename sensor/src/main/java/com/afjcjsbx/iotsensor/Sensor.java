package com.afjcjsbx.iotsensor;

import com.afjcjsbx.iotsensor.nodekeeper.interfaces.FindBestNode;
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
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.Naming;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


public class Sensor {

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
    private static final int PUBLISH_DATA_TIME_INTERVAL = 1000 * 30; // 30 seconds
    private static final int RETRY_RECONNECT_INTERVAL = 1000 * 30; // 30 seconds

    private static final int PORT = 8883;

    private static String bestNode;


    public Sensor() {
        clientId = StringUtils.getAlphaNumericString(32);
    }

    // Weather api
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather";

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    static void printUsage() {
        System.out.println(
                "Usage:\n" +
                        "  --help        This message\n" +
                        "  --clientId    Client ID to use when connecting (optional)\n" +
                        "  -z|--zone Weather locality or fog node \n" +
                        "  --lat Weather locality latitude in coordinates\n" +
                        "  --lon Weather locality longitude in coordinates\n" +
                        "  -e|--endpoint NoodeKeeper endpoint hostname\n" +
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

        Sensor sensor = new Sensor();
        sensor.init(args);

    }

    private void close() throws IOException {
        httpClient.close();
    }


    private void init(String[] args) throws InterruptedException {

        parseCommandLine(args);
        if (showHelp) {
            printUsage();
            return;
        }

        if (endpoint == null) {
            throw new MqttException("must provide a valid endpoint");
        } else if (lat == null || lon == null) {
            throw new MqttException("must provide a locality");
        } else if (clientId == null) {
            throw new MqttException("mqttClient must not be null");
        }


        while (true) {

            try {
                // lookup method to find reference of remote object
                FindBestNode access = (FindBestNode) Naming.lookup("rmi://localhost:1900/findBestNode");
                bestNode = access.findBestNode();
                System.err.println("Sensor attached to:" + bestNode);
            } // end try
            catch (Exception e) {
                System.err.println("EchoRMIClient exception: ");
                e.printStackTrace();
            }


            try {

                //MqttClient client = new MqttClient("tcp://" + endpoint + ":" + PORT, clientId, new MemoryPersistence());
                MqttClient client = new MqttClient("tcp://" + bestNode, clientId, new MemoryPersistence());
                MqttConnectOptions options = new MqttConnectOptions();
                client.setCallback(new SimpleMqttCallBack());

                options.setConnectionTimeout(60);
                options.setKeepAliveInterval(60);
                options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);

                System.out.println("starting connect the server...");
                client.connect(options);
                System.out.println("connected!");


                while (true) {
                    Sensor obj = new Sensor();

                    try {
                        String message = obj.sendGet();
                        /**
                         * Read JSON from a file into a Map
                         */

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

                        System.out.println("Publishing message: " + weather);
                        MqttMessage mqttMessage = new MqttMessage(new Gson().toJson(weather).getBytes());
                        mqttMessage.setQos(qos);

                        client.publish(topic, mqttMessage);
                        Thread.sleep(PUBLISH_DATA_TIME_INTERVAL);

                    } finally {
                        obj.close();
                    }

                }


            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(RETRY_RECONNECT_INTERVAL);
            }
        }

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
            System.out.println(result);
            return result;
        }

    }

}