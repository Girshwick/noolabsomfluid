package org.NooLab.demoiinstances;

 

import processing.core.*;

import org.NooLab.somfluid.SomAppFactoryClientIntf;
import org.NooLab.somfluid.SomApplicationIntf;
import org.NooLab.somfluid.SomApplicationResults;
import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidFactoryClassifierIntf;
import org.NooLab.somfluid.SomFluidMonoResultsIntf;
import org.NooLab.somfluid.SomFluidMonoTaskIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomProcessControlIntf;
import org.NooLab.somfluid.app.SomAppProperties;
import org.NooLab.somfluid.app.SomAppUsageIntf;
import org.NooLab.somfluid.app.SomApplicationEventIntf;

import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somtransform.algo.externals.AlgorithmPluginsLoader;
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
			 somInstance.classifyData(0);
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
		
		println("   z  ->  interrupt the current process, export current results and write persistent models ");
		
		println("   x  ->  exit");
		println();
		println("------------------------------------------------------------------");
		
	}
	
	private void startEngines(){
		println();
		println("starting...");
		
		
		
		somInstance = new SomModuleInstanceA1( SomFluidFactory._INSTANCE_TYPE_CLASSIFIER, 
			 									SomFluidFactory._GLUE_MODULE_ENV_NONE,
			 									sourceForProperties ) ;
		somInstance.startInstance() ;
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
class SomModuleInstanceA1 implements 	Runnable,
										// for messages from the engine to the outer application layers like this module
										SomApplicationEventIntf{
	
	
	//SomFluidFactory sfFactory;
	SomFluidFactoryClassifierIntf sfcFactory;
	
	SomFluidFactory sfFactory ;
	SomAppFactoryClientIntf sfaFactory ;
	
	SomFluidProperties sfProperties;
	SomApplicationIntf somApp ;
	
	SomProcessControlIntf somProcessControl ;

	
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
		if (instanceType == SomFluidFactory._INSTANCE_TYPE_CLASSIFIER){
			
			prepareSomFluidClassifier();
		}
  
		
	}
	
	public void issueUserbreak() {
		//  
		somProcessControl.interrupt(0);
	}

	 
 
	
	private void explicitlySettingAppProperties(){
		
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
		sfProperties.setSomType( SomFluidProperties._SOMTYPE_MONO ) ;      // we define to create a SOM for targeted modeling 
 
		
		// data
		int srctype = SomFluidProperties._SRC_TYPE_FILE;
		 
		sfProperties.setPathToSomFluidSystemRootDir("D:/data/projects/");
		
		ps = sfProperties.getPersistenceSettings();
		ps.setIncomingDataSupervisionDir("");
		ps.setIncomingDataSupervisionActive(false);
		
		ps.setProjectName("bank2"); 									   // will be used also for output files
		 

		
		// sfProperties.addDataSource( srctype,"D:/data/raw/simprofiles.txt");// the basic mode: data from a file; it also can receive data through ports

		// a more difficult file
		sfProperties.addDataSource( srctype,"bankn_d2.txt");               // if the persistence settings are available, the relative path will be guessed
		// sfProperties.addDataSource( srctype,"D:/data/projects/bank2/data/raw/bankn_d2.txt"); we can also provide a file from an arbitrary location
		
		sfProperties.setExtendingDataSourceEnabled(false); 				   // default=false; true for data updates via internal Glue-client or via directory supervision for online learning
		 
		
		cs = sfProperties.getModelingSettings().getClassifySettings() ; 
		cs.setTargetMode(ClassificationSettings._TARGETMODE_MULTI ) ;      // requires the determination of values that define an interval for a target group
																	       // a virtual column will be created which encodes these settings (by SomTransformer)
		cs.setTargetMode(ClassificationSettings._TARGETMODE_SINGLE ) ;
		
		cs.setSingleTargetGroupDefinition( 0.1, 0.41);		               // min max of the interval [0|1][ min|max] in case of _TARGETMODE_SINGLE, ineffective if _TARGETMODE_MULTI
		
		cs.setTargetGroupDefinition( new double[]{0.28, 0.62, 1.0});	   // applies only to MULTI mode, at least 2 values are required (for 1 group interval)
		cs.setTargetGroupDefinitionAuto(true);							   //  - if [0,1] AND if _TARGETMODE_MULTI , then the target groups will be inferred from the data
																		   //  - TODO in this case, one should be able to provide a "nominal column" that indeed contains the "names"
		cs.setTargetGroupDefinitionExclusions( new double[]{0.4} );		   // these values are NOT recognized as belonging to any of the target groups, == singular dot-like holes in the intervals


		

		sfProperties.setMessagingActive(false) ;					       // if true, the SomFluidFactory will start the glueClient ;
		   																   // the SomFluid is then accessible through messaging (see the SomController application)
		sfProperties.setglueModuleMode( glueModuleMode ) ;

		
		sfProperties.activateMultithreadedProcesses(false);

		
		sfProperties.setInitializationOK(true) ;
		
	}
	
	
	public String classifyData(int index){
		
		SomApplicationResults somResult;
		
		// somResult = somApp.classify(  null, data ); // double[], ArrayList<Double>
		// put this idStr to a map, but let the SomApp do that

		// returns a UUID = (short timestamp +"-"+ index +"-"+ GUID) , which serves as an identifier
		
		return "";
	}

	 
	private void prepareSomFluidClassifier(){
		
		SomAppProperties clappProperties;
		
		clappProperties = SomAppProperties.getInstance( sourceForProperties );
		
		sfaFactory = SomFluidFactory.get(clappProperties);
		 
		somApp = sfaFactory.createSomApplication( clappProperties );   
		
 

	}
	
	
	private void prepareSOM(){
		
		 
		
		// this might be called with some URL or xml-string that represents the source containing the settings
		// if this exists, it will be loaded
		sfProperties = SomFluidProperties.getInstance( sourceForProperties );	
		
		// load from /resources in jar
		
		if ((sourceForProperties.length()==0) || ( sfProperties.initializationOK()==false )){
			// explicitlySettingProperties();
		}
		
		 
		sfFactory = SomFluidFactory.get(sfProperties);					   // creating the factory	
		
		  
		sfProperties.addFilter( sfFactory, "var",0.3,"<",1,1,true);        // filter that act on the values of observations
																		   // can be defined only with an existing factory since we need access to the data
																		   // not yet functional
		
		sfFactory.saveStartupTrace(SomFluidFactory._INSTANCE_TYPE_SOM, _prepareStartupTraceInfo());
		sfProperties.save();
		sfProperties.exportXml();
		String xstr = sfProperties.getExportedXml();
		
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
		
		
		
		// sfFactory.produce( sfTask );          	// this produces the SomFluid and the requested som-type according to
													// SomFluidProperties._SOMTYPE_MONO, referring implicitly to sfTask; 
													//
	
		sfTask = (SomFluidMonoTaskIntf)sfFactory.createTask( ); 
		sfTask.setStartMode(1) ;  
		sfTask.setContinuity(2,0,200);
		
		sfFactory.produce( sfTask );
		
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
	

	
}

