#!/usr/bin/python

import subprocess,os,time,re,argparse

def main(node):
    sink_processes = []
    cmd = ["./run-iperf-sink.sh"]
    (node_cxns, all_cxns) = get_sink_node_cxns(node)
    started_udp_server = False
    started_tcp_server = False
    for cxn in node_cxns :
        udp = False
        log_index = "-".join(cxn)
        logfile = "iperf_sink_%s.log"%(log_index.strip())
        #cmd_args = [cxn[1], cxn[2]]
        if len(cxn) == 3:
            # udp send rate
            #cmd_args = ["n%s %s"%(cxn[1].strip(), cxn[2].strip())]
            cmd_args = ["udp"]
            udp = True
        else:
            cmd_args = ["tcp"]
        print "Iperf sink cmd args: "+str(cmd_args)
        if (udp and (not started_udp_server)) or ((not udp) and (not started_tcp_server)):
            with open(logfile, 'w') as log:
                p = subprocess.Popen(cmd + cmd_args, stdout=log, cwd=".", stderr=subprocess.STDOUT, env=os.environ.copy())
                sink_processes.append(p)

    # Now wait for sources to terminate	
    wait_for_sinks(sink_processes)

def get_sink_node_cxns(node):
    """ Format of connections file line is src_host, dest_host"""
    node_cxns = [] 
    all_cxns = []
    with open("../iperf_connections.txt", 'r') as cxns:
        for line in cxns:
            cxn = line.strip().split(',')	
            all_cxns.append(cxn)
            if node == "n%s"%cxn[1].strip():
                node_cxns.append(cxn) 
        
    return (node_cxns, all_cxns)	

def wait_for_sinks(sink_processes):
    for p in sink_processes: 
        p.wait()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Run one or more iperf sinks from this node')
    parser.add_argument('--node', dest='node', help='node id')
    args=parser.parse_args()
    main(args.node)



