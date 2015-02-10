#!/usr/bin/python

import subprocess,os,time,re,argparse

eg_dir = os.path.dirname(os.path.realpath(__file__))
data_dir = '%s/log'%eg_dir

def main(exp_time_str):

    sim_env = os.environ.copy()
    print "Analysing logs in %s/%s"%(data_dir,exp_time_str)       

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Analyse emulation logs')
    parser.add_argument('--expTimeStr', dest='exp_time_str', help='time_str of run to plot (hh-mm-ss-DDDddmmyy)')
    args=parser.parse_args()

    main(args.exp_time_str)

