#!/bin/bash
# Must invoke this with sudo

ip link add veth7.core type veth peer name veth7.host
ip link add veth8.core type veth peer name veth8.host
ip link add veth9.core type veth peer name veth9.host
ip link add veth10.core type veth peer name veth10.host
ip link add veth11.core type veth peer name veth11.host

ifconfig veth7.host "10.0.7.11/24"
ifconfig veth8.host "10.0.8.11/24"
ifconfig veth9.host "10.0.9.11/24"
ifconfig veth10.host "10.0.10.11/24"
ifconfig veth11.host "10.0.11.11/24"
 
ifconfig veth7.host up
ifconfig veth8.host up
ifconfig veth9.host up
ifconfig veth10.host up
ifconfig veth11.host up

sysctl -w net.ipv4.ip_forward=1
