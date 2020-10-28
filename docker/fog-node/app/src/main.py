import os
import random
import json
import threading, time
from paho.mqtt import client as mqtt_client
from influxdb import InfluxDBClient
from ml import MachineLearning
from datetime import datetime

broker_address = 'localhost'
broker_port = 1883
broker_username = 'admin'
broker_password = 'admin'

influx_db_host = '52.44.221.201'
influx_db_port = 8086
influx_db_username = 'admin'
influx_db_password = 'admin'
influx_db_database = 'home'

city = "default"
topic = "weather/" + city
# generate client ID with pub prefix randomly
client_id = 'python-mqtt-fog-node-' + city
client_influx = InfluxDBClient(influx_db_host, influx_db_port, influx_db_username, influx_db_password, influx_db_database)

dataset = []

def connect_mqtt() -> mqtt_client:
    def on_connect(client, userdata, flags, rc):
        if rc == 0:
            print("Connected to MQTT Broker!")
        else:
            print("Failed to connect, return code %d\n", rc)

    client = mqtt_client.Client(client_id)
    client.on_connect = on_connect
    client.username_pw_set(broker_username, broker_password)
    client.connect(broker_address, broker_port)
    return client


def subscribe(client: mqtt_client):
    def on_message(client, userdata, msg):
        weatherObject = json.loads(msg.payload.decode())
        temp = []
        # convert to celsius
        converted_temp = weatherObject['temp'] - 273.15
        temp.append(converted_temp)
        dataset.append(temp)
        print(temp)

        # datetime object containing current date and time
        actual_time = datetime.now()
        json_body = [
            {
                "measurement": "temperature",
                "tags": {
                    "type": "measured"
                },
                "time": datetime.now().strftime('%Y-%m-%dT%H:%M:%SZ'),
                "fields": {
                    "temperature": float(converted_temp),
                    "city": city
                }
            }
        ]
        print("Write points: {0}".format(json_body))
        client_influx.write_points(json_body)        

    client.subscribe(topic)
    client.on_message = on_message

def print_every_n_seconds(n=60*1):
    while True:
        ml = MachineLearning(client_influx, city)
        time.sleep(n)

def run():

    try:
        city = os.getenv('CITY', 'default')
    except:
        print("Env variable CITY doesn't found")

    try:
        broker_address = os.getenv('BROKER_ADDRESS', 'localhost')
        broker_port = os.getenv('BROKER_PORT', 1883)
    except:
        print("Broker env variables missing")

    try:
        broker_username = os.getenv('BROKER_USERNAME', 'admin')
        broker_password = os.getenv('BROKER_PASSWORD', 'admin')
    except:
        print("Broker credentials env variables missing")

    thread = threading.Thread(target=print_every_n_seconds, daemon=True)
    thread.start()

    client = connect_mqtt()
    subscribe(client)
    client.loop_forever()


if __name__ == '__main__':
    run()