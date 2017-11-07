#!/usr/bin/python

import subprocess,os,time,re,argparse,glob, pandas

from extract_results import *
from compute_stats import compute_cumulative_percentiles
from energy import get_network_power_usage, get_cpu_power_usage, get_ops_power_usage

def gen_core_results(exp_dir, gen_sub_results):

    sim_env = os.environ.copy()
    print "Analysing logs in %s"%(exp_dir)       
    
    # Get src logfilename
    src_log = get_src_logfile(exp_dir) 
    #src_log = get_src_logfiles(exp_dir)[0]

    # Get sink logfilename
    #sink_log = get_sink_logfile(exp_dir)

    finished_sink_log = get_finished_sink_logfile(exp_dir)
    print 'Found finished sink log: ', finished_sink_log

    finished = bool(finished_sink_log)

    sink_logs = get_sink_logfiles(exp_dir)
    print 'Found sink logs: ', sink_logs

    op_logs = get_processor_logfiles(exp_dir)
    print 'Found op logs: ', op_logs
    t_sub_begin = False

    if finished: 
        # Get time src started sending
        with open(src_log, 'r') as src:
            # Get time src sent first message
            t_src_begin = src_tx_begin(src)
            #if not t_src_begin: raise Exception("Could not find t_src_begin.")
            if not t_src_begin: "WARNING: Could not find t_src_begin."

        if gen_sub_results:
            with open(src_log, 'r') as src:
                t_sub_begin = sub_tx_begin(src)
                #if not t_src_begin: raise Exception("Could not find t_src_begin.")
                if not t_sub_begin: "WARNING: Could not find t_sub_begin."

        with open(finished_sink_log, 'r') as sink:
            # Get time sink received first message
            t_finished_sink_begin = sink_rx_begin(sink)
            if not t_finished_sink_begin: raise Exception("Could not find t_sink_begin.")
        with open(finished_sink_log, 'r') as sink:
            (total_tuples, total_bytes, t_finished_sink_end) = sink_rx_end(sink)
            finished_total_tuples = total_tuples
            finished_total_bytes = total_bytes #N.B.This assumes all tuples are the same size.
            avg_bytes_per_tuple = total_bytes / total_tuples

            if not t_finished_sink_end: raise Exception("Could not find t_sink_end.")
        with open(finished_sink_log, 'r') as sink:
            rx_latencies = sink_rx_latencies(sink)
            #if not rx_latencies: raise Exception("Could not find any latencies.")
        with open(finished_sink_log, 'r') as sink:
            tuple_ids = sink_rx_tuple_ids(sink)

        #If there are replicated sinks, need to treat the sinks that didn't finish
        #first differently.
        t_min_sink_begin = t_finished_sink_begin 
        for sink_log in filter(lambda log: log != finished_sink_log, sink_logs):
            print 'Processing potentially unfinished log: ', sink_log
            if t_sub_begin: raise Exception("TODO: sub tput + replicated sinks.")
            with open(sink_log, 'r') as sink:
                (tuples, bytez) = unfinished_sink_tuples(sink, t_finished_sink_end, tuple_ids)

                #TODO: This will fail for sinks that didn't finish.
                #(tuples, sink_total_bytes, t_sink_end) = sink_rx_end(sink)
                #if not t_sink_end: raise Exception("Could not find t_sink_end.")
                if tuples == 0: continue
                total_tuples += tuples
                total_bytes += bytez 
                #max_t_sink_end = max(max_t_sink_end, t_sink_end)
            with open(sink_log, 'r') as sink:
                # Get time sink received first message
                t_sink_begin = sink_rx_begin(sink)
                if not t_sink_begin: raise Exception("Could not find t_sink_begin.")
                #if not t_sink_begin: t_min_sink_begin 

                if t_sink_begin and t_sink_begin < t_min_sink_begin:
                    t_min_sink_begin = t_sink_begin
            with open(sink_log, 'r') as sink:
                rx_latencies += unfinished_sink_rx_latencies(sink, t_finished_sink_end)
                #if not rx_latencies: raise Exception("Could not find any latencies.")

        deduped_tx_latencies = dedup_latencies(rx_latencies)
        total_dupes = len(rx_latencies) - len(deduped_tx_latencies)
        print 'Found %d duplicates.'%total_dupes
        record_stat('%s/dupes.txt'%exp_dir, {'total_dupes':total_dupes})

    op_tputs = {}
    op_interval_tputs = {}
    op_utils = {}
    op_weight_infos= {}
    link_interval_tputs = {}
    for op_log in op_logs + sink_logs:
        with open(op_log, 'r') as f:
            (op_id, tput) = processor_tput(f)
            if op_id:
                op_tputs[op_id] = tput 

        with open(op_log, 'r') as f:
            (op_id, interval_tputs) = get_interval_tputs(f)
            if op_id:
                op_interval_tputs[op_id] = interval_tputs

        with open(op_log, 'r') as f:
            (op_id, utils) = get_utils(f)
            if op_id:
                op_utils[op_id] = utils 

        with open(op_log, 'r') as f:
            (op_id, weight_infos) = get_weight_infos(f)
            if op_id:
                op_weight_infos[op_id] = weight_infos 

        with open(op_log, 'r') as f:
            (op_id, interval_tputs) = get_link_interval_tputs(f)
            if op_id:
                for up_id in interval_tputs:
                    link_interval_tputs[(op_id, up_id)] = interval_tputs[up_id]
    
    op_transmissions = {}
    for op_log in op_logs + get_src_logfiles(exp_dir):
        with open(op_log, 'r') as f:
            (op_id, transmissions) = get_transmissions(f)
            if op_id:
                op_transmissions[op_id] = transmissions

    if t_sub_begin:
        total_sub_tuples = len(sub_rx_latencies(t_sub_begin, deduped_tx_latencies))
        print 'Total sub tuples=%d (from %d)'%(total_sub_tuples, len(deduped_tx_latencies))

    if finished:
        # Record the tput, k, w, query name etc.
        # Compute the mean tput
        if t_src_begin: 
            src_sink_mean_tput = mean_tput(t_src_begin, t_finished_sink_end, total_bytes)
            record_stat('%s/tput.txt'%exp_dir, {'src_sink_mean_tput':src_sink_mean_tput})
            src_sink_frame_rate = frame_rate(t_src_begin, t_finished_sink_end, total_tuples) 
            record_stat('%s/tput.txt'%exp_dir, {'src_sink_frame_rate':src_sink_frame_rate}, 'a')
        
        if t_sub_begin:
            t_sub_end = t_finished_sink_end # For now, assume we want to ignore tuples at the start
            total_sub_bytes = avg_bytes_per_tuple * total_sub_tuples
            sub_mean_tput = mean_tput(t_sub_begin, t_sub_end, total_sub_bytes)
            record_stat('%s/tput.txt'%exp_dir, {'sub_mean_tput':sub_mean_tput})

        record_sink_sink_stats(t_min_sink_begin, t_finished_sink_end, total_bytes, total_tuples, deduped_tx_latencies, exp_dir)
        "N.B. finished_total_bytes will be wrong here if tuples are different sizes."
        deduped_joint_latencies = sorted(list(deduped_tx_latencies.values()), key=lambda x: x[2])[:finished_total_tuples]
        t_min_finished_sink_end = deduped_joint_latencies[-1][2]
        record_sink_sink_joint_finished_stats(t_min_sink_begin, deduped_joint_latencies[-1][2], finished_total_bytes, finished_total_tuples, deduped_joint_latencies, exp_dir)

        node_net_rates = get_node_net_rates(exp_dir)
        network_power_usage = get_network_power_usage(node_net_rates, total_tuples, t_min_sink_begin, t_min_finished_sink_end)
        record_network_power_usage(network_power_usage, exp_dir)

        node_cpu_rates = get_node_cpu_rates(exp_dir)
        cpu_power_usage = get_cpu_power_usage(node_cpu_rates, total_tuples, t_min_sink_begin, t_min_finished_sink_end)
        record_cpu_power_usage(cpu_power_usage, exp_dir)
    
        # Alternatively, estimate CPU power usage based only on operator utilization
        ops_power_usage = get_ops_power_usage(op_utils, total_tuples, t_min_sink_begin, t_min_finished_sink_end) 
        record_ops_power_usage(ops_power_usage, exp_dir)

        total_power = network_power_usage['total'] + ops_power_usage['total_excl_base']
        total_power_excl_base = network_power_usage['total_excl_base'] + ops_power_usage['total_excl_base']
        record_stat('%s/power.txt'%exp_dir, {'total_power':total_power, 'total_power_excl_base':total_power_excl_base})

        """
        sink_sink_mean_tput = mean_tput(t_sink_begin, t_sink_end, total_bytes)
        record_stat('%s/tput.txt'%exp_dir, {'sink_sink_mean_tput':sink_sink_mean_tput}, 'a')
        sink_sink_frame_rate = frame_rate(t_sink_begin, t_sink_end, tuples) 
        record_stat('%s/tput.txt'%exp_dir, {'sink_sink_frame_rate':sink_sink_frame_rate}, 'a')

        lstats = latency_stats(rx_latencies)
        record_stat('%s/latency.txt'%exp_dir, lstats)
        """

    record_stat('%s/op-tputs.txt'%exp_dir, op_tputs)
    record_op_interval_tputs(op_interval_tputs, exp_dir)
    record_op_weight_infos(op_weight_infos, exp_dir)
    record_op_utils(op_utils, exp_dir)
    record_link_interval_tputs(link_interval_tputs, exp_dir)
    record_op_transmissions(op_transmissions, exp_dir)

    error_check(exp_dir)

