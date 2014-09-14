#!/usr/bin/python

import sys, re, threading, argparse, socket

def main(num_nodes, host, port):

	t = threading.Thread(target=start_server, args=(host,port,num_nodes,))
	t.setDaemon(True)
	t.start()

def start_server(host, port, num_nodes):

	app_link_states = AppLinks(num_nodes)
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

def start_worker(conn, addr, app_link_states):
	try:
		with conn.makefile() as reader:
			while True:
				line = reader.readline().decode('utf8')
				if not line: return			
				app_link_states.handleUpdate(line)	
	finally:
		conn.close()

class AppLinkState:

	def __init__(self, nodes):
		self.num_nodes = nodes
		self.emulator_ips = self.init_emulator_ips()
		self.downstreams = {}
		self.downstreams_lock = threading.Lock()
		self.update_regex = re.compile(r'(.*):(\d+),(.*):(\d+)|(.*):(\d+),None')
		self.has_downstream_regex = re.compile(r'(.*):(\d+),(.*):(\d+)')
		self.no_downstream_regex = re.compile(r'(.*):(\d+),None')
		self.emulator_ip_regex = re.compile(r'192.168.20([1-6]).101')

	def _init_emulator_ips(self):
		result = {}
		for in range(1, self.num_nodes+1):
			ip = "192.168.20%d.101"%i
			result[i] = ip 

	def handleUpdate(update)	
		match = re.search(self.update_regex, update)
		if not match: raise Exception("Invalid link painter update: %s"%update)
		match = re.search(self.has_downstream_regex, update)
		if match:
			node_addr = match.group(1)
			node_port = match.group(2)
			nbr_addr = match.group(3)
			nbr_port = match.group(4)
			if re.search(emulator_ip_regex, node_addr) and re.search(emulator_ip_regex, nbr_addr):
				self._set_downstream(node_addr, node_port, nbr_addr, nbr_port)
			else:
				raise Exception("Unexpected node or nbr address: %s,%s"%(node_addr, nbr_addr))
		else:
			match = re.search(self.no_downstream_regex, update)
			if not match: raise Exception("Logic error, should be no downstream update: %s"%update)	
			node_addr = match.group(1)
			node_port = match.group(2)
			if re.search(emulator_ip_regex, node_addr): 
				self._unset_downstream(node_addr, node_port)
			else:
				raise Exception("Unexpected node address: %s"%node_addr)

	def _set_downstream(node_addr, node_port, nbr_addr, nbr_port):
		with self.downstreams_lock:
			self.downstreams[(node_addr,node_port)] = (nbr_addr, nbr_port)
		
	def _unset_downstream(node_addr, node_port, nbr_addr, nbr_port):
		with self.downstreams_lock:
			self.downstreams[(node_addr,node_port)] = None 

			
	def compute_downstream_ids(node)
		emulator_ip = self.emulator_ips[node]
		downstream_ids = self._init_downstream_ids()
		with self.downstreams_lock:
			for (addr, port) in self.downstreams.keys():
				if addr == emulator_ip:
					if self.downstreams[(addr,port)]:
						(downstream_addr, downstream_port) = self.downstreams[(addr,port)]
						downstream_id = re.search(self.emulator_ip_regex, self.downstream_addr)
						downstream_ids[node] = True
		return downstream_ids
		
	def _init_downstream_ids(node):
		result = {}
		for i in range(1, self.num_nodes+1):
			if i != node:
				result[i] = False
		return result

if __name__=="__main__":
	parser = argparse.ArgumentParser(description='Monitor and distribute OLSR link state information to workers.')		
	parser.add_argument('--addr', dest='addr', help='server socket address')
	parser.add_argument('--port', dest='port', help='server socket port')
	parser.add_argument('--num_nodes', dest='num_nodes', default='6', help='number of nodes [6]')
	args = parser.parse_args()
	main(int(args.num_nodes), args.addr, int(args.port))
