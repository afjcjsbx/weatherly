# Weatherly
## Weather forecasting machine learning with IoT devices and fog computing


Weatherly is a distributed weather forecast application that uses Recurrent neural network(RNN) on fog nodes and iot sensors for temperature monitoring.

### To run a Fog Node:
Build container image running the build.sh script or use the following command:
 ```
docker build -t fog-node:latest .
 ```
Run the Fog Node container with the following command:
 ```
docker run -e CITY=Rome -e BROKER_ADDRESS=192.168.1.2 -e BROKER_PORT=1883 -e BROKER_USERNAME=afjcjsbx
 -e BROKER_PASSWORD=password -e INFLUXDB_ADDRESS=1.2.3.4 -e INFLUXDB_PORT=8086 -e INFLUXDB_DATABASE=home 
-e INFLUXDB_USERNAME=afjcjsbx -e INFLUXDB_PASSWORD=password -p 8086:8086 -p 1883:1883 fog-node
 ```
or use docker compose:
 ```
version: '3.6'

services:

    fog-node-1:
        image: fog-node:latest
        container_name: fog-node-1

        restart: always
        ports:
            - "8086:8086"
            - "1883:1883"
        environment:
            - CITY=Rome
            - BROKER_ADDRESS=broker.emqx.io
            - BROKER_PORT=1883
            - BROKER_USERNAME=username
            - BROKER_PASSWORD=password
            - INFLUXDB_ADDRESS=1.2.3.4
            - INFLUXDB_PORT=8086
            - INFLUXDB_DATABASE=home
            - INFLUXDB_USERNAME=username
            - INFLUXDB_PASSWORD=password
 ```
### To run a sensor:
Build Jar from iot-sensor-mqtt module and run:
```
java -cp "iot-sensor-mqtt-1.0-SNAPSHOT-jar-with-dependencies.jar" com.afjcjsbx.iotsensor.SensorPython
```
### Arguments ##

| Parameter                 | Example       | Description   |	
| :------------------------ |:-------------:| :-------------|
| --clientId 	       |	"weather-sensor-1-name"          | Client ID to use when connecting
| --lat 	       |	41.89          |Weather locality latitude in coordinates
| --lon          | 13.59           |Weather locality longitude in coordinates
| -e or --endpoint 	       |	"broker.emqx.io"	            |Local fog node ip or Mqtt cloud service
| -apikey or --apikey         | 52cb6acs8ejvn42hc4b92bffc187beac             | openweathermap.org private API key
| -t or --topic          | /weather/Rome          | Topic name to publish data



### Libs
Java libs used in sensor:
- [Apache HttpComponents](https://hc.apache.org/)
- [Project Lombok](https://projectlombok.org/)
- [gson](https://github.com/google/gson)

Python libs used:
- [influxdb-python](https://github.com/influxdata/influxdb-python)
- [numpy](https://numpy.org/)
- [pandas](https://pandas.pydata.org/)
- [TensorFlow](https://www.tensorflow.org/)
- [Keras](https://keras.io/)
- [paho-mqtt](https://pypi.org/project/paho-mqtt/)


### Note



Abstract: Internet of Things (IoT) is adding value to products
and applications in the recent years. The connectivity of the IoT
devices over the network has widely reduced the power
consumption, robustness and connectivity to access data over the
network. IoT is powering many frontiers of industries and is seen
as a promising technology to take Big Data Analytics to a level
higher. Weather monitoring system as a module is an issue
among IoT research community and it has been widely
addressed. A new weather monitoring system is developed using
various sensors connecting to Raspberry Pi. The implementation
and data visualization on the data collected are discussed in this
paper in detail. Weather parameters like temperature, humidity,
PM 2.5 and PM 10 concentrations and Air Quality Index (AQI)
are monitored and visualized in graphical means using the
Raspberry Pi as server and data accessed over the intranet or
internet in a specified subnet or world wide web. The data
visualization is provided as result and proves to be a robust
framework for analyzing weather parameters in any
geographical location studying the effect of smog and PM 2.5
concentration.
