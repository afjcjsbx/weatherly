FROM java:8-jdk-alpine
COPY /iot-sensor-mqtt-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "iot-sensor-mqtt-1.0-SNAPSHOT-jar-with-dependencies.jar"]