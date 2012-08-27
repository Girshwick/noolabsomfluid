package org.NooLab.somfluid.astor.trans;

import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import org.NooLab.astor.storage.iciql.NodeContent;
import org.NooLab.astor.storage.iciql.NodeFingerprints;
import org.NooLab.astor.storage.iciql.SomNames;
import org.NooLab.itexx.storage.randomwords.iciql.Contexts;
import org.NooLab.somfluid.astor.SomAssociativeStorage;
import org.NooLab.somfluid.astor.query.SomQueryIntf;
import org.NooLab.somfluid.astor.storage.db.AstorDataBase;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.DSomCore;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;

 
/*
 *   TODO:  close the process on shutdown ... SomProcess.interrupt() ...
 * 
 * 
 * 
 */

/**
 * 
 * the node content maintains a process, that is continuously 
 *   (1) reading the Astor SOM and 
 *   (2) creating histograms, that are saved independently, either
 *       - to a database (could be remote)
 *       - or a binary object store
 * 
 * this class maintains an observer that receives messages if a node has been affected,
 * that is, if a context has been added to it.
 * This happens either in the 
 *   - last phase of Astor-Learning 
 *        ... epochs are looped in DSomCore
 *        ... records are touched in DSomDataPerception
 *   - during online assimilation of records
 *   
 * Here, the observer runs in an inner class, any incoming record contributes 
 * to a queue in a raw manner (without check for double-entries),
 * which then is worked upon in a separate process
 * 
 * 
 * 
 * 
 * ........................................................
 * 
 * Such, any second layer process also runs independently from the first layer;
 * as a consequence, there could be many 2nd-layer (L2) processes
 * 
 * These processes could even run on different machines, working on a copy
 * of the externalized SomHisto data
 *
 */
public class SomAstorNodeContent {

	boolean collectorIsRunning = false, collectoIsWorking=false;
	boolean cheobsIsRunning = false;
	
	SomAssociativeStorage astorHost;
	SomAstorNodeContent astorNodeC ;
	
	VirtualLattice astorLattice = null ;
	long somId=-1L;
	
	SomChangeEventObserver changeObserver;
	SomCollector somCollector;
	
	ArrayList<Long> changedNodes = new ArrayList<Long> ();
	
	transient ArrUtilities arrutil = new ArrUtilities ();
	transient PrintLog out = new PrintLog(2,true);
	private SomQueryIntf somQuery;
	private AstorDataBase astorDb;
	
	// ========================================================================
	public SomAstorNodeContent(SomAssociativeStorage astorHost){
		astorNodeC = this;
		
		this.astorHost = astorHost;
		
		astorDb = astorHost.getAstorDb();
		
 		changeObserver = new SomChangeEventObserver() ;
		somCollector = new SomCollector();
		
		astorLattice = astorHost.getAstorSomLattice() ;
		
		// care for the mapping of the lattice to the database::nodefingerprints
		
		establishNodeFingerprints() ;
		
	}
	
	
	/**
	 * here we check whether the nodes of our SOM are registered... 
	 * 
	 */
	private void establishNodeFingerprints() {
		
		ArrayList<MetaNode> nodes ;
		MetaNode node;
		long[] idValues;
		long numGuid, _numGuid, _key ;
		String nodeContentTable ;
		
		try{
			
			somId = astorLattice.getNumGuid() ;
			
			nodes = astorLattice.getNodes();
		
			nodeContentTable = "" ; // get it from SomNames
															out.print(2, "preparing node references: fingerprints and maps...");
			for (int i=0;i<nodes.size();i++){
				
				node = nodes.get(i);
				
				// the nodes elf-assign a unique numerical guid...
				numGuid = node.getNodeNumGuid() ;
				
				// ...yet, if we use a database, we will overwrite this guid by the unique LONG value that
				//    is being assigned by the database !!
				// 
				// for a given index position within the lattice, we always will get the same guid ! 
				idValues = astorDb.updateLatticNodeRegistration( somId,numGuid,i ) ;
				
				_key = idValues[0];
				_numGuid = idValues[1];
				
				 // overwriting...
				if ( numGuid != _numGuid){  
					node.setSerialID(_key) ;
					node.setNumGuid(_numGuid) ;
					
					 
					ArrayList<Long> nodeGuids = astorLattice.getNodeGuids() ;  
					Map<Long,Object> nodeUidMap = astorLattice.getNodeUidMap() ;
					Map<Long,Long> nodeSerialsMap = astorLattice.getNodeSerialsMap() ;
					 
					
					astorLattice.getNodeGuids().add(_numGuid);
					int p = astorLattice.getNodeGuids().indexOf(numGuid);
					if (p>=0){
						astorLattice.getNodeGuids().remove(p);
					}
					if (nodeUidMap.containsKey(numGuid)){
						nodeUidMap.remove( numGuid ) ;
					}
					nodeUidMap.put(_numGuid, node) ;
					
					nodeSerialsMap.put(_numGuid, _key) ;
					if (nodeSerialsMap.containsKey(numGuid)){
						nodeSerialsMap.remove( numGuid ) ;
					}
				 
				}
															
				astorLattice.setLatestNodeIndex(_key) ; // this refers to the runindex
				// this is important for som processes which include merge and split
				// actually... merge should never occur in default operation, only in dedicated re-organization...
			}// i-> all nodes
																out.print(2, "preparing node references done.");
		}catch(Exception e){
			out.printErr(1, "\nProblem in <establishNodeFingerprints()>... \n");
			e.printStackTrace() ;
		}
		
		_key=0;
	}
	// ========================================================================	
	
	
	public void close(){
		collectorIsRunning = false;
		cheobsIsRunning    = false ;
	}

