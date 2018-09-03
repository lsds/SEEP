#!/usr/bin/env python

import time
from fabric.api import *

env.use_ssh_config = True
#env.user = 'pi'
env.user = 'dokeeffe'

adhoc_hosts = [
	#"pi@191.168.181.107", # docpi2 # v3Pi2
	#"pi@191.168.181.114", # docpiv1 # v3PiMy
	"pi@191.168.181.110", # docpi4 # v3Pi4
	#"pi@191.168.181.106", # docpi1 # v3Pi1
    #"pi@191.168.181.101:23", # mypi # Src
	#"pi@191.168.181.108", # docpi3 # v3Pi3
	#"191.168.181.116", # csg-laptop 
	#"191.168.181.115", # bowfell 
]

ap_hosts = [
	#"192.168.0.107", # docpi2
	#"192.168.0.114", # docpiv1
	#"192.168.0.106", # docpi1
	#"192.168.0.101", # mypi ap
	#"192.168.0.111", # mypi wired
	#"192.168.0.108", # docpi3
	#"192.168.0.110", # docpi4
]

#env.hosts = ap_hosts
env.hosts = adhoc_hosts 
throttle_host = 'pi@191.168.181.106'  # docpi1 
local_repo_root = "/data/dev/seep-github"
repo_root = "/home/pi/dev/seep-ita"
demo_relative = "/seep-system/examples/acita_demo_2015"
local_demo_root = local_repo_root + demo_relative 
demo_root = repo_root + demo_relative 


@hosts('localhost')
def localtest():
    with lcd("~"):
        local('echo "Hello World from %s"'%env.host_string)

#@parallel
def test():
	with cd("~"):
		run('echo "Hello World from %s"'%env.host_string)
	local('echo "Hello World from %s"'%env.host_string)

def extraconf():
	with lcd(local_demo_root):
		with cd(demo_root):
			put('extraConfig.properties')	

@hosts('localhost')
def local_workers(numWorkers):
    with lcd(local_demo_root + "/tmp"):
        #run('pkill "java" || /bin/true')
        local('rm -f *worker*.log')
        local('rm -f /tmp/*net-util.txt')
        local('rm -f /tmp/*cpu-util.txt')
        put(local_demo_root + '/core-emane/vldb/config/olsrd-net-rates.sh', 'net-rates.sh')
        #time.sleep(3)
        print "Starting combined net/cpu monitor."
        local('(nohup %s/core-emane/vldb/config/combined-util.sh 2>&1 >/dev/null < /dev/null &) && /bin/true'%local_demo_root)
        for i in range(0, int(numWorkers)):
            print "Starting worker on port 350%d"%(i+1)
            local('(nohup java -classpath "../lib/*" uk.ac.imperial.lsds.seep.Main Worker 350%d >localhost.worker350%d.log 2>&1 < /dev/null &) && /bin/true'%(i+1, i+1))
			#time.sleep(2)
			

@hosts('localhost')
def workers(numWorkers):
    now = time.strftime("%s", time.gmtime())
    execute(set_time, now)
    execute(set_retries)
    execute(remote_workers, numWorkers)
    
def remote_workers(numWorkers):
    with lcd(local_demo_root):
        with cd(demo_root):
            put('extraConfig.properties', 'extraConfig.properties')
            put('core-emane/vldb/config/olsrd-net-rates.sh', 'tmp/net-rates.sh')
            #put('core-emane/vldb/config/combined-util.sh', 'tmp/combined-util.sh')

    with cd(demo_root + "/tmp"):
        #run('pkill "java" || /bin/true')
        run('rm -f *worker*.log')
        run('rm -f /tmp/*net-util.txt')
        run('rm -f /tmp/*cpu-util.txt')
        print "Starting combined net/cpu monitor."
        run('(nohup %s/core-emane/vldb/config/combined-util.sh &>/dev/null < /dev/null &) && /bin/true'%demo_root, pty=False)
        #time.sleep(10)
        for i in range(0, int(numWorkers)):
			print "Starting worker on port 350%d"%(i+1)
			logfile = "%s.worker350%d.log"%(env.host_string.replace("@",".at."), i+1)
			run('(nohup java -classpath "../lib/*" uk.ac.imperial.lsds.seep.Main Worker 350%d >%s 2>&1 < /dev/null &) && /bin/true'%(i+1, logfile), pty=False)
			#time.sleep(10)


def gather(expdir):
	run('pkill "java" || /bin/true')
	with cd(demo_root + "/tmp"):
		#run('tar -czf workers.tar.gz *worker*.log')
		with lcd(local_demo_root + "/core-emane/log/" + expdir):
			#get('*worker*.log')
			#get('workers.tar.gz')
			port = env.port if env.port else '22'
			local('scp -C -P %s %s:%s/*worker*.log .'%(port, env.host, demo_root+"/tmp"))
			local('scp -C -P %s %s:/tmp/*net-util.txt .'%(port, env.host))
			local('scp -C -P %s %s:/tmp/*cpu-util.txt .'%(port, env.host))

