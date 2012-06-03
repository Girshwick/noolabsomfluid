package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;

 
import org.NooLab.chord.IndexedItemsCallbackIntf;
import org.NooLab.chord.MultiDigester;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
 
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.engines.det.DSomDataPerceptionAbstract.NodeDigester;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
 
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexDistanceIntf;
import org.NooLab.utilities.logging.PrintLog;




/**
 * 
 *  this class compares a given profile (a list of double) to  </br>
 *  a collection of other profiles, which is given either </br>
 *  - as a list of node indexes    ( mode 1), or </br>
 *  - as a list of record indexes; ( mode 2) </br> </br>
 *  
 *  such it may be used as a wrapper for the identification of BMU and 
 *  the creation of a array of nodes that is sorted along the similarity
 * 
 *  the class allows for arbitrary populations of nodes to be checked
 * 
 */
public class ProfileVectorMatcher{
	
	ArrayList<MetaNode> nodeCollection = new ArrayList<MetaNode>(); 
	
	ArrayList<Integer> nodeCollectionIndexes = new ArrayList<Integer>() ;
	
	ArrayList<ArrayList<Double>> dataVectorCollection = new ArrayList<ArrayList<Double>>();
	
	ArrayList<IndexDistanceIntf> bestMatchesCandidates = new ArrayList<IndexDistanceIntf> ();
	ArrayList<Integer> boundingIndexList = new ArrayList<Integer>();
	
	ArrayList<Double> dataProfileValues;
	int bmuCount;
	
	int mode = 0;
	int outMode=0;
	
	NodeDigester nodeDigester = null ;
	int multiProcessingLevel;
	
	transient PrintLog out = new PrintLog(2,true);

	int lastStatus = 0;
	
	// ..........................................................
	public ProfileVectorMatcher(){
	}
	public ProfileVectorMatcher( int mppLevel, PrintLog outprn){
		multiProcessingLevel = mppLevel ;
		out = outprn;
	}	// ..........................................................
	
	public void clear() {
		// 
		nodeCollection.clear();
		bestMatchesCandidates.clear();
		dataVectorCollection.clear() ;
		nodeCollectionIndexes.clear() ;
	}
	
	
	public ProfileVectorMatcher setNodeCollection( ArrayList<MetaNode> nodecollection ){
		
		nodeCollection = new ArrayList<MetaNode>(nodecollection);
		
		if (nodeCollectionIndexes.size() == 0) {
			for (int i = 0; i < nodecollection.size(); i++) {
				nodeCollectionIndexes.add(i);
			}
		}
		
		mode = 1;
		return this;
	}
	
	public void setNodeCollectionByIndex(ArrayList<Integer> nodeIndexCollection) {
		 
		nodeCollectionIndexes.addAll(nodeIndexCollection) ;
	}
	
	public void addNodeToCollection( MetaNode nodeForCollection ){
		nodeCollection.add(nodeForCollection) ; mode = 1;
	}

	
	
	public void addRecordToCollection( ArrayList<Double> vector){
		mode = 2;
		// actually, it is a table? but we need not to use this large object...
		dataVectorCollection.add( vector ) ;
	}
	
	
	public ProfileVectorMatcher setParameters( ArrayList<Double> profilevalues, int bmucount, ArrayList<Integer> boundingindexlist){
		
		dataProfileValues = new ArrayList<Double> (profilevalues);
		bmuCount = bmucount;
		boundingIndexList =  new ArrayList<Integer>(boundingindexlist);
		
		return this;
	}
	
