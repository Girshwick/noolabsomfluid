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
	
	ClassificationDescription  classifyDescription;
/*  put this all in ClassificationDescription
     public double Sum_of_Cost    = -1;
    public double weighted_variance_total = -1;
	
    // these two are set in the "describe classification" context
    public String majorityLabel="" ; // the most frequent class in the node
    public String majorityValue="" ; // the most frequent TV norm-value in the node
    
    //  the proportion of "cases" in the Node;
    //  what a "case" is, is defined in SOMPrototypes
	//  this has as many items as there are target groups 
	        -> note that there are 2 modes: 
	           - several tgroups defining 1 target class, or
	           - several tgroups defined for multi-class modeling
	                                                                             
	public double[] targetproportion ;  
	public boolean[] isPrototype ;
 
  
 */

	
	// TODO for those contexts as represented by these interfaces, we need the respective properties Objects for persistence
	
	int virtualRecordCount = 0; // the count of weight adjustment operations applied to the node
	private boolean contentSensitiveInfluence;
	private boolean changeIsSizedependent=true;
	
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
	public void adjustProfile(  ArrayList<Double> datarecord,
								double learningrate, double influence, 
								double sizeFactor, int i) {

		int return_value=-1, contrastEnh;
    	int err=1;
    	int vn,_d, recordcount  ;
    	boolean _blocked=true, calcThis, calculateAllVariables=false ;
    	double contextual_influence_reduction=1;
    	double _new_w=0;
    	double _old_w, w_d ,_LR=1.0f,change ;
    	
    	
    	ArrayList<Double> usevector, nodeProfile, vector2 ;

		try {

			err = 2;
			// in case of excluded variable (that are not IX or TV) we possibly
			// want to calculate the profile values to
			// calculateAllVariables = modelingSettings.calc_all_variables();
			calculateAllVariables = false ;
			
			usevector = intensionality.getWeightsVector();
			
			// creating copies of the two vectors
			nodeProfile = new  ArrayList<Double>(intensionality.getProfileVector().getValues()) ;
			vector2 = new ArrayList<Double>(datarecord);

			recordcount = extensionality.getCount() ;
			
			if (contentSensitiveInfluence) {
				// not available yet
				/*
				 * content sensitive update means, that nodes show a context specific resistance to get updated 
				 * in the standard way. 
				 * If the relative difference is very large, the update will be smaller However, in order to 
				 * normalize the actual difference into the relative difference, we need a global statistics
				 * abut each variable across all nodes. This is very expensive, thus 
				 * we update only very rarely (around 5x per learning epoch)
				 * 
				 * So far we have no such statistics running, thus we cannot perform this yet
				 */

			} // contentSensitiveInfluence ?

			err = 3;
			// data object is available through the object pointer <SD> ==
			// SOM_data
			_d = intensionality.getProfileVector().getValues().size();

			 
			
			contrastEnh = 0;
			

			for (int w = 0; w < _d; w++) { // across all fields
				err = 4;

				_old_w = nodeProfile.get(w); // saving weight of variable[w] as old weight

				// work on the variable only if allowed
				calcThis = (usevector.get(w) > 0);
				
				if (calcThis) {
					if (calculateAllVariables) {
						// we may update all variables, the distance will be
						// calc'd only for used ones anyway !
						if (w == similarity.getIndexTargetVariable()) {
							calcThis = false; // always to exclude: the index
												// column
						}
						// || ( w == similarity.getIndexIdColumn() ))
						// we include the target variable, so we can see the expected mean value,
						// albeit this info will be often overruled

					}
				}

				if (calcThis) {

            		err =5;
                     
                      if ( ((nodeProfile.get(w)>=0) && ( vector2.get(w)>=0)) && (vector2.get(w)<=1)){ 
                    	  // excluding MV ...
                                                      
                       
                                                     		err = 6;
                         sizeFactor = 1;

                         if (virtualRecordCount+ recordcount<= Math.sqrt( 0.1+recordcount)){
                    	   
                        
                                                        
                                                     		err = 7;
                          w_d =0 ;
                          w_d = (nodeProfile.get(w) - vector2.get(w)) ;
                          w_d = w_d +  w_d * (3*nodeProfile.get(w) - vector2.get(w))/2 ;

                       }
                       else
                       {
                          w_d = (nodeProfile.get(w) - vector2.get(w)) ;

                          									err = 8;
                       } //else:  recordcount <= ?
                       
                       // w_d = w_d * contextual_influence_reduction; // not specified so far, working with default=1 here

                                                         	err = 9;
                         _LR = learningrate ;
                         
                         if ( ( recordcount+ Math.log10(1+virtualRecordCount)<=2) 
                        	  && (!_blocked)){ // also in the neighbourhood ...
                        	 // not used so far
                                                         	err = 10;
                            _LR = (float) (_LR + ( 1-_LR)/(11 + Math.log10(1+_d) + Math.log10(1+virtualRecordCount)));
                         }
                                                         	err = 11;
                           change = (float) ((_LR * influence * w_d)* (1/(1+contrastEnh))) ;
                           
                           change = (float) adaptChangeRelativeToNodeExtSize( change,recordcount ) ;
                           
                                                         	err = 12;
                           if ( (change<-1) || (change>2)){
                              change=0;
                           }
                           if (virtualRecordCount==0){
                        	   _new_w = nodeProfile.get(w);
                        	   vector2.set(w,nodeProfile.get(w) );
                           }
                           else{
                        	   _new_w = vector2.get(w) + change;
                           }
                           
                           virtualRecordCount = virtualRecordCount+1 ;
                      
                       										err = 14;
                       if (_new_w>1){ 
                       		_new_w = 1 ;
                       		
                       		System.out.println("\nProblem in AdjustWeights(a): "+
                       		                   "new weight = "+String.format("%4.3f", _new_w)+
                       		                   "\nLR        = "+String.format("%4.3f", _LR)+
                       		                   "\nInfluence = "+String.format("%4.3f", influence)+
                       		                   "\nw_d       = "+String.format("%4.3f", w_d)+
                       		                   "\nFVirtRecord_count = "+String.valueOf(virtualRecordCount)+
                       		                   "\n");
                       
                       
                       		_new_w= 1/(4-4);
                       }
                       
                       if ((_new_w<0) && (_new_w>-0.3)){_new_w=0;}

                       nodeProfile.set(w, _new_w);
                       										err = 15;
                       if ((_new_w>=0) && (_new_w<=1)){
                       		return_value = 0 ;   
                       }
                   } // exclude MV for both vectors   
                                                     
                } // usagevector[w] ?

            } // w-> across all fields
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	private double adaptChangeRelativeToNodeExtSize(double _cval, int recordcount) {

		double _v, return_value;

		return_value = _cval;
		if (changeIsSizedependent) {

			// thats for merging nodes: _v := ((_fval1*_count1) +
			// (_fval2*_count2))/(_count1 + _count2)

			_v = Math.log10(1 + recordcount) + 1.0; // >= 1 ,, FVirtRecord_count
			_v = Math.sqrt(1 / _v);

			return_value = _cval * _v;
		}

		return return_value;

	}
	
	
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
	public IntensionalitySurfaceIntf importIntensionalitySurface( long serialID ) {
		IntensionalitySurfaceIntf intensionality;
			
		intensionality = virtualLattice.distributeIntensionalitySurface(serialID); // ProfileVector@30d83d
		
		return intensionality;
	}

	
	@Override
	public IntensionalitySurfaceIntf importIntensionalitySurface() {
		// that's the intenionality of the whole lattice ! 
		
		IntensionalitySurfaceIntf intensionality;
		
		intensionality = virtualLattice.distributeIntensionalitySurface(); // ProfileVector@30d83d
		
		return intensionality;
	}
	
	@Override
	public SimilarityIntf importSimilarityConcepts() {
		SimilarityIntf similarity;
		
		similarity = virtualLattice.distributeSimilarityConcept(); // Similarity@1db6942
		return similarity;
	}

	@Override
	public SimilarityIntf importSimilarityConcepts(long serialID) {
SimilarityIntf similarity;
		
		similarity = virtualLattice.distributeSimilarityConcept(serialID); // Similarity@1db6942
		return similarity;
	}

	@Override
	public ExtensionalityDynamicsIntf importExtensionalityDynamics() {
		ExtensionalityDynamicsIntf extensionality;
		
		extensionality = virtualLattice.distributeExtensionalityDynamics();
		return extensionality;
	}

	@Override
	public ExtensionalityDynamicsIntf importExtensionalityDynamics(long serialID) {
		ExtensionalityDynamicsIntf extensionality;
		
		extensionality = virtualLattice.distributeExtensionalityDynamics(serialID);
		return extensionality;
	}

	@Override
	public MetaNodeConnectivityIntf importMetaNodeConnectivity() {
		MetaNodeConnectivityIntf nodeConnectivity ;
		nodeConnectivity = virtualLattice.distributeNodeConnectivity();
		return nodeConnectivity;
	}

	@Override
	public MetaNodeConnectivityIntf importMetaNodeConnectivity(long serialID) {
		
		MetaNodeConnectivityIntf nodeConnectivity ;
		nodeConnectivity = virtualLattice.distributeNodeConnectivity(serialID);
		return nodeConnectivity;
	}

	@Override
	public void setContentSensitiveInfluence(boolean flag) {
		
		contentSensitiveInfluence = flag ;
	}





	

 
	


	 

}








