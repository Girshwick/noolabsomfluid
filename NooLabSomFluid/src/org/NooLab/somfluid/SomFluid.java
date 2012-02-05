package org.NooLab.somfluid;

import java.util.ArrayList;
import java.util.Observable;

import org.NooLab.repulsive.RepulsionField;
import org.NooLab.repulsive.components.data.SurroundResults;
import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.somfluid.components.IndexedDistances;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.env.communication.LatticeFutureVisor;
import org.NooLab.somfluid.env.communication.NodeTask;
import org.NooLab.somfluid.env.communication.NodesInformer;
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
	 * VirtualLattice is essentially an  ArrayList of &lt;MetaNodeIntf&gt;
	 * we never can call the routines of the MetaNode directly, we always have
	 * to to use an event mechanism  
	 */
	VirtualLattice virtualLatticeNodes; 
	
	SomDataObject somDataObject;
	
	SomTasks somTasks;
	
 	
	boolean isActivated=false, isInitialized=false;
	boolean processIsRunning=false;
	Thread sfThread;
	
	StringedObjects so = new StringedObjects();
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
		
		out.setPrefix("[SomFluid-main]");
	}
	
	protected void completingInitialization( long numericID ){
		
		this.numericID = numericID;
		
		sfThread = new Thread (this,"sfThread-"+numericID);
		
		virtualLatticeNodes = new VirtualLattice(this,latticeProperties);
		
		// if we are allowed to load the data, we'll do it there
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
		
		// we need to harvest the events here!
		particleField = sfFactory.createPhysicalField( this, initialNodeCount);
		 
											out.print(2, "creating the logical som lattice...");
		for (int i=0;i<initialNodeCount;i++){
			
			mnode = new MetaNode( virtualLatticeNodes, somDataObject  );
			virtualLatticeNodes.addNode(mnode) ;
			
											out.print(4,"Node <"+i+">, serial = "+mnode.getSerialID());
			 
			registerNodeinNodesInformer( mnode );
			 
			
			particle = particleField.getParticles().get(i);
			particle.setIndexOfDataObject( mnode.getSerialID() );
			
			
		}
											out.print(2, "logical som lattice created.");
		// 
	}

	protected void initializeNodesWithData(){

		NodeTask task;
		Variables vars;
		String guid; 
		LatticeFutureVisor latticeFutureVisor;
		
		
		vars = somDataObject.getVariables() ;
		
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
		task = new NodeTask( NodeTask._TASK_SETVAR, (Object)so.encode( (Object)vars.getActiveVariableLabels()) );
		// do it for all nodes
		this.notifyAllNodes( task );
											out.print(4, "returned from task sending...  -> now waiting");
		latticeFutureVisor.waitFor(); // it will wait for completion of "_TASK_SETVAR", for all nodes since we did not define a particular one		

											out.print(4, "continue, next task...");
		// set target variable ... TODO other messages about dynamic configuration : blacklist, whitelist, sim function 
		task = new NodeTask( NodeTask._TASK_SETTV, (Object)so.encode( (Object)vars.getActiveTargetVariable()) );
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
		// "by index" refers to 
		// this.notifyNodeByIndex(1, new NodeTask( NodeTask._TASK_SETDATA, variablesSetupDef, null) );
		// this.notifyNodeBySerial( virtualLatticeNodes.getNode(5).getSerialID(), new NodeTask( NodeTask._TASK_SETDATA, new String("123-"+i)) );

	}
	
	
	// strat this through message queue and task process,  
	private void performTargetedModeling( SomFluidTask sfTask ) {
		String activeTvLabel;
		DSom dSom ;
		
		activeTvLabel = sfProperties.getModelingSettings().getActiveTvLabel() ; // "TV"
		// TargetVariable  targetVariable;
		
		dSom = new DSom( sfFactory, somDataObject, virtualLatticeNodes, sfTask );
		dSom.performTargetedModeling();
		
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
			
		}
	}

	// ..........................................
	class TaskDispatcher{

		public TaskDispatcher(SomFluidTask sfTask) {
			
			// dependent on task we invoke different methods and worker classes
			if (sfTask.somType == SomFluidProperties._SOMTYPE_MONO){
				sfFactory.loadSource();
				performTargetedModeling( sfTask );
			}else{
				// sfFactory.openSource();
				// performAssociativeStorage( sfTask );
			}
		}
		
		
	} // inner class TaskDispatcher
	// ========================================================================



	// ------------------------------------------------------------------------
	
	public void setSerialID(long serial) {
		numericID = serial;
	}


	public long getSerialID() {
		return numericID;
	}


	public String getNeighborhoodNodes( int nodeindex ) {
		int particleindex=nodeindex;
		
		// we need a map that translates between nodes and particles
		
		
		// asking for the surrounding, -> before start set the selection radius == new API function
		String guid = particleField.getSurround( particleindex, 1, true);
		
 		return  guid;
	}
	
	
	
	// --- Events from RepulsionField / physical particle field ---------------
	
	public SomDataObject getSomDataObject() {
		return somDataObject;
	}

	@Override
	public void onSelectionRequestCompleted(Object resultsObj) {
		
		// TODO: this should be immediately forked into objects, since the requests could be served in parallel
		SurroundResults results;
		String str ;
		int[] particleIndexes;
		
		results = (SurroundResults)resultsObj;
		
		// we have to prepare the results in the particlefield!
		// we need the list of lists: 
		// for each particle he have a list of indexes ArrayList<Long> getIndexesOfAllDataObject()
		particleIndexes = results.getParticleIndexes();
		
		virtualLatticeNodes.digestParticleSelection(results);
		
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
