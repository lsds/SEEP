#!/bin/bash

# Orig
# net.ipv4.tcp_frto = 2
# net.ipv4.tcp_frto_response = 0
# net.ipv4.tcp_low_latency = 0

# Low latency
#sysctl net.ipv4.tcp_frto=1
#sysctl net.ipv4.tcp_frto_response=2
#sysctl net.ipv4.tcp_low_latency=1

sysctl net.ipv4.tcp_frto=2
sysctl net.ipv4.tcp_frto_response=0
sysctl net.ipv4.tcp_low_latency=1


