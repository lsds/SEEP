#!/bin/bash

expDir=$(date +%H-%M-%S-%a%d%m%y)
mkdir -p log/$expDir
cp ../tmp/*.log log/$expDir

