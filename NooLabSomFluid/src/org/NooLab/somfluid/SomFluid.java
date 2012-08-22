package org.NooLab.somfluid;

import java.util.ArrayList;

import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
 
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.objects.StringedObjects;

import org.NooLab.field.FieldIntf;
import org.NooLab.field.interfaces.FixedNodeFieldEventsIntf;
import org.NooLab.field.interfaces.PhysicalGridFieldIntf;
import org.NooLab.field.interfaces.RepulsionFieldEventsIntf;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldIntf;

import org.NooLab.somfluid.properties.* ;
import org.NooLab.somfluid.app.SomAppProperties;
import org.NooLab.somfluid.app.SomAppUsageIntf;
import org.NooLab.somfluid.app.SomAppValidationIntf;
import org.NooLab.somfluid.app.SomApplicationEventIntf;
import org.NooLab.somfluid.astor.SomAStorageQueryHandler;
import org.NooLab.somfluid.astor.SomAssociativeStorage;
import org.NooLab.somfluid.components.* ;
  
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.engines.det.results.ModelOptimizerDigester;
import org.NooLab.somfluid.core.engines.det.results.SimpleSingleModelDigester;
import org.NooLab.somfluid.core.engines.det.results.SomResultDigesterIntf;
import org.NooLab.somfluid.data.Variables;
  
import org.NooLab.somfluid.env.data.*;
import org.NooLab.somtransform.SomTransformer;



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
	SomProcessControlIntf somProcessControl ;
	
	ArrayList<SomDataObject> somDataObjects = new ArrayList<SomDataObject>();
	
	SomTasks somTasks;
	
	
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
	

	private void performAssociativeStorage(SomFluidTask sfTask) {
		 
		
		
		SomAssociativeStorage astor ;
		SomAStorageQueryHandler astorQueryHandler;
		
		
		sfTask.setDescription("Astor()") ;
		sfTask.setSomHost(null) ;
		
		astor = new SomAssociativeStorage( this, sfFactory, sfProperties ); 
		sfTask.setTaskType( SomFluidTask._TASK_SOMSTORAGEFIELD);
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
		
		if (sfTask.isCompleted){
			
			// not active yet... SomResultDigesterIntf resultHandler = sfTask.getSomResultHandler();
			// not active yet... resultHandler.handlingResults() ;
			
			if (SomFluidTask.taskIsModelOptimizer(sfTask)){ 

				SomFluidTask _task = sfFactory.somFluidModule.somTasks.getItemByGuid(sfTask.guidID);
				if (_task == null) {
					somTasks.add(sfTask);
				}

				// put the model to the list
				// provide an option ...
				sfFactory.getSomObjects().addSom(sfTask.somHost, sfTask.guidID);

				ModelOptimizer moz = (ModelOptimizer) sfTask.somHost;
				moz.saveResults();

				if (sfProperties.getOutputSettings().isExportApplicationModel()) {
					// call as task ?
					try {

						exportModel(sfTask);

					} catch (Exception e) {
						e.printStackTrace();
					}
					sfTask.setExported(true);
					
					out.printErr(2, "\nThe following task has been finished, exported and closed: " + sfTask.guidID + "\n");
				} else {
					out.printErr(2, "\nThe following task has been finished and closed: " + sfTask.guidID + "\n");
				}
			}
		} // completed "ModelOptimizer"
		
		if (SomFluidTask.taskIsSomApplication(sfTask)){ 
			
			out.printErr(2,"\nThe following task has been finished and closed: "+sfTask.guidID+"\n");
			
		}  // completed "SomApplication"
	}




	// ========================================================================
	public String addTask(SomFluidTask somFluidTask) {
		 
		somFluidTask.guidID = GUID.randomvalue() ;
		
		somTasks.setSfFactory(sfFactory) ;
		
		somTasks.add(somFluidTask);
		
		 
		out.setPrefix( "[SomFluid]") ; 
		out.print(2, "...now there are "+somTasks.size()+" tasks in the SomFluid-queue...") ; out.delay(100) ;
		
		isActivated = true;
		
		return somFluidTask.guidID  ;
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
						
						if ((sfTask!=null) && (sfTask.taskDispatched==0) && (sfTask.isCompleted==false) && (sfTask.isExported==false)){
							out.print(2,"\nworking on task, id = "+sfTask.guidID);
							int n = sfProperties.getModelingSettings().getOptimizerSettings().getMaxStepsAbsolute();
							out.print(2,"expected infimum number of explored variable combinations : " +n+"\n");
									 
							sfTask.taskDispatched=1;
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
					out.print(2,"task ("+somTasks.getItem(0).guidID+") has been completed.\n"); // yet, the completion flag is set by the process itself !
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
					
					if (sfTask.somType == SomFluidProperties._SOMTYPE_MONO){
						
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


	
	public SomDataObject createSomDataObject() {
		SomDataObject _somDataObject;
		
		
		_somDataObject = new SomDataObject(sfProperties) ;
		
		_somDataObject.setFactory(sfFactory);
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
	
	public SomDataObject loadSource( String srcname ) throws Exception{
		
		SomDataObject somDataObject;
		int result=-1;
		String srcName ="";
		String loadedsrc ;
		
		
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

	public PrintLog getOut() {
		return out;
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

	@Override
	public void onLayoutCompleted(int flag) {
		// TODO Auto-generated method stub
		
	}
 
	
	// ------------------------------------------------------------------------
	
	
	
}
