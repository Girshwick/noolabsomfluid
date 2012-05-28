package org.NooLab.demoiinstances;

 

import processing.core.*;

import org.NooLab.somfluid.SomAppFactoryClientIntf;
import org.NooLab.somfluid.SomApplicationIntf;
import org.NooLab.somfluid.SomApplicationResults;
import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidClassTaskIntf;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidFactoryClassifierIntf;
import org.NooLab.somfluid.SomFluidMonoResultsIntf;
import org.NooLab.somfluid.SomFluidMonoTaskIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidStartup;
import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somfluid.SomProcessControlIntf;
import org.NooLab.somfluid.app.IniProperties;
import org.NooLab.somfluid.app.SomAppModelLoader;
import org.NooLab.somfluid.app.SomAppProperties;
import org.NooLab.somfluid.app.SomAppUsageIntf;
import org.NooLab.somfluid.app.SomApplicationEventIntf;
 

import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somtransform.algo.externals.AlgorithmPluginsLoader;
import org.NooLab.utilities.callback.ProcessFeedBackContainerIntf;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.files.PathFinder;
import org.NooLab.utilities.logging.LogControl;





/**
 * 
 * This applet demonstrates the application of a SomFluid model to new data;
 * 
 * here, the data source is just a file, of course, it could be a data base, 
 * a stream or whatsoever
 * 
 * the results are collected and indexed by a double key, a globally unique identifier
 * and a local long serial numerical id (whose start value can be set);
 * 
 *  indexing of results is on the level of individual records and 
 *  on the level of source accesses.
 *  
 * 
 */



public class A1_SomFluid_Application_Applet extends PApplet{

	 
	private static final long serialVersionUID = 8918471551051086099L;
	
	SomModuleInstanceA1 somInstance ;
	String sourceForProperties = "";
	 
	
	public void setup(){
		
		background( 208,188,188);
		draw();
		 
		// put this on top of any application: it cares for a well-defined base directory
		SomFluidStartup.setApplicationID("soapp");
		
		showKeyCommands();
		
		
		// start this in its own thread...
		
		// use LogControl to control output of log messages
		LogControl.globeScope = 2; // 1=local definitions  default = 2
		LogControl.Level = 2 ;     // the larger the more detailed logging... (could be really verbose!)
  
		
		// testPowerSet();
		
	}
	 
	public void draw(){
		background( 208,188,188);
	}


	public void keyPressed() {

		if (key=='c'){ // start a classification directly, a data set must be available
			// uses the last model, if available
			boolean rB=false;
			looping = false;
			this.noLoop();
			
			if (SomFluidStartup.getLastProjectName().length()==0){
				
				rB = openProject(  ) ;
				key=0;
			}else{
				rB=true;
			}
			if ((rB) && (SomFluidStartup.getLastDataSet().length()==0)){
				
				rB = SomFluidStartup.introduceDataSet().length() > 0;
			}
			IniProperties.saveIniProperties() ;
			
			looping = true;
			this.loop() ;
			
			if (rB){
				rB = SomFluidStartup.checkClassifierSetting();
			}
			if ((rB) && (SomFluidStartup.getLastProjectName().length()>0) && (SomFluidStartup.getLastDataSet().length()>0)){
				startEngines(); 
			}else{
				System.err.println("");
			}
			
		}
		
		if (key=='s'){ // start the service for streaming "wireless" classification
			System.err.println("Not yet implemented.");
		}
		
		if (key=='d'){
			looping = false;
			this.noLoop();
			
 
			
			SomFluidStartup.introduceDataSet();

			IniProperties.saveIniProperties() ;
			
			System.err.println("The following file has been selected as data source for classification : "+ SomFluidStartup.getLastDataSet() );
			System.err.println("The project path is currently set to : " + (IniProperties.fluidSomProjectBasePath+"/"+SomFluidStartup.getLastProjectName()).replace("//", "/"));
			
			looping = true;
			this.loop() ;
		}
		
		if (key=='o'){
			try {
				looping = false;
				this.noLoop();
				
				SomFluidStartup.selectActiveProject() ;
				
				looping = true;
				this.loop() ;
			} catch (Exception e) {
			}
		}
		 
		if (key=='p'){ //select  a different base folder for projects
			
			looping = false;
			this.noLoop();
			
			SomFluidStartup.selectProjectHome();
			
			looping = true;
			this.loop() ;
		}
		
		if (key=='z'){
			interrupSomFluidProcess();
			
		}

		
		if (key=='x'){
			// somInstance. ...
			System.exit(0);
		}
	}
	
	 
	private void showKeyCommands(){

		println();
		println("Welcome to FluidSom Demo !\n");
		println("the following key commands are available for minimalistic user-based control...");
		println();
		println("   c  ->  start classification of data, if necessary, file & folder will be offered for selection");
		println();
		println("   d  ->  declare data set (as file) ");
		println("   s  ->  start as service ");
		println();
		println("   o  ->  open project ");
		println("   p  ->  select project space (where all projects are located)");
		println();
		println("   z  ->  interrupt the current process, export current results and write persistent models ");
		
		println("   x  ->  exit");
		println();
		println("------------------------------------------------------------------");
		
	}
	
