#!/usr/bin/python

import subprocess,os,time,re,argparse

def main(node, cmd, log_prefix):
    src_processes = []
    #cmd = ["./run-iperf-src.sh"]
    (node_cxns, all_cxns) = get_node_cxns(node)
    write_node_cxns(node_cxns, all_cxns)
    for cxn in node_cxns :
        log_index = "-".join(cxn)
        logfile = "%s_%s.log"%(log_prefix, log_index.strip())
        #cmd_args = [cxn[1], cxn[2]]
        cmd_args = ["n%s"%cxn[1].strip()]
        with open(logfile, 'w') as log:
            p = subprocess.Popen([cmd] + cmd_args, stdout=log, cwd=".", stderr=subprocess.STDOUT, env=os.environ.copy())
            src_processes.append(p)

    # Now wait for sources to terminate	
    wait_for_srcs(src_processes)

def get_node_cxns(node):
    """ Format of connections file line is src_host, dest_host"""
    node_cxns = [] 
    all_cxns = []
    with open("../iperf_connections.txt", 'r') as cxns:
        for line in cxns:
            cxn = line.strip().split(',')	
            all_cxns.append(cxn)
            if node == "n%s"%cxn[0].strip():
                node_cxns.append(cxn) 
        
    return (node_cxns, all_cxns)	

def write_node_cxns(node_cxns, all_cxns):
    with open("node_cxns.txt", 'w') as cxns:
        cxns.write("Node cxns=")
        cxns.write(str(node_cxns)+"\n")
        cxns.write("All cxns=")
        cxns.write(str(all_cxns)+"\n")

def wait_for_srcs(src_processes):
    for p in src_processes: 
        p.wait()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Run one or more iperf srcs from this node')
    parser.add_argument('--node', dest='node', help='node id')
    parser.add_argument('--cmd', dest='cmd', help='Command to run')
    parser.add_argument('--logprefix', dest='log_prefix', help='Prefix of logfile')
    args=parser.parse_args()
    main(args.node, args.cmd, args.log_prefix)



