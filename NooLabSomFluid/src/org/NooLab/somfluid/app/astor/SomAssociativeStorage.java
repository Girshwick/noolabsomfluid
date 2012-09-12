package org.NooLab.somfluid.app.astor;

import java.util.ArrayList;
import java.util.Collections;

import org.NooLab.field.FieldIntf;
import org.NooLab.field.repulsive.components.data.SurroundResults;
import org.NooLab.itexx.storage.DataStreamProviderIntf;
import org.NooLab.itexx.storage.TexxDataBaseSettingsIntf;
import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somfluid.app.astor.query.SomQueryFactory;
import org.NooLab.somfluid.app.astor.query.SomQueryIntf;
import org.NooLab.somfluid.app.astor.storage.db.AstorDataBase;
import org.NooLab.somfluid.app.astor.stream.SomDataStreamer;
import org.NooLab.somfluid.app.astor.trans.SomAstorNodeContent;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.LatticePreparation;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.engines.det.SomTargetedModeling;
import org.NooLab.somfluid.core.engines.det.results.ModelProperties;
import org.NooLab.somfluid.core.nodes.LatticeProperties;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.env.data.SomTexxDataBase;
import org.NooLab.somfluid.lattice.VirtualLattice;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.structures.Variables;

import org.NooLab.somsprite.ProcessCompletionMsgIntf;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
import org.NooLab.somtransform.SomTransformer;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.SerialGuid;



/**
 * 
 * TODO : the node has to keep 3 indices: the docid, the fingerprint ID of the word and the context ID
 *        currently, only 1 index is maintained by the extensionality. ???
 *        ...but we have the listOfRecords, which refers to the global table "randomwords"
 *        (later we will have several tables, in order to split the query among words according to their initial spelling !!!
 *         as there are even much, much more contexts than different words ...the figures could easily reach millions...
 *        
 *        anyway, from there, we can infer the fingerprint-id 
 * 
 * This takes the same role as the ModelOptimizer or the SimpleSingleModel
 * 
 * 
 * the idea is to keep most of the infrastructure (DSom+ ... the Lattice, nodes), just the loops provided by
 * ModelOptimizer are abandoned, do not take place.
 * Instead, other things are organized by it
 *   - streaming of data for online learning as part of the Astor framework
 *     class: SomDataStreamer 
 *   - externalization of references and histograms into a database   
 *     the histogram creation is running in a separate thread, and does not wait for 
 *     completion of 
 *     class: SomAstorNodeContent
 *   - periodic reorganization of the Network, including growing
 *   
 * 		That is, growing never takes place online during a particular modeling,
 * 		It is just registered that growing could be necessary, then the growth supervisor checks the grid
 * 		and triggers a reorganization
 *   
 *   - persistence (object and XML)

 *   
 *  A particular level 2 process is the treatment of the words collected (as contexts) within a L1-node.
 *  This treatment could take the words enrich them with semantic information from WordNet or Leipzig,
 *  and performing a secondary analysis, just on the words from a node and its close surround
 *  The result of this analysis would be ... 
 *   
 *   
 * --------------------------------------------------------
 * 
 * about online learning
 * 
 * note that any record that flows in MUST have some valid ID
 * the ID in the SOM should refer to the contextid NOT to the table id !!!!!!!!!!!!!!!!!!!!!!!
 * 
 * 
 * --------------------------------------------------------
 * 
 * Aster uses a different implementation for the BasicStatistics: without histograms in the node
 * Due to memory issues, we outsource the histograms into a different class, where we can deal 
 * more MEM-efficiently with it,... although we need additional effort to keep it up to date
 *  
 * 
 * Another issue that is important right from the beginning is persistence;
 * 
 * Data handling is very different to ModelOptimizer
 * 
 * If the count of nodes is beyond a certain threshold, say 150 or so, the
 * search for the best matching unit BMU will be organized completely different
 * In this case, 5..8..10 sustained processes are created, which run a queue.
 * The nodes are assigned in a fixed manner ( needs update in case of growth)
 * A particular record then is provided to each of these processes, and each of them
 * creates its own list of BMU, which then are matched  
 *  
 * A second issue is the missing target variable.
 * The validation is not based on TV. 
 *  
 * 
 * modeling needs internal statistics, such that variance is controlled 
 * additionally, we use variance of variances as a target target variable for intermittent optimization
 * 
 * the focus here is on incremental learning, that is, a proper update service
 * primary data source is a database
 * 
 * 
 * 
 * 
 * also, a second version will be made available for further processing, which is a "crystallization"
 * based upon adhoc multi-criteria optimization:
 *    given an initial SOM, a threshold is chosen dynamically, such that 
 *    - the distance between clusters is in a certain range
 *    - clusters comprise at least 5(+) nodes
 *    - the contrast (variance of diff) between clusters and valleys is larger than those between nodes 
 * 
 * This class is just an app-like wrapper, organizing references, processes and classes
 * 
 * 
 *  implements  
 *  
 */
