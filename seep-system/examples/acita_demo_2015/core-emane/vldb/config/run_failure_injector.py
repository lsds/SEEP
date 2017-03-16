#!/usr/bin/python -u

import subprocess,os,time,re,argparse

def main(node):
    cycle_spec = get_node_cycle_spec(node)
    if cycle_spec:
        now = time.time()
        exp_start = get_exp_start()
        if exp_start + cycle_spec[0] > now: 
            print "Node %s waiting for %.1f seconds to start failure injector"%(node, exp_start + cycle_spec[0] - now)
            time.sleep(exp_start + cycle_spec[0] - now)

        print "Node %s starting failure injector at %.1f (%s)"%(node, time.time(), time.strftime("%H:%M:%S"))
        while True:
            time.sleep(cycle_spec[1])
            toggle_netif("down")
            print "Node %s net inf down at %.1f (%s)"%(node, time.time(), time.strftime("%H:%M:%S"))
            time.sleep(cycle_spec[2])
            toggle_netif("up")
            print "Node %s net inf up at %.1f (%s)"%(node, time.time(), time.strftime("%H:%M:%S"))

    print "No failure cycles defined for node %s, terminating."%(node)

def toggle_netif(direction):
    cmd = ["ip", "link", "set", "dev", "eth0", direction]
    with open("failure-injector.log", 'a') as log:
        subprocess.Popen(cmd, stdout=log, cwd=".", stderr=subprocess.STDOUT, env=os.environ.copy())

    cmd = ["ip", "link", "set", "dev", "ctrl0", direction]
    with open("failure-injector.log", 'a') as log:
        subprocess.Popen(cmd, stdout=log, cwd=".", stderr=subprocess.STDOUT, env=os.environ.copy())

def get_node_cycle_spec(node):
    """ Format of cycle spec file line is node,offset_sec,up_duration_sec,down_duration_sec"""
    with open("../failure_cycles.txt", 'r') as f:
        for line in f:
            cycle_spec = line.strip().split(',')	
            if node == cycle_spec[0].strip():
                return (float(cycle_spec[1]), int(cycle_spec[2]), int(cycle_spec[3]))

def get_exp_start():
    with open("../start_failures.txt", 'r') as f:
        for line in f:
            return float(line.strip())

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Run a failure injector on this node.')
    parser.add_argument('--node', dest='node', help='node id')
    args=parser.parse_args()
    main(args.node)
