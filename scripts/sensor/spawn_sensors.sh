#!/bin/bash
java -cp iot-sensor-mqtt-1.0-SNAPSHOT-jar-with-dependencies.jar com.afjcjsbx.iotsensor.TestSensors -z Rome --lat 42.12 --lon 13.12 -e nodekeeper -p 1900 -t weather --apikey 52cb6ac6b2056c8f84b92bffc187beac &