public class SomAssociativeStorage 
									implements 
												SomHostIntf,
												SomAstorFrameIntf,
												ProcessCompletionMsgIntf{

	// objects above
	SomFluid somFluid ;
	SomFluidTask sfTask;
	SomFluidFactory sfFactory;
	
	// using observer in its own thread, is part of SomDataObj
	SomDataStreamer somDataStreamer ; 
	SomDataObject somDataObj;
	SomProcessIntf somProcess;
	
	SomTransformer somTransformer;
	
	SomFluidProperties sfProperties ;
	
	// objects below
	DSom dSom ;
	VirtualLattice astorSomLattice;
	LatticePropertiesIntf latticeProps; 

	//
	AstorDataBase astorDb;
	
	SomAstor somAstor;
	SomAstorNodeContent astorNodeContent;
	
	private SomTexxDataBase randomWordsDb;
	
	SomQueryIntf somQuery;
	
	private ArrayList<Integer> usedVariables = new ArrayList<Integer>();
	private boolean somprocessCompleted = false;
	
	String dataStreamProviderGuid="" ;

	
	
	transient PrintLog out = new PrintLog(2,true);
	private int databaseStructureCode;
	
	
	// ========================================================================
	public SomAssociativeStorage( SomFluid somfluid, 
								  SomFluidFactory factory, 
								  SomFluidTask sftask,
								  SomFluidProperties properties, 
								  String dspGuid) {
		
		somFluid = somfluid ;
		sfFactory = factory ;
		sfProperties = properties;
		
		this.sfTask = sftask;
		
		latticeProps  = new LatticeProperties();
		
		dataStreamProviderGuid = dspGuid;
		
	}
	// ========================================================================
	
	// care for data connection, incoming data link : we are learning incrementally !!

	public void close(){
		
		if (somAstor!=null){
			somAstor.clear(); // also stops the threads
		}
		
		if (astorNodeContent!=null){
			astorNodeContent.close();
		}
		
		astorDb.close() ;
		randomWordsDb.close() ;
	}
	
	public void perform() throws Exception{
		
		PersistenceSettings ps ;
		long serialID=0;
		serialID = SerialGuid.numericalValue();
		
		SomTargetedModeling stm;
		
		sfTask.setCallerStatus(0) ;
		 
		
		try{
			
			randomWordsDb = somDataObj.getSomTexxDb();
			
			// creating a separate instance of sfp in AstorProperties as a mirror of sfProperties
			// the sfp inside is INDEPENDENT !!!
			AstorProperties astorProperties = new AstorProperties(sfProperties);

			ps = astorProperties.getPersistenceSettings() ;
			ps.setConfigSqlResourceJarPath("org/NooLab/somfluid/app/astor/resources/sql");
			ps.setAppNameShortStr("astor") ;
			
			// create/care for database, read somid if it is available ...
			astorDb = new AstorDataBase( astorProperties ) ;
			
			astorDb.setInternalCfgStoreName("create-db-sql-xml");
			// alternative to using "PersistenceSettings" -> astorDb.setCfgResourceJarPath("") ;
			
			astorDb.setRandomWordsDb(randomWordsDb) ;
			astorDb.prepareDatabase("astornodes",0) ;
			   // .................
			
			
			// create the lattice
			somAstor = new SomAstor( this, sfFactory, sfProperties, sfTask, serialID);
			
			astorSomLattice = somAstor.getSomLattice();
			
			establishDbRegistration(astorSomLattice);
			
			// create/care for database, create entries 
			
			// SomQuery abstracts from the "physical" representation of the SOM:
			// it establishes a common interface for querying a live som (into the SomLattice,  
			// or into a representation that is externally stored, e.g. in a database or a sequential object store
			
			somQuery = SomQueryFactory.getInstance(astorSomLattice);
			// somQuery.setExternalStorage();
			
			int n = Math.max( astorSomLattice.getNodes().size() * 20 ,10000) ;
			n = 500;

			//<<<<<<<<<<<<<< TODO: DEBUG ONLY, remove this for production 
			                                
			astorNodeContent = new SomAstorNodeContent( this );
			astorNodeContent.registerObservedSomProcess( somAstor );
			
			
			//>>>>>>>>>>>>>>
			
			somAstor.setChangesThreshold(n);
			
			somAstor.prepare(usedVariables);
			
			String guid = somAstor.start();
			
			out.print(2, "\nSom-Astor  is running , identifier: "+guid) ; 

			// the node content collector should start earliest after initial instantiation of a full L1-SOM,
			// or after full resume
			// astorNodeContent = new SomAstorNodeContent( this );
//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< TODO: DEBUG DEACT ONLY , unblock for production !!!!!!!!!!!!!!!!!!			
			//astorNodeContent.registerObservedSomProcess( somAstor );
			
			while (somAstor.isRunning()==true){
				out.delay(10);
			}

			// ensure persistence
			
			
			// clear structures, stop threads
			somAstor.clear() ;
			
			
		}catch(Exception e){
			// restart option ... ?
			if (out.getPrintlevel()>=1){
				e.printStackTrace();
			}
		}
		
		somAstor = null;
	}
	

	private void establishDbRegistration(VirtualLattice somLattice) {
		
		long somId ;
		int r=-1;
		// note, that the somLattice maintains its own small ini store in order to provide startup persistence !!
		somId = somLattice.getNumGuid();
		
		// if the som-id is unknown to the table, it will create a new entry
		
		String nodecontentTable = ""; // for the root SOM, this is empty,
		                              // for derived offsprings, the names a re created and maintained
		                              // by "virtualLattice"??
		r = astorDb.updateSomLatticRegistration(somId,nodecontentTable) ;
		// 539910151985355102
		if (r!=0){
			
		}
		
	}
	public void setInitialVariableSelection(ArrayList<String>  vs){
		
		//ArrayList<Double> initialUsageIndicator = new ArrayList<Double>();
		
		if (somDataObj==null){
			return;
		}
		Variables variables = somDataObj.getVariables() ;
		String label;
		
		
		for (int i=0;i<vs.size();i++){
			label = vs.get(i);
			int ix = variables.getIndexByLabel(label);
			usedVariables.add(ix) ;
		}
		Collections.sort(usedVariables); 
		usedVariables.trimToSize();
		
	}

	
	public int prepareDataObject(int sourceMode, int dbStructureCode, String dbname) { 
		
		int sz = 0,limit = -1;
		int result = -1;
		databaseStructureCode = dbStructureCode ;
		
		
		try {

			
			
			if (sourceMode == SomAstorFrameIntf._ASTOR_SRCMODE_DB){

			}
			if (sourceMode == SomAstorFrameIntf._ASTOR_SRCMODE_FILE){
				
			}
			
			
			if (sfProperties.getSourceType() == DataStreamProviderIntf._DSP_SOURCE_DB){

				
				if ( sfFactory.getInstanceType() == FieldIntf._INSTANCE_TYPE_ASTOR){
					
					limit = 1000 ;
					somDataStreamer = new SomDataStreamer( this, sfProperties ) ;
					
				}else{
					limit = 80000 ; ;
				}
				
				
				result = -5 ;
				
				// this will read the DbAccess Object, ...as such it expects a "normal" datatable
				somDataObj = somFluid.loadDbTable( somDataStreamer, databaseStructureCode );
				result = -6 ;
				
				sz = somDataObj.getData().getRowcount();
			}
			
			if (sfProperties.getSourceType() == DataStreamProviderIntf._DSP_SOURCE_FILE){

				if (sfTask.getResumeMode() >= 1) {

				}
				
				result = -5 ;
				somDataObj = somFluid.loadSource(""); 
				result = -6 ;
				
				sz = somDataObj.getData().getRowcount();
				
			} // file

			if (sz>3){
				result = 0 ;
			}
		} catch (Exception e) {
			result = -17;
			e.printStackTrace();
		}
		return result;
	}

	public void setDataObject(SomDataObject sdo) {
		somDataObj = sdo;
	}

	@Override
	public void selectionEventRouter(SurroundResults results, VirtualLattice somLattice) {
		 
		somLattice.handlingRoutedSelectionEvent(results);
	}

	@Override
	public void processCompleted(Object processObj, Object msg) {
		 
		somprocessCompleted = true;
	}
	@Override
	public void onTargetedModelingCompleted(ModelProperties results) {
		 
		
	}
	@Override
	public SomFluidTask getSfTask() {
		
		return sfTask;
	}
	@Override
	public SomFluidFactory getSfFactory() {
		
		return sfFactory;
	}
	@Override
	public SomFluid getSomFluid() {
		
		return somFluid;
	}
	@Override
	public SomFluidProperties getSfProperties() {
		
		return sfProperties;
	}
	@Override
	public ModelProperties getSomResults() {

		return null;
	}
	@Override
	public SomDataObject getSomDataObj() {
		// 
		return somDataObj;
	}
	@Override
	public SomProcessIntf getSomProcess() {
		
		return somProcess;
	}
	@Override
	public ModelProperties getResults() {
		
		return null;
	}
	@Override
	public String getOutResultsAsXml(boolean asHtmlTable) {
		
		return null;
	}

	public SomDataStreamer getSomDataStreamer() {
		return somDataStreamer;
	}

	public VirtualLattice getAstorSomLattice() {
		return astorSomLattice;
	}

	public SomTexxDataBase getRandomWordsDb() {
		return randomWordsDb;
	}

	public AstorDataBase getAstorDb() {
		return astorDb;
	}

	public SomAstor getSomAstor() {
		return somAstor;
	}

	public SomAstorNodeContent getAstorNodeContent() {
		return astorNodeContent;
	}

	public SomQueryIntf getSomQuery() {
		return somQuery;
	}

	public ArrayList<Integer> getUsedVariables() {
		return usedVariables;
	}

	 
	
	 
	
}