def record_sink_sink_stats(t_sink_begin, t_sink_end, total_bytes, tuples, deduped_tx_latencies, exp_dir):
    sink_sink_mean_tput = mean_tput(t_sink_begin, t_sink_end, total_bytes)
    record_stat('%s/tput.txt'%exp_dir, {'sink_sink_mean_tput':sink_sink_mean_tput}, 'a')
    sink_sink_frame_rate = frame_rate(t_sink_begin, t_sink_end, tuples) 
    record_stat('%s/tput.txt'%exp_dir, {'sink_sink_frame_rate':sink_sink_frame_rate}, 'a')
    deduped_latencies = map(lambda (latency, txts, rxts): latency, deduped_tx_latencies.values())
    lstats = latency_stats(pd.Series(deduped_latencies))
    record_stat('%s/latency.txt'%exp_dir, lstats)
    lpercentiles = compute_cumulative_percentiles(deduped_latencies)
    record_percentiles(lpercentiles, 'lat', exp_dir)
    record_latencies(deduped_tx_latencies, '%s/tx_latencies.txt'%exp_dir)

def record_sink_sink_joint_finished_stats(t_sink_begin, t_sink_end, total_bytes, tuples, deduped_joint_latencies, exp_dir):
    #sink_sink_mean_tput = mean_tput(t_sink_begin, t_sink_end, total_bytes)
    sink_sink_mean_tput = mean_tput(t_sink_begin, t_sink_end, total_bytes)
    record_stat('%s/tput.txt'%exp_dir, {'sink_sink_joint_mean_tput':sink_sink_mean_tput}, 'a')
    sink_sink_frame_rate = frame_rate(t_sink_begin, t_sink_end, tuples) 
    record_stat('%s/tput.txt'%exp_dir, {'sink_sink_joint_frame_rate':sink_sink_frame_rate}, 'a')
    deduped_latencies = map(lambda x: x[0], deduped_joint_latencies)
    #lstats = latency_stats(pd.Series(deduped_latencies))
    #record_stat('%s/latency.txt'%exp_dir, lstats)
    #lpercentiles = compute_cumulative_percentiles(deduped_latencies)
    #record_percentiles(lpercentiles, 'lat', exp_dir)

