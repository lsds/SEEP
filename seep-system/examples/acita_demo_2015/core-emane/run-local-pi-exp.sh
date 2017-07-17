#!/bin/bash

screen -p 3 -X stuff "./run-pi-worker.sh 3501\n"
screen -p 4 -X stuff "./run-pi-worker.sh 3502\n"
screen -p 5 -X stuff "./run-pi-worker.sh 3503\n"
