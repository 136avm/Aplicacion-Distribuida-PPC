#!/bin/bash

parar() {
    if kill -0 $PID_SERVIDOR 2>/dev/null; then
        kill $PID_SERVIDOR
    fi

    if kill -0 $PID_CLIENTE 2>/dev/null; then
        kill $PID_CLIENTE
    fi

    exit 0
}

trap parar SIGINT

mvn clean package || exit 1

java -jar target/MainServidor-jar-with-dependencies.jar &
PID_SERVIDOR=$!

java -jar target/ClienteGUI-jar-with-dependencies.jar &
PID_CLIENTE=$!

wait

