#!/usr/bin/python

import subprocess,os,time,re,argparse

def main(node):
    src_processes = []
    cmd = ["./run-iperf-src.sh"]
    (node_cxns, all_cxns) = get_node_cxns(node)
    write_node_cxns(node_cxns, all_cxns)
    for cxn in node_cxns :
        log_index = "-".join(cxn)
        logfile = "iperf_src_%s.log"%(log_index.strip())
        #cmd_args = [cxn[1], cxn[2]]
        if cxn[1] == "all":
            #Noise source
            cmd_args = ["all"]
        elif len(cxn) == 3:
            # udp send rate
            #cmd_args = ["n%s %s"%(cxn[1].strip(), cxn[2].strip())]
            cmd_args = ["n%s"%cxn[1].strip(), cxn[2].strip()]
        else:
            cmd_args = ["n%s"%cxn[1].strip()]
        print "Iperf src cmd args: "+str(cmd_args)
        with open(logfile, 'w') as log:
            p = subprocess.Popen(cmd + cmd_args, stdout=log, cwd=".", stderr=subprocess.STDOUT, env=os.environ.copy())
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
    args=parser.parse_args()
    main(args.node)



