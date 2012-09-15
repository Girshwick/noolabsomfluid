package org.NooLab.demoiinstances;


import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import processing.core.*;

import org.NooLab.field.FieldIntf;
import org.NooLab.somfluid.SomApplicationResults;
import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidIntf;
import org.NooLab.somfluid.SomFluidMonoResultsIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidResultsIntf;
import org.NooLab.somfluid.SomFluidStateDescriptionIntf;
import org.NooLab.somfluid.clapp.SomAppUsageIntf;
import org.NooLab.somfluid.clapp.SomApplicationEventIntf;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.core.engines.det.results.ValidationSet;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.properties.ValidationSettings;
import org.NooLab.somfluid.storage.ContainerStorageDevice;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somfluid.tasks.SomFluidMonoTaskIntf;
import org.NooLab.somfluid.tasks.SomFluidProbTaskIntf;
import org.NooLab.somfluid.tasks.SomFluidTask;
import org.NooLab.somfluid.util.PowerSetSpringSource;
import org.NooLab.somtransform.algo.externals.AlgorithmPluginsLoader;
import org.NooLab.structures.InstanceProcessControlIntf;
import org.NooLab.utilities.callback.ProcessFeedBackContainerIntf;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexDistanceIntf;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.LogControl;





/**
 * 
 * Later: this applet should start the SomFluid as an application!!
 * Any communication should be performed through Glue
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
 * 
 */



public class M2_loadmodifysettings_SomFluidModuleApplet extends PApplet{

	 
	private static final long serialVersionUID = 8918471551051086099L;
	
