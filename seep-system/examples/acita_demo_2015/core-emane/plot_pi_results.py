#!/usr/bin/python

import subprocess,os,time,re,argparse,sys

from compute_stats import compute_stats

script_dir = os.path.dirname(os.path.realpath(__file__))

# alg =1, k=2, tput = 3
exp_compute_coords = { 'pi-tput-vs-rr' : (1,3), 'pi-tput-scaling' : (2,3)}

def main(time_strs, exp_name): 

    #time_str = 'ft_results'

    data_dir = '%s/log'%script_dir
    #plot('pi_tput', time_str, script_dir, data_dir)
    #if len(time_strs) > 1: # Cross plot
    if not time_strs: # Cross plot
        # Get raw tputs etc

        # Gen raw exps.txt

        # Create aggregated plot data 
        for exp in exp_compute_coords.keys(): 
            # Get the raw results for each run of this experiment 
            raw_results = get_raw_result_lines('%s/pi_results/%s/exps.txt'%(data_dir, exp))

            # Compute the aggregate results across all runs
            exp_results = compute_exp_results(exp, raw_results)
            write_exp_results(exp, exp_results, data_dir) # Record aggregated results
            plot(exp, 'pi_results/%s'%exp, script_dir, data_dir, add_to_envstr=';expname=\'%s\''%'fr')

    elif exp_name:
        plot('pi_tput', time_str, script_dir, data_dir, add_to_envstr=';expname=\'%s\''%exp_name)
    else:
        for time_str in time_strs:
            plot('pi_op_cum_tput_fixed_kvarsession', time_str, script_dir, data_dir)
            plot('pi_op_tput_fixed_kvarsession', time_str, script_dir, data_dir)
            plot('pi_op_cum_util_fixed_kvarsession', time_str, script_dir, data_dir)
            plot('pi_op_weight_info_fixed_kvarsession', time_str, script_dir, data_dir)

def plot(p, time_str, script_dir, data_dir, term='pdf', add_to_envstr=''):
    exp_dir = '%s/%s'%(data_dir,time_str)
    tmpl_dir = '%s/vldb/config'%script_dir
    tmpl_file = '%s/%s.plt'%(tmpl_dir,p)

    if term == 'pdf': 
        term_ext = '.pdf'
    elif term == 'latex' or term == 'epslatex': 
        term_ext = '.tex'
    else: raise Exception('Unknown gnuplot terminal type: '+term)

    envstr = 'timestr=\'%s\';outputdir=\'%s\';tmpldir=\'%s\';term=\'%s\';termext=\'%s\''%(time_str,data_dir,tmpl_dir, term, term_ext)
    envstr += add_to_envstr 

    plot_proc = subprocess.Popen(['gnuplot', '-e', envstr, tmpl_file], cwd=exp_dir)
    plot_proc.wait()

def get_raw_result_lines(raw_exps_file):
    with open(raw_exps_file, 'r') as raw:
        # Read and parse relevant data
        lines = (line.rstrip() for line in raw)
        lines = list(line for line in lines if line and not line.startswith('#'))
        return lines

## Helper functions to compute experiment results
def compute_exp_results(exp, raw_results):
    x = exp_compute_coords[exp][0]
    y = exp_compute_coords[exp][1]
    return compute_xy_exp_results(x, y, raw_results)

def compute_xy_exp_results(x, y, raw_results):
    exp_results = {}
    for line in raw_results:
        els = line.split(' ')
        exp_results[els[x]] = exp_results.get(els[x], []) + [els[y]]

    for exp in exp_results:
        exp_results[exp] = compute_stats(map(float, exp_results[exp]))

    return exp_results

## Helper function to record experiment results
def write_exp_results(exp, exp_results, data_dir):
    with open('%s/pi_results/%s/results.txt'%(data_dir, exp), 'w') as rf:
        for exp in sorted(exp_results.keys()):
            rf.write('%s %s\n'%(exp, " ".join('{:.1f}'.format(x) for x in exp_results[exp])))

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Plot ft experiments')
    #parser.add_argument('--timeStr', dest='time_str', help='log dir containing exp results')
    parser.add_argument('--timeStrs', default=None, dest='time_strs', help='log dir containing exp results')
    parser.add_argument('--expName', default=None, dest='exp_name', help='plot aggregate results with this name')
    args=parser.parse_args()

    timeStrs = args.time_strs.split(',') if args.time_strs else []
    #main(args.time_str, args.exp_name)
    main(timeStrs, args.exp_name)

