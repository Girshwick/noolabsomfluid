package org.NooLab.somfluid.env.communication;

import org.NooLab.somfluid.components.VirtualLattice;



public class LatticeFutureVisor implements Runnable, LatticeFutureVisorIntf{  

	VirtualLattice virtualLattice;
	String guidF;
	
	Thread lfvThrd; 
	boolean isWaiting ;
	
	// ------------------------------------------------------------------------
	public LatticeFutureVisor(VirtualLattice somlattice, int taskId) {

		
		virtualLattice = somlattice;
		lfvThrd = new Thread (this, "lfvThrd");
		
		open( taskId);
	}

	public void waitFor(){
		
		isWaiting = true;
		
		lfvThrd.start();
		
		while (isWaiting ){
			virtualLattice.getOut().delay(1);
		}
	}

	public void open( int taskId) {
		// this guid is issued by the lattice, which knows about it
		// the lattice will provide this guid to all nodes, each of which maintain a FiFo list of such GUIDs
		// once they have finished, the send a signal to the lattice, which counts down -1 for each node,
		// if the count down arrives at 0, it will release the event to here
		guidF = virtualLattice.openLatticeFuture( this, taskId ); // no params: for all nodes
		
		virtualLattice.getOut().delay(100);
	}

	@Override
	public void run() {
		 
		
		
		// the waiting thread will be finished by an event-like callback from the lattice
		int z=0;
		while ((isWaiting ) && (z<50000)){
			virtualLattice.getOut().delay(2);
			z++;
		}
		
	}

	@Override
	public void futureHasCompleted(String guid) {
		 
		if (guidF.contentEquals(guid)){
			isWaiting=false;
		}
		
	}
	
	
}
