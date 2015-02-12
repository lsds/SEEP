#!/usr/bin/python

import subprocess,os,time,re,argparse,glob

from extract_results import *

eg_dir = os.path.dirname(os.path.realpath(__file__))

def main(rel_exp_dir):

    sim_env = os.environ.copy()
    exp_dir = "%s/%s"%(eg_dir, rel_exp_dir) 
    print "Analysing logs in %s"%(exp_dir)       

    # Get src logfilename
    src_log = get_src_logfile(exp_dir) 

    # Get sink logfilename
    sink_log = get_sink_logfile(exp_dir)

    # Get time src started sending
    with open(src_log, 'r') as src:
        lines = src.readlines()
        # Get time src sent first message
        t_src_begin = src_tx_begin(lines)
        if not t_src_begin: raise Exception("Could not find t_src_begin.")

    with open(sink_log, 'r') as sink:
        lines = sink.readlines()
        # Get time sink received first message
        t_sink_begin = sink_rx_begin(lines)
        if not t_sink_begin: raise Exception("Could not find t_sink_begin.")
        (tuples, total_bytes, t_sink_end) = sink_rx_end(lines)
        if not t_sink_end: raise Exception("Could not find t_sink_end.")
        #rx_latencies = sink_rx_latencies(lines)


    # Compute the mean tput
    src_sink_mean_tput = mean_tput(t_src_begin, t_sink_end, total_bytes)
    sink_sink_mean_tput = mean_tput(t_sink_begin, t_sink_end, total_bytes)

    # Record the tput, k, w, query name etc.
    record_stat('%s/tput.txt'%exp_dir, {'src_sink_mean_tput':src_sink_mean_tput})
    record_stat('%s/tput.txt'%exp_dir, {'sink_sink_mean_tput':sink_sink_mean_tput}, 'a')

def get_src_logfile(exp_dir):
    return get_logfile_type(exp_dir, is_src_log)

def get_sink_logfile(exp_dir):
    return get_logfile_type(exp_dir, is_sink_log)

def get_logfile_type(exp_dir, fn):
    files = glob.glob("%s/*.log"%exp_dir)
    for filename in files:
        with open(filename, 'r') as f:
            lines = f.readlines()
            if fn(lines): return filename 
    return None

def mean_tput(t_start, t_end, bites):
    duration_s = float(t_end - t_start) / 1000.0
    kb = (8.0 * float(bites)) / 1024.0 
    return kb / duration_s 

def record_stat(stat_file, stats, mode='w'):
    with open(stat_file, mode) as sf:
        for stat in stats:
            sf.write('%s=%s\n'%(str(stat), str(stats[stat])))

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Analyse emulation logs')
    parser.add_argument('--expDir', dest='exp_dir', help='relative path to exp dir')
    args=parser.parse_args()

    main(args.exp_dir)

