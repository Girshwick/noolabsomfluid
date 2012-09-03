package org.NooLab.somfluid;

import java.util.ArrayList;

import org.NooLab.utilities.callback.ProcessFeedBackContainerIntf;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.strings.StringsUtil;

import org.NooLab.somfluid.app.up.IniProperties;
import org.NooLab.somfluid.app.up.SomFluidStartup;
import org.NooLab.somfluid.clapp.SomAppClassifier;
import org.NooLab.somfluid.clapp.SomAppModelLoader;
import org.NooLab.somfluid.clapp.SomAppProperties;
import org.NooLab.somfluid.clapp.SomAppResultAnalyses;
import org.NooLab.somfluid.clapp.SomApplicationEventIntf;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.env.data.DataReceptor;
 


/**
 * 
 * is hosting references to all required SomApp objects
 * 
 * 
 * TODO: 
 *        - provide possibility for collecting feed back 
 * 
 *
 */
class SomApplication implements SomApplicationIntf{

	SomFluidFactory sfFactory ; 
	SomFluidTask sfTask;
	SomFluid somFluid;
	
	SomDataObject somData;
	
	SomAppProperties soappProperties ; 
	SomAppClassifier soappClassify;
	SomAppModelLoader soappLoader;
	
	SomAppResultAnalyses resultAnalyses;  
	
	/** a Guid that identifies the process as the master process for progress (and other) messages */
	String messageProcessGuid;
	transient SomApplicationEventIntf messagePort ;
	
	transient PrintLog out;
	transient DFutils fileutil = new DFutils();
	transient StringsUtil strgutil = new StringsUtil();
	private String lastStatusMessage="";
	
	// ========================================================================
	public SomApplication( 	SomFluid somfluid, SomFluidTask task, SomFluidFactory factory ){
		
		somFluid = somfluid;
		messagePort = somFluid.appInformer ;
		sfTask = task;
		
		messageProcessGuid = GUID.randomvalue() ;
		
		init( factory, factory.getSomAppProperties() );
	}

	public SomApplication( SomFluidFactory factory, SomAppProperties properties) {
		
		init( factory, properties );
		
	}
	
	private void init( SomFluidFactory factory, SomAppProperties properties){
		
		sfFactory = factory ;
		somFluid = sfFactory.somFluidModule ; 
		
		soappProperties = sfFactory.getSomAppProperties() ;
		out = sfFactory.getOut() ;
		
		soappLoader = new SomAppModelLoader(this, sfFactory );
	}
	// ========================================================================	


