#!/bin/bash
set -e
#
#  tc uses the following units when passed as a parameter.
#  kbps: Kilobytes per second
#  mbps: Megabytes per second
#  kbit: Kilobits per second
#  mbit: Megabits per second
#  bps: Bytes per second
#       Amounts of data can be specified in:
#       kb or k: Kilobytes
#       mb or m: Megabytes
#       mbit: Megabits
#       kbit: Kilobits
#  To get the byte figure from bits, divide the number by 8 bit
#

#
# Name of the traffic control command.
TC=/sbin/tc

# The network interface we're planning on limiting bandwidth.
IF=wlan0 # Interface

# IP address of the machine we are controlling
#IP=191.168.181.106     # Host IP
IP=$(ifconfig wlan0 | grep 'inet addr:' | grep -oP '\d+\.\d+\.\d+\.\d+' | head -1)

# Filter options for limiting the intended interface.
U32="$TC filter add dev $IF protocol ip parent 1:0 prio 1 u32"

start() {

	DNLD=$1
	UPLD=$1
	LOSS=$1
echo "Throttling $IP to $1"
# We'll use Hierarchical Token Bucket (HTB) to shape bandwidth.
# For detailed configuration options, please consult Linux man
# page.

	$TC qdisc add dev $IF root handle 1: htb default 30
	$TC class add dev $IF parent 1: classid 1:2 htb rate 500mbit
	$TC qdisc add dev $IF parent 1:2 handle 10:0 netem loss $LOSS%
	$U32 match ip src $IP/32 flowid 1:2

# The first line creates the root qdisc, and the next two lines
# create two child qdisc that are to be used to shape download
# and upload bandwidth.
#
# The 4th and 5th line creates the filter to match the interface.
# The 'dst' IP address is used to limit download speed, and the
# 'src' IP address is used to limit upload speed.

# @dokeeffe: Seems this is the way to limit incoming. ffff:0 is the handle for the ingress queue (as opposed to 1:0 for root, the outgoing queue).
#$TC qdisc add dev $IF handle ffff: ingress
#$TC filter add dev $IF parent ffff: protocol ip prio 1 u32 match ip dst $IP police rate $DNLD burst 10k drop flowid :1

	#This controls packet loss instead of just limiting bandwidth
	ip link set dev ifb0 up
	$TC qdisc add dev $IF ingress
	$TC filter add dev $IF parent ffff: protocol ip prio 1 u32 match ip dst $IP flowid 1:1 action mirred egress redirect dev ifb0
	$TC qdisc add dev ifb0 root netem loss $LOSS% 

}

stop() {

	# Stop the bandwidth shaping.
	$TC qdisc del dev $IF root
	$TC qdisc del dev $IF ingress 
	$TC qdisc del dev ifb0 root
	ip link set dev ifb0 down
}

init() {
	modprobe ifb
}

restart() {

# Self-explanatory.
    stop
    sleep 1
    start

}

show() {
    # Display status of traffic control status.
    $TC -s qdisc ls dev $IF
    $TC -s qdisc ls dev ifb0
}

case "$1" in

  start)

    echo -n "Starting bandwidth shaping: "
    start $2
    echo "done"
    ;;

  stop)

    echo -n "Stopping bandwidth shaping: "
    stop
    echo "done"
    ;;

  restart)

    echo -n "Restarting bandwidth shaping: "
    restart
    echo "done"
    ;;

  show)

    echo "Bandwidth shaping status for $IP $IF:"
    show
    echo ""
    ;;

  init)

    echo "Initializing ifb kernel module"
    init
    echo "done"
    ;;

  *)

    pwd=$(pwd)
    echo "Usage: tc.bash {start|stop|restart|show}"
    ;;

esac

exit 0
