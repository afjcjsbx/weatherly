
import random
import json
import threading, time
from paho.mqtt import client as mqtt_client
from influxdb import InfluxDBClient
from ml import MachineLearning
from datetime import datetime

city = "Rome"
#city = os.getenv('CITY', 'Rome')

# broker mqtt endpoint
broker = 'broker.emqx.io'
port = 1883
topic = "weather/" + city
# generate client ID with pub prefix randomly
client_id = f'python-mqtt-{random.randint(0, 100)}'
client_influx = InfluxDBClient('localhost', 8086, 'afjcjsbx', 'admin', 'home')


dataset = []


def connect_mqtt() -> mqtt_client:
    def on_connect(client, userdata, flags, rc):
        if rc == 0:
            print("Connected to MQTT Broker!")
        else:
            print("Failed to connect, return code %d\n", rc)

    client = mqtt_client.Client(client_id)
    client.on_connect = on_connect
    client.connect(broker, port)
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
        print("entroooo")

        ml = MachineLearning(city)
        #ml.train(dataset)
        #dataset.clear()
        time.sleep(n)

def run():

    thread = threading.Thread(target=print_every_n_seconds, daemon=True)
    thread.start()

    client = connect_mqtt()
    subscribe(client)
    client.loop_forever()


if __name__ == '__main__':
    run()