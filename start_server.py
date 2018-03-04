'''
FILE NAME
start_server.py
Version 1.1
 
1. REQUIRES
* Any Raspberry Pi

2. HARDWARE
* Any Raspberry Pi
* DHT11 or 22
* 1 x YL - 69 Soil Moisture Sensor Module
* 10KOhm resistor
* Breadboard
* Wires

3. SOFTWARE
Command line terminal
Simple text editor
Libraries:
from flask import Flask, request, jsonify, Adafruit_DHT

// 4. END
'''

import fcntl
import json
import os
import time

import Adafruit_DHT
import RPi.GPIO as GPIO  # This is the GPIO library we need to use the GPIO pins on the Raspberry Pi
from flask import Flask, request, jsonify

os.environ['PYTHON_EGG_CACHE'] = '/var/www'

app = Flask(__name__)
app.debug = True  # Make this False if you are no longer debugging


class TempHumModel():
    def __init__(self, temp, hum):
        self.temp = temp
        self.hum = hum


class GenericResponse():
    def __init__(self, status, description):
        self.status = status
        self.description = description


@app.route("/")
def hello():
    return "Hello World!"


@app.route("/getTempHum", methods=['GET'])  # endpoint to get temperature and humidity values
def get_temp_hum():
    h, t = Adafruit_DHT.read_retry(11, 10)
    # app.logger.info("temp: %s", t)
    model = TempHumModel(t, h)
    # app.logger.info('successfully read temp: %s', model.temp)
    return jsonify(
        temperature=model.temp,
        humidity=model.hum)


@app.route("/checkPlant", methods=['GET'])  # check if output led is ON or OFF
def check_plant():
    # Set our GPIO numbering to BCM
    GPIO.setmode(GPIO.BCM)

    # Define the GPIO pin that we have our digital output from our sensor connected to
    channel = 2

    # Set the GPIO pin to an input
    GPIO.setup(channel, GPIO.IN)

    # read input from gpio 17
    input_value = GPIO.input(channel)
    app.logger.info("value %d", input_value)

    if input_value:
        app.logger.info("LED off - not enough water")
    else:
        app.logger.info("LED on - plant is happy")

    # test send push notification
    send_push_notification()

    return jsonify(enough_water=input_value)


@app.route("/check_motion", methods=['GET'])  # check if output of pin is 1 or 0
def check_motion():
    GPIO.setwarnings(False)
    # set out GPIO numbering to BCM
    GPIO.setmode(GPIO.BCM)

    # define the GPIO pin that we have our digital output
    GPIO.setup(17, GPIO.OUT)
    value = GPIO.input(17)
    if value == 0:  # When output from motion sensor is LOW
        print "No intruders", value
    elif value == 1:  # When output from motion sensor is HIGH
        print "Intruder detected", value
    else:
        print "Something went wrong!!! output:", value

    return jsonify(any_intruder=value)


@app.route('/sendRegKey', methods=['POST'])
def send_reg_key():
    try:
        content = request.get_json()
        if "deviceRegKey" in content:
            if content["deviceRegKey"]:
                deviceRegKey = content["deviceRegKey"]
            else:
                app.logger.error("no value for deviceRegKey")
                r1 = {'status': 902, 'message': 'no value for deviceId'}
                return jsonify(r1)
        else:
            app.logger.error("invalid json")
            r2 = {'status': 903, 'message': 'invalid json'}
            return jsonify(r2)
        if "fcmRegKey" in content:
            if content["fcmRegKey"]:
                fcmRegKey = content["fcmRegKey"]
            else:
                app.logger.error("no value for fcmRegKey")
                r3 = {'status': 904, 'message': 'no value for fcmRegKey'}
                return jsonify(r3)
        else:
            app.logger.error("invalid json")
            r4 = {'status': 903, 'message': 'invalid json'}
            return jsonify(r4)

    except ValueError as er:
        app.logger.error(er)
        return jsonify(GenericResponse('ERROR', er))

    status, msg = save_reg_key(deviceRegKey, fcmRegKey)

    if status:
        data = {'status': 200, 'message': msg}
    else:
        data = {'status': 901, 'message': msg}

    return jsonify(data)


@app.route("/getCandies", methods=['GET'])  # endpoint to action the candy dispenser
def get_candies():
    try:
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(20, GPIO.OUT)
        while True:
            GPIO.output(20, True)
            time.sleep(1)
            break
        GPIO.cleanup(20)
    except ValueError as er:
        app.logger.error(er)
        return jsonify(GenericResponse('ERROR', er))

    data = {'status': 200, 'message': 'ok'}
    return jsonify(data)


def save_reg_key(deviceRegKey, fcmRegKey):  # add deviceRegKey and fcmRegKey to local file

    # load local json file
    with touchopen('/var/www/devices.json', 'r+') as f:
        # Acquire a non-blocking exclusive lock
        fcntl.lockf(f, fcntl.LOCK_EX)

        obj = json.load(f)
        found = 0
        for i in obj["devices"]:
            if i["deviceRegKey"] == deviceRegKey:
                if i["fcmRegKey"] != fcmRegKey:
                    i["fcmRegKey"] = fcmRegKey
                    f.seek(0)
                    f.write(json.dumps(obj))
                    f.truncate()
                    return True, "Updated"
                found = 1
                break
        if found == 0:
            obj["devices"].append({"deviceRegKey": deviceRegKey, "fcmRegKey": fcmRegKey})
            f.seek(0)
            f.write(json.dumps(obj))
            f.truncate()
            return True, "Added"
        else:
            return True, "Already Exist"


def touchopen(filename, *args, **kwargs):
    # Open the file in R/W and create if it doesn't exist. *Don't* pass O_TRUNC
    fd = os.open(filename, os.O_RDWR | os.O_CREAT)

    # Encapsulate the low-level file descriptor in a python file object
    return os.fdopen(fd, *args, **kwargs)


def send_push_notification():  # send a fcm push notification to all android devices
    from pyfcm import FCMNotification

    API_KEY = 'fcm api key here'
    push_service = FCMNotification(api_key=API_KEY)

    # parse list of fcmRegKeys
    with touchopen('/var/www/devices.json', 'r') as f:
        fcntl.lockf(f, fcntl.LOCK_EX)

        obj = json.load(f)

        registration_ids = []
        for i in obj["devices"]:
            registration_ids.append(i["fcmRegKey"])

    # Send to multiple devices by passing a list of ids.
    app.logger.info("regId size:%d", len(registration_ids))

    message_title = "MySmartHome"
    message_body = "Check your home"
    result = push_service.notify_multiple_devices(registration_ids=registration_ids, message_title=message_title,
                                                  message_body=message_body)

    print result
