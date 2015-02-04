#!/usr/bin/python

import subprocess,os,time,re,argparse

master_port = 3500
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

            print 'Starting workers'
            workers = start_workers(w, k, time_str, sim_env)
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

                if not poll_workers(workers):
                   break

                time.sleep(0.5)

        finally:
            if workers:
                stop_workers(workers)
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
        args = ['java', '-jar', '%s/lib/%s'%(eg_dir, seep_jar), 'Master',
                '%s/dist/%s'%(eg_dir,query_jar), query_base]
        p = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=log, stderr=subprocess.STDOUT, env=sim_env)
        return p

def stop_master(p):
    p.stdin.write('6\n')
    p.stdin.close()
    p.terminate()
    print 'Terminated master.'

def start_workers(w, k, time_str, sim_env):
    nxt_worker_port = master_port + 1
    #Start source
    logfilename = wlog(w,k,nxt_worker_port,time_str)
    source = start_worker(nxt_worker_port, logfilename, sim_env)
    print 'Started source on %d'%nxt_worker_port

    #Start k * w op workers
    ops = []
    for log_op in range(0,logical_ops-2):
        phys_ops = []
        for phys_op in range(0, k):
            nxt_worker_port += 1
            logfilename = wlog(w,k,nxt_worker_port,time_str)
            p = start_worker(nxt_worker_port, logfilename, sim_env)
            print 'Started op log=%d, k=%d on %d'%(log_op, phys_op, nxt_worker_port)
            phys_ops.append(p)
        ops.append(phys_ops)

    #Start sink
    nxt_worker_port += 1
    logfilename = wlog(w,k,nxt_worker_port,time_str)
    sink = start_worker(nxt_worker_port, logfilename, sim_env)
    print 'Started sink on %d'%nxt_worker_port

    return (source, ops, sink)

def start_worker(port, logfilename, sim_env):
    with open(data_dir+'/'+logfilename, 'w') as log:
        args = ['java', '-jar', '%s/lib/%s'%(eg_dir, seep_jar), 'Worker', '%d'%port]
        p = subprocess.Popen(args, stdout=log, stderr=subprocess.STDOUT, env=sim_env)
        return p

def poll_workers(workers):
    if not workers[0].poll() is None:
        return False
    if not workers[2].poll() is None:
        return False

    for log in workers[1]:
        for phys in log:
            if not phys.poll() is None:
                return False

    print 'All workers still running.'
    return True

def stop_workers(workers):
    try:
        workers[0].terminate()
        print 'Stopped source.'
    except:
        pass

    try:
        workers[2].terminate()
        print 'Stopped sink.'
    except:
        pass

    for l, log in enumerate(workers[1]):
        for k, phys in enumerate(log):
            try:
                phys.terminate()
                print 'Terminate worker log=%d, k=%d'%(l,k)
            except:
                pass

def mlog(k, time_str):
    return 'master-k%d-%s.log'%(k,time_str)

def wlog(w, k, port, time_str):
    return 'worker-w%d-k%d-port%d-%s.log'%(w,k,port,time_str)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Run simulations.')

    parser.add_argument('--workers', dest='w', default='6', help='Total number of workers to start (3)')
    parser.add_argument('-k', dest='k', default='2', help='Number of replicas for each intermediate operator')
    parser.add_argument('--plotOnly', dest='plot_time_str', default=None, help='time_str of run to plot (hh-mm-DDDddmmyy)[None]')
    parser.add_argument('--nomaster', dest='no_master', default=False, help='Disable master (False)')
    
    args=parser.parse_args()
    
    main(int(args.w), int(args.k), args.plot_time_str, not bool(args.no_master))

