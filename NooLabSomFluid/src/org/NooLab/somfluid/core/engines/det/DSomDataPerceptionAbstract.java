package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;

 
import org.NooLab.chord.IndexedItemsCallbackIntf;
import org.NooLab.chord.MultiDigester;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
 
import org.NooLab.utilities.datatypes.IndexDistanceIntf;
import org.NooLab.utilities.logging.PrintLog;




public class DSomDataPerceptionAbstract {  
 
	
	protected DSom           parentSom;
	protected SomProcessIntf somProcess ;
	protected SomDataObject  somData;
	protected VirtualLattice somLattice;
	
	protected SomFluidAppGeneralPropertiesIntf sfProperties;
	protected ModelingSettings modelingSettings ;
	
	ArrayList<MetaNodeIntf> nodeCollection = new ArrayList<MetaNodeIntf>(); 
	private ArrayList<Integer> nodeIndexCollection = new ArrayList<Integer>()  ;
	private ProfileVectorMatcher bmuSearch;
	NodeDigester nodeDigester;
	
	transient protected int errorsCount=0; 
	transient PrintLog out;
	
	// ========================================================================
	public DSomDataPerceptionAbstract( DSom dsom) {

		parentSom = dsom ;
		
		somProcess = parentSom.getSomProcessParent() ;
		
		somData = dsom.somData ;
		
		somLattice = parentSom.somLattice;
		
		sfProperties = parentSom.sfProperties ;
		modelingSettings = sfProperties.getModelingSettings() ;
		
		int errorsCount=0;
		
		out = dsom.out ;
		
		if (out==null){
			out= somProcess.getSomLattice().getOut();
		}
		
		if (sfProperties.getMultiProcessingLevel()>0){
			prepareNodeDigester();
		}
	}
	// ========================================================================
	
	public void closeThreads(){
	
		if (nodeDigester!=null){
			
			nodeDigester.close() ;
			out.delay(10) ;
			nodeDigester = null;
			System.gc();
		}
		
	}
	
	
	public void prepareNodeDigester(){
		
		int n;
		int threadcount = 4 ;
		
		
		createNodeIndexCollection();
		
		nodeDigester = new NodeDigester();
		
		n = nodeIndexCollection.size(); 
			
		nodeDigester.prepareTaskSplitting( nodeIndexCollection, threadcount) ;
		 
	}
	
	
	class NodeDigester  implements IndexedItemsCallbackIntf{
		 
		MultiDigester digester ;
		boolean hasCompleted;
		int poisonPill=0;
		
		@Override
		public int getClosedStatus(){
			return poisonPill ;
		}
		
		public void close(){
			poisonPill = 1;
		}
		
		@Override
		public void perform( int processID, int nodeListId ) {
			
			// call to method that performs the semantic operation on the selected item, as identified by id  
			bmuSearch.checkNode( nodeListId ) ;
			
		}

		public void prepareTaskSplitting(ArrayList<Integer> nodeCollectionIndexes, int threadcount) {
			 
			// providing also right now the callback address (=this class)
			// the interface contains just ONE routine: perform()
			// if (digester==null)
			digester = new MultiDigester(threadcount, (IndexedItemsCallbackIntf)this ) ;
			  
			// note, that the digester need not to know "anything" about our items, just the amount of items
			// we would like to work on.
			// the digester then creates simply an array of indices, which then point to the actual items,
			// which are treated anyway here (below) !
			digester.prepareItemSubSets( nodeCollectionIndexes.size(),0 );
			
		}

		/**
		 * this is called privately from ProfileVectorMatcher  
		 * @param nodeCollectionIndexes
		 * @param threadcount
		 */
		public void digestingNodes(ArrayList<Integer> nodeCollectionIndexes, int threadcount) {
			hasCompleted = false;  
			  
			digester.execute() ;
			
			hasCompleted = true;
		}
		
		public boolean hasCompleted(){
			return hasCompleted;
		}
		
	}
	
	
	private void createNodeIndexCollection(){
		
		if (nodeIndexCollection==null){
			nodeIndexCollection = new ArrayList<Integer>() ;
		}else{
			nodeIndexCollection.clear() ;
		}
		for (int n = 0; n < somLattice.size(); n++) {
			// MetaNodeIntf node = somLattice.getNode(n);
			// we should not allocate the whole node, since they are large...
			// we just should add the indexes
			// nodeCollection.add(node);
			nodeIndexCollection.add(n); // should be global for the perception object, as it could be quite expensive for large maps
		}
	}
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
																  ArrayList<Integer> boundingIndexList,
																  int mppLevel) {
		
		ArrayList<IndexDistanceIntf> bestMatchesCandidates = new ArrayList<IndexDistanceIntf> (); 
		
		
		 
		
			 
		boolean bmuBufferAvailable;
		// comparing the imported values[] against all nodes in lattice
		//
		
			
		if ((mppLevel>0) && (nodeDigester==null)){
			prepareNodeDigester() ;
		}
		
		try {
			
			
			/*
			 * we refer to the AreaPerspective of the field, from where we determine the indexes of the nodes
			 * within a given radius around a given coordinate.
			 * 
			 */
			
			// for sufficiently developed maps, we store the last 2 or three BMU to the record (separate structure)
			// from this then we can derive a surround, such that the search time remains almost constant (slow linear O(n)) 
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

				if ((nodeIndexCollection==null) || (nodeIndexCollection.size()==0)){
					createNodeIndexCollection();
				}
			}

			
			// here, nodeCollection is a sample from from somLattice, in this collection we search for 
			// the best match for the profile values (format: ArrayList Double)
			bmuSearch = new ProfileVectorMatcher( sfProperties.getMultiProcessingLevel(), out);
			
			if (nodeDigester==null){
				mppLevel=0;
			}
			if (mppLevel >0){
				// nodeDigester.digester.reset() ;
				bmuSearch.setNodeDigester(nodeDigester) ; // simply setting the reference
			}

if (dataRowIndex>2){
	bmuSearch.setOutMode(1);
}
			
			// this provides just the reference, NOT copies !
			bmuSearch.linkNodeCollection( somLattice.getNodes());
			
			// and this provides the selection, which is either all, or reduced by some preprocessing or buffering
			bmuSearch.setNodeCollectionByIndex(nodeIndexCollection);
			
			bmuSearch.setParameters( recordprofilevalues, bmuCount, boundingIndexList);
			
			// this respects deactivated nodes
			bmuSearch.createListOfMatchingUnits(1); // 1=nodes -> profiles ??? 
			
			bestMatchesCandidates = bmuSearch.getList( -1 ) ;
			
			if ((bestMatchesCandidates==null) || (bestMatchesCandidates.size()==0)){
				// actually, should be null only in case of serious problems
				// get the state message and the last state flag
				if (bmuSearch.lastStatus <0){
					// count the errors;
					errorsCount++;
				}
			}
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
			
		}
		return bestMatchesCandidates ;
	}
	
	public void resetNodeCollectoinIndices(){
		nodeCollection.clear();
		nodeCollection = null;
		nodeIndexCollection.clear();
		nodeIndexCollection=null;
	}
}
