# ![alt text](icon.png) Weatherly 

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

