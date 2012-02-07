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
		
		while ((recordsConsidered < sampleRecordIDs.size() ) && (parentSom.getUserbreak()==false) ) {
			err = 2;
			loweredPriorityPause(recordsConsidered);
			
			// drawing the next record ID from the preselected set of IDs by random, NOT one after each other
			currentRecordIndex = getNextRecordId();
			
			
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
			testrecord = selectPreparePerceptDataRecord( currentRecordIndex );
						 				if (testrecord == null)continue;
						 
			winningNodeIndexes = getBestMatchingNodes( testrecord, 5, boundingIndexList);
				         				if ((winningNodeIndexes==null) || (winningNodeIndexes.size()==0))continue;
				        	 
			adoptInfluenceAndReach( sampleRecordIDs.size(), recordsConsidered, somSteps ,timeConstant, neighbourhoodDecay);
			
			// dependent on option control and learning epoch, we may allow for multiple winners
			
			
			indexedDistances = getAffectedNodes( winningNodeIndexes, 1 );
			
			// this affects just the profile vector ! 
            updateNodesInVicinity( learningRate, testrecord, indexedDistances);
			

			learningRate = adjustLearningRate( learningRate, recordsConsidered, sampleRecordIDs.size(), 
											   currentEpoch, somSteps);


				
			recordsConsidered++;
		} // -> 
		
		
	} // go()


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
	
	
	private double adjustLearningRate(double learningrate, int recordsConsidered, int size, int currentepoch, int somsteps) {
		// 
		
		return 0;
	}

	private void updateNodesInVicinity( double learningrate, ArrayList<Double> datarecord, ArrayList<IndexDistanceIntf> nodesPtr) {

		double maxDistInSample, nodeDistance, influence, sizeFactor=1.0;
		int nodeIndex;
		MetaNodeIntf node ;
		
		
		if (nodesPtr.size()<1){
			return;
		}
		
		
		try{
			// the nodelist comes in a sorted state
			maxDistInSample = nodesPtr.get(nodesPtr.size()-1).getDistance() ;
			
			for (int i=0;i<nodesPtr.size();i++){
				
				nodeIndex = nodesPtr.get(i).getIndex() ;
				nodeDistance =nodesPtr.get(i).getDistance() ; 
				
				node = somLattice.getNode(nodeIndex);
				
				node.getIntensionality().prepareWeightVector();
				
				influence = getInfluenceforDistance( (nodeDistance*nodeDistance), 1) ;
				
				node.setContentSensitiveInfluence( parentSom.modelingSettings.getContentSensitiveInfluence() );
				
				
				node.adjustProfile( datarecord, // this is normalized data
									learningrate, influence, sizeFactor, 0);// contrast_enh,
				
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
											out.print(2, "nodes selected  n = "+nodelist.size()) ;
		return nodelist;
	}

	
	private ArrayList<IndexDistance> getBestMatchingNodes( ArrayList<Double> values, int bmuCount, ArrayList<Integer> boundingIndexList) {
		
		ArrayList<Integer> bestMatches = new ArrayList<Integer> (); 
		
		ArrayList<IndexDistance> bestMatchesCandidates = new ArrayList<IndexDistance> (); 
		
		MetaNodeIntf node;
		ProfileVectorIntf profile;
		
		int blockemptynodes = 0;
		int num_row, n, num_col ;
		int   err = 0 ;
		double dsq = 999999;
		double miniDis;
		boolean hb, suppressSQRT = true;
		int bmuIndex;
	
		miniDis = 1000000000;
		bmuIndex = -1;
		err = 1;
		// comparing the imported values[] against all nodes in lattice
		//
		err = 1;
		try {
	
			
			// this has to be accelerated: (1) multi-digester (MANDATORY), (2) flexibly created coarse pre-digensting SOMs, 
			for (n = 0; n < somLattice.size(); n++) {
	
				node = somLattice.getNode(n);
				err = 3;
				if ((node == null) ){
	
					continue;
				}
	
				
											SimilarityIntf simIntf = node.getSimilarity();
											out.print(4,"similarity obj in node("+n+") = "+simIntf.toString());
											
				profile = node.getProfileVector();
				dsq = node.getSimilarity().similarityWithinDomain( profile.getValues(), values, suppressSQRT) ;
				// dsq = getAdvancedDistanceMeasure(1, SOMnodes[n].dweights, values);
	
	
				if (dsq < 0) {
					out.printErr(2,"Problem in calculating distance, node index: "+n+" , dsq<0 = " + String.valueOf(dsq));
					return null;
				}
				
				hb = (dsq >= 0) && (nodeIsCandidate( dsq, bestMatchesCandidates, bmuCount ));
				
				
				
				if (blockemptynodes > 0) {
					if (hb == true) {
						if (node.getExtensionality().getCount() <= (blockemptynodes - 1)) {
							hb = false;
						}
					}
				}
				if (hb == true) {
	
					 
					bestMatchesCandidates = acquireBmuCandidate( bestMatchesCandidates, bmuCount, n, dsq );
					err = 8;
	
				} // dsq < miniDis ?
	
			} // n->
			err = 9;
	
			for (int i=0;i<bestMatchesCandidates.size();i++){
				bestMatches.add( bestMatchesCandidates.get(i).getIndex() ) ;
			}
			
			err = 0;
		}catch(Exception e){
			e.printStackTrace();
		}
		finally {
	
		}
		return bestMatchesCandidates ;
	}

	/**
	 * 
	 * 
	 * @param actualRecordCount respective to the actual epoch after preparing the sample, which is much smaller for the first epochs
	 * @param recordsConsidered
	 * @param timeConstant
	 * @param neighbourhoodDecay
	 */
	private void adoptInfluenceAndReach( int actualRecordCount, int recordsConsidered, int steps, double timeConstant, double neighbourhoodDecay) {
		 
		double _sf, _Nv,  rc, _f, speed=1.0;
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
	
		_f =   (1.1f * Math.log10(1.0f+1.0f*_sf*Math.log( size)))*((recordsConsidered + 8.0f*steps)/rc) / (timeConstant * speed);
		_f =   Math.exp(-(neighbourhoodDecay * _f)) ;
		_Nv =  (Math.round( mapRadius * 0.92f)) * _f;
		
		neighbourhoodRadius =  ((neighbourhoodRadius * _Nv)); // _Nv
		neighbourhoodRadius =  (Math.round( mapRadius * 0.92f)) * _f;
									    
	    if (neighbourhoodRadius>mapRadius){
	    	//  neighbourhoodRadius = (mapRadius * 0.94f);
	    }
		
	    double ad =  somLattice.getAveragePhysicalDistance() ;
	    neighbourhoodSize =  (int) ((neighbourhoodRadius*neighbourhoodRadius) * Math.PI * 0.8)  ;
	
	    out.print(2, "Record # "+recordsConsidered+"  NBR scale = "+String.format("%.4f",_Nv)+
			   		 ",   NBR = "+String.format("%.4f",neighbourhoodRadius) +
			   		 ",   n = "+neighbourhoodSize) ;
	    
	}

	private ArrayList<Double> selectPreparePerceptDataRecord(int iindex) {
		ArrayList<DataTableCol> table;
		ArrayList<Double> rowData;
		
		
		rowData = parentSom.somData.getDataTable().getDataTableRow(iindex);
			
		return rowData;
	}
	
	private void loweredPriorityPause(int ix) {
		
		if (parentSom.loweredPriority == true) {
			if ((ix % 10) == 0) {
				parentSom.out.delay(5);
			}
		}
	}

	private ArrayList<IndexDistance> acquireBmuCandidate( ArrayList<IndexDistance> currentBestMatches , int bmuCount,  
														  int bmuIndex, double distanceValue){
		
		IndexDistance ixDist ;
		
		if (currentBestMatches.size()<bmuCount){
			ixDist = new IndexDistance( bmuIndex , distanceValue, "");  
			currentBestMatches.add(ixDist ) ;
		}else{
			if ( currentBestMatches.get( currentBestMatches.size()-1).getDistance() < distanceValue){
				// no change
				return currentBestMatches;
			}
			for (int i=0;i<currentBestMatches.size();i++){
				if ( distanceValue < currentBestMatches.get(i).getDistance() ){
					ixDist = new IndexDistance( bmuIndex , distanceValue, ""); 
					currentBestMatches.add(i,ixDist ) ;
					break;
				}
			}// i->
			if (currentBestMatches.size()>bmuCount){
				currentBestMatches.remove( currentBestMatches.size()-1) ;
			}
		}
		
		return currentBestMatches;
	}
	
	private boolean nodeIsCandidate( double distanceValue, ArrayList<IndexDistance> candidates, int bmuCount ){
		boolean rB = false;
	
		if (candidates.size()<bmuCount){
			rB = true;
		}else{
			double d = candidates.get( candidates.size()-1).getDistance() ;
			if (distanceValue < d){
				rB=true;
			}
		}
		
		return rB;
	}

	
	private double getInfluenceforDistance(double DistToNodeSqr, int mode) {

		int return_value = -1;

		double WidthSq = 1, _v, mapwidth;
		double Influence = 0.0;

		try {

			// mode = 0;

			mapwidth = 1.2 * Math.sqrt( somLattice.size() );
			
			WidthSq = neighbourhoodRadius * neighbourhoodRadius;

			Influence = (float) Math.exp(-(DistToNodeSqr) / (0.001));

			if (WidthSq <= 0) {
				return return_value;
			}

			if (mode == 0) {
				Influence = (float) Math.exp(-(2 * DistToNodeSqr)
						/ (2 * WidthSq));
			}
			if (mode == 1) {
				Influence = (float) Math.exp(-(2 * (DistToNodeSqr) * Math.log(mapwidth)) / (2 * WidthSq));

				if ((neighbourhoodRadius > 10)
						&& (DistToNodeSqr / neighbourhoodRadius > 0.69)) {
					Influence = Influence * (1.01 - (DistToNodeSqr / neighbourhoodRadius));
				}
			}

			if (mode == 2) {
				Influence = (float) Math
						.exp(-(1.6 * Math.sqrt(DistToNodeSqr) * Math.log(mapwidth)) / (2 * WidthSq));

				if ( (neighbourhoodRadius > 10) && 
					 (DistToNodeSqr / neighbourhoodRadius > 0.4)) {
					Influence = Influence * (1.01 - (DistToNodeSqr / neighbourhoodRadius));
				}
				if ( (neighbourhoodRadius > 3) && (DistToNodeSqr >= 3) && 
					 (DistToNodeSqr / neighbourhoodRadius > 0.3)) {

					_v = Math.exp(-(1 / (1 - (DistToNodeSqr / neighbourhoodRadius))));
					Influence = Influence * _v;
				}
			}

			if (mode == 3) {
				Influence = (float) Math.exp(-(2 * DistToNodeSqr * Math.log(mapwidth * Math.sqrt(mapwidth))) / (2 * WidthSq));
			}
			 
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

		return Influence;
	}
	
	
	
	
	
	

}
