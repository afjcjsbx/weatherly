import logging
from datetime import datetime
from influxdb import InfluxDBClient
import os

import boto3

def function_handler(event, context):

    # influx configuration - edit these
    ifuser = "grafana"
    ifpass = "admin"
    ifdb   = "home"
    ifhost = "127.0.0.1"
    ifport = 8086
    measurement_name = "weather"

    # take a timestamp for this measurement
    time = datetime.utcnow()

    # format the data as a single measurement for influx
    body = [
        {
            "measurement": measurement_name,
            "time": time,
            "fields": {
                "sensor_name": event['sensor_name'],
                "locality_name":  event['locality_name'],
                "country": event['country'],
                "temp": event['temp'],
                "pressure":  event['pressure'],
                "humidity": event['humidity'],
                "wind_speed": event['wind_speed'],
                "wind_deg":  event['wind_deg'],
                "sunrise": event['sunrise'],
                "sunset": event['sunset'],
                "lat": event['lat'],
                "lon": event['lon']
            }
        }
    ]

    # connect to influx
    ifclient = InfluxDBClient(ifhost,ifport,ifuser,ifpass,ifdb)

    # write the measurement
    ifclient.write_points(body)





    #----------------------------------------------------#
    # Cloud

    # influx configuration - edit these
    ifhost = os.environ['remote_node_ip']

    # connect to influx
    ifclient = InfluxDBClient(ifhost,ifport,ifuser,ifpass,ifdb)
    # write the measurement
    ifclient.write_points(body)

    #----------------------------------------------------#



    return