	public void registerObservedSomProcess( Observable obj){
		
		obj.addObserver(changeObserver) ;
	}
	
	// ----------------------------------------------------
	
	class SomChangeEventObserver 
									implements 
												Runnable,
												Observer{
		
		Thread cheobsThrd;
		ArrayList<Long> _changedNodes = new ArrayList<Long> (); 

		public SomChangeEventObserver(){
			cheobsThrd = new Thread(this,"cheobsThrd") ;
			cheobsThrd.start();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			cheobsIsRunning = true; 
			int n1,n2;
			while (cheobsIsRunning){
				
				if (_changedNodes.size()>0){
					
					n1 = _changedNodes.size();
					_changedNodes = (ArrayList<Long>)arrutil.makeItemsUnique(_changedNodes) ;
					n2 = _changedNodes.size();
					out.print(2, "n1,n2 : "+n1+", "+n2);
					
					if (_changedNodes.size()>0){
						changedNodes.addAll(_changedNodes);
						_changedNodes.clear();
					}
					
				}else{
					out.delay(1000);
				}
				
			}
		}

		@Override
		public void update(Observable o, Object arrData) { // arrData has been cloned before sending...
			// observer sending this data could be found by searching the project code for "138709" (it is in SomAstor :: nodeChangeEvent() )
			
			String senderName = o.toString().replace("org.NooLab.somfluid.astor.", "") ;
			
			out.print(2, "<SomAstorNodeContent::SomChangeEventObserver> received an update message : "+
							arrData.toString()+" from sender "+
					        senderName ) ;
			
			if ((arrData!=null) && (senderName.contains("SomAstor"))){
				try{
					ArrayList<Long> informedAboutNodes = (ArrayList<Long> )arrData;
					if (informedAboutNodes.size()>0){
						_changedNodes.addAll(informedAboutNodes) ;
					}
				}catch(Exception e){
				}
			}
		}
		
		
		
		
	}
	
	// ----------------------------------------------------
	class SomCollector implements Runnable{
		
		
		Thread collectorThrd;
		
		// ------------------------------------------------
		public SomCollector(){
			
			collectorThrd = new Thread(this,"collectorThrd") ;
			// astorNodeC
			startCollector();
			
		}
		
		public void setLattice(VirtualLattice somlattice){
			astorLattice = somlattice;
		}
		
		public void startCollector(){
			collectorThrd.start() ;
		}
		// ------------------------------------------------
		@Override
		public void run() {
			
			collectorIsRunning = true;
			long changedNode = -1L;
			
			while (collectorIsRunning) {
				
				if ((collectoIsWorking==false) &&
					((astorLattice==null) || (astorLattice.getNodes().size()<50) ||
					(astorLattice.isInitializing() ))){
					
					if (changedNodes.size()>0){
						collectoIsWorking=true;
						
						while (changedNodes.size()>0){
							
							changedNode = changedNodes.get(0);
							if (changedNode>=0){
								updateCollectionOfNode(changedNode);
								changedNodes.remove(0) ;
							}
							
						}// changedNodes -> []
						
						collectoIsWorking=false;
					}// changedNodes>0 ?
					
					
				}// lattice ready
				else{
					if (collectorIsRunning){
						out.delay(1000); // no need for hurry here... ([1000..5000])
					}
				} // still nothing to do
				
			}// ->collectorIsRunning
		}
		
	}
	
