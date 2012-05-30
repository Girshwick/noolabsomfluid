package org.NooLab.somfluid;


import java.util.ArrayList;

import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.util.XmlStringHandling;
import org.NooLab.somtransform.SomFluidXMLHelper;
import org.NooLab.somtransform.algo.externals.AlgorithmPluginsLoader;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;

import com.jamesmurty.utils.XMLBuilder;



/**
 * 
 * note that there are a lot more of parameters that could be set for the SOM in order to exert full control,
 * e.g. in the context of commercial predictions;
 * yet, there are reasonable default values, and in the long run those parameters are 
 * adjusted autonomously anyway by the system itself  
	 
	 sfProperties.importTransformationParameterDefaults("standards.ini");

 * 
 */
public class SomFluidPropertiesHandler implements SomFluidPropertiesHandlerIntf{

	SomFluidProperties sfProperties;
	
	ModelingSettings ms;
	ClassificationSettings cs;
	PersistenceSettings ps;

	VariableSettingsHandlerIntf variableSettings ;
	
	String algorithmsConfigPath ="" ;
	
	private String projectName="", dataSourceName="";
	String supervisedOnlineFolder="" ;
	int nodeCount = 3;
	int srctype = 1;
	int surrogateActivated = 0;
	
	private int numberOfSimulatedRecords;

	private int targetMode;

	
	
	// ---- some helpers ----
	transient DFutils fileutil = new DFutils();
	transient StringsUtil strgutil = new StringsUtil();
	transient PrintLog out = new PrintLog(2,false);
	transient ArrUtilities arrutil = new ArrUtilities();
	transient XmlStringHandling xMsg = new XmlStringHandling() ;

	
	// ========================================================================
	public SomFluidPropertiesHandler( SomFluidProperties properties){
		sfProperties = properties;

		ms = sfProperties.getModelingSettings();
		cs = ms.getClassifySettings() ;
		
		out.setPrefix("[SomFluid-init]");
	}
	// ========================================================================
	
