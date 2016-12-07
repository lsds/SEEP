#!/usr/bin/python

import subprocess,os,time,re,argparse,sys,socket

script_dir = os.path.dirname(os.path.realpath(__file__))
#script_dir = '%s/dev/seep-ita/seep-system/examples/acita_demo_2015/core-emane'%os.environ['HOME']

print 'Appending script_dir to path'
sys.path.append(script_dir)
from compute_stats import compute_stats,median,compute_relative_raw_vals,compute_cumulative_percentiles, compute_percentile_stats
from run_sessions import run_sessions
from util import chmod_dir, pybool_to_javastr, copy_pdfs, copy_results
from notify import notify

ticksPerSecond = 1000.0 * 1000.0 * 1000.0
maxWaitSeconds = 1000000000
latency_percentile = '95%'
latency_regex = re.compile('%s_lat=(\d+)'%(latency_percentile))
latency_percentile = 'max'
latency_regex = re.compile('max_lat=(\d+)')
max_latency = 1000000.0 

def main(ks,variables,sessions,params,plot_time_str=None):

    #script_dir = os.path.dirname(os.path.realpath(__file__))
    time_str = 'unknown'
    try:
        data_dir = '%s/log'%script_dir
        params['daemon_server'] = get_daemon_server()

        session_ids = map(int, sessions.split(",")) if params['specific'] else range(0,int(sessions))
        print 'Experiment session ids=%s'%str(session_ids)
        include_failed = params['includeFailed']

        if plot_time_str:
            time_str = plot_time_str
            with open("%s/%s/plotOnly-cmdline%s.txt"%(data_dir,time_str,'-include-failed' if include_failed else ''), 'w') as f:
                f.write("%s\n"%params['cmdline'])
        else:
            time_str = time.strftime('%H-%M-%S-%a%d%m%y')

            exp_dir = "%s/%s"%(data_dir,time_str)
            if not os.path.isdir(exp_dir): os.mkdir(exp_dir)
            with open("%s/cmdline.txt"%(exp_dir), 'w') as f:
                f.write("%s\n"%params['cmdline'])
            #run_experiment(ks, mobilities, nodes, session_ids, params, time_str, data_dir )
            run_experiment(ks, variables, session_ids, params, time_str, data_dir )

        if not params['iperf']:
            #record_var_statistics(ks, mobilities, nodes, session_ids, time_str, data_dir, 'tput', get_tput)
            #record_var_statistics(ks, mobilities, nodes, session_ids, time_str, data_dir, 'lat', get_latency)
            record_var_statistics(ks, variables, session_ids, time_str, data_dir, 'tput', get_tput_include_failed if include_failed else get_tput)
            record_var_statistics(ks, variables, session_ids, time_str, data_dir, 'lat', get_latency_include_failed if include_failed else get_latency)

            
            if len(variables['mobility']) > 1:
                for p in ['tput_vs_mobility', 'median_tput_vs_mobility', 
                    'latency_vs_mobility', 'tput_vs_mobility_stddev', 
                    'latency_vs_mobility_stddev', 'rel_tput_vs_mobility_stddev',
                    'rel_latency_vs_mobility_stddev', 'tput_vs_netsize_stddev']:
                    plot(p, time_str, script_dir, data_dir)
            elif len(variables['nodes']) > 1:
                for p in ['tput_vs_nodes_stddev', 'latency_vs_nodes_stddev', 
                        'rel_tput_vs_nodes_stddev', 'rel_latency_vs_nodes_stddev']:
                    plot(p, time_str, script_dir, data_dir)
            elif len(variables['dimension']) > 1:
                for p in ['tput_vs_dimension_stddev', 'latency_vs_dimension_stddev', 
                        'rel_tput_vs_dimension_stddev', 'rel_latency_vs_dimension_stddev']:
                    plot(p, time_str, script_dir, data_dir)
            elif len(variables['cpudelay']) > 1:
                for p in ['tput_vs_cpudelay_stddev', 'latency_vs_cpudelay_stddev', 
                        'rel_tput_vs_cpudelay_stddev', 'rel_latency_vs_cpudelay_stddev']:
                    plot(p, time_str, script_dir, data_dir)
            elif len(variables['rctrl_delay']) > 0:
                for p in ['tput_vs_rctrldelay_stddev', 'latency_vs_rctrldelay_stddev', 
                        'rel_tput_vs_rctrldelay_stddev', 'rel_latency_vs_rctrldelay_stddev']:
                    plot(p, time_str, script_dir, data_dir)
            elif len(variables['buf_size']) > 0:
                for p in ['tput_vs_bufsize_stddev', 'latency_vs_bufsize_stddev', 
                        'rel_tput_vs_bufsize_stddev', 'rel_latency_vs_bufsize_stddev']:
                    plot(p, time_str, script_dir, data_dir)
            elif len(variables['retx_timeout']) > 0:
                for p in ['tput_vs_retx_timeout_stddev', 'latency_vs_retx_timeout_stddev', 
                        'rel_tput_vs_retx_timeout_stddev', 'rel_latency_vs_retx_timeout_stddev']:
                    plot(p, time_str, script_dir, data_dir)
            elif len(variables['srcrates']) > 1 or len(variables['rctrl_delay']) > 1:
                record_tput_vs_lat_statistics(ks, time_str, data_dir)
                for p in ['tput_vs_latency_stddev', 'rel_tput_vs_latency_stddev']:
                    plot(p, time_str, script_dir, data_dir)
            else:
                plot('latency_percentiles', time_str, script_dir, data_dir)
                plot('m1_tput_vs_mobility_stddev', time_str, script_dir, data_dir)
                plot('m1_latency_vs_mobility_stddev', time_str, script_dir, data_dir)
                plot('tput_vs_netsize_stddev', time_str, script_dir, data_dir)
                plot('m1_rel_tput_vs_mobility_stddev', time_str, script_dir, data_dir)
                plot('m1_joint_tput_vs_mobility_stddev', time_str, script_dir, data_dir)
                plot('m1_joint_latency_vs_mobility_stddev', time_str, script_dir, data_dir)
                plot_fixed_mob('m1_raw_latency_percentiles', variables['mobility'][0], len(session_ids), time_str, script_dir, data_dir, params)
                #latex_plot('tput_vs_netsize_stddev', time_str, script_dir, data_dir)

        # Do any plots that summarize all sessions for fixed k and mob.

            for k in ks:
                if len(variables['nodes']) <= 1 and len(variables['dimension']) <= 1 and len(variables['cpudelay']) <=1 and len(variables['srcrates']) <=1 and len(variables['rctrl_delay']) <=1:
                    for mob in variables['mobility']:
                        plot_fixed_kmob('cum_lat_fixed_kmob', k, mob, len(session_ids), time_str, script_dir, data_dir, params)
                        plot_fixed_kmob('cum_raw_lat_fixed_kmob', k, mob, len(session_ids), time_str, script_dir, data_dir, params)

                        for session in session_ids:
                            plot_fixed_kmobsession('cum_lat_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                            plot_fixed_kmobsession('tx_lat_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                            plot_fixed_kmobsession('op_tput_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                            plot_fixed_kmobsession('op_cum_tput_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                            plot_fixed_kmobsession('op_qlen_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                            plot_fixed_kmobsession('link_tput_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                            #plot_fixed_kmobsession('link_cost_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                            plot_fixed_kmobsession('op_cum_util_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                            if params['dstat']:
                                plot_fixed_kmobsession('cpu_util_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                                plot_fixed_kmobsession('cpu_wait_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                                plot_fixed_kmobsession('page_stats_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                                plot_fixed_kmobsession('io_stats_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                                plot_fixed_kmobsession('disk_stats_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params)
                            if params['emanestats']:
                                for stat in get_emane_mac_stats(script_dir):
                                    plot_fixed_kmobsession('emane_stats_fixed_kmobsession', k, mob, session, time_str, script_dir, data_dir, params, add_to_envstr=';stat=\'%s\''%stat)
                elif len(variables['nodes']) <= 1 and len(variables['dimension']) <= 1 and len(variables['cpudelay']) <=1 and len(variables['mobility']) <=1 and len(variables['rctrl_delay']) <= 1:
                    for var in variables['srcrates']:
                        for session in session_ids:
                            if k > 1: plot_fixed_kvarsession('op_qlen_fixed_kvarsession', k, 'srcrate', var, 'r', session, time_str, script_dir, data_dir, params)
                            plot_fixed_kvarsession('tx_lat_fixed_kvarsession', k, 'srcrate', var, 'r', session, time_str, script_dir, data_dir, params)
                else:
                    for var in variables['srcrates']:
                        for session in session_ids:
                            if k > 1: plot_fixed_kvarsession('op_qlen_fixed_kvarsession', k, 'srcrate', var, 'r', session, time_str, script_dir, data_dir, params)
                            plot_fixed_kvarsession('tx_lat_fixed_kvarsession', k, 'srcrate', var, 'r', session, time_str, script_dir, data_dir, params)
                    #TODO
                    pass
                # Uncomment this and tweak params when plotting movement analysis.
                #for node in range(3,10):
                #    plot_fixed_kmobsession('node_distances', k, mob, session, time_str, script_dir, data_dir, params, add_to_envstr=';node=\'n%d\''%node)

            logdir = '%s/%s'%(data_dir, time_str)
            chmod_dir(logdir)
            sharedir = os.path.expanduser('~/shares/%s'%time_str)
            print sharedir
            if not os.path.isdir(sharedir): os.mkdir(sharedir)
            copy_pdfs(logdir, sharedir)
            copy_results(logdir, sharedir)

    finally:
        if params['notifyAddr']:
            notify('Job %s@%s complete'%(time_str,socket.gethostname()), params['notifyAddr'], params['notifySmtp'])

def get_daemon_server():
    if not 'server' in globals(): return None
    global server
    return server

def get_session_dir(k, var, var_suffix, session, time_str, data_dir):
    return '%s/%s/%dk/%.2f%s/%ds'%(data_dir, time_str, k, var, var_suffix, session)

def create_exp_dirs(ks, mobilities, sessions, time_str, data_dir):
    root_exp_dir = '%s/%s'%(data_dir,time_str)
    os.mkdir(root_exp_dir)
    for k in ks:
        k_dir = '%s/%dk'%(root_exp_dir, k)
        os.mkdir(k_dir)
        for mob in mobilities:
            mob_dir = '%s/%.2fm'%(k_dir, mob)
            for session in range(0, sessions):
                os.mkdir('%s/%ds'%(mob_dir, session))

def run_experiment(ks, variables, sessions, params, time_str, data_dir):
    #TODO: Not using data dir here!
    for k in ks:
        if len(variables['nodes']) > 1:
            for n in variables['nodes']:
                run_sessions(time_str, k, variables['mobility'][0], n, 'n', sessions, params)
        elif len(variables['dimension']) > 1:
            for dim in variables['dimension']:
                params['x'] = dim
                params['y'] = dim
                run_sessions(time_str, k, variables['mobility'][0], variables['nodes'][0], 'd', sessions, params)
        elif len(variables['cpudelay']) > 1:
            for cpudelay in variables['cpudelay']:
                params['defaultProcessingDelay'] = cpudelay 
                run_sessions(time_str, k, variables['mobility'][0], variables['nodes'][0], 'c', sessions, params)
        elif len(variables['srcrates']) > 1:
            params['rateLimitSrc'] = 'true'
            for srcrate in variables['srcrates']:
                params['frameRate'] = srcrate 
                run_sessions(time_str, k, variables['mobility'][0], variables['nodes'][0], 'r', sessions, params)
        elif len(variables['rctrl_delay']) > 0:
            for rctrl_delay in variables['rctrl_delay']:
                params['routingCtrlDelay'] = rctrl_delay 
                run_sessions(time_str, k, variables['mobility'][0], variables['nodes'][0], 'rcd', sessions, params)
        elif len(variables['buf_size']) > 0:
            for buf_size in variables['buf_size']:
                params['maxTotalQueueSizeTuples'] = buf_size 
                params['inputQueueLength'] = buf_size 
                run_sessions(time_str, k, variables['mobility'][0], variables['nodes'][0], 'bsz', sessions, params)
        elif len(variables['retx_timeout']) > 0:
            for retx_timeout in variables['retx_timeout']:
                params['retransmitTimeout'] = retx_timeout 
                params['trySendAlternativesTimeout'] = retx_timeout # TODO: Should this be changed differently
                params['downstreamsUnroutableTimeout'] = retx_timeout # TODO: Should this be changed differently
                params['failureCtrlTimeout'] = retx_timeout + 1000 # TODO: Should this be changed differently?
                run_sessions(time_str, k, variables['mobility'][0], variables['nodes'][0], 'retx', sessions, params)
        else:
            for mob in variables['mobility']:
                run_sessions(time_str, k, mob, variables['nodes'][0], 'm', sessions, params)
                    
def record_var_statistics(ks, variables, sessions, time_str, data_dir, metric_suffix, get_metric_fn):
    raw_vals = {}
    all_loglines = []

    if len(variables['nodes']) > 1:
        var_vals = variables['nodes']
        var_suffix = 'n'
    elif len(variables['dimension']) > 1:
        var_vals = variables['dimension']
        var_suffix = 'd'
    elif len(variables['cpudelay']) > 1:
        var_vals = variables['cpudelay']
        var_suffix = 'c'
    elif len(variables['srcrates']) > 1:
        var_vals = variables['srcrates']
        var_suffix = 'r'
    elif len(variables['rctrl_delay']) > 0:
        var_vals = variables['rctrl_delay']
        var_suffix = 'rcd'
    elif len(variables['buf_size']) > 0:
        var_vals = variables['buf_size']
        var_suffix = 'bsz'
    elif len(variables['retx_timeout']) > 0:
        var_vals = variables['retx_timeout']
        var_suffix = 'retx'
    else: 
        var_vals = variables['mobility']
        var_suffix = 'm'
    
    for k in ks:
        raw_vals[k] = {}
        for (i_var, var) in enumerate(var_vals):
            writeHeader = i_var == 0
            metrics = get_metrics(k, var, var_suffix, sessions, time_str, data_dir, get_metric_fn)
            raw_vals[k][var] = metrics 

            #First record any cumulative stats for fixed kvar
            record_fixed_kvar_statistics(k, var, var_suffix, metrics.values(), time_str, data_dir, metric_suffix)

            #Next record cumulative stats for raw  latencies for fixed kvar (if recording latency stats)
            if metric_suffix == 'lat': 
                raw_latencies = [lat for session_lats in get_metrics(k, var, var_suffix, sessions, time_str, data_dir, get_raw_latencies).values() for lat in session_lats]
                record_fixed_kvar_statistics(k, var, var_suffix, raw_latencies, time_str, data_dir, 'raw-lat')
                percentile_stat_vals = compute_percentile_stats(raw_latencies)

                with open('%s/%s/%dk/%.2f%s/%dk-%s.data'%(data_dir,time_str,k,var,var_suffix,k, metric_suffix),'w' if writeHeader else 'a') as fixed_var_plotdata:
                    if writeHeader: 
                        fixed_var_plotdata.write('#k v mean min .05 .25 .5 .75 .9 .95 .99 max\n')
                    logline = '%d %.4f %s\n'%(k, var, " ".join(map(str, percentile_stat_vals)))
		    fixed_var_plotdata.write(logline)

                with open('%s/%s/%dk-%s.data'%(data_dir,time_str,k, metric_suffix),'w' if writeHeader else 'a') as all_var_plotdata:
                    if writeHeader: 
                        all_var_plotdata.write('#k v mean min .05 .25 .5 .75 .9 .95 .99 max\n')
                    logline = '%d %.4f %s\n'%(k, var, " ".join(map(str, percentile_stat_vals)))
                    all_var_plotdata.write(logline)

                # TODO: Get rid of the below and just use the percentiles above for latency
                # Will need to update gnuplot plots first. 
                meanVal,stdErrVal,maxVal,minVal,medianVal,lqVal,uqVal = compute_stats(raw_latencies)  
                logline = '%.4f %.1f %d %.1f %.1f %.1f %.1f %.1f %.1f\n'%(var,meanVal, k, stdErrVal, maxVal, minVal, medianVal, lqVal, uqVal)
                all_loglines.append(logline)

		"""	
                with open('%s/%s/%dk-%s.data'%(data_dir,time_str,k, metric_suffix),'w' if writeHeader else 'a') as rx_vs_var_plotdata:
                    if writeHeader: 
                        rx_vs_var_plotdata.write('#k=%d\n'%k)
                        rx_vs_var_plotdata.write('#var mean k stdErr max min med lq uq\n')
                    logline = '%.4f %.1f %d %.1f %.1f %.1f %.1f %.1f %.1f\n'%(var,meanVal, k, stdErrVal, maxVal, minVal, medianVal, lqVal, uqVal)
                    all_loglines.append(logline)
		"""	

            else: 
                #Now record any aggregate stats across all kvar 
                meanVal,stdErrVal,maxVal,minVal,medianVal,lqVal,uqVal = compute_stats(metrics.values())  

                # record stats vs varility 
                with open('%s/%s/%dk-%s.data'%(data_dir,time_str,k, metric_suffix),'w' if writeHeader else 'a') as rx_vs_var_plotdata:
                    if writeHeader: 
                        rx_vs_var_plotdata.write('#k=%d\n'%k)
                        rx_vs_var_plotdata.write('#var mean k stdErr max min med lq uq\n')
                    logline = '%.4f %.1f %d %.1f %.1f %.1f %.1f %.1f %.1f\n'%(var,meanVal, k, stdErrVal, maxVal, minVal, medianVal, lqVal, uqVal)
                    rx_vs_var_plotdata.write(logline)
                    all_loglines.append(logline)

    # Write a joint log file too in case we want to plot a histogram
    with open('%s/%s/all-k-%s.data'%(data_dir,time_str,metric_suffix),'w') as all_rx_vs_var_plotdata:
        for line in all_loglines:
           all_rx_vs_var_plotdata.write(line) 

	#TODO Do relative weights with raw_vals.
    if 1 in ks:
        all_loglines = []
        relative_raw_vals = compute_relative_raw_vals(raw_vals)
        if not relative_raw_vals:
            if params['includeFailed']: return 
            else: raise Exception("No relative raw vals - presumably divide by zero?")

        for k in ks:
            for (i_var, var) in enumerate(var_vals):
                writeHeader = i_var == 0
                metrics = relative_raw_vals[k][var]
                meanVal,stdErrVal,maxVal,minVal,medianVal,lqVal,uqVal = compute_stats(metrics.values())  

                #record relative stats vs varility
                with open('%s/%s/%dk-rel-%s.data'%(data_dir,time_str,k,metric_suffix), 'w' if writeHeader else 'a') as rel_rx_vs_var_plotdata:	
                    if writeHeader:
                        rel_rx_vs_var_plotdata.write('#k=%d\n'%k)
                        rel_rx_vs_var_plotdata.write('#var mean k stdErr max min med lq uq\n')
                    logline =  '%.4f %.1f %d %.1f %.1f %.1f %.1f %.1f %.1f\n'%(var,meanVal, k, stdErrVal, maxVal, minVal, medianVal, lqVal, uqVal)
                    rel_rx_vs_var_plotdata.write(logline)
                    all_loglines.append(logline)

        # Write a joint log file too in case we want to plot a histogram
        with open('%s/%s/all-k-rel-%s.data'%(data_dir,time_str,metric_suffix),'w') as all_rel_rx_vs_var_plotdata:
            for line in all_loglines:
               all_rel_rx_vs_var_plotdata.write(line) 

def record_fixed_kvar_statistics(k, var, var_suffix, values, time_str, data_dir, metric_suffix):
    #First sort the metrics in ascending order
    #Then get the total number of metrics
    #Then get the x increment per value 
    cum_distribution = compute_cumulative_percentiles(values) 

    with open('%s/%s/%dk/%.2f%s/cum-%s.data'%(data_dir,time_str,k,var,var_suffix,metric_suffix),'w') as cum_fixed_kvar_plotdata:
	cum_fixed_kvar_plotdata.write('#p %s\n'%metric_suffix)
	for (p, value) in cum_distribution: 
            logline = '%.2f %d\n'%(p,value)
            cum_fixed_kvar_plotdata.write(logline)

def record_tput_vs_lat_statistics(ks, time_str, data_dir):
    for k in ks: 
        with open('%s/%s/%dk-tput.data'%(data_dir,time_str,k),'r') as k_tput_plotdata:
            with open('%s/%s/%dk-lat.data'%(data_dir,time_str,k),'r') as k_lat_plotdata:
               tput_lines = k_tput_plotdata.readlines()
               lat_lines = k_lat_plotdata.readlines()
               if len(tput_lines) != len(lat_lines): raise Exception("Logic error - mismatching tput vs lat lists")
               joint_lines = map(lambda (tput,lat): tput.strip()+" "+lat, zip(tput_lines, lat_lines))
               with open('%s/%s/%dk-tput-vs-lat.data'%(data_dir, time_str, k), 'w') as k_tput_vs_lat_plotdata:
                   for joint_line in joint_lines: 
                        k_tput_vs_lat_plotdata.write(joint_line) 

    # Read in all-k-tput & all-k-lat and merge into all-k-tput-vs-lat.data
    with open('%s/%s/all-k-tput.data'%(data_dir,time_str),'r') as all_k_tput_plotdata:
        with open('%s/%s/all-k-lat.data'%(data_dir,time_str),'r') as all_k_lat_plotdata:
           tput_lines = all_k_tput_plotdata.readlines()
           lat_lines = all_k_lat_plotdata.readlines()
           if len(tput_lines) != len(lat_lines): raise Exception("Logic error - mismatching tput vs lat lists")
           joint_lines = map(lambda (tput,lat): tput.strip()+" "+lat, zip(tput_lines, lat_lines))
           with open('%s/%s/all-k-tput-vs-lat.data'%(data_dir, time_str), 'w') as all_k_tput_vs_lat_plotdata:
               for joint_line in joint_lines: 
                    all_k_tput_vs_lat_plotdata.write(joint_line) 

def get_metrics(k, var, var_suffix, sessions, time_str, data_dir, get_metric_fn):
    metrics = {} 
    for session in sessions:
        logdir = '%s'%(get_session_dir(k,var,var_suffix, session,time_str,data_dir))
        metric = get_metric_fn(logdir)
        metrics[session] = metric 

    return metrics

def get_tput(logdir, include_failed=False):
    #regex = re.compile('src_sink_mean_tput=(\d+)')
    regex = re.compile('sink_sink_mean_tput=(\d+)')
    logfilename = '%s/tput.txt'%logdir
    if not os.path.exists(logfilename): 
        if include_failed: return 0.0
        else: raise Exception("Could not find tput log %s"%logfilename)

    with open(logfilename, 'r') as tput_log:
        for line in tput_log:
            match = re.search(regex, line)
            if match:
                return float(match.group(1))
            
    if include_failed: return 0.0
    else: raise Exception("Could not find tput in %s"%logfilename)

def get_tput_include_failed(logdir):
    return get_tput(logdir, True)

def get_latency(logdir, include_failed=False):
    #TODO: Handle float for latency in regex!
    regex = latency_regex
    logfilename = '%s/latency.txt'%logdir
    if not os.path.exists(logfilename): 
        if include_failed: return max_latency 
        else: raise Exception("Could not find latency log %s"%logfilename)

    with open(logfilename, 'r') as latency_log:
        for line in latency_log:
            match = re.search(regex, line)
            if match:
                return float(match.group(1))
            
    if include_failed: return max_latency 
    else: raise Exception("Could not find %s latency in %s"%(latency_percentile, logfilename))

def get_latency_include_failed(logdir):
    return get_latency(logdir, True)

def get_raw_latencies(logdir):
    result = []
    logfilename = '%s/cum-lat.data'%logdir
    if not os.path.exists(logfilename): return result
    with open(logfilename, 'r') as latency_log:
        for line in latency_log:
            if not line.startswith('#'):
                result.append(int(line.split()[1]))

    return result

def get_emane_mac_stats(script_dir):
    result = []
    for line in open('%s/vldb/config/emane-mac-stats.txt'%script_dir, 'r'):
        result.append(line.strip())
    return result

def plot(p, time_str, script_dir, data_dir, term='pdf', add_to_envstr=''):
    exp_dir = '%s/%s'%(data_dir,time_str)
    tmpl_dir = '%s/vldb/config'%script_dir
    tmpl_file = '%s/%s.plt'%(tmpl_dir,p)

    if term == 'pdf': 
        term_ext = '.pdf'
    elif term == 'latex' or term == 'epslatex': 
        term_ext = '.tex'
    else: raise Exception('Unknown gnuplot terminal type: '+term)

    envstr = 'timestr=\'%s\';outputdir=\'%s\';tmpldir=\'%s\';term=\'%s\';termext=\'%s\';percentile=\'%s\''%(time_str,data_dir,tmpl_dir, term, term_ext,latency_percentile)
    envstr += add_to_envstr 

    plot_proc = subprocess.Popen(['gnuplot', '-e', envstr, tmpl_file], cwd=exp_dir)
    plot_proc.wait()

def latex_plot(p, time_str, script_dir, data_dir):
    plot(p, time_str, script_dir, data_dir, term='epslatex')
    # rm p.pdf
    # ps2pdf -dEPSCrop p.eps p.pdf
    # replace p.tex.tmpl p-text.tex 'input=p, text=todo'
    # pdflatex p-text.tex

def plot_fixed_mob(p, mob, sessions, time_str, script_dir, data_dir, params, term='pdf'):
    mob_envstr = ';mob=\'%.2f\';query=\'%s\';duration=\'%s\';runs=\'%d\''%(mob,params['query'],params['duration'],sessions)
    plot(p, time_str, script_dir, data_dir, term, mob_envstr)

def plot_fixed_kmob(p, k, mob, sessions, time_str, script_dir, data_dir, params, term='pdf'):
    kmob_envstr = ';k=\'%d\';mob=\'%.2f\';query=\'%s\';duration=\'%s\';runs=\'%d\''%(k,mob,params['query'],params['duration'],sessions)
    plot(p, time_str, script_dir, data_dir, term, kmob_envstr)

def plot_fixed_kmobsession(p, k, mob, session, time_str, script_dir, data_dir, params, term='pdf', add_to_envstr=''):
    kmobsession_envstr = '%s;k=\'%d\';mob=\'%.2f\';query=\'%s\';duration=\'%s\';session=\'%d\''%(add_to_envstr,k,mob,params['query'],params['duration'],session)
    plot(p, time_str, script_dir, data_dir, term, kmobsession_envstr)

def plot_fixed_kvarsession(p, k, varname, varval, varext, session, time_str, script_dir, data_dir, params, term='pdf', add_to_envstr=''):
    kvarsession_envstr = '%s;k=\'%d\';varname=\'%s\';var=\'%.2f\';varext=\'%s\';query=\'%s\';duration=\'%s\';session=\'%d\''%(add_to_envstr,k,varname,varval,varext,params['query'],params['duration'],session)
    plot(p, time_str, script_dir, data_dir, term, kvarsession_envstr)

if __name__ == "__main__" or __name__ == "__builtin__":
    parser = argparse.ArgumentParser(description='Run simulations.')
    parser.add_argument('--ks', dest='ks', default='1,2,3,5', help='replication factors [1,2,3,5]')
    parser.add_argument('--h', dest='h', default='2', help='chain length (2)')
    #parser.add_argument('--x', dest='x', default='1200', help='Grid x dimension (1200)')
    #parser.add_argument('--y', dest='y', default='1200', help='Grid y dimension (1200)')
    parser.add_argument('--dimension', dest='dimension', default='1200', help='Grid dimension (1200)')
    parser.add_argument('--cpuDelay', dest='cpu_delay', default='0', help='Processing delay for each operator')
    parser.add_argument('--query', dest='query', default='chain', help='query type: (chain), join, debsgc, fr, frshard, nameassist')
    parser.add_argument('--pausetimes', dest='pts', default='5.0', help='pause times [5.0]')
    parser.add_argument('--sessions', dest='sessions', default='2', help='number of sessions (2)')
    parser.add_argument('--specific', dest='specific', default=False, action='store_true')
    #parser.add_argument('--mobility', dest='mobility', default='static', help='mobility model: static,waypoint')
    #parser.add_argument('--query', dest='query_type', default='linear', help='query type: linear,join,mixed,parallel')
    parser.add_argument('--plotOnly', dest='plot_time_str', default=None, help='time_str of run to plot (hh-mm-DDDddmmyy)[None]')
    parser.add_argument('--nodes', dest='nodes', default='10', help='Total number of core nodes in network')
    parser.add_argument('--model', dest='model', default="Emane", help='Wireless model (Basic, Emane)')
    parser.add_argument('--routing', dest='routing', default='OLSRETX', help='Net layer routing alg (OLSR, OLSRETX)')
    parser.add_argument('--preserve', dest='preserve', default=False, action='store_true', help='Preserve session directories')
    parser.add_argument('--saveconfig', dest='saveconfig', default=False, action='store_true', help='Export the session configuration to an XML file')
    parser.add_argument('--constraints', dest='constraints', default='', help='Initial mapping constraints for each session ')
    parser.add_argument('--placement', dest='placement', default='', help='Explicit static topology to use for all sessions')
    parser.add_argument('--user', dest='user', default='dan', help='Non-root user to start processes with')
    parser.add_argument('--duration', dest='duration', default='100000', help='Mobility params duration')
    parser.add_argument('--maxFanIn', dest='max_fan_in', default='2', help='Max fan-in for join operators (2)')
    parser.add_argument('--sources', dest='sources', default='2', help='Number of unreplicated sources (for join operators)')
    parser.add_argument('--sinks', dest='sinks', default='1', help='Number of unreplicated sinks')
    parser.add_argument('--trace', dest='trace', default=None, help='Mobility trace to use, if any (sftaxi, debs13)')
    parser.add_argument('--verbose', dest='verbose', action='store_true', default=False, help='Verbose core logging')
    parser.add_argument('--masterPostDelay', dest='master_postdelay', default=None, help='Time to wait after starting master service before deploying query')
    parser.add_argument('--workerPreDelay', dest='worker_predelay', default=None, help='Time to wait before starting worker')
    parser.add_argument('--refresh', dest='refresh_ms', default=None, help='Time between updating node position in model')
    parser.add_argument('--scaleSinks', dest='scale_sinks', default=False, action='store_true', help='Replicate sinks k times')
    parser.add_argument('--sinkScaleFactor', dest='sink_scale_factor', default=0, help='Replicate sinks this many times')
    parser.add_argument('--colocateSrcSink', dest='colocate_src_sink', default=False, action='store_true', help='Colocate src and sink workers')
    parser.add_argument('--quagga', dest='quagga', default=False, action='store_true', help='Start quagga services (zebra, vtysh)')
    parser.add_argument('--pcap', dest='pcap', default=False, action='store_true', help='Start pcap service for workers.')
    parser.add_argument('--emanestats', dest='emanestats', default=False, action='store_true', help='Start emanestats service on master')
    parser.add_argument('--dstat', dest='dstat', default=False, action='store_true', help='Start dstat service on master.')
    parser.add_argument('--duplex', dest='duplex', default=False, action='store_true', help='Send in both directions for iperf tests')
    parser.add_argument('--iperf', dest='iperf', default=False, action='store_true', help='Do an iperf test')
    parser.add_argument('--iperfcxns', dest='iperfcxns', default=None, help='Do an iperf test')
    parser.add_argument('--sinkDisplay', dest='sink_display', default=False, action='store_true', help='Start a gui for query output')
    parser.add_argument('--gui', dest='gui', default=False, action='store_true', help='Show placements in core GUI')
    parser.add_argument('--slave', dest='slave', default=None, help='Hostname of slave')
    parser.add_argument('--emaneMobility', dest='emane_mobility', default=False, action='store_true', help='Use emane location events for mobility (instead of ns2)')
    parser.add_argument('--notifyAddr', dest='notify_addr', default=None, help='Send email from/to addr on job completion.')
    parser.add_argument('--notifySmtp', dest='notify_smtp', default='smarthost.cc.ic.ac.uk', help='Smtp server to use for email notifications.')
    parser.add_argument('--xyScale', dest='xy_scale', default=None, help='Scale factor for each (x,y) coordinate (static placement only)')
    parser.add_argument('--meanderRouting', dest='meander_routing', default=None, help='Override meander routing alg (backpressure, hash, shortestPath)')
    parser.add_argument('--noiseNodes', dest='noise_nodes', default=0, help='Number of rf noise sources')
    parser.add_argument('--roofnet', dest='roofnet', default=False, action='store_true', help='Use roofnet placements and packet losses')
    parser.add_argument('--emaneModel', dest='emane_model', default='Ieee80211abg', help='Emane model to use (if using emane)')
    parser.add_argument('--txRateMode', dest='txratemode', default='4', help='Emane 802.11 transmission rate mode (4=11Mb/s, 12=54Mb/s)')
    parser.add_argument('--srcRates', dest='src_rates', default=None, help='Fixed frame rates for sources to send at.')
    parser.add_argument('--includeFailed', dest='include_failed', default=False, action='store_true', help='Include results of failed runs in recorded stats.')
    parser.add_argument('--pinAll', dest='pin_all', default=False, action='store_true', help='pin all nodes if pinned seed defined')
    parser.add_argument('--injectFailures', dest='inject_failures', default=None, help='Start a failure cycle service according to config file.')
    parser.add_argument('--routingCtrlDelay', dest='rctrl_delay', default=None, help='Routing control delay (ms)')
    parser.add_argument('--bufSize', dest='buf_size', default=None, help='Max size of intermediate buffers')
    parser.add_argument('--retransmitTimeout', dest='retx_timeout', default=None, help='Time to wait before retransmitting')
    parser.add_argument('--initialPause', dest='initial_pause', default=None, help='Initial pause before source starts sending (ms)')
    parser.add_argument('--pinnedSeed', dest='pinned_seed', default=None, help='Random seed to use for initial shuffle of pinned nodes')

    args=parser.parse_args()

    ks=map(lambda x: int(x), args.ks.split(','))
    pts=map(lambda x: float(x), args.pts.split(','))
    sessions=args.sessions
    nodes=map(lambda x: int(x), args.nodes.split(','))
    dimension=map(lambda x: int(x), args.dimension.split(','))
    cpudelay=map(lambda x: int(x), args.cpu_delay.split(','))
    srcrates=map(lambda x: int(x), args.src_rates.split(',')) if args.src_rates else []
    rctrl_delay=map(lambda x: int(x), args.rctrl_delay.split(',')) if args.rctrl_delay else []
    buf_size=map(lambda x: int(x), args.buf_size.split(',')) if args.buf_size else []
    retx_timeout=map(lambda x: int(x), args.retx_timeout.split(',')) if args.retx_timeout else []

    variables = { "mobility" : pts, "nodes" : nodes, "dimension" : dimension,
            "cpudelay" : cpudelay, "srcrates": srcrates, "rctrl_delay" : rctrl_delay,
            "buf_size" : buf_size, "retx_timeout": retx_timeout}

    if len(filter(lambda x: x > 1, map(len, variables.values()))) > 1:
        raise Exception("Multiple parameters being varied at the same time: %s"%str(variables))

    params = {} 
    params['cmdline'] = " ".join(sys.argv[:])
    print params['cmdline']
	
    if args.model: params['model']=args.model
    params['net-routing']=args.routing
    params['specific']=args.specific
    params['preserve']=args.preserve
    params['h']=int(args.h)
    if len(dimension) == 1:
        params['x']=args.dimension
        params['y']=args.dimension
    if len(cpudelay) == 1:
        params['defaultProcessingDelay'] = int(cpudelay[0]) 
    params['query']=args.query
    params['saveconfig']=args.saveconfig
    params['constraints']=args.constraints
    params['placement']=args.placement
    params['user']=args.user
    params['duration']=args.duration
    params['sources']=args.sources
    params['sinks']=args.sinks
    params['fanin']=args.max_fan_in
    params['pyScaleOutSinks']=args.scale_sinks
    params['scaleOutSinks']=pybool_to_javastr(args.scale_sinks)
    params['sinkScaleFactor']=args.sink_scale_factor
    params['colocateSrcSink']=args.colocate_src_sink
    params['quagga']=args.quagga
    params['pcap']=args.pcap
    params['emanestats']=args.emanestats
    params['dstat']=args.dstat
    params['duplex']=args.duplex
    params['iperf']=args.iperf
    params['iperfcxns']=args.iperfcxns
    params['sinkDisplay']=args.sink_display
    params['enableSinkDisplay']=pybool_to_javastr(args.sink_display)
    params['enableGUI']= "true" if args.gui else "false"
    params['slave']= args.slave 
    params['verbose']= args.verbose 
    params['emaneMobility']= args.emane_mobility
    params['notifyAddr'] = args.notify_addr
    params['notifySmtp'] = args.notify_smtp
    params['xyScale'] = args.xy_scale
    params['noiseNodes'] = int(args.noise_nodes)
    params['roofnet'] = args.roofnet
    params['emaneModel'] = args.emane_model
    params['txratemode'] = args.txratemode
    params['includeFailed'] = args.include_failed
    params['pinnedSeed'] = args.pinned_seed
    params['pinAll'] = args.pin_all
    params['injectFailures'] = args.inject_failures
    if args.trace: params['trace']=args.trace
    if args.master_postdelay: params['master_postdelay'] = args.master_postdelay
    if args.worker_predelay: params['worker_predelay'] = args.worker_predelay
    if args.refresh_ms: params['refresh_ms'] = args.refresh_ms
    if args.meander_routing: params['meanderRouting'] = args.meander_routing
    if args.initial_pause: params['initialPause'] = args.initial_pause

    #main(ks,pts,nodes,sessions,params,plot_time_str=args.plot_time_str)
    main(ks,variables,sessions,params,plot_time_str=args.plot_time_str)