def get_src_logfile(exp_dir):
    return get_logfile(exp_dir, is_src_log)

def get_src_logfiles(exp_dir):
    return get_logfiles(exp_dir, is_src_log)

def get_sink_logfile(exp_dir):
    return get_logfile(exp_dir, is_sink_log)

def get_finished_sink_logfile(exp_dir):
    return get_logfile(exp_dir, is_finished_sink_log)

def get_logfile(exp_dir, type_fn):
    files = glob.glob("%s/*worker*.log"%exp_dir)
    for filename in files:
        with open(filename, 'r') as f:
            if type_fn(f): return filename 
    files = glob.glob("%s/*/*worker*.log"%exp_dir)
    for filename in files:
        with open(filename, 'r') as f:
            if type_fn(f): return filename 
    return None

def get_logfiles(exp_dir, type_fn):
    files = glob.glob("%s/*worker*.log"%exp_dir)
    filenames = []
    for filename in files:
        with open(filename, 'r') as f:
            if type_fn(f): filenames.append(filename)
    return filenames 

def get_sink_logfiles(exp_dir):
    files = glob.glob("%s/*worker*.log"%exp_dir)
    filenames = []
    for filename in files:
        with open(filename, 'r') as f:
            if is_sink_log(f): filenames.append(filename) 
    return filenames 

