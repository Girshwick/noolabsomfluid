package applet;


import java.util.ArrayList;
import java.util.Arrays;

import processing.core.*;

import org.NooLab.somfluid.SomApplicationResults;
import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidIntf;
import org.NooLab.somfluid.SomFluidMonoResultsIntf;
import org.NooLab.somfluid.SomFluidMonoTaskIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidResultsIntf;
import org.NooLab.somfluid.SomFluidStateDescriptionIntf;
import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somfluid.app.SomAppUsageIntf;
import org.NooLab.somfluid.app.SomApplicationEventIntf;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.core.engines.det.results.ValidationSet;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.properties.ValidationSettings;
import org.NooLab.somfluid.util.PowerSetSpringSource;
import org.NooLab.utilities.callback.ProcessFeedBackContainerIntf;
import org.NooLab.utilities.logging.LogControl;





/**
 * 
 * Later: this applet should start the SomFluid as an application!!
 * Any communication sould be performed through Glue
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



public class SomFluidModuleApplet extends PApplet{

	 
	private static final long serialVersionUID = 8918471551051086099L;
	
	SomModuleInstance somInstance ;
	String sourceForProperties = "";
	
	
	public void setup(){
		 
		showKeyCommands();
		
		background( 208,188,188);
		// start this in its own thread...
		
		// use LogControl to control output of log messages
		LogControl.globeScope = 2; // 1=local definitions  default = 2
		LogControl.Level = 2 ;     // the larger the more detailed logging... (could be really verbose!)

		 
		
		somInstance = new SomModuleInstance( SomFluidFactory._INSTANCE_TYPE_SOM , 
											 SomFluidFactory._GLUE_MODULE_ENV_NONE,
											 sourceForProperties ) ;
		somInstance.startInstance() ;
		
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
		powerset.getConstraints().addMandatoryItems(new String[]{"B","E"});
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

	 
		if (key=='c'){
			somInstance.classifyData();
		}
		
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



/**
 * 
 * this object could be instantiated by the glue layer, if there is the correct information available
 * 
 */
