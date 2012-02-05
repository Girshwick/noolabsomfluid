package org.NooLab.somfluid.core.nodes;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;


import org.NooLab.repulsive.intf.DataObjectIntf;
import org.NooLab.somfluid.components.DataSourceIntf;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.categories.connex.MetaNodeConnectivityIntf;
import org.NooLab.somfluid.core.categories.connex.NetworkMessageIntf;
import org.NooLab.somfluid.core.categories.connex.NodesMessageIntf;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.imports.MetaNodeConnectivityImportIntf;
import org.NooLab.somfluid.core.categories.imports.ExtensionalityDynamicsImportIntf;
import org.NooLab.somfluid.core.categories.imports.IntensionalitySurfaceImportIntf;
import org.NooLab.somfluid.core.categories.imports.SimilarityImportIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;
import org.NooLab.somfluid.env.communication.NodeObserverIntf;
import org.NooLab.somfluid.env.communication.NodeTask;
import org.NooLab.utilities.logging.PrintLog;


/**
 * 
 * the meta-node is either <br/>
 * - a standard SOM node  <br/>
 * - any arbitrary function which returns a "signature",  <br/>
 *   e.g. any SOM, a ANN  <br/><br/>
 *   
 * in case of default SOM, the Node is just the profile with its cluster content, in other words, 
 * nearly a constant, only dependent from the data, which are constant anyway. <br/><br/>
 * 
 * If we replace this fixation with a "function" (category theory: arrow), we gain a 
 * lot of flexibility and adaptivity. <br/><br/>
 * The structure which reflects this is the MetaNode. <br/>
 * 
 * This allows for multi-dimensional fractal growth, since the Som inside a node may grow and 
 * differentiate, too, of course
 * 
 * Another important difference to standard SOM nodes is that the nodes are actively processing
 * their content, i.e. additionally to the calculation triggered by the
 * nework (change in data, explicit activation trigger), they also run more or less periodically an 
 * internal update mechanism 
 * 
 * -> NOT the central instance distributes the data once the data are in the network, but the meta nodes themselves!!
 * 
 * The Meta-Node has private properties, that are instantiated by the network, but that are separate
 * instances afterwards. 
 * These properties are
 * - similarity function
 * - activity level
 * - connectivity : amount of relations, types of relations (purely random, directed in/out)
 * 
 * the processes of a meta node are
 * 
 * - updating the weight vector
 * - growing directed fibers (axons, dendrites)
 * - "efforts" devoted to "digesting" versus "transmitting" (default 0.98 : 0.02)
 * - differentiating into SOM (nested, pullulated/outgrowing)
 * - 
 * 
 * The SomFluid objects holds a copy of the weight vectors of all nodes (?in this lattice?)
 * 
 * 
 * Nodes do not hold a copy of the data in most scenarios;
 * instead, they refer to the SomDataObject that contains the input data
 * 
 * Yet there is the possibility (via imported interface) that the intensity of the ...
 * 
 * A proper distinction between the extensional part and the intensional part is established.
 * In the intensional part, profiles are distinguished from weights (in standard kohonen, they are not distinguished)
 * It is important to understand that what is commonly called "weight" vector is not a weight vector at all. 
 * Its the profile vector that describes the intensional mapping of the extensional list.
 * 
 * 
 * blacklist, whitelist are defined on the level of the lattice
 */
