package org.NooLab.somfluid;

import java.util.ArrayList;

import org.NooLab.repulsive.components.data.SurroundResults;
import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.SomTasks;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.application.SomAppUsageIntf;
import org.NooLab.somfluid.core.application.SomAppValidationIntf;
import org.NooLab.somfluid.core.application.SomApplication;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.SomTargetedModeling;
import org.NooLab.somfluid.core.engines.det.adv.SomBags;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.env.communication.GlueClientAdaptor;
import org.NooLab.somfluid.env.communication.LatticeFutureVisor;
import org.NooLab.somfluid.env.communication.NodeTask;
import org.NooLab.somfluid.env.communication.NodesInformer;
import org.NooLab.somtransform.SomTransformer;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.objects.StringedObjects;


/*

	When someone says, "This is really tricky code," I hear them say, "This is really bad code."
	- Steve McConnell
	
	Thus, no tricks are included here.
*/

/**
 * 
 * SomFluid is starting the learning process and is  
 * running a supervising process about the state and the activity of the network
 * 
 * SomFluid does not possess any capabilities for graphical output.
 * The graphics is a separate module hosted by the SomFactory, to which the Fluid connects via port sending;
 * format of exchange is standardized, such the displayed info can be switched easily 
 * 
 * 
 * 
 */