	SomModuleInstanceM2 somInstance ;
	String sourceForProperties = "";
	
	
	public void setup(){
		 
		showKeyCommands();
		
		background( 208,188,188);
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

		if (key=='c'){
			// somInstance.classifyData();
		}
		
		if (key=='m'){
			startEngines() ;
		}
		
		if (key=='r'){
			resume() ;
		}
		
		if (key=='t'){
			
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
		println("Welcome to FluidSom!\n");
		println("the following key commands are available for minimalistic user-based control...");
		println();
		println("   m  ->  start modeling, start from scratch by importing the data file ");
		println("   r  ->  resume modeling, load persistent models, and continue modeling ");
		
		println("   t  ->  apply just and only the transformation model and export the transformed data ");
		println();
		println("   c  ->  apply a previously exported model to new data = perform the classification task ");
		println("   i  ->  activate the file listener, which digests new data from a directory ");
		println();
		println("   d  ->  duplicate project except exports and results ");
		println("   D  ->  duplicate project, but remove everything except the data file and the transformation rules ");
		println();
		println("   p  ->  create a new project in the project space, the data file must exist somewhere to copy from! ");
		println("   z  ->  interrupt the current process, export current results and write persistent models ");
		
		println("   x  ->  exit");
		println();
		println("------------------------------------------------------------------");
		
	}
	
	private void startEngines(){
		println();
		println("starting...");
		
		somInstance = new SomModuleInstanceM2( FieldIntf._INSTANCE_TYPE_SOM , 
			 									SomFluidFactory._GLUE_MODULE_ENV_NONE,
			 									sourceForProperties ) ;
		somInstance.startInstance() ;
	}
	
	private void resume(){
		
		somInstance = new SomModuleInstanceM2( FieldIntf._INSTANCE_TYPE_SOM , 
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
class SomModuleInstanceM2 implements 	Runnable,

										// for messages from the engine to the outer application layers like this module
										SomApplicationEventIntf{
	
	
	SomFluidFactory sfFactory;
	SomFluidProperties sfProperties;
	SomFluidIntf somFluid;
	InstanceProcessControlIntf somProcessControl ;
	
	int instanceType = 1;
	int glueModuleMode = 0;
	String sourceForProperties = "";
	
	// initial number of nodes in the SOM lattice
	int nodeCount = 47;
	
	
	Thread smiThrd;
	@SuppressWarnings("unused")
	private boolean resumeMode;
	
	public SomModuleInstanceM2(int instancetype, int glueMode, String propertiesSource ){
		
		instanceType = instancetype;
		
		glueModuleMode =  glueMode ;
		
		sourceForProperties = propertiesSource;
		
		smiThrd = new Thread(this, "SomModuleInstance");
	 
	}
	 
	public void setResumeMode(boolean flag) {
		//
		resumeMode = flag;
	}

	public void classifyDataApplyModel() {
		SomAppUsageIntf somApp ;
		double[] data = new double[5] ;
		String idStr;
		SomApplicationResults somResult;
		
		somApp = sfFactory.getSomApplication();
		somApp.waitForResults(true) ; // no decoupling, direct return of results
		
		// if wait, provide the callback:  idStr = somApp.classify((SomApplicationEventIntf)this,)
		somResult = somApp.classify(  null, data ); // double[], ArrayList<Double>
		
		// put this idStr to a map, but let the SomApp do that
	}

	public void resume() {
		// 
		
		resumeMode=true;
		smiThrd.start();
	}

	public void startInstance(){
		smiThrd.start();
	}
	
	public void run() {
		
		/*
		 *  dependent on instanceType, we will initialize a particular primary functionality, 
		 *  the constants are available statically through "SomFluidFactory._INSTANCE_TYPE_SOM ..."
		 *    1 = som
		 *    2 = sprite 
		 *    3 = optimizer 
		 *    4 = transformer 
		 *  
		 * the issue is that those 4 groups of functionalities all depend on each other 
		 * and can't be separated from each other in a sound manner;
		 * any of those will use one of the other at some point during modeling,
		 * 
		 * Nevertheless the module should be able to exhibit a particular "main" functionality 
		 * 
		 */
		if (instanceType == FieldIntf._INSTANCE_TYPE_SOM){
			if (resumeMode){
				resumeMode=false;
				try {
					resumeSOM();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				prepareSOM();
			}
		}
		if (instanceType == FieldIntf._INSTANCE_TYPE_OPTIMIZER){
			prepareSomOptimizer();
		}
		if (instanceType == FieldIntf._INSTANCE_TYPE_SPRITE){
			prepareSomSprite();
		}
		if (instanceType == FieldIntf._INSTANCE_TYPE_TRANSFORM){
			prepareSomTransformer();
		}
		
	}
	
	public void issueUserbreak() {
		//  
		somProcessControl.interrupt(0);
	}

	private void prepareSomTransformer() {
		// opens the SomTransformer interface, accepting data and transformation rules 
		
	}

	private void prepareSomSprite() {
		// opens the sprite interface: accepts a small table (role: SomMapTable) and performs a dependency search
		
	}

	private void prepareSomOptimizer() {
		// loads an existing targeted SOM and starts/continues/repeats the optimizing according to the settings 
		// results are saved or provided to the Glue, then the Som-layer stops / application exits
	
	}
	
	private void explicitlySettingProperties(){
		
		ClassificationSettings cs;
		PersistenceSettings ps;
		AlgorithmPluginsLoader lap ;
		
		SomFluid sf;

		// this creates an XML string with all parameters and their default values
		String str = sfProperties.getDefaultExport();
		
		/*
		 * note that there are a lot more of parameters that could be set for the SOM in order to exert full control,
		 * e.g. in the context of commercial predictions;
		 * yet, there are reasonable default values, and in the long run those parameters are 
		 * adjusted autonomously anyway by the system itself  
		 */

		
		// loading "builtinscatalog.xml" which is necessary for global indexing and activation of built-in algorithms
		sfProperties.setAlgorithmsConfigPath("D:/dev/java/somfluid/plugins/"); 

		// here we need an absolute path, the file is needed also for advanced auto assignments of built-in algorithms, 
		// even if there are no custom algorithms
		sfProperties.getPluginSettings().setBaseFilePath("D:/dev/java/somfluid/plugins/", "catalog.xml") ;
		// the plugin jarfiles are expected to sit in a relative sub-dir "transforms/" to this base-dir...

		sfProperties.setPluginsAllowed(false) ; // could be controlled form the outside in a dynamic manner

		try {
			if (sfProperties.isPluginsAllowed()){
				lap = new AlgorithmPluginsLoader(sfProperties, true);
				if (lap.isPluginsAvailable()) {
					lap.load();
				}
			}
			// feedback about how much plugins loaded ...
		} catch (Exception e) {
			e.printStackTrace();
		}
		// sfProperties.importTransformationParameterDefaults("standards.ini");
		
		
		sfProperties.setInstanceType( instanceType ) ;                     // the main role the module is exhibiting, MANDATORY !!!
							 								               // _SOM  _OPTIMIZER _SPRITE  _TRANSFORM

		// target oriented modeling 
		// lattice
		sfProperties.setSomType( FieldIntf._SOMTYPE_MONO ) ;      // we define to create a SOM for targeted modeling 

		sfProperties.setInitialNodeCount(nodeCount);                       // initial size; yet it does not matter much since the SomFluid will grow anyway 
		
		
		sfProperties.setAutoAdaptResolutionAllowed(1) ;					   // this will allow SomFluid to choose a proper size and a proper 
																		   // number of particles (resolution), dependent on the data 
		
		// data
		int srctype = SomFluidProperties._SRC_TYPE_FILE;
		 
		sfProperties.setPathToSomFluidSystemRootDir("D:/data/projects/");
		
		ps = sfProperties.getPersistenceSettings();
		ps.setIncomingDataSupervisionDir("");
		ps.setIncomingDataSupervisionActive(false);
		
		ps.setProjectName("bank2"); 									   // will be used also for output files
		ps.setKeepPreparedData(true); 									   // includes persistence of transformer model
		ps.autoSaveSomFluidModels(true);
		ps.autoPackagingOfCompleteModels(true);
		
		

		
		// sfProperties.addDataSource( srctype,"D:/data/raw/simprofiles.txt");// the basic mode: data from a file; it also can receive data through ports

		// a more difficult file
		sfProperties.addDataSource( srctype,"bankn_d2.txt");               // if the persistence settings are available, the relative path will be guessed
		// sfProperties.addDataSource( srctype,"D:/data/projects/bank2/data/raw/bankn_d2.txt"); we can also provide a file from an arbitrary location
		
		sfProperties.setExtendingDataSourceEnabled(false); 				   // default=false; true for data updates via internal Glue-client or via directory supervision for online learning
		
		
		sfProperties.setSimulationMode( SomFluidProperties._SIM_NONE);     // default=_NONE; can be used to create data from (apriori) profiles, 
		 															       //                or to extend the body of data by surrogate data (random, but same distribution and same covar)
																		   // use _SIM_PROFILES if the provided data describe prototypical apriori profiles
		sfProperties.setSimulationSize(3000) ; 							   // applies only if simulation mode <> _SIM_NONE

		   																   // note that the data are not loaded at that point,
	       																   // such it is only a request for blacklisting variables
		sfProperties.getModelingSettings().setRequestForBlacklistVariablesByLabel( new String[]{"Name","KundenNr"}) ;

																		   // these variables are excluded once and for all -> they won't get transformed either
																		   // if mode 1+ then they even won't get imported
		sfProperties.setAbsoluteFieldExclusions( new String[]{"Name","KundenNr"} , 1);

		sfProperties.getModelingSettings().setActiveTvLabel("*TV") ;       // the target variable; wildcarded templates like "*TV" are possible
		sfProperties.getModelingSettings().setTvGroupLabels("Label") ; 	   // optional, if available this provides the label of the column that contains the labels for the target groups, if there are any
																		   // the only effect will be a "nicer" final output
		//sfProperties.getModelingSettings().setTvLabelAuto("TV") ;        // the syllable(s) that will be used to identify the target variable as soon as data are available
																	       // allows a series of such identifiers

        sfProperties.getModelingSettings().setInitialAutoVariableSelection(1); // default=0==no; 1= by PCA, 2= by canonical KNN, 3= combined by PCA & KNN (not avail. yet)
        sfProperties.getModelingSettings().setInitialVariableSelection( new String[]{"Stammkapital","Bonitaet","Bisher","Branchenscore"});
        
		sfProperties.getModelingSettings().setTargetedModeling(true) ;     // VERY important setting, determines the fundamental mode in which the SOM will run
																		   // invokes validation, and eventually feature selection via evo + sprite
																		   // by default, mode is "_TARGETMODE_SINGLE"
		
		sfProperties.getModelingSettings().setCanonicalReduction(true);	   // 
		
		
		
		cs = sfProperties.getModelingSettings().getClassifySettings() ; 
		cs.setTargetMode(ClassificationSettings._TARGETMODE_MULTI ) ;      // requires the determination of values that define an interval for a target group
																	       // a virtual column will be created which encodes these settings (by SomTransformer)
		cs.setTargetMode(ClassificationSettings._TARGETMODE_SINGLE ) ;
		
		cs.setSingleTargetGroupDefinition( 0.1, 0.41);		               // min max of the interval [0|1][ min|max] in case of _TARGETMODE_SINGLE, ineffective if _TARGETMODE_MULTI
		
		cs.setTargetGroupDefinition( new double[]{0.28, 0.62, 1.0});	   // applies only to MULTI mode, at least 2 values are required (for 1 group interval)
		cs.setTargetGroupDefinitionAuto(true);							   //  - if [0,1] AND if _TARGETMODE_MULTI , then the target groups will be inferred from the data
																		   //  - TODO in this case, one should be able to provide a "nominal column" that indeed contains the "names"
		cs.setTargetGroupDefinitionExclusions( new double[]{0.4} );		   // these values are NOT recognized as belonging to any of the target groups, == singular dot-like holes in the intervals
		
		
		// the next 4 parameters form a group !! and make sense only for targeted SOM
		
		cs.setErrorCostRatioRiskPreference( 0.18 );                        // if the ECR is not met by the conditions of a node in a developed SOM, it will NOT be
		 																   // regarded as a part of the positive prediction, thus contributing all contained cases as "false negatives" 	
		
		cs.setPreferredSensitivity( 0.15);                                 // the minimum fraction of cases that should be considered for calculations of cost
																		   // if the model does not achieve this portion at ECR, the ECR gets overruled, since the calculation of
																		   // of the quality of the data does not make much sense at TP<1%

		cs.setCapacityAsSelectedTotal( 0.35 );							   // since the preferred sensitivity can lead to large amounts of data being selected, which in turn
																		   // leads to an unrealistic or irrelevant description of the model, we use a further 
																		   // threshold: the maximum portion of selected data;
																		   // this will be overruled only by the data itself: if there are more cases in the data than this defined portion 		
		
		cs.setEcrAdaptationAllowed(true) ;								   // allow for an adaptive ECR
		
		//
		
		
		cs.addExtendedResultRequest( ClassificationSettings._XREQ_MISCLASSIFICATIONS_FULL ) ;
		cs.addExtendedResultRequest( ClassificationSettings._XREQ_ROC_FULL) ;
		cs.addExtendedResultRequest( ClassificationSettings._XREQ_OPTIMALCUTS,3, 0.95 ) ;
		
		sfProperties.getModelingSettings().setExtendedDiagnosis( true ) ;  // activates some post-calculation investigations a la SPELA
																		   // ParetoPopulationExplorer, SomModelDescription, Coarseness, MultiCrossValidation, MetricsStructure 
		
				    													   // whichever of these 4 stopping criteria for the optimizer is reached first...
		sfProperties.getModelingSettings().getOptimizerSettings().setMaxStepsAbsolute( 35 );     // low only for testing, or initial exploration, typically 500+
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -       // note that this step-count applies WITHIN a step on L2 as 
																								 // controlled by "setMaxL2LoopCount" (see below, typically max=4)
		
		sfProperties.getModelingSettings().getOptimizerSettings().setMaxAvgVariableVisits(21) ;  // required for comparing models across population regarding the variable selections 
		sfProperties.getModelingSettings().getOptimizerSettings().setDurationHours(0.8) ;        // an absolute time limit in hours 
		sfProperties.getModelingSettings().getOptimizerSettings().setStepsAtLeastWithoutChange(300) ; // stop if there is no improvement for some time
		
		sfProperties.setDataUptakeControl(0);                              // if negative, the data won't load automatically into the SOM
		
		sfProperties.defineSomBags(50,35,6000);							   // defines bagging, based on supplied parameters:
		sfProperties.applySomBags( false) ;			   					   // p1: min records per node, p2: max number of nodes, p3: max number of records 	
		sfProperties.setAutoSomBags( false ) ;						       // if true (=default) no settings have to be explicitly defined, adaptive default values will be taken  			
		

		// we also may consider to split-bag in case of NVE : config: max number of groups target variables,
		// such to build small soms for any of the items, and combine them to a 
		// compound model
		
		sfProperties.setAbsoluteRecordLimit(-1); // 434) ;
		
		sfProperties.setMaxL2LoopCount(5) ;								   // if>1 invokes somsprite and somscreen for optimizing the feature vector
		
		sfProperties.setRestrictionForSelectionSize(678) ;				   // no more than [N] nodes will be selected; that means, if the SOM grows beyond a certain size, 
																	       // a symmetry break will occur
																		   // if the size of a SOM grows to  sqrt(mapsize) > 3.5*sqrt(n/pi)
																	       // then the coarse-som preprocessing will be organized, if it is allowed
		
		sfProperties.setGrowthMode( ModelingSettings._SOM_GROWTH_PRESELECT);// growth modes can be combined ! PRESELECT -> coarse-som preprocessing 
		sfProperties.setGrowthMode( ModelingSettings._SOM_GROWTH_LATERAL); //  
		sfProperties.removeGrowthMode( ModelingSettings._SOM_GROWTH_LATERAL); 
		sfProperties.setGrowthSizeAdaptationIntensity( 5 ) ;			   // 5=normal == default, 1=greedy -> large nodes, rather small lattice, 10=allowing sparsely filled nodes 
				
		sfProperties.setActivationOfGrowing( true );                       // activates/deactivates growing without removing the settings, default=true
		
		sfProperties.setWinnersCountMultiple(1) ; 							   // max 5, if=1 == default = single winner
																	       // only the best winner will be actually updated by the data ;
																		   // the further winners only update their profile
																		   // in most cases, a singular winner (n=1) provides the best results
		                                                                   // more winners leads to "smearing" of information
		 
		sfProperties.setValidationActive( true );						   // whether the model should be validated, applies only if SomType = _SOMTYPE_MONO 
																		   // for most validation styles, the validation sample will be drawn before bagging
																		   // if optimizing && validation off, internal quality of SOM will be used for optimization
		                       
		sfProperties.setValidationParameters( 20, 46.3 );			       // basic parameters for validation: p1:n repeats p2+=sample sizes to keep aside for validation
																		   // also possible: style of validation
		sfProperties.setValidationSampleSizeAutoAdjust(true) ;			   // will care for the sample such that enough "cases" are present in the samples   
		
		sfProperties.addSurrogatedSimulationData( 0.21, 0.3, 1 ) ; 		   // amount of records as in fraction, amount of noise as fraction of stdev per variable
																		   // this results in a more robust model, since small differences are prevented from being overweighted
		
		sfProperties.surrogateAppMode( 0,1,0 ) ; 						   // global on/off, initial modeling on/off, optimizing on/off 

		sfProperties.setShowSomProgress( SomFluidProperties._SOMDISPLAY_PROGRESS_BASIC ) ;
		// sfProperties.setShowSomProgress( SomFluidProperties._SOMDISPLAY_PROGRESS_STEPS ) ;
		
		
		// results
		// sfProperties.sampleVariableContributionsThruTopModels( int n_models = 10 )
																		   // defining what should happen upon results
																		   // saving, sending, displaying, nothing
		
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
		
		sfProperties.getOutputSettings().setResultfileOutputPath("") ;     // there is a default !
		sfProperties.getOutputSettings().setResultFilenames( _prepareResultFilesMap() ); // there is a default
		sfProperties.getOutputSettings().setAsXml(false);                  // default = false
		sfProperties.getOutputSettings().createZipPackageFromResults(true);           // default = true
		
		
		// TODO:  
		// sfProperties.checkDerivationsOnRawData(true) ;
		// sfProperties.setExportExtendedResultDataTable(true) ; 		   // writes a data table which contains the original variables + the MLE estimated derived variables
		
		// general env

		sfProperties.setMessagingActive(false) ;					       // if true, the SomFluidFactory will start the glueClient ;
		   																   // the SomFluid is then accessible through messaging (see the SomController application)
		sfProperties.setglueModuleMode( glueModuleMode ) ;

		
		sfProperties.activateMultithreadedProcesses(false);

		
		sfProperties.setInitializationOK(true) ;
		
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
	
	
	private void prepareSOM(){
		
		
		/* TODO 
		 * - we need a similar persistence mechanism as for the glue stuff !!
		 *   each SOM has a unique ID, like serial UID (via anonymous UID server)
		 * 
		 * - establish a glue connection if desired
		 * 
		 * - everything is handled by the factory
		 */
		
		
		
		// this might be called with some URL or xml-string that represents the source containing the settings
		// if this exists, it will be loaded
		sfProperties = SomFluidProperties.getInstance( sourceForProperties );	
		
		
		if ((sourceForProperties.length()==0) || ( sfProperties.initializationOK()==false )){
			explicitlySettingProperties();
		}
		
		 
		sfFactory = SomFluidFactory.get(sfProperties);					   // creating the factory	
		
		  
		sfProperties.addFilter( "var",0.3,"<",1,1,true);        // filter that act on the values of observations
																		   // can be defined only with an existing factory since we need access to the data
																		   // not yet functional
		
		sfFactory.saveStartupTrace(FieldIntf._INSTANCE_TYPE_SOM, _prepareStartupTraceInfo());
		sfProperties.save();
		
		SomFluidMonoTaskIntf sfTask = (SomFluidMonoTaskIntf)sfFactory.createTask( ); //  
		 
 		
		sfTask.setContinuity( 1,1) ;                 // param1: Level of Spela looping: 1=simple model, 2=checking 
													// param2: number of runs: (1,1) building a stable model, then stop 
													//                         (2,500) including screening S1, S2, max 500 steps in S2
													//                         (2,3,500) max 3 levels in S1
													//      				   (2,0,500) no dependency screening, just evo-optimizing
													//      
		
		sfTask.setStartMode(1) ;             		// default=1 == starting after producing the FluidSom
										 			//        <1 == only preparing, incl. 1 elementary run to load the data, 
		                                 			//              but not starting the modeling process (v switches in the middle between 3..100)
										 			//        >100  minimum delay in millis
		
		
		
		//           	
	
		sfTask = (SomFluidMonoTaskIntf)sfFactory.createTask( ); 
		sfTask.setStartMode(1) ;  
		sfTask.setContinuity(2,0,200);
		
		try {
			
			sfFactory.produce( sfTask );			// this produces the SomFluid and the requested som-type according to
													// SomFluidProperties._SOMTYPE_MONO, referring implicitly to sfTask; 
													//
			
		} catch (Exception e) {
		 
			e.printStackTrace();
		}
		
		// if we like to have graphical output, then start the applet for displaying it and 
		// define shake hands by means of GlueClients...
		
		
		/*
		 
		...some important API stuff for a running system
		 
		sfFactory.getSomTransformer().introduceTransformation( );
		sfFactory.getSomTransformer().introduceTransformations( filename );
		sfFactory.getSomData().addObservations( ... );
		sfFactory.getSomTransformer().reFreshCalculation( ); // e.g. after adding data
		
		*/
	}

	private String _prepareStartupTraceInfo() {
		
		String cfgroot, userdir, lastproject, infoStr="";
		
		
		cfgroot =  sfProperties.getSystemRootDir() ;
		lastproject = sfProperties.getPersistenceSettings().getProjectName() ;
		
		// TODO better -> several categories: load file, modify it, write it back
		infoStr =  "cfgroot::"+cfgroot+"\n" +
		           "project::"+lastproject+"\n" ;
				                           	   

		
		return infoStr;
	}

	private IndexedDistances _prepareResultFilesMap(){
		
		IndexDistance filmapItem ;
		IndexedDistances rfMap = new IndexedDistances() ;
		
		
		
		return rfMap;
	}
	
	
	
	public void importNewProperties( String xmlPropertiesSource){
		
		
	}
	
	// ------------------------------------------------------------------------
 
	 
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

	
	// call back events for providing informaton about the state of modeling

	@Override
	public void onCalculation(double fractionPerformed) {
		// in case of large tasks
	}

	@Override
	public void onProcessStarted(SomFluidTask sfTask, int applicationId, String pid) {
		// TODO Auto-generated method stub
		
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

