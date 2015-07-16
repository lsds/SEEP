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

    sink_logs = get_sink_logfiles(exp_dir)

    op_logs = get_processor_logfiles(exp_dir)

    # Get time src started sending
    with open(src_log, 'r') as src:
        # Get time src sent first message
        t_src_begin = src_tx_begin(src)
        #if not t_src_begin: raise Exception("Could not find t_src_begin.")
        if not t_src_begin: "WARNING: Could not find t_src_begin."

    if len(sink_logs) == 1:
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
    else:
       
        min_t_sink_begin = -1
        max_t_sink_end = 0
        total_bytes = 0
        total_tuples = 0
        rx_latencies = []
        for (i, sink_log) in enumerate(sink_logs):
            with open(sink_log, 'r') as sink:
                # Get time sink received first message
                t_sink_begin = sink_rx_begin(sink)
                if not t_sink_begin: raise Exception("Could not find t_sink_begin.")
                if min_t_sink_begin < 0 or min_t_sink_begin > t_sink_begin:
                    min_t_sink_begin = t_sink_begin
            with open(sink_log, 'r') as sink:
                #TODO: This will fail for sinks that didn't finish.
                (tuples, sink_total_bytes, t_sink_end) = sink_rx_end(sink)
                if not t_sink_end: raise Exception("Could not find t_sink_end.")
                total_bytes += sink_total_bytes
                total_tuples += tuples
                max_t_sink_end = max(max_t_sink_end, t_sink_end)
            with open(sink_log, 'r') as sink:
                rx_latencies += sink_rx_latencies(sink)
                #if not rx_latencies: raise Exception("Could not find any latencies.")

    op_tputs = {}
    for op_log in op_logs:
        with open(op_log, 'r') as f:
            (op_id, tput) = processor_tput(f)
            if op_id:
                op_tputs[op_id] = tput 

    # Record the tput, k, w, query name etc.
    # Compute the mean tput
    if t_src_begin: 
        src_sink_mean_tput = mean_tput(t_src_begin, t_sink_end, total_bytes)
        record_stat('%s/tput.txt'%exp_dir, {'src_sink_mean_tput':src_sink_mean_tput})
        src_sink_frame_rate = frame_rate(t_src_begin, t_sink_end, tuples) 
        record_stat('%s/tput.txt'%exp_dir, {'src_sink_frame_rate':src_sink_frame_rate}, 'a')


    if len(sink_logs) == 1:
        record_sink_sink_stats(t_sink_begin, t_sink_end, total_bytes, tuples, rx_latencies, exp_dir)
    else:
        record_sink_sink_stats(min_t_sink_begin, max_t_sink_end, total_bytes, total_tuples, rx_latencies, exp_dir)
    """
    sink_sink_mean_tput = mean_tput(t_sink_begin, t_sink_end, total_bytes)
    record_stat('%s/tput.txt'%exp_dir, {'sink_sink_mean_tput':sink_sink_mean_tput}, 'a')
    sink_sink_frame_rate = frame_rate(t_sink_begin, t_sink_end, tuples) 
    record_stat('%s/tput.txt'%exp_dir, {'sink_sink_frame_rate':sink_sink_frame_rate}, 'a')

    lstats = latency_stats(rx_latencies)
    record_stat('%s/latency.txt'%exp_dir, lstats)
    """

    record_stat('%s/op-tputs.txt'%exp_dir, op_tputs)

def record_sink_sink_stats(t_sink_begin, t_sink_end, total_bytes, tuples, rx_latencies, exp_dir)
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

def get_sink_logfiles(exp_dir):
    files = glob.glob("%s/*worker*.log"%exp_dir)
    filenames = []
    for filename in files:
        with open(filename, 'r') as f:
            if is_sink_log(f): filenames.append(filename) 
    return filenames 

def get_processor_logfiles(exp_dir):
    files = glob.glob("%s/*worker*.log"%exp_dir)
    filenames = []
    for filename in files:
        with open(filename, 'r') as f:
            if is_processor_log(f): filenames.append(filename) 
    return filenames 

def mean_tput(t_start, t_end, bites):
    duration_s = float(t_end - t_start) / 1000.0
    kb = (8.0 * float(bites)) / 1024.0 
    return kb / duration_s 

def frame_rate(t_start, t_end, tuples):
    duration_s = float(t_end - t_start) / 1000.0
    return float(tuples) / duration_s 

def latency_stats(latencies):
    result = {}
    percentiles=[.25,.5,.75,.9,.95,.99]
    for k,v in dict(latencies.describe(percentiles=percentiles)).iteritems():
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

