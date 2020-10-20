# Weatherly
## IoT Weather Monitoring System with Fog Computing


1. Setup fog node
2. Install Greengrass on your fog node
3. Install influxDB
4. Install Grafana
5. Deploy local lambda function on your fog node using Aws Greengrass Core Console
6. Setup Devices (sensors)
7. Run sensors on the same network of fog node

### To run a sensor:
Build Jar from iot-sensor-mqtt module and run:
```
java -cp "iot-sensor-mqtt-1.0-SNAPSHOT-jar-with-dependencies.jar" com.afjcjsbx.iotsensor.MqttPublishSample
```
### Arguments ##

| Parameter                 | Example       | Description   |	
| :------------------------ |:-------------:| :-------------|
| --clientId 	       |	"weather-sensor-1-north"          | Client ID to use when connecting
| -z or --zone 	       |	"Colosseum"          |Weather locality or fog node name 
| --lat 	       |	41.89          |Weather locality latitude in coordinates
| --lon          | 13.59           |Weather locality longitude in coordinates
| -e or --endpoint 	       |	"127.0.0.1"	            |Local fog node ip or AWS Greengrass Core endpoint hostname
| -r or --rootca		       | certs/root-ca-cert.pem	           | Path to the root certificate
| -c or --cert  	        | /certs/d30d4126ed.cert.pem         | Path to the IoT device certificate
| -k or --key         | certs/d30d4126ed.private.key             | Path to the IoT device private key
| -apikey or --apikey         | 52cb6acs8ejvn42hc4b92bffc187beac             | openweathermap.org private API key
| -t or --topic          | /hello/world/pubsub           | Topic name to publish data


### Grafana ##
1. To visualize wheather data connect to the fog node: http://fog-node-ip-address:3000 (make sure you have the 3000 port open)
2. Import the dashboard.json in grafana folder

### Libs
Java libs used in sensor:
- [PApache HttpComponents](https://hc.apache.org/)
- [Project Lombok](https://projectlombok.org/)
- [gson](https://github.com/google/gson)

Python libs used in lambda function:
- [influxdb-python](https://github.com/influxdata/influxdb-python)
- [requests](https://github.com/psf/requests)
- [MessagePack](https://github.com/msgpack/msgpack-python)
- [pytz](https://pypi.org/project/pytz/)
- [certifi](https://github.com/certifi/python-certifi)
- [chardet](https://github.com/chardet/chardet)
- [idna](https://pypi.org/project/idna/0.6/)
- [boto3](https://github.com/boto/boto3)
- [urllib3](https://github.com/urllib3/urllib3)


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
