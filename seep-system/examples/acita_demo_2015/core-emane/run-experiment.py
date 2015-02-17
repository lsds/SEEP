#!/usr/bin/python

import subprocess,os,time,re,argparse

from compute_stats import compute_stats,median,compute_relative_raw_vals
from run_sessions import run_sessions

ticksPerSecond = 1000.0 * 1000.0 * 1000.0
maxWaitSeconds = 1000000000

def main(ks,mobilities,sessions,plot_time_str=None):

    script_dir = os.path.dirname(os.path.realpath(__file__))
    data_dir = '%s/log'%script_dir

    if plot_time_str:
        time_str = plot_time_str
    else:
        time_str = time.strftime('%H-%M-%S-%a%d%m%y')
        # create exp dirs.
        #create_exp_dirs(ks, mobilities, sessions, time_str, data_dir)
        run_experiment(ks, mobilities, sessions, time_str, data_dir )

    record_statistics(ks, mobilities, sessions, time_str, data_dir)

    plot_tput_vs_mobility(time_str, script_dir, data_dir)
    plot_median_tput_vs_mobility(time_str, script_dir, data_dir)

def get_session_dir(k, mob, session, time_str, data_dir):
    return '%s/%s/%dk/%.2fm/%ds'%(data_dir, time_str, k, mob, session)

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

def run_experiment(ks, mobilities, sessions, time_str, data_dir):
    #TODO: Not using data dir here!
    for k in ks:
        for mob in mobilities:
            run_sessions(time_str, k,mob,sessions)
                    
def record_statistics(ks, mobilities, sessions, time_str, data_dir):
    vals = {}
    aggregate_vals = {}
    raw_vals = {}
    for k in ks:
        vals[k] = {}
        raw_vals[k] = {}
        aggregate_vals[k] = {}
        for (i_mob, mob) in enumerate(mobilities):
            vals[k][mob] = {} 
            aggregate_vals[k][mob] = {}
            writeHeader = i_mob == 0
            tputs = get_tputs(k, mob, sessions, time_str, data_dir)
            raw_vals[k][mob] = tputs
            stats = {}
            for session in range(0, sessions): 
                stats[session] = {'MEAN-TPUT':tputs[session]}
            vals[k][mob] = stats 
            meanVal,stdDevVal,maxVal,minVal,medianVal,lqVal,uqVal = compute_stats(tputs.values())  

            aggregate_vals[k][mob] = { "CONFIG-MEAN-TPUT":"%.1f"%meanVal,
                                        "CONFIG-MEDIAN-TPUT":"%.1f"%medianVal,
                                        "CONFIG-MAX-TPUT":"%.1f"%maxVal,
                                        "CONFIG-MIN-TPUT":"%.1f"%minVal,
                                        "CONFIG-STDDEV-TPUT":"%.1f"%stdDevVal}
            # record stats vs mobility 
            with open('%s/%s/%dk.data'%(data_dir,time_str,k),'w' if writeHeader else 'a') as rx_vs_mob_plotdata:
                if writeHeader: 
                    rx_vs_mob_plotdata.write('#k=%d\n'%k)
                    rx_vs_mob_plotdata.write('#mob mean ? stdDev max min med lq uq\n')
                rx_vs_mob_plotdata.write('%.4f %.1f %d %.1f %.1f %.1f %.1f %.1f %.1f\n'%(mob,meanVal, 1, stdDevVal, maxVal, minVal, medianVal, lqVal, uqVal))
	#TODO Do relative weights with raw_vals.
#Fix up tabs!!!
    if 1 in ks:
        relative_raw_vals = compute_relative_raw_vals(raw_vals)
        for k in ks:
            for (i_mob, mob) in enumerate(mobilities):
                writeHeader = i_mob == 0
                tputs = relative_raw_vals[k][mob]
                meanVal,stdDevVal,maxVal,minVal,medianVal,lqVal,uqVal = compute_stats(tputs.values())  

                #record relative stats vs mobility
                with open('%s/%s/%dk-rel.data'%(data_dir,time_str,k), 'w' if writeHeader else 'a') as rel_tput_vs_mob_plotdata:	
                    if writeHeader:
                        rel_tput_vs_mob_plotdata.write('#k=%d\n'%k)
                        rel_tput_vs_mob_plotdata.write('#mob mean ? stdDev max min med lq uq\n')
                    rel_tput_vs_mob_plotdata.write('%.4f %.1f %d %.1f %.1f %.1f %.1f %.1f %.1f\n'%(mob,meanVal, 1, stdDevVal, maxVal, minVal, medianVal, lqVal, uqVal))
	
	
    return (vals,aggregate_vals)

def get_tputs(k, mob, sessions, time_str, data_dir):
    tputs = {} 
    for session in range(0,sessions):
        logfilename = '%s/tput.txt'%(get_session_dir(k,mob,session,time_str,data_dir))
        tput = get_tput(logfilename)
        tputs[session] = tput 

    return tputs

def get_tput(logfilename):
    regex = re.compile('src_sink_mean_tput=(\d+)')
    with open(logfilename, 'r') as tput_log:
        for line in tput_log:
            match = re.search(regex, line)
            if match:
                return float(match.group(1))
            
    raise Exception("Could not find tput in %s"%logfilename)

def plot_tput_vs_mobility(time_str, script_dir, data_dir):
    exp_dir = '%s/%s'%(data_dir,time_str)
    print exp_dir
    plot_proc = subprocess.Popen(['gnuplot', '-e',
'timestr=\'%s\';outputdir=\'%s\''%(time_str,data_dir),
script_dir+'/vldb/config/tput_vs_mobility.plt'], cwd=exp_dir)
    plot_proc.wait()

def plot_median_tput_vs_mobility(time_str, script_dir, data_dir):
    exp_dir = '%s/%s'%(data_dir,time_str)
    print exp_dir
    plot_proc = subprocess.Popen(['gnuplot', '-e',
'timestr=\'%s\';outputdir=\'%s\''%(time_str,data_dir),
script_dir+'/vldb/config/median_tput_vs_mobility.plt'], cwd=exp_dir)
    plot_proc.wait()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Run simulations.')
    parser.add_argument('--ks', dest='ks', default='1,2,3,5', help='replication factors [1,2,3,5]')
    parser.add_argument('--pausetimes', dest='pts', default='0.0,2.0,4.0,6.0,8.0', help='pause times [0.0,2.0,4.0,6.0,8.0]')
    parser.add_argument('--sessions', dest='sessions', default='2', help='number of sessions (2)')
    #parser.add_argument('--mobility', dest='mobility', default='static', help='mobility model: static,waypoint')
    #parser.add_argument('--query', dest='query_type', default='linear', help='query type: linear,join,mixed,parallel')
    parser.add_argument('--plotOnly', dest='plot_time_str', default=None, help='time_str of run to plot (hh-mm-DDDddmmyy)[None]')
    #parser.add_argument('--placements', dest='placements', default='', help='placements 0,1,2,...')
    args=parser.parse_args()

    ks=map(lambda x: int(x), args.ks.split(','))
    pts=map(lambda x: float(x), args.pts.split(','))
    #mobs=map(lambda x: float(x), [] if args.mobility in 'static' else args.ds.split(','))
    sessions=int(args.sessions)
   # placements=map(lambda x: str(int(x)), [] if not args.placements else args.placements.split(','))

    main(ks,pts,sessions,plot_time_str=args.plot_time_str)

