package org.NooLab.somfluid.app.astor;

import java.util.ArrayList;
import java.util.Collections;

import org.NooLab.field.FieldIntf;
import org.NooLab.field.repulsive.components.data.SurroundResults;
import org.NooLab.itexx.storage.DataBaseAccessDefinitionIntf;
import org.NooLab.itexx.storage.DataStreamProviderIntf;
import org.NooLab.itexx.storage.TexxDataBaseSettingsIntf;
import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.app.astor.query.SomQueryFactory;
import org.NooLab.somfluid.app.astor.query.SomQueryIntf;
import org.NooLab.somfluid.app.astor.storage.db.AstorDataBase;
import org.NooLab.somfluid.app.astor.stream.SomDataStreamer;

import org.NooLab.somfluid.app.astor.trans.SomAstorNodeContent;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.det.DSom;

import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.engines.det.SomTargetedModeling;
import org.NooLab.somfluid.core.engines.det.results.ModelProperties;
import org.NooLab.somfluid.core.nodes.LatticeProperties;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.env.data.SomTexxDataBase;
import org.NooLab.somfluid.lattice.VirtualLattice;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.storage.DataTable;
import org.NooLab.somfluid.structures.Variable;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.somfluid.tasks.SomFluidTask;

import org.NooLab.somsprite.ProcessCompletionMsgIntf;

import org.NooLab.somtransform.SomTransformer;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.SerialGuid;
import org.NooLab.utilities.strings.ArrUtilities;
import org.apache.commons.collections.CollectionUtils;



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
 *        
 * 
 * This class takes the same role as the ModelOptimizer or the SimpleSingleModel
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
 *  implements  
 *  
 */