	public ArrayList<IndexDistanceIntf> getList( int sizeRestriction ){
		
		return bestMatchesCandidates;
	}
	

	
	protected void checkNode( int n){
		

		ArrayList<Integer> bestMatches = new ArrayList<Integer> (); 
		
		MetaNodeIntf node;
		ProfileVectorIntf profile;
		
		int blockemptynodes = 0;
		int nodix ;
		int err = 0 ;
		double dsq = 999999;
		
		boolean hb, suppressSQRT = true;
		 
		
		if ((n<0) || (n>nodeCollectionIndexes.size())){
			lastStatus = -10 ;
			return;
		}
		
		nodix = nodeCollectionIndexes.get(n);
		
		if ((nodix<0) || (nodix>nodeCollection.size()-1)){
			lastStatus = -11 ;
			return; // continue ;
		}
		node = nodeCollection.get( nodix);
		err = 3;
		if ((node == null) || (node.getActivation() < 0)){
			lastStatus = -15 ;
			return ; // continue;
		}

		
									SimilarityIntf simIntf = node.getSimilarity();
									out.print(4,"similarity obj in node("+n+") = "+simIntf.toString());
									
		profile = node.getProfileVector();
		dsq = node.getSimilarity().similarityWithinDomain( profile.getValues(), dataProfileValues, suppressSQRT) ;
		// dsq = getAdvancedDistanceMeasure(1, SOMnodes[n].dweights, values);
		// node.getSimilarity.usageIndicationVector is wrong, hence profile.getValues() is also wrong
									if (outMode==0){
										outMode=1;
										String str = ArrUtilities.arr2Text(profile.getValues(), 1) ;
										// out.print(2, "createListOfMatchingNodes(), profile values vector 1 : "+str) ;
									}

		if (dsq < 0) {
			out.printErr(2,"Problem in calculating distance, relative node index: "+n+" , dsq<0 = " + String.valueOf(dsq));
			dsq = node.getSimilarity().similarityWithinDomain( profile.getValues(), dataProfileValues, suppressSQRT) ; // XXX DEBUG ONLY
			lastStatus = -17 ;
			return  ;
		}
		
		hb = (dsq >= 0) && (nodeIsCandidate( dsq, bestMatchesCandidates, bmuCount ));
		
		
		
		if (blockemptynodes > 0) {
			if (hb == true) {
				if (node.getExtensionality().getCount() <= (blockemptynodes - 1)) {
					hb = false;
				}
			}
		}
		lastStatus = 0 ;
		if (hb == true) {

			
			bestMatchesCandidates = acquireBmuCandidate( bestMatchesCandidates, bmuCount, n, dsq );
			err = 8;

		} // dsq < miniDis ?

	}
	
	
	
	public void setNodeDigester( NodeDigester digester){
		nodeDigester = digester ;
	}
	
	private void createListOfMatchingNodesMPP(){
		
		int n, threadcount = 2;
		 
		nodeDigester.digestingNodes( nodeCollectionIndexes, threadcount) ;
		 
	}
	
