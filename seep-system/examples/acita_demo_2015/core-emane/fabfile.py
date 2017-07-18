#!/usr/bin/env python

from fabric.api import *


adhoc_hosts = [
	"191.168.181.107", # docpi2
	#"191.168.181.114", # docpiv1
	#"191.168.181.106", # docpi1
	#"191.168.181.101", # mypi
]

ap_hosts = [
	"192.168.0.107", # docpi2
	#"192.168.0.114", # docpiv1
	#"192.168.0.106", # docpi1
	#"192.168.0.101", # mypi
]

env.hosts = ap_hosts

repo_root = "/home/pi/dev/seep-ita"
demo_root = repo_root + "/seep-system/examples/acita_demo_2015"

# mypi.port = 23

env.user = "pi"

#env.password = "notrecommended"

def test():
	with cd("~"):
		run('echo "Hello World."')

def extraconf():
	with lcd(demo_root):
		with cd(demo_root):
			put('extraConfig.properties', "extraConfig.properties")	
			run("cat extraConfig.properties")

def workers(numWorkers):
	with cd(demo_root + "/tmp"):
		run('pkill "java" || /bin/true')
		run('rm -f worker*.log')
		for i in range(0, int(numWorkers)):
			print "Starting worker on port 350%d"%(i+1)
			run('(nohup java -classpath "../lib/*" uk.ac.imperial.lsds.seep.Main Worker 350%d >worker350%d.log 2>&1 < /dev/null &) && /bin/true'%(i+1, i+1))

def to_ap():
	sudo('cp /etc/network/interfaces.orig /etc/network/interfaces')

def to_adhoc():
	sudo('cp /etc/network/interfaces.adhoc /etc/network/interfaces')

def reboot():
	sudo('reboot')

def clean():
	run('pkill "java"')

def git_pull():
	with cd(repo_root):
		run('git stash')
		run('git pull --rebase')
		#run('git stash pop')

@parallel
def rebuild():
	with cd(repo_root):
		run(./meander-bld.sh)

def git_status():
	with cd(repo_root):
		run('git status')
