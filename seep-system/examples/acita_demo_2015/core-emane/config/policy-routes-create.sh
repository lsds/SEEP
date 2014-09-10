#!/bin/bash

ANDROID_MASTER_NET="192.168.56.0/24"
ANDROID_MASTER_IP="192.168.56.101"
ANDROID_MASTER_ETH="eth2"
ANDROID_MASTER_ETH_IP="192.168.56.103"

ANDROID_MASTER_VETH="veth7.host"
ANDROID_MASTER_VETH_NET="10.0.7.0/24"
ANDROID_MASTER_VETH_IP="10.0.7.11"
ANDROID_MASTER_VETH_IP_CORE="10.0.7.10"

ANDROID_WORKER1_NET="192.168.129.0/24"
ANDROID_WORKER1_IP="192.168.129.101"
ANDROID_WORKER1_ETH="eth3"
ANDROID_WORKER1_ETH_IP="192.168.129.102"

ANDROID_WORKER1_VETH="veth8.host"
ANDROID_WORKER1_VETH_NET="10.0.8.0/24"
ANDROID_WORKER1_VETH_IP="10.0.8.11"
ANDROID_WORKER1_VETH_IP_CORE="10.0.8.10"

#ANDROID_MASTER=192.168.56.101
#ANDROID_MASTER=192.168.56.101
#ANDROID_MASTER=192.168.56.101
#ANDROID_MASTER=192.168.56.101

# Clear existing routes
ip route flush table android-master
ip route flush table android-worker1
ip rule del table android-master
ip rule del table android-worker1

# FROM Master
echo "ip rule from master"
ip rule add from $ANDROID_MASTER_IP dev $ANDROID_MASTER_ETH table android-master 
echo "ip route from master"
ip route add $ANDROID_WORKER1_NET via $ANDROID_MASTER_VETH_IP_CORE dev $ANDROID_MASTER_VETH table android-master 

# From Worker 1
echo "ip rule from worker"
ip rule add from $ANDROID_WORKER1_IP dev $ANDROID_WORKER1_ETH table android-worker1 
echo "ip route from worker"
ip route add $ANDROID_MASTER_NET via $ANDROID_WORKER1_VETH_IP_CORE dev $ANDROID_WORKER1_VETH table android-worker1 

# TO Master 
# N.B. TO Rules must be lower priority than the FROM rules above
echo "ip rule to master"
ip rule add to $ANDROID_MASTER_IP dev $ANDROID_MASTER_VETH table android-master
echo "ip route to master"
ip route add $ANDROID_MASTER_NET via $ANDROID_MASTER_IP dev $ANDROID_MASTER_ETH table android-master


#TODO: Should the dev here be ANDROID_WORKER1_VETH_CORE?
# To Worker 1
echo "ip rule to worker1"
ip rule add to $ANDROID_WORKER1_IP dev $ANDROID_WORKER1_VETH table android-worker1
echo "ip route to worker1"
ip route add $ANDROID_WORKER1_NET via $ANDROID_WORKER1_IP dev $ANDROID_WORKER1_ETH table android-worker1

ip route flush cache

