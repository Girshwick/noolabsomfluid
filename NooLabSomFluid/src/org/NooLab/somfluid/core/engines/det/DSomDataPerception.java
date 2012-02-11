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

 

public class DSomDataPerception {

	
	DSom           parentSom;
	SomDataObject  somData;
	VirtualLattice somLattice;
	
	ArrayList<Integer> sampleRecordIDs, volatileSample;
	
	
	int       currentEpoch, somSteps, neighbourhoodSize=1 ;
	double    mapRadius, neighbourhoodRadius;
	double    learningRate, timeConstant, neighbourhoodDecay = 1.45 ;
	
	double    constStartLearningRate = 0.1 ;
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
		int err=1, f;
		double indexColValue;
		ArrayList<IndexDistance>  winningNodeIndexes ;
		
		// this cold be filled via the pre-locating service SOM of much lower resolution, followed
		// by a selection of indexes in the scaled vicinity; by default it is empty (and initialized)
		ArrayList<Integer> boundingIndexList = new ArrayList<Integer> ();
		
		ArrayList<Double> testrecord ;
		// this provides the index of nodes/particles together with the distance to the
		// node that is located at the center of the queried area
		ArrayList<IndexDistanceIntf> indexedDistances   ;
		
		
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
						 
			winningNodeIndexes = getBestMatchingNodes( testrecord, 5, boundingIndexList);
				         				if ((winningNodeIndexes==null) || (winningNodeIndexes.size()==0))continue;
				         				out.print(3, "winning node index: "+ winningNodeIndexes.get(0).getIndex() );
				         				
            // set calculateAllVariables = true; for all nodes in the last epoch

			updateWinningNode( winningNodeIndexes, testrecord, currentRecordIndex, learningRate);
			
		    if ((currentEpoch+1)<somSteps){ 
		    	// this also defines the size of the neighborhood 
		    	adoptInfluenceAndReach( sampleRecordIDs.size(), recordsConsidered, somSteps ,timeConstant, neighbourhoodDecay);
			
		    	// dependent on option control and learning epoch, we may allow for multiple winners
			
			              
		    	indexedDistances = getAffectedNodes( winningNodeIndexes, 1 );
		    
			
			
		    	// this affects just the profile vectors of the nodes ! 
		    	if ((currentEpoch+1)<somSteps){ // in the last step we do not adjust the vicinity any more... it is just classifying...
				    updateNodesInVicinity( winningNodeIndexes, learningRate, testrecord, indexedDistances); // influence ?
		    	}
		    
		    	learningRate = adjustLearningRate( learningRate, recordsConsidered, sampleRecordIDs.size(), currentEpoch, somSteps);
		    }				


