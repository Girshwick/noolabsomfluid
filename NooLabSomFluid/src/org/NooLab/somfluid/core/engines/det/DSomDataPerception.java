package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;
import java.util.Map;

import org.NooLab.utilities.logging.PrintLog;

import org.NooLab.repulsive.components.data.IndexDistance;
import org.NooLab.repulsive.components.data.IndexDistanceIntf;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.DataTableCol;
import org.NooLab.somfluid.properties.ModelingSettings;

 

public class DSomDataPerception {

	boolean _DEBUG = true;
	
	
	DSom           parentSom;
	SomDataObject  somData;
	VirtualLattice somLattice;
	
	ArrayList<Integer> sampleRecordIDs, volatileSample;
	
	
	int       currentEpoch, somSteps, neighbourhoodSize=1 ;
	double    mapRadius, neighbourhoodRadius;
	double    learningRate, timeConstant, neighbourhoodDecay = 1.45 ;
	
	double    constStartLearningRate = 0.25 ;
	int       learningRateFixationmode = 2 ;
	
	PrintLog out;
	
	// ========================================================================
	public DSomDataPerception( DSom dsom , ArrayList<Integer> sampleRecords) {
		 
		parentSom = dsom ;
		
		somData = dsom.somData ;
		
		sampleRecordIDs = new ArrayList<Integer>(sampleRecords);
		volatileSample  = new ArrayList<Integer>(sampleRecords);
		
		somLattice = parentSom.somLattice;
		
		out = dsom.out ;
	}
	// ========================================================================	

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
	
