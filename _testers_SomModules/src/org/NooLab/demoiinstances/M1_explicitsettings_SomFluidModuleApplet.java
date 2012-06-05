package org.NooLab.demoiinstances;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import processing.core.*;

import org.NooLab.somfluid.SomAppPublishing;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidIntf;
import org.NooLab.somfluid.SomFluidMonoResultsIntf;
import org.NooLab.somfluid.SomFluidMonoTaskIntf;
import org.NooLab.somfluid.SomFluidProbTaskIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidPropertiesHandler;
import org.NooLab.somfluid.SomFluidPropertiesHandlerIntf;
import org.NooLab.somfluid.SomFluidStartup;
import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somfluid.SomProcessControlIntf;
import org.NooLab.somfluid.app.IniProperties;
import org.NooLab.somfluid.app.SomApplicationEventIntf;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.data.VariableSettingsHandlerIntf;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.storage.ContainerStorageDevice;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somfluid.util.PowerSetSpringSource;
import org.NooLab.utilities.callback.ProcessFeedBackContainerIntf;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.LogControl;

   
/**
 * 
 * TODO Roc-AuC is too optimistic (not cut at sensitivity=100)  
 *      TV columns need complete scan for values ...
 *  
 * 
 * Later: this applet should start the SomFluid as an external application!!
 * 	      Any communication should be performed through Glue, or "wirelessly" through TCP
 * 
 * 
 * the boot process uses a static object "SomFluidStartup", 
 * which in any must be configured at first, before any other activities can happen
 * 
 * 
 * 
 * note that the SomFluid instance always contains the full spectrum of tools, yet,
 * it behaves as such or such (Som, Sprite, Optimizer, transformer), according to the request.
 * 
 * 
 * nice examples here:  http://technojeeves.com/
 * 
 * 
 * 
 * TODO: missing value portion in extensions update ... 
 *       put used expressions into results
 *		       
 *    				
 */

public class M1_explicitsettings_SomFluidModuleApplet extends PApplet{

	 
	private static final long serialVersionUID = 8918471551051086099L;
	
	SomModuleInstanceM1 somInstance ;
	String sourceForProperties = "" ;
	PApplet applet;
	
	// note that this refers only to any single loop of exploration-by-selection, it is an infimum! 
	// the actual number is approx+  5 * L2-loopcount (max 5) + (size of selection), 
	// for serious modeling, an appropriate value here is always >20!   
	int _numberOfExploredCombinations  = 25;
	