	public boolean isCollectorIsRunning() {
		return collectorIsRunning;
	}
	// ----------------------------------------------------
	
	// this creates a new histogram for the documents ???
	public int updateCollectionOfNode( long changedNodeUid ){
		/*
		 * the context of this method
		 * 
		 * actually, we do not actualize nodes but documents....
		 *  
		 * each node contains a collection of similar contexts;
		 *   these contexts are similar, but they may refer to different words
		 *   each node thus represents a class of similar "contexts", or a "class of words" 
		 * 
		 * we build a histogram , where 
		 *   each bin is representing a node
		 *   
		 * for a given document, we need 
		 * 
		 * -> we need a database with the docid as a key, where the data
		 *    represent the histogram of (raw!!!) frequencies
		 *    this data are encoded as array [;], additional info is the w*h size of the map
		 *    size of the basic map could be 20'000 !!  
		 *   
		 * but we use a very simple table, using SomGuid, nodeGUID  + data columns 
		 *     data are just two columns : Fingerprint id and doc id
		 *     
		 * such we can query the table for a node and the frequency of the doc in it
		 * 
		 * all nodes together then represent a histogram
		 * 
		 * SELECT count(*) FROM CONTEXTS where docid = 1234 && nodeid = {item from looped list}
		 * 
		 *  
		 * this collects the abundance of the document on the basis of the word classes,
		 * regardless the exact words
		 * 
		 * 
		 * This has to be repeated for all documents in the context table == in the SomMap
		 * we get the list of documents from the table via
		 * 
		 * SELECT distinct docid FROM CONTEXTS ; ... any condition
		 * 
		 * second we need a  
		 *  
		 * 			  id             
					  somid          ** the id of the som as it is stored in the table somnames** 
					  nodeid         ** the numerical GUID from nodefingerprints **
					  
					  docid          ** a particular node may contain various context and various words,
					                    where word refers to the "center" position of the context**
					                                                            
					  fingerprintid  ** if word, the same value as in database "rg-fingerprint" ! 
					                 	if sentence, then the same value as in ....  **
					                 	                         
					  contextid      ** for the same word there might be different contexts **

		 */
		
		int result=0, rec;
		MetaNodeIntf node;
		ArrayList<Integer> records ;
		
		
		// TODO: should fork into a container immediately !!! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		// this method is called periodically by the threaded process in class "SomCollector", 
		// which in turn get active only in case of a sufficient number of node updates (2 updates to the same node count as 2...) 
		
		if (astorHost==null){
			return -9;
		}
		
		try{
		
			// query the node for its content
			// get the query interface...  it has been prepared by astorHost = SomAssociativeStorage
			somQuery = astorHost.getSomQuery() ;
			
			// the lattice maintains a map from serialIds to object reference of the nodes. 
			// we need to get the node for a particular changedNodeIndex
			if (astorLattice==null){
				return  -1;
				// throw exception ...
			}
			node = astorLattice.getNodeByNumGuid( changedNodeUid );
			
			if (node==null){
				
				// NodeFingerprints nfp = astorDb.getNodeEntry( changedNodeUid ); 
				
				return  -3 ;
			}
			records = node.getExtensionRecordsIndexValues();
			
			// from here onwards: database stuff! 
			
			// we first need a difference list: we retrieve 
			// all records for a nodeid
			
			
			// reading randomwords:contexts, writing astornodes::nodecontent-1
			// this index values should point to the contextid in the table contexts
			
			/* from that we update the database astornodes
				with fields  somid, docid, nodeid, contextid 
			
			   astorDb.prepareDatabase("astornodes") ;
			*/
			Contexts contxt;
			long ctxtId,fpindex,docid, _iukey ;
			
			 
			long _somID = somId ;
			_somID = astorLattice.getNumGuid() ;
			
			if ((records!=null) && (records.size()>0)){

				for (int i=0;i<records.size();i++){
					
					rec = records.get(i) ;
			
					// SELECT * FROM CONTEXTS where CONTEXTID = 34;
					contxt = astorDb.getContextsEntryByRecId( rec ); 
					// String wordlabel = (String)ar.get(0) ;
					
					if (contxt!=null){
						docid = contxt.docid;
						fpindex = contxt.fpindex;
						ctxtId = contxt.contextid;
						// changedNodeUid = node's num guid , _somID

						_iukey = astorDb.insertUpdateNodeContent( _somID, changedNodeUid, docid, ctxtId, fpindex);
					}
 					
				}// i-> all records
			}// records ?
			
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return result ;
	}
	
	
}