def get_processor_logfiles(exp_dir):
    files = glob.glob("%s/*worker*.log"%exp_dir)
    filesLevel1 = glob.glob("%s/*/*worker*.log"%exp_dir)
    filenames = []
    for filename in files+filesLevel1:
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

def record_percentiles(percentiles, metric_suffix, exp_dir):
    with open('%s/cum-%s.data'%(exp_dir,metric_suffix),'w') as cum_fixed_kmobsession_plotdata:
        cum_fixed_kmobsession_plotdata.write('#p %s\n'%metric_suffix)
        for (p, value) in percentiles: 
            logline = '%.2f %d\n'%(p,value)
            cum_fixed_kmobsession_plotdata.write(logline)

def record_latencies(latencies, latencies_file):
    with open(latencies_file,'w') as lf:
        lf.write('# latency')
        tx_sorted = [] 
        for ts in latencies: 
            tx_sorted.append((latencies[ts][1], latencies[ts][0]))
        for (tx, latency) in sorted(tx_sorted): 
            lf.write('%d %d\n'%(tx/1000, latency))

def record_op_interval_tputs(op_interval_tputs, exp_dir):
    for op in op_interval_tputs:
        op_interval_tput_file = "%s/op_%s_interval_tput.txt"%(exp_dir, op)
        with open(op_interval_tput_file, 'w') as f:
            f.write('# tput cum\n')
            for (ts, tput, cum) in op_interval_tputs[op]:
                f.write('%d %.1f %.1f\n'%(ts/1000, tput, cum))

def record_link_interval_tputs(link_interval_tputs, exp_dir):
    links_tput_dir = "%s/link-tputs"%(exp_dir)
    if not os.path.exists(links_tput_dir): os.mkdir(links_tput_dir)
    for (op, up) in link_interval_tputs:
        link_interval_tput_file = "%s/link-tputs/op_%s_up_%s_link_interval_tput.txt"%(exp_dir, op, up)
        with open(link_interval_tput_file, 'w') as f:
            f.write('# tput cum cost\n')
            for (ts, tput, cum, cost) in link_interval_tputs[(op, up)]:
                f.write('%d %.1f %.1f %.1f\n'%(ts/1000, tput, cum, cost))