class SomModuleInstance implements 	Runnable,
								    SomApplicationEventIntf  {
	
	
	SomFluidFactory sfFactory;
	SomFluidProperties sfProperties;
	SomFluidIntf somFluid;
	
	int instanceType = 1;
	int glueModuleMode = 0;
	String sourceForProperties = "";
	
	// initial number of nodes in the SOM lattice
	int nodeCount = 47;
	
	
	Thread smiThrd;
	
	public SomModuleInstance(int instancetype, int glueMode, String propertiesSource ){
		
		instanceType = instancetype;
		
		glueModuleMode =  glueMode ;
		
		sourceForProperties = propertiesSource;
		
		smiThrd = new Thread(this, "SomModuleInstance");
	 
	}
	 
	public void classifyData() {
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
		if (instanceType == SomFluidFactory._INSTANCE_TYPE_SOM){
			prepareSOM();
		}
		if (instanceType == SomFluidFactory._INSTANCE_TYPE_OPTIMIZER){
			prepareSomOptimizer();
		}
		if (instanceType == SomFluidFactory._INSTANCE_TYPE_SPRITE){
			prepareSomSprite();
		}
		if (instanceType == SomFluidFactory._INSTANCE_TYPE_TRANSFORM){
			prepareSomTransformer();
		}
		
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
		
		SomFluid sf;

		// this creates an XML string with all parameters and their default values
		String str = sfProperties.getDefaultExport();
		
		/*
		 * note that there are a lot more of parameters that could be set for the SOM in order to exert full control,
		 * e.g. in the context of commercial predictions;
		 * yet, there are reasonnable default values, and in the long run those parameters are 
		 * adjusted autonomously anyway by the system itself  
		 */
		
		
		
		sfProperties.setInstanceType( instanceType ) ;                     // the main role the module is exhibiting
															               // _SOM  _OPTIMIZER _SPRITE  _TRANSFORM

		// target oriented modeling 
		// lattice
		sfProperties.setSomType( SomFluidProperties._SOMTYPE_MONO ) ;      // we define to create a SOM for targeted modeling 

		sfProperties.setInitialNodeCount(nodeCount);                       // initial size; yet it does not matter much since the SomFluid will grow anyway 
		
		sfProperties.setAutoAdaptResolutionAllowed(1) ;					   // this will allow SomFluid to choose a proper size and a proper 
																		   // number of particles (resolution), dependent on the data 
		
		// data
		int srctype = SomFluidProperties._SRC_TYPE_FILE;
		
		ps = sfProperties.getPersistenceSettings();
		ps.setPathToSomFluidSystemRootDir("D:/data/projects/");
		ps.setIncomingDataSupervisionDir("");
		ps.setIncomingDataSupervisionActive(false);
		
		ps.setProjectName("bank2");
		ps.setKeepPreparedData(true); // includes persistence of transformer model
		ps.autoSaveSomFluidModels(true);
		ps.autoPackagingOfCompleteModels(true);
		
		// sfProperties.addDataSource( srctype,"D:/data/raw/simprofiles.txt");// the basic mode: data from a file; it also can receive data through ports

		// a more difficult file
		sfProperties.addDataSource( srctype,"bankn_d2.txt"); // if the persistence settings are available, the relative path will be guessed
		// sfProperties.addDataSource( srctype,"D:/data/projects/bank2/data/raw/bankn_d2.txt"); we can also provide a file from an arbitrary location
		
		sfProperties.setExtendingDataSourceEnabled(false); 				   // default=false; true for data updates via internal Glue-client or via directory supervision for online learning
		
		
		sfProperties.setSimulationMode( SomFluidProperties._SIM_NONE);     // default=_NONE; can be used to create data from (apriori) profiles, 
		 															       //                or to extend the body of data by surrogate data (random, but same distribution and same covar)
																		   // use _SIM_PROFILES if the provided data describe prototypical apriori profiles
		sfProperties.setSimulationSize(3000) ; 							   // applies only if simulation mode <> _SIM_NONE

		   																   // note that the data are not loaded at that point,
	       																   // such it is only a request for blacklisting variables
		sfProperties.getModelingSettings().setRequestForBlacklistVariablesByLabel( new String[]{"Name","KundenNr"}) ;

		sfProperties.getModelingSettings().setActiveTvLabel("*TV") ;       // the target variable; wildcarded templates like "*TV" are possible
		sfProperties.getModelingSettings().setTvGroupLabels("Label") ; 	   // optional, if available this provides the label of the column that contains the labels for the target groups, if there are any
																		   // the only effect will be a "nicer" final output
		//sfProperties.getModelingSettings().setTvLabelAuto("TV") ;          // the syllable(s) that will be used to identify the target variable as soon as data are available
																	       // allows a series of such identifiers
        sfProperties.getModelingSettings().setInitialAutoVariableSelection(0); // default=0==no; 1=by PCA, 2=by KNN	
        sfProperties.getModelingSettings().setInitialVariableSelection( new String[]{"Stammkapital","Bonitaet","Bisher","Branchenscore"});
        
		sfProperties.getModelingSettings().setTargetedModeling(true) ;     // VERY important setting, determines the fundamental mode in which the SOM will run
																		   // invokes validation, and eventually feature selection via evo + sprite
																		   // by default, mode is "_TARGETMODE_SINGLE"
		cs = sfProperties.getModelingSettings().getClassifySettings() ; 
		cs.setTargetMode(ClassificationSettings._TARGETMODE_MULTI ) ;      // requires the determination of values that define an interval for a target group
																	       // a virtual column will be created which encodes these settings (by SomTransformer)
		cs.setTargetMode(ClassificationSettings._TARGETMODE_SINGLE ) ;
		
		cs.setSingleTargetGroupDefinition( 0.1, 0.41);		               // min max of the interval [0|1][ min|max] in case of _TARGETMODE_SINGLE, ineffective if _TARGETMODE_MULTI
		
		cs.setTargetGroupDefinition( new double[]{0.28, 0.62, 1.0});	   // applies only to MULTI mode, at least 2 values are required (for 1 group interval)
		cs.setTargetGroupDefinitionAuto(true);							   //  - if [0,1] AND if _TARGETMODE_MULTI , then the target groups will be inferred from the data
																		   //  - TODO in this case, one should be able to provide a "nominal column" that indeed contains the "names"
		cs.setTargetGroupDefinitionExclusions( new double[]{0.4} );		   // these values are NOT recognized as belonging to any of the target groups, == singular dot-like holes in the intervals
		
		
		cs.setErrorCostRatioRiskPreference( 0.28 );                        // if the ECR is not met by the conditions of a node in a developed SOM, it will NOT be
		 																   // regarded as a part of the model 	
		
		
		cs.addExtendedResultRequest( ClassificationSettings._XREQ_MISCLASSIFICATIONS_FULL ) ;
		cs.addExtendedResultRequest( ClassificationSettings._XREQ_ROC_FULL) ;
		cs.addExtendedResultRequest( ClassificationSettings._XREQ_OPTIMALCUTS,3, 0.95 ) ;
		
				    													   // whichever of the this stopping criteria for the optimizer is reached first...
		sfProperties.getModelingSettings().getOptimizerSettings().setMaxStepsAbsolute( 23 );     // only for testing, or initial exploration
		sfProperties.getModelingSettings().getOptimizerSettings().setMaxAvgVariableVisits(21) ;  // required for comparing models across population regarding the variable selections 
		sfProperties.getModelingSettings().getOptimizerSettings().setDurationHours(0.8) ;        // an absolute time limit in hours 
		sfProperties.getModelingSettings().getOptimizerSettings().setAtLeastWithoutChange(300) ; // stop if there is no improvement for some time
		
		sfProperties.setDataUptakeControl(0);                              // if negative, the data won't load automatically into the SOM
		
		sfProperties.defineSomBags(50,35,6000);							   // defines bagging, based on supplied parameters:
		sfProperties.applySomBags( false) ;			   					   // p1: min records per node, p2: max number of nodes, p3: max number of records 	
		sfProperties.setAutoSomBags( false ) ;						       // if true (=default) no settings have to be explicitly defined, adaptive default values will be taken  			
		

		// we also may consider to split-bag in case of NVE : config: max number of groups target variables,
		// such to build small soms for any of the items, and combine them to a 
		// compound model
		
		sfProperties.setAbsoluteRecordLimit(-1); // 434) ;
		
		sfProperties.setMaxL2LoopCount(3) ;								   // if>1 invokes somsprite and somscreen for optimizing the feature vector
		
		sfProperties.setRestrictionForSelectionSize(678) ;				   // no more than [N] nodes will be selected; that means, if the SOM grows beyond a certain size, 
																	       // a symmetry break will occur
																		   // if the size of a SOM grows to  sqrt(mapsize) > 3.5*sqrt(n/pi)
																	       // then the coarse-som preprocessing will be organized, if it is allowed
		
		sfProperties.setGrowthMode( ModelingSettings._SOM_GROWTH_PRESELECT);// growth modes can be combined ! PRESELECT -> coarse-som preprocessing 
		sfProperties.setGrowthMode( ModelingSettings._SOM_GROWTH_LATERAL); //  
		sfProperties.removeGrowthMode( ModelingSettings._SOM_GROWTH_LATERAL); 
		sfProperties.setGrowthSizeAdaptationIntensity( 5 ) ;			   // 5=normal == default, 1=greedy -> large nodes, rather small lattice, 10=allowing sparsely filled nodes 
				
		sfProperties.setActivationOfGrowing( true );                       // activates/deactivates growing without removing the settings, default=true
		
		sfProperties.setMultipleWinners(1) ; 							   // max 5, if=1 == default = single winner
																	       // only the best winner will be actually updated by the data ;
																		   // the further winners only update their profile
																		   // in most cases, a singular winner (n=1) provides the best results
		                                                                   // more winners leads to "smearing" of information
		 
		sfProperties.setValidationActive( true );						   // whether the model should be validated, applies only if SomType = _SOMTYPE_MONO 
																		   // for most validation styles, the validation sample will be drawn before bagging
																		   // if optimizing && validation off, internal quality of SOM will be used for optimization
		                       // TODOsample size does not work ...
		sfProperties.setValidationParameters( 20, 46.3 );			       // basic parameters for validation: p1:n repeats p2+=sample sizes to keep aside for validation
																		   // also possible: style of validation
		sfProperties.setValidationSampleSizeAutoAdjust(true) ;			   // will care for the sample such that enough "cases" are present in the samples   
		
		sfProperties.addSurrogatedSimulationData( 0.21, 0.3, 1 ) ; 		   // amount of records as in fraction, amount of noise as fraction of stdev per variable
																		   // this results in a more robust model, since small differences are prevented from being overweighted
		sfProperties.surrogateAppMode( 0,1,0 ) ; 						   // global on/off, initial modeling on/off, optimizing on/off 

		
		// results
																		   // defining what should happen upon results
																		   // saving, sending, displaying, nothing
		

		sfProperties.setMessagingActive(false) ;					       // if true, the SomFluidFactory will start the glueClient ;
		   																   // the SomFluid is then accessible through messaging (see the SomController application)
		sfProperties.setglueModuleMode( glueModuleMode ) ;

		
		sfProperties.activateMultithreadedProcesses(false);

		
		sfProperties.setInitializationOK(true) ;
		
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
		
		 
		sfFactory = SomFluidFactory.get(sfProperties);			// creating the factory	
		
		  
		sfProperties.addFilter( "var",0.3,"<",1,1,true);        // filter that act on the values of observations
																// can be defined only with an existing factory since we need access to the data
																// not yet functional
		
		SomFluidMonoTaskIntf sfTask = (SomFluidMonoTaskIntf)sfFactory.createTask( ); //  
		 
 		
		sfTask.setContinuity( 1,1) ;                // param1: Level of Spela looping: 1=simple model, 2=checking 
													// param2: number of runs: (1,1) building a stable model, then stop 
													//                         (2,500) including screening S1, S2, max 500 steps in S2
													//                         (2,3,500) max 3 levels in S1
													//      				   (2,0,500) no dependency screening, just eveo optimizing
													//            
		sfTask.setStartMode(1) ;             		// default=1 == starting after producing the FluidSom
										 			//        <1 == only preparing, incl. 1 elementary run to load the data, 
		                                 			//              but not starting the modeling process (v switches in the middle between 3..100)
										 			//        >100  minimum delay in millis
		
		
		
		sfFactory.produce( sfTask );          		// this produces the SomFluid and the requested som-type accoding to
													// SomFluidProperties._SOMTYPE_MONO, refering implicitly to sfTask; 
													//
		
	
		
		sfTask = (SomFluidMonoTaskIntf)sfFactory.createTask( ); 
		sfTask.setStartMode(1) ;  
		sfTask.setContinuity(2,0,200);
		
		sfFactory.produce( sfTask );
		
		
		// if we like to have graphical output, then start the applet for displaying it and 
		// define shake hands by means of GlueClients...
		
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