	@Override
	public void initializeDefaults() {
		
		setPlugins( true ); // make this dynamic
		
		sfProperties.setAutoAdaptResolutionAllowed(1) ;				// this will allow SomFluid to choose a proper size and a proper 
																	// number of particles (resolution), dependent on the data 

		 
							// this comes from the out-most application layer (user, or remote control)  
							String rootFolder = SomFluidStartup.getProjectBasePath(); // IniProperties.fluidSomProjectBasePath ;
							rootFolder = DFutils.createPath(rootFolder, "/") ;
							
		sfProperties.setPathToSomFluidSystemRootDir(rootFolder);	// within this dir all project base directories are located
										// sth like "D:/data/projects/"
		
		
		// data
		ps.setIncomingDataClassifyFirst(false);						// interesting for nested systems...
		
		ps.setProjectName( SomFluidStartup.getLastProjectName() );	// will be used also for output files
		            // sth like "bank2" , i.e. the simple name of a folder where all the project files are located 
		
		ps.setKeepPreparedData(true); 								// includes persistence of transformer model
		ps.autoSaveSomFluidModels(true);
		ps.setExportTransformModelAsEmbeddedObj(true);
		
		sfProperties.addDataSource( srctype, dataSourceName);       // if the persistence settings are available, the relative path will be used
		
							// this way we could also provide a file (or database) from an arbitrary location
							// sfProperties.addDataSource( srctype,"D:/data/projects/bank2/data/raw/bankn_d2.txt"); 
						
		sfProperties.setDataUptakeControl(0);                       // if negative, the data won't load automatically into the SOM
		
		cs.addExtendedResultRequest( ClassificationSettings._XREQ_MISCLASSIFICATIONS_FULL ) ;
		cs.addExtendedResultRequest( ClassificationSettings._XREQ_ROC_FULL) ;
		cs.addExtendedResultRequest( ClassificationSettings._XREQ_OPTIMALCUTS,3, 0.95 ) ;
		
		ms.setExtendedDiagnosis( true ) ;  							// activates some post-calculation investigations a la SPELA
																	// ParetoPopulationExplorer, SomModelDescription, Coarseness, MultiCrossValidation, MetricsStructure 

		
		sfProperties.setMaxL2LoopCount(5) ;							// if>1 invokes somsprite and somscreen for optimizing the feature vector
		
		sfProperties.setRestrictionForSelectionSize(678) ;			// no more than [N] nodes will be selected as neighborhood
					// that means, if the SOM grows beyond a certain size, a symmetry break will occur in the map ;
		   			// if the size of a SOM grows to  sqrt(mapsize) > 3.5*sqrt(n/pi)
	       			// then the coarse-som preprocessing will be organized, if it is allowed

		
		sfProperties.setGrowthMode( ModelingSettings._SOM_GROWTH_PRESELECT);// growth modes can be combined ! PRESELECT -> coarse-som preprocessing 
		sfProperties.setGrowthMode( ModelingSettings._SOM_GROWTH_LATERAL); //  
		sfProperties.removeGrowthMode( ModelingSettings._SOM_GROWTH_LATERAL); 
		sfProperties.setGrowthSizeAdaptationIntensity( 5 ) ;			   // 5=normal == default, 1=greedy -> large nodes, rather small lattice, 10=allowing sparsely filled nodes 
				
	
		
		sfProperties.addSurrogatedSimulationData( 0.21, 0.3, 1 ) ; 		   // amount of records as in fraction, amount of noise as fraction of stdev per variable
																		   // this results in a more robust model, since small differences are prevented from being overweighted
		
		sfProperties.surrogateAppMode( surrogateActivated,1,0 ) ; 						   // global on/off, initial modeling on/off, optimizing on/off 

		sfProperties.setAbsoluteRecordLimit(-1); // 435
		
		cs.setTargetMode(targetMode) ;
		
		
		sfProperties.setGlueInstanceType(1);
		sfProperties.setMessagingActive(false) ;					    // if true, the SomFluidFactory will start the glueClient ;
		   																// the SomFluid is then accessible through messaging (see the SomController application)
		sfProperties.setglueModuleMode( 0 ) ;							// 0 = off, others: act as source, receptor, or server

		// general environment

		sfProperties.activateMultithreadedProcesses(false);
		
		assimilateVariableSettings();
		
		sfProperties.setShowSomProgress( SomFluidProperties._SOMDISPLAY_PROGRESS_BASIC ) ;
	}
	
	
	@Override
	public void initializeDefaultsFromFile(String inifilename) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void setPlugins( boolean loadNow){
		
		 
		AlgorithmPluginsLoader lap ;
		
		// loading "builtinscatalog.xml" which is necessary for global indexing and activation of built-in algorithms
		sfProperties.setAlgorithmsConfigPath( algorithmsConfigPath ); 

		// here we need an absolute path, the file is needed also for advanced auto assignments of built-in algorithms, 
		// even if there are no custom algorithms
		sfProperties.getPluginSettings().setBaseFilePath(algorithmsConfigPath, "catalog.xml") ;
		// the plugin jarfiles are expected to sit in a relative sub-dir "transforms/" to this base-dir...
		
		
		String catfilename = fileutil.createpath( algorithmsConfigPath , "catalog.xml");
		if (fileutil.fileexists(catfilename)==false){
			out.printErr(1, "File <catalog.xml> not found in provided folder : \n"+
							"   "+ catfilename +"\n"+
							"...thus no plugins for transformation algorithms will be loaded!");
			loadNow=false;
		}
		String algofolder = sfProperties.getPluginSettings().getBaseFilePath();
		int n = fileutil.enumerateFiles( "", ".jar", algofolder) ;
		if ((fileutil.direxists( algofolder) ==false) || (n<=0)){
			out.printErr(1, "Catalog file has been found, yet the expected folder  \n"+
							"   "+ algofolder +"\n"+
							"does not exist, or does not contain any file.");
				loadNow=false;
		}else{
			if (sfProperties.isPluginsAllowed()){
				String plur = "s" ; if (n==1)plur="" ;
				out.print(2, n+" plugin file"+plur+" found.\n") ;
			}
		}
		
		sfProperties.setPluginsAllowed( loadNow ) ; // could be controlled form the outside in a dynamic manner

		try {
			if (sfProperties.isPluginsAllowed()){
				lap = new AlgorithmPluginsLoader(sfProperties, true);
				if (lap.isPluginsAvailable()) {
					lap.load();
				}
			}
			// feedback about how much plugins loaded ...
			
			//
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setAlgorithmsConfigPath(String pathToCatalogFolder) throws Exception {

		algorithmsConfigPath = pathToCatalogFolder ; 
		
		algorithmsConfigPath = algorithmsConfigPath.trim();
		algorithmsConfigPath = strgutil.replaceAll(algorithmsConfigPath, "\\", "/");
		algorithmsConfigPath = strgutil.replaceAll(algorithmsConfigPath, "//", "/");
		
		if (fileutil.direxists( algorithmsConfigPath)==false){
			throw(new Exception("requested path does not exist : "+pathToCatalogFolder));
		}
		if (algorithmsConfigPath.endsWith("/")==false){
			algorithmsConfigPath = algorithmsConfigPath+"/"; 
		}
	}

	@Override
	public void setInstance(String description, int ...nodecount ) {
	 
		description = description.trim().toLowerCase();
		
		
		if (description.startsWith("som")){
			sfProperties.setInstanceType( SomFluidFactory._INSTANCE_TYPE_OPTIMIZER ) ;   
				// the main role the module is exhibiting, MANDATORY !!!
				// _SOM  _OPTIMIZER _TRANSFORM

				// target oriented modeling lattice
			sfProperties.setSomType( SomFluidProperties._SOMTYPE_MONO ) ;   // we define to create a SOM for targeted modeling, target group settings must be supplied 

			ms.setTargetedModeling(true) ;     								// VERY important setting, determines the fundamental mode in which the SOM will run
			   																// invokes validation, and eventually feature selection via evo + sprite
			   																// by default, mode is "_TARGETMODE_SINGLE"
			
			if ((nodecount!=null) && (nodecount.length>0)){
				nodeCount = nodecount[0] ;
			}else{
				nodeCount = 100; 
				
			}
			sfProperties.setInitialNodeCount(nodeCount);                    // initial size; yet it does not matter much since the SomFluid could grow anyway
			
			sfProperties.setMultipleWinners(1) ; 							   // max 5, if=1 == default = single winner
		       // only the best winner will be actually updated by the data ;
			   // the further winners only update their profile
			   // in most cases, a singular winner (n=1) provides the best results
            // more winners leads to "smearing" of information

		} // som for targeted modeling
		
		
		if (description.startsWith("map")){
			sfProperties.setSomType( SomFluidProperties._SOMTYPE_PROB ) ;
			
			if ((nodecount!=null) && (nodecount.length>0)){
				nodeCount = nodecount[0] ;	
			}else{
				nodeCount = 1000; 
			}
			sfProperties.setInitialNodeCount(nodeCount);
		} // som map as associative storage
		
		 
		ps = sfProperties.getPersistenceSettings() ;
	}

	@Override
	public void setSomTargetMode(int targetmode) {
		
		/*
		 *   _TARGETMODE_MULTI  	requires the determination of values that define an interval for a target group
		 							a virtual column will be created which encodes these settings (by SomTransformer)
		 							
		 	 _TARGETMODE_SINGLE 	standard modeling, target groups can be translated to a binary flag: is target=preferred outcome, or not 
		*/
		
		cs.setTargetMode(targetMode) ;
		targetMode = targetmode;
	}

	
	@Override
	public void setDataSourcing(String srcDescription, int activateOnlineMode) {
		
		srcDescription = srcDescription.trim().toLowerCase();
		
		
		if (srcDescription.startsWith("file")){
		
			srctype = SomFluidProperties._SRC_TYPE_FILE;			// alternatives: db (not realized), online learning on continuous stream (not realized)
			// SomFluidProperties._SRC_TYPE_XONLINE
			
			if (activateOnlineMode>0){
				String rootFolder = SomFluidStartup.getProjectBasePath(); // IniProperties.fluidSomProjectBasePath ;
				if (fileutil.direxists(rootFolder)==false){
					// create tmp
				}
				if (fileutil.direxists(rootFolder) ){
					String prjname = SomFluidStartup.getLastProjectName();
					supervisedOnlineFolder = fileutil.createpath(rootFolder, "online/incoming") ;
					ps.setIncomingDataSupervisionDir(supervisedOnlineFolder);
				}
				ps.setIncomingDataSupervisionActive(true);
				
				
			} // activateOnlineMode>0 ?
			else {
				ps.setIncomingDataSupervisionActive(false);
			}
			
		} // "file" ?

		
		
		
	}

	@Override
	public void setDataSourceName(String srcName) {
		
		dataSourceName = srcName;
	}
	

	@Override
	public void setSupervisedDirectory(String folder) throws Exception {
		
		if (srctype != SomFluidProperties._SRC_TYPE_FILE ){
			ps.setIncomingDataSupervisionActive(false);
			throw(new Exception("Supervising folders for online learning mode is possible only if typw of source is FILE !"));
		}
		
		ps.setIncomingDataSupervisionDir(folder);
		sfProperties.setExtendingDataSourceEnabled(false); 				   // default=false; true for data updates via internal Glue-client or via directory supervision for online learning
		

	}


	@Override
	public String getSupervisedDirectory() {
		return ps.getIncomingDataSupervisionDir();
	}

	@Override
	public void setDataSimulationByPrototypes(int numberOfRecords) {
		
		numberOfSimulatedRecords = numberOfRecords;
		
		if (numberOfSimulatedRecords>100){
			sfProperties.setSimulationSize(3000) ; 							   // applies only if simulation mode <> _SIM_NONE
			sfProperties.setSimulationMode( SomFluidProperties._SIM_PROFILES );  
		}else{
			sfProperties.setSimulationMode( SomFluidProperties._SIM_NONE);     
			   // default=_NONE; can be used to create data from (apriori) profiles, 
		       //                or to extend the body of data by surrogate data (random, but same distribution and same covar)
			   // use _SIM_PROFILES if the provided data describe prototypical apriori profiles
			
		}

	}

	@Override
	public void setMaxNumberOfPrototypes(int n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMethodforInitialVariableSelection(int mode) {
	
		ms.setInitialAutoVariableSelection(mode);
	}

	
	@Override
	public VariableSettingsHandlerIntf getVariableSettingsHandler() {

		if (variableSettings==null){
			variableSettings = new VariableSettingsHandler();
		}
		return variableSettings ;
	}

	@Override
	public void setSingleTargetDefinition( String level, double criterionLowerLimit, double criterionUpperLimit, String label) {
	 
		int defLevel = 0;
		
		level = level.trim().toLowerCase() ;
		if (level.length()==0)level="raw";
		
		if (level.startsWith("raw")){
			defLevel = ClassificationSettings._TARGET_DEFLEVEL_RAW;
		}else{
			defLevel = ClassificationSettings._TARGET_DEFLEVEL_NORM ;
		}
		cs.setTargetGroupDefinitionLevel(defLevel) ;
		
		cs.setSingleTargetGroupDefinition( 0.1, 0.41,"intermediate");	// min max of the interval [0|1][ min|max] in case of _TARGETMODE_SINGLE, ineffective if _TARGETMODE_MULTI
		// the label is helpful for selecting models

		/*
		 * the "outcome" or target can be mapped onto a binary variable
		 */
		setSomTargetMode(ClassificationSettings._TARGETMODE_SINGLE ) ; 
		// alternatively: ClassificationSettings._TARGETMODE_MULTI : simultaneous multi-class modeling
		// the difference concerns the interpretation of the individual nodes

	}

	@Override
	public void preferSmallerModels(boolean flag, int preferredMinimumSize) {

		ms.setCanonicalReduction(true);	   								   // not implemented yet
		ms.setCanonicalReductionLimit(preferredMinimumSize) ;
		ms.setCanonicalExploration( ModelingSettings._CANONIC_BOTTOM,1,3); // n,m: n=removals from a given model, m=canonic loops
		ms.setCanonicalExploration( ModelingSettings._CANONIC_TOP ,1, 3);  // top=exclude best predictors, bottom=exclude worst predictors


	}
	
	
	@Override
	public void setOptimizerStoppingCriteria( int absoluteStepCount, double ...stoppingConstraints) {

		int explorationDepth  = -3 ; 
		int explorationLength = -3 ; // -1 = not set, -3 = auto, as soon as data are known
		double timeConstraint = -3 ; // 
		  
		
		if (stoppingConstraints!=null){
			
			
		} // stoppingConstraints = null ?
		
		
		   // whichever of these 4 stopping criteria for the optimizer is reached first...
		ms.getOptimizerSettings().setMaxStepsAbsolute( absoluteStepCount );	// low only for testing, or initial exploration, typically 500+
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -       
        				// note that this step-count applies WITHIN a step on L2 as 
		   				// controlled by "setMaxL2LoopCount" (see below, typically max=4)

		ms.getOptimizerSettings().setMaxAvgVariableVisits(21) ;  			// required for comparing models across population regarding the variable selections 
		ms.getOptimizerSettings().setDurationHours(0.8) ;        			// an absolute time limit in hours 
		ms.getOptimizerSettings().setStepsAtLeastWithoutChange(100) ; 		// stop if there is no improvement for some time



		ms.getOptimizerSettings().setBalancedEvolutionaryExploration(true) ;


	}

	public void setAutoBagging() {
	
		sfProperties.setAutoSomBags( false ) ; 	// if true (=default) no settings have to be explicitly defined, 
												// adaptive default values will be taken		
	}
	
	@Override
	public void setBagging(int maxNodeCount, int recordsPerNode, int... maxRecordCount) {
		
		int maxRecords = -1;
		boolean apply = true;
		maxNodeCount=0;
		recordsPerNode=0;
		// defines bagging, based on provided parameters
		sfProperties.defineSomBags( maxNodeCount, recordsPerNode, maxRecords); 			
		
		apply = false;
		
		if ((maxNodeCount<=1) || (recordsPerNode<=8)){
			apply=false;
		}
		sfProperties.applySomBags( apply ) ;		// p1: min records per node, p2: max number of nodes, p3: max number of records 	
		
		boolean hb =sfProperties.getModelingSettings().getSomBagSettings().isApplySomBags() ;
		 
			 
		 
		// we also may consider to split-bag in case of NVE : config: max number of groups target variables,
		// such to build small soms for any of the items, and combine them to a 
		// compound model
		

	}

	@Override
	public void activateGrowingOfSom(boolean flag, int maxNodeSize, double avgQuantile) {


		sfProperties.setActivationOfGrowing( flag );                       // activates/deactivates growing without removing the settings, default=true
	}

	@Override
	public void setBooleanAdaptiveSampling(boolean b) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * returns the effective result folder
	 */
	@Override
	public String setResultsPersistence(int i) {
		
		String path;
		boolean createOutFiles = i>=1;
		
		
		sfProperties.getOutputSettings().setWriteResultFiles(createOutFiles);
		
		path = sfProperties.getSystemRootDir() ;
		path = ps.getPathToSomFluidSystemRootDir();
		path = fileutil.createpath(path, ps.getProjectName()) ; // bank2\export\results
		path = fileutil.createpath(path, "export/results/") ;
		sfProperties.getOutputSettings().setResultfileOutputPath(path) ;     
				// there is a default ! ...which is based on
				// sfProperties.setPathToSomFluidSystemRootDir("D:/data/projects/");
				// and : PersistenceSettings.setProjectName("bank2");
		
		sfProperties.getOutputSettings().setResultFilenames( null); // there are defaults, but the user might set his own preferences
		
		sfProperties.getOutputSettings().setAsXml(false);           // default = false
		sfProperties.getOutputSettings().setHtmlCompatible(true) ;  // if as xml, we then will use html compatible tags
		
		sfProperties.getOutputSettings().createZipPackageFromResults(true);// default = true
		
		sfProperties.getOutputSettings().exportApplicationModel(false);    // controls exporting the file package that is necessary for applying the data to new data
		sfProperties.getOutputSettings().setIncludeDataToExportedPackages(true);  // some parameters for that export...
		sfProperties.getOutputSettings().setIncludeResultsToExportedPackages(true);
		sfProperties.getOutputSettings().setZippedExportedPackages(true);
		
		// sfProperties.sampleVariableContributionsThruTopModels( int n_models = 10 )
		// defining what should happen upon results: saving, sending, displaying, nothing

		return  sfProperties.getOutputSettings().getResultfileOutputPath();
	}
	
	@Override
	public String getResultBaseFolder() {
		return sfProperties.getOutputSettings().getResultfileOutputPath();
	}

	public void assimilateVariableSettings(){
		assimilateVariableSettings(variableSettings);
	}
	
	public void assimilateVariableSettings( VariableSettingsHandlerIntf varsettings ) {
		
		variableSettings = varsettings;
		 
		if (variableSettings==null){
			return;
		}
		
		String[] strings ;
		
		strings = (String[]) arrutil.changeArrayStyle( variableSettings.getInitialSelection());
		ms.setInitialVariableSelection( strings );
		
		strings = (String[]) arrutil.changeArrayStyle( variableSettings.getBlackListedVariables());
		ms.setRequestForBlacklistVariablesByLabel( strings );

		strings = (String[]) arrutil.changeArrayStyle( variableSettings.getAbsoluteExclusions());
		sfProperties.setAbsoluteFieldExclusions( strings , 1);

		ms.setActiveTvLabel( variableSettings.getTargetVariable() );// "*TV") ;       							
		ms.setTvGroupLabels("Label") ; 
		
	}
	
	@Override
	public void exportVariableSettings( VariableSettingsHandlerIntf variablesettings, String xfilename) {
		
		if (variablesettings!=null){
			variableSettings = variablesettings;
		}
		
		if (variableSettings==null){
			return;
		}
		// check if actually used/filled...
		
		ArrayList<String> strList;
		SomFluidXMLHelper xEngine = new SomFluidXMLHelper();
		
		XMLBuilder  builder = xEngine.getXmlBuilder( "properties" ).a("target", "somfluid").a("section","variables");
		
		String xstr, str;
		 
		try{
			
			
			// ............................................
			builder = builder.e("general")
								.e("date").a("value", "").up()
								.e("project").a("name", "").up();
			
			builder = builder.up();
			// ............................................
			
			builder = builder.e("variables");
				strList = variableSettings.getInitialSelection();
				str = xEngine.digestStringList( strList ) ;
			
				builder = builder.e("initial").a("list", str);
				
				strList = variableSettings.getBlackListedVariables();
				str = xEngine.digestStringList( strList ) ;
			
				builder = builder.e("blacklist").a("list", str);
				
				strList = variableSettings.getAbsoluteExclusions() ;
				str = xEngine.digestStringList( strList ) ;
				int mode = variableSettings.getAbsoluteExclusionsMode() ;
				
				builder = builder.e("excluded").a("list", str).a("mode", ""+mode);
				

			builder = builder.up();
			
			// ............................................

			
			
			// ............................................

			
			
			// ............................................

			
			
			// ............................................

			
			
			// ............................................
			xstr = xEngine.getXmlStr(builder, false);
		
			fileutil.writeFileSimple( xfilename, xstr);
			
			
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	@Override
	public boolean loadVariableSettingsFromFile(String  filename) {

		boolean loadingOk=false;
		String str, rawXmlStr="";
		ModelingSettings ms = sfProperties.getModelingSettings();

		
		
		
		if (rawXmlStr.length()==0){
			return false;
		}
		
		xMsg.clear() ;
		xMsg.setContentRoot( "properties") ;
		
		str = xMsg.getSpecifiedInfo(rawXmlStr, "//properties", "target") ; // = "somfluid" ?
		str = xMsg.getSpecifiedInfo(rawXmlStr, "//properties", "section") ;// = "variables" ?
		
		
		
		 
		// note that the data are not loaded at that point,
		// such it is only a request for blacklisting variables
		// if any those variables do not exist, no error message will appear by default
		
		// we provide a small interface for dealing with initial variable settings all at once
		VariableSettingsHandlerIntf variableSettings = getVariableSettingsHandler();

		variableSettings.setInitialSelection( new String[]{"Stammkapital","Bonitaet","Bisher","Branchenscore"});
		variableSettings.setBlackListedVariables( new String[]{"Name","KundenNr"} ) ;
		variableSettings.setAbsoluteExclusions( new String[]{"Name","KundenNr","Land","Region"} , 1);

		variableSettings.setTargetVariables("*TV"); 		// of course only if instance = "som" (or transformer! for certain transformations)
														// if wildcard is used, the first one found is used as active, unless defined otherwise
														// by setActiveTargetVariable("")
		
		
		sfProperties.getModelingSettings().setRequestForBlacklistVariablesByLabel( new String[]{"Name","KundenNr"}) ;

		
																		  // these variables are excluded once and for all -> they won't get transformed either
																		  // if mode 1+ then they even won't get imported
		sfProperties.setAbsoluteFieldExclusions( new String[]{"Name","KundenNr","Land","Region"} , 1);

		ms.setActiveTvLabel("*TV") ;       								// the target variable; wildcarded templates like "*TV" are possible
		ms.setTvGroupLabels("Label") ; 	   								// optional, if available this provides the label of the column that contains the labels for the target groups, if there are any
																		// the only effect will be a "nicer" final output
		//sfProperties.getModelingSettings().setTvLabelAuto("TV") ; 	// the syllable(s) that will be used to identify the target variable as soon as data are available
																	    // allows a series of such identifiers
	 	
		ms.setInitialVariableSelection( new String[]{"Stammkapital","Bonitaet","Bisher","Branchenscore"});
        
		loadingOk = true;
		
		
		
		return loadingOk;
	}
	

	

	
	
	
	
	
	
	
	
	
	
	
	
}