	private void startEngines(){
		println();
		println("starting...");
		
		println();
		System.out.println("data source   : " + SomFluidStartup.getLastDataSet() );
		System.out.println("project name  : " + SomFluidStartup.getLastProjectName() );
		System.out.println("project space : " + IniProperties.fluidSomProjectBasePath);
		println();
		
		somInstance = new SomModuleInstanceA1( SomFluidFactory._INSTANCE_TYPE_CLASSIFIER, 
			 									SomFluidFactory._GLUE_MODULE_ENV_NONE,
			 									sourceForProperties ) ;
		somInstance.startInstance() ;
	}
	

	private void interrupSomFluidProcess(){
		somInstance.issueUserbreak();
	}
	
	private boolean openProject(){
		boolean rB=false;
		
		looping = false;
		this.noLoop(); // that's mandatory, otherwise, the dialog won't be drawn
		
		try {
		
			SomFluidStartup.selectActiveProject();
			rB=true;
			
		} catch (Exception e) {
			rB=false;
		}
		 
		looping = true;
		this.loop() ;
		// http://code.google.com/p/processing/source/checkout
		
		return rB;
	}
	
	
}



// ============================================================================



/**
 * 
 * this object could be instantiated by the glue layer, if there is the correct information available
 * 
 * 
 * 
 * 
 */
