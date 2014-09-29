#!/usr/bin/python

import sys, re, threading, argparse, socket, time, os

images_dir = '/home/acita14demo/dev/seep-ita/seep-system/examples/acita_demo_2015/core-emane/config'

def main(num_nodes, host, port):

	app_link_states = AppLinkState(num_nodes)
	gui_t = threading.Thread(target=start_gui_painter, args=(num_nodes, app_link_states,))
	gui_t.setDaemon(True)
	gui_t.start()

	start_server(host, port, app_link_states)

def start_server(host, port, app_link_states):

	try:
		server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
		server_socket.bind((host, port))
		server_socket.listen(10)
		while True:
			conn, addr = server_socket.accept()
			#print 'Received connection from %s'%str(addr)
			t = threading.Thread(target=start_worker, args=(conn, addr, app_link_states,))
			t.setDaemon(True)
			t.start()
	finally: 
		server_socket.close()

	print 'Link painter exiting.'

def start_gui_painter(num_nodes, app_link_states):
	reset_node_icons(num_nodes)


	prev_hosting_nodes = set()	
	prev_downstreams=init_prev_downstreams(num_nodes)
	while True:
		hosting_nodes = app_link_states.get_hosts()
		if hosting_nodes != prev_hosting_nodes:
			update_icons(num_nodes, hosting_nodes)
		for i in range(1, num_nodes+1):
			node_downstreams = app_link_states.compute_downstream_ids(i)
			if node_downstreams != prev_downstreams[i]:
				print 'Node %d downstreams changed: %s'%(i, str(node_downstreams))
				update_node_app_links(i, node_downstreams)
				prev_downstreams[i] = node_downstreams
		#update_app_links(app_link_states)
		prev_hosting_nodes=hosting_nodes
		time.sleep(1)

def init_prev_downstreams(num_nodes):
	prev_downstreams = {}
	for i in range(1, num_nodes+1):
		prev_downstreams[i] = {}
	return prev_downstreams
	
def reset_node_icons(num_nodes):
	for i in range(1, num_nodes+1):
		show_initial_icon(num_nodes, i)
		
def update_icons(num_nodes, hosting_nodes):
	for hosting_node in hosting_nodes:
		if hosting_node == 1:
			show_hosting_source(num_nodes, hosting_node)
		else:
			show_hosting_op(num_nodes, hosting_node)

def show_initial_icon(num_nodes, node):
	set_node_icon(num_nodes, node, "%s/%s"%(images_dir, "host.gif"))

def show_hosting_source(num_nodes, node):
	set_node_icon(num_nodes, node, "%s/%s"%(images_dir, "host_op_blue.png"))

def show_hosting_op(num_nodes, node):
	set_node_icon(num_nodes, node, "%s/%s"%(images_dir, "host_op_green.png"))
	
def set_node_icon(num_nodes, node, img_path):
	cmd = "coresendmsg node number=%d icon=%s"%(node+num_nodes, img_path)
	os.system(cmd)
		
def update_node_app_links(node, node_downstreams):
	for downstream_id in node_downstreams.keys():
		if node != downstream_id:
			if node_downstreams[downstream_id]:
				show_app_link(node, downstream_id)
			else:
				hide_app_link(node, downstream_id)

def show_app_link(node, downstream_id):
	print 'Showing link from %d to %d'%(node, downstream_id)
	#set_app_link(node, downstream_id, True)

def hide_app_link(node, downstream_id):
	print 'Hiding link from %d to %d'%(node, downstream_id)
	#set_app_link(node, downstream_id, False)

def set_app_link(node, downstream_id, show):
	node_core_id = core_placement_id(node) 
	downstream_core_id = core_placement_id(downstream_id) 
	ifip_node = "127.%d.0.%d"%(node,node)
	ifip_dest = "127.%d.0.%d"%(node, downstream_id)
	flag = 'add' if show else 'del'
	cmd = "coresendmsg link	flags=%s n1number=%d n2number=%d if1ip4='%s' if1ip4mask=24 if2ip4='%s' if2ip4mask=24"%(flag, node_core_id, downstream_core_id, ifip_node, ifip_dest)
	print 'Sending command to core: %s'%cmd
	os.system(cmd)

def core_placement_id(node):
	return node + 7

