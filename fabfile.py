from fabric.api import run, local

def host_type():
	run('uname -s')

def execute_sw_local():
	local('java -jar dist/*.jar Main new')
	
def send_system_to_node(jar, properties, machine):
	scp_jar = 'scp '+str(jar)+' ubuntu@'+str(machine)+'/home/ubuntu'
	scp_properties = 'scp '+str(properties)+' ubuntu@'+str(machine)+'/home/ubuntu'
	local(scp_jar)
	local(scp_properties)

def deploy_system(jar, properties, nodes):
	print "Assuming user ubuntu and copying to /home/ubuntu !!!"
	for node in nodes:
		print "Sending to "+str(node)
		send_system_to_node(jar, properties, nodes)