def record_op_weight_infos(op_weight_infos, exp_dir):
    for op in op_weight_infos:
        op_weight_infos_file = "%s/op_%s_weight_infos.txt"%(exp_dir, op)
        with open(op_weight_infos_file, 'w') as f:
            f.write('#t ltqlen iq oq ready w skew pending(0-i) w(0-i)\n')
            for (ts, ltqlen, iq, oq, ready, pending, skew, w, wi, wdqru) in op_weight_infos[op]:
                line = '%d %d %d %d %d %.1f %d'%(ts/1000, ltqlen, iq, oq, ready, w, skew)
                if pending:
                    line += " " + " ".join(map(str, pending))
                if wi:
                    line += " " + " ".join(map(str, wi))
                if bool(pending) != bool(wi): raise Exception("Logic error: op %s, p=%s, wi=%s"%(op,str(pending),str(wi))) 
                line += "\n"
                f.write(line)

def record_op_utils(op_utils, exp_dir):
    for op in op_utils:
        op_utils_file = "%s/op_%s_utils.txt"%(exp_dir, op)
        with open(op_utils_file, 'w') as f:
            f.write('#ts_s util cum interval_ms\n')
            for (ts, util, cum, interval) in op_utils[op]:
                f.write('%d %.1f %.1f %d\n'%(ts/1000, util, cum, interval))

def record_op_transmissions(op_transmissions, exp_dir):
    ts_total_transmissions = {}
    op_total_transmissions = {}
    for op in op_transmissions:
        op_transmissions_file = "%s/op_%s_transmissions.txt"%(exp_dir, op)
        op_total_transmissions[op] = len(op_transmissions[op])
        with open(op_transmissions_file, 'w') as f:
            f.write('# t ts dsop\n')
            for (t, ts, ds_op) in op_transmissions[op]:
                f.write('%d %d %d\n'%(t, ts, ds_op))
                ts_total_transmissions[ts] = ts_total_transmissions.get(ts, 0) + 1

    ts_total_transmissions_file = "%s/ts_total_transmissions.txt"%(exp_dir)
    with open(ts_total_transmissions_file, 'w') as f:
        f.write('#ts count\n') 
        f.write('#total %d\n'%(sum(ts_total_transmissions.values())))
        for ts in sorted(ts_total_transmissions.keys()):
            f.write('%d %d\n'%(ts, ts_total_transmissions[ts]))

    op_total_transmissions_file = "%s/op_total_transmissions.txt"%(exp_dir)
    with open(op_total_transmissions_file, 'w') as f:
        f.write('#op total\n')
        for op in op_total_transmissions:
            f.write('%s %d\n'%(op, op_total_transmissions[op]))

def record_network_power_usage(network_power_usage, exp_dir):
    with open(exp_dir+'/network_energy_usage.txt', 'w') as f:
        for node in network_power_usage:
            f.write('%s/%.3f\n'%(node, network_power_usage[node]))

def record_cpu_power_usage(cpu_power_usage, exp_dir):
    with open(exp_dir+'/cpu_energy_usage.txt', 'w') as f:
        for node in cpu_power_usage:
            f.write('%s/%.3f\n'%(node, cpu_power_usage[node]))

def record_ops_power_usage(ops_power_usage, exp_dir):
    with open(exp_dir+'/ops_energy_usage.txt', 'w') as f:
        for node in ops_power_usage:
            f.write('%s/%.3f\n'%(node, ops_power_usage[node]))

def error_check(exp_dir):
    logs = get_logfiles(exp_dir, lambda f: True)
    all_errors = {}
    for log in logs:
        with open(log, 'r') as f:
            errors = get_errors(log)
            if errors: all_errors[log] = errors
    
    if all_errors: print all_errors

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Analyse emulation logs')
    parser.add_argument('--expDir', dest='exp_dir', help='relative path to exp dir')
    parser.add_argument('--sub', dest='gen_sub_results', default=False, action='store_true', help='Gen for subset of tuples (ft only)')
    args=parser.parse_args()

    gen_core_results(args.exp_dir, args.gen_sub_results)

