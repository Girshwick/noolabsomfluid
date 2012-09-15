package org.NooLab.demoiinstances;



import java.util.ArrayList;

import processing.core.*;

import org.NooLab.field.FieldIntf;
import org.NooLab.itexx.storage.DataStreamProviderIntf;
import org.NooLab.itexx.storage.TexxDataBaseSettingsIntf;
import org.NooLab.itexx.storage.somfluid.db.DataBaseAccessDefinition;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidMonoResultsIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidPropertiesHandler;
import org.NooLab.somfluid.SomFluidPropertiesHandlerIntf;
 
import org.NooLab.somfluid.app.up.IniProperties;
import org.NooLab.somfluid.app.up.SomFluidStartup;
import org.NooLab.somfluid.clapp.SomApplicationEventIntf;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.storage.ContainerStorageDevice;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somfluid.structures.VariableSettingsHandlerIntf;
import org.NooLab.somfluid.tasks.SomFluidMonoTaskIntf;
import org.NooLab.somfluid.tasks.SomFluidProbTaskIntf;
import org.NooLab.somfluid.tasks.SomFluidTask;
import org.NooLab.structures.InstanceProcessControlIntf;

import org.NooLab.utilities.callback.ProcessFeedBackContainerIntf;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.LogControl;

   
/*
 * 
 * TODO :   reading the database should NOT insert a further column left-most to the table
 *          if the description file (or a parameter) forbids it: -> new field in VariableSettings
 *           [structure]
 *           insertid=0
 */


/**
 * 
 * this applet organizes the test of associative storage
 * particularly the settings and the data flow 
 * 
 * 
 * 
 * 
 * 
 * Technical remark:
 * it is very important to start the JVM with the following parameters !!
 * 
 *
   -Xmx1140m
   -XX:+ExplicitGCInvokesConcurrent
   -XX:+UseConcMarkSweepGC

	
 *    				
 */

public class S1_explicitsettings_SomFluidModuleApplet extends PApplet{

	 
	private static final long serialVersionUID = 8918471551051086099L;
	
	SomModuleInstanceS1 somInstance ;
	String sourceForProperties = "" ;
	PApplet applet;
	
	// note that this refers only to any single loop of exploration-by-selection, it is an infimum! 
	// the actual number is approx+  5 * L2-loopcount (max 5) + (size of selection), 
	// for serious modeling, an appropriate value here is always >20!   
	int _numberOfExploredCombinations  = 31;
	
	
	public void setup(){
		 
		applet = this;
		background( 208,188,188);
		 
		try {
			SomFluidStartup.setApplicationID( "astor", this.getClass() );
		
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return;
		}
		  
		showKeyCommands();
		
		
		draw();
		
		// start this in its own thread...
		
		// use LogControl to control output of log messages
		LogControl.globeScope = 2; // 1=local definitions  default = 2
		LogControl.Level = 2 ;     // the larger the more detailed logging... (could be really verbose!)
  
		
	}
	
	
	
	public void draw(){
		background( 208,188,188);
	}


