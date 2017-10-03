#!/usr/bin/python

import subprocess,os,time,re,argparse,socket

master_port = 3500
worker_base_port = 3500
eg_dir = os.path.dirname(os.path.realpath(__file__))
seep_jar = 'seep-system-0.0.1-SNAPSHOT.jar'
query_jar = 'acita_demo_2015.jar'
query_base = 'Base'
data_dir_base = '%s/log'%eg_dir
user = 'root'

def main(k,h,query,w,hostname, extra_props_dir, local_worker_id=1):
    sim_env = os.environ.copy()
    time_str = time.strftime('%H-%M-%a%d%m%y')
    session_params = read_session_params()

    worker_predelay = int(session_params.get('worker_predelay', '5'))
    try:
        print 'Waiting 5 seconds to start worker.'
        time.sleep(worker_predelay)
        worker_logfilename = wlog(w, k, query, hostname,local_worker_id, time_str) 
        print 'Starting worker with logfile %s'%worker_logfilename
        worker = start_worker(k, h, query, worker_logfilename, sim_env, extra_props_dir, local_worker_id)

        print 'Waiting for any process to terminate.'
        while True:
            if not worker.poll() is None:
               break 

            time.sleep(0.5)

    finally:
        if worker:
            stop_worker(worker)

def start_worker(k, h, query, logfilename, sim_env, extra_props_dir, local_worker_id):
    worker_port = worker_base_port + local_worker_id
    extra_java_params=map(lambda line: '-D'+line, read_extra_params())
    worker_processors = ",".join(map(str, range(3,64,4)))
    #worker_processors = "25-31"
    #taskset_params = ['taskset', '-ac', worker_processors]
    taskset_params = []
    with open('%s%d/%s'%(data_dir_base,local_worker_id,logfilename), 'w') as log:
        args = ['sudo', '-u', user] + taskset_params + ['java', '-verbose:gc',
                '-XX:+PrintGCDetails', '-XX:+PrintGCTimeStamps',
                '-Dplatform.dependencies=true',
                '-Djava.awt.headless=true',
                '-DuseCoreAddr=true','-DreplicationFactor=%d'%k,'-DchainLength=%d'%h,'-DqueryType=%s'%query, '-DextraProps=%s'%extra_props_dir] + extra_java_params + ['-jar', '%s/../lib/%s'%(eg_dir, seep_jar), 'Worker', '%d'%worker_port]
        p = subprocess.Popen(args, stdout=log, stderr=subprocess.STDOUT, env=sim_env)
        return p

def stop_worker(worker):
    try:
        worker.terminate()
        print 'Stopped worker.'
    except:
        pass

def wlog(w, k, query, hostname, local_worker_id, time_str):
    return 'worker-w%d-k%d-%s-%s-%d-%s.log'%(w,k,query,hostname,local_worker_id,time_str)

def read_extra_props_dir():
    with open('../extraPropsDir.txt', 'rb') as f:
        for line in f:
            return line.strip()

def read_k():
    with open('../k.txt', 'rb') as f:
        for line in f:
            return int(line.strip())

def read_h():
    with open('../h.txt', 'rb') as f:
        for line in f:
            return int(line.strip())

def read_query():
    with open('../query.txt', 'rb') as f:
        for line in f:
            return line.strip()

def read_extra_params():
    extra_params=[]
    with open('../extra_params.txt', 'rb') as f:
        for line in f:
            extra_params.append(line.strip()) 
    return extra_params

def read_session_params():
    session_params={}
    with open('../session_params.txt', 'rb') as f:
        for line in f:
            kv = line.strip().split('=')
            session_params[kv[0]]=kv[1] 
    return session_params

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Run simulations.')

    parser.add_argument('--k', dest='k', help='Number of replicas for each intermediate operator')
    parser.add_argument('--h', dest='h', help='Number of logical operators (chain)')
    parser.add_argument('--query', dest='query', help='Query type: chain, join')
    parser.add_argument('--id', dest='local_worker_id', help='Worker id')

    args=parser.parse_args()

    k = int(args.k) if args.k else read_k()
    h = int(args.h) if args.h else read_h()
    local_worker_id = int(args.local_worker_id)
    query = args.query if args.query else read_query() 
    w = 2 + (k*h)
    wname = socket.gethostname()
    extra_props_dir = read_extra_props_dir()
    
    main(k, h, query, w, wname, extra_props_dir, local_worker_id)

