package com.afjcjsbx.iotsensor;

import java.io.*;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.afjcjsbx.iotsensor.util.MqttException;
import com.afjcjsbx.iotsensor.util.MyWeather;
import com.afjcjsbx.iotsensor.util.SimpleMqttCallBack;
import com.afjcjsbx.iotsensor.util.WeatherObject;
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


public class MqttPublishSample {

    private static String clientId;
    private static String lat;
    private static String lon;

    private static String rootCaPath;
    private static String certPath;
    private static String keyPath;
    private static String endpoint;
    private static String topic;
    private static boolean showHelp = false;

    // Configuration parameters
    private static final int qos = 0;
    private static final int SLEEP_TIME = 1000 * 60;

    private static final int PORT = 8883;

    // Weather api
    private static final String API_KEY = "52cb6ac6b2056c8f84b92bffc187beac";
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather";

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    static void printUsage() {
        System.out.println(
                "Usage:\n" +
                        "  --help        This message\n" +
                        "  --clientId    Client ID to use when connecting (optional)\n" +
                        "  --lat Weather locality latitude in coordinates\n" +
                        "  --lon Weather locality longitude in coordinates\n" +
                        "  -e|--endpoint AWS Greengrass Core endpoint hostname\n" +
                        "  -r|--rootca   Path to the root certificate\n" +
                        "  -c|--cert     Path to the IoT thing certificate\n" +
                        "  -k|--key      Path to the IoT thing private key\n" +
                        "  -t|--topic    topic to subscribe\n"
        );
    }

    static void parseCommandLine(String[] args) {
        for (int idx = 0; idx < args.length; ++idx) {
            switch (args[idx]) {
                case "--help":
                    showHelp = true;
                    break;
                case "--clientId":
                    if (idx + 1 < args.length) {
                        clientId = args[++idx];
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
                case "-r":
                case "--rootca":
                    if (idx + 1 < args.length) {
                        rootCaPath = args[++idx];
                    }
                    break;
                case "-c":
                case "--cert":
                    if (idx + 1 < args.length) {
                        certPath = args[++idx];
                    }
                    break;
                case "-k":
                case "--key":
                    if (idx + 1 < args.length) {
                        keyPath = args[++idx];
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

        if (endpoint == null) {
            throw new MqttException("must provide a valid endpoint");
        } else if (lat == null || lon == null) {
            throw new MqttException("must provide a locality");
        } else if (clientId == null) {
            throw new MqttException("mqttClient must not be null");
        } else if (rootCaPath == null) {
            throw new MqttException("invalid rootCa");
        } else if (certPath == null) {
            throw new MqttException("invalid cert");
        } else if (keyPath == null) {
            throw new MqttException("invalid key");
        }



        while (true) {
            try {

                MqttClient client = new MqttClient("ssl://" + endpoint + ":" + PORT, clientId);
                MqttConnectOptions options = new MqttConnectOptions();
                client.setCallback(new SimpleMqttCallBack());

                options.setConnectionTimeout(60);
                options.setKeepAliveInterval(60);
                options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);

                SSLSocketFactory socketFactory = getSocketFactory(rootCaPath,
                        certPath, keyPath, "");
                options.setSocketFactory(socketFactory);

                System.out.println("starting connect the server...");
                client.connect(options);
                System.out.println("connected!");


                while (true) {
                    MqttPublishSample obj = new MqttPublishSample();

                    try {
                        String message = obj.sendGet();
                        /**
                         * Read JSON from a file into a Map
                         */

                        Gson gson = new Gson();
                        WeatherObject event = gson.fromJson(message, WeatherObject.class);

                        MyWeather weather = MyWeather.newBuilder()
                                .sensor_name(clientId)
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
                        Thread.sleep(SLEEP_TIME);

                    } finally {
                        obj.close();
                    }

                }


            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(5000);
            }
        }
    }

    private void close() throws IOException {
        httpClient.close();
    }

    private static SSLSocketFactory getSocketFactory(final String caCrtFile,
                                                     final String crtFile, final String keyFile, final String password)
            throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        X509Certificate caCert = null;

        FileInputStream fis = new FileInputStream(caCrtFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        while (bis.available() > 0) {
            caCert = (X509Certificate) cf.generateCertificate(bis);
            // System.out.println(caCert.toString());
        }

        // load client certificate
        bis = new BufferedInputStream(new FileInputStream(crtFile));
        X509Certificate cert = null;
        while (bis.available() > 0) {
            cert = (X509Certificate) cf.generateCertificate(bis);
            // System.out.println(caCert.toString());
        }

        // load client private key
        PEMParser pemParser = new PEMParser(new FileReader(keyFile));
        Object object = pemParser.readObject();
        PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder()
                .build(password.toCharArray());
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter()
                .setProvider("BC");
        KeyPair key;
        if (object instanceof PEMEncryptedKeyPair) {
            System.out.println("Encrypted key - we will use provided password");
            key = converter.getKeyPair(((PEMEncryptedKeyPair) object)
                    .decryptKeyPair(decProv));
        } else {
            System.out.println("Unencrypted key - no password needed");
            key = converter.getKeyPair((PEMKeyPair) object);
        }
        pemParser.close();

        // CA certificate is used to authenticate server
        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(caKs);

        // client key and certificates are sent to server so it can authenticate
        // us
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(),
                new java.security.cert.Certificate[]{cert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }


    private String sendGet() throws Exception {

        URIBuilder b = new URIBuilder(API_URL);
        b.addParameter("lat", lat);
        b.addParameter("lon", lon);
        b.addParameter("appid", API_KEY);

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