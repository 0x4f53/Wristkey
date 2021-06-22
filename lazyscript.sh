#!/usr/bin/env bash

read -p "Enter account name: " accountName
read -p "Enter shared secret: " sharedSecret
secret = `echo ${sharedSecret// /%s}`
adb shell input text "$accountName"
adb shell input keyevent 66
sleep 5
adb shell input text "$secret"
sleep 5
adb shell input keyevent 66