	// here we feed the data into the SOM 
	public void go( ) {
		 
		int recordsConsidered =0, currentRecordIndex, currentSampleIndex;
		int err=1, f, winnerIndex;
		
		double indexColValue;
		ArrayList<IndexDistanceIntf>  winningNodeIndexes , neighborhoodIndexes;
		
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
		recordsConsidered=0;
		
		while ((recordsConsidered < sampleRecordIDs.size() ) && (parentSom.getUserbreak()==false) ) {
			err = 2;
			loweredPriorityPause(recordsConsidered);
			
			// drawing the next record ID from the preselected set of IDs by random, NOT one after each other
			currentRecordIndex = getNextRecordId();
			
											if (currentRecordIndex > dtable.rowcount() ){
												continue;
											}
			
 
			if (currentRecordIndex<0){
				break;
			}
			// the sample index need to be translated into the real record id
			// which we accomplish by means of the values in the index column
			// -> the import of the table has to check whther there is an index column !!!!
			// we created a map (value in index column) -> (row number)
			
			// indexColValue = getIndexValueFromRow( sampleRecordIDs, currentSampleIndex, f) ;
			
			// currentRecordIndex = ixValMap.get(indexColValue) ;
			
			
			
			// get the record at the index position we just determined... 
			// respecting use vector;  (weight vector will be considered later (!) in determining the similarity)
			testrecord = selectPreparePerceptDataRecord( currentRecordIndex, 2); // 2: normalized data
						 				if (testrecord == null)continue;
						 
			winningNodeIndexes = getBestMatchingNodes( currentRecordIndex, testrecord, 5, boundingIndexList);
			
				         				if ((winningNodeIndexes==null) || (winningNodeIndexes.size()==0))continue;
				         				out.print(3, "winning node index: "+ winningNodeIndexes.get(0).getIndex() );
		    int k=0;     				
            // set calculateAllVariables = true; for all nodes in the last epoch

		    // we update the winning node without further consideration ONLY in the last step
		    if (((currentEpoch+1)>=somSteps) || (currentEpoch<=1)){
		    	
		    	updateWinningNode( winningNodeIndexes, testrecord, currentRecordIndex, learningRate );
			
		    	// this also defines the size of the neighborhood 
		    	adoptInfluenceAndReach( sampleRecordIDs.size(), recordsConsidered, somSteps ,timeConstant);
		    }
	    	
	    	// in the final step we do nothing structural any more 
		    if ((currentEpoch+1)<somSteps){ 
if (currentEpoch>1){
	int kk;
	kk=0;
}
		    	// dependent on option control and learning epoch, we may allow for multiple "winners"
	            /* - variant 1: the insertion actually happens only on 1 place, but the other ones
	             *              get strongly updated in an "as-if" manner, dependent on similarity to intensional description;
	             *              occasionally, this may lead to sudden shifts
	             *              requirement: different way to calculate the profile, i.e. not
	             *              strictly as the mean 
	             * - variant 2: the record gets inserted  
	             */
				
				   // ... 	
				if ((recordsConsidered<4) && (neighbourhoodSize<=1)){ adoptInfluenceAndReach( sampleRecordIDs.size(), recordsConsidered, somSteps ,timeConstant ); }

				// still: does not return correctly sized samples... as if in hex mode...
				affectedNodesIndexes = getAffectedNodes( winningNodeIndexes, 1 );
		    	
				winnerIndex = winningNodeIndexes.get(0).getIndex() ;
		    	
		    	winnerIndex = avoidEmptyNodes( winnerIndex, affectedNodesIndexes );

		    	if (winnerIndex<0){
			    	int minFill = this.parentSom.modelingSettings.getMinimalSplitSize() ;
			    	// beyond this filling, the profile values may depart from the "mean" values
		    		updateWinningNode( winningNodeIndexes, testrecord, currentRecordIndex, learningRate, minFill);
		    	}
		    	
		    	// this also defines the size of the neighborhood 
		    	adoptInfluenceAndReach( sampleRecordIDs.size(), recordsConsidered, somSteps ,timeConstant );

		    	
		    	// this affects just the profile vectors of the nodes ! 
		    	if ((currentEpoch+1)<somSteps){ // in the last step we do not adjust the vicinity any more... it is just classifying...
		    		 
		    		updateNodesInVicinity( winningNodeIndexes, affectedNodesIndexes, learningRate, testrecord ,recordsConsidered); // influence ?
		    	}
		    
		    	learningRate = adjustLearningRate( learningRate, recordsConsidered, sampleRecordIDs.size(), currentEpoch, somSteps);
		    	
		    	/* in the epoch N-1 we check for splits, growth according to settings
		    	 * especially, whether adjacent nodes are drastically different filled (<10, >100), (>1000, <30) (<3,>40)
		    	 * but: variance-controlled
		    	 * if a node has strange variance as compared to other nodes (global, local neighbourhood,), 
		    	 *   or in targeted modeling, if ppv << average ~~ ppv of node is in 10% quantil of all nodes
		    	 */
		    	if ((currentEpoch+2)==somSteps){
		    		
		    		structuralAdaptation( winningNodeIndexes, affectedNodesIndexes );
		    	}   
		    	
		    } // currentEpoch: not the last one ?			
		    
		    if ((recordsConsidered<=1) || (recordsConsidered%100==0)){
		    	out.print(3, "epoch: "+currentEpoch+"(of "+(somSteps-1)+"), record "+recordsConsidered+", learnrate: "+String.format("%.2f",learningRate)+
		    			     " ,  nb size: "+neighbourhoodSize);
		    }

											out.print(3,"neighbourhoodSize : "+neighbourhoodSize);
			recordsConsidered++;

			int tn = Thread.activeCount();
			// out.print(2, "  - - -  active_threads: "+tn);

			 
			if ( (_DEBUG) &&((currentEpoch+1)<somSteps) && (recordsConsidered%100==0)){
				//System.gc();
				out.delay(10);
				out.print(4, "  - - -  active_threads: "+tn);
			}
			if (_DEBUG)out.delay(5); // abc124 DEBUG ONLY
		} // -> 
		
		 
		if (_DEBUG)out.delay(130);
		careForCoverage( recordsConsidered, sampleRecordIDs.size(), currentEpoch, somSteps,1 );
		err=0;
	} // go()

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
		
