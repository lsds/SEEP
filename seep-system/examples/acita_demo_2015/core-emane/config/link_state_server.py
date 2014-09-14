#!/usr/bin/python

import sys, re, threading, argparse, socket

def main(num_nodes, host, port):

	link_states = LinkState(num_nodes)

	#start_reader(link_states)
	t = threading.Thread(target=start_server, args=(host,port,link_states,))
	t.setDaemon(True)
	t.start()

	start_tailer(link_states)

	#start_server(link_states)
def start_tailer(link_states):

	while True:
		line = sys.stdin.readline()
		if not line:
			raise Exception("Link state server tail failed.")

		link_states.handleUpdate(line)
		print 'Link states: %s\n'%(str(link_states))

def start_server(host, port, link_states):

	try:
		server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
		server_socket.bind((host, port))
		server_socket.listen(10)
		while True:
			conn, addr = server_socket.accept()
			#print 'Received connection from %s'%str(addr)
			t = threading.Thread(target=start_worker, args=(conn, addr, link_states,))
			t.setDaemon(True)
			t.start()
	finally: 
		server_socket.close()

def start_worker(conn, addr, link_states):
	try:
		msg = '%s\n'%(str(link_states))
		utf8_msg = unicode(msg, 'utf-8')
		conn.send(utf8_msg)
	finally:
		conn.close()

class LinkState:

	def __init__(self, nodes):
		self.num_nodes = nodes
		self.link_states = self._init_nodes_map()
		self.next_updates = self._init_nodes_map()
		self.link_state_lock = threading.Lock()

		self.start_update_regex = re.compile(r'n(\d+) start')
		self.update_regex = re.compile(r'n(\d+) n(\d+) (\d+)$')
		self.end_update_regex = re.compile(r'n(\d+) end')


	def _init_nodes_map(self):
		result = {}
		for i in range(1, self.num_nodes+1):
			result[i] = {}
			for j in range(1, self.num_nodes+1):
				if i != j:
					result[i][j] = None
		return result
		
	def handleUpdate(self, update):
		match = re.search(self.start_update_regex, update)
		if match:
			node = int(match.group(1))
			self._assert_next_updates_reset(node)
			return

		match = re.search(self.update_regex, update)
		if match:
			node = int(match.group(1))
			nbr = int(match.group(2))
			hops = int(match.group(3))
			if not hops == 1: raise Exception("Unexpected number of hops %d -> %d = %d"%(node, nbr, hops))
			self.next_updates[node][nbr] = hops
			return

		match = re.search(self.end_update_regex, update)
		if match:
			node = int(match.group(1))
			self._update_link_states(node)
			self._reset_next_updates(node)
			return
		
		raise Exception("Invalid update: %s"%update)

	def __str__(self):
		copy_link_states = {}
		with self.link_state_lock:
			for i in range(1, self.num_nodes+1):
				copy_link_states[i] = {}
				for j in range(1, self.num_nodes+1):
					if i != j:
						copy_link_states[i][j] = self.link_states[i][j]

		# No real need for all the above at the moment, but might
		# want to change this later.
		return str(copy_link_states)

	def _update_link_states(self, node):
		with self.link_state_lock:
			for nbr in self.next_updates[node].keys():
				self.link_states[node][nbr] = self.next_updates[node][nbr]
				#Assume symmetric links for now
				self.link_states[nbr][node] = self.next_updates[node][nbr]
			

	def _reset_next_updates(self, node):
		for nbr in self.next_updates[node].keys():
			self.next_updates[node][nbr] = None
		
	def _assert_next_updates_reset(self, node):
		for nbr in self.next_updates[node].keys():
			if self.next_updates[node][nbr]:
				raise Exception("Didn't reset node %d neighbours properly: %s"%(node, str(self.next_updates[node][nbr])))
		
if __name__=="__main__":
	parser = argparse.ArgumentParser(description='Monitor and distribute OLSR link state information to workers.')		
	parser.add_argument('--port', dest='port', help='server socket port')
	parser.add_argument('--addr', dest='addr', default='', help='server socket address')
	parser.add_argument('--num_nodes', dest='num_nodes', default='6', help='number of nodes [6]')
	args = parser.parse_args()
	
	main(int(args.num_nodes), args.addr, int(args.port))