	private void createListOfMatchingNodes(){

		ArrayList<Integer> bestMatches = new ArrayList<Integer> (); 
		
		MetaNodeIntf node;
		ProfileVectorIntf profile;
		
		int blockemptynodes = 0;
		int n,nodix ;
		int err = 0 ;
		double dsq = 999999;
		
		boolean hb, suppressSQRT = true;
		 
		// comparing the imported values[] against all nodes in nodeCollection
		// if (profileValues.size()==0){return;}
		err = 1;
		
		if (dataProfileValues.size()==0){
			bestMatchesCandidates.clear();
			return  ;
		}
		
		try {
			// we should avoid to create the dispatcher for each record
			// instead, it should be hosted one level higher in "DSomDataPerceptionAbstract{}" where 
			// the method "getBestMatchingNodes()" is the entry point for this one
			
			// MultiprocDispatcher
			// this has to be accelerated: (1) multi-digester (MANDATORY), (2) flexibly created coarse pre-digesting SOMs, 
			// we parallelize the nodes only if there are more than 4+ ..., 			
			
			
			
			// for (n = 0; n < somLattice.size(); n++) {
			for (n = 0; n < nodeCollectionIndexes.size(); n++) {
				 
				nodix = nodeCollectionIndexes.get(n);
				
				if ((nodix<0) || (nodix>nodeCollection.size()-1)){
					continue ;
				}
				node = nodeCollection.get( nodix);
				err = 3;
				if ((node == null) || (node.getActivation() < 0)){
					continue;
				}
	
											SimilarityIntf simIntf = node.getSimilarity();
											out.print(4,"similarity obj in node("+n+") = "+simIntf.toString());
											
				profile = node.getProfileVector();
								
				dsq = node.getSimilarity().similarityWithinDomain( profile.getValues(), dataProfileValues, suppressSQRT) ;
				// dsq = getAdvancedDistanceMeasure(1, SOMnodes[n].dweights, values);
				// node.getSimilarity.usageIndicationVector is wrong, hence profile.getValues() is also wrong
											if (outMode==0){
												outMode=1;
												// String str = ArrUtilities.arr2Text(profile.getValues(), 1) ;
												// out.print(2, "createListOfMatchingNodes(), profile values vector 1 : "+str) ;
											}
	
				if (dsq < 0) {
					// on option : throw exception
					out.printErr(3,"Problem in calculating distance, relative node index: "+n+" , dsq<0 = " + String.valueOf(dsq));
					// dsq = node.getSimilarity().similarityWithinDomain( profile.getValues(), profileValues, suppressSQRT) ; 
					// XXX DEBUG ONLY
					bestMatchesCandidates.clear();
					lastStatus = -18 ;
					return  ;
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
			lastStatus = 0 ;
			for (int i=0;i<bestMatchesCandidates.size();i++){
				bestMatches.add( bestMatchesCandidates.get(i).getIndex() ) ; // ???
			}
			
			err = 0;
		}catch(Exception e){
			e.printStackTrace();
		}
		finally {
	
		}
		n=0;
	}

	private void createListOfMatchingRecords( SimilarityIntf simIntf ){

		
		ArrayList<Double> dValues;
		ArrayList<Integer> bestMatches = new ArrayList<Integer> (); 
		
		 
		int   n  ;
		double dsq = 999999;
		boolean hb, suppressSQRT = true;
		 
		try {
	
			//  ArrayList<ArrayList<Double>> 
			for (n = 0; n <  dataVectorCollection.size(); n++) {
				
				dValues = dataVectorCollection.get(n);
				  
				 					
				
				dsq = simIntf.similarityWithinDomain( dataProfileValues, dValues, suppressSQRT) ;
				// dsq = getAdvancedDistanceMeasure(1, SOMnodes[n].dweights, values);
	
	
				if (dsq < 0) {
					out.printErr(2,"Problem in calculating distance, record index: "+n+" , dsq<0 = " + String.valueOf(dsq));
					return  ;
				}
				
				hb = (dsq >= 0) && (nodeIsCandidate( dsq, bestMatchesCandidates, bmuCount ));
				
				if (hb == true) {
					
					bestMatchesCandidates = acquireBmuCandidate( bestMatchesCandidates, bmuCount, n, dsq );
					 
	
				} // dsq < miniDis ?
	
			} // n->
			 
	
			for (int i=0;i<bestMatchesCandidates.size();i++){
				bestMatches.add( bestMatchesCandidates.get(i).getIndex() ) ;
			}
			 
		}catch(Exception e){
			e.printStackTrace();
		}
		finally {
	
		}
	}
	
	/**
	 * this creates a semi-ordered list, avoiding the cost of a full sort;
	 * we only need the beginning and the end of of list being sorted properly
	 * 
	 */
	public void createListOfMatchingUnits( int sourceType ){

		createListOfMatchingUnits(1,null);
	}
	
	public void createListOfMatchingUnits( int sourceType, SimilarityIntf simIntf){
		
		if (sourceType<=1){ // replace by constant 
			if ((multiProcessingLevel>=1) && (nodeDigester!=null)){
				createListOfMatchingNodesMPP();
			}else{
				createListOfMatchingNodes();
			}
		}else{
			createListOfMatchingRecords(simIntf);
		}
		 
	}
	
	// synchronized
	 private boolean nodeIsCandidate( double distanceValue, ArrayList<IndexDistanceIntf> candidates, int bmuCount ){
		boolean rB = false;
	
		if ((candidates==null) || (candidates.size()<bmuCount)){
			rB = true;
		}else{
			double d ;
			IndexDistanceIntf c = null;
			int sz = candidates.size()-1 ;
			boolean avail=false;
			
			while ((c==null) && (sz>0)){ 
				sz--;
				c = candidates.get(sz);
				if (c!=null){
					avail=true;
					d = c.getDistance();
					if (distanceValue < d) {
						rB = true;
					}
				} // c != null
			} // -> anything found?
			if (avail==false){
				rB=true;
			}
		}
		
		return rB;
	}
	

	/*
	 * TODO: this should not be a method, it should be a class, that handles the logging in 2 layers,
	 * using a list for each of the processes, which then are combined by that class ... 
	 * such we would not need synchronizing, and ther would be no collisions either
	 * 
	 */
	synchronized private ArrayList<IndexDistanceIntf> acquireBmuCandidate( ArrayList<IndexDistanceIntf> currentBestMatches , int bmuCount,  
														  	  int bmuIndex, double distanceValue){
		
		IndexDistance ixDist ;
		int tIndex ;
		
		try{
			

			if (currentBestMatches.size()<bmuCount){
				
				tIndex = bmuIndex;

				ixDist = new IndexDistance( bmuIndex,distanceValue, "");
				
				if ((currentBestMatches.size()==0) || ( distanceValue > currentBestMatches.get(0).getDistance())){
					// empty or larger than last one ?
					int sz = currentBestMatches.size() ;
					IndexDistanceIntf cbm =null;
					
					while ((sz>0) && (cbm==null)){
						sz--;
						cbm = currentBestMatches.get( sz ) ;
					}
					
					if ((cbm==null) || (currentBestMatches.size()==0) || (distanceValue > cbm.getDistance())){
						currentBestMatches.add( ixDist ) ;
					}else{
						
						if ((currentBestMatches.size()>=2) && ( distanceValue < cbm.getDistance())){
							currentBestMatches.add( sz, ixDist ) ;	
						}
					}
					
					
				}else{
					// insert the new best to the first position
					currentBestMatches.add(0, ixDist ) ;
					
					if (currentBestMatches.size()>100){ // TODO: replace by constant: absoluteLimitFOrBestMatchesCount
						currentBestMatches.remove(100);
					}
				}
				
			}else{
				
				int sz = currentBestMatches.size()-1 ;
				IndexDistanceIntf cbmi, cbm=null;
				
				if (sz>0){
					sz = currentBestMatches.size();
					while ((sz>0) && (cbm==null)){
						sz--;
						try{
							cbm = currentBestMatches.get( sz ) ;
						}catch(Exception e){
							cbm=null;
						}
					} // ->
				}
				
				if ((sz>0) && (cbm!=null) && ( cbm.getDistance() < distanceValue)){
					// no change necessary, since even the last position in our short list is smaller than the candidate
					return currentBestMatches;
				}
				
				for (int i=0;i<currentBestMatches.size();i++){
					
					cbmi = currentBestMatches.get(i) ;
					if ((cbmi!=null) && ( distanceValue < cbmi.getDistance() )){
						ixDist = new IndexDistance( bmuIndex , distanceValue, ""); 
						currentBestMatches.add(i,ixDist ) ;
						break;
					}
				}// i->
				
				int bmuLimit = Math.max( 2, bmuCount);
				if (currentBestMatches.size()> bmuLimit){
					sz = currentBestMatches.size()-1;
					try{
						currentBestMatches.remove( sz ) ;
						// there might be a collision between threads, which is not relevant, though, so we keep it silent here
						// (another thread could have removed it already
					}catch(Exception e){}
				}
			}
			
		}catch(Exception e){
			
		}
		
		
		return currentBestMatches;
	}
	
	
	public void partialSort(int direction, int nPositions) {
		//
		ArrayList<Integer> bestMatches = new ArrayList<Integer> (); 
		
		IndexDistanceIntf ixDist ;
		int k;
		boolean done=false;
		double v1,v2 ;
		
		if (nPositions>bestMatchesCandidates.size()-1){
			nPositions = bestMatchesCandidates.size()-1 ;
		}

		
		while (done==false){
			done=true;
		
			int i=0;
			while (i<bestMatchesCandidates.size()-1){
				k = bestMatchesCandidates.size()-1-nPositions;
				
				v1 = bestMatchesCandidates.get(i).getDistance() ;
				v2 = bestMatchesCandidates.get(i+1).getDistance() ;
				
				if (v1>v2){
					ixDist = bestMatchesCandidates.get(i);
					bestMatchesCandidates.remove(i) ;
					bestMatchesCandidates.add(i+1,ixDist);
					done=false;
					i--;
				}
				i++;
			} // i->
		}
		
		for (int i=0;i<bestMatchesCandidates.size();i++){
			bestMatches.add( bestMatchesCandidates.get(i).getIndex() ) ;
		}
		
	}
	public void linkNodeCollection(ArrayList<MetaNode> nodes) {
		nodeCollection = nodes; 
		
	}
	/**
	 * @param outMode the outMode to set
	 */
	public void setOutMode(int outMode) {
		this.outMode = outMode;
	}
	


	
}	

