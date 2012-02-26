package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;

import org.NooLab.repulsive.components.data.IndexDistance;
import org.NooLab.repulsive.components.data.IndexDistanceIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.categories.similarity.Similarity;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.utilities.logging.PrintLog;




/**
 * 
 *  this class compares a given profile (a list of double) to  </br>
 *  a collection of other profiles, which is given either </br>
 *  - as a list of node indexes    ( mode 1), or </br>
 *  - as a list of record indexes; ( mode 2) </br> </br>
 *  
 *  such it may be used as a wrapper for the identificaion of BMU and 
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
	
	ArrayList<Double> profileValues;
	int bmuCount;
	
	int mode = 0;
	
	PrintLog out = new PrintLog(2,true);
	
	// ..........................................................
	public ProfileVectorMatcher(){
	}
	public ProfileVectorMatcher( PrintLog outprn){
		
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
		nodeCollection = new ArrayList<MetaNode> (nodecollection); 
		mode = 1;
		return this;
	}
	
	public void setNodeCollectionByIndex(ArrayList<Integer> nodeIndexCollection) {
		// TODO Auto-generated method stub
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
		
		profileValues = new ArrayList<Double> (profilevalues);
		bmuCount = bmucount;
		boundingIndexList =  new ArrayList<Integer>(boundingindexlist);
		
		return this;
	}
	
	public ArrayList<IndexDistanceIntf> getList( int sizeRestriction ){
		
		return bestMatchesCandidates;
	}
	
	private void createListOfMatchingNodes( ){

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
		try {
	
			
			// this has to be accelerated: (1) multi-digester (MANDATORY), (2) flexibly created coarse pre-digensting SOMs, 
			
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
				dsq = node.getSimilarity().similarityWithinDomain( profile.getValues(), profileValues, suppressSQRT) ;
				// dsq = getAdvancedDistanceMeasure(1, SOMnodes[n].dweights, values);
	
	
				if (dsq < 0) {
					out.printErr(2,"Problem in calculating distance, relative node index: "+n+" , dsq<0 = " + String.valueOf(dsq));
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
	
			for (int i=0;i<bestMatchesCandidates.size();i++){
				bestMatches.add( bestMatchesCandidates.get(i).getIndex() ) ;
			}
			
			err = 0;
		}catch(Exception e){
			e.printStackTrace();
		}
		finally {
	
		}
		 
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
				  
				 					
				
				dsq = simIntf.similarityWithinDomain( profileValues, dValues, suppressSQRT) ;
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
	 * this creates a semi-orderd list, avoiding the cost of a full sort;
	 * we only need the beginning and the end of of list being sorted properly
	 * 
	 */
	public void createListOfMatchingUnits( int sourceType ){

		createListOfMatchingUnits(1,null);
	}
	public void createListOfMatchingUnits( int sourceType, SimilarityIntf simIntf){
		
		if (sourceType<=1){
			createListOfMatchingNodes();
		}else{
			createListOfMatchingRecords(simIntf);
		}
		
		
	}
	
	private boolean nodeIsCandidate( double distanceValue, ArrayList<IndexDistanceIntf> candidates, int bmuCount ){
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
	

	private ArrayList<IndexDistanceIntf> acquireBmuCandidate( ArrayList<IndexDistanceIntf> currentBestMatches , int bmuCount,  
														  int bmuIndex, double distanceValue){
		
		IndexDistance ixDist ;
		int tIndex ;
		
		if (currentBestMatches.size()<bmuCount){
			
			tIndex = bmuIndex;

			ixDist = new IndexDistance( bmuIndex,distanceValue, "");
			
			if ((currentBestMatches.size()==0) || ( distanceValue > currentBestMatches.get(0).getDistance())){
				// empty or larger than last one ?
				if ((currentBestMatches.size()==0) || (distanceValue > currentBestMatches.get( currentBestMatches.size()-1).getDistance())){
					currentBestMatches.add( ixDist ) ;
				}else{
					
					if ((currentBestMatches.size()>=2) && ( distanceValue < currentBestMatches.get(currentBestMatches.size()-1).getDistance())){
						currentBestMatches.add( currentBestMatches.size()-1, ixDist ) ;	
					}
				}
				
				
			}else{
				currentBestMatches.add(0, ixDist ) ;
			}
			
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
	


	
}	