	public void keyPressed() {

		
		
		if (key=='m'){
			
			boolean rB=false ; 
			looping = false;
			this.noLoop();
			
			if (IniProperties.lastProjectName.length()==0){
				try {
					SomFluidStartup.selectActiveProject();
					rB=true;
				} catch (Exception e) {
					rB=false;
				}
			}else{
				rB=true;
			}
			
			
			if ((rB) && ( IniProperties.dataSource.length()==0)){
				
				key=0;
				rB = SomFluidStartup.introduceDataSet().length() > 0 ;
			}
			looping = true;
			this.loop() ;
			
			if (rB){
				startEngines(1) ; // 1= optimizer, 0=simple som (not recommended for most tasks)
			}
		}
		
		if (key=='r'){
			resume(1) ;
		}
		
		if (key=='q'){
			runQueries();
			System.err.println("not implemented yet.") ;
		}
		
		if (key=='z'){
			interrupSomFluidProcess();
		}

		
		if (key=='x'){
			// somInstance. ...
			IniProperties.saveIniProperties();
			System.exit(0);
		}
		
		if (key=='c'){ // duplicate project, just strip results and exports
			SomFluidFactory.duplicateProject(0);
			System.err.println("not implemented yet.") ;
		}

		if (key=='C'){ // duplicate project, to bare bones
			System.err.println("not implemented yet.") ;
			SomFluidFactory.duplicateProject(1);
		}
		
		if (key=='d'){
			// open data source
			key=0;
			 
			looping = false;

			String datasrc = SomFluidStartup.introduceDataSet() ;
			System.err.println("File selected as source : "+datasrc);
			
			looping = true;
			this.loop() ;
		}
		
		if (key=='o'){
			// open project
			openProject();
		}
		
		if (key=='p'){ //select a different base folder for projects
			 
			looping = false;
			key=0;
			String selectedFolder;
			try {
				selectedFolder = SomFluidStartup.selectProjectHome();

			} catch (Exception e) {
				System.err.println(e.getMessage());
				return;
			}
			System.err.println("Folder selected as base folder for projects : "+selectedFolder);
			
			looping = true;
			this.loop() ;
		}
		
		if (key=='P'){ // capital "P": // create project space...
			
			looping = false;
			this.noLoop();
			key=0;
			try {
			
				String psLabel = SomFluidStartup.getNewProjectSpaceLabel();
				
				if (SomFluidFactory.projectSpaceExists(psLabel)){
					return;
				}

				IniProperties.lastProjectName = psLabel ;
				SomFluidFactory.completeProjectSpace();
			 
				System.err.println("Folder selected as new project space : "+SomFluidStartup.getProjectBasePath() + psLabel);
				
				// now we offer a file selection dialog, which will copy 
				SomFluidFactory.organizeRawProjectData();
					
				
				
				
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			
			looping = true;
			this.loop() ;
			
		}

	}
	
	 

	private void showKeyCommands(){

		println();
		println("Welcome to Astor, the Associative Storage based on FluidSom!\n");
		println("the following key commands are available for minimalistic user-based control...");
		println();
		println("   m  ->  start modeling, start from scratch by importing the data file ");
		println("   M  ->  start modeling, reload previously prepared data ");
		println("   r  ->  resume modeling, load persistent models, and continue modeling ");
		
		println("   q  ->  run some example queries towards the som ");
		println();
		println("   i  ->  activate the file listener, which digests new data from a directory ");
		println();
		println("   c  ->  copy project except exports and results ");
		println("   C  ->  copy project, but remove everything except the data file and the transformation rules ");
		println();
		println("   d  ->  open another data set ");
		println("   o  ->  open another project ");
		println("   p  ->  select a different base folder (=project space) for all projects ");
		println("   P  ->  create a new project in the project space, the data file must exist somewhere to copy from! ");
		println();
		println("   z  ->  interrupt the current process, export current results and write persistent models ");
		
		println("   x  ->  exit");
		println();
		println("------------------------------------------------------------------");
		println();

		_showCurrentInputSettings();
	}
	
	private void _showCurrentInputSettings(){
		String qs="";
		if (SomFluidStartup.isDataSetAvailable()==false){
			qs = "not " ;
		}
		println("current project : "+ SomFluidStartup.getLastProjectName()+ ",  data are "+qs+"available.");
		if (qs.length()==0){
		println("data source     : "+ SomFluidStartup.getLastDataSet() ) ;
		}

	}

	
	/**
	 * 
	 * @param somtype 0=default: simple, single run; 1=som optimizer
	 * @param lastProjectName2 
	 */
	private void startEngines( int somtype ){
		println();
		println("starting project <"+ IniProperties.lastProjectName+"> ...");
		
		if (somtype>=1){ 
			somtype = FieldIntf._INSTANCE_TYPE_OPTIMIZER ;
		}
		if (somtype<=0){ 
			somtype = FieldIntf._INSTANCE_TYPE_ASTOR ;
		}
		
		somInstance = new SomModuleInstanceS1(  
			 									SomFluidFactory._GLUE_MODULE_ENV_NONE,
			 									sourceForProperties ) ;
		somInstance.setAbsoluteNumberOfExploredCombinations( _numberOfExploredCombinations );
		somInstance.startInstance() ;
	}
	
	private void openProject(){
		
		looping = false;
		this.noLoop(); // that's mandatory, otherwise, the dialog won't be drawn
		
		try {
		
			SomFluidStartup.selectActiveProject();
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		looping = true;
		this.loop() ;
		// http://code.google.com/p/processing/source/checkout
	}

	private void runQueries() {
		 if (somInstance==null){
			 return;
		 }
		
		 somInstance.runDemoQueries();
		 
	}

	private void resume(int somtype){
		
		if (somtype>=1){ 
			somtype = FieldIntf._INSTANCE_TYPE_OPTIMIZER ;
		}
		if (somtype>=1){ 
			somtype = FieldIntf._INSTANCE_TYPE_ASTOR ;
		}
		
		somInstance = new SomModuleInstanceS1( 	 
												SomFluidFactory._GLUE_MODULE_ENV_NONE,
												sourceForProperties ) ;
		somInstance.setResumeMode(true);
		somInstance.resume();
	}
	
	private void interrupSomFluidProcess(){
		somInstance.issueUserbreak();
	}
}



// ============================================================================



/**
 * 
 * this object could be instantiated by the glue layer, if there is the correct information available
 * 
 */
class SomModuleInstanceS1 implements 	Runnable,

										// for messages from the engine to the outer application layers like this module
										SomApplicationEventIntf{
	
	
	SomFluidFactory sfFactory;
	SomFluidProperties sfProperties;
	SomFluidPropertiesHandlerIntf sfPropertiesDefaults;
	
	SomFluidProbTaskIntf sfTask;
	
	InstanceProcessControlIntf somProcessControl ;
	
	int instanceType = FieldIntf._INSTANCE_TYPE_OPTIMIZER;
	
	int absoluteNumberOfExploredCombinations = 10;
	
	String sourceForProperties = "";
	
	String lastProjectName="" ;
	
	// initial number of nodes in the SOM lattice
	int nodeCount = 900; // 110000 are possible for 1.2 gigabytes in VM
	// we need a version with smaller (=minimized) structure for associative storage...
	// indices about stored / referenced objects could be backed to a database, such the
	// SOM could host an "infinite" number of documents (...the same database where randomwords is contained)
	// any de-referencing would result in a join within the same database
	
	Thread smiThrd;
	
	private boolean resumeMode;
	
	// ------------------------------------------------------------------------
	public SomModuleInstanceS1( int glueMode, String propertiesSource ){
		
		
		sourceForProperties = propertiesSource;
		
		smiThrd = new Thread(this, "SomModuleInstance");
	}
	public void setResumeMode(boolean flag) {
		//
		resumeMode = flag;
	}


	public void resume() {
		// 
		
		resumeMode=true;
		smiThrd.start();
	}

	public void runDemoQueries() {
		// TODO Auto-generated method stub
		
	}
	public void startInstance( ){
		lastProjectName = IniProperties.lastProjectName ;
		smiThrd.start();
	}
	
	public void run() {
		
		 
		if (instanceType == FieldIntf._INSTANCE_TYPE_OPTIMIZER){
			prepareSomOptimizer();
		}
		 
		
	}
	
	public void issueUserbreak() {
		//  
		if (somProcessControl!=null){
			somProcessControl.interrupt(0);
		}else{
			System.err.println("Nothing to stop, <somProcessControl> was not up and running...");
		}
	}
 

	private void prepareSomOptimizer(){
	
		// loads an existing targeted SOM and starts/continues/repeats the optimizing according to the settings 
		// results are saved or provided to the Glue, then the Som-layer stops / application exits
	
		
		// this might be called with some URL or xml-string that represents the source containing the settings
		// if this exists, it will be loaded
		sourceForProperties = "" ;
		sfProperties = SomFluidProperties.getInstance( sourceForProperties ); // not available
		
		defineDataBaseAccesData();
		
		// alternatively... load from /resources in jar ...
		 
		
		if ((sourceForProperties.length()==0) || ( sfProperties.initializationOK()==false )){
		
			sfPropertiesDefaults = new SomFluidPropertiesHandler(sfProperties);
		
			explicitlySettingProperties();
			 
		}
	
		
		sfFactory = SomFluidFactory.get(sfProperties);					   // creating the factory; calling without "factorymode" parameter, optimizer will be assumed	
		 
		 
		sfFactory.saveStartupTrace( FieldIntf._INSTANCE_TYPE_SOM, 
									sfPropertiesDefaults.getStartupTraceInfo() );
		sfProperties.save();
		sfProperties.exportXml();  					// String xstr = sfProperties.getExportedXml();
		
		
		/*
		 * everything is started by "SomFluid" through a queued task mechanism;
		 * 
		 * different application types use different perspectives onto the task
		 * 
		 */
		  
		sfTask = (SomFluidProbTaskIntf)sfFactory.createTask( FieldIntf._INSTANCE_TYPE_ASTOR ); //
		
		sfTask.setStartMode(1) ;             		// default=1 == starting after producing the FluidSom
										 			//        <1 == only preparing, incl. 1 elementary run to load the data, 
		                                 			//              but not starting the modeling process (v switches in the middle between 3..100)
										 			//        >100  minimum delay in millis
		
		
		sfTask = (SomFluidProbTaskIntf)sfFactory.createTask( ); 
		sfTask.setStartMode(1) ;  
		 
		 
		sfTask.activateDataStreamReceptor(true) ;   // after learning the initial bunch of data
													// Astor Som switches to "online learning"
		
		try {
			
			sfFactory.produce( sfTask );
							// this produces the SomFluid and the requested som-type according to
							// SomFluidProperties._SOMTYPE_MONO, referring implicitly to sfTask; 
							
		} catch (Exception e) {
			e.printStackTrace();
		}          		
		
	 	// if we like to have graphical output, then start the applet for displaying it and 
		// define shake hands by means of GlueClients...
		

	}


	
	
	private void defineDataBaseAccesData() {

		DataBaseAccessDefinition dbAccess ;
		TexxDataBaseSettingsIntf dbSettings ;
		ArrayList<String> fieldlist ;
		
		try {
			
			// reading from  dbDefinitionResource ="texx-db-definition-xml" ;
			sfProperties.getDatabaseDefinitionInfo("randomwords",1);
			
			dbAccess = sfProperties.getDbAccessDefinition() ;
			 
			dbSettings = sfProperties.getDatabaseSettings() ;
			
			fieldlist = dbSettings.getTableFields() ;
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}
	
	private void explicitlySettingProperties(){
		
		 
		PersistenceSettings ps;
		 

		String dbDefinitionResource="" ;
		
		try {
			
			String instance = "astor";
			
			if (instance.contentEquals("astor")){

				sfPropertiesDefaults.setInstance( "astor" ,nodeCount );
				//check this !!
				instanceType = sfProperties.getSomInstanceType() ;
									
				sfProperties.setSomType( FieldIntf._SOMTYPE_PROB ) ; // == associative storage = Astor
				
				// choose a fixed or a fluid grid
				sfPropertiesDefaults.setGridType( FieldIntf._SOM_GRIDTYPE_FIXED ) ;
			    //sfPropertiesDefaults.setGridType( FieldIntf._SOM_GRIDTYPE_FLUID) ;
				
				
				// choose a guessing instance: low-resolution grid, speeding the 
				// node selection in large grids
				
				
				
				// this applies only if the instance is of type "som", which includes optimizing of the model
				// the absolute number of different models that will be checked
				sfPropertiesDefaults.setOptimizerStoppingCriteria( absoluteNumberOfExploredCombinations ) ;					
									//	optionally, any of the following parameters ( [n], 21, 100, 0.8)


				/* we may create a complete som for classification from externally defined "top-down" profiles
				   that's very important in design approaches
				   note that the data file will be interpreted as a collection of prototypes;
				   yet, if the data contain more records than it has been defined as the number of prototypes,
				   it will be regarded as empirical data  	
				   by default = Off */
				sfPropertiesDefaults.setDataSimulationByPrototypes( 0 );				// or any number >100 for switching on
				sfPropertiesDefaults.setMaxNumberOfPrototypes(50) ;
				
				/*
				 * the "outcome" or target can be mapped onto a binary variable
				 * level of definition parameters may be "raw" or "relative" (=normalized, hence expressing percentage quantiles)
				 * 
				 * will be overridden by settings from the optional description file about variable settings
				 */
				// sfPropertiesDefaults.setSingleTargetDefinition( "raw", 0.1, 0.41, "intermediate" ) ;
									// the Single-Target-Mode can be defined with several non-contiguous intervals within [0..1] 
				
									// we have to check rather soon whether such values occur at all 
									// in the TV column of the table: on data import, or reloading of the SomData 
				
				sfPropertiesDefaults.activateGrowingOfSom( false, 300, 15.1) ; // n = max node size, averaged across the top 15% of nodes acc. to size

			}

			// variables...
			defineInitialVariableSettings(); 
									// alternatively, defineInitialVariableSettings([filename]);
									// (re-)creating the file
									// sfPropertiesDefaults.exportVariableSettings(null, ""); // a dedicated path could be applied
			 
			
			// -------------------------------------------------------------------------------
			
			// "file", "db", "tcp"
			sfPropertiesDefaults.setDataSourcing( DataStreamProviderIntf._DSP_SOURCED_FILE ,0) ; 						
			sfPropertiesDefaults.setDataSourceName( IniProperties.dataSource );
			
			
			// ... overruling by setting a database as source 
			sfPropertiesDefaults.setDataSourcing( DataStreamProviderIntf._DSP_SOURCED_DB ,0) ;
			// dbDefinitionResource = "texx-db-definition-xml" ; // default
			sfPropertiesDefaults.setDatabaseDefinitionResource( dbDefinitionResource,"randomwords",1 );
			 
			
			// -------------------------------------------------------------------------------
			// optional, if there are indeed plugin files ...
			sfPropertiesDefaults.setAlgorithmsConfigPath( "D:/dev/java/somfluid/plugins/" );
			

			 
			
			sfProperties.addFilter( "var",0.3,"<",1,1,true);       
									// filter that act on the values of observations
			   						// can be defined only with an existing factory since we need access to the data
			   						// !! not yet functional

			
			sfPropertiesDefaults.initializeDefaults();
			
			
			sfProperties.setExtendingDataSourceEnabled(true); 
			
			
			// controlling the granularity/verbosity of console messages 
			if (LogControl.Level>2){
				sfProperties.setShowSomProgress( SomFluidProperties._SOMDISPLAY_PROGRESS_STEPS ) ; // default: _SOMDISPLAY_PROGRESS_BASIC
			}
			sfProperties.setShowSomProgress( SomFluidProperties._SOMDISPLAY_PROGRESS_STEPS );
			
			sfProperties.setMultiProcessingLevel(0); 
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("The system will exit.");
			System.exit(-7) ;
		}
		
		// --- --- ---

		/*
		 * in this folder there will be enumerated sub-folders containing the results
		 */
		String resultFolder = sfPropertiesDefaults.setResultsPersistence(1,"");	// optionally: a dedicated path
		       resultFolder = sfPropertiesDefaults.getResultBaseFolder();

		// set ready flag
		sfProperties.setInitializationOK(true) ;						
	}
	
	  
	 
	 
	/**
	 * a small compartment, which optionally also may read the initial variable settings from a file
	 * 
	 * @param filename
	 * @throws Exception 
	 */
	private void defineInitialVariableSettings( String ...filenames) throws Exception {

		boolean configByFileExcluded=false;
		String[] filnames = new String[0];
		
		

		if ((filenames!=null) && (filenames.length>0)){
			filnames = new String[ filenames.length];
			System.arraycopy(filenames, 0, filnames, 0, filnames.length) ;
		}
		
		if ((configByFileExcluded==false) && ((filenames==null) || (filenames.length==0) || (DFutils.fileExists(filenames[0])==false))){
			// String str  =IniProperties.lastProjectName; // = Astor
			
			String filepath = DFutils.createPath( IniProperties.fluidSomProjectBasePath, IniProperties.lastProjectName);
			
			filepath = DFutils.createPath( filepath , "data/description/");
			filepath = DFutils.createPath( filepath , "astor-variables.txt");
			
			String filename = sfPropertiesDefaults.checkForVariableDescriptionFile(0, filepath) ; 
			// 0 = typology file, 1 = textual description (background information)
			
			if (DFutils.fileExists(filename)){
				filnames = new String[]{filename};
			}
		}
		
		if ((filnames!=null) && (filnames.length>0) && (DFutils.fileExists(filnames[0]))){
			 
			if (sfPropertiesDefaults.loadVariableSettingsFromFile( filnames[0]) ){
				// recognizes format by itself, if successful, we may return
				return;
			}
		}
		
		// .................. do the following only if there was not a definition file
		
		
		// note that the data are not loaded at that point,
		// such it is only a request for blacklisting variables
		// if any those variables do not exist, no error message will appear by default
		
		// we provide a small interface for dealing with initial variable settings all at once
		VariableSettingsHandlerIntf variableSettings = sfPropertiesDefaults.getVariableSettingsHandler();

		variableSettings.setTargetVariables(""); 	
		variableSettings.setTvGroupLabels("") ;

		sfProperties.addFieldExclusionByIndex(15);
		sfProperties.setFieldExclusionByIndexFrom(15);
		sfProperties.setFieldExclusionByIndexUntil(15);
		sfProperties.setFieldExclusionByIndexFrom("wordlabel", 1); 
		sfProperties.setFieldExclusionByIndexUntil("wordlabel"); 
		sfProperties.setFieldExclusionByBetween(2,15);

	}

	
	
	private void resumeSOM() throws Exception{
		
		String startupTraceInfo="", lastproject="", systemroot="", propertiesFileName, dir;
		String[] infoStrings, infopart;
		FileOrganizer fileorg ;
		
		try {
											System.out.println("resuming...");
			instanceType = FieldIntf._INSTANCE_TYPE_SOM;
			
			startupTraceInfo = SomFluidFactory.loadStartupTrace( instanceType ) ;
		
			infoStrings = startupTraceInfo.split("\n");
			
			for (int i=0;i<infoStrings.length;i++){
				infopart = infoStrings[i].split("::");
				if (infopart[0].contains("cfgroot")) {
					systemroot = infopart[1];
				}
				if (infopart[0].contains("project")) {
					lastproject = infopart[1];
				}
			}
			
			if ((systemroot.length()==0) || (lastproject.length()==0) || (DFutils.fileExists(systemroot )==false)){
				throw(new Exception("Cannot resume, since boot/trace file was not found or it did not contain the relevant information.\n"+
									"You must start normally and finish the project before you can resume it.\n")) ;
			}
			
			// load properties, still only as a bootstrap...
			sfProperties = SomFluidProperties.getInstance( sourceForProperties );
			sfProperties.setInstanceType( instanceType ) ;
			 
			fileorg = sfProperties.getFileOrganizer() ;
			
			sfProperties.setSystemRootDir(systemroot) ;
			sfProperties.getPersistenceSettings().setPathToSomFluidSystemRootDir(systemroot) ;
			sfProperties.getPersistenceSettings().setProjectName(lastproject) ;
			
			fileorg.setPropertiesBase(sfProperties);
			
			dir = fileorg.getObjectStoreDir("");
			propertiesFileName = DFutils.createPath( dir, SomFluidProperties._STORAGE_OBJ ) ;
			
			// now loading the desired properties into a new object;
			ContainerStorageDevice storageDevice ;
			storageDevice = new ContainerStorageDevice();
			
			sfProperties = (SomFluidProperties)storageDevice.loadStoredObject(propertiesFileName) ;
			
			sfProperties.setFileOrganizer(fileorg) ;
			
			sfFactory = SomFluidFactory.get(sfProperties);	
			somProcessControl = sfFactory.getSomProcessControl() ; // sending messages into the SomProcess, like interrupt, injecting new best metric, changing properties and threshold on the fly ...
											sfFactory.getOut().print(2, "loading description of previous task ...");
			// loading the task and starting it (file = SomFluidTask.trace)
			Object taskObj = sfFactory.loadTaskTrace(lastproject);
			
			if (taskObj==null){
				
				SomFluidMonoTaskIntf sfTask = (SomFluidMonoTaskIntf)sfFactory.createTask( ); 
				sfTask.setContinuity( 1,1) ;                 
				sfTask.setStartMode(1) ;
	
				sfTask = (SomFluidMonoTaskIntf)sfFactory.createTask( ); 
				sfTask.setStartMode(1) ;  
				sfTask.setContinuity(2,0,200);
				
				sfFactory.produce( sfTask );
			}else{
				if (taskObj instanceof SomFluidMonoTaskIntf) {
					SomFluidMonoTaskIntf sfTask = (SomFluidMonoTaskIntf)taskObj ;
					sfTask.setResumeMode(1);
											sfFactory.getOut().print(2, "putting previous task to the queue...");
					sfFactory.produce( sfTask );
				}
				if (taskObj instanceof SomFluidProbTaskIntf) {
					SomFluidProbTaskIntf sfTask = (SomFluidProbTaskIntf)taskObj ;
					sfTask.setResumeMode(1);
					sfFactory.produce( sfTask );
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void importNewProperties( String xmlPropertiesSource){
		
		
	}
	
	// ------------------------------------------------------------------------
 
	 
	public int getAbsoluteNumberOfExploredCombinations() {
		return absoluteNumberOfExploredCombinations;
	}


	public void setAbsoluteNumberOfExploredCombinations(int value) {
		absoluteNumberOfExploredCombinations = value;
	}


	// call back event that provides access to the results of a modeling SOM
	@Override
	public void onResultsCalculated( SomFluidMonoResultsIntf results ) {
		// 
		System.out.println("client received event message <onResultsCalculated()> ") ;
	}

	 
	@Override
	public void onClassificationPerformed(Object resultObject) {
		
		System.out.println("client received event message <onClassificationPerformed()> ") ;
	}

	
	// call back events for providing information about the state of modeling

	@Override
	public void onCalculation(double fractionPerformed) {
		// in case of large tasks
	}

	@Override
	public void onProcessStarted(SomFluidTask sfTask, int applicationId, String pid) {
	 
		
	}

	@Override
	public void onStatusMessage(SomFluidTask sfTask, int applicationId, int errcode, String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProgress(ProcessFeedBackContainerIntf processFeedBackContainer) {
		// TODO Auto-generated method stub
		
	}

	 
	

	
}

