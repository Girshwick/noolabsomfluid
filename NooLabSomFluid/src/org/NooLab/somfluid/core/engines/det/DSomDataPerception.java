package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;
import java.util.Map;

import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexDistanceIntf;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;


import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.engines.det.results.FrequencyList;
import org.NooLab.somfluid.core.engines.det.results.FrequencyListGeneratorIntf;
import org.NooLab.somfluid.core.engines.det.results.ItemFrequencies;
import org.NooLab.somfluid.core.engines.det.results.ItemFrequency;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.properties.ModelingSettings;


 

public class DSomDataPerception 
									extends 
												DSomDataPerceptionAbstract 
									implements 
												FrequencyListGeneratorIntf{

	
	boolean _DEBUG = false;
	
 

	ArrayList<Integer> sampleRecordIDs, volatileSample;
	
	int       currentEpoch, somSteps, neighbourhoodSize=1 ;
	double    mapRadius, neighbourhoodRadius;
	double    learningRate, timeConstant, neighbourhoodDecay = 1.45 ;
	
	double    constStartLearningRate = 0.25 ;
	int       learningRateFixationmode = 2 ;



	
	
	
	
	// ========================================================================
	public DSomDataPerception( DSom dsom , ArrayList<Integer> sampleRecords) {
		super(dsom) ;
		 
		sampleRecordIDs = new ArrayList<Integer>(sampleRecords);
		volatileSample  = new ArrayList<Integer>(sampleRecords);
		
	}
	// ========================================================================	

	public void clear(){
		sampleRecordIDs.clear() ;
		volatileSample.clear() ;
	}
	// ........................................................................
	public void setLoopParameters(int epoch, int somsteps) {
	 
		currentEpoch = epoch;
		somSteps = somsteps;
	}

	public void setDynamicsParameters(double learningrate, double timeconstant, double nbRadius ) {
		 
		learningRate = learningrate;
		timeConstant = timeconstant ;
		neighbourhoodRadius = nbRadius;
	}
	
	
	
	
	// ........................................................................
	
	// call from the outside
	public void go() {
		go(0);
	}
	
	// here we feed the data into the SOM 
	private void go( int mode ) {
		 
		int recordsConsidered =0, currentRecordIndex;
		int  f, winnerIndex;
		int minFill ;
		
		
		ArrayList<IndexDistanceIntf>  winningNodeIndexes;
		
		// this cold be filled via the pre-locating service SOM of much lower resolution, followed
		// by a selection of indexes in the scaled vicinity; by default it is empty (and initialized)
		ArrayList<Integer> boundingIndexList = new ArrayList<Integer> ();
		
		ArrayList<Double> testrecord ;
		// this provides the index of nodes/particles together with the distance to the
		// node that is located at the center of the queried area
		ArrayList<IndexDistanceIntf> affectedNodesIndexes   ;
		
		
		DataTable dtable = somData.getDataTable();
		f = dtable.getFirstIndexColumnCandidate();
		Map<Double, Integer> ixValMap = dtable.getIndexValueMap() ;
		
		if ((f<0) || (ixValMap==null)){
			return;
		}
		
		minFill = this.parentSom.modelingSettings.getMinimalSplitSize() ;
		
		/*
		 	multi-threading SOM (parallel processing)
		 	
		 	the task that is parallelized is the search through the SOM for a Winner
		 	first we will create an assignment of the nodes to particular
		 *  
		 *   
		 */
		
		
		int currentSomSize = 1 ;
		
		recordsConsidered=0;
		
		while ((recordsConsidered < sampleRecordIDs.size() ) && (parentSom.getUserbreak()==false) ) {
			 
			if (currentSomSize != somLattice.getNodes().size()){
				currentSomSize = somLattice.getNodes().size();
				// update nodes list for MPP
			}
			
			loweredPriorityPause(recordsConsidered);
			
			// drawing the next record ID from the preselected set of IDs by random, NOT one after each other
			if (mode==0){
				currentRecordIndex = getNextRecordId();
			}else{
				currentRecordIndex = sampleRecordIDs.get( recordsConsidered);
			}
			recordsConsidered++;   
											if (displayProgress(3)){
												// display epoch and 10-percentage, and compound of it
												// currentEpoch
												if ((currentEpoch>=1) || (sampleRecordIDs.size()>1200))
												out.printprc(2, recordsConsidered, sampleRecordIDs.size(), ((int)sampleRecordIDs.size()/10), " records visited in epoch "+currentEpoch) ;
											}
											if (currentRecordIndex > dtable.rowcount() ){
												break; // continue;
											}
			
 
			if (currentRecordIndex<0){
				break;
			}
			// the sample index need to be translated into the real record id
			// which we accomplish by means of the values in the index column
			// -> the import of the table has to check whether there is an index column !!!!
			// we created a map (value in index column) -> (row number)
			
			// indexColValue = getIndexValueFromRow( sampleRecordIDs, currentSampleIndex, f) ;
			
			// currentRecordIndex = ixValMap.get(indexColValue) ;
			
			
			
			// get the record at the index position we just determined... 
			// respecting use vector;  (weight vector will be considered later (!) in determining the similarity)
			testrecord = selectPreparePerceptDataRecord( currentRecordIndex, 2); // 2: normalized data
			
						 				if (testrecord == null){
						 					
						 					continue;
						 				}
			// getBestMatchingNodes() is in abstract super() class... ... in producing progress here !!!!!!!
			ClassificationSettings cs = modelingSettings.getClassifySettings();
				
			// TODO: abc124  here we select the appropriate method for determining the Winner ...
			// in case of non-target clustering mode, we will have a variance criterion
			// for instance, learning a property of the SOM itself, like the distance of the record to the
			// class that represents x% of the data. Here the assignment of what is "target" changes all the time
			
			int mppLevel = sfProperties.getMultiProcessingLevel() ;
			// getBestMatchingNodes() should respect minimum fill, if the winner is already filled well 
			winningNodeIndexes = getBestMatchingNodes( currentRecordIndex, testrecord, 5, boundingIndexList,mppLevel);
			                            
				         				if ((winningNodeIndexes==null) || (winningNodeIndexes.size()==0))continue;
				         				out.print(4, "winning node index: "+ winningNodeIndexes.get(0).getIndex() );
		        				
            // set calculateAllVariables = true; for all nodes in the last epoch

		    // we update the winning node without further consideration ONLY in the last step
		    if (((currentEpoch+1)>=somSteps) || (currentEpoch<=1) || (mode>0)){ //  
		    	if (recordsConsidered < sampleRecordIDs.size()/2){ learningRate = 0.04 ; }
if ((currentEpoch+1)>=somSteps){
	int nn;
	nn=0;
}
	
		    	updateWinningNode( winningNodeIndexes, testrecord, currentRecordIndex, learningRate );
			
		    	// this also defines the size of the neighborhood 
		    	int ce = currentEpoch+mode;
		    	ce = Math.min(ce,somSteps);
		    	ce = currentEpoch ;
		    	adoptInfluenceAndReach( sampleRecordIDs.size(), recordsConsidered-1, ce, somSteps ,timeConstant);
		    }

		    
	    	// in the final step we do nothing structural any more ! ...in order to get a stable representation 
		    if (((currentEpoch+1)<somSteps) && (mode==0)){ 
				// ????
				if ((recordsConsidered<4) && (neighbourhoodSize<=1)){ adoptInfluenceAndReach( sampleRecordIDs.size(), recordsConsidered-1, currentEpoch, somSteps ,timeConstant ); }

				// retrieves the list of nodes from the physical ParticleField that are in the vicinity of the winner 
				affectedNodesIndexes = getAffectedNodes( winningNodeIndexes, 1 );
		    	
				winnerIndex = winningNodeIndexes.get(0).getIndex() ;
		    	
				if ((currentEpoch>=1) && (((double)(recordsConsidered-1)/(double)sampleRecordIDs.size())<0.7)){ 
					
			    	int newWinnerIndex = avoidEmptyNodes( winnerIndex, affectedNodesIndexes );
			    	if (newWinnerIndex >=0){
			    		// this will probably exchange the provided testrecord with one that is more dissimilar 
			    		// to the node than the currently tested record... this may help to prevent false positives, increasing the ppv
			    		// this also means that we have to insert the records as based on their similarity, 
			    		// -> structure is not Integer but IndexDistance
			    		testrecord = detailedAssignmentCheck( winnerIndex, newWinnerIndex, testrecord ) ;
			    		winnerIndex = newWinnerIndex ;
			    	}
				}
		    	
		    	if (winnerIndex>=0){
		    		// update here only ...
			    	
			    	// beyond this filling, the profile values may depart from the "mean" values
		    		updateWinningNode( winningNodeIndexes, testrecord, currentRecordIndex, learningRate, minFill);
		    	}
		    	
		    	// this also defines the size of the neighborhood 
		    	adoptInfluenceAndReach( sampleRecordIDs.size(), recordsConsidered-1, currentEpoch, somSteps ,timeConstant );

		    	
		    	// this affects just the profile vectors of the nodes ! 
		    	if ((currentEpoch+1)<somSteps){ // in the last step we do not adjust the vicinity any more... it is just classifying...
		    		 
		    		updateNodesInVicinity( winningNodeIndexes, affectedNodesIndexes, learningRate, testrecord ,recordsConsidered-1); // influence ?
		    	}
		    
		    	learningRate = adjustLearningRate( learningRate, recordsConsidered-1, sampleRecordIDs.size(), currentEpoch, somSteps);
		    	
		    	/* in the epoch N-1 we check for splits, growth according to settings
		    	 * especially, whether adjacent nodes are drastically different filled (<10, >100), (>1000, <30) (<3,>40)
		    	 * but: variance-controlled
		    	 * if a node has strange variance as compared to other nodes (global, local neighborhood,), 
		    	 *   or in targeted modeling, if ppv << average ~~ ppv of node is in 10% quantil of all nodes
		    	 */
		    	if ((currentEpoch+2)==somSteps){
		    		
		    		structuralAdaptation( winningNodeIndexes, affectedNodesIndexes );
		    	}   
		    	affectedNodesIndexes.clear();
		    	winningNodeIndexes.clear();
		    	
		    	affectedNodesIndexes=null;
		    	winningNodeIndexes=null;
		    } // currentEpoch: not the last one ?			
		    
		    if ((recordsConsidered-1<=1) || (recordsConsidered%100==0)){
		    	out.print(4, "epoch: "+currentEpoch+"(of "+(somSteps-1)+"), record "+recordsConsidered+", learnrate: "+String.format("%.3f",learningRate)+
		    			     " ,  nb size: "+neighbourhoodSize);
		    }

											out.print(4,"neighbourhoodSize : "+neighbourhoodSize);
			// recordsConsidered++;

			int tn = Thread.activeCount();
			// out.print(2, "  - - -  active_threads: "+tn);

			 
			if ( (_DEBUG) &&((currentEpoch+1)<somSteps) && (recordsConsidered%100==0)){
				//System.gc();
				out.delay(1);
				out.print(4, "  - - -  active_threads: "+tn);
			}
			if (_DEBUG)out.delay(5); // abc124 DEBUG ONLY
			
		} // recordsConsidered < sampleRecordIDs.size() ->  ...
		
		// now we do sth really expensive: checking the similarity of all records in a node to that node
		// we do it after phase 3 and after 50% of phase 4
		if (currentEpoch+1==somSteps-1){
			// in the pre-last step only...
			// this we can use to adjust the size of the map, by splitting relatively large/heterogeneous nodes,
			// or nodes with small beta (this would require calculating local results...)
			if (mode<=5){
				parentSom.somLattice.calculateInternals();
				// reAssignStrangers(mode); 
				// does not improve, maybe there is a slight stabilizing effect, yet... on a lower level...
				// mainly, because the balance is disturbed...
				// even for very small SOMs!!
			}
		}
		
		parentSom.somLattice.activateNodes(); 
		
		if (parentSom.modelingSettings.getMinimalNodeSize()>0){
			careForMinimalFill( parentSom.modelingSettings.getMinimalNodeSize() ) ;
		}
		 
		 
		
 		if (_DEBUG)out.delay(130);
		 
		 
	} // go()

	private ArrayList<Double> detailedAssignmentCheck( int winnerIndex,int newWinnerIndex, ArrayList<Double> testrecord) {
		// 
		 
		return testrecord;
	}

	private void structuralAdaptation(  ArrayList<IndexDistanceIntf> winningNodeIndexes, 
										ArrayList<IndexDistanceIntf> nodesInVicinity ){
		
		/*
		int somGrowthMode = _SOM_GROWTH_NONE;
	 	int clustermerge = 1 ;
		int clustersplit = 1 ;
		int minimalSplitSize = 15; 
		int intensityForRearrangements = -1 ;
		*/
		// determinables : growth absolute, growth relative per records ( log10(data) * sqrt(log(nodes))? )
		// records per cluster, if ppv<0.98 
		ModelingSettings modset;
		int wNodeIndex;
		
		
		modset = parentSom.modelingSettings ;
		
		wNodeIndex = winningNodeIndexes.get(0).getIndex() ;
		
		// checkForSplit( winningNodeIndexes, nodesInVicinity ) ;
		
	}
	
	
	

	/**
	 * this checks the most dissimilar records in all nodes, and tries to re-allocate them,
	 * if nodes from the the record is removed are small (<30) the extensions are again evaluated for that node
	 * (and profile is adapted anyway on removal, insertion)
	 *  
	 */
	public void reAssignStrangers( int iterationCount) {
		int maxIter=4;
		
		int n,nr=0, ze,z,maxReA=22,
		    leastSimRecIndex,tindex,sindex , ix ;
		double leastSimRecDist,cnodeDist ;
		ArrayList<IndexDistanceIntf>  indexListR;
		IndexedDistances rankedRecords = new IndexedDistances();
		ArrayList<ItemFrequency> rIndexes ;
		IndexDistanceIntf ixd ;
		MetaNode node;
		ExtensionalityDynamicsIntf ext;
		
		ModelingSettings modset;  
		ArrayList<Integer> boundingIndexList = new ArrayList<Integer> ();
		ArrayList<Integer> encounteredRecords = new ArrayList<Integer> ();
		ArrayList<Double> cRecordVector;
		ArrayList<MetaNode> nodes;
		
		ArrayList<IndexDistanceIntf>  candidateNodes ;
		modset = parentSom.getModelingSettings();
		nodes = parentSom.somLattice.getNodes() ;
		
		ItemFrequencies items;
		ItemFrequency fitem;
		FrequencyList frequencyList;
		ArrayList<FrequencyList> reassignments = new ArrayList<FrequencyList>();
		
		try{
			

			for (int i=0;i< nodes.size();i++){

				node = nodes.get(i) ;
				ext = node.getExtensionality();
				
				indexListR = node.getListOfQualifiedIndexes();

				frequencyList = null;
				
				n = indexListR.size() ;
				z=0;ze=0;
				// is the node large enough?
				while ((ze<maxReA) && (ext.getCount() > modset.getMinimalSplitSize()+1 )){
					
					// get the least similar record
					int irx = indexListR.size()-1-z;
					if (irx<=0){
						break;
					}
					ixd  = indexListR.get(irx);
					
					leastSimRecIndex = ixd.getIndex() ;
					leastSimRecDist  = ixd.getDistance() ;
					
					cRecordVector = somData.getRecordByIndex( leastSimRecIndex,2) ; // 2 == record from normalized data
					
					if (encounteredRecords.indexOf(leastSimRecIndex)>=0){
						z++;
						continue;
					}
					
					
					// now the question is: would that cRecordVector match better to some other node ?
					candidateNodes = getBestMatchingNodes( leastSimRecIndex, cRecordVector, 2, boundingIndexList,0);
					
					
					if ((candidateNodes.size()>0)){
						cnodeDist = candidateNodes.get(0).getDistance() ;
						if (cnodeDist < leastSimRecDist * 0.98){
							// YES!!! now we reallocate: stats aware removal, and update of  the found node
							out.print(4, "record found for re-allocation from node <"+leastSimRecIndex+"> to node <"+candidateNodes.get(0).getIndex()+">");

							encounteredRecords.add(leastSimRecIndex);
							tindex = candidateNodes.get(0).getIndex();
							if (tindex==i){ z++; continue;}
							frequencyList = findFrequencyListBySerial( reassignments, tindex);
							ze++;
							nr++;
							
							if (( reassignments.size()==0) || (frequencyList==null)){
								
								frequencyList = new FrequencyList( ((FrequencyListGeneratorIntf)this) ) ;
								frequencyList.setSerialID( tindex ); // target node for record
								frequencyList.setListIndex( i) ;
								reassignments.add(frequencyList) ;	
							}
							
							// "frequencyList" represents a target node
							// "ItemFrequencies" is a wrapper class for the list of items 
							items = frequencyList.getItemFrequencies();
							
							// note that record id's are unique, hance each fitem is unique, hence frequence is not a "frequency" here,
							// we may use it as a container for an index (of the source node in this case) 
							fitem = new ItemFrequency ();
							fitem.observedValue = leastSimRecIndex;
							fitem.frequency = i ;

							items.add(fitem);
							// now we have for each target a list of src nodes, and for each of them a list of record indexes
							
							// we maintain a ranking list, as IndexDistance <recordindex,matching similarity>
							IndexDistance rankRec = new IndexDistance( leastSimRecIndex, cnodeDist ,"");
							rankedRecords.getItems().add(rankRec);
						}
					}
					
					z++;
				} // -> 

				if ((frequencyList!=null) &&(frequencyList.getItemFrequencies().size()>0)){
					// reassignments.add(frequencyList) ;	
				}

			}// i-> all nodes
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		out.print(2,"\n"+nr+" records found for re-allocation ...\n");
		
		try{
			
			// sorting the ranked list
			rankedRecords.sort() ;
			n = rankedRecords.getItems().size() ;
			n = (int) (n*0.212) ;
			if (n<=0){return;}
			while (rankedRecords.getItems().size()>n){
				rankedRecords.getItems().remove(0) ;
			}
			encounteredRecords.clear() ;
			
			for (int i=0;i<reassignments.size();i++){
				
				frequencyList = reassignments.get(i) ;
				
				if (frequencyList==null){
					continue;
				}
				// index of target node
				tindex = (int) frequencyList.getSerialID() ;
				
				// items = frequencyList.getItemFrequencies();
				
				rIndexes = frequencyList.getItemFrequencies().getItems() ;
				
				for (int d=0;d<rIndexes.size();d++){
					fitem = rIndexes.get(d);
					if (fitem!=null){
						ix = (int) fitem.observedValue ;
						sindex = fitem.frequency ;
						
						
						// we do not move it explicitly, since the profile will change dynamically,
						// we just remove it, while the profile and the stats are adapted to the removal
						 
						// we maintain a ranking list for nodes regarding their match dis-quality
						// we take only the lower half of the records for actual reassignment
						// practically, the record ix has to identify itself on the ranking list 
						//
						if (rankedRecords.indexOfIndex(ix)<0){
							continue;
						}
						
						// we collect the records
						encounteredRecords.add(ix) ;
						if (iterationCount==maxIter){
							out.print(2, "removing record "+ix+" from node <"+sindex+">, candidate node would be <"+tindex+">");
						}
						//
						node = nodes.get(sindex) ;
						ext = node.getExtensionality();
						int en1 = ext.getCount(); 
						if (ext.getCount() > modset.getMinimalSplitSize()+1 ){
							node.removeDataAndAdjust( ix, learningRate);
						}
						int en2 = ext.getCount() ;
						en1 = en1-en2;
					}
				}// d -> all records to be moved from sindex to tindex
				 
			} // all reassignment lists
			
			if ((iterationCount<=maxIter) && (encounteredRecords.size()>0)){
				// now we send this sample again to the SOM
				sampleRecordIDs.clear() ;
				sampleRecordIDs.addAll(encounteredRecords) ;
				go( iterationCount+1); // 1=a marker that prevents recursive call
				       // we could use that for controlling iterations
			} 
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		
		
		
	}
	
	
	private FrequencyList findFrequencyListBySerial( ArrayList<FrequencyList> reassignments, int tindex) {

		FrequencyList fL = null;
		
		for (int i=0;i<reassignments.size();i++){
			
			if (reassignments.get(i).getSerialID() == tindex){
				fL = reassignments.get(i);
				break ;
			}
		}
		
		return fL;
	}
	
	 

	protected void careForMinimalFill( int minimalNodeSize ) {
		// 
		int n ;
		ArrayList<Integer> inactivedNodesIndexes = new ArrayList<Integer> ();
		ArrayList<MetaNode>  nodes;

		
		nodes = parentSom.somLattice.getNodes();

		// get the list of nodes as index, together with their count,
		// that are small than the required threshold
		
		for (int i=0;i<nodes.size();i++){
			
			n = nodes.get(i).getExtensionality().getCount() ;
			if ((n>0) && (n<minimalNodeSize)){
				// send records to other nodes in the lattice;
				// 1. mark this node as inactive
				nodes.get(i).setActivation(-1) ;
				// 2. get indexes of record, 
				
				// send them to the lattice
				
			}
			
		} // i-> all nodes
		
		// activate all nodes
		for (int i=0;i<inactivedNodesIndexes.size();i++){
			// ix = inactivedNodesIndexes.get(i) ;
			nodes.get(i).setActivation(1) ;
		}
	}

	
	private int avoidEmptyNodes( int winnerIndex, ArrayList<IndexDistanceIntf> indexedDistances ){

		int ix,sz,szw,  newWinnerIndex = -1;
		ArrayList<IndexDistanceIntf> extSizeInNeighbors;
		ArrayList<Integer> nodeList = new ArrayList<Integer>();
		MetaNodeIntf node ;
		
		if (currentEpoch >= 1) {
			
			node = parentSom.somLattice.getNode(winnerIndex) ;
			szw = node.getExtensionality().getCount() ;
			
			
			if (szw < parentSom.modelingSettings.getMinimalSplitSize() ){ // .getMinimalNodeSize()
				// if the winner node is itself still too small, we return without further action
				return winnerIndex;
			}
			
			int nn = 9;
			if (indexedDistances.size() < nn) {
				nn = indexedDistances.size();
			}
			extSizeInNeighbors = new ArrayList<IndexDistanceIntf>();
			
			for (int n = 0; n < nn; n++) {
				// if it is an active node?
				ix = indexedDistances.get(n).getIndex() ;
				node = parentSom.somLattice.getNode(ix) ;
				// if (node.getActivation()>=0)
				{
					sz = node.getExtensionality().getCount() ;
					if (sz==0){
						extSizeInNeighbors.add( indexedDistances.get(n) );
					}
				}
				
			} // n -> all nodes in vicinity

			if (extSizeInNeighbors.size()==0){
				return winnerIndex;
			}
			
if (extSizeInNeighbors.size()>0){
	out.print(4, "empty nodes around winner (index:"+winnerIndex+"), e="+extSizeInNeighbors.size());
	
}
			// index distances should be sorted
			for (int i=0;i<extSizeInNeighbors.size();i++){
				ix = extSizeInNeighbors.get(i).getIndex() ;
				nodeList.add(ix) ;
			} // i-> all nodes as IndexDistance (index,spatial distance) that are still empty
			// get most similar node to this list of nodes as given by their indexes ;
			
			// getMostSimilarNodes( , nodeList);
			//
			// else: probably not the record to be added should be put the new node, but the record
			// which is the most dissimilar one
			
			ix = extSizeInNeighbors.get(0).getIndex() ;
			node = parentSom.somLattice.getNode(ix) ;
				
			// if there are several, we should take the most similar, such it would fit
			// better into the topology of the map
			newWinnerIndex = ix;
				 
			
		} // (currentEpoch>=1 ?

		return newWinnerIndex;
	}
	
	
	

	private int getNextRecordId() {
	
		int  _rs, err=1,  _arr_pos = 0, _next_record = 0;
		
		// we draw a random number [0 .. length of the array _row_IDs], take
		// that as a result
		// and remove the respective record
		// only in the beginning the position is equal to the content, later it
		// is not
		// sync'd anymore!! thus we have to ask for the content of the array
		
		try {
		
															err = 1;
		     _rs = volatileSample.size();
		     												err = 2;
		     if (_rs > 0) {
		    	 											err = 3;
		    	 _arr_pos = parentSom.getRandom().nextInt(_rs);
	             											 
	             							out.print(4,"random int : " + String.valueOf(_arr_pos)); //
	             											 
		    	 											err = 4;
		    	 if (_arr_pos >= 0) {
		    		 										err = 5;
		    		
		    												err = 6;
	
		    		_next_record = volatileSample.get(_arr_pos) ;
	          	   	
		    		volatileSample.remove(_arr_pos);
		    	 }
		     } else {
		    	 _next_record = -1;
		                               out.print(2, "problem in getNextRecordId (sample body is empty), err =" + String.valueOf(err) + 
		                            		   		" , _rs " + String.valueOf(_rs) + "  , _arr_pos " + String.valueOf(_arr_pos));
		     }
		
		     												err = 6;
		     volatileSample.trimToSize() ;		     err=0;
		}
		finally {
			if (err>0){
				System.out.println("\nProblem in determining _next_record : " + String.valueOf(err)+"\n");
			}
		}
		return _next_record;
	}
	
	
	private double adjustLearningRate( double learningrate, int recordsConsidered, int recordCount, int currentepoch, int somsteps) {
		// 
		double _LF = 1,  _learnrate, _learningrate;
		

		// describing the scaling dynamics of the learning rate
		if (learningRateFixationmode == 1) {
			_LF = ((double)(1+recordsConsidered) / (double)recordCount);
			_LF = (double) (0.5 + Math.sqrt(_LF) / 2.0);
			_LF = 1 / _LF;
		
		// _Lf<0 , thus sqrt grows more for small values <0.5
		
		}
		
		if (learningRateFixationmode == 2) {
			_LF = ((double)(1.0*(1+recordsConsidered)) / (double)(1.0*recordCount));
			_LF = (double) ((1 + Math.log(1.0 + _LF)) / 2.0);
			_LF = ((1.0 /_LF)-1.5)*2.0; // intervall [0.07..0.9999] , 1.0-> 0.07
			// records+ -> smaller down to 1 
		}
		constStartLearningRate = 1.0;
		/*
		double _v = (double) ( constStartLearningRate * Math.exp(-((double)(2+currentepoch) / ((double)somsteps + 3.0)) * _LF));
		double _v2 = ( // Math.log10
	               			((double)(1 +  Math.log( 1.0 + _LF  ) )
	               		    )
	               		);
		*/
		double steepness = 0.4 ; 
					// 0.4   -> [0.065 .. 0.007] : ratio= 8.7
        			//  0    -> [0.084 .. 0.027] : ratio= 3.1
        			// -0.4  -> [0.105 .. 0.047] : ratio= 2.2
		
		_learnrate = (Math.log( 1+1/(constStartLearningRate * Math.exp( -(3.2  * _LF ) ) / 2.0) ) - steepness)/20 ;
		
																			         //    Math.log10
		                                                                        //       _LF steepness of dependency on ratio recordnum/recordcount
		_learningrate = 0.005+(_learnrate + 0.02)/3.0;   
		
		return   _learningrate;
		 
	}
 
	private void updateNodesInVicinity( ArrayList<IndexDistanceIntf> winningNodeIndexes, 
										ArrayList<IndexDistanceIntf> nodesInVicinity,
										double learningrate, 
										ArrayList<Double> datarecord ,
										int recordsConsidered ) {

		double  nodeDistance, normalizedDistance, influence, sizeFactor=1.0;
		int nodeIndex,wNodeIndex, kn  ;
		MetaNodeIntf node ;
		boolean closeVicinity;
		
		ArrayList<IndexDistanceIntf> nodesPtr= nodesInVicinity;
		 
		
		if (nodesPtr.size()<1){
			return;
		}
		
		nodeIndex = nodesPtr.get(0).getIndex() ;
		node = somLattice.getNode(nodeIndex);
		// int ixPosition = node.getSimilarity().getIndexIdColumn() ;
		// int k=this.currentEpoch ;
		try{
			// the nodelist comes in a sorted state
			// maxDistInSample = nodesPtr.get(nodesPtr.size()-1).getDistance() ;
			
			// 
			kn = nodesPtr.size();
			if (kn > neighbourhoodSize){
				kn = neighbourhoodSize;
			}
			if (kn<1)kn=1;
			// if (kn>6){kn6=6;} else{kn6=kn;}
			
			wNodeIndex = winningNodeIndexes.get(0).getIndex() ;
			
			// out.print(2, true, "") ;
			
			// we do not update the winner, which is at position 0 of the nodelist 
			for (int i=1;i<kn;i++){ // index 0 
				
				closeVicinity = (i<=6);
									
				nodeIndex = nodesPtr.get(i).getIndex() ;
				nodeDistance =nodesPtr.get(i).getDistance() ; 
				normalizedDistance = nodeDistance/ somLattice.getAveragePhysicalDistance() ;
				
				node = somLattice.getNode(nodeIndex);
				
				node.getIntensionality().prepareWeightVector();
				
				influence = getInfluenceforDistance( (normalizedDistance*normalizedDistance), 1) ;
				
				node.setContentSensitiveInfluence( parentSom.modelingSettings.getContentSensitiveInfluence() );
				
				                 // this is normalized data
				node.adjustProfile( datarecord, nodeIndex, learningrate, influence, sizeFactor, 0);// contrast_enh, 
				
				                            if ((currentEpoch==2) && ( recordsConsidered % 50==0))
				              				out.print(4, "winner ix "+wNodeIndex+
				              						     ", distance "+String.format("%.2f",nodeDistance)+
				              						     ", influence " +String.format("%.2f",influence) ) ;
				if (closeVicinity){
					
				}
				
				// node.getExtensionality().addRecordByIndex( (int)Math.round(datarecord.get(ixPosition)) );
				
			} // i->
			
		}catch(Exception e){
			
		}
		 
	}
	
	
	/** 
	 * this returns a list of indexed distances; </br> 
	 * (collection of pairs (nodeindex, spatial distance) NOT semantic vector distance!!  </br></br>
	 * 
	 * since this invokes the (possibly external) RepulsionField, this query runs encapsulated in an object, 
	 * 
	 * TODO : in case of multiple winners, the return type needs to be an ArrayList of ArrayList of IndexDistanceIntf !!
	 *        since each returned node could be influenced by any of the winners
	 */
	private ArrayList<IndexDistanceIntf> getAffectedNodes( ArrayList<IndexDistanceIntf> winningNodeIndexes, int winnersCount) {
		
		int nodeIndex;
		ArrayList<IndexDistanceIntf> nodelist = new ArrayList<IndexDistanceIntf>();
		
		
		// TODO: allow multiple winners, then create a union of the sets, ...for now, we allow only 1 winner
		winnersCount = 1;
		if ( winnersCount > winningNodeIndexes.size()){
			winnersCount = winningNodeIndexes.size();
		}
		// multiple centers are handled in the query object, 
		// the distances to the winner nodes then is calculated for any of the selected node;
		// we need this info for determining the influence
		 
		// the object waits for the returned list
		// the object needs a callback up to SomFluid, which in turn collects the events from RepulsionField
		
		while (parentSom.somLattice.getLatticeQuery()>0){ // just for DEBUG ONLY
			out.delay(10) ;
		}
		// the query can be performed through the virtual lattice
		// selection results arrive in "SomFluid.onSelectionRequestCompleted ()" and 
		// are sent finally to "digestParticleSelection()" via FiFo msg queue (for decoupling)
		nodeIndex = winningNodeIndexes.get(0).getIndex() ;
		
		
		// this performs the query, waits for the results and filters the results according to current radius...
		nodelist = parentSom.somLattice.getNeighborhoodNodes( nodeIndex , neighbourhoodSize ) ;
		 
		
		// the lattice performs the call and organizes the wait by itself, -> no complicated callbacks to here...
		// it wraps the call through the SomFluid object : parentSom.somFluidParent.getNeighborhoodNodes( index );
											out.print(4, "nodes selected  n = "+nodelist.size()) ;
		int restrictedSize = parentSom.getModelingSettings().getRestrictionForSelectionSize() ;									
		if ((restrictedSize>3) && ( nodelist.size() > restrictedSize )){
			// we remove particles: the list is ordered according to spatial distance, so we simply can remove the
			// elements from the end of the list
			while (nodelist.size() > restrictedSize ){
				nodelist.remove(restrictedSize);
			}
		}
			
		if (nodelist.size()>neighbourhoodSize){
			// nodelist = (ArrayList<IndexDistanceIntf>) nodelist.subList(0,nodelist.size()-1) ;
			if ( nodelist.size()>neighbourhoodSize) {
				for (int i=neighbourhoodSize;i<nodelist.size();i++){
					// index not correct yet ...
					if (nodelist.size()>=neighbourhoodSize){
						nodelist.remove(neighbourhoodSize);          
					}
				}
			}
		}
		return nodelist;
	}

	/**
	 * 
	 * will take into consideration:
	 * - variance
	 * - ppv or variance in tv variable 
	 * - ECR
	 * 
	 * @param winningNodeIndexes
	 * @param nodesInVicinity
	 */
	@SuppressWarnings("unused")
	private void checkForSplit(  ArrayList<IndexDistanceIntf> winningNodeIndexes, 
										ArrayList<IndexDistanceIntf> nodesInVicinity ) {
		ModelingSettings modset;
		
		int nodeIndex,wNodeIndex, nec,crn,k,kn, kn6, bmuExtensionSize=0 ;
		MetaNodeIntf node ;
		boolean closeVicinity;
		ArrayList<Integer> emptyNeighbors = new ArrayList<Integer>();
		ArrayList<IndexDistanceIntf> nodesPtr= nodesInVicinity;
		 
		
		modset = parentSom.modelingSettings ;
		
		wNodeIndex = winningNodeIndexes.get(0).getIndex() ;
		
		kn = nodesPtr.size();
		if (kn > neighbourhoodSize){
			kn = neighbourhoodSize;
		}
		if (kn<1)kn=1;
		if (kn>6){kn6=6;} else{kn6=kn;}
		
		
		// first we count the empty nodes around the winning BMU, but only, if the BMU is larger than 10; 
		bmuExtensionSize = somLattice.getNode(wNodeIndex).getExtensionality().getStatistics().getFieldValues().get(1).getCount() ;
		// the same value: int n = somLattice.getNode(wNodeIndex).getExtensionality().getCount() ;
		nec=0;
		 
		if (bmuExtensionSize>10){
			for (int i = 1; i < kn6; i++) { // index 0
				nodeIndex = nodesPtr.get(i).getIndex() ;
				crn = somLattice.getNode(nodeIndex).getExtensionality().getStatistics().getFieldValues().get(1).getCount() ;
				
				if (crn==0){
					nec++;
					emptyNeighbors.add( nodeIndex ) ;
				}
				
			}
		} // bmuExtensionSize>10
		
		// clustermerge; getClustersplit() {
		
		
		if ((parentSom.getModelingSettings().splitNodes()) && (emptyNeighbors.size()>0) && (currentEpoch<=2)){
			// careForCoverage( recordsConsidered, sampleRecordIDs.size(), currentEpoch, somSteps,0 );
			splitNode( wNodeIndex, emptyNeighbors, 1, MetaNodeIntf._NODE_SPLITMODE_MINIMAL ) ; 
		}
		
		if (parentSom.getModelingSettings().mergeNodes() ){
			
		}
		
		
	}
	protected void splitNode(int wNodeIndex, int newNodes ) {
		// 
		
	}

	/**
	 * 
	 * if emptyNeighbors contains a node that is empty, the node addressed by srcNodeIndex will 
	 * transfer some records 
	 * 
	 * @param wNodeIndex
	 * @param emptyNeighbors
	 * @param numberOfSplits
	 */
	protected void splitNode(int srcNodeIndex, ArrayList<Integer> emptyNeighbors, int numberOfSplits, int splitmode ) {
		// 
		ArrayList<IndexDistanceIntf> sortedNeighbors = new ArrayList<IndexDistanceIntf> ();
		ArrayList<Integer> boundingIndexList = new ArrayList<Integer>();
		ArrayList<MetaNode> nodeCollection = new ArrayList<MetaNode>(); 
		
		ProfileVectorMatcher bmuSearch;
		
		ArrayList<Integer> exportedRecordIndexes = new ArrayList<Integer> () ;
		
		MetaNode  node, bmnNode;
		ArrayList<Double> profilevalues;
		
		int  ix, bmuCount, bestMatchingNeighborIndex;
		
		int a=1;
		if (a==2){
			return;
		}
		
		bmuCount = emptyNeighbors.size();
		if (bmuCount<=1){
			return;
		}
		try {
			
			for (int n = 0; n < emptyNeighbors.size(); n++) {
				
				node = somLattice.getNode( emptyNeighbors.get(n) );
			 
				nodeCollection.add(node);
			}
			
			profilevalues = somLattice.getNode(srcNodeIndex).getIntensionality().getProfileVector().getValues();
			
			bmuSearch = new ProfileVectorMatcher( parentSom.sfProperties.getMultiProcessingLevel(),out);
			bmuSearch.setNodeCollection( nodeCollection).setParameters(profilevalues, bmuCount, boundingIndexList);
			
			bmuSearch.createListOfMatchingUnits(1);
			
			sortedNeighbors = bmuSearch.getList( -1 ) ; // correctly sorted ?
			
			if (sortedNeighbors.size()==0){
				return;
			}
			// the best matching node within this collection is on pos 0, 
			// yet the index reported from this position refers to emptyNeighbors not to the somLattice...
			bestMatchingNeighborIndex = sortedNeighbors.get(0).getIndex() ;
			// ... thus we have to translate back...
			ix = emptyNeighbors.get( bestMatchingNeighborIndex ) ;
			// and to get the node through the translated index
			bmnNode = somLattice.getNode( ix ) ;
			 
			// next, remove some records from the source node into a local collection,
			// either a minimal set of 3 records, or balanced (taking into consideration total variance
			
			if (splitmode == MetaNodeIntf._NODE_SPLITMODE_MINIMAL){
if (currentEpoch>=2){
	a=0;
}
				// those records that are least similar to the profile will be exported, the removal will adapt the basic statistics 
				// 3 = the number of records to be transferred, 
				// flag = whether to remove the exported indexes; if yes, the statistics will be adapted
				node = somLattice.getNode( srcNodeIndex ) ;
				exportedRecordIndexes = node.exportDataFromNode( 3 , -1, true) ; // -1 = least similar ones
			} 
			
			if (splitmode == MetaNodeIntf._NODE_SPLITMODE_BALANCED){
				// the allowed portion of records to be transferred
				// the removal will adapt the basic statistics
				exportedRecordIndexes = bmnNode.exportDataFromNode( 0.19, 0.61, -1,true) ;
			}
			
			bmnNode.importDataByIndex( exportedRecordIndexes ) ;
			
			// and now , from all nodes in the direct neighborhood of this freshly filled node,
			// we take just 1 record: the one that matches the profile of bmnNode most
			int n = bmnNode.getExtensionality().getCount() ;
			
			n= n+1-1;
		}catch(Exception e){
			e.printStackTrace();
		}
	
		
	}
	

	

	/**
	 * 
	 * @param actualRecordCount respective to the actual epoch after preparing the sample, which is much smaller for the first epochs
	 * @param recordsConsidered
	 * @param timeConstant
	 * @param neighbourhoodDecay
	 */
	private void adoptInfluenceAndReach( int actualRecordCount, int recordsConsidered, int currentEpoch, int steps, double timeConstant ) {
		 
		double _sf,rp, sr,_Nv,  rc, _f,_fx, speed=1.0;
		int size;
		
		if (mapRadius<=0.01){
			mapRadius = parentSom.getdSomCore().getMapRadius(); 
		};
		
		rc = actualRecordCount;
		size = somLattice.size();
		
		mapRadius = Math.sqrt((double)size)*0.4 ;
			
if (this.currentEpoch==2){
	_f=0.0;
}
	
		_sf=1.1;
		if (size>=6){
			_sf = Math.sqrt(size) + 0.7*size/(Math.log10(size)) ;
		}
		
		// _Nv = (float) (Math.round(mapRadius * 0.92) * Math.exp(-(neighbourhoodDecay * (1.1 * Math.log10(1+1*_sf*Math.log(size)))*((recordsConsidered + 8*steps)/rc) / timeConstant)));
	
		sr = ((double)steps/(double)(0.7 + currentEpoch));
		
		rp = ((double)recordsConsidered + (8.0*(1+currentEpoch)))/(double)(rc+ (8.0*steps))  ;
		rp = (rp * Math.sqrt(rp) )/ ( Math.log(timeConstant) * sr*sr * speed) ; // Math.log(timeConstant) 
		
		_f =   (1.1f * Math.log10(1.0f+1.0f*_sf*Math.log( size)))*(rp);
		
		_fx =   Math.exp(-(neighbourhoodDecay *2.7* _f)) ;
		_Nv =  (Math.round( mapRadius * 0.92)) * _fx;
											
		
		neighbourhoodRadius =  _Nv;
									    
	    									
	    // double  ad =  somLattice.getAveragePhysicalDistance() ;
	    
	    neighbourhoodSize =  (int) ((neighbourhoodRadius*neighbourhoodRadius) * Math.PI )  ;
	    if (neighbourhoodSize>(double)size*0.46)neighbourhoodSize = (int)Math.round((double)size*0.46);
	    
	    								if (recordsConsidered>176){
	    									out.printErr(4, "calculating neighborhood : _f="+String.format("%.4f",_f)+
	    													"  _fx="+String.format("%.4f",_fx) +"  ");
	    
	    									if (recordsConsidered%10==0)
	    									{
	    				out.print(5, " --- Record # "+recordsConsidered+"   nb radius = "+String.format("%.3f",_Nv)+
	    							 "   timeConstant = "+String.format("%.3f",timeConstant)+
	    							 ",  rp = "+String.format("%.3f",rp) +
	    							 ",  f  = "+String.format("%.3f",_f) +
	    							 ",  fx = "+ String.format("%.3f",_fx) +
	    							 ",  size = "+neighbourhoodSize) ;
	    									}
	    									rc=0;
	    								}
	    								
	     if (currentEpoch+1==steps){
	    	 neighbourhoodSize = (int) (neighbourhoodSize * 0.4) ;
	     }
	    									
	    				
	}

	private ArrayList<Double> selectPreparePerceptDataRecord( int iindex , int rawOrNorm) {
	 
		ArrayList<Double> rowData;
		
		if (rawOrNorm<=1){
			rowData = parentSom.somData.getDataTable().getDataTableRow(iindex);
		}else{
			rowData = parentSom.somData.getNormalizedDataTable().getDataTableRow(iindex);
		}
			
		return rowData;
	}
	
	private void loweredPriorityPause(int ix) {
		
		if (parentSom.loweredPriority == true) {
			if ((ix % 10) == 0) {
				parentSom.out.delay(5);
			}
		}
	}

	
	private double getInfluenceforDistance(double DistToNodeSqr, int mode) {

		int return_value = -1;

		double WidthSq = 1, _v, mapwidth;
		double influence = 0.0;

		try {

			// mode = 0;

			mapwidth = 1.2 * Math.sqrt( somLattice.size() );
			
			WidthSq = neighbourhoodRadius * neighbourhoodRadius;

			influence = (double) Math.exp(-(DistToNodeSqr) / (0.001));

			if (WidthSq <= 0) {
				return return_value;
			}

			if (mode == 0) {
				influence = (float) Math.exp(-(2 * DistToNodeSqr)/ (2 * WidthSq));
						
			}
			if (mode == 1) {
				influence = (double) Math.exp(-(2.0 * (DistToNodeSqr) * Math.log((double)mapwidth)) / (2.0 * (double)WidthSq));

				if ((neighbourhoodRadius > 10) && (DistToNodeSqr / neighbourhoodRadius > 0.69)) {
						
					influence = influence * (1.01 - (DistToNodeSqr / neighbourhoodRadius));
				}
			}

			if (mode == 2) {
				influence = (float) Math.exp(-(1.6 * Math.sqrt(DistToNodeSqr) * Math.log(mapwidth)) / (2 * WidthSq));
						

				if ( (neighbourhoodRadius > 10) && (DistToNodeSqr / neighbourhoodRadius > 0.4)) { 
					
					influence = influence * (1.01 - (DistToNodeSqr / neighbourhoodRadius));
				}
				if ( (neighbourhoodRadius > 3) && (DistToNodeSqr >= 3) && (DistToNodeSqr / neighbourhoodRadius > 0.3)) {
					 

					_v = Math.exp(-(1 / (1 - (DistToNodeSqr / neighbourhoodRadius))));
					influence = influence * _v;
				}
			}

			if (mode == 3) {
				influence = (double) Math.exp(-(2 * DistToNodeSqr * Math.log(mapwidth * Math.sqrt(mapwidth))) / (2 * WidthSq));
			}
			 
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

		return influence;
	}
	
	private void updateWinningNode(	ArrayList<IndexDistanceIntf> winningNodeIndexes, 
									ArrayList<Double> dataNewRecord,
									int recordIndexInTable, 
									double currentLearningrate) {
		
		updateWinningNode(	winningNodeIndexes, dataNewRecord,recordIndexInTable, currentLearningrate, -1);
	}
	
	private void updateWinningNode(	ArrayList<IndexDistanceIntf> winningNodeIndexes, 
									ArrayList<Double> dataNewRecord,
									int recordIndexInTable, 
									double currentLearningrate,
									int fillingLimitForMeanStyle) {
		
		int nodeindex = -1,   wni ,maxWni ;
		MetaNodeIntf node;
		double relativeInfluence=0.3, sizeFactor=1.0;
	 
		
		try{
			 
			// restricting the number of winners to take into consideration
			wni = winningNodeIndexes.size();
			maxWni = this.parentSom.getModelingSettings().getWinningNodesCount() ;
			if (maxWni<=0){
				maxWni=1;
			}
			if (wni > maxWni){
				wni = maxWni;
			}
			
			
			for (int i=0;i<wni;i++){
				
				nodeindex = winningNodeIndexes.get(i).getIndex() ;
				node = somLattice.getNode(nodeindex) ;	
				
				if (i<=0){
					int iwn = i;
					if (this.currentEpoch+1==this.somSteps)iwn=-1;
											if ((i<=1) && (currentEpoch==0)){
												//
												/*
												out.print(2, "somLattice address : "+somLattice.toString()) ;
												ArrayList<Double>  uv = node.getIntensionality().getUsageIndicationVector() ;
												String str = ArrUtilities.arr2Text(uv, 1) ;
												out.printErr(2, ">>>   node UIV : "+str) ;
												*/
											}
					// ... calculate new profile, calculate new variance, CoV
					node.insertDataAndAdjust( dataNewRecord, recordIndexInTable, 
											  iwn,currentLearningrate, 
											  fillingLimitForMeanStyle );
					break;
				}else{
					// no data insertion, but only adopting the profile in an as-if mode
					// note, that also the vicinity of those secondary winners will be updated!
					
					// for normal updates of profiles, influence is determined by getInfluenceforDistance() in dependency to 
					// the distance to the winner, here, we reduce the influence of the virtual insert to a portion of the
					// influence, a standard insert would have, e.g. 0.3
					node.adjustProfile( dataNewRecord, nodeindex, currentLearningrate, relativeInfluence, sizeFactor, i);
				}
				 
				
				// if ((i>=parentSom.modelingSettings.getWinningNodesCount()) || (i>=0)){ break;  }
			}// i-> all winningNodeIndexes
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	
	private boolean displayProgress(int level) {
		boolean rB=false;
		
		if (level<=0){
			rB = (sfProperties.getShowSomProgressMode()>= SomFluidProperties._SOMDISPLAY_PROGRESS_NONE) ;
		}
		if (level==1){
			rB = (sfProperties.getShowSomProgressMode()>= SomFluidProperties._SOMDISPLAY_PROGRESS_BASIC) ;
		}
		if (level==2){
			rB = (sfProperties.getShowSomProgressMode()>= SomFluidProperties._SOMDISPLAY_PROGRESS_STEPS) ;
		}
		if (level>=3){
			rB = (sfProperties.getShowSomProgressMode()> SomFluidProperties._SOMDISPLAY_PROGRESS_PERC) ;
		}
		
		
		
		return rB;
	}
	

}
