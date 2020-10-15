## IoT Based Weather Monitoring System


### Parameters (file const.go) ##

| Parameter                 | Example       | Description   |	
| :------------------------ |:-------------:| :-------------|
| --clientId 	       |	"Queue"          |queue name 
| --lat 	       |	"127.0.0.1"          |Weather locality latitude in coordinates
| --lon          | 12345           |Weather locality longitude in coordinates
| -e or --endpoint 	       |	10	            |Local fog node ip or AWS Greengrass Core endpoint hostname
| -r or --rootca		       | 15	           | Path to the root certificate
| -c or --cert  	        | 1         | Path to the IoT thing certificate
| -k or --key         | 1             | Path to the IoT thing private key
| -t or --topic          | /weather           | Topic to subscribe
## TODOs ##

### Libs
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
