#!/usr/bin/python

import sys, re, threading

def main():

	link_states = LinkState(6)

	start_tailer(link_states)

	#start_server(link_states)

class LinkState

	def __init__(self, nodes):
		self.num_nodes = nodes
		self.link_states = self._init_nodes_map()
		self.next_updates = self._init_nodes_map()
		self.link_state_lock = threading.Lock()

		self.start_update_regex = re.compile(r'n(/d+) start')
		self.update_reges = re.compile(r'n(/d+) n(/d+) (/d+)$')
		self.end_update_regex = re.compile(r'n(/d+) start')


	def _init_nodes_map(self):
		result = {}
		for i in range(1, self.num_nodes+1):
			result[i] = {}
			for j in range(1, self.num_nodes+1):
				if i != j:
					result[i][j] = None
		
	def handleUpdate(self, update):
		match = re.search(self.start_update_regex, update)
		if match:
			node = int(match.group(1))
			self.assert_next_updates_reset(node)
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
		

	def str(self):
		copy_link_states = {}
		with link_state_lock:
			for i in range(1, self.num_nodes+1):
				for j in range(1, self.num_nodes+1):
					if i != j:
						copy_link_states[i][j] = self.link_states[i][j]

		# No real need for all the above at the moment, but might
		# want to change this later.
		return str(copy_link_states)

	def _update_link_states(self, node):
		with link_state_lock:
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
	main()
