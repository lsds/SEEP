package uk.ac.imperial.lsds.seep.multi;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.LockSupport;

public class ResultAggregator {
	
	/*
	 * A ResultAggregatorNode encapsulates the
	 * complete and partial state (windows) of
	 * a batch - the result of a query task.
	 * 
	 * Each such node is part of a linked list
	 * that links all nodes together from left
	 * to right.
	 * 
	 * Nodes are statically linked.
	 * 
	 * Nodes are removed from the list only by 
	 * marking them as "complete", which means
	 * that all windows contained in this task
	 * are complete.
	 */
	private static class ResultAggregatorNode {
		
		int index;
		
		ResultAggregatorNode next;
		
		WindowResult closing, pending, opening, complete;
		
		public ResultAggregatorNode (int index) {
			
			this.index = index;
			
			/* Initialize windows */
			this.closing  = null;
			this.opening  = null;
			this.pending  = null;
			
			this.complete = null;
			
			next = null;
		}
		
		public void init (
			WindowResult opening,
			WindowResult closing, 
			WindowResult pending,
			WindowResult complete
			) {
			
			/* Initialize windows */
			this.closing  = opening;
			this.opening  = closing;
			this.pending  = pending;
			
			this.complete = complete;
		}
		
		public void connectTo (ResultAggregatorNode node) {
			this.next = node;
		}
	}
	
	int size;
	AtomicIntegerArray slots;
	ResultAggregatorNode [] nodes;
	
	/* Sentinels */
	ResultAggregatorNode nextToAggregate;
	ResultAggregatorNode nextToForward;
	
	public ResultAggregator (int size) {
		
		this.size = size;
		
		slots = new AtomicIntegerArray(size);
		nodes = new ResultAggregatorNode [size];
		for (int i = 0, j = i - 1; i < size; i++, j++) {
			slots.set(i, -1);
			nodes[i] = new ResultAggregatorNode (i);
			if (j < 0)
				nodes[size - 1].connectTo(nodes[0]);
			else
				nodes[j].connectTo(nodes[i]);
		}
		nextToAggregate = nodes[0];
		nextToForward   = nodes[0];
	}
	
	public void add (
			int           taskid,
			WindowResult opening,
			WindowResult closing, 
			WindowResult pending,
			WindowResult complete
			) {
		
		if (taskid < 0) { /* Invalid task id */
			return ;
		}
		int idx = ((taskid - 1) % size);
		while (! slots.compareAndSet(idx, -1, 0)) {
			
			System.err.println(String.format("warning: result aggregator blocked at %s q %d t %4d idx %4d", 
				Thread.currentThread(), taskid, idx));
			LockSupport.parkNanos(1L);
		}
		
		/* Slot `idx` has been reserved for this task id */
		ResultAggregatorNode node = nodes[idx];
		node.init (opening, closing, pending, complete);
		
		/* Aggregate, starting from `nextToAggregate` */
		
		
		/* Forward and free */
	}
	
	public IQueryBuffer removeMin() {
		ResultCollectorTreeNode node = root;
		while(! node.isLeaf()) {
			if (node.counter.getAndDecrement() > 0) {
				node = node.left;
			} else {
				node = node.right;
			}
		}
		return node.centerBuffer;
	}
}
