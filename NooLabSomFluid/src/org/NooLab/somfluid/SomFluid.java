package org.NooLab.somfluid;

import java.util.ArrayList;

import org.NooLab.repulsive.components.data.SurroundResults;
import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.SomTasks;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.adv.SomBags;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.env.communication.GlueClientAdaptor;
import org.NooLab.somfluid.env.communication.LatticeFutureVisor;
import org.NooLab.somfluid.env.communication.NodeTask;
import org.NooLab.somfluid.env.communication.NodesInformer;
import org.NooLab.somfluid.transformer.SomTransformer;
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

	protected void initializeNodesWithData(){
	
		// "by index" refers to 
		// this.notifyNodeByIndex(1, new NodeTask( NodeTask._TASK_SETDATA, variablesSetupDef, null) );
		// this.notifyNodeBySerial( virtualLatticeNodes.getNode(5).getSerialID(), new NodeTask( NodeTask._TASK_SETDATA, new String("123-"+i)) );

		// TODO: we may perform a PCA, thus deriving a weight vector (NOT: profile vector!!!)
		//       that would prepare the SOM into the direction of the main dimensions
		
		
	}
	
	
	protected void initializeNodesWithRandomvalues(){

		NodeTask task;
		Variables vars;
		String guid; 
		LatticeFutureVisor latticeFutureVisor;
		
		
		vars = somDataObject.getActiveVariables() ;
		
		if (vars.size()<=1){
			return;
		}
		if (virtualLatticeNodes==null){
			return;
		}
		 
											out.print(2, "loading data definitions to Som-Lattice...");
											
		// initialize feature vectors : only active variables , WITHOUT id, tv !!!
		
		
		latticeFutureVisor = new LatticeFutureVisor(virtualLatticeNodes,  NodeTask._TASK_SETVAR );
		 
											out.print(4, "before task sending...");
		task = new NodeTask( NodeTask._TASK_SETVAR, (Object)sob.encode( (Object)vars.getActiveVariableLabels()) );
		// do it for all nodes
		this.notifyAllNodes( task );
											out.print(4, "returned from task sending...  -> now waiting");
		latticeFutureVisor.waitFor(); // it will wait for completion of "_TASK_SETVAR", for all nodes since we did not define a particular one		

	 
											out.print(4, "continue, next task...");
		// set target variable ... TODO other messages about dynamic configuration : blacklist, whitelist, sim function 
		task = new NodeTask( NodeTask._TASK_SETTV, (Object)sob.encode( (Object)vars.getActiveTargetVariable()) );
		// do it for all nodes
		this.notifyAllNodes( task );
		
		 
											out.print(3, "loading data definitions done.");
											out.print(3, "initializing nodes...");
											
		latticeFutureVisor = new LatticeFutureVisor(virtualLatticeNodes,  NodeTask._TASK_RNDINIT );
		
		
		// ATTENTION: we have to wait !!! The informer immediately returns, then the init is sent before the node initialized!
		// the need for waiting until a process is completed is quite rare, and should occur only in the startup phase,
		// even on loading data into the SOM (learning) there should be no need to wait
		task = new NodeTask( NodeTask._TASK_RNDINIT  );
		this.notifyAllNodes( task );
		
		latticeFutureVisor.waitFor(); delay(100); 
											out.print(1, "initialization of SomFluid done.");
											
											
	}
	
	
	// strat this through message queue and task process,  
	private void performTargetedModeling( SomFluidTask sfTask ) {
		String activeTvLabel;
		DSom dSom ;
		
		
		// now, the somDataObject knows about the DataTable
		// if there is some data in SomDataObject, it will be loaded into nodes
		
		// we have to remove empty columns, blacklisted columns, columns that are excluded dynamically 
		// by criteria like derivation level 
		somDataObject.determineActiveVariables();
		
		 
		
		initializeNodesWithRandomvalues(); // adopting feature vectors, not yet the data of course
		
		initializeNodesWithData(); 
		
		  
		
		activeTvLabel = sfProperties.getModelingSettings().getActiveTvLabel() ; // "TV"
		// TargetVariable  targetVariable;
		
		if (sfProperties.getModelingSettings().getSomBagSettings().getApplySomBags()==false){
			// no bagging, just standard normal som-ing 
			dSom = new DSom( this, somDataObject, virtualLatticeNodes, sfTask );
			dSom.performTargetedModeling();
			
		}else{
			// we create bags according to parameters, eachbag will run a DSom then...
			// results will be collected by SomBag, and meta-results will be evaluated also there
			somBags.createBags();
			
			somBags.runBags();
		}
		
		
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
		
 		return  guid;
	}
	
	
	
	// --- Events from RepulsionField / physical particle field ---------------
	
	public SomDataObject getSomDataObject() {
		return somDataObject;
	}

	public SomFluidProperties getSfProperties() {
		return sfProperties;
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
		
		virtualLatticeNodes.getSelectionResultsQueue().add( clonedResults );
		
		// this result will then be taken as a FiFo by a digesting process, that
		// will call the method "digestParticleSelection(results);"
		// yet, it is completely decoupled, such that the current thread can return and finish
		
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
		
		out.print(2,"Layout of particle field has been completed.");
	}

	@Override
	public void onCalculationsCompleted() {

		if (sfFactory.physicalFieldStarted==0){
			out.print(2,"Calculations in particle field have been completed.");
		}
		sfFactory.physicalFieldStarted=1;
	}

	public PrintLog getOut() {
		return out;
	}
	
	// ------------------------------------------------------------------------
	
	
	
	
}