	// further work....
	// best model when using ALL data(limited exploration without cross-validation stability)
	// noising data
	// results: lift factor at TP-0 TP-ECR (actual cost), TP-ECR (effective @ sensitivity constraint)
	// showing final selection, showing PCA selection, switching on PCA for start
	// making things parallel: prob. best as per record if there are many
	// splitting variable selection task
	// growth
	// enabling volatile sampling in evo-devo : biased removal, plain removal, 
	// simulating data from profiles
	// cluster based 0-models

	
	public void setup(){
		 
		applet = this;
		background( 208,188,188);
		 
		try {
			SomFluidStartup.setApplicationID("");
		
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
  
		// testPowerSet();
		
	}
	
	@SuppressWarnings("unused")
	private void testPowerSet(){
	
		PowerSetSpringSource powerset;
		String[] itemsarr = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R"}; 
		ArrayList<String> items = new ArrayList<String>(Arrays.asList(itemsarr)); 
		// Id, Name, KundenNr, SalesOrg, Region, Land, intern_Rating, Bonitaet, Bisher, Kunde_seit, Stammkapital, Gründungsdatum, Anzahl_Mitarbeiter, Rechtsform, Branchenscore, Mahnung_TV]
		// this array has to have the same dimension and the same order as the string array continaing 
		// the items, if we want to set them all at once; yet, there is also the possibility to use
		// powerset.setSelectionProbability("A", 0.12)
		
		double[] selectionProbs = new double[]{0.28,1.0,0.98,0.699,1.0,0.6,0.68,0.78,0.54,0.64,0.67,0.89,0.81,0.71,0.74,0.75,0.93,0.77}; 
		
		powerset = new PowerSetSpringSource(items);
		
		powerset.setPreferredSizeLimit(6) ;
		
		
		powerset.setSelectionProbabilities( 0.52 ); // for all the same
		powerset.setSelectionProbability( "A",0.62 ); // for one individually
		powerset.setSelectionProbabilities(selectionProbs ); // for all individually at once
		 
		powerset.setMaxSelectionCounts(3) ;
		
		// this have to be changed to index positions
		powerset.getConstraints().addExcludingItems(new String[]{"C","G","J"});
		try {
			powerset.getConstraints().addMandatoryItems(new String[]{"B","E"});
		} catch (Exception e) {
			e.printStackTrace();
		}

		powerset.getConstraints().setMaximumLength(14);
		powerset.getConstraints().setMinimumLength(3) ;
		
		powerset.activateConstraints(1);
		
		// powerset.printAll() ;
		
		ArrayList<String> setItems = null;
		int i=1;
		boolean selectSimilar=false;
		
		System.out.println();
		
		while (true){
			
			if (selectSimilar){
				setItems = powerset.getNextSimilar(setItems, 1,3 );
				//
			}else{
				// setItems = powerset.getNextRandom();
				int lo; 
				int hi;
				if ((setItems!=null) && (setItems.size()>0)){
					lo = 4;
					hi = 8;
					if (i%3!=0){
						setItems = powerset.getNextSimilar(setItems, lo,hi );
					}else{
						setItems = powerset.getNextByLength(4, 3,4);
					}
				}else{
					setItems = powerset.getNextRandom();
				}
				
			}
			
			if (setItems.size()>0){
				String str = setItems.toString() ;
				System.out.println(i+"  "+str);
			}else{
				if (i>2){
					break;
				}
			}
			
			if ((setItems.size()>4) && (selectSimilar==false)){
				selectSimilar=true;
				
			}else{
				selectSimilar=false;
			}
			i++;
			if (i>2000){
				break;
			}
		}
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
		println("Welcome to FluidSom!\n");
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
			somtype = SomFluidFactory._INSTANCE_TYPE_OPTIMIZER ;
		}
		if (somtype<=0){ 
			somtype = SomFluidFactory._INSTANCE_TYPE_SOM ;
		}
		
		somInstance = new SomModuleInstanceM1(  
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
			somtype = SomFluidFactory._INSTANCE_TYPE_OPTIMIZER ;
		}
		if (somtype>=1){ 
			somtype = SomFluidFactory._INSTANCE_TYPE_SOM ;
		}
		
		somInstance = new SomModuleInstanceM1( 	 
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
class SomModuleInstanceM1 implements 	Runnable,

										// for messages from the engine to the outer application layers like this module
										SomApplicationEventIntf{
	
	
	SomFluidFactory sfFactory;
	SomFluidProperties sfProperties;
	SomFluidPropertiesHandlerIntf sfPropertiesDefaults;
	
	SomFluidMonoTaskIntf sfTask;
	SomFluidIntf somFluid;
	SomProcessControlIntf somProcessControl ;
	 
	int instanceType = SomFluidFactory._INSTANCE_TYPE_OPTIMIZER;
	
	int absoluteNumberOfExploredCombinations = 10;
	
	String sourceForProperties = "";
	
	String lastProjectName="" ;
	
	// initial number of nodes in the SOM lattice
	int nodeCount = 47;
	
	
	Thread smiThrd;
	
	private boolean resumeMode;
	
	// ------------------------------------------------------------------------
	public SomModuleInstanceM1( int glueMode, String propertiesSource ){
		
		
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
		
		 
		if (instanceType == SomFluidFactory._INSTANCE_TYPE_OPTIMIZER){
			prepareSomOptimizer();
		}
		 
		
	}
	
	public void issueUserbreak() {
		//  
		somProcessControl.interrupt(0);
	}
 
	 
	private void prepareSomOptimizer(){
	
		// loads an existing targeted SOM and starts/continues/repeats the optimizing according to the settings 
		// results are saved or provided to the Glue, then the Som-layer stops / application exits
	
		
		// this might be called with some URL or xml-string that represents the source containing the settings
		// if this exists, it will be loaded
		sourceForProperties = "" ;
		sfProperties = SomFluidProperties.getInstance( sourceForProperties ); // not available
		
		// alternatively... load from /resources in jar ...
		 
		
		if ((sourceForProperties.length()==0) || ( sfProperties.initializationOK()==false )){
		
			sfPropertiesDefaults = new SomFluidPropertiesHandler(sfProperties);
		
			explicitlySettingProperties();
			 
		}
	
		
		sfFactory = SomFluidFactory.get(sfProperties);					   // creating the factory; calling without "factorymode" parameter, optimizer will be assumed	
		 
		 
		
		
		sfFactory.saveStartupTrace( SomFluidFactory._INSTANCE_TYPE_SOM, 
									sfPropertiesDefaults.getStartupTraceInfo() );
		sfProperties.save();
		sfProperties.exportXml();  					// String xstr = sfProperties.getExportedXml();
		
		
		/*
		 * everything is started by "SomFluid" through a queued task mechanism;
		 * 
		 * different application types use different perspectives onto the task
		 * 
		 */
		
		sfTask = (SomFluidMonoTaskIntf)sfFactory.createTask( SomFluidFactory._INSTANCE_TYPE_OPTIMIZER ); //  
		 
		
		sfTask.setContinuity( 1,1) ;                // param1: Level of Spela looping: 1=simple model, 2=checking 
													// param2: number of runs: (1,1) building a stable model, then stop 
													//                         (2,500) including screening S1, S2, max 500 steps in S2
													//                         (2,3,500) max 3 levels in S1
													//      				   (2,0,500) no dependency screening, just evo-optimizing
													//      
		
		sfTask.setStartMode(1) ;             		// default=1 == starting after producing the FluidSom
										 			//        <1 == only preparing, incl. 1 elementary run to load the data, 
		                                 			//              but not starting the modeling process (v switches in the middle between 3..100)
										 			//        >100  minimum delay in millis
		
		
		sfTask = (SomFluidMonoTaskIntf)sfFactory.createTask( ); 
		sfTask.setStartMode(1) ;  
		sfTask.setContinuity(2,0,200);
		 
		
		
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


	
	
	private void explicitlySettingProperties(){
		
		ClassificationSettings cs;
		PersistenceSettings ps;
		ModelingSettings ms;


		
		try {
			
			String instance = "som";
			
			if (instance.contentEquals("som")){

				sfPropertiesDefaults.setInstance( "som" ,nodeCount ); 
									// alternatively: "map" "transformer", or detailed by constants: 
									// (SomFluidFactory._INSTANCE_TYPE_OPTIMIZER, SomFluidProperties._SOMTYPE_MONO)

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
			
			defineSemanticModelingParameters();

			defineGeneralModelingParameters();
			
			
			
			sfPropertiesDefaults.setDataSourcing( "file",0) ; 						// "file", "db", "xml", [0|1] = online learning switch on/off
			sfPropertiesDefaults.setDataSourceName( IniProperties.dataSource );

			String folder = sfPropertiesDefaults.getSupervisedDirectory();  		// ... also available: setSupervisedDirectory(""); 

			
			// optional, if there are indeed plugin files ...
			sfPropertiesDefaults.setAlgorithmsConfigPath( "D:/dev/java/somfluid/plugins/" );
			

			sfPropertiesDefaults.publishApplicationPackage(true, "D:/data/projects/_classifierTesting");
			
			sfProperties.addFilter( "var",0.3,"<",1,1,true);       
									// filter that act on the values of observations
			   						// can be defined only with an existing factory since we need access to the data
			   						// !! not yet functional

			
			sfPropertiesDefaults.initializeDefaults();
			
			
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
		String resultFolder = sfPropertiesDefaults.setResultsPersistence(1);	// optionally: a dedicated path
		       resultFolder = sfPropertiesDefaults.getResultBaseFolder();

		// set ready flag
		sfProperties.setInitializationOK(true) ;						
	}
	
	
	private void defineGeneralModelingParameters() {
		 
		// 					 default = 1 == using reasoning based upon PCA
        					int _smode = SomFluidPropertiesHandlerIntf._INIT_VARSELECTION_METHOD_PCA;
        					
        sfPropertiesDefaults.setMethodforInitialVariableSelection( _smode); 
        sfPropertiesDefaults.preferSmallerModels(true,5) ; //TODO: task relate



		// implicitly activating bagging by maxNodeCount, recordsPerNode
		// bagging will cause a "super"-model integrating the individual ones, which are in parallel
		sfPropertiesDefaults.setBagging( 15, 50 ); 		// in combination, these parameters provoke bagging = several som to be built, if the values are exceeded 	
							// additionally, as option, one may set the absolute number of records beyond which bagging will occur ([],[],6000) 
	
		
		// this applies only to single target models; it will create nested models (mostly just 2) where the first one is a FP=0% model for the anti-target 
		sfPropertiesDefaults.setBooleanAdaptiveSampling( false ) ;  		// goal: activated as default
		
		
		 
		sfProperties.setValidationActive( true );						// whether the model should be validated, applies only if SomType = _SOMTYPE_MONO 
																		// for most validation styles, the validation sample will be drawn before bagging
																		// if optimizing && validation off, internal quality of SOM will be used for optimization
		                       
		sfProperties.setValidationParameters( 20, 46.3 );			    // basic parameters for validation: p1:n repeats p2+=sample sizes to keep aside for validation
																		// also possible: style of validation
		sfProperties.setValidationSampleSizeAutoAdjust(true) ;			// will care for the sample such that enough "cases" are present in the samples   
		
		
		// TODO:  
		// sfProperties.checkDerivationsOnRawData(true) ;
		
	}
	
	
	
	private void defineSemanticModelingParameters() {
		 
		ClassificationSettings cs = sfProperties.getModelingSettings().getClassifySettings() ;

		// the next 4 parameters form a group !! and make sense only for targeted SOM
		
		cs.setErrorCostRatioRiskPreference( 0.18 );                     // if the ECR is not met by the conditions of a node in a developed SOM, it will NOT be
		 																// regarded as a part of the positive prediction, thus contributing all contained cases as "false negatives" 	
		
		cs.setPreferredSensitivity( 0.23);                              // the minimum fraction of cases that should be considered for calculations of cost
																		// if the model does not achieve this portion at ECR, the ECR gets overruled, since the calculation of
																		// of the quality of the data does not make much sense at TP<1%

		cs.setCapacityAsSelectedTotal( 0.35 );							// since the preferred sensitivity can lead to large amounts of data being selected, which in turn
																		// leads to an unrealistic or irrelevant description of the model, we use a further 
																		// threshold: the maximum portion of selected data;
																		// this will be overruled only by the data itself: if there are more cases in the data than this defined portion 		
		
		cs.setEcrAdaptationAllowed(true) ;								// allow for an adaptive ECR; that's important particularly if the initial setting is too optimistic
		
		
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
		
		ModelingSettings ms = sfProperties.getModelingSettings();

		if ((filenames!=null) && (filenames.length>0)){
			filnames = new String[ filenames.length];
			System.arraycopy(filenames, 0, filnames, 0, filnames.length) ;
		}
		
		if ((configByFileExcluded==false) && ((filenames==null) || (filenames.length==0) || (DFutils.fileExists(filenames[0])==false))){
			String filename = sfPropertiesDefaults.checkForVariableDescriptionFile(0) ; // 0 = typology file, 1 = textual description (background information)
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

		
		variableSettings.setInitialSelection( new String[]{"Stammkapital","Bonitaet","Bisher","Branchenscore"});
		variableSettings.setBlackListedVariables( new String[]{"Name","KundenNr"} ) ;
		variableSettings.setAbsoluteExclusions( new String[]{"Name","KundenNr","Land","Region"} , 1);

		//ms.setActiveTvLabel("*TV") ;       			// the target variable; wildcarded templates like "*TV" are possible
		variableSettings.setTargetVariables("*TV"); 	// of course only if instance = "som" (or transformer! for certain transformations)
		                    // sometimes, a file  contains several potential target variables, which would act as confounders if not excluded
							// thus we can mark them all by wildcard... , or by list:
		// variableSettings.setTargetVariables("Mahnung_TV","*TV"); // in this format, the first item always denotes the active target variable 

		// ===>>> any wildcards will be resolved during import of data
		
		// ms.setInitialVariableSelection( new String[]{"Stammkapital","Bonitaet","Bisher","Branchenscore"});
		// sfProperties.getModelingSettings().setRequestForBlacklistVariablesByLabel( new String[]{"Name","KundenNr"}) ;

		
																		  // these variables are excluded once and for all -> they won't get transformed either
																		  // if mode 1+ then they even won't get imported
		//sfProperties.setAbsoluteFieldExclusions( new String[]{"Name","KundenNr","Land","Region"} , 1);
 
		
		variableSettings.setTvGroupLabels("Label") ;
		// ms.setTvGroupLabels("Label") ; 	   							// optional, if available this provides the label of the column that contains the labels for the target groups, if there are any
																		// the only effect will be a "nicer" final output
		//sfProperties.getModelingSettings().setTvLabelAuto("TV") ; 	// the syllable(s) that will be used to identify the target variable as soon as data are available
																	    // allows a series of such identifiers
 	       
		ClassificationSettings cs = sfProperties.getModelingSettings().getClassifySettings() ;
		// 
		if ((sfProperties.getSomType() == SomFluidProperties._SOMTYPE_MONO ) &&
			(cs.getTargetMode() == ClassificationSettings._TARGETMODE_MULTI)){
			
			// may be defined also by file
			if (cs.getTargetGroupDefinition().length==0){
				cs.setTargetGroupDefinition( new double[]{0.28, 0.62, 1.0});	// applies only to MULTI mode, at least 2 values are required (for 1 group interval)
				cs.setTargetGroupDefinitionAuto(true);							//  - if [0,1] AND if _TARGETMODE_MULTI , then the target groups will be inferred from the data
																				//  - TODO in this case, one should be able to provide a "nominal column" that indeed contains the "names"
				cs.setTargetGroupDefinitionExclusions( new double[]{0.4} );		// these values are NOT recognized as belonging to any of the target groups, == singular dot-like holes in the intervals
			}
		}

	}

	
	
	private void resumeSOM() throws Exception{
		
		String startupTraceInfo="", lastproject="", systemroot="", propertiesFileName, dir;
		String[] infoStrings, infopart;
		FileOrganizer fileorg ;
		
		try {
											System.out.println("resuming...");
			instanceType = SomFluidFactory._INSTANCE_TYPE_SOM;
			
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
			
			dir = fileorg.getObjectStoreDir();
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

