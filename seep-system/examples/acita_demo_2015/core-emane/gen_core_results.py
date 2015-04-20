#!/usr/bin/python

import subprocess,os,time,re,argparse,glob, pandas

from extract_results import *


def main(exp_dir):

    sim_env = os.environ.copy()
    print "Analysing logs in %s"%(exp_dir)       

    # Get src logfilename
    src_log = get_src_logfile(exp_dir) 

    # Get sink logfilename
    sink_log = get_sink_logfile(exp_dir)

    # Get time src started sending
    with open(src_log, 'r') as src:
        # Get time src sent first message
        t_src_begin = src_tx_begin(src)
        #if not t_src_begin: raise Exception("Could not find t_src_begin.")
        if not t_src_begin: "WARNING: Could not find t_src_begin."

    with open(sink_log, 'r') as sink:
        # Get time sink received first message
        t_sink_begin = sink_rx_begin(sink)
        if not t_sink_begin: raise Exception("Could not find t_sink_begin.")
    with open(sink_log, 'r') as sink:
        (tuples, total_bytes, t_sink_end) = sink_rx_end(sink)
        if not t_sink_end: raise Exception("Could not find t_sink_end.")
    with open(sink_log, 'r') as sink:
        rx_latencies = sink_rx_latencies(sink)
        #if not rx_latencies: raise Exception("Could not find any latencies.")

    # Record the tput, k, w, query name etc.
    # Compute the mean tput
    if t_src_begin: 
        src_sink_mean_tput = mean_tput(t_src_begin, t_sink_end, total_bytes)
        record_stat('%s/tput.txt'%exp_dir, {'src_sink_mean_tput':src_sink_mean_tput})
        src_sink_frame_rate = frame_rate(t_src_begin, t_sink_end, tuples) 
        record_stat('%s/tput.txt'%exp_dir, {'src_sink_frame_rate':src_sink_frame_rate}, 'a')

    sink_sink_mean_tput = mean_tput(t_sink_begin, t_sink_end, total_bytes)
    record_stat('%s/tput.txt'%exp_dir, {'sink_sink_mean_tput':sink_sink_mean_tput}, 'a')
    sink_sink_frame_rate = frame_rate(t_sink_begin, t_sink_end, tuples) 
    record_stat('%s/tput.txt'%exp_dir, {'sink_sink_frame_rate':sink_sink_frame_rate}, 'a')

    lstats = latency_stats(rx_latencies)
    record_stat('%s/latency.txt'%exp_dir, lstats)

def get_src_logfile(exp_dir):
    return get_logfile(exp_dir, is_src_log)

def get_sink_logfile(exp_dir):
    return get_logfile(exp_dir, is_sink_log)

def get_logfile(exp_dir, type_fn):
    files = glob.glob("%s/*worker*.log"%exp_dir)
    for filename in files:
        with open(filename, 'r') as f:
            if type_fn(f): return filename 
    return None

def mean_tput(t_start, t_end, bites):
    duration_s = float(t_end - t_start) / 1000.0
    kb = (8.0 * float(bites)) / 1024.0 
    return kb / duration_s 

def frame_rate(t_start, t_end, tuples):
    duration_s = float(t_end - t_start) / 1000.0
    return float(tuples) / duration_s 

def latency_stats(latencies):
    result = {}
    for k,v in dict(latencies.describe()).iteritems():
        result['%s_lat'%k] = v
    return result

def record_stat(stat_file, stats, mode='w'):
    with open(stat_file, mode) as sf:
        for stat in stats:
            line = '%s=%s\n'%(str(stat), str(stats[stat]))
            print line
            sf.write(line)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Analyse emulation logs')
    parser.add_argument('--expDir', dest='exp_dir', help='relative path to exp dir')
    args=parser.parse_args()

    main(args.exp_dir)

