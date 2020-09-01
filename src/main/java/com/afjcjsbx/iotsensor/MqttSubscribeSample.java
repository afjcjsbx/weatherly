package com.afjcjsbx.iotsensor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.afjcjsbx.iotsensor.util.SimpleMqttCallBack;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MqttSubscribeSample {

    private static final String topic = "/update";
    private static final String content = "Message_from_MqttPublishSample";
    private static final int qos = 1;
    private static final String broker = "tcp://localhost:1883";
    private static final String clientId = "JavaSample";

    private static final String API_KEY = "52cb6ac6b2056c8f84b92bffc187beac";
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=Veroli&appid=" + API_KEY;


    public static void main(String[] args) {

        String serverUrl = "ssl://192.168.1.9:8883";
        String caFilePath = "src/main/resources/certs/core-root-ca-cert.pem";
        String clientCrtFilePath = "src/main/resources/certs/05bf32660c.cert.pem";
        String clientKeyFilePath = "src/main/resources/certs/05bf32660c.private.key";
        String mqttUserName = "HelloWorld_Subscriber";

        MqttClient client;
        try {
            client = new MqttClient(serverUrl, mqttUserName, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            //options.setUserName(mqttUserName);
            //options.setPassword(mqttPassword.toCharArray());

            options.setConnectionTimeout(60);
            options.setKeepAliveInterval(60);
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);


            SSLSocketFactory socketFactory = getSocketFactory(caFilePath,
                    clientCrtFilePath, clientKeyFilePath, "");
            options.setSocketFactory(socketFactory);

            client.setCallback(new SimpleMqttCallBack());

            System.out.println("starting connect the server...");
            client.connect(options);
            System.out.println("connected!");
            Thread.sleep(1000);

            client.subscribe(
                    topic,
                    0);
            //client.disconnect();
            //System.out.println("disconnected!");


        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                new java.security.cert.Certificate[] { cert });
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }

}