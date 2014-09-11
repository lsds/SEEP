#!/bin/bash

ANDROID_MASTER_NET="192.168.56.0/24"
ANDROID_MASTER_IP="192.168.56.101"
VMCORE_ANDROID_MASTER_ETH="eth2"
VMCORE_ANDROID_MASTER_ETH_IP="192.168.56.103"

VMCORE_MASTER_VETH="veth7.host"
MASTER_VETH_NET="10.0.7.0/24"
VMCORE_MASTER_VETH_IP="10.0.7.11"
CORE_MASTER_VETH_IP="10.0.7.10"

ANDROID_WORKER1_NET="192.168.129.0/24"
ANDROID_WORKER1_IP="192.168.129.104"
VMCORE_ANDROID_WORKER1_ETH="eth3"
VMCORE_ANDROID_WORKER1_ETH_IP="192.168.129.103"

VMCORE_WORKER1_VETH="veth8.host"
WORKER1_VETH_NET="10.0.8.0/24"
VMCORE_WORKER1_VETH_IP="10.0.8.11"
CORE_WORKER1_VETH_IP="10.0.8.10"

#ANDROID_MASTER=192.168.56.101
#ANDROID_MASTER=192.168.56.101
#ANDROID_MASTER=192.168.56.101
#ANDROID_MASTER=192.168.56.101

# Clear existing routes
ip route flush table android-master
ip route flush table android-worker1
ip route flush table android-worker2
ip route flush table android-worker3
ip route flush table android-worker4
ip route flush table android-worker5
ip route flush table android-worker6
ip rule del table android-master
ip rule del table android-worker1
ip rule del table android-worker2
ip rule del table android-worker3
ip rule del table android-worker4
ip rule del table android-worker5
ip rule del table android-worker6

# FROM Master
echo "ip rules from"
ip rule add from $ANDROID_MASTER_IP dev $VMCORE_ANDROID_MASTER_ETH table android-master 
ip rule add from $ANDROID_WORKER1_IP dev $VMCORE_ANDROID_WORKER1_ETH table android-worker1 
ip rule add from $ANDROID_WORKER2_IP dev $VMCORE_ANDROID_WORKER2_ETH table android-worker2 
ip rule add from $ANDROID_WORKER3_IP dev $VMCORE_ANDROID_WORKER3_ETH table android-worker3 
ip rule add from $ANDROID_WORKER4_IP dev $VMCORE_ANDROID_WORKER4_ETH table android-worker4 
ip rule add from $ANDROID_WORKER5_IP dev $VMCORE_ANDROID_WORKER5_ETH table android-worker5 
ip rule add from $ANDROID_WORKER6_IP dev $VMCORE_ANDROID_WORKER6_ETH table android-worker6 


# N.B. TO Rules must be lower priority than the FROM rules above
#Think this is wrong?
echo "ip rules to"
ip rule add to $ANDROID_MASTER_IP dev $VMCORE_MASTER_VETH table android-master
ip rule add to $ANDROID_WORKER1_IP dev $VMCORE_WORKER1_VETH table android-worker1
ip rule add to $ANDROID_WORKER2_IP dev $VMCORE_WORKER2_VETH table android-worker2
ip rule add to $ANDROID_WORKER3_IP dev $VMCORE_WORKER3_VETH table android-worker3
ip rule add to $ANDROID_WORKER4_IP dev $VMCORE_WORKER4_VETH table android-worker4
ip rule add to $ANDROID_WORKER5_IP dev $VMCORE_WORKER5_VETH table android-worker5
ip rule add to $ANDROID_WORKER6_IP dev $VMCORE_WORKER6_VETH table android-worker6