	public boolean loadModel() throws Exception{
		 
		boolean rB=false;
		out.print(2,"loading model...") ;
		 
		try{
			
			soappLoader.setActiveModel("");
			soappLoader.setActiveVersion("");
			
			
			// accessing the data source and extracting the column headers in the source
			// will be available as   soappLoader.expectedVarLabels
			soappLoader.retrieveExpectedVarLabels();
			
			// creates a catalog if it is not available, checks sub-directories,
			// and selects a model that fits the request options and the structural demands (feature vector)
			rB = soappLoader.selectFromAvailableModels();
			
			// as a result model and version are defined and can be loaded  
			
			if (rB){
				// loading the som model: 
				// if a version has been provided, it will be tried to choose that one,
				// else a particular one will be selected acc. to settings, and loaded
				rB = soappLoader.loadModel();
				
			}
			// in this way we access it 
			// SomAppClassifier  soc = soappLoader.getSoappClassifier() ;
			// SomAppTransformer sot = soappLoader.getSoappTransformer() ;
			
			// for maintaining meta-description
			// SomModelCatalog smc = soappLoader.getSoappModelCatalog() ; // :)
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		

		return rB;
	}
	 


	// called from SomAppModelLoader
	public boolean checkApplicability() {
		
		boolean rB=true;
		String filename,version ,_testversion, somxfile,transfxfile;
		
		//int selectionMode = soappProperties.getModelSelectionMode();
		
		filename = soappProperties.getBaseModelFolder();
		// String model   = soappProperties.getActiveModel() ;

		version = soappProperties.getPreferredModelVersion() ;
		
		
		if (version==null)version ="" ;
		version = version.trim();
		
		if ((version.length()>0 ) && (version.contentEquals("*")==false)){
			_testversion = fileutil.createpath(filename, version);
			
			if (fileutil.direxists(_testversion)){
				somxfile    = fileutil.createpath(_testversion, "som.xml");
				transfxfile = fileutil.createpath(_testversion, "transform.xml");
				if ((fileutil.fileexists(somxfile)) && (fileutil.fileexists(transfxfile))){
					
				}else{
					rB = false;
				}
				
			}else{
				if (fileutil.fileexists(_testversion+".zip")){
					
				}else{
					rB = false;
				}
			}
		}else{
			// get one according to criteria
			// 1. get all, inclusive properties
		}
		
		return rB;
	}
	
	
	
	
	public String perform() {
											out.print(2,"\ngoing to start performing classifications...") ;
											
		
		// reading the data source, into SomDataObject 
		// if not present, we do nothing (waiting just for the incoming data)
		
		// this reading we have to do tolerant against shifts of raster: possibly we have to rearrange it
		 
		soappClassify = soappLoader.getSoappClassifier() ;
		// soappClassify = null from there ??
		
		
		// this applies the loaded transformation model to the loaded data, creating the table of normalized data
		soappClassify.prepare();
		
		// create a guid-identifiable container object that is containing 
		//   guid, universal serial, data section, status, commands, results
		String guidStr = soappClassify.createDataTask();
		 
		soappClassify.start( SomAppClassifier._MODE_FILE ); // 1 = filemode, 3=service
		
		
		while (soappClassify.processIsRunning()==false){
			out.delay(0) ;
		}
		// waiting ?
		while (soappClassify.processIsRunning()){
			out.delay(0); // 0 = implies a wait by sleep(0,1)
		}
		
		resultAnalyses = soappClassify.getResultAnalyses() ;
		
		sfTask.isCompleted = true;
		
		somFluid.onTaskCompleted(sfTask);
		
		return guidStr;
	}


	public SomAppModelLoader getSomAppModelLoader(){
		
		return soappLoader;
	}
	
	public void close(){
		soappClassify.clear() ;
	}

	public SomDataObject loadSource( ) {// throws Exception{
		SomDataObject sdo;
		
		sdo = loadSource();
		
		return sdo;
	}

	public SomDataObject loadSource( String srcname ){// throws Exception{
		
		SomDataObject somDataObject;
		String srcName ="";
		// 

		srcName = srcname;
		if ((srcName.length()==0) || (DFutils.fileExists(srcName)==false)){
			srcName = soappProperties.getDataSrcFilename();
		}
		
		soappProperties.setDataSourceFile(srcName);
		
		somDataObject = createSomDataObject() ;
		
		DataReceptor dataReceptor = new DataReceptor( somDataObject );
		
		if (fileutil.fileexists(srcName)==false){
			if ((srcName.indexOf("/")<0) && (srcName.indexOf("\\")<0)){
				String path = fileutil.createpath(IniProperties.fluidSomProjectBasePath+"/"+SomFluidStartup.getLastProjectName() , "data") ;
				srcName = fileutil.createpath( path, srcName); 
			}
		}
		// establishes a "DataTable" from a physical source
		try {
		
			dataReceptor.loadFromFile(srcName);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (fileutil.fileexists(srcName)){
			soappProperties.setDataSourceFile(srcName);
		}
		
		
		// "importDataTable()" needs a SomTransformer as : SomTransformerIntf transformer; in SomData
		try {
		
			soappLoader.createSomTransformerInstance(somDataObject);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// imports the DataTable into the SomDataObject, and uses a SomTransformer instance 
		// in order to provide a basic numeric version of the data by calling SomTransformer.basicTransformToNumericalFormat()
		somDataObject.importDataTable( dataReceptor, 1 ); 
		
		// it is NOT normalized, just imported into table, list of variables and format detected
		/*
		 	not here in the classification task...
				somDataObject.acquireInitialVariableSelection();
				somDataObject.ensureTransformationsPersistence(0);
								
		somDataObject.save();
		*/
		somData = somDataObject ;
		return somDataObject;
	}

	
	public SomFluidTask getSomFluidTask() {
		return sfTask;
	}

	public SomDataObject getSomData() {
		return somData;
	}

	@Override
	public void setSomData(SomDataObject somdata) {
		somData = somdata ;
	}

	public SomDataObject createSomDataObject() {
		SomDataObject _somDataObject = null;
		
		
		
		try {
			
			_somDataObject = new SomDataObject( soappProperties, sfFactory.getSfProperties()) ;
			_somDataObject.setFactory(sfFactory);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			return null;
		}

		_somDataObject.setOut(out);
		
		_somDataObject.reestablishObjects();
		
		_somDataObject.prepare();
		
		return _somDataObject;
	}

	public String getLastStatusMessage() {
		return lastStatusMessage;
	}

	public SomFluid getSomFluid() {
		return somFluid;
	}

	public SomAppProperties getSoappProperties() {
		return soappProperties;
	}

	public SomAppClassifier getSoappClassify() {
		return soappClassify;
	}

	public SomAppModelLoader getSoappLoader() {
		return soappLoader;
	}

	public SomAppResultAnalyses getResultAnalyses() {
		return resultAnalyses;
	}

	@Override
	public SomApplicationEventIntf getMessagePort() {
		return messagePort;
	}

	 
 	@Override  // by ProcessFeedBackIntf
	public void update(ProcessFeedBackContainerIntf processFeedBackContainer) {
 		messagePort.onProgress( processFeedBackContainer );		
	}

 	/**
 	 * a Guid that identifies the process as the master process for progress (and other) messages
 	 */
	@Override  // by ProcessFeedBackIntf
	public String getMessageProcessGuid() {
		return messageProcessGuid;
	}

	@Override  // by ProcessFeedBackIntf
	public PrintLog setOut() {
		 return out;
	}

	
}
