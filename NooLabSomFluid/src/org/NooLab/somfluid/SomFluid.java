package org.NooLab.somfluid;


import java.util.ArrayList;

import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
 
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.objects.StringedObjects;
import org.NooLab.utilities.strings.ArrUtilities;

import org.NooLab.field.FieldIntf;
import org.NooLab.field.interfaces.FixedNodeFieldEventsIntf;
import org.NooLab.field.interfaces.PhysicalGridFieldIntf;
import org.NooLab.field.interfaces.RepulsionFieldEventsIntf;

import org.NooLab.itexx.storage.DataBaseAccessDefinitionIntf;
import org.NooLab.itexx.storage.DataStreamProvider;
import org.NooLab.itexx.storage.DataStreamProviderIntf;
import org.NooLab.itexx.storage.TexxDataBaseSettings;
import org.NooLab.itexx.storage.TexxDataBaseSettingsIntf;

import org.NooLab.somfluid.properties.* ;
import org.NooLab.somfluid.structures.VariableSettingsHandlerIntf;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.somfluid.tasks.SomFluidSubTask;
import org.NooLab.somfluid.tasks.SomFluidTask;
import org.NooLab.somfluid.tasks.SomFluidTaskIntf;
import org.NooLab.somfluid.tasks.SomSubTasks;
import org.NooLab.somfluid.tasks.SomTasks;
import org.NooLab.somfluid.app.astor.SomAStorageQueryHandler;
import org.NooLab.somfluid.app.astor.SomAssociativeStorage;
import org.NooLab.somfluid.app.astor.SomAstorFrameIntf;
import org.NooLab.somfluid.app.astor.stream.SomDataStreamer;
import org.NooLab.somfluid.clapp.SomAppProperties;
import org.NooLab.somfluid.clapp.SomAppUsageIntf;
import org.NooLab.somfluid.clapp.SomAppValidationIntf;
import org.NooLab.somfluid.clapp.SomApplicationEventIntf;
import org.NooLab.somfluid.components.* ;
  
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.engines.det.results.ModelOptimizerDigester;
import org.NooLab.somfluid.core.engines.det.results.SimpleSingleModelDigester;
  
import org.NooLab.somfluid.env.data.*;
import org.NooLab.somtransform.SomTransformer;
import org.NooLab.structures.InstanceProcessControlIntf;

import org.NooLab.itexx.storage.nodes.SomNodesDataConverter;
import org.NooLab.itexx.storage.nodes.SomTexxProperties;
import org.NooLab.itexx.storage.somfluid.db.DataBaseAccessDefinition;


/*

	When someone says, "This is really tricky code," I hear them say, "This is really bad code."
	- Steve McConnell
	
	Thus, no tricks are included here.
*/

/**
 * 
 * SomFluid is starting the learning process and is  
 * running a supervising process about the state and the activity of the network
 * 
 * SomFluid does not possess any capabilities for graphical output.
 * The graphics is a separate module hosted by the SomFactory, to which the Fluid connects via port sending;
 * format of exchange is standardized, such the displayed info can be switched easily 
 * 
 * 
 * TODO:
 * 
 * transposing the data table, 
 * - clustering variables
 * 
 *  cross-validation by Utans
 * 
 * 
 */
