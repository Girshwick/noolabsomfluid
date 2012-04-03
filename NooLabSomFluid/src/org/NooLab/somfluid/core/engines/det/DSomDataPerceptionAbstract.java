package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;

 
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
 
import org.NooLab.utilities.datatypes.IndexDistanceIntf;
import org.NooLab.utilities.logging.PrintLog;




public class DSomDataPerceptionAbstract {

	
	
	protected DSom           parentSom;
	protected SomDataObject  somData;
	protected VirtualLattice somLattice;
	
	
	
	
	PrintLog out;
	
	// ========================================================================
	public DSomDataPerceptionAbstract( DSom dsom) {

		parentSom = dsom ;
		
		somData = dsom.somData ;
		
		somLattice = parentSom.somLattice;
		
		out = dsom.out ;
		
	}
	// ========================================================================
	
	
	

	/**
	 * 
	 * Creates a sorted list of nodes that match a given vector of values best;
	 * blacklist of variables, target variables and index variables are not considered;
	 * 
	 * the activated similarity measure will be applied. 
	 * 
	 * 
	 * @param dataRowIndex
	 * @param profilevalues
	 * @param bmuCount
	 * @param boundingIndexList
	 * @return
	 */
	// this acts as a wrapper for "BmuIdentification{}"
	protected ArrayList<IndexDistanceIntf> getBestMatchingNodes(  int dataRowIndex,
																  ArrayList<Double> recordprofilevalues, 
																  int bmuCount, 
																  ArrayList<Integer> boundingIndexList) {
		
		ArrayList<IndexDistanceIntf> bestMatchesCandidates = new ArrayList<IndexDistanceIntf> (); 
		ArrayList<MetaNodeIntf> nodeCollection = new ArrayList<MetaNodeIntf>(); 
		ArrayList<Integer> nodeIndexCollection = new ArrayList<Integer>()  ;
		ProfileVectorMatcher bmuSearch;
		
		
			 
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
					// MetaNodeIntf node = somLattice.getNode(n);
					// we should not allocate the whole node, since they are large...
					// we just should add the indexes
					// nodeCollection.add(node);
					nodeIndexCollection.add(n); // should be global for the perception object, as it could be quite expensive for large maps
				}

			}

			
			// here, nodeCollection is a sample from from somLattice, in this collection we search for 
			// the best match for the profilevalues (format: ArrayList Double)
			bmuSearch = new ProfileVectorMatcher(out);
			
if (dataRowIndex>2){
	bmuSearch.setOutMode(1);
}
			
			// this provides just the reference, NOT copies !
			bmuSearch.linkNodeCollection( somLattice.getNodes());
			
			// and this provides the selection, which is either all, or reduced by some preprocessing or buffering
			bmuSearch.setNodeCollectionByIndex(nodeIndexCollection);
			
			bmuSearch.setParameters( recordprofilevalues, bmuCount, boundingIndexList);
			
			// this respects deactivated nodes
			bmuSearch.createListOfMatchingUnits(1); // 1=nodes -> profiles
			
			bestMatchesCandidates = bmuSearch.getList( -1 ) ;
			
			/*
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
			*/
			
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
}