public class SomAssociativeStorage 
									implements 
												SomHostIntf,
												SomAstorFrameIntf,
												ProcessCompletionMsgIntf{

	String astorDbResourcePath = "org/NooLab/somfluid/app/astor/resources/sql";
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
	
	/** this refers to the source of the node data: is it from L1 som (words) or from L2 Som (docs)*/
	int dbStructureCode =-1;
	
	private SomTexxDataBase randomWordsDb;
	
	SomQueryIntf somQuery;
	
	private ArrayList<Integer> usedVariables = new ArrayList<Integer>();
	private boolean somprocessCompleted = false;
	
	String dataStreamProviderGuid="" ;

	
	
	transient PrintLog out = new PrintLog(2,true);
	private int databaseStructureCode;
	private int prepareAbstraction;
	
	/** by powers of 10 */
	double nodesDbUpdateIntensity = 1.5;
	private boolean prepareFingerprints=false;
	
	
	// ========================================================================
	public SomAssociativeStorage( SomFluid somfluid, 
								  SomFluidFactory factory, 
								  SomFluidTask sftask,
								  SomFluidProperties properties, 
								  String dspGuid) {
		
		somFluid = somfluid ;
		sfFactory = factory ;
		sfProperties = properties;
		
		// general instantiation, even if we will not use it (but most likely we will do)
		somDataStreamer = new SomDataStreamer( this , sfProperties);
		
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
	
	/**
	 * 
	 * @param prepareabstraction if >0 a database for node content (only indexes) will be created
	 * @param dbName  e.g. "astornodes" if we create a L1 SOM from contexts, or
	 *                     "astornodes-L2" if we read L1 and create L2
	 *                     databases like "astornodes-L2" are needed for fast query of the SOM in order
	 *                     to avoid looping across the nodes over and over again
	 *                     The query will also use a db that contains the direct neighborhoods
	 *                     the neighborhoods table is created after learning has been finished
	 * @throws Exception
	 */
	public int perform( String dbName , int prepareabstraction ) throws Exception{
		
		int result = -1;
		PersistenceSettings ps ;
		long serialID=0;
		
		serialID = SerialGuid.numericalValue();
		
		SomTargetedModeling stm;
		
		sfTask.setCallerStatus(0) ;
		 
		prepareAbstraction = prepareabstraction ;
		
		TexxDataBaseSettingsIntf dbas = sfProperties.getDatabaseSettings() ;
		DataBaseAccessDefinitionIntf dbaccess = dbas.getDbAccessDefinition() ;
		dbStructureCode = dbaccess.getDatabaseStructureCode() ;
		
		
		try{
			
			randomWordsDb = somDataObj.getSomTexxDb();
			
			// creating a separate instance of sfp in AstorProperties as a mirror of sfProperties
			// the sfp inside is INDEPENDENT !!!
			AstorProperties astorProperties = new AstorProperties(sfProperties);

			ps = astorProperties.getPersistenceSettings() ;
			ps.setConfigSqlResourceJarPath( astorDbResourcePath );
			ps.setAppNameShortStr("astor") ;
			
			// create/care for database, read somid if it is available ...
			astorDb = new AstorDataBase( astorProperties ) ;
			
			astorDb.setInternalCfgStoreName("create-db-sql-xml");
			// alternative to using "PersistenceSettings" -> astorDb.setCfgResourceJarPath("") ;
			
			astorDb.setRandomWordsDb(randomWordsDb) ;
			
			if (prepareAbstraction>=1){
				
				/*  
				 * 	in some cases we need a random graph of the sequence of nodes;
              		for doing this, we need a numerical representation of the name == a fingerprint vector, 
              		which is used in the same way as we use it for words;
              		because it is expensive, we avoid it for SOM that we just query, without further abstraction 
				 */
				
				prepareFingerprints = (prepareAbstraction>=2); 
				
				astorDb.prepareDatabase( dbName,0, prepareFingerprints) ; // TODO: name should be dynamical
			}                       // the nodes db, e.g. astornodes or astornodes-L2
			else{
				if (dbName.length()>0){ // astornodes here
					astorDb.prepareDatabase( dbName,0, prepareFingerprints) ;	
				}
				// astorDb.prepareDatabase( dbName,0, prepareFingerprints)
				
			}
				// .................
			
			
			
			// create the lattice
			somAstor = new SomAstor( this, sfFactory, sfProperties, sfTask, serialID);
			
			astorSomLattice = somAstor.getSomLattice();
			
			if (prepareAbstraction>=1){
				establishDbRegistration(astorSomLattice);
			}
			
			// create/care for database, create entries 
			
			// SomQuery abstracts from the "physical" representation of the SOM:
			// it establishes a common interface for querying a live som (into the SomLattice,  
			// or into a representation that is externally stored, e.g. in a database or a sequential object store
			
			somQuery = SomQueryFactory.getInstance(astorSomLattice);
			// somQuery.setExternalStorage();
			

			//<<<<<<<<<<<<<< TODO: DEBUG ONLY, remove this for production ??? 

			astorNodeContent = new SomAstorNodeContent( this );
			astorNodeContent.registerObservedSomProcess( somAstor );
			
			//>>>>>>>>>>>>>>
			
			somAstor.setPrepareAbstraction(prepareAbstraction) ;
			somAstor.setDatabaseStructureCode(databaseStructureCode);
			
				int n = Math.max( astorSomLattice.getNodes().size() * 20 ,10000) ;
				n = 100;
			// update period for nodes db : adaptive per node size
			somAstor.setChangesThreshold(n); 
			// update period : manually, directly... both conditions must be fulfilled !!
			somAstor.setUpdatePeriodByChangeCount( (int) Math.pow(10, nodesDbUpdateIntensity) ); 
			
			
			somAstor.prepare(usedVariables);
			
			String guid = somAstor.start();
			
			out.print(2, "\nSom-Astor  is running , identifier: "+guid) ; 

			// the node content collector should start earliest after initial instantiation of a full L1-SOM,
			// or after full resume
			while (somAstor.isRunning()==true){
				out.delay(10);
				if (somAstor.initializationCompleted){
					break;
				}
			}
			
			if (somAstor.initializationCompleted){
				astorNodeContent = new SomAstorNodeContent( this );
				
//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< TODO: DEBUG DEACT ONLY , unblock for production !!!!!!!!!!!!!!!!!!			
				astorNodeContent.registerObservedSomProcess( somAstor );
				n=-1;
				while (somAstor.isRunning() == true) {
					out.delay(100);

					if (somAstor.calculationFinished) {
						out.delay(1000);
						if (n<0){
							n = astorNodeContent.getFailingNodeRequests().size() ;
							if (n>0){
								out.printErr(2, "For some nodes their entry in the database has not been updated due to id-mismatch (n="+n+")...");
							}
						}
						
					}
				}
			}
			// ensure persistence
			
			
			// clear structures, stop threads
			somAstor.clear() ;
			result = 0;
			
		}catch(Exception e){
			// restart option ... ?
			if (out.getPrintlevel()>=1){
				e.printStackTrace();
			}
			result = -7;
			if (somAstor!=null){
				somAstor.clear() ;
			}
		}
		
		somAstor = null;
		return result;
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
	@SuppressWarnings("unchecked")
	public void setInitialVariableSelection(ArrayList<String>  vs){
		
		//ArrayList<Double> initialUsageIndicator = new ArrayList<Double>();
		int n;
		Variables variables;
		ArrayList<String> excludedVars,blacklistVars,ixesVars,allVars, xVars,sVars;
		ArrayList<Variable> ixVars;
		
		if (somDataObj==null){
			return;
		}
		variables = somDataObj.getVariables() ;
		String label;
		
		if ((vs!=null) && (vs.size()>0)){
			for (int i = 0; i < vs.size(); i++) {
				label = vs.get(i);
				int ix = variables.getIndexByLabel(label);
				usedVariables.add(ix);
			}
		}
		// if autoselectMode = _ALL ( | _RANDOM_ | N<num> )       <<<<<<<<<<<<<< TODO: introduce a constant, String to parse
		if ((vs!=null) && (vs.size()==0)){
			if (usedVariables.size()<=1){
				variables = this.somDataObj.getVariables();
				excludedVars  = variables.getAbsoluteFieldExclusions();
				blacklistVars = variables.getBlacklistLabels() ;
				ixVars = variables.getAllIndexVariables();
				ixesVars = variables.getLabelsForVariablesList(ixVars);
				allVars = variables.getLabelsForVariablesList(variables);
				
				// we have the wrong list of excluded  here...  reference to a wrong database? where is it set before  
				xVars = (ArrayList<String>)ArrUtilities.interSection(allVars, excludedVars);
				
				xVars.addAll((ArrayList<String>) ArrUtilities.interSection(allVars, blacklistVars));
				xVars.addAll((ArrayList<String>) ArrUtilities.interSection(allVars, ixesVars));
				
				sVars = (ArrayList<String>) ArrUtilities.disjunctionLR(allVars, xVars,1) ;
				n = sVars.size();
				
				usedVariables.addAll( variables.getIndexesForLabelsList(sVars) ) ;
				
				n = sVars.size();
				n = blacklistVars.size();
				n = ixesVars.size();
			}
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
	public void addStreamingData(DataTable dataTable) {

		out.printErr(2, "\n>>> SomHost (=SomAssociativeStorage) received data (n:"+dataTable.getRowcount()+") through the streaming path...\n");
		somDataStreamer.addData( dataTable );
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

	public int getPrepareAbstraction() {
		return prepareAbstraction;
	}

	public void setPrepareAbstraction(int flag) {
		prepareAbstraction = flag;
	}

	public int getDbStructureCode() {
		return dbStructureCode;
	}

	public void setDbStructureCode(int dbStructureCode) {
		this.dbStructureCode = dbStructureCode;
	}

	public double getNodesDbUpdateIntensity() {
		return nodesDbUpdateIntensity;
	}
	/** defines period by number of node changes by powers of 10 , e.g. 2.5 = 250 */
	public void setNodesDbUpdateIntensity( double intensity ) {
		nodesDbUpdateIntensity = intensity;
	}

	 
	
	 
	
}