echo "ip route from master"
ip route add $ANDROID_WORKER1_NET via $CORE_MASTER_VETH_IP dev $VMCORE_MASTER_VETH table android-master 
ip route add $ANDROID_WORKER2_NET via $CORE_MASTER_VETH_IP dev $VMCORE_MASTER_VETH table android-master 
ip route add $ANDROID_WORKER3_NET via $CORE_MASTER_VETH_IP dev $VMCORE_MASTER_VETH table android-master 
ip route add $ANDROID_WORKER4_NET via $CORE_MASTER_VETH_IP dev $VMCORE_MASTER_VETH table android-master 
ip route add $ANDROID_WORKER5_NET via $CORE_MASTER_VETH_IP dev $VMCORE_MASTER_VETH table android-master 
ip route add $ANDROID_WORKER6_NET via $CORE_MASTER_VETH_IP dev $VMCORE_MASTER_VETH table android-master 

echo "ip route from worker1"
ip route add $ANDROID_MASTER_NET via $CORE_WORKER1_VETH_IP dev $ANDROID_WORKER1_VETH table android-worker1 
ip route add $ANDROID_WORKER2_NET via $CORE_WORKER1_VETH_IP dev $ANDROID_WORKER1_VETH table android-worker1 
ip route add $ANDROID_WORKER3_NET via $CORE_WORKER1_VETH_IP dev $ANDROID_WORKER1_VETH table android-worker1 
ip route add $ANDROID_WORKER4_NET via $CORE_WORKER1_VETH_IP dev $ANDROID_WORKER1_VETH table android-worker1 
ip route add $ANDROID_WORKER5_NET via $CORE_WORKER1_VETH_IP dev $ANDROID_WORKER1_VETH table android-worker1 
ip route add $ANDROID_WORKER6_NET via $CORE_WORKER1_VETH_IP dev $ANDROID_WORKER1_VETH table android-worker1 

echo "ip route from worker2"
ip route add $ANDROID_MASTER_NET via $CORE_WORKER2_VETH_IP dev $ANDROID_WORKER2_VETH table android-worker2 
ip route add $ANDROID_WORKER1_NET via $CORE_WORKER2_VETH_IP dev $ANDROID_WORKER2_VETH table android-worker2 
ip route add $ANDROID_WORKER3_NET via $CORE_WORKER2_VETH_IP dev $ANDROID_WORKER2_VETH table android-worker2 
ip route add $ANDROID_WORKER4_NET via $CORE_WORKER2_VETH_IP dev $ANDROID_WORKER2_VETH table android-worker2 
ip route add $ANDROID_WORKER5_NET via $CORE_WORKER2_VETH_IP dev $ANDROID_WORKER2_VETH table android-worker2 
ip route add $ANDROID_WORKER6_NET via $CORE_WORKER2_VETH_IP dev $ANDROID_WORKER2_VETH table android-worker2 


echo "ip route from worker3"
ip route add $ANDROID_MASTER_NET via $CORE_WORKER3_VETH_IP dev $ANDROID_WORKER3_VETH table android-worker3 
ip route add $ANDROID_WORKER1_NET via $CORE_WORKER3_VETH_IP dev $ANDROID_WORKER3_VETH table android-worker3 
ip route add $ANDROID_WORKER2_NET via $CORE_WORKER3_VETH_IP dev $ANDROID_WORKER3_VETH table android-worker3 
ip route add $ANDROID_WORKER4_NET via $CORE_WORKER3_VETH_IP dev $ANDROID_WORKER3_VETH table android-worker3 
ip route add $ANDROID_WORKER5_NET via $CORE_WORKER3_VETH_IP dev $ANDROID_WORKER3_VETH table android-worker3 
ip route add $ANDROID_WORKER6_NET via $CORE_WORKER3_VETH_IP dev $ANDROID_WORKER3_VETH table android-worker3 

