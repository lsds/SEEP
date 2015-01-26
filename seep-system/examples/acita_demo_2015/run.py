#!/usr/bin/python

import subprocess,os,time,re,argparse

master_port = 3500
logical_ops = 3
eg_dir = os.path.dirname(os.path.realpath(__file__))
seep_jar = 'seep-system-0.0.1-SNAPSHOT.jar'
data_dir = '%s/log'%eg_dir

def main(w, k, plot_time_str):
    print 'Starting %d workers to execute query with %d logical operators and %d op replicas'%(w,logical_ops,k)
    if w < 2 + (logical_ops - 2) * k:
        raise Exception("Not enough workers (%d) for chain query with %d logical operators (including src and sink) given replication factor %d"%(w,logical_ops, k))
    
    sim_env = os.environ.copy()
       
    if plot_time_str:
        time_str = plot_time_str
    else:
        time_str = time.strftime('%H-%M-%a%d%m%y')

        print 'Starting master'
        master_logfilename = mlog(k, time_str) 
        master = start_master(master_logfilename, sim_env)

        time.sleep(5)


        print 'Starting workers'
        workers = start_workers(w, k, time_str, sim_env)
        time.sleep(5)
    
        stop_workers(workers)

        time.sleep(1)
        stop_master(master)

def start_master(logfilename, sim_env):
    with open(data_dir+'/'+logfilename, 'w') as log:
        args = ['java', '-jar', '%s/lib/%s'%(eg_dir, seep_jar), 'Master', '%s/dist/query.jar', 'TODO']
        p = subprocess.Popen(args, stdout=log, stderr=subprocess.STDOUT, env=sim_env)
        return p

def stop_master(p):
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

def stop_workers(workers):
    workers[0].terminate()
    print 'Stopped source.'

    workers[2].terminate()
    print 'Stopped sink.'

    for l, log in enumerate(workers[1]):
        for k, phys in enumerate(log):
            phys.terminate()
            print 'Terminate worker log=%d, k=%d'%(l,k)

def mlog(k, time_str):
    return 'master-k%d-%s.log'%(k,time_str)

def wlog(w, k, port, time_str):
    return 'worker-w%d-k%d-port%d-%s.log'%(w,k,port,time_str)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Run simulations.')

    parser.add_argument('--workers', dest='w', default='3', help='Total number of workers to start (3)')
    parser.add_argument('-k', dest='k', default='1', help='Number of replicas for each intermediate operator')
    parser.add_argument('--plotOnly', dest='plot_time_str', default=None, help='time_str of run to plot (hh-mm-DDDddmmyy)[None]')
    
    args=parser.parse_args()
    
    main(int(args.w), int(args.k), args.plot_time_str)