											out.print(3,"neighbourhoodSize : "+neighbourhoodSize);
			recordsConsidered++;
		} // -> 
		
		careForCoverage( recordsConsidered, sampleRecordIDs.size(), currentEpoch, somSteps,1 );
		err=0;
	} // go()


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
		double _LF = 1, _v, _learnrate;
		
		// describing the scaling dynamics of the learning rate
		if (learningRateFixationmode == 1) {
			_LF = (recordsConsidered / recordCount);
			_LF = (float) (0.5 + Math.sqrt(_LF) / 2);
			_LF = 1 / _LF;
		
		// _Lf<0 , thus sqrt grows more for small values <0.5
		
		}
		
		if (learningRateFixationmode == 2) {
			_LF = (recordsConsidered / recordCount);
			_LF = (float) ((1 + Math.log(1 + _LF)) / 2);
			_LF = 1 / _LF;
		
		}
		
		_v = (float) (constStartLearningRate * Math.exp(-(currentepoch / (somsteps + 2)) * _LF));
		_learnrate = constStartLearningRate * Math.exp(-((3 * Math.log10(5 + recordsConsidered)) / (Math.log10(1 + recordCount))) * _LF) / 2;
		_learnrate = (2 * _v + _learnrate) / 3;
		
		return   _learnrate;
		 
	}

	private void updateNodesInVicinity( ArrayList<IndexDistance> winningNodeIndexes, 
										double learningrate, 
										ArrayList<Double> datarecord, 
										ArrayList<IndexDistanceIntf> nodesPtr) {

		double maxDistInSample, nec, ne=0,crn,nodeDistance, normalizedDistance, influence, sizeFactor=1.0;
		int nodeIndex,wNodeIndex, ixPosition,k,kn, kn6, bmuExtensionSize=0 ;
		MetaNodeIntf node ;
		boolean closeVicinity;
		ArrayList<Integer> emptyNeighbors = new ArrayList<Integer>(); 
		
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
				
				if (closeVicinity){
					
					
				}
				
				// node.getExtensionality().addRecordByIndex( (int)Math.round(datarecord.get(ixPosition)) );
				
				
			} // i->
			
			if ((emptyNeighbors.size()>0) && (currentEpoch<=2)){
				// careForCoverage( recordsConsidered, sampleRecordIDs.size(), currentEpoch, somSteps,0 );
				splitNode( wNodeIndex, emptyNeighbors, 1, MetaNodeIntf._NODE_SPLITMODE_MINIMAL ) ; 
			}
			
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
	private ArrayList<IndexDistanceIntf> getAffectedNodes( ArrayList<IndexDistance> winningNodeIndexes, int winnersCount) {
		
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
		// selection results arrive in "SomFluid.onSelectionRequestCompleted ()" and are sent finally to "digestParticleSelection()"
		nodeIndex = winningNodeIndexes.get(0).getIndex() ;
		
		// this performs the query, waits for the results and filters the results according to current radius...
		nodelist = parentSom.somLattice.getNeighborhoodNodes( nodeIndex , neighbourhoodSize ) ;
		
		// the lattice performs the call and organizes the wait by itself, -> no complicated callbacks to here...
		// it wraps the call through the SomFluid object : parentSom.somFluidParent.getNeighborhoodNodes( index );
											out.print(3, "nodes selected  n = "+nodelist.size()) ;
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
		ArrayList<IndexDistance> sortedNeighbors = new ArrayList<IndexDistance> ();
		ArrayList<Integer> boundingIndexList = new ArrayList<Integer>();
		ArrayList<MetaNodeIntf> nodeCollection = new ArrayList<MetaNodeIntf>(); 
		
		ProfileVectorMatcher bmuSearch;
		
		ArrayList<Integer> exportedRecordIndexes = new ArrayList<Integer> () ;
		
		MetaNodeIntf node, bmnNode;
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
	// this acts as a wrapper for "BmuIdentification{}"
	private ArrayList<IndexDistance> getBestMatchingNodes( 	ArrayList<Double> profilevalues, 
															int bmuCount, 
															ArrayList<Integer> boundingIndexList) {
		
		ArrayList<IndexDistance> bestMatchesCandidates = new ArrayList<IndexDistance> (); 
		ArrayList<MetaNodeIntf> nodeCollection = new ArrayList<MetaNodeIntf>(); 
		
		ProfileVectorMatcher bmuSearch;
		
		MetaNodeIntf node;
		
	
		// comparing the imported values[] against all nodes in lattice
		//
		
		try {
	
			for (int n = 0; n < somLattice.size(); n++) {
				node = somLattice.getNode(n);
				nodeCollection.add(node);
			}
			// here, nodeCollection is a sample from from somLattice, in this collection we search for 
			// the best match for the profilevalues (format: ArrayList Double)
			bmuSearch = new ProfileVectorMatcher(out);
			bmuSearch.setNodeCollection(nodeCollection).setParameters(profilevalues, bmuCount, boundingIndexList);
			bmuSearch.createListOfMatchingUnits(1); // 1=nodes -> profiles
			bestMatchesCandidates = bmuSearch.getList( -1 ) ;
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally {
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
	private void adoptInfluenceAndReach( int actualRecordCount, int recordsConsidered, int steps, double timeConstant, double neighbourhoodDecay) {
		 
		double _sf, _Nv,  rc, _f,_fx, speed=1.0;
		int size;
		
		if (mapRadius<=0.01){
			mapRadius = parentSom.dSomCore.getMapRadius(); 
		};
		
		
		rc = actualRecordCount;
		size = somLattice.size();
		
	
		_sf=0.1;
		if (size>=6){
			_sf = Math.sqrt(size) + 0.7*size/(Math.log10(size)) ;
		}
		
		// _Nv = (float) (Math.round(mapRadius * 0.92) * Math.exp(-(neighbourhoodDecay * (1.1 * Math.log10(1+1*_sf*Math.log(size)))*((recordsConsidered + 8*steps)/rc) / timeConstant)));
	
		_f =   (1.1f * Math.log10(1.0f+1.0f*_sf*Math.log( size)))*(((double)recordsConsidered + (double)(8.0*(double)steps)/rc)) / (timeConstant * speed);
		_fx =   Math.exp(-(neighbourhoodDecay * _f)) ;
		_Nv =  (Math.round( mapRadius * 0.92)) * _fx;
											
		// neighbourhoodRadius =  Math.sqrt((neighbourhoodRadius * _Nv)); // _Nv
		neighbourhoodRadius =  (Math.round( mapRadius * 0.92f)) * _fx;
									    
	    if (neighbourhoodRadius>mapRadius){
	    	//  neighbourhoodRadius = (mapRadius * 0.94f);
	    }
	    									
	    double ad; 									
	    ad =  somLattice.getAveragePhysicalDistance() ;
	    neighbourhoodSize =  (int) ((neighbourhoodRadius*neighbourhoodRadius) * Math.PI * 0.8)  ;
	
	    								if (recordsConsidered>176){
	    									out.printErr(4, "calculating neighborhood : _f="+String.format("%.4f",_f)+
	    													"  _fx="+String.format("%.4f",_fx) +"  ");
	    
	    									// if (recordsConsidered%10==0)
	    									{
	    										out.print(3, "Record # "+recordsConsidered+"   NBR scale = "+String.format("%.4f",_Nv)+"   timeConstant = "+String.format("%.4f",timeConstant)+
	    													 ",   NBR = "+String.format("%.4f",neighbourhoodRadius) +
	    													 ",   n = "+neighbourhoodSize) ;
	    									}
	    									rc=0;
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
	
	private void updateWinningNode(	ArrayList<IndexDistance> winningNodeIndexes, 
									ArrayList<Double> dataNewRecord,
									int recordIndexInTable, 
									double currentLearningrate) {
		
		int nodeindex = -1, nodeExtSize=-1;
		MetaNodeIntf node;
		
		
		try{
			
			
			
			// ... calculate new profile
			
			
			// calculate new variance, CoV
			
			for (int i=0;i<winningNodeIndexes.size();i++){
				
				nodeindex = winningNodeIndexes.get(i).getIndex() ;
				node = somLattice.getNode(nodeindex) ;	
				
				node.insertDataAndAdjust( dataNewRecord, recordIndexInTable, currentLearningrate);
				 
				break; // TODO: make this dependent from option
			}// i-> all winningNodeIndexes
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	
	
	

}