echo "ip route from worker4"
ip route add $ANDROID_MASTER_NET via $CORE_WORKER4_VETH_IP dev $ANDROID_WORKER4_VETH table android-worker4 
ip route add $ANDROID_WORKER1_NET via $CORE_WORKER4_VETH_IP dev $ANDROID_WORKER4_VETH table android-worker4 
ip route add $ANDROID_WORKER2_NET via $CORE_WORKER4_VETH_IP dev $ANDROID_WORKER4_VETH table android-worker4 
ip route add $ANDROID_WORKER3_NET via $CORE_WORKER4_VETH_IP dev $ANDROID_WORKER4_VETH table android-worker4 
ip route add $ANDROID_WORKER5_NET via $CORE_WORKER4_VETH_IP dev $ANDROID_WORKER4_VETH table android-worker4 
ip route add $ANDROID_WORKER6_NET via $CORE_WORKER4_VETH_IP dev $ANDROID_WORKER4_VETH table android-worker4 

echo "ip route from worker5"
ip route add $ANDROID_MASTER_NET via $CORE_WORKER5_VETH_IP dev $ANDROID_WORKER5_VETH table android-worker5 
ip route add $ANDROID_WORKER1_NET via $CORE_WORKER5_VETH_IP dev $ANDROID_WORKER5_VETH table android-worker5 
ip route add $ANDROID_WORKER2_NET via $CORE_WORKER5_VETH_IP dev $ANDROID_WORKER5_VETH table android-worker5 
ip route add $ANDROID_WORKER3_NET via $CORE_WORKER5_VETH_IP dev $ANDROID_WORKER5_VETH table android-worker5 
ip route add $ANDROID_WORKER4_NET via $CORE_WORKER5_VETH_IP dev $ANDROID_WORKER5_VETH table android-worker5 
ip route add $ANDROID_WORKER6_NET via $CORE_WORKER5_VETH_IP dev $ANDROID_WORKER5_VETH table android-worker5 


echo "ip route from worker6"
ip route add $ANDROID_MASTER_NET via $CORE_WORKER6_VETH_IP dev $ANDROID_WORKER6_VETH table android-worker6 
ip route add $ANDROID_WORKER1_NET via $CORE_WORKER6_VETH_IP dev $ANDROID_WORKER6_VETH table android-worker6 
ip route add $ANDROID_WORKER2_NET via $CORE_WORKER6_VETH_IP dev $ANDROID_WORKER6_VETH table android-worker6 
ip route add $ANDROID_WORKER3_NET via $CORE_WORKER6_VETH_IP dev $ANDROID_WORKER6_VETH table android-worker6 
ip route add $ANDROID_WORKER4_NET via $CORE_WORKER6_VETH_IP dev $ANDROID_WORKER6_VETH table android-worker6 
ip route add $ANDROID_WORKER5_NET via $CORE_WORKER6_VETH_IP dev $ANDROID_WORKER6_VETH table android-worker6 

# TO Master 
echo "ip routes to."
ip route add $ANDROID_MASTER_NET via $ANDROID_MASTER_IP dev $VMCORE_ANDROID_MASTER_ETH table android-master
ip route add $ANDROID_WORKER1_NET via $ANDROID_WORKER1_IP dev $VMCORE_ANDROID_WORKER1_ETH table android-worker1
ip route add $ANDROID_WORKER2_NET via $ANDROID_WORKER2_IP dev $VMCORE_ANDROID_WORKER2_ETH table android-worker2
ip route add $ANDROID_WORKER3_NET via $ANDROID_WORKER3_IP dev $VMCORE_ANDROID_WORKER3_ETH table android-worker3
ip route add $ANDROID_WORKER4_NET via $ANDROID_WORKER4_IP dev $VMCORE_ANDROID_WORKER4_ETH table android-worker4
ip route add $ANDROID_WORKER5_NET via $ANDROID_WORKER5_IP dev $VMCORE_ANDROID_WORKER5_ETH table android-worker5
ip route add $ANDROID_WORKER6_NET via $ANDROID_WORKER6_IP dev $VMCORE_ANDROID_WORKER6_ETH table android-worker6

ip route flush cache

