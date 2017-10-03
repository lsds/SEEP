#!/usr/bin/python

import subprocess,os,time,re,argparse

eg_dir = os.path.dirname(os.path.realpath(__file__))
seep_jar = 'seep-system-0.0.1-SNAPSHOT.jar'
query_jar = 'acita_demo_2015.jar'
query_base = 'Base'
data_dir = '%s/log'%eg_dir

def main(k, h, query, extra_props_dir, plot_time_str, run_master):
    sim_env = os.environ.copy()
    session_params = read_session_params()
   
    master_postdelay = int(session_params.get('master_postdelay', '5'))
    
    if plot_time_str:
        time_str = plot_time_str
    else:
        time_str = time.strftime('%H-%M-%a%d%m%y')

        try:
            if run_master:

                print 'Starting master'
                master_logfilename = mlog(k, query, time_str) 
                master = start_master(k,h, query, extra_props_dir, master_logfilename, sim_env)

                time.sleep(master_postdelay)

            print 'Waiting for workers.'
            time.sleep(10)

            if run_master:
                deploy_query(master)

                print 'Waiting for deployed complete signal.'
                wait_for_deploy(master)
                
                if not master.poll() is None:
                    return

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

def start_master(k, h, query, extra_props_dir, logfilename, sim_env):
    extra_java_params=map(lambda line: '-D'+line, read_extra_params())
    master_processors = ",".join(map(str, range(3,64,4)))
    #master_processors = "25-31"
    #taskset_params = ['taskset', '-ac', master_processors]
    taskset_params = []

    with open(data_dir+'/'+logfilename, 'w') as log:
        args = taskset_params + ['java','-Dplatform.dependencies=true','-DuseCoreAddr=true','-DreplicationFactor=%d'%k,'-DchainLength=%d'%h,
                '-DqueryType=%s'%query, '-DextraProps=%s'%extra_props_dir] + extra_java_params + ['-jar', '%s/../lib/%s'%(eg_dir, seep_jar), 'Master', '%s/dist/%s'%(eg_dir,query_jar), query_base]
        p = subprocess.Popen(args, stdin=subprocess.PIPE, stdout=log, stderr=subprocess.STDOUT, env=sim_env)
        return p

def stop_master(p):
    p.stdin.write('6\n')
    p.stdin.close()
    p.terminate()
    print 'Terminated master.'

def mlog(k, query, time_str):
    return 'master-k%d-%s-%s.log'%(k,query,time_str)

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

def wait_for_deploy(master):
    while not os.path.exists("deployComplete.txt"):
        if not master.poll() is None:
            return
        time.sleep(1)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Run simulations.')

    parser.add_argument('--k', dest='k', help='Number of replicas for each intermediate operator')
    parser.add_argument('--h', dest='h', help='Number of logical operators (chain)')
    parser.add_argument('--query', dest='query', help='Query type: chain, join')
    parser.add_argument('--plotOnly', dest='plot_time_str', default=None, help='time_str of run to plot (hh-mm-DDDddmmyy)[None]')
    parser.add_argument('--nomaster', dest='no_master', default=False, help='Disable master (False)')
    
    args=parser.parse_args()

    k = int(args.k) if args.k else read_k()
    h = int(args.h) if args.h else read_h()
    query = args.query if args.query else read_query() 
    extra_props_dir = read_extra_props_dir()
    
    main(k, h, query, extra_props_dir, args.plot_time_str, not bool(args.no_master))

