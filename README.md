# mysmarthome
smart home project which connects sensors to a raspberry pi and a mobile client app

# Introduction
My personal project which implements a DIY smarthome environment. It includes a web server Flask application with uWSGI and Nginx and a client application (android app).
The server it's written in Python and I host it to one of my raspberry pi. I have connected some sensors like a temperature and humidity sensor (1 x DHT11), a soil moisture sensor (1 x YL - 69) and an electrical candy dispencer. 
The android application receive push notifications when the plant which have the humidity sensor detects that it needs water.
Also through the android application using google vision api we detect face and if it's happy I let the candy dispencer to serve candies (m&ms) :)

# Prerequisites
A raspberry pi (I have the pi 3 model) with raspbian-jessie OS.
Install all of the pieces that we need:
- will install pip, the Python package manager, in order to install and manage your Python components:
Python 2:
sudo apt-get update
sudo apt-get install python-pip python-dev nginx
Install Flask and uWSGI:
pip install uwsgi flask

![pic1](/docs/IMG_1.jpg)
