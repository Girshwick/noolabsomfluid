package applet;


import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidIntf;
import org.NooLab.somfluid.SomFluidMonoResultsIntf;
import org.NooLab.somfluid.SomFluidMonoTaskIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidResultsIntf;
import org.NooLab.somfluid.SomFluidStateDescriptionIntf;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.env.communication.GlueBindings;
import org.NooLab.utilities.logging.LogControl;

import processing.core.PApplet;



public class SomFluidModuleApplet extends PApplet{

	 
	private static final long serialVersionUID = 8918471551051086099L;
	
	SomModuleInstance somInstance ;
	
	
	
	public void setup(){
		 
		showKeyCommands();
		
		background( 208,188,188);
		// start this in its own thread...
		
		// use LogControl to control output of log messages
		LogControl.globeScope = 2; // 1=local definitions  default = 2
		LogControl.Level = 2 ;

		
		somInstance = new SomModuleInstance();
			
	}
	
	
	
	public void draw(){
		 
	}


	public void keyPressed() {

	 
		  
		
		if (key=='x'){
			// somInstance. ...
			System.exit(0);
		}
	}
	
	 
	private void showKeyCommands(){

		println();
		println("Welcome to FluidSom!\n");
		println("the following key commands are available...");
 
		println("   x  ->  exit");
		println();
		println("------------------------------------------------------------------");
		println("starting...");
	}
	
}



// ============================================================================




class SomModuleInstance implements 	Runnable,
								    SomFluidResultsIntf{
	
	
	SomFluidFactory sfFactory;
	SomFluidProperties sfProperties;
	SomFluidIntf somFluid;
	
	int nodeCount = 61;
	
	GlueBindings glueConnection;
	
	
	Thread smiThrd;
	
	public SomModuleInstance(){
		
		smiThrd = new Thread(this, "SomModuleInstance");
		smiThrd.start();
	}
	 
	public void run() {
		
		prepareSOM();
	}
	
	private void prepareSOM(){
		ClassificationSettings cs;
		/* TODO 
		 * - we need a similar persistence mechanism as for the glue stuff !!
		 *   each SOM has a unique ID, like serial UID (via anonymous UID server)
		 * 
		 * - establish a glue connection if desired
		 * 
		 * - everything is handled by the factory
		 */
		
		
		// this might be called with some URL or xml-string that represents the source containing the settings
		sfProperties = SomFluidProperties.getInstance();	
		
		// this creates an XML string with all parameters and their default values
		String str = sfProperties.getDefaultExport();
		
		/*
		 * note that there are a lot more of parameters that could be set for the SOM in order to exert full control,
		 * e.g. in the context of commercial predictions;
		 * yet, there are reasonnable default values, and in the long run those parameters are 
		 * adjusted autonomously anyway by the system itself  
		 */
		
		
		// target oriented modeling 
		// lattice
		sfProperties.setSomType( SomFluidProperties._SOMTYPE_MONO ) ;      // we define to create a SOM for targeted modeling 
		sfProperties.setInitialNodeCount(nodeCount);                             // initial size; yet it does not matter much since the SomFluid will grow anyway 
		
		sfProperties.setAutoAdaptResolutionAllowed(1) ;					   // this will allow SomFluid to choose a proper size and a proper 
																		   // number of particles (resolution), dependent on the data 
		
		
		
		// data
		sfProperties.addDataSource(1,"D:/data/raw/simprofiles.txt") ;      // the basic mode: data from a file; it also can receive data through ports
		// sfProperties.extendDataSource(); also via internal Glue-client
		
		sfProperties.getModelingSettings().setActiveTvLabel("TV") ;        // the target variable; wildcarded templates like "*TV" are possible 
		sfProperties.getModelingSettings().setTargetedModeling(true) ;     // invokes validation, and eventually feature selection via evo + sprite
																		   // by default, mode is "_TARGETMODE_SINGLE"
		cs = sfProperties.getModelingSettings().getClassifySettings() ; 
		cs.setTargetMode(ClassificationSettings._TARGETMODE_MULTI ) ;      // requires the determination of values that define an interval for a target group
																	       // a virtual column will be created which encodes these settings (by SomTransformer) 
		cs.setSingleTargetGroupDefinition( 0.28, 0.62);		               // min max of the interval [0|1][ min|max] in case of _TARGETMODE_SINGLE
		cs.setErrorCostRatioRiskPreference( 0.04 );
		
		cs.addExtendedResultRequest( ClassificationSettings._XREQ_MISCLASSIFICATIONS_FULL ) ;
		cs.addExtendedResultRequest( ClassificationSettings._XREQ_ROC_FULL) ;
		cs.addExtendedResultRequest( ClassificationSettings._XREQ_OPTIMALCUTS,3, 0.95 ) ;
		
				
		sfProperties.setDataUptakeControl(0);                              // if negative, the data won't load automatically into the SOM
		sfProperties.setMessagingActive(false) ;						   // if true, the SomFluidFactory will start the glueClient.start();
		
		sfProperties.defineSomBags(50,35,6000);							   // applies bagging, based on supplied parameters:
		sfProperties.applySomBags( false) ;			   					   // p1: min records per node, p2: max number of nodes, p3: max number of records 	
								
		sfProperties.activateMultithreadedProcesses(false);
		
		// TODO
		sfProperties.setRestrictionForSelectionSize(678) ;				   // if the size of a SOM grows to  sqrt(mapsize) > 3.5*sqrt(n/pi)
																	       // then organize coarse-som preprocessing
		
		sfProperties.setMultipleWinners(1) ; 							   // max 5, if=1 == default = single winner
		
		// results
																		   // defining what should happen upon results
		//
		
		
		sfFactory = SomFluidFactory.get(sfProperties);					   // creating the factory	

		
		// environment                                                     // setting up "wireless" = file-free connectivity by the Glue messaging system
		
		
		
		// starting it... yet, the SomFluid is not directly accessible, like this...
		// sfFactory.getSomFluid().start();
		
		// in order to be compatible with the config for a continuously active system, 
		// we instead create a task using the factory
		// this task refers to the properties as defined above, but it also can overwrite them
		
		// we have to select the appropriate perspective: "Mono" or "Prob"
		// the type cast has to match the setting of the SomType
		SomFluidMonoTaskIntf sfTask = (SomFluidMonoTaskIntf)sfFactory.createTask( ); //  
		 
		sfTask.setResultsReceptor(this) ;			// callback 
		
		sfTask.setContinuity(0,1) ;                 // Level, number of runs; (0,1) building a stable model, then stop 
		sfTask.setStartMode(1) ;             			// default=1 == starting after producing the FluidSom
										 			//        <1 == only preparing, incl. 1 elementary run to load the data, 
		                                 			//              but not starting the modeling process
										 			//        >100  minimum delay in millis
		
		sfFactory.produce( sfTask );          		// this produces the SomFluid, refering implicitly to sfTask 
													// if not defined otherwise in sfTask, the SomFluid will be started to create a model
		
		// if we like to have graphical output, then start the applet for displaying it and 
		// define shake hands by means of GlueClients...
		
	}

	// call back event that provides access to the results of a modeling SOM
	@Override
	public void onResultsCalculated( SomFluidMonoResultsIntf results ) {
		// 
		
	}

	// call back events for providing informaton about the state of modeling
	@Override
	public void onCalculation(SomFluidStateDescriptionIntf results) {
		
		
	}
	
	 
	
}