		checkForSplit( winningNodeIndexes, nodesInVicinity ) ;
		
		
		
		
	}
	
	
	// depending on the data, some nodes may develop into honey pots, collecting
	private void careForCoverage(int recordsconsidered, int size, int epoch, int steps, int enforce ) {
		
		boolean performEqualizer;
		
		
		performEqualizer = (  ((currentEpoch==0) && (recordsconsidered % 10 ==0)) ) || (enforce>0);
			
		
		if (performEqualizer){
			// TODO : make this a class ...
			
			
			
			// set selection size to 6
			// this.parentSom.sfFactory.getPhysicalField().setSelectionSize(6);
			// TODO: check whether this setting destroy the buffer in RF... (should not)
			
			for (int  i=0;i<somLattice.size();i++){
				
				// has this node an empty node in its vicinity ?
				// -> get the surrounding, restrict it to 6 nodes
				
				
				// check whether any of them is empty, get the one with largest size
				// how many of the neighbors are empty? 
				
				
				// remember the node which we have treated by shuffling records into it
				// we should not tuch it any mode (...well, it is not empty any more)
				
			}// i-> all nodes
			
			
		} // performEqualizer
		
	}
	
	private int avoidEmptyNodes( int winnerIndex, ArrayList<IndexDistanceIntf> indexedDistances ){

		int ix,sz,  newWinnerIndex = -1;
		ArrayList<IndexDistanceIntf> extSizeInNeighbors;
		MetaNodeIntf node ;
		
		if (currentEpoch >= 1) {
			
			node = parentSom.somLattice.getNode(winnerIndex) ;
			sz = node.getExtensionality().getCount() ;
			
			
			if (sz < parentSom.modelingSettings.getMinimalNodeSize()){
				// if the winner node is itself still too small, we return without further action
				return -1;
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
				if (node.getActivation()>=0){
					sz = node.getExtensionality().getCount() ;
					if (sz==0){
						extSizeInNeighbors.add( indexedDistances.get(n) );
					}
				}
				
			} // n -> all nodes in vicinity

			if (extSizeInNeighbors.size()==0){
				return -1;
			}
			
			// index distances should be sorted
			for (int i=0;i<extSizeInNeighbors.size();i++){
				
			} // i-> all nodes as IndexDistance (index,spatial distance) that are still empty
			
			ix = indexedDistances.get(0).getIndex() ;
			node = parentSom.somLattice.getNode(ix) ;
				
			newWinnerIndex = ix;
				 
			
		} // (currentEpoch>=1 ?

		return newWinnerIndex;
	}
	
	
	private double getIndexValueFromRow(ArrayList<Integer> recordIndexes, int currentSampleIndex, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	private int getNextRecordId() {
	
		int  _rs, err=1,  _arr_pos = 0, _next_record = 0;
		String hs1;
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
		                               out.print(2, "problem in getNextRecordId , err =" + String.valueOf(err) + 
		                            		   		" , _rs " + String.valueOf(_rs) + "  , _arr_pos " + String.valueOf(_arr_pos));
		     }
		
		     												err = 6;
		     
		     err=0;
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
		double _LF = 1, _v,_v2, _learnrate, _learningrate;
		
if (currentepoch==2){
	_v=0.0;
}
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
		_v = (double) ( constStartLearningRate * Math.exp(-((double)(2+currentepoch) / ((double)somsteps + 3.0)) * _LF));
		_v2 = ( // Math.log10
	               			((double)(1 +  Math.log( 1.0 + _LF  ) )
	               		    )
	               		);
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

		double maxDistInSample, nec, ne=0,crn,nodeDistance, normalizedDistance, influence, sizeFactor=1.0;
		int nodeIndex,wNodeIndex, ixPosition,k,kn, kn6, bmuExtensionSize=0 ;
		MetaNodeIntf node ;
		boolean closeVicinity;
		ArrayList<Integer> emptyNeighbors = new ArrayList<Integer>(); 
		ArrayList<IndexDistanceIntf> nodesPtr= nodesInVicinity;
		 
		
		if (nodesPtr.size()<1){
			return;
		}
		
		nodeIndex = nodesPtr.get(0).getIndex() ;
		node = somLattice.getNode(nodeIndex);
		ixPosition = node.getSimilarity().getIndexIdColumn() ;
		k=this.currentEpoch ;
		try{
			// the nodelist comes in a sorted state
			maxDistInSample = nodesPtr.get(nodesPtr.size()-1).getDistance() ;
			
			// 
			kn = nodesPtr.size();
			if (kn > neighbourhoodSize){
				kn = neighbourhoodSize;
			}
			if (kn<1)kn=1;
			if (kn>6){kn6=6;} else{kn6=kn;}
			
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
			

			
			k=0;
			
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
		ArrayList<Integer> indexes ;
		
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
			out.delay(1000) ;
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

	private void checkForSplit(  ArrayList<IndexDistanceIntf> winningNodeIndexes, 
										ArrayList<IndexDistanceIntf> nodesInVicinity ) {
		ModelingSettings modset;
		
		int nodeIndex,wNodeIndex, nec,crn,ixPosition,k,kn, kn6, bmuExtensionSize=0 ;
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
		
		
		if ((emptyNeighbors.size()>0) && (currentEpoch<=2)){
			// careForCoverage( recordsConsidered, sampleRecordIDs.size(), currentEpoch, somSteps,0 );
			splitNode( wNodeIndex, emptyNeighbors, 1, MetaNodeIntf._NODE_SPLITMODE_MINIMAL ) ; 
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
		
		int nos = 0, ix, bmuCount, bestMatchingNeighborIndex;
		
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
			
			bmuSearch = new ProfileVectorMatcher(out);
			bmuSearch.setNodeCollection( nodeCollection).setParameters(profilevalues, bmuCount, boundingIndexList);
			bmuSearch.createListOfMatchingUnits(1);
			sortedNeighbors = bmuSearch.getList( -1 ) ;
			
			if (sortedNeighbors.size()==0){
				return;
			}
			// the best matching node within this collection is on pos 0, 
			// yet the index reported from this position refers to emptyNeighbors not to the somLattice...
			bestMatchingNeighborIndex = sortedNeighbors.get(0).getIndex() ;
			// ... thus we have to translate back...
			ix = emptyNeighbors.get( bestMatchingNeighborIndex ) ;
			// and to get the node through the translated indes
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
	 * restrictionForSelectionSize
	 * 
	 * 
	 * @param dataRowIndex
	 * @param profilevalues
	 * @param bmuCount
	 * @param boundingIndexList
	 * @return
	 */
	// this acts as a wrapper for "BmuIdentification{}"
	private ArrayList<IndexDistanceIntf> getBestMatchingNodes( 	int dataRowIndex,
															ArrayList<Double> profilevalues, 
															int bmuCount, 
															ArrayList<Integer> boundingIndexList) {
		
		ArrayList<IndexDistanceIntf> bestMatchesCandidates = new ArrayList<IndexDistanceIntf> (); 
		ArrayList<MetaNodeIntf> nodeCollection = new ArrayList<MetaNodeIntf>(); 
		ArrayList<Integer> nodeIndexCollection = new ArrayList<Integer>()  ;
		ProfileVectorMatcher bmuSearch;
		
		MetaNodeIntf node;
		
		ArrayList<ArrayList<Integer>> recordBmuLinks; 
			 
		boolean bmuBufferAvailable;
		// comparing the imported values[] against all nodes in lattice
		//
		
		try {
			
			/*
			 * we refer to the AreaPerspective of the field, from where we determine the indexes of the nodes
			 * within a given radius around a given coordinate.
			 * 
			 */
			
			// for sufficiently developed maps, we store the last 2 or three BMU to the record (separate structure)
			// from this then we can derive a surround, such that the search time remains alsmost constant (slow linear O(n)) 
			// even for large maps 
			
			bmuBufferAvailable=false ;
			// is the BMU buffer available ?
			// yes...
			if (bmuBufferAvailable){
				if (somLattice.bmuBufferActivated){
					parentSom.bmuBuffer.isBufferavailable(dataRowIndex);
				
				}
			}  // else: advanced pre-processing which we can use to determine a sub-area
			if (bmuBufferAvailable == false) {
				// no: we have to search through the whole map for the given record
				for (int n = 0; n < somLattice.size(); n++) {
					node = somLattice.getNode(n);
					// we should not allocate the whole node, since they are large...
					// we just should add the indexes
					// nodeCollection.add(node);
					nodeIndexCollection.add(n);
				}

			}

			
			// here, nodeCollection is a sample from from somLattice, in this collection we search for 
			// the best match for the profilevalues (format: ArrayList Double)
			bmuSearch = new ProfileVectorMatcher(out);
			
			// this provides just the reference, NOT copies !
			bmuSearch.linkNodeCollection( somLattice.getNodes());
			
			// and this provides the selection, which is either all, or reduced by some preprocessing or buffering
			bmuSearch.setNodeCollectionByIndex(nodeIndexCollection);
			
			bmuSearch.setParameters(profilevalues, bmuCount, boundingIndexList);
			
			// this respects deactivated nodes
			bmuSearch.createListOfMatchingUnits(1); // 1=nodes -> profiles
			bestMatchesCandidates = bmuSearch.getList( -1 ) ;
			
			
			double cr = (((double)(currentEpoch))/(double)somSteps);
			double rr = (((double)(sampleRecordIDs.size()))/(double)somData.getRecordCount() ) ;  
												// not quite correct, should be the size of the master sample
			
			if ((cr>=0.42) || (rr>0.28)){                    
				somLattice.bmuBufferActivated = true;
			}
			somLattice.bmuBufferActivated = false;
			
			if (somLattice.bmuBufferActivated){
				// ArrayList<IndexDistance>
				// we take the first two BMU candidates into the buffer
				// parentSom.bmuBuffer.add( dataRowIndex, bestMatchesCandidates.get(0)) ;
				// parentSom.bmuBuffer.add( dataRowIndex, bestMatchesCandidates.get(1)) ;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally {
			nodeCollection.clear();
			nodeCollection = null;
			nodeIndexCollection.clear();
			nodeIndexCollection=null;
		}
		return bestMatchesCandidates ;
	}

	/**
	 * 
	 * @param actualRecordCount respective to the actual epoch after preparing the sample, which is much smaller for the first epochs
	 * @param recordsConsidered
	 * @param timeConstant
	 * @param neighbourhoodDecay
	 */
	private void adoptInfluenceAndReach( int actualRecordCount, int recordsConsidered, int steps, double timeConstant ) {
		 
		double _sf,rp, sr,_Nv,  rc, _f,_fx, speed=1.0;
		int size;
		
		if (mapRadius<=0.01){
			mapRadius = parentSom.dSomCore.getMapRadius(); 
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
									    
	    									
	    double ad; 									
	    ad =  somLattice.getAveragePhysicalDistance() ;
	    
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
		ArrayList<DataTableCol> table;
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
		
		int nodeindex = -1, nodeExtSize=-1;
		MetaNodeIntf node;
		
		MetaNode n;
		
		try{
			 
			// ... calculate new profile
			
			
			// calculate new variance, CoV
			
			for (int i=0;i<winningNodeIndexes.size();i++){
				
				nodeindex = winningNodeIndexes.get(i).getIndex() ;
				node = somLattice.getNode(nodeindex) ;	
				
				int iwn = i;
				if (this.currentEpoch+1==this.somSteps)iwn=-1;
				node.insertDataAndAdjust( dataNewRecord, recordIndexInTable, iwn,currentLearningrate, fillingLimitForMeanStyle);
				 
				
				if ((i>=parentSom.modelingSettings.getWinningNodesCount()) || (i>=0)){
					break; 
				}
			}// i-> all winningNodeIndexes
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	
	
	

}
