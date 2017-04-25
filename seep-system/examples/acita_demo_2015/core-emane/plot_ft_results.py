#!/usr/bin/python

import subprocess,os,time,re,argparse,sys

script_dir = os.path.dirname(os.path.realpath(__file__))

def main(time_str): 

    #time_str = 'ft_results'

    data_dir = '%s/log'%script_dir
    plot('sr_tput', time_str, script_dir, data_dir)
    plot('sr_retransmissions', time_str, script_dir, data_dir)

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

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Plot ft experiments')
    parser.add_argument('--timeStr', dest='time_str', default="ft_results", help='log dir containing exp results')
    args=parser.parse_args()

    main(args.time_str)
