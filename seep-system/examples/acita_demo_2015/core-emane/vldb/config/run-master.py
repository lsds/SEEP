#!/usr/bin/python

import subprocess,os,time,re,argparse

logical_ops = 4
eg_dir = os.path.dirname(os.path.realpath(__file__))
seep_jar = 'seep-system-0.0.1-SNAPSHOT.jar'
query_jar = 'acita_demo_2015.jar'
query_base = 'Base'
data_dir = '%s/log'%eg_dir

def main(w, k, plot_time_str, run_master):
    print 'Starting %d workers to execute query with %d logical operators and %d op replicas'%(w,logical_ops,k)
    if w < 2 + (logical_ops - 2) * k:
        raise Exception("Not enough workers (%d) for chain query with %d logical operators (including src and sink) given replication factor %d"%(w,logical_ops, k))
    
    sim_env = os.environ.copy()
       
    if plot_time_str:
        time_str = plot_time_str
    else:
        time_str = time.strftime('%H-%M-%a%d%m%y')

        try:
            if run_master:
                print 'Starting master'
                master_logfilename = mlog(k, time_str) 
                master = start_master(master_logfilename, sim_env)

                time.sleep(5)

            print 'Waiting for workers.'
            time.sleep(10)

            if run_master:
                deploy_query(master)
                time.sleep(5)

                run_query(master)
                time.sleep(5)

            print 'Waiting for any process to terminate.'
            while True:
                if run_master and not master.poll() is None:
                   break 

                time.sleep(0.5)

        finally:
            if run_master and master:
                stop_master(master)

def deploy_query(master):
    print 'Deploying query'
    # Bad idea according to docs but anyway...
    master.stdin.write('1\n')
    #master.communicate(input='1\n')
    print 'Deployed query'

def run_query(master):
    print 'Starting query'
    master.stdin.write('2\n')
    time.sleep(0.5)
    master.stdin.write('\n')
    print 'Started query'

def start_master(logfilename, sim_env):
    with open(data_dir+'/'+logfilename, 'w') as log:
        args = ['java', '-DuseCoreAddr="true"','-jar', '%s/lib/%s'%(eg_dir, seep_jar), 'Master',
                '%s/dist/%s'%(eg_dir,query_jar), query_base]
        p = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=log, stderr=subprocess.STDOUT, env=sim_env)
        return p

def stop_master(p):
    p.stdin.write('6\n')
    p.stdin.close()
    p.terminate()
    print 'Terminated master.'

def mlog(k, time_str):
    return 'master-k%d-%s.log'%(k,time_str)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Run simulations.')

    parser.add_argument('--workers', dest='w', default='6', help='Total number of workers to start (3)')
    parser.add_argument('-k', dest='k', default='2', help='Number of replicas for each intermediate operator')
    parser.add_argument('--plotOnly', dest='plot_time_str', default=None, help='time_str of run to plot (hh-mm-DDDddmmyy)[None]')
    parser.add_argument('--nomaster', dest='no_master', default=False, help='Disable master (False)')
    
    args=parser.parse_args()
    
    main(int(args.w), int(args.k), args.plot_time_str, not bool(args.no_master))

