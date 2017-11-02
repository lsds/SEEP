#!/usr/bin/python -u

import subprocess,os,time,re,argparse,random

def main(node):
    offset, cycle_spec = get_node_cycle_spec(node)
    if cycle_spec: 
        print "Found failure cycle spec for %s"%(node)
        wait_for_offset(node, offset)
        do_cycles(node, cycle_spec)
        return

    offset, slot_length, p_fail = get_node_fail_prob_spec(node)
    if p_fail and p_fail > 0.0:
        print "Found probabilistic failure spec for %s"%(node)
        wait_for_offset(node,offset)
        inject_probabilistic_failures(node, slot_length, p_fail)
        return

    print "No failure cycles defined for node %s, terminating."%(node)

def wait_for_offset(node, offset):
    now = time.time()
    exp_start = get_exp_start()
    if exp_start + offset > now: 
        print "Node %s waiting for %.1f seconds to start failure injector"%(node, exp_start + offset - now)
        time.sleep(exp_start + offset - now)
   
def do_cycles(node, cycle_spec):
    print "Node %s starting failure injector at %.1f (%s)"%(node, time.time(), time.strftime("%H:%M:%S"))
    while True:
        for spec in cycle_spec:
            direction = "up" if spec >= 0 else "down"
            toggle_netif(direction)
            print "Node %s net inf %s at %.1f (%s)"%(node, direction, time.time(), time.strftime("%H:%M:%S"))
            time.sleep(abs(spec))

def toggle_netif(direction):
    cmd = ["ip", "link", "set", "dev", "eth0", direction]
    with open("failure-injector.log", 'a') as log:
        subprocess.Popen(cmd, stdout=log, cwd=".", stderr=subprocess.STDOUT, env=os.environ.copy())

    cmd = ["ip", "link", "set", "dev", "ctrl0", direction]
    with open("failure-injector.log", 'a') as log:
        subprocess.Popen(cmd, stdout=log, cwd=".", stderr=subprocess.STDOUT, env=os.environ.copy())

def get_node_cycle_spec(node):
    """ Format of cycle spec file line is node,offset_sec,up_duration_sec,down_duration_sec"""
    if os.path.exists("../failure_cycles.txt"):
        with open("../failure_cycles.txt", 'r') as f:
            for line in f:
                cycle_spec = line.strip().split(',')	
                if node == cycle_spec[0].strip():
                    return float(cycle_spec[1]), map(float, cycle_spec[2:])
    return None,None

def get_exp_start():
    with open("../start_failures.txt", 'r') as f:
        for line in f:
            return float(line.strip())

def get_node_fail_prob_spec(node):
    """ Format of fail spec file line is node,offset_sec,slot_length_sec_float,p_fail_float"""
    if os.path.exists("../failure_probs.txt"):
        with open("../failure_probs.txt", 'r') as f:
            for line in f:
                fail_spec = line.strip().split(',')	
                spec_node = fail_spec[0].strip() 
                if node == spec_node or spec_node == '*':
                    return float(fail_spec[1]),float(fail_spec[2]), float(fail_spec[3])
    return None,None,None

def inject_probabilistic_failures(node, slot_length, p_fail):
    while True:
        rand = random.random()
        if rand < p_fail:
            print "Probabilistic failure triggered for %s at %s"%(node,time.strftime("%H:%M:%S"))
            toggle_netif("down")

        #Sleep for slot_length seconds
        time.sleep(slot_length)

        if rand < p_fail:
            print "Node %s recovering from probabilistic failure at %s"%(node,time.strftime("%H:%M:%S"))
            toggle_netif("up")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Run a failure injector on this node.')
    parser.add_argument('--node', dest='node', help='node id')
    args=parser.parse_args()
    main(args.node)
