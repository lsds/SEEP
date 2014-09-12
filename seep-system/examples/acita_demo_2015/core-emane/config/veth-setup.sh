#!/bin/bash
# Must invoke this with sudo
source demo_env.sh

echo "Creating veths."
#ip link add $CORE_MASTER_VETH type veth peer name $VMCORE_MASTER_VETH 
echo $CORE_WORKER1_VETH $VMCORE_WORKER1_VETH
ip link add "$CORE_WORKER1_VETH" type veth peer name "$VMCORE_WORKER1_VETH" 
ip link add "$CORE_WORKER2_VETH" type veth peer name "$VMCORE_WORKER2_VETH" 
ip link add "$CORE_WORKER3_VETH" type veth peer name "$VMCORE_WORKER3_VETH" 
ip link add "$CORE_WORKER4_VETH" type veth peer name "$VMCORE_WORKER4_VETH" 
ip link add "$CORE_WORKER5_VETH" type veth peer name "$VMCORE_WORKER5_VETH" 
ip link add "$CORE_WORKER6_VETH" type veth peer name "$VMCORE_WORKER6_VETH" 

#ifconfig $VMCORE_MASTER_VETH  "$VMCORE_MASTER_VETH_IP/24"
echo "Assigning veth ips"
ifconfig $VMCORE_WORKER1_VETH  "$VMCORE_WORKER1_VETH_IP/24"
ifconfig $VMCORE_WORKER2_VETH  "$VMCORE_WORKER2_VETH_IP/24"
ifconfig $VMCORE_WORKER3_VETH  "$VMCORE_WORKER3_VETH_IP/24"
ifconfig $VMCORE_WORKER4_VETH  "$VMCORE_WORKER4_VETH_IP/24"
ifconfig $VMCORE_WORKER5_VETH  "$VMCORE_WORKER5_VETH_IP/24"
ifconfig $VMCORE_WORKER6_VETH  "$VMCORE_WORKER6_VETH_IP/24"
#ifconfig veth7.host "10.0.7.11/24"
 
#ifconfig $VMCORE_MASTER_VETH up 
echo "Bringing up veths."
ifconfig $VMCORE_WORKER1_VETH up 
ifconfig $VMCORE_WORKER2_VETH up 
ifconfig $VMCORE_WORKER3_VETH up 
ifconfig $VMCORE_WORKER4_VETH up 
ifconfig $VMCORE_WORKER5_VETH up 
ifconfig $VMCORE_WORKER6_VETH up 

#ifconfig veth7.host up

sysctl -w net.ipv4.ip_forward=1
