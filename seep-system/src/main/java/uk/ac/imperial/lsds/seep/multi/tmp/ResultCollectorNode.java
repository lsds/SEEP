package uk.ac.imperial.lsds.seep.multi.tmp;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import uk.ac.imperial.lsds.seep.multi.PartialWindowResults;

public class ResultCollectorNode {
	
	private static enum Status { IDLE, FIRST, SECOND, RESULT };
	
	PartialWindowResults left, center, right;
	Lock lock;
	
	Status status = Status.IDLE;
	
	ResultCollectorNode  leftParent = null;
	ResultCollectorNode rightParent = null;
	
	ResultCollectorNode  leftChild = null;
	ResultCollectorNode rightChild = null;
	
	PartialWindowResults _1st = null;
	PartialWindowResults _2nd = null;
	
	public ResultCollectorNode () {
		
		this.left   = null;
		this.center = null;
		this.right  = null;
		
		this.leftParent = this.rightParent = null;
		
		this.leftChild = this.rightChild = null;
		
		lock = new ReentrantLock();
	}
	
	public void init (PartialWindowResults left, PartialWindowResults center, PartialWindowResults right) {
		
		this.left   =   left;
		this.center = center;
		this.right  =  right;
	}
	
	public void lock () {
		lock.lock();
	}
	
	public void unlock () {
		lock.unlock();
	}
	
	public void setRightParent(ResultCollectorNode rightParent) {
		
		this.rightParent = rightParent;
		
		rightParent.setLeftChild(this);
	}
	
	public void setLeftParent(ResultCollectorNode leftParent) {
		
		this.leftParent = leftParent;
		
		leftParent.setRightChild(this);
	}
	
	public void setRightChild (ResultCollectorNode rightChild) {
		
		this.rightChild = rightChild;
	}
	
	public void setLeftChild (ResultCollectorNode leftChild) {
		
		this.leftChild = leftChild;
	}
	
	public boolean isLeaf () {
		
		return (this.rightChild == null && this.leftChild == null);
	}
	
	public void insert (PartialWindowResults results) {
		
		lock.lock();
		if (status == Status.IDLE) {
			status = Status.FIRST;
			_1st = results;
			lock.unlock();
		} else
		if (status == Status.FIRST) {
			status = Status.SECOND;
			_2nd = results;
			lock.unlock();
		} else {
			lock.unlock();
			System.err.println("error: invalid node status");
			System.exit(1);
		}
	}
}
