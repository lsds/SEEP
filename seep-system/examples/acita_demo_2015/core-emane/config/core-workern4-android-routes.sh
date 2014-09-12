#!/bin/bash
# Master 
source demo_env.sh
ip route add $ANDROID_WORKER4_NET via $VMCORE_WORKER4_VETH_IP dev $CORE_VMCORE_ETH

#ip route add $ANDROID_MASTER_NET via $CORE_MASTER_IP dev $CORE_MANET_ETH 
ip route add $ANDROID_WORKER1_NET via $CORE_WORKER1_IP dev $CORE_MANET_ETH 
ip route add $ANDROID_WORKER2_NET via $CORE_WORKER2_IP dev $CORE_MANET_ETH 
ip route add $ANDROID_WORKER3_NET via $CORE_WORKER3_IP dev $CORE_MANET_ETH 
ip route add $ANDROID_WORKER5_NET via $CORE_WORKER5_IP dev $CORE_MANET_ETH 
ip route add $ANDROID_WORKER6_NET via $CORE_WORKER6_IP dev $CORE_MANET_ETH 

#WORKER2_ANDROID_NET="193.168.XXX.0/24"
#WORKER2_CORE_IP="10.0.0.11"

#ip route add $WORKER2_ANDROID_NET via $WORKER2_CORE_IP dev $CORE_ETH 

#WORKER3_ANDROID_NET="193.168.XXX.0/24"
#WORKER3_CORE_IP="10.0.0.12"

#ip route add $WORKER3_ANDROID_NET via $WORKER3_CORE_IP dev $CORE_ETH 

#WORKER4_ANDROID_NET="193.168.XXX.0/24"
#WORKER4_CORE_IP="10.0.0.13"

#ip route add $WORKER4_ANDROID_NET via $WORKER4_CORE_IP dev $CORE_ETH 