public class SomFluid 
                      implements Runnable,
								 SomFluidIntf,
								 
								 SomSupervisionIntf ,
								 RepulsionFieldEventsIntf,
								 FixedNodeFieldEventsIntf
								 {
	//    
	
	

	SomFluidProperties sfProperties;
	SomFluidFactory sfFactory ;
	InstanceProcessControlIntf somProcessControl ;
	
	ArrayList<SomDataObject> somDataObjects = new ArrayList<SomDataObject>();
	
	SomTasks somTasks;
	SomSubTasks somSubTasks;
	
	SomFluid sf ;
	SomAppProperties soappProperties;
	
	SomTransformer soappTransformer ;
	SomApplicationDsom somApplication;
	
	boolean isActivated=false, isInitialized=false;
	boolean processIsRunning=false;
	Thread sfThread;
	
	private boolean processIsActivated=false;
	// private RepulsionFieldIntf particleField;
	private PhysicalGridFieldIntf particleField;
	
	private boolean userBreak;
	transient SomApplicationEventIntf appInformer ;
	
	
	transient DFutils fileutil = new DFutils();
	transient StringedObjects sob = new StringedObjects();
	transient PrintLog out = new PrintLog(2, false, "[SomFluid]");
	
	
	// ------------------------------------------------------------------------
	protected SomFluid( SomFluidFactory factory){
		
		sfThread = new Thread (this,"sfThread" );
		somTasks = new SomTasks( sfFactory ) ;
		
		sfFactory = factory;
		sfProperties = sfFactory.getSfProperties() ;
		
		somProcessControl = sfFactory.getSomProcessControl() ;
		appInformer = sfFactory.appInformer ;
		
		sf = this;
		
	 
		if (sfFactory.preparePhysicalParticlesField>0){
			prepareParticlesField( (RepulsionFieldEventsIntf)this);	
		}
		cleanTmpFolders(); 
	}
	 
	private void cleanTmpFolders() {
		String tmpdir = fileutil.getTempDir(); // fileutil.createTempDir(""); // createTempDir("");
		// SomDataObject._TEMPDIR_PREFIX)
		DFutils.reduceFolderListByAge( tmpdir,20, "~noo", 2.3) ; // 2.3 days 
	}  

	// ========================================================================
	
	 
	public void prepareParticlesField( RepulsionFieldEventsIntf eventSink ){
		
		int initialNodeCount = sfProperties.getInitialNodeCount();
 
		out.setPrefix( "[SomFluid]") ; 
		out.print(2, "creating the physical node field for " + initialNodeCount + " particles... this could take a few seconds...");

		// we need to harvest the events here!
		if ((particleField != null) && (particleField.getNumberOfParticles() != initialNodeCount)) {
			particleField.close();
			particleField = null;
		}
		if (particleField == null) {
			
			// sfFactory.establishPhysicalFieldMessaging( this); // RepulsionFieldEventsIntf eventSink
			particleField = sfFactory.createPhysicalField( eventSink, initialNodeCount);
			// this one extra!
			// establishPhysicalFieldMessaging( RepulsionFieldEventsIntf eventSink)
		}

	}
	
 




	/**
	 * start this through message queue and task process,  
	 * 
	 * @param sfTask
	 */
	private void performTargetedModeling( SomFluidTask sfTask ) {
		 
		SimpleSingleModel simo ;
		SimpleSingleModelDigester simoResultHandler ;
		ModelingSettings modset = sfProperties.getModelingSettings() ;
			
		sfTask.setSomHost(null) ;
		somDataObjects.clear();
		
		simo = new SimpleSingleModel(this, sfTask, sfFactory );
		
		simoResultHandler = new SimpleSingleModelDigester( simo,sfFactory );
		sfTask.setSomResultHandler( simoResultHandler ) ;
		
		sfTask.setDescription("performTargetedModeling()") ;
		
		simo.prepareDataObject() ;
		simo.setInitialVariableSelection( modset.getInitialVariableSelection() ) ;
		
		simo.setSaveOnCompletion(true) ;
		
		simo.perform();
		// will return in "onTaskCompleted()"
		// handling requests about persistence: saving/sending model, results
		
		
	}
	

	private void performAssociativeStorage(SomFluidTask sfTask) throws Exception {
		 
		
		
		SomFluidSettings somfluidSettings ; 
		SomAssociativeStorage astor ;
		SomAStorageQueryHandler astorQueryHandler;
		TexxDataBaseSettingsIntf databaseSettings;
		DataStreamProvider dataStreamProvider ;
		
		boolean dataIsPrimary = true;
		
		somfluidSettings = sfProperties.getSomFluidSettings() ;
		databaseSettings = sfProperties.getDatabaseSettings() ;
		
		String str ="",dspGuid="",   dbname="", nodesDbName="";
		int r, databaseStructureCode = -1 ;
		
		sfTask.setDescription("Astor()") ;
		sfTask.setSomHost(null) ;
		 
		
		
		// are we going to create a secondary SOM?
		// then we have to read the "astornodes" database first... creating the input
		if (sfTask.getSourceDatabaseType() == SomFluidTaskIntf._SOURCE_DB_SOMNODES ){
			
			// this is due to a call from SomClients
			dataIsPrimary = false;
			
			str = sfProperties.getApplicationContext() ;
			sfProperties.setApplicationContext("itexx") ;
			// now we have to read the nodes database, e.g. astornodes ...
			// and create a table from that
			// this also checks if it is necessary at all, by comparing two "select distinct" for docid in two databases
			
			r = convertNodesToTable( sfTask ) ;
								
			if (r<0){
				throw(new Exception("Preparing the database for SomFluid process in performAssociativeStorage() failed ...\nSub-process will be stopped.")) ;
			}
			
			organizingDatabaseAccessForL2( sfTask );
			
			nodesDbName = "astornodes-L2";  
			// this will take the definitions for astornodes, if the name extension matches the definitions: extensions="-L*;-id*"
			// such, we can create different instances of a particular type of database
			/* TODO: actually, we need sth like a meta ID for the nodesDbName 
			*		 as it is neither bound to a session, nor to a L1 Som...
			*/
			
		}else{
		
					str = str + " " ;
		
		    // we accept streaming only if we work on a "data table" that we can read directly, NOT a nodes db!
		    if ((dataIsPrimary == true) && (sfTask.activateDataStreamReceptor())){
			
		    	int dspix = DataStreamProviderIntf._DSP_SOURCE_DB;
		    	// DataStreamProvider "receives" data from some data source like db, tcp, or file
		    	// it is part of iTexxStorage
		    	dataStreamProvider = new DataStreamProvider( dspix , databaseSettings, null );
			
		    	/*
			   	The type org.NooLab.itexx.comm.tcp.TexxCommTcpSettingsIntf cannot be resolved. 
			   	It is indirectly referenced from required .class files
			   
		    	  DataStreamProviderIntf._DSP_SOURCE_DB, 
							databaseSettings, 
					 		sfProperties.getDbAccessDefinition());
		    	*/
				dspGuid = dataStreamProvider.getGuid();
			
				TexxDataBaseSettingsIntf dbas = sfProperties.getDatabaseSettings() ;
				DataBaseAccessDefinitionIntf dbaccess = dbas.getDbAccessDefinition() ;
			
				dbas.setDatabaseName("randomwords");  // contains table "contexts" with field "randomcontext"
				
				databaseStructureCode = TexxDataBaseSettingsIntf._DATABASE_STRUC_CONTEXTS_L0;
				dbaccess.setDatabaseStructureCode( databaseStructureCode );
			}
		    
		    // TODO ACTUALLY this should come from the task
		    dbname = "randomwords"; // contains table "contexts"
		    nodesDbName = "astornodes";
		    
		    databaseStructureCode = TexxDataBaseSettingsIntf._DATABASE_STRUC_CONTEXTS_L0;
		    
		    VariableSettingsHandlerIntf varSett = sfProperties.getVariableSettings();
		    if (varSett.getIdVariable().length()==0){
		    	varSett.setIdVariable("id");
		    }
		} // ? else
		// else ...in normal mode
		
		astor = new SomAssociativeStorage( this, sfFactory, sfTask, sfProperties, dspGuid ); 
		sfTask.setTaskType( SomFluidTask._TASK_SOMSTORAGEFIELD);
		
		// reading the data is buggy !!! 
		r = astor.prepareDataObject( SomAstorFrameIntf._ASTOR_SRCMODE_DB, databaseStructureCode, dbname) ; 
		
		if (r==0){
			astor.setInitialVariableSelection( somfluidSettings.getInitialVariableSelection() ) ;
			
			
			// extensionality contains strange data, else we should fill the secondary index field
			r = astor.perform( nodesDbName, sfTask.getPreparingAbstraction() ) ;
		}

		
	}

	private void organizingDatabaseAccessForL2( SomFluidTask sfTask ) throws Exception {

		String dbname;
		int databaseStructureCode = TexxDataBaseSettingsIntf._DATABASE_STRUC_RNDDOCS_L1;
		
		
		TexxDataBaseSettingsIntf dbas = sfProperties.getDatabaseSettings() ;
		DataBaseAccessDefinitionIntf dbaccess = dbas.getDbAccessDefinition() ;
		
		dbas.setDatabaseName("astordocs");

		dbname = "astordocs"; // contains table "randomdocuments", made from histogram upon a primary SOM
		dbname = sfTask.getSourceDatabaseName();

		dbaccess.setDbUser("sa");
		dbaccess.setDbpassword("sa");
		dbaccess.setDatabaseName(dbname) ;
		
		// 
		sfProperties.getDatabaseDefinitionInfo("astordocs",TexxDataBaseSettingsIntf._DATABASE_STRUC_RNDDOCS_L1);
		//
 		
		dbaccess.setDatabaseStructureCode( databaseStructureCode );
		
		sfProperties.setDatabaseSettings((TexxDataBaseSettings) dbas) ;
		
		VariableSettingsHandlerIntf varSett = sfProperties.getVariableSettings();
		varSett.setIdVariable("id");
		varSett.setIdVariableCandidates( new String[]{"*id","*index","*ix"});
		varSett.setTargetVariable("");
		varSett.getInitialSelection().add("rd_*") ;
		// we have to set the index column !!!
		// eventually removing persistence files...  somDataObject.ensureTransformationsPersistence
		
		// int[] gm = sfProperties.getGrowthModes() ;
		// int n = gm.length ;
		
	}

	private int convertNodesToTable(SomFluidTask sfTask) throws Exception {
		int result = -1;
		
		String storagedir ;
		long somID;
		

		DataBaseAccessDefinition dba = new DataBaseAccessDefinition();
		SomTexxProperties stxProperties = new SomTexxProperties();
		SomNodesDataConverter nodesDataConverter;

		
		dba.setUser( sfProperties.getPersistenceSettings().getDbUser() );
		dba.setPassword( sfProperties.getPersistenceSettings().getDbPassword() ) ;
		dba.setDatabaseName( sfTask.getSourceDatabaseName() );
			// String str = sfTask.getSourceDatabaseName();
			
		stxProperties.setDocSomDatabaseAccessSettings(dba) ;	
			
		// probably we need a different dbaccess then after extracting the data from nodes ...
		stxProperties.setContext( InstanceProcessControlIntf._APP_CONTEXT_ITEXX ) ;
		stxProperties.setDatabaseAccessSettings(dba);
		
		// we need the id of the astor SOM (L1) that shall serve as input for L2
		stxProperties.setSomId( sfTask.getTransferSourceSomId() );
		// somclients client is initiated through SomAstor, which knows about the somId...
	
		stxProperties.setNodesSomDataBaseName(  sfTask.getTransferSourceDatabase() ) ;
		stxProperties.setDocSomDataBaseName(  sfTask.getTransferTargetDatabase() ) ;
			// String dbname = sfTask.getTransferTargetDatabase();

			
		storagedir = DFutils.createPath(sfProperties.getSystemRootDir(),"storage") ;
			         if (fileutil.listofFiles("", ".db", storagedir).size()==0){
			        	 throw(new Exception("No data base for SomFluid process in performAssociativeStorage()..."));
			         }
		stxProperties.setStorageDir(storagedir) ;
		
		somID = sfTask.getTransferSourceSomId() ; // the ID of the AstorNodes Som
		 								out.print(2, "accessing Som Nodes for extracting advanced preparations (histograms etc.)...");
		 								
		// sth is wrong there, the context id is not caught correctly 								
		nodesDataConverter = new SomNodesDataConverter( stxProperties ) ; 
		
		if (nodesDataConverter.checkForImport() ){
		
			ArrayList<ArrayList<?>> histogramTable = nodesDataConverter.createHistogramTable(somID);
		
			// this histogram table will be saved into a database
			// see Texx/randomGraph for this 
			// texx.txxDataBase.storeDocTable(doctable);
			result = nodesDataConverter.storeDocTable(histogramTable);
										out.print(2, "extracting done...");
										
		}else{
			result=0;
		}
		
		return result;
	}

	/**
	 * 
	 * optimizes a single model, using evo-screener and sprite-derivation
	 * 
	 * @param sfTask
	 * @throws Exception 
	 */
	private void performModelOptimizcreener(SomFluidTask sfTask) throws Exception {
		
		ModelOptimizer moz ;
		ModelOptimizerDigester optimizerResultHandler ;
		somDataObjects.clear();
		
		sfTask.setDescription("ModelOptimizer()") ;
		sfTask.setSomHost(null) ;
		
		
		moz = new ModelOptimizer( this , sfTask, sfFactory );

		optimizerResultHandler = new ModelOptimizerDigester( moz , sfFactory);
		sfTask.setSomResultHandler( optimizerResultHandler ) ;
		
		moz.checkConsistencyOfRequest() ;
		
		moz.perform();
		// will return in "onTaskCompleted()"
		
		/*
		 * 
		 * don't forget about SomBags as kind of ensemble learning !!!
		 * 
		 */
		
		
	}
	
	
	/**
	 * 
	 * creates a series of models, using slight variation in "strong" parameters such as
	 * max number of clusters, ECR-catalog (esp. 0-alpha, 0-beta models)
	 * 
	 * @param sfTask
	 */
	private void performSpelaMetaModeling(SomFluidTask sfTask) {
		
		
	}


	private void performClassificationApp(SomFluidTask sfTask) {
		
		String classTaskId = "-1" ;
		
		SomApplication soapp;
		// result handler ?
		
		soapp = new SomApplication(this , sfTask, sfFactory) ;
		
		sfFactory.setSomApplication(soapp) ;
		somDataObjects.clear();
		
		sfTask.setDescription("SomApplication()") ;
		sfTask.setSomHost(null) ;
		sfTask.setTaskType( SomFluidTask._TASK_CLASSIFICATION);
		
		// setMessagePort( SomApplicationEventIntf msgCallbackIntf );
		try {
			
			soapp.loadSource( soapp.soappProperties.getDataSrcFilename() ) ;
			
			if (soapp.loadModel() ){
			
				classTaskId = soapp.perform();
				
				if (appInformer != null){
					appInformer.onProcessStarted( sfTask, FieldIntf._INSTANCE_TYPE_CLASSIFIER, classTaskId) ;
				}
			}else{
				String str = soapp.getLastStatusMessage();
				
				if (appInformer != null){
					appInformer.onStatusMessage( sfTask, FieldIntf._INSTANCE_TYPE_CLASSIFIER, -1, str) ;
											// -1 ... we should use a centralized error code directory instead
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}


	public void exportModel( String taskGuid, String packageName)  throws Exception{
		
		// we need an available ModelOptimizer instance, or a simpleSom instance...
		// everything is accessible through the tasks admin
		SomFluidTask sfTask ;
		
		sfTask = somTasks.getItemByGuid(taskGuid);
		
		if (sfTask!=null){
			SomHostIntf somhost = sfTask.getSomHost();
			if (somhost!=null){
				exportModel( somhost, packageName);
				sfTask.setExported(true);
			}
		}
		
	}

	private void exportModel(SomFluidTask sfTask)  throws Exception{
		
		if (sfTask!=null){
			SomHostIntf somhost = sfTask.getSomHost();
			if (somhost!=null){
				exportModel( somhost, "");
				sfTask.setExported(true);
			}
		}
	}
	
	private void exportModel( SomHostIntf somHost, String packageName) throws Exception{
		
		int modelcount=0;
		String xRootDir = "",prj,dir,tfilename, sfilename,filename;
		String pkgsubdir="0", somXstring = "", transformXstring = "",srXstring = "";
		SomObjects somObjects;
		
		boolean enforceExport=false;
		
		
		if (somHost==null){
			throw(new Exception("The structure hosting the model was null."));
		}
		if ((somHost.getSfTask().isExported()) && (enforceExport==false)){
			throw(new Exception("The model has already been exported (enforcing the export is disabled)."));
		}
		
		
		// goal for dir = D:/data/projects/bank2/export/packages/ ....
		dir = sfProperties.getSystemRootDir();                        // D:/data/projects/
		prj = sfProperties.getPersistenceSettings().getProjectName(); // +"bank2" -> D:/data/projects/bank2/
		
		dir = DFutils.createPath(dir, prj);
		dir = DFutils.createPath(dir, "export/packages/"); // here we maintain a small meta file: latest export, date in days since,
		  												   // D:/data/projects/bank2/export/packages/
		
		xRootDir = fileutil.createEnumeratedSubDir( dir, "", 0, 19, -3 ); // 19 = maxCount, -3 = remove oldest by date, -2 = remove first by sort 
		  
		ModelOptimizer moz = (ModelOptimizer )somHost ; // ;
		
		
		somObjects = sfFactory.getSomObjects();
		// somTransformer: extractTransformationsXML()

			boolean embed = sfProperties.getPersistenceSettings().isExportTransformModelAsEmbeddedObj() ;
			// creating an xml image
			moz.getSomDataObj().getTransformer().getSelfReference().determineRequiredRawVariablesByIndexes( moz.getOutresult().getBestMetric().getVarIndexes() );
			
			moz.getSomDataObj().getTransformer().extractTransformationsXML(embed);
			
			ArrayList<String> txstrings = moz.getSomDataObj().getTransformer().getXmlImage() ;
			
			transformXstring = ArrUtilities.arr2Text(txstrings,"\n");
					
            tfilename = DFutils.createPath(xRootDir, "transform.xml");
				
		// also ensures sufficient statistical description;
		// we may have several SOMs, in case of bags or ensembles !!
            
        // first we extract the xml ...
        modelcount = sfFactory.getSomObjects().extractSomModels();
        // ...then we retrieve it
		ArrayList<String> sxstrings = somObjects.getXmlImage() ;
		
		if ((sxstrings.size()<=1) || (txstrings.size()<=1) ){
			throw(new Exception("There are no models to export, or the translation of models into xml failed.")) ;
		}
		
		// organizational conditions, such as time of auto-invalidation
		// save this String to the export directory
											
											
		somXstring = ArrUtilities.arr2Text( sxstrings ,"\n") ;
											// int p = somXstring.indexOf( "<node index")+500;
											// out.print(2, "\n"+ somXstring.substring(0,p) );
		sfilename = DFutils.createPath(xRootDir, "som.xml");
		
		fileutil.writeFileSimple(tfilename, transformXstring);
		fileutil.writeFileSimple(sfilename, somXstring);
		
		if (sfProperties.getOutputSettings().writeResultFiles()){
			String rpath = sfProperties.getOutputSettings().getLastResultLocation();
			if (fileutil.direxists(rpath)){
				String refMsg = "location of exported package :\t "+xRootDir;
				filename = fileutil.createpath(rpath, "packageExportsLocation.txt");
				fileutil.writeFileSimple(filename, refMsg);
			}
		}
		
		/*
		 *  there are essentially those two files, transform.xml, and som.xml
		 *   ::  +2  if it is required that the data should be included (original and transformed will be written)
		 *   ::  +1  if the results package should be included
		 *  
		 *  nested som models or bags/ensembles of models are all combined into a single xml
		 */
		
		if (sfProperties.getOutputSettings().isIncludeResultsToExportedPackages()){
			boolean asHtmlTable=false ; // TODO: on option
			
			sfProperties.getOutputSettings().setAsXml(true);
			srXstring = somHost.getOutResultsAsXml(asHtmlTable);
			
			filename = DFutils.createPath(xRootDir, "somresults.xml");
			fileutil.writeFileSimple(filename, srXstring);	
		}
		
		if (sfProperties.getOutputSettings().isIncludeDataToExportedPackages()){
			
			filename = DFutils.createPath(xRootDir, sfProperties.getPersistenceSettings().getProjectName().trim()+ ".txt");
			somHost.getSomDataObj().getData().saveTableToFile( filename );
			
			filename = DFutils.createPath(xRootDir, sfProperties.getPersistenceSettings().getProjectName().trim()+ "_norm.txt");
			somHost.getSomDataObj().getNormalizedDataTable().saveTableToFile( filename );
		}
		
		if (sfProperties.getOutputSettings().isExportApplicationModel()){
			
			SomAppPublishing appPublishing = sfProperties.getOutputSettings().getAppPublishing();
			appPublishing.publishApplicationModel( xRootDir ) ;
		
			if (sfProperties.getOutputSettings().isZippedExportedPackages()){
				// create a zip package from exported directory: everything is in the folder <xRootDir>
				// naming: include base path and project name into the name, as well as a properties file which we create on the fly
				// TODO:  satisfy "setZippedExportedPackages()"
			}
		} // exporting ?
	}




	@Override
	public void onTaskCompleted( SomFluidTask sfTask ) {
		 
		out.printErr(1, "\nSomFluid task has been completed, returning instance : "+ sfTask.getDescription()+", "+sfTask.getSomHost().toString().replace("org.NooLab.somfluid.", "")+"\n" );
		
		/* sfTask contains a reference to 
		 *   - somHost
		 *   - the SomResultDigesterIntf
		 * thus we just can call the interface's method ...
		 * the routing to the appropriate class will be taken "automatically" (we prepared it in the task specific method)
		 * 
		 * it will use persistence settings
		 */
		
		
		
		// 
		
		if (sfTask.isCompleted()){
			
			// not active yet... SomResultDigesterIntf resultHandler = sfTask.getSomResultHandler();
			// not active yet... resultHandler.handlingResults() ;
			
			if (SomFluidTask.taskIsModelOptimizer(sfTask)){ 

				SomFluidTask _task = sfFactory.somFluidModule.somTasks.getItemByGuid(sfTask.getGuidID());
				if (_task == null) {
					somTasks.add(sfTask);
				}

				// put the model to the list
				// provide an option ...
				sfFactory.getSomObjects().addSom(sfTask.getSomHost(), sfTask.getGuidID());

				ModelOptimizer moz = (ModelOptimizer) sfTask.getSomHost();
				moz.saveResults();

				if (sfProperties.getOutputSettings().isExportApplicationModel()) {
					// call as task ?
					try {

						exportModel(sfTask);

					} catch (Exception e) {
						e.printStackTrace();
					}
					sfTask.setExported(true);
					
					out.printErr(2, "\nThe following task has been finished, exported and closed: " + sfTask.getGuidID() + "\n");
				} else {
					out.printErr(2, "\nThe following task has been finished and closed: " + sfTask.getGuidID() + "\n");
				}
			}
		} // completed "ModelOptimizer"
		
		if (SomFluidTask.taskIsSomApplication(sfTask)){ 
			
			out.printErr(2,"\nThe following task has been finished and closed: "+sfTask.getGuidID()+"\n");
			
		}  // completed "SomApplication"
	}




	@Override
	public void statusMessage(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCalculationsCompleted() {
		
		if (sfFactory.getPhysicalFieldStarted()==0){
			out.print(2,"Calculations (SomFluid as event mgr) in particle field are going to be completed, please wait...");
			
		}
		
		
		// sfFactory.setPhysicalFieldStarted(1);
		
		sfFactory.getFieldFactory().setInitComplete(true);
		
	}

	// ========================================================================
	public String addTask(SomFluidTask somFluidTask) {
		 
		somFluidTask.setGuidID(GUID.randomvalue()) ;
		
		somTasks.setSfFactory(sfFactory) ;
		
		somTasks.add(somFluidTask);
		
		 
		out.setPrefix( "[SomFluid]") ; 
		out.print(2, "...now there are "+somTasks.size()+" tasks in the SomFluid-queue...") ; out.delay(100) ;
		
		isActivated = true;
		
		return somFluidTask.getGuidID()  ;
	}


	public String addTask(SomFluidSubTask somFluidSubTask) {
		
		somSubTasks.setSfFactory(sfFactory) ;
		
		somSubTasks.add(somFluidSubTask);
		
		return somFluidSubTask.getGuid()  ;
	}
	

	@Override
	public void start() {
		if (processIsActivated){
			return;
		}
		processIsActivated=true;
		if(processIsRunning==false){
			sfThread.start();
			isActivated = true;
		}
	}

	
	@Override
	public void run() {
		
		boolean isWorking = false;
		SomFluidTask sfTask = null;
		
		processIsRunning=true;
		
		try{
			
			while (processIsRunning){
				
				// out.print(2,"task dispatching process is running (tasks:"+somTasks.size()+") ");
				
				if ((isWorking==false) && (isActivated)){//) && (isInitialized)){
					isWorking=true;
											out.print(5,"...tdp (2)") ;
					if ((somTasks!=null) && (somTasks.size()>0)){
										    out.print(5,"...tdp (3)") ;
					    // take the first in queue
						sfTask = somTasks.getItem(0) ;
						// actually, we need a selection loop??? to get the FIRST non-treated one?
						
						if ((sfTask!=null) && (sfTask.getTaskDispatched()==0) && (sfTask.isCompleted()==false) && (sfTask.isExported()==false)){
							out.print(2,"\nworking on task, id = "+sfTask.getGuidID());
							int n = sfProperties.getModelingSettings().getOptimizerSettings().getMaxStepsAbsolute();
							
							if (sfProperties.isITexxContext()==false){
								out.print(2,"expected infimum number of explored variable combinations : " +n+"\n");
							}
									 
							sfTask.setTaskDispatched(1);
							TaskDispatcher td = new TaskDispatcher(sfTask);
							
							if (td.isWorking==false){
								out.print(5,"...tdp (91)") ;
								isWorking=false;
							}

						}else{
							isWorking=false;	
						}
											out.print(5,"...tdp (4)") ;
					}else{
						isWorking=false;
					}
					
				}
				
				if ((isWorking) && (somTasks.size()>0) && (somTasks.getItem(0).isCompleted())){
											out.print(5,"...tdp (6)") ;
					out.print(2,"task ("+somTasks.getItem(0).getGuidID()+") has been completed.\n"); // yet, the completion flag is set by the process itself !
					somTasks.setStopped(true) ; // remove(0);
					isWorking=false;
				}
				out.delay(500);
			}
			
											out.print(5,"...tdp (-1)") ;
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	// ..........................................
	class TaskDispatcher{

		private boolean isWorking = false;

		public TaskDispatcher(SomFluidTask sfTask) {
			 
			String _typeId = sfTask.getTaskType().toLowerCase();
			
			try{
				
				somTasks.add(sfTask) ;
				
				
				// dependent on task we invoke different methods and worker classes
				
				if ( SomFluidTask.taskIsModeling( _typeId ) ){ // replace by a proc + constant : modeling
					
					
					if (sfTask.getSomType() == FieldIntf._SOMTYPE_PROB ){
						// _INSTANCE_TYPE_ASTOR
						performAssociativeStorage(sfTask) ;
					}
					if (sfTask.getSomType() == FieldIntf._SOMTYPE_MONO){
						
						/*
						// accessing the persistent file,
						// it may be an external file containing raw data, or
						// if sth exists an already prepared one
						if (sfTask.workingMode == SomFluidTask._SOM_WORKMODE_FILE){ // ==default
							// r = sfFactory.loadSource();
							
						}
						
						if (sfTask.workingMode == SomFluidTask._SOM_WORKMODE_PIKETT){ 
							// goto standby mode for this task
							sfTask.isStandbyActive = true;
						}
						*/
						if (sfTask.getSpelaLevel()<=1){
						// preparing the data, at least transforming and normalizing it
						// is embedded into the SomDataObject, where it is called by
						// importDataTable()
						// (of course, it can be called separately too,
						
							performTargetedModeling( sfTask );
							this.isWorking=true;
						}
						if (sfTask.getSpelaLevel()==2){
							performModelOptimizcreener(sfTask) ;
							this.isWorking=true;
						}
					}else{
						// multi class learning
					}
				}
				if ( SomFluidTask.taskIsSomStorageFields( _typeId )  ){ //  associative storage
					
					performAssociativeStorage( sfTask );
					this.isWorking=true;
				}
				if ( SomFluidTask.taskIsClassification( _typeId ) ){ //  classifying data using an existent model
					performClassificationApp( sfTask );
				}
				
				  
				
			}catch(Exception e){
				e.printStackTrace();
				this.isWorking=false;
			}
			
		}


		


		
		
	} // inner class TaskDispatcher
	 

	// ========================================================================

	private SomDataObject createSomDataObject(TexxDataBaseSettingsIntf databaseSettings) {

		
		SomDataObject _somDataObject;
		
		_somDataObject = createSomDataObject() ;
		
		_somDataObject.setDatabaseSettings( sfProperties.getDatabaseSettings() );
		_somDataObject.setDbAccessDefinition( sfProperties.getDbAccessDefinition() ) ;

		return _somDataObject;
	}
	
	public SomDataObject createSomDataObject() {
		SomDataObject _somDataObject;
		
		
		try{
			

			_somDataObject = new SomDataObject(sfProperties,sfFactory.getSfProperties()) ;
			
			_somDataObject.setFactory(sfFactory);
				
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
		_somDataObject.setOut(out);
		 
		_somDataObject.prepare();
		
		somDataObjects.add(_somDataObject) ;
		
		_somDataObject.setIndex(somDataObjects.size()-1);
		
		return _somDataObject;
	}

	// ========================================================================
	
	public SomDataObject loadProfileSource( String srcName ) throws Exception{
		SomDataObject somDataObject;
		
		
		somDataObject = createSomDataObject() ;
	
		
		SomTransformer transformer = new SomTransformer( somDataObject, sfProperties );
		
		somDataObject.setTransformer(transformer) ;
		
		DataReceptor dataReceptor = new DataReceptor( somDataObject );
		
		// establishes a "DataTable" from a physical source
		dataReceptor.loadProfilesFromFile( srcName );
	
		// imports the DataTable into the SomDataObject, and uses a SomTransformer instance 
		// in order to provide a basic numeric version of the data by calling SomTransformer.basicTransformToNumericalFormat()
		somDataObject.importProfilesTable( dataReceptor, 1 ); 
		
		
											out.print(4, "somDataObject instance @ loadSource : "+somDataObject.toString()) ;
											
		return somDataObject;
	
	}
	
	public SomDataObject loadSource( String srcname, SomTransformer transformer ) throws Exception{
	
		return null;
	}
	
	public SomDataObject loadDbTable( SomDataStreamer streamer, int databaseStructureCode) {
	
		DataBaseAccessDefinition dbAccess;
		
		
		SomDataObject somDataObject = null ;
		
		 
		
		try {
			
			dbAccess = sfProperties.getDbAccessDefinition() ;
			
			
			if (databaseStructureCode == TexxDataBaseSettingsIntf._DATABASE_STRUC_CONTEXTS_L0){

				if ((dbAccess==null) || (dbAccess.getxColumns()==null) || (dbAccess.getxColumns().getItems().size()==0)){
					
					sfProperties.getDatabaseDefinitionInfo("randomwords",TexxDataBaseSettingsIntf._DATABASE_STRUC_CONTEXTS_L0);
					 
				}
				
			}
			if (databaseStructureCode == TexxDataBaseSettingsIntf._DATABASE_STRUC_RNDDOCS_L1){
				if ((dbAccess==null) || (dbAccess.getxColumns()==null) || (dbAccess.getxColumns().getItems().size()==0)){
					
					sfProperties.getDatabaseDefinitionInfo("astordocs", TexxDataBaseSettingsIntf._DATABASE_STRUC_RNDDOCS_L1 );
					 
				}
			}
			
		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}
		
		
		
		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> difference to filemode
		somDataObject = createSomDataObject( sfProperties.getDatabaseSettings() ) ;
		somDataObject.setSomDataStreamer( streamer ); 
		
		// correct db defined now ?

		
		SomTransformer transformer = new SomTransformer( somDataObject, sfProperties );
		
		somDataObject.setTransformer(transformer) ;
		
		DataReceptor dataReceptor = new DataReceptor( somDataObject );
		
		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> difference to filemode
		
		dataReceptor.loadFromDataBase( 1000 , databaseStructureCode);
		// the data loaded there is just a seed for the beginning...
		// in resume mode, we load the Som and the SomDataObj directly
		// yet, the data table in SomDataObj will not grow beyond 5000 records or so!!!
		// max and min are also taken from the db, so the global values for normalization are available anyway
		// de-referencing is done through the database
		            
		somDataObject.importDataTable( dataReceptor, 1 ); 

		if (somDataObject.getData().getRowcount()<=1){
			out.printErr(1, "loading data tables did NOT complete successfully. ...stopping.");
			// this.sfFactory.c
			somProcessControl.interrupt(0);
			out.delay(500) ;
			System.exit(-5) ;
		}

		
		// as defined by the user
		somDataObject.acquireInitialVariableSelection();
		
		// 
		somDataObject.ensureTransformationsPersistence(0);
											out.print(4, "somDataObject instance @ loadSource : "+somDataObject.toString()) ;
								
		 
		Variables variables = somDataObject.getVariables() ;
 
		variables.setAbsoluteFieldExclusions( sfProperties.getAbsoluteFieldExclusions() );
		
		// translating wildcards into accurate labels, also sets id,tv indicators in variable items
		variables.explicateGenericVariableRequests();
		
		Variables av;
		av = somDataObject.getActiveVariables();
		
		av.setUsageIndicationVector( variables.getUsageIndicationVector() ) ;
		av.setInitialUsageVector( variables.getInitialUsageVector() );
		

		// int[] use = variables.getUseIndicatorArray() ;
		
		
		if ((somDataObject.getVariablesLabels()==null) || (somDataObject.getVariablesLabels().size()==0)){
			
			somDataObject.updateVariableLabels();
			
		}
		
		return somDataObject;
	}

	
	


	public SomDataObject loadSource( String srcname ) throws Exception{
		
		SomDataObject somDataObject;
		 
		String srcName ="";
		
		
		
		//  
		srcName = srcname;
		if ((srcName.length()==0) || (DFutils.fileExists(srcName)==false)){
			srcName = sfProperties.getDataSrcFilename();
		}
		
		
		// check whether there is already an SDO
		/*
		if (somDataObjects==null){
			somDataObjects = new ArrayList<SomDataObject>();
		}
		for (int i=0;i<somDataObjects.size();i++){
			somDataObject = somDataObjects.get(i) ;
			loadedsrc = somDataObject.getDataReceptor().getLoadedFileName() ;
			if (loadedsrc.contentEquals(srcname)){
				// return _somDataObject;
			}
			somDataObjects.get(0).clear();
		}
		
		*/
		
		somDataObject = createSomDataObject() ;
	
		
		SomTransformer transformer = new SomTransformer( somDataObject, sfProperties );
	
		somDataObject.setTransformer(transformer) ;
		
		DataReceptor dataReceptor = new DataReceptor( somDataObject );
		
		// establishes a "DataTable" from a physical source
		dataReceptor.loadFromFile(srcName);
	
		Variables variables = somDataObject.getVariables() ;
		
		// imports the DataTable into the SomDataObject, and uses a SomTransformer instance 
		// in order to provide a basic numeric version of the data by calling SomTransformer.basicTransformToNumericalFormat()
		somDataObject.importDataTable( dataReceptor, 1 ); 
		
		// as defined by the user
		somDataObject.acquireInitialVariableSelection();
		
		// 
		somDataObject.ensureTransformationsPersistence(0);
											out.print(4, "somDataObject instance @ loadSource : "+somDataObject.toString()) ;
								
		 
		
 
		variables.setAbsoluteFieldExclusions( sfProperties.getAbsoluteFieldExclusions() );
		
		// translating wildcards into accurate labels, also sets id,tv indicators in variable items
		variables.explicateGenericVariableRequests();

											
		somDataObject.save();
		
		return somDataObject;
	
	} 



	public boolean loadLastOfKnownTransformerModels( SomDataObject somDataObj ) {
		
		SomTransformer somTransformer;
		 
		somTransformer = somDataObj.getTransformer().getSelfReference();
		
		return false;
	}
 


	public SomDataObject getSomDataObject(int index) {
		SomDataObject _somDataObject=null;
		if ((index>=0) && (index<somDataObjects.size())){
			_somDataObject = somDataObjects.get(index) ;
		}
		
		return _somDataObject;
	}

 
	
	// --- Events from RepulsionField / physical particle field ---------------
	
	

	/**
	 * @return the somDataObjects
	 */
	public ArrayList<SomDataObject> getSomDataObjects() {
		if (somDataObjects==null){
			somDataObjects = new ArrayList<SomDataObject>() ;
		}
		return somDataObjects;
	}


	/**
	 * @param somDataObjects the somDataObjects to set
	 */
	public void setSomDataObjects(ArrayList<SomDataObject> somDataObjects) {
		this.somDataObjects = somDataObjects;
	}

	public void addSomDataObjects(SomDataObject somDataObj) {
		// 
		somDataObjects.add(somDataObj) ;
	}




	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}

	// ,
  	// these are public, but clients anyway have access only through the factory,
	// and the factory provides only the usage interface
	public SomAppValidationIntf getSomValidationInstance(){
		if (somApplication==null){
			somApplication = new SomApplicationDsom();
		}
		return (SomAppValidationIntf)somApplication ;
	}
	
	public SomAppUsageIntf getSomUsageInstance(){
		if (somApplication==null){
			somApplication = new SomApplicationDsom();
		}
		return (SomAppUsageIntf)somApplication ;
	}

	// ------------------------------------------------------------------------
	
	public SomTransformer getSoappTransformer() {
		
		if (soappTransformer==null){
			soappTransformer = new SomTransformer( sfFactory, sfProperties );
		}
		return soappTransformer;
	}

	public void setSoappTransformer(SomTransformer soappTransformer) {
		this.soappTransformer = soappTransformer;
	}

	public void setApplicationMessagePort( SomApplicationEventIntf msgCallbackIntf) {
		appInformer = msgCallbackIntf;
	}




	public void registerDataReceptor(DataFileReceptorIntf datareceptor ) {
		somDataObjects.get(0).registerDataReceptor(datareceptor );
				
	}




	/**
	 * @return the particleField 
	 */
	public PhysicalGridFieldIntf getParticleField() {
	//public RepulsionFieldIntf getParticleField() {
		return particleField;
	}


	// ------------------------------------------------------------------------
	// callbacks from RepulsionField 



	@Override
	public void onSelectionRequestCompleted(Object results) {
		out.printErr(1,"SomFluid, receiving result message...") ;
	}




	@Override
	public void onAreaSizeChanged(Object observable, int width, int height) {

		
	}




	@Override
	public void onActionAccepted(int action, int state, Object param) {
		
	}




	@Override
	public void onLayoutCompleted(int flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getInitComplete() {
		 
		return false;
	}
	// ------------------------------------------------------------------------

	public void setUserbreak(boolean flag) {
		out.printErr(1, "A request for stopping all processes has been received, please wait...");
		userBreak = flag;
	}
	public boolean getUserbreak() {
		
		if ( somProcessControl.getInterruptRequest()>0){
			setUserbreak(true) ;
		}
		return userBreak;
	}

	public PrintLog getOut() {
		return out;
	}

 
 
	
	// ------------------------------------------------------------------------
	
	
	
}
