import numpy as np
import pandas as pd
from influxdb import InfluxDBClient
import logging
import sys
from datetime import datetime, timedelta
import json
import time

class MachineLearning:
	def __init__(self, city):
		# body of the constructor
		self.city = city
		self.train()

	def train(self):

		# datetime object containing current date and time
		now = datetime.now()
		#start_time = now.strftime('%Y-%m-%dT%H:%M:%SZ')
		start_time = datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%SZ')
		print(start_time)


		client = InfluxDBClient('localhost', 8086, 'afjcjsbx', 'admin', 'home')
		query_measured = "select mean(temperature) from temperature where city = '" + self.city + "' and type != 'predicted' and time < now() + 60m  group by time(1m) fill(previous) order by time desc limit 30"
		rs = client.query(query_measured)

		training_set = []
		measurement = list(rs.get_points(measurement='temperature'))
		for i in measurement:
			value = []
			value.append(i['mean'])
			training_set.append(value)

		training_set.reverse()
		print("Training set: {0}\n".format(training_set))

		#Feature Scaling 
		from sklearn.preprocessing import MinMaxScaler
		from sklearn.preprocessing import StandardScaler

		sc = MinMaxScaler(feature_range=(0,1))
		# sc = StandardScaler()
		training_set_scaled = sc.fit_transform(training_set)


		x_train = []
		y_train = []
		n_future = 5 # next 15 minutes temperature forecast
		n_past = 30 # Past 60 minutes
		for i in range(0,len(training_set_scaled)-n_past-n_future+1):
		    x_train.append(training_set_scaled[i : i + n_past , 0])     
		    y_train.append(training_set_scaled[i + n_past : i + n_past + n_future , 0 ])
		x_train , y_train = np.array(x_train), np.array(y_train)
		x_train = np.reshape(x_train, (x_train.shape[0] , x_train.shape[1], 1) )

		#print(x_train)
		#print(y_train)


		from keras.models import Sequential
		from keras.layers import LSTM,Dense ,Dropout
		# Fitting RNN to training set using Keras Callbacks. Read Keras callbacks docs for more info.
		from keras.layers import Bidirectional


		regressor = Sequential()
		regressor.add(Bidirectional(LSTM(units=30, return_sequences=True, input_shape = (x_train.shape[1],1) ) ))
		regressor.add(Dropout(0.2))
		regressor.add(LSTM(units= 30 , return_sequences=True))
		regressor.add(Dropout(0.2))
		regressor.add(LSTM(units= 30 , return_sequences=True))
		regressor.add(Dropout(0.2))
		regressor.add(LSTM(units= 30))
		regressor.add(Dropout(0.2))
		regressor.add(Dense(units = n_future, activation='linear'))
		regressor.compile(optimizer='adam', loss='mean_squared_error',metrics=['acc'])
		regressor.fit(x_train, y_train, epochs=25, batch_size=1)


		query_measured = "select mean(temperature) from temperature where city = '" + self.city + "' and type != 'predicted' and  time >= now() - 5m  group by time(1m) fill(previous) order by time desc limit 50"
		rs = client.query(query_measured)

		test_set = []
		measurement = list(rs.get_points(measurement='temperature'))
		for i in measurement:
			val = []
			val.append(i['mean'])
			test_set.append(val)

		test_set.reverse()
		print("Test set: {0}\n".format(test_set))

		testing = sc.transform(test_set)
		testing = np.array(testing)
		testing = np.reshape(testing,(testing.shape[1],testing.shape[0],1))

		predicted_temperature = regressor.predict(testing)
		predicted_temperature = sc.inverse_transform(predicted_temperature)
		predicted_temperature = np.reshape(predicted_temperature,(predicted_temperature.shape[1],predicted_temperature.shape[0]))

		predictions = predicted_temperature.tolist()
		print("Valori predetti: {0}\n".format(predictions))



		for index in range(n_future):
			#future_time = start_time + timedelta(minutes=index * 1)
			future_time = (now + timedelta(minutes = index * 1)).strftime('%Y-%m-%dT%H:%M:%SZ')
			#predictions[index].append(future_time)
			json_body = [
				{
					"measurement": "temperature",
					"tags": {
						"type": "predicted"
					},
					"time": future_time,
					"fields": {
						"temperature": predictions[index][0],
						"city": self.city,
					}
				}
			]
			print("Scrivo punto: {0}\n".format(json_body))
			client.write_points(json_body)

		print(predictions)



	def run(self):
		format = "%(asctime)s: %(message)s"
		logging.basicConfig(format=format, level=logging.INFO, datefmt="%H:%M:%S")

		d.logging.info("Main    : before creating thread")
		t1 = threading.Thread(target=self.train)
		t1.start()
		logging.info("Main    : wait for the thread to finish")