class SomModuleInstanceA1 implements 	Runnable,
										// for messages from the engine to the outer application layers like this module
										SomApplicationEventIntf{
	


	SomFluidFactoryClassifierIntf sfcFactory;
	SomAppFactoryClientIntf _sfaFactory ;
	
	SomAppProperties soappProperties;
	SomApplicationIntf somApp ;
	
	SomFluidProperties sfProperties;
	
	
	SomProcessControlIntf somProcessControl ;

	
	String dataSource = "";
	
	int instanceType = SomFluidFactory._INSTANCE_TYPE_CLASSIFIER ;  
	int glueModuleMode = 0;
	String sourceForProperties = "";
	
	// initial number of nodes in the SOM lattice
	
	Thread smiThrd;

	
	// ------------------------------------------------------------------------
	public SomModuleInstanceA1( int instancetype, int glueMode, 
								String propertiesSource ){
		
		instanceType = instancetype;
		
		glueModuleMode =  glueMode ;
		
		sourceForProperties = propertiesSource;
		
		smiThrd = new Thread(this, "SomApp");
	 
	}
	// ------------------------------------------------------------------------

 
	
	public void startInstance(){
		smiThrd.start();
	}
	
	public void run() {
		
		
		
		classifyData(0); // via ini properties
		
  
		
	}
	
	public void issueUserbreak() {
		//  
		somProcessControl.interrupt(0);
	}

	 
 
	
	public String classifyData(int index){
		
		SomApplicationResults somResult;
		SomAppModelLoader somLoader;
		 
		String guid="";
		 
		if (instanceType == SomFluidFactory._INSTANCE_TYPE_CLASSIFIER){
			
			dataSource = SomFluidStartup.getLastDataSet();  // just the simple name, sth like "bank_C.txt"
			
			
			// 
			try {
				
				prepareSomFluidClassifier();
				

				somApp = sfcFactory.createSomApplication( soappProperties ) ;
				// last settings... will be started by TaskDispatcher in SomFluid
				 
				
				// sfTask contains a Guid  ??? 
				SomFluidClassTaskIntf sfTask = (SomFluidClassTaskIntf)sfcFactory.createTask( instanceType ); //  
				
				sfTask.setStartMode(1) ;  
				
				
				// producing the task = putting the task into the queue
				sfcFactory.produce( sfTask );
				
				guid = sfTask.getGuid();
			
				// safe this to the map index-guid
				
			} catch (Exception e) {
				 
				e.printStackTrace();
			}
			 
		} // instance = _INSTANCE_TYPE_CLASSIFIER ?
		 
		
		return guid;
	}

	
	private void explicitlySettingAppProperties(){
		
		AlgorithmPluginsLoader lap;
		
		// loading "builtinscatalog.xml" which is necessary for global indexing and activation of built-in algorithms
		soappProperties.setAlgorithmsConfigPath("D:/dev/java/somfluid/plugins/"); 

		// here we need an absolute path, the file is needed also for advanced auto assignments of built-in algorithms, 
		// even if there are no custom algorithms
		soappProperties.getPluginSettings().setBaseFilePath("D:/dev/java/somfluid/plugins/", "catalog.xml") ;
		
		
		soappProperties.setPluginsAllowed(true) ; 					// could be controlled form the outside in a dynamic manner

		try {
			if (soappProperties.isPluginsAllowed()){
				lap = new AlgorithmPluginsLoader(soappProperties, true);
				if (lap.isPluginsAvailable()) {
					lap.load();
				}
			}
			// feedback about how much plugins loaded ...
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		
		soappProperties.setInstanceType( instanceType ) ;			// "very" global: SomFluidFactory._INSTANCE_TYPE_CLASSIFIER = 7
		
		// in this folder there should be ZIPs containing model packages or sub-folders containing the model files
		
		soappProperties.setBaseModelFolder( SomFluidStartup.getProjectBasePath(), SomFluidStartup.getLastProjectName(), "models") ;
						// or, less elegant + flexible, by dedicated string:
						// soappProperties.setBaseModelFolder("D:/data/projects/_classifierTesting/bank2/models");
		
		// this refers to the name of the project as it is contained in the model file!!
		// on first loading, a catalog of available model will be created for faster access if it does not exists
		soappProperties.setActiveModel( SomFluidStartup.getLastProjectName() ); // sth like "bank2",  i.e. just the name, it must exist as a sub-directory
		
		soappProperties.setIndexVariables( new String[]{"Id","Mahnung_TV"});     
		                                                            // the user might be using a compound/structured key for identification, so we map such compound id
																	// if one of those does not exist, it will be disregarded silently
																    // !!! note that they are not used as index at all, the are just routed through
		// soappProperties.setIndexVariableColumnIndexes( new int[]{0});           
		                                                            // alternatively (& less controlled) we could provide the list of columns 
																	// containing such index information, these indices refer to the provided data !!!
		
		// if those usage parameters are not set, the values will be taken from the model 
		soappProperties.setRequiredConfidenceBySupport(8);
		soappProperties.setRiskLevelByECR(0.25) ;
				
		
		// if there are several model packages of the same name in the "BaseModelFolder", this
		// will control the mode of selection from those
		// soappProperties.setModelSelectionMode( SomAppProperties._MODELSELECT_VERSION,"0" ); 
		soappProperties.setModelSelectionMode( SomAppProperties._MODELSELECT_LATEST );
		soappProperties.setModelSelectionMode( SomAppProperties._MODELSELECT_BEST );
		// alternatively, we set the active model to blank here, and provide the package name ;
		// whenever the active model name is given (and existing) it will be preferred!
		//    clappProperties.setActiveModel("");
		//    clappProperties.setModelPackageName("1"); // this directory or zip-packages must exist and will be turned into 
		
		/* we also need a selection mode that allows to hold multiple models, according to criteria: 
		
		   all of them require to provide the max number of models to be loaded
		   
		   - set of variables sets,                   SomAppProperties._MODELSELECT_MULTI_ASSIGNATES
		     requires to provide those sets 
		     advantage: if a record contains missing values in some of the variables, 
		                we can try to select the matching model 
		   - distinctiveness of metric across models  SomAppProperties._MODELSELECT_MULTI_DISTINCTSTRUC
		     requires to provide distinctiveness criteria (number of mismatches of intensions of models)
		   - etc. 
		*/
		
		/*         
		 * basically we may provide 2 modes: explicit call = project mode, and waiting
		 * 
		 *  project : requires a source file by absolute path
		 *  
		 *  service : requires a service mode incl parameter: 
		 *                        file-based 
		 *                            - via supervised directory, a result file will be returned to that directory
		 *                        queue-based via listener to internal port based delivery (TCP),
		 *                            an external application may send / receive requests for 
		 *                               - confirmation of feature vector
		 *                               - data tables of any size according to that vector 
		 *                                  by containment of field labels, for each record a GUID will be returned  
		 *                               - get results upon request   
		 */
		
		
		soappProperties.setWorkingMode( SomAppProperties._WORKINGMODE_PROJECT ) ;
		
		if ( soappProperties.getWorkingMode() ==  SomAppProperties._WORKINGMODE_PROJECT){
								// could be any location, no preferred directory necessary, but it must exist, of course
			// soappProperties.setDataSourceFile("D:/data/projects/_classifierTesting/bank2/data/bank_C.txt");	
			soappProperties.setDataSourceFile( SomFluidStartup.getLastDataSet() );
		}
		//
		if ( soappProperties.getWorkingMode() ==  SomAppProperties._WORKINGMODE_SERVICEFILE ){
			
			// could be any directory, if it does not exists, it will be created
			soappProperties.setSupervisedDirectory( SomFluidStartup.getProjectBasePath(),SomFluidStartup.getLastProjectName(), "service");
			
			// clappProperties.addSupervisedFilename("<namefilter>");
		}
		
		// 
		soappProperties.setInstanceType( instanceType ) ;
		
	}
	 
	private void prepareSomFluidClassifier() throws Exception {
		 
		String prjName, path ;
		
		soappProperties = SomAppProperties.getInstance( sourceForProperties );
		soappProperties.connectGeneralProperties();
		
		
		explicitlySettingAppProperties();
		
		// does the source exist?
		// IniProperties. 
		prjName = SomFluidStartup.getLastProjectName() ;  // "bank2"
		path = IniProperties.fluidSomProjectBasePath;
		
		// TODO ... if not, exit from here via Exception
		
		
		sfcFactory = (SomFluidFactoryClassifierIntf) SomFluidFactory.get(soappProperties);
		sfcFactory.setMessagePort(this) ;
		
		somApp = sfcFactory.createSomApplication( soappProperties );   
		String msgProcessGuid = somApp.getMessageProcessGuid(); 
		// a Guid that identifies the process as the master process for progress (and other) messages
	}
	
	 
 
	
	@Override
	public void onProcessStarted( SomFluidTask sfTask, int applicationId, String pid) {
		
		if (applicationId == SomFluidFactory._INSTANCE_TYPE_CLASSIFIER ){
			System.err.println("A classification task has been started (id: " +pid+") for task Id <"+sfTask.getGuidID()+">.");
		}
		
		
	}
	
	// ------------------------------------------------------------------------
 
	// in a decoupled environment, these messages have to be passed through a message service,
	// then re-elicited by the receptor module
	 
	@Override
	public void onClassificationPerformed(Object resultObject) {
		
		System.out.println("client received event message <onClassificationPerformed()> ") ;
	}



	// call back event that provides access to the results of a modeling SOM
	@Override
	public void onResultsCalculated( SomFluidMonoResultsIntf results ) {
		// 
		System.out.println("client received event message <onResultsCalculated()> ") ;
	}

	 
	@Override
	public void onCalculation(double fractionPerformed) {
		// in case of large tasks
	}
 
	@Override
	public void onStatusMessage(SomFluidTask sfTask, int applicationId, int errcode, String msg) {
	 
		
	}
 
	@Override
	public void onProgress( ProcessFeedBackContainerIntf processFeedBackContainer ) {
		 
		// processFeedBackContainer contains a guid about the master process, information about the originating object, a base message, and the progress state)
		
		double progress = processFeedBackContainer.getCompletionProgress();
		String procId   = processFeedBackContainer.getMessageProcessID() ;
		String loopingObj = processFeedBackContainer.getHostingObjectName();
		
		if (progress>50)processFeedBackContainer.setDisplayMode( ProcessFeedBackContainerIntf._DISPLAY_SMOOTH );
		// we may delegate the task of displaying back to the progress display helper
		processFeedBackContainer.pushDisplay( loopingObj );
	}


 

	
}