@hosts('localhost')
def mkexpdir():
    with lcd(local_demo_root + "/core-emane/log"):
        local('pkill "java" || /bin/true')
        expdir = local('date +%H-%M-%S-%a%d%m%y', capture=True)
        local('mkdir -p %s'%expdir)
        local('cp %s/*.log %s'%(local_demo_root+'/tmp', expdir))
        local('mkdir -p %s/net-util'%expdir)
        local('mkdir -p %s/cpu-util'%expdir)
        local('cp /tmp/*net-util.txt %s/net-util'%(expdir))
        local('cp /tmp/*cpu-util.txt %s/cpu-util'%(expdir))
        execute(gather, expdir)
        #local('for f in $(find log/%s -name "*.tar.gz") ; do cd $(dirname $f) ; tar -xzvf workers.tar.gz ; cd - ; done'%expdir)
        local('./record_net_rates.py --expDir log/%s'%expdir)
        local('./record_cpu_rates.py --expDir log/%s'%expdir)
        local('./gen_core_results.py --expDir log/%s'%expdir)
        local('./plot_pi_results.py --timeStrs %s'%expdir)

def to_ap():
	sudo('cp /etc/network/interfaces.orig /etc/network/interfaces')

def to_adhoc():
	sudo('cp /etc/network/interfaces.adhoc /etc/network/interfaces')


def check_workers():
	run('ps aux | grep java')

def reboot():
	with settings(warn_only=True):
		sudo('reboot')

@hosts('localhost')
def clean():
    local('pkill "java" || /bin/true')
    with warn_only():
        local('pkill -f "combined-util.sh" || /bin/true')
    execute(remote_clean)

def remote_clean():
    run('pkill "java" || /bin/true')
    with warn_only():
        run('pkill -f "combined-util.sh" || /bin/true')

def ls(dir):
	run('ls %s'%dir)

def cat(file):
	run('cat %s'%file)

def git_pull():
	with cd(repo_root):
		run('git stash')
		run('git pull --rebase')
		#run('git stash pop')

@hosts(throttle_host)
def throttle(rate):
    with cd(demo_root):
        sudo("core-emane/vldb/config/throttle-loss.sh start %s"%rate)
        #run("./core-emane/vldb/config/throttle.sh show")

@hosts(throttle_host)
def unthrottle():
    with cd(demo_root):
        sudo("core-emane/vldb/config/throttle-loss.sh stop")

def test_throttle(do_probe=False):
    with cd(demo_root):
        if do_probe: sudo("modprobe ifb")
        sudo("core-emane/vldb/config/throttle-loss.sh show")

@parallel
def rebuild():
	with cd(repo_root):
		run('./frontier-bld.sh pi')

def git_status():
	with cd(repo_root):
		run('git status')

def git_diff():
	with cd(repo_root):
		run('git diff')

# Note: Make sure there are no uncommitted/conflicting changes on remotes first!
def git_rebase(stash=False):
    with cd(repo_root):
        if stash: run('git stash')

        #sudo('ssh -f -D 443 pi@191.168.181.101 -p 23 sleep 10')
        sudo('ssh -f -D 443 dokeeffe@191.168.181.115 sleep 10')
        #run('git -c http.proxy=socks5h://localhost:443 fetch --dry-run')
        run('git -c http.proxy=socks5h://localhost:443 pull --rebase')
        if stash: run('git stash pop')

def port_range():
	run('cat /proc/sys/net/ipv4/ip_local_port_range')

@hosts('localhost')
def sync_time():
    #now = time.strftime("%H:%M:%S", time.gmtime())
    now = time.strftime("%s", time.gmtime())
    execute(set_time, now)

#@parallel
def set_time(now):
    #sudo('date +%%T -u -s %s'%now)
    sudo('date -s @%s'%now)

def get_time():
    run('date +%s')

def get_date():
    run('date')

def get_retries():
    run('cat /proc/sys/net/ipv4/tcp_retries2')

def grep_logs(pattern):
	with cd(demo_root + "/tmp"):
		run('grep -PIn %s *.log ; true'%pattern)

def copy_file(f, dest_dir):
    put(f, dest_dir)

def extract_file(f, dest_dir):
    #with cd(dest_dir):
    with cd(demo_root + "/resources"):
		run('tar -xzf %s'%f)

#def apt_install(pkg):
#    sudo('apt-get update')
#    sudo('apt-get install -y %s'%pkg)

# Original value was 15
#@parallel
def set_retries(retries=6):
    sudo('sysctl -w net.ipv4.tcp_retries2=%d'%(int(retries)))