public class SomFluid 
                      extends
                      			 NodesInformer
                      			 
                      implements Runnable,
								 SomFluidIntf,
								 
								 SomSupervisionIntf,
								 // events from article field, namely selections!
								 RepulsionFieldEventsIntf{
	
	
	String name = "";
	long numericID = 0L;

	SomFluidProperties sfProperties;
	SomFluidFactory sfFactory ;
	
	RepulsionFieldIntf particleField ;

	LatticePropertiesIntf latticeProperties;
	
	/** 
	 * VirtualLattice is essentially an ArrayList of &lt;MetaNodeIntf&gt;
	 * we never can call the routines of the MetaNode directly, we always have
	 * to to use an event mechanism  
	 */
	VirtualLattice virtualLatticeNodes; 
	
	SomDataObject somDataObject;
	
	SomTasks somTasks;
	
	SomBags somBags;
	
	SomFluid sf ;
 	
	SomApplication somApplication;
	
	boolean isActivated=false, isInitialized=false;
	boolean processIsRunning=false;
	Thread sfThread;
	
	StringedObjects sob = new StringedObjects();
	PrintLog out = new PrintLog(2, true);
	
	// ------------------------------------------------------------------------
	protected SomFluid( SomFluidFactory factory){
		
		basicInitialization( factory );
	}
	
	protected SomFluid( SomFluidFactory factory, long numericid ){
		
		basicInitialization( factory );
		
		completingInitialization(numericid);
	}
	
	private void basicInitialization(SomFluidFactory factory){
		
		sfFactory = factory;
		
		sfProperties = sfFactory.sfProperties ;
		latticeProperties = sfFactory.latticeProperties ;
		
		somDataObject = new SomDataObject( sfProperties ); 
		somDataObject.setFactory(sfFactory);
		somDataObject.setOut(out) ;
		
		
		somBags = new SomBags(this, sfProperties) ;
		
		sf = this;
		
		out.setPrefix("[SomFluid-main]");
	}
	
	protected void completingInitialization( long numericID ){
		
		this.numericID = numericID;
		
		sfThread = new Thread (this,"sfThread-"+numericID);
		
		virtualLatticeNodes = new VirtualLattice(this,latticeProperties);
		
		// if we are allowed to load the data, we'll do it there
		// note that the task of normalizing the data is performed by the transformer part
		// the SOM itself ALWAYS expects normalized data, even 
		somDataObject.prepare() ;
		
		initStructures();
		
		somTasks = new SomTasks( sfFactory ) ;
		
		
		
	}
	// ------------------------------------------------------------------------

	
	private void initStructures() {
		
		// note that the nodes will not contain a working feature vector,
		// all lists get initialized and defined, but remain empty
		createVirtualLattice( sfProperties.getInitialNodeCount() );
		
	}

	/**  
	 * Note that the particles in the field are just containers! They are not identical to nodes.
	 * Hence, our nodes need to be attached to the particles   */
	private void createVirtualLattice( int initialNodeCount) {
		MetaNode mnode;
		long idbase;
		Particle particle;
											out.print(2, "creating the physical node field for "+initialNodeCount+" particles...");
		// we need to harvest the events here!
		particleField = sfFactory.createPhysicalField( this, initialNodeCount);
		 
											out.print(2, "creating the logical som lattice for "+initialNodeCount+" nodes...");
		for (int i=0;i<initialNodeCount;i++){
			
			mnode = new MetaNode( virtualLatticeNodes, somDataObject  );
			virtualLatticeNodes.addNode(mnode) ;
			
											out.print(4,"Node <"+i+">, serial = "+mnode.getSerialID());
			 
			registerNodeinNodesInformer( mnode );
			 
			
			particle = particleField.getParticles().get(i);
			particle.setIndexOfDataObject( mnode.getSerialID() );
			
			
		}
		
		
		
		double d = particleField.getAverageDistanceBetweenParticles();
		virtualLatticeNodes.setAveragePhysicalDistance(d);
		
											out.print(2, "logical som lattice created.");
		// 
	}

	// ========================================================================
	
	
	
	protected void performTransformations() {
		
		SomTransformer transformer;
		transformer = sfFactory.getTransformer();
		
		
	}


	
	
	
	/**
	 * start this through message queue and task process,  
	 * 
	 * @param sfTask
	 */
	private void performTargetedModeling( SomFluidTask sfTask ) {
		// since we need this also for evo-optimization, we put it to a small class  
		// ther we use a different constructor (taking a copy from this basic instance...)
		
		SomTargetedModeling targetedModeling;
		
		targetedModeling = new SomTargetedModeling( this, sfProperties, somDataObject, virtualLatticeNodes, sfTask);
		targetedModeling.init().perform();
		 
	}

	// ========================================================================
	public void addTask(SomFluidTask somFluidTask) {
		 
		somTasks.add(somFluidTask);
	}


	@Override
	public void start() {
		
		
		sfThread.start();
		isActivated = true;
	}

	
	@Override
	public void run() {
		
		boolean isWorking = false;
		SomFluidTask sfTask = null;
		
		processIsRunning=true;
		
		try{
			
			while (processIsRunning){
				
			
				if ((isWorking==false) && (isActivated)){//) && (isInitialized)){
					isWorking=true;
					
					if ((somTasks!=null) && (somTasks.size()>0)){
						
						sfTask = somTasks.getItem(0) ;
						new TaskDispatcher(sfTask);
					}
					
					if ((sfTask!=null) && (sfTask.isCompleted())){
						somTasks.remove(0);
						isWorking=false;
					}
				}
				
				out.delay(2500);
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	// ..........................................
	class TaskDispatcher{

		public TaskDispatcher(SomFluidTask sfTask) {
			
			// dependent on task we invoke different methods and worker classes
			if (sfTask.somType == SomFluidProperties._SOMTYPE_MONO){
				
				// accessing the perssistent file,
				// it may be an external file containing raw data, or
				// if sth exists an already prepared one
				sfFactory.loadSource();
				 
				
				 
				
				// preparing the data, at least transforming and normalizing it
				// is embedded into the SomDataObject, where it is called by
				// importDataTable()
				// (of course, it can be called separately too,

				performTargetedModeling( sfTask );
				
			}else{
				// sfFactory.openSource();
				// performAssociativeStorage( sfTask );
			}
		}


		
		
	} // inner class TaskDispatcher
	// ========================================================================


	
	public void setSerialID(long serial) {
		numericID = serial;
	}


	public long getSerialID() {
		return numericID;
	}


	public String getNeighborhoodNodes( int nodeindex, int surroundN ) {
		int particleindex=nodeindex;
		
		// we need a map that translates between nodes and particles
		
		particleField.setSelectionSize( surroundN ) ;
		
		// asking for the surrounding, -> before start set the selection radius == new API function
		String guid = particleField.getSurround( particleindex, 1, true);
		
		// will immediately return, the selection will be sent 
		// through event callback to "onSelectionRequestCompleted()" below 
 		return  guid;
	}
	
	
	
	// --- Events from RepulsionField / physical particle field ---------------
	
	public SomDataObject getSomDataObject() {
		return somDataObject;
	}

	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}

	// ,
  	// these are public, but clients anyway have access only through the factory,
	// and the factory provides only the usage interface
	public SomAppValidationIntf getSomValidationInstance(){
		if (somApplication==null){
			somApplication = new SomApplication();
		}
		return (SomAppValidationIntf)somApplication ;
	}
	public SomAppUsageIntf getSomUsageInstance(){
		if (somApplication==null){
			somApplication = new SomApplication();
		}
		return (SomAppUsageIntf)somApplication ;
	}

	// ------------------------------------------------------------------------
	
	@Override
	public void onSelectionRequestCompleted( Object resultsObj ) {
		
		// TODO: this should be immediately forked into objects, since the requests could be served in parallel
		SurroundResults results, clonedResults;
		String str ;
		int[] particleIndexes;
		
		results = (SurroundResults)resultsObj;  
		
		/*
		 *  here we have to use a message queue running in its own process, otherwise
		 *  the SurroundRetrieval Process will NOT be released...
		 *  We have a chain of direct calls
		 *  
		 */
		
		// we have to prepare the results in the particlefield!
		// we need the list of lists: 
		// for each particle he have a list of indexes ArrayList<Long> getIndexesOfAllDataObject()
		particleIndexes = results.getParticleIndexes();
		
		clonedResults = (SurroundResults) sob.decode( sob.encode(results) );
											
											int n = clonedResults.getParticleIndexes().length   ;
											str = clonedResults.getGuid() ;
												
											out.print(5, "particlefield delivered a selection (n="+n+") for GUID="+str);

		
		// this result will then be taken as a FiFo by a digesting process, that
		// will call the method "digestParticleSelection(results);"
		// yet, it is completely decoupled, such that the current thread can return and finish
		
		if (virtualLatticeNodes.selectionResultsQueueDigesterAlive()==false){
											out.print(3, "restarting selection-results queue digester...");
			virtualLatticeNodes.startSelectionResultsQueueDigester();
			delay(50);
		}
		
		ArrayList<SurroundResults> rQueue = virtualLatticeNodes.getSelectionResultsQueue(); 
		rQueue.add( clonedResults );
		
		return; 
	}

	@Override
	public void onAreaSizeChanged(Object observable, int width, int height) {
		
	}

	@Override
	public void onActionAccepted(int action, int state, Object param) {
		
	}

	@Override
	public void statusMessage(String msg) {
		
	}

	@Override
	public void onLayoutCompleted(int flag) {
		
		out.print(3,"Layout of particle field has been completed.");
	}

	@Override
	public void onCalculationsCompleted() {

		if (sfFactory.physicalFieldStarted==0){
			out.print(4,"Calculations in particle field have been completed.");
			
		}
		sfFactory.physicalFieldStarted=1;
		
		sfFactory.getFieldFactory().setInitComplete(true);
	}

	public PrintLog getOut() {
		return out;
	}
	
	// ------------------------------------------------------------------------
	
	
	
	
}