public class MetaNode   extends
                                    AbstractMetaNode
						implements  
						
								 	NetworkMessageIntf,
								 	// for messages from the Node level (between nodes)
								 	NodesMessageIntf
								 				    	  {
	


	
	// TODO for thos contexts as represented by these interfaces, we need the respective properties Objects for persistence
	
	// ------------------------------------------------------------------------
	public MetaNode( VirtualLattice virtualLatticeNodes, DataSourceIntf somData ){
		super(virtualLatticeNodes,  somData );
		

	
	}
	// ------------------------------------------------------------------------	
	  
	// use it like so: (resource: http://tutorials.jenkov.com/java-generics/methods.html)
	// WeightVector weightVector   = getInfoFromNode( WeightVector.class, 1);
	@Override
	public <T> T getInfoFromNode(Class<T> rqClass, int infoID)  throws IllegalAccessException, 
																		InstantiationException {
		
		String className = rqClass.getSimpleName();
		T object = null ;
		
		// T object = rqClass.newInstance();
		// set properties via reflection.
		
		if (className.toLowerCase().contains("profilevector")){
			object = (T) profileVector;
		}
		
		// size, StatsObject, etc. ....
		
		return object;
	}
 
	
	
	// ---- Events from NetworkMessageIntf ------------------------------------

	@Override
	public void onRequestForAdaptingWeightVector( Object obj, Object params) {
		ProfileVectorIntf weightVector;
		
		
		if (openLatticeFutureTask == NodeTask._TASK_ADAPT){
			virtualLattice.nodeInformsAboutCompletedTask( openLatticeFutureGuid );
		}
	}



	/**
	 * note that objects arrive as encoded objects in order to guarantee perfect decoupling without clone()-ing!!
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onDefiningFeatureSet(Object obj , DataHandlingPropertiesIntf obj2) {
		 
		// ArrayList<String> varlabels;
		Object dcobj = decodeMsgObject(obj);
		
		if (dcobj!=null){
			// varlabels = (ArrayList<String>) dcobj;
											out.print(4, "node <" + serialID + ">, onDefiningFeatureSet()");
			variableLabels = new ArrayList<String>((ArrayList<String>) dcobj); // (varlabels);
		}
		
		if (openLatticeFutureTask == NodeTask._TASK_SETVAR){
			virtualLattice.nodeInformsAboutCompletedTask( openLatticeFutureGuid );
			openLatticeFutureGuid = "";
			openLatticeFutureTask = -1;
			// tasks for waiting should not overlap !!!
		}
		
	}
	
	@Override
	public void onDefiningTargetVar(Object obj) {

		Object dcobj = decodeMsgObject(obj);
		
		if (dcobj!=null){
			
			targetVariableLabel = (String)dcobj ;
		}

		// 
		if (openLatticeFutureTask == NodeTask._TASK_SETTV){
			virtualLattice.nodeInformsAboutCompletedTask( openLatticeFutureGuid );
		}
	}
	
	
	@Override
	public void onSendingDataObject( Object data, DataHandlingPropertiesIntf datahandler) {
		
		out.print(2, "node <"+serialID+">, onSendingDataObject()");

		// formatting the data object: here, the object contains the index pointer to the
		// SomDataObject
		
		
		
		//  we put this index into the list
		sdoIndexValues.add( 0L );
		
		// and trigger recalculation, if immediate recalc is requested by propertized option
		
		
		// check for waiting futures
	 
		if (openLatticeFutureTask == NodeTask._TASK_SETDATA){
			virtualLattice.nodeInformsAboutCompletedTask( openLatticeFutureGuid );
		}
		return  ;
	}


	@Override
	public void onRequestForDedicatedUpdate() {
		//  
		
		 
		if (openLatticeFutureTask == NodeTask._TASK_UPDATE){
			virtualLattice.nodeInformsAboutCompletedTask( openLatticeFutureGuid );
		}
	}

	@Override
	public void onRequestForRandomInit(Object obj ) {
		 
		initializeSOMnode();
		
											out.print(4, "task _TASK_RNDINIT> received ...");
				
		if (openLatticeFutureTask == NodeTask._TASK_RNDINIT){
											out.print(4, "task identified as <_TASK_RNDINIT>, node now informing lattice about guid : "+openLatticeFutureGuid);
			virtualLattice.nodeInformsAboutCompletedTask( openLatticeFutureGuid );
		}
	}

	@Override
	public void onRequestForDataRemoval(ArrayList<Long> dataIndex) {
		//  
		
		
		// NodeTask._TASK_REMOVE
	}


	@Override
	public void onRequestForDataRemoval() {
		//  
		
		// NodeTask._TASK_REMOVE
	}


	@Override
	public void onRequestForMemoryReset() {
		//  
		
		
		// NodeTask._TASK_CLEAR
	}


	@Override
	public void onArrivalOfChemicalStimulus() {
		//  
		
	}


	@Override
	public void onRequestForChangingActivityLevel() {
		//  
		
	}

	// ========================================================================

	@Override
	public IntensionalitySurfaceIntf importIntensionalitySurface() {
		IntensionalitySurfaceIntf intensionality;
			
		intensionality = virtualLattice.distributeIntensionalitySurface();
		
		return intensionality;
	}

	@Override
	public SimilarityIntf importSimilarityConcepts() {
		SimilarityIntf similarity;
		
		similarity = virtualLattice.distributeSimilarityConcept();
		return similarity;
	}

	@Override
	public MetaNodeConnectivityIntf importMetaNodeConnectivity() {
		MetaNodeConnectivityIntf nodeConnectivity ;
		nodeConnectivity = virtualLattice.distributeNodeConnectivity();
		return nodeConnectivity;
	}

	@Override
	public ExtensionalityDynamicsIntf importExtensionalityDynamics() {
		ExtensionalityDynamicsIntf extensionality;
		
		extensionality = virtualLattice.distributeExtensionalityDynamics();
		return extensionality;
	}



	

 
	


	 

}








