package org.NooLab.somfluid.core.engines.det;

import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.engines.det.adv.SomBags;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.env.communication.LatticeFutureVisor;
import org.NooLab.somfluid.env.communication.NodeTask;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.objects.StringedObjects;




public class SomTargetedModeling {

	SomFluid somFluid;
	SomFluidProperties sfProperties; 
	SomDataObject somDataObject;
	VirtualLattice virtualLatticeNodes; 
	SomFluidTask sfTask;
	
	SomBags somBags;
	
	StringedObjects sob = new StringedObjects();
	PrintLog out = new PrintLog(2, true);
	
	
	// ========================================================================
	public SomTargetedModeling( SomFluid somfluid,
								SomFluidProperties sfproperties, 
								SomDataObject somDataObj,
								VirtualLattice latticeNodes, 
								SomFluidTask sftask) {
		
		somFluid = somfluid  ;
		sfProperties = sfproperties  ; 
		somDataObject = somDataObj ;
		virtualLatticeNodes = latticeNodes ; 
		sfTask = sftask  ;
		
	}
	// ========================================================================
	
	
	public SomTargetedModeling(SomTargetedModeling targetModeling) {
		 
		somFluid = targetModeling.somFluid  ;
		sfProperties = targetModeling.sfProperties  ; 
		somDataObject = targetModeling.somDataObject ;
		virtualLatticeNodes = targetModeling.virtualLatticeNodes ; 
		sfTask = targetModeling.sfTask  ;
	}

	public SomTargetedModeling init(){

		String activeTvLabel;
		// now, the somDataObject knows about the DataTable
		// if there is some data in SomDataObject, it will be loaded into nodes
		
		// we have to remove empty columns, blacklisted columns, columns that are excluded dynamically 
		// by criteria like derivation level 
		somDataObject.determineActiveVariables();
		
		ClassificationSettings cf = sfProperties.getModelingSettings().getClassifySettings() ;
		int targetMode = cf.getTargetMode();
		
		if ( targetMode==ClassificationSettings._TARGETMODE_MULTI ) {
			if ((cf.getTargetGroupDefinition().length==0) || (cf.getTargetGroupDefinition()[0].length==0) ||
				(cf.getAutomaticTargetGroupDefinition()) ){

				somDataObject.inferTargetGroups( sfProperties.getModelingSettings() );
			}
		}
		
		initializeNodesWithRandomvalues(); // adopting feature vectors, not yet the data of course
		
		initializeNodesWithData(); 
		
		  
		
		activeTvLabel = sfProperties.getModelingSettings().getActiveTvLabel() ; // "TV"
		// TargetVariable  targetVariable;
		
		return this;
	}

	public void perform() {
		 
		

		
		DSom dSom ;
		
		
		// no bagging, just standard normal som-ing 
		dSom = new DSom( somFluid, somDataObject, virtualLatticeNodes, sfTask );
		dSom.setEmbeddingInstance( this ) ;


		if (sfProperties.getModelingSettings().getSomBagSettings().getApplySomBags()){
			
			// we create bags according to parameters, eachbag will run a DSom then...
			// results will be collected by SomBag, and meta-results will be evaluated also there
			somBags.createBags( dSom ); // will put the bags as samples into object "dataSampler{}"
			
			somBags.runBags();

		} else{
			
			// TODO: make a deep copy of modeling settings (serial object) and change some of them !!
			dSom.performTargetedModeling();
		}
		 
		
		
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
		somFluid.notifyAllNodes( task );
											out.print(4, "returned from task sending...  -> now waiting");
		latticeFutureVisor.waitFor(); // it will wait for completion of "_TASK_SETVAR", for all nodes since we did not define a particular one		

	 
											out.print(4, "continue, next task...");
		// set target variable ... TODO other messages about dynamic configuration : blacklist, whitelist, sim function 
		task = new NodeTask( NodeTask._TASK_SETTV, (Object)sob.encode( (Object)vars.getActiveTargetVariable()) );
		// do it for all nodes
		somFluid.notifyAllNodes( task );
		
		 
											out.print(3, "loading data definitions done.");
											out.print(3, "initializing nodes...");
											
		latticeFutureVisor = new LatticeFutureVisor(virtualLatticeNodes,  NodeTask._TASK_RNDINIT );
		
		
		// ATTENTION: we have to wait !!! The informer immediately returns, then the init is sent before the node initialized!
		// the need for waiting until a process is completed is quite rare, and should occur only in the startup phase,
		// even on loading data into the SOM (learning) there should be no need to wait
		task = new NodeTask( NodeTask._TASK_RNDINIT  );
		somFluid.notifyAllNodes( task );
		
		latticeFutureVisor.waitFor(); out.delay(100); 
											out.print(1, "initialization of SomFluid done.");
											
											
	}
	
}