def start_worker(conn, addr, app_link_states):
	print 'Starting painter worker for ip: %s'%str(addr)
	try:
		reader = conn.makefile()
		while True:
			line = reader.readline().decode('utf8')
			print 'Painter worker %s received: %s'%(str(addr), line)
			if not line: return			
			app_link_states.handleUpdate(line)	
	finally:
		reader.close()
		conn.close()

class AppLinkState:

	def __init__(self, nodes):
		self.num_nodes = nodes
		self.emulator_ips = self._init_emulator_ips()
		self.downstreams = {}
		self.downstreams_lock = threading.Lock()
		self.update_regex = re.compile(r'(.*):(\d+),(.*):(\d+)|(.*):(\d+),None')
		self.has_downstream_regex = re.compile(r'(.*):(\d+),(.*):(\d+)')
		self.no_downstream_regex = re.compile(r'(.*):(\d+),None')
		self.emulator_ip_regex = re.compile(r'192.168.20([1-6]).101')
		if self.num_nodes > 6: raise Exception("Fix emulator_ip_regex")
		self.hosting_nodes = set()
		self.hosting_nodes_lock = threading.Lock()

	def _init_emulator_ips(self):
		result = {}
		for i in range(1, self.num_nodes+1):
			ip = "192.168.20%d.101"%i
			result[i] = ip 
		return result

	def get_hosts(self):
		with self.hosting_nodes_lock:
			hosts_copy = frozenset(self.hosting_nodes)
		return hosts_copy

	def handleUpdate(self, update):
		match = re.search(self.update_regex, update)
		if not match: raise Exception("Invalid link painter update: %s"%update)
		match = re.search(self.has_downstream_regex, update)
		if match:
			node_addr = match.group(1)
			node_port = match.group(2)
			nbr_addr = match.group(3)
			nbr_port = match.group(4)
			if re.search(self.emulator_ip_regex, node_addr) and re.search(self.emulator_ip_regex, nbr_addr):
				self._set_downstream(node_addr, node_port, nbr_addr, nbr_port)
				self._add_hosting_node(node_addr)
			else:
				raise Exception("Unexpected node or nbr address: %s,%s"%(node_addr, nbr_addr))
		else:
			match = re.search(self.no_downstream_regex, update)
			if not match: raise Exception("Logic error, should be no downstream update: %s"%update)	
			node_addr = match.group(1)
			node_port = match.group(2)
			if re.search(self.emulator_ip_regex, node_addr): 
				self._unset_downstream(node_addr, node_port)
				self._add_hosting_node(node_addr)
			else:
				raise Exception("Unexpected node address: %s"%node_addr)

	def _set_downstream(self, node_addr, node_port, nbr_addr, nbr_port):
		with self.downstreams_lock:
			self.downstreams[(node_addr,node_port)] = (nbr_addr, nbr_port)
		
	def _unset_downstream(self, node_addr, node_port, nbr_addr, nbr_port):
		with self.downstreams_lock:
			self.downstreams[(node_addr,node_port)] = None 

			
	def compute_downstream_ids(self, node):
		emulator_ip = self.emulator_ips[node]
		downstream_ids = self._init_downstream_ids(node)
		with self.downstreams_lock:
			for (addr, port) in self.downstreams.keys():
				if addr == emulator_ip:
					if self.downstreams[(addr,port)]:
						(downstream_addr, downstream_port) = self.downstreams[(addr,port)]
						downstream_id = self._get_core_node_id(downstream_addr)
						downstream_ids[downstream_id] = True
		return downstream_ids
		
	def _init_downstream_ids(self, node):
		result = {}
		for i in range(1, self.num_nodes+1):
			if i != node:
				result[i] = False
		return result

	def _add_hosting_node(self, node_addr):
		node_id = self._get_core_node_id(node_addr)
		with self.hosting_nodes_lock:
			self.hosting_nodes.add(node_id)

	def _get_core_node_id(self, emulator_ip):
		return int(re.search(self.emulator_ip_regex, emulator_ip).group(1))

if __name__=="__main__":
	parser = argparse.ArgumentParser(description='Monitor and distribute OLSR link state information to workers.')		
	parser.add_argument('--addr', dest='addr', help='server socket address')
	parser.add_argument('--port', dest='port', help='server socket port')
	parser.add_argument('--num_nodes', dest='num_nodes', default='6', help='number of nodes [6]')
	args = parser.parse_args()
	main(int(args.num_nodes), args.addr, int(args.port))
