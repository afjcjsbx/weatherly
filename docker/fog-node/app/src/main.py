import os
import random
import json
import threading, time
from paho.mqtt import client as mqtt_client
from influxdb import InfluxDBClient
from ml import MachineLearning
from datetime import datetime
from pytz import timezone

broker_address = None
broker_port = 1883
broker_username = 'admin'
broker_password = 'admin'

influx_db_host = 'localhost'
influx_db_port = 8086
influx_db_username = 'admin'
influx_db_password = 'admin'
influx_db_database = 'home'

city = 'default'
topic = 'weather/'

# generate client ID with pub prefix randomly
client_id = ''
client_influx = None

dataset = []

def connect_mqtt() -> mqtt_client:
    def on_connect(client, userdata, flags, rc):
        if rc == 0:
            print("MQTT Broker connected, subscribing on topic: {0}".format(topic))
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
                "time": datetime.now(timezone('Europe/Rome')).strftime('%Y-%m-%dT%H:%M:%SZ'),
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

    global city
    city = os.getenv('CITY', 'Rome')
    global topic
    topic = topic + city
    global client_id
    client_id = 'python-mqtt-fog-node-' + city
    global broker_address
    broker_address = os.getenv('BROKER_ADDRESS', 'broker.emqx.io')

    global broker_port
    broker_port = int(os.environ.get('BROKER_PORT', 1883))

    global broker_username
    broker_username = os.getenv('BROKER_USERNAME', 'afjcjsbx')
    global broker_password
    broker_password = os.environ.get('BROKER_PASSWORD', 'qwerty')

    global influx_db_host
    influx_db_host = os.environ.get('INFLUXDB_ADDRESS', '52.44.221.201')
    global influx_db_port
    influx_db_port = int(os.environ.get('INFLUXDB_PORT', 8086))

    global influx_db_username
    influx_db_username = os.environ.get('INFLUXDB_USERNAME', 'afjcjsbx')
    global influx_db_password
    influx_db_password = os.environ.get('INFLUXDB_PASSWORD', 'admin')
    global influx_db_database
    influx_db_database = os.environ.get('INFLUXDB_DATABASE', 'home')

    global client_influx
    client_influx = InfluxDBClient(influx_db_host, influx_db_port, influx_db_username, influx_db_password, influx_db_database)

    print("*************************************************")
    print("fog-node-id: {0}".format(client_id))
    print("CITY: {0}".format(city))
    print("BROKER_ADDRESS: {0}".format(broker_address))
    print("BROKER_PORT: {0}".format(broker_port))
    print("BROKER_USERNAME: {0}".format(broker_username))
    print("BROKER_PASSWORD: {0}".format(broker_password))
    print("INFLUXDB_ADDRESS: {0}".format(influx_db_host))
    print("INFLUXDB_PORT: {0}".format(influx_db_port))
    print("INFLUXDB_USERNAME: {0}".format(influx_db_username))
    print("INFLUXDB_PASSWORD: {0}".format(influx_db_password))
    print("INFLUXDB_DATABASE: {0}".format(influx_db_database))
    print("*************************************************")


    thread = threading.Thread(target=print_every_n_seconds, daemon=True)
    thread.start()

    client = connect_mqtt()
    subscribe(client)
    client.loop_forever()


if __name__ == '__main__':
   run()