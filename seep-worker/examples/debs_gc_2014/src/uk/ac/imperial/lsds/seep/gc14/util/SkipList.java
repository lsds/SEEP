package uk.ac.imperial.lsds.seep.gc14.util;

/*
 * Copyright (C) 2010 Zhenya Leonov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 * CHANGES TO THE ORIGINAL CODE:
 * 
 * This version of the original skiplist has been stripped down to the essentials
 * needed to add, remove, and search in a list of float values.
 * 
 */

import java.io.Serializable;
import java.util.Random;


public final class SkipList implements Serializable{

	// Made the list serializable
	private static final long serialVersionUID = 1L;
	
	private static final double P = .5;
	private static final int MAX_LEVEL = 128;
	private transient int size = 0;
	private transient int level = 1;
	private transient Random random = new Random();
	private transient Node head = new Node(-1, MAX_LEVEL);
	private transient Node[] update = new Node[MAX_LEVEL];
	private transient int[] index = new int[MAX_LEVEL];
	
	private transient Node medianPointer = null;
	
	public SkipList() {
		//Reinitialise transient structures
		size = 0;
		level = 1;
		random = new Random();
		head = new Node(-1, MAX_LEVEL);
		update = new Node[MAX_LEVEL];
		index = new int[MAX_LEVEL];
		medianPointer = null;
		
		for (int i = 0; i < MAX_LEVEL; i++) {
			head.next[i] = head;
			head.dist[i] = 1;
		}
		head.prev = head;
	}

	public boolean add(float e) {
		final int newLevel = randomLevel();		
		Node x = head;
		Node y = head;
		int i;
		int idx = 0;
		for (i = level - 1; i >= 0; i--) {
			while (x.next[i] != y
					&& x.next[i].element < e) {
				idx += x.dist[i];
				x = x.next[i];
			}
			y = x.next[i];
			update[i] = x;
			index[i] = idx;
		}
		if (newLevel > level) {
			for (i = level; i < newLevel; i++) {
				head.dist[i] = size + 1;
				update[i] = head;
			}
			level = newLevel;
		}
		x = new Node(e, newLevel);
		for (i = 0; i < level; i++) {
			if (i > newLevel - 1)
				update[i].dist[i]++;
			else {
				x.next[i] = update[i].next[i];
				update[i].next[i] = x;
				x.dist[i] = index[i] + update[i].dist[i] - idx;
				update[i].dist[i] = idx + 1 - index[i];

			}
		}
		x.prev = update[0];
		x.next().prev = x;
		size++;
		
		if (medianPointer == null)
			medianPointer = x;
		else {
			// inserted left or right of median?
			if (e <= medianPointer.element) {
				// if inserted left and even number of elements: move pointer left
				if (size % 2 == 0)
					medianPointer = medianPointer.prev;					
			}
			else {
				// if inserted right and uneven number of elements: move pointer right
				if (size % 2 == 1)
					medianPointer = medianPointer.next();					
			}
		}
		
		return true;
	}

	public float get(int index) {
		return search(index).element;
	}

	public boolean remove(float element) {
		int is = this.size();
		Node curr = head;
		for (int i = level - 1; i >= 0; i--) {
			while (curr.next[i] != head
					&& curr.next[i].element < element)
				curr = curr.next[i];
			update[i] = curr;
		}
		curr = curr.next();
//		System.out.println("curr: "+curr+" == "+head);
//		System.out.println("curr: "+curr.element+" != "+element);
		if (curr == head || Math.floor(curr.element) != Math.floor(element)){
			System.out.println("ERROR");
			System.exit(1);
			return false;
		}
		
		if (size == 1) 
			medianPointer = null;
		else {
			if (medianPointer == curr)
				medianPointer = curr.prev;
			else {
				// removed left or right of median?
				if (curr.element <= medianPointer.element) {
					// if removed left and uneven number of elements: move pointer right
					if (size % 2 == 1)
						medianPointer = medianPointer.next();					
				}
				else {
					// if removed right and even number of elements: move pointer left
					if (size % 2 == 1)
						medianPointer = medianPointer.prev;					
				}
			}
		}
		
		delete(curr, update);
		curr = null;
//		System.gc();
		int es = this.size();
		if (es-is == 0)
			System.out.println("ERROR");
		return true;
	}

	public float getMedian() {
		if (size < 1)
			return 0f;

		float median = ((size % 2) == 0) ? 
				(medianPointer.element + medianPointer.next().element)/2f :
				medianPointer.element;

		return median;
		
	}

	public int size() {
		return size;
	}

	private static class Node {
		private float element;
		private Node prev;
		private final Node[] next;
		private final int[] dist;

		private Node(final float element, final int size) {
			this.element = element;
			next = new Node[size];
			dist = new int[size];
		}
		
		private Node next(){
			return next[0];
		}
	}

	private int randomLevel() {
		int randomLevel = 1;
		while (randomLevel < MAX_LEVEL - 1 && random.nextDouble() < P)
			randomLevel++;
		return randomLevel;
	}

	private void delete(final Node node, final Node[] update) {
		for (int i = 0; i < level; i++)
			if (update[i].next[i] == node) {
				update[i].next[i] = node.next[i];
				update[i].dist[i] += node.dist[i] - 1;
			} else
				update[i].dist[i]--;
		node.next().prev = node.prev;
		while (head.next[level - 1] == head && level > 1)
			level--;
		size--;
	}

	private Node search(final int index) {
		Node curr = head;
		int idx = -1;
		for (int i = level - 1; i >= 0; i--)
			while (idx + curr.dist[i] <= index) {
				idx += curr.dist[i];
				curr = curr.next[i];
			}
		return curr;
	}
}