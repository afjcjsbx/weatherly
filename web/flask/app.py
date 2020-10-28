import requests
import configparser
from flask import Flask, render_template, request
from influxdb import InfluxDBClient
from datetime import datetime, timedelta
import json
from flask import jsonify

influxdb_host = '52.44.221.201'
influxdb_port = 8086
influxdb_username = 'afjcjsbx'
influxdb_password = 'admin'
influxdb_database = 'home'
client_influxdb = InfluxDBClient(influxdb_host, influxdb_port, influxdb_username, influxdb_password, influxdb_database)

app = Flask(__name__)
# weekdays as a tuple
weekDays = ("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")
 
@app.route('/')
def home():

    client = InfluxDBClient(influxdb_host, influxdb_port, influxdb_username, influxdb_password, influxdb_database)
    query_cities = "select distinct(city) as city from temperature;"

    rs = client_influxdb.query(query_cities)
    cities = []

    dataset = list(rs.get_points(measurement='temperature'))
    for i in dataset:
        cities.append(i['city'])

    return render_template('home.html', cities=cities)

@app.route('/weather')
def weather_dashboard():
    city = request.args.get('city')

    query_measured = "select mean(temperature) from temperature where city = '" + city + "' and type = 'measured' and time <= now() + 60m group by time(1m) fill(previous) order by time desc limit 50"
    query_predicted = "select * from temperature where city = '" + city + "' and type = 'predicted' and time >= now() - 30m order by time desc limit 150"

    line_labels = []
    line_measured_values = []


    rs = client_influxdb.query(query_measured)
    measurement = list(rs.get_points(measurement='temperature'))
    for j in measurement:
        t = datetime.strptime(j['time'], '%Y-%m-%dT%H:%M:%SZ')
        line_labels.append(t.strftime('%Y-%m-%dT%H:%M:%SZ'))

        mean = j['mean']
        line_measured_values.append(mean)

    line_labels.reverse()
    line_measured_values.reverse()


    labels_predicted = []
    predicted_values = []

    try:
        actual_temp = line_measured_values[len(line_measured_values)-1]
        actual_temp = round(actual_temp, 2)
    except:
        actual_temp = 0
        print("error to calculate actual temp")

    rs = client_influxdb.query(query_predicted)
    measurement = list(rs.get_points(measurement='temperature'))
    for j in measurement:
        t = datetime.strptime(j['time'], '%Y-%m-%dT%H:%M:%SZ')
        labels_predicted.append(t.strftime('%Y-%m-%dT%H:%M:%SZ'))

        mean = j['temperature']
        predicted_values.append(mean)

    print("measured labels: {0}".format(line_labels))
    print("measured values: {0}".format(line_measured_values))
    print("predicted labels: {0}".format(labels_predicted))
    print("predicted values: {0}".format(predicted_values))


    labels_predicted.reverse()
    predicted_values.reverse()

    try:
        max_predicted = max(predicted_values)
        max_measured = max(line_measured_values)
        max_value = max(max_predicted, max_measured) + 3
    except:
        max_value = 20
        print("error to calculate max range")


    mapping1=[]
    mapping2=[]
    for h, w in zip(line_labels, line_measured_values):
        mapping1.append({"x": h, "y": w})

    for h, w in zip(labels_predicted, predicted_values):
        mapping2.append({"x": h, "y": w})


    actual_date = datetime.now()
    actual_date_day = actual_date.weekday()
    actual_date_day_as_string = weekDays[actual_date_day]

    return render_template('line_chart.html', data1 = mapping1, data2 = mapping2, title='Weather forecast in ' + city, max=max_value, actual_temp=actual_temp, actual_day=actual_date_day_as_string)


if __name__ == '__main__':
    app.run()

