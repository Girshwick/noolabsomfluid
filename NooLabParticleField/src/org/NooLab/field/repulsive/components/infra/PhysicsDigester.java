package org.NooLab.field.repulsive.components.infra;


import org.NooLab.chord.CompletionEventMessageCallIntf;
import org.NooLab.chord.IndexedItemsCallbackIntf;
import org.NooLab.chord.MultiDigester;
import org.NooLab.field.repulsive.RepulsionFieldCore;
import org.NooLab.field.repulsive.components.FluidFieldCollectStatistics;
import org.NooLab.field.repulsive.particles.RepulsionFieldParticles;



public class PhysicsDigester implements IndexedItemsCallbackIntf{
	 
	MultiDigester digester=null ;
	 
	//Vector<String>  rowText ;
	RepulsionFieldParticles pparticles;
	boolean baseDataChanged=false;
	boolean activated=false;
	
	RepulsionFieldCore parentField;
	FluidFieldCollectStatistics statisticsCollector;
	CompletionEventMessageCallIntf completionObserver ;
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	public PhysicsDigester(RepulsionFieldCore parent ){
		
		this.parentField = parent ;
		
		statisticsCollector = parentField.getStatisticsCollector() ;
	}
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	
	public void doParallelPhysics(  RepulsionFieldParticles particles, int threadcount){ 
		 
		
		// providing also right now the callback address (=this class)
		// the interface contains just ONE routine: perform()
		if ((digester==null) || (baseDataChanged) || (parentField.nextThreadCount>0)){
			
			parentField.nextThreadCount=0;
			
			digester = new MultiDigester(threadcount, (IndexedItemsCallbackIntf)this ) ;
			digester.setPriority(7);
			completionObserver = parentField; 
			 
			// parentField implements CompletionEventMessageCallIntf, and will be called
			// if the process finished
			
			this.pparticles = particles;
		
		// rowText = rowtext ;
		
		// note, that the digester need not to know "anything" about our items, just the amount of items
		// we would like to work on.
		// the digester then creates simply an array of indices, which then point to the actual items,
		// which are treated anyway here (below) !
			digester.prepareItemSubSets( pparticles.size(),0 );
		 
		}else{
			// will also take any change of "threadcount"
			digester.reset();
		}

		
		digester.execute() ;
		activated=true;
		
		completionObserver.processUpdate( this, 0, "");
		if (statisticsCollector!=null){
		statisticsCollector.isPhysicsProcessActivated=true;
		
		statisticsCollector.isWaiting=false;
		if (parentField.nextThreadCount>0){
			threadcount = parentField.nextThreadCount;
		}
		}
		parentField.increaseStepsCounter(1);
		
		// do NOT do this here: System.gc(); uses a lot of resources, blocks all other threads
		
	}
	
	 
	 
	// this will be called back, the multi-threaded digester selects an id, which is called from within one of the threads
	// the processID is just for fun... (and supervision)
	public void perform( int processID, int id ) {

		parentField.doPhysicsFor(id);

		
		if (parentField.fieldThrd != null) {
			if (parentField.isStopped()) {
				digester.stopAll();
			}
		}
	  
	}

	 
	public void setBaseDataChanged(boolean flag) {
		this.baseDataChanged = flag;
	}

	public boolean isActivated() {
		return activated;
	}

} //  class Digester
