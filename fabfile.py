from fabric.api import run, local

def host_type():
	run('uname -s')

def execute_sw_local():
	local('java -jar dist/*.jar Main new')
	
