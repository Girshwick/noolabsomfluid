package org.NooLab.somfluid.properties;






import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.components.DataFilter;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.engines.det.ClassesDictionary;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.data.DataSampler;
import org.NooLab.somfluid.properties.CrystalizationSettings;
import org.NooLab.somfluid.properties.OptimizerSettings;
import org.NooLab.somfluid.properties.SomBagSettings;
import org.NooLab.somfluid.properties.SpelaSettings;
import org.NooLab.somfluid.properties.SpriteSettings;
import org.NooLab.somfluid.properties.ValidationSettings;
import org.NooLab.somfluid.structures.Variables;
import org.NooLab.utilities.strings.ArrUtilities;
import org.NooLab.utilities.xml.XmlFileRead;

import com.jamesmurty.utils.XMLBuilder;





public class ModelingSettings extends SomDynamicsAbstract implements Serializable{

	private static final long serialVersionUID = -464934381782562062L;

	/**  
	 * codes for canonical exclusions of variables   
	 */
	public static final int _CANONIC_TOP    =  1;
	public static final int _CANONIC_BOTTOM = -1;
	public static final int _CANONIC_NONE   =  0;

	
	// object references ..............
	
	// 
	ClassesDictionary classesDictionary;
	

	//various setting sub-domains

	/**    */
	ClassificationSettings classifySettings ;
	
	/**    */
	SpriteSettings spriteSettings ;
	
	/**    */
	OptimizerSettings optimizerSettings ;
	
	/**    */
	SpelaSettings spelaSettings ;

	/**    */
	SomBagSettings somBagSettings;
	
	/** parameters for validation, such like sample sizes COOS validation , block sampling (important for time series)  */
	ValidationSettings validationSettings;

	
	/** e.g. parameters about controlling NVE */
	DataTransformationSettings transformationSettings ;
	
	
	
	
	// main variables / properties ....
	Variables variables;
	 
	ArrayList<String> targetVariableCandidates = new ArrayList<String> (); 
	String activeTvLabel="" ;
	String tvTargetGroupLabelColumnHeader = "" ;
	
	/** if true, then "ClassificationSettings" apply */
	boolean targetedModeling = true ; 
	
	int distanceMethod = SimilarityIntf._SIMDIST_ADVSHAPE ;
	
	double maxMissingValuePortionPerNode = 0.79 ;
	double defaultDistanceContributionBetweenMV = 0.11 ;
	
	/** whether the size of the map is adopted to 
	 *  - the number of records, 
	 *  - the number of variables 
	 *  - the task (multi class or not)
	 *  - the relative frequency of targets in data
	 *  - the quality of the model 
	 */
	boolean autoSomSizing = true ;
	
	/** whether the SOM can grow & shrink, applying adding, splitting and merging nodes */
	boolean autoSomDifferentiate = true ;
	
	boolean somCrystalization = false ;
		
	/** 
	 * the mode of growth for the SOM lattice; use the pre-defined constants;</br>
	 * the growth is automatically controlled by some relative measures on variance (intra-/inter-node,) 
	 * and size of nodes, where relative means "compared to the SOM lattice averages" 
	 */
	ArrayList<Integer> somGrowthModes = new ArrayList<Integer>(); 
	/** if not "auto",, then it will require parameters to be defined and stored to the list "somGrowthControlParams" */
	
	int somGrowthControl = _SOM_GROWTH_CTRL_AUTO;
	
	/** these parameters control the growth, they include (in this order):  ;
	 *  use -3.0 if you do not want to apply a certain dimension
	 */
	ArrayList<Double> somGrowthControlParams = new ArrayList<Double>() ;
	boolean activationOfGrowing = true ;
	 
	// sominternals ...................
	
	
	/** 
	 * values: <=0  : off; </br>
	 *           1  : only after epoch 3+, but not the last epoch step;</br>
	 *           2+ : is interpreted as a percentage value, such that [p] percent of updates are followed
	 *                by a check for split, growth; note, that this may reduce speed considerably !</br>
	 *                will be applied only in epoch 3+, but not the last epoch step;</br></br>
	 *                
	 * if  somGrowthMode changes from none to any other, this will be set to 1, if it was <0=switched off
	 */
	int intensityForRearrangements = -1 ;
	
	
	   
	int restrictionForSelectionSize = -1;

	int minimalNodeSize = 3 ;

	/** 1=single winner, 2+ multiple winners (influence degrades) */
	int winningNodesCount = 1; 
	
	// if node grow beyond that, they will be split; this could provoke bagging or growing, if allowed 
	private int absoluteRecordLimit = -1;
	// ................................
	// constants ......................

	int somType;
	
	boolean calculateAllVariables ;
	
	// feedback, diagnosis, and debug
	int confirmDataReading = -1;
	
	// dependent on user/agent-based & project-wide setting
	int actualRecordCount = 0 ; 
	
	// 1=usagevector is available and will be provided
	int useVectorModegetDedicated = 0; 
	 
	private int maxL2LoopCount = -1;
	
	private boolean contentSensitiveInfluence;
	
	

	private DataFilter dataFilter;

	private ArrayList<String> blacklistedVariablesRequest = new ArrayList<String>();
	private ArrayList<String> whitelistedVariablesRequest = new ArrayList<String>();
	
	private ArrayList<String> treatmentDesignVariablesRequest = new ArrayList<String>();
	private ArrayList<String> groupDesignVariablesRequest = new ArrayList<String>();
		
	

	private boolean isExtendedDiagnosis = false;

	private boolean determineRobustModels = true;

	private boolean checkingSamplingRobustness;

	private boolean canonicalReduction;

	private boolean performCanonicalExploration;
		
	private int canonicalReductionLimit;

	private double coverageByCasesFraction;

	
	// values <=1 -> low priority
	int[] threadPriorities = new int[10] ;
	
	 
	
	// volatile variables .............
	// only local , for speeding repeated request   .  .  .  .  .  .  .  .  .     
	ArrayList<String> variableLabels = new ArrayList<String>() ;
	private int maxSomEpochCount = 4 ;
	private boolean evolutionaryAssignateSelection = false ;
	private boolean spriteAssignateDerivation = false ;
	
	transient public ArrUtilities arrutil = new ArrUtilities();
	
	/** 
	 * the level 2 loop in modeling is concerned about the selection of features;
	 * we may use evolutionary optimization together with the derivation of additional assignates 
	 * 
	 * SpriteAssignateDerivation() 
	 * 
	 * further description are to be found here: 
	 * http://theputnamprogram.wordpress.com/2011/12/21/technical-aspects-of-modeling/      
	 */
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
	public ModelingSettings(){
		
		super();
		
		somGrowthModes.add( _SOM_GROWTH_NONE );
		
		classifySettings = new ClassificationSettings(this) ;
		
		spriteSettings = new SpriteSettings (this) ;
		optimizerSettings = new OptimizerSettings (this) ;
		spelaSettings = new SpelaSettings(this) ;
		
		somBagSettings = new SomBagSettings(this) ;
		validationSettings = new ValidationSettings(this) ;
		
		
		
		/** e.g. parameters about NVE */
		transformationSettings = new DataTransformationSettings(this)  ;
		
		
		threadPriorities[3] = 1 ; // [3] = Som calculations
	}
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
	
	/**
	 * 
	 * this loads a .mxs file, which is a xml file containing the modeling settings
	 * 
	 * 
	 */
	public void load( String workspacepath, String projectName ){
		
		XmlFileRead xmlfile ;
		
		String filename,   str ;
		
		
		
		try{
			
			
 			filename = workspacepath + projectName + ".mxs" ;
			
			xmlfile = new XmlFileRead( filename) ;
			
			str = xmlfile.getXmlTagData("variables", "blacklist", "labels") ;
			      if (str.length()>0){
			    	  
			      }
		 
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		str = "" ;
	}
	
	
	
	public void setRandomSeed(){
		
		
		random = new Random();
		random.setSeed(1234) ;
		
	}

	public void setRandomSeed(int seed){
		random = new Random();
		random.setSeed(1+Math.abs(seed)) ;
		
	}

	
	// setters / getters   . . . . . . . . . . . . . . . . . . . . . . . . . . 


	public Random getRandom() {
		return random;
	}

	 

	 
 

 
  
	  

	public int  confirmDataReading() {
		 
		return confirmDataReading;
	}

	private void updateGrowthModesIndicators(int growthModes, int i) {
		// 
		
	}

	public ClassificationSettings getClassifySettings() {
		return classifySettings;
	}

	public int getActualRecordCount() {

		return actualRecordCount;
	}

  
 
	public int UseVectorModegetDedicated() {
	 
		return useVectorModegetDedicated;
	}

 
	public boolean getAutoSomSizing() {
		 
		return autoSomSizing;
	}

 
	public void setAutoSomSizing(boolean autoMode) {
		autoSomSizing = autoMode;
		
	}


	
	public void setSomClassesDictionary(ClassesDictionary classDict) {
		classesDictionary = classDict;
	}

 
	public ClassesDictionary getSomClassesDictionary() {
		 
		return classesDictionary;
	}
	
	

	public int getThreadPriority( int targetindex){
		
		return threadPriorities[targetindex] ;
	}


 

	public boolean getTargetedModeling() {
		return targetedModeling;
	}

	public void setTargetedModeling( boolean flag) {
		this.targetedModeling = flag;
	}

	public String getActiveTvLabel() {
		
		if ((activeTvLabel==null) || (activeTvLabel.length()==0)){
			
			if ((variables!=null) && (variables.getTvColumnIndex()>=0)){
				activeTvLabel = variables.getItem(variables.getTvColumnIndex()).getLabel() ;
			}
			if ((activeTvLabel.length()==0) && (variables!=null)){
				int ix = variables.getIndexByLabel( activeTvLabel ) ;
				if (ix>=0){
					activeTvLabel = variables.getItem(ix).getLabel() ;
				}else{
					activeTvLabel = "";
				}
			}
		}
		return activeTvLabel;
	}

	public void setActiveTvLabel(String tvLabel) {
		activeTvLabel = tvLabel;
	}

	public void setTvGroupLabels(String tvColLabel){
		tvTargetGroupLabelColumnHeader = tvColLabel ;
	}
	
	public String getTvTargetGroupLabelColumnHeader() {
		return tvTargetGroupLabelColumnHeader;
	}

	public void setTvTargetGroupLabelColumnHeader(
			String tvTargetGroupLabelColumnHeader) {
		this.tvTargetGroupLabelColumnHeader = tvTargetGroupLabelColumnHeader;
	}

	public ArrayList<String> getTargetVariableCandidates() {
		return targetVariableCandidates;
	}

	public DataSampler getDataSampler() {
		return dataSampler;
	}

	public void setDataSampler(DataSampler sampler) {
		this.dataSampler = sampler;
	}

	public int getMinimalNodeSize() {
		return minimalNodeSize;
	}

	public void setMinimalNodeSize(int minimalNodeSize) {
		this.minimalNodeSize = minimalNodeSize;
	}

	public int getMinimalSplitSize() {
		return minimalSplitSize;
	}

	public void setMinimalSplitSize(int minimalSplitSize) {
		this.minimalSplitSize = minimalSplitSize;
	}

	public boolean mergeNodes(){
		return clusterMerge>0;
	}
	
	public boolean splitNodes(){
		return clusterSplit>0;
	}
	
	
	public int getClusterMerge() {
		return clusterMerge;
	}

	public void setClusterMerge(int clustermerge) {
		clusterMerge = clustermerge;
	}

	public int getClusterSplit() {
		return clusterSplit;
	}

	public void setClusterSplit(int clustersplit) {
		clusterSplit = clustersplit;
	}

	

	public int getMaxSomEpochCount() {
		return maxSomEpochCount;
	}

	public double getInitialLearningRate() {

		return 0.2;
	}

	public boolean getEvolutionaryAssignateSelection() {
		 
		return evolutionaryAssignateSelection;
	}

	public void setEvolutionaryAssignateSelection(
			boolean evolutionaryAssignateSelection) {
		this.evolutionaryAssignateSelection = evolutionaryAssignateSelection;
	}

	public boolean getSpriteAssignateDerivation() {
		 
		return spriteAssignateDerivation;
	}

	public void setSpriteAssignateDerivation(boolean spriteAssignateDerivation) {
		this.spriteAssignateDerivation = spriteAssignateDerivation;
	}

	/**  */
	public int getMaxL2LoopCount() {
		// 
		return maxL2LoopCount;
	}
	/** 
	 * the level 2 loop in modeling is concerned about the selection of "features";
	 * 
	 * @param maxL2LoopCount
	 */
	public void setMaxL2LoopCount(int maxL2loopcount) {
		maxL2LoopCount = maxL2loopcount;
	}

	public boolean isContentSensitiveInfluence() {
		
		return contentSensitiveInfluence;
	}
	public boolean getContentSensitiveInfluence() {
		
		return contentSensitiveInfluence;
	}
	public void setContentSensitiveInfluence(boolean contentSensitiveInfluence) {
		this.contentSensitiveInfluence = contentSensitiveInfluence;
	}

	
	public boolean isCalculateAllVariables() {
		return calculateAllVariables;
	}
	public boolean getCalculateAllVariables() {
		return calculateAllVariables;
	}
	public void setCalculateAllVariables(boolean flag) {
		this.calculateAllVariables = flag;
	}


	public void setRestrictionForSelectionSize(int sizevalue) {
		 
		restrictionForSelectionSize = sizevalue;
	}

	public int getRestrictionForSelectionSize() {
		return restrictionForSelectionSize;
	}

	public double getMaxMissingValuePortionPerNode() {
		return maxMissingValuePortionPerNode;
	}

	public void setMaxMissingValuePortionPerNode(
			double maxMissingValuePortionPerNode) {
		this.maxMissingValuePortionPerNode = maxMissingValuePortionPerNode;
	}

	public double getDefaultDistanceContributionBetweenMV() {
		return defaultDistanceContributionBetweenMV;
	}

	public void setDefaultDistanceContributionBetweenMV(
			double defaultDistanceContributionBetweenMV) {
		this.defaultDistanceContributionBetweenMV = defaultDistanceContributionBetweenMV;
	}

	public ClassesDictionary getClassesDictionary() {
		return classesDictionary;
	}

	public void setClassesDictionary(ClassesDictionary classesDictionary) {
		this.classesDictionary = classesDictionary;
	}

	public Variables getVariables() {
		return variables;
	}

	public void setVariables(Variables variables) {
		this.variables = variables;
	}

	public int getDistanceMethod() {
		return distanceMethod;
	}

	public void setDistanceMethod(int distanceMethod) {
		this.distanceMethod = distanceMethod;
	}

	public boolean isAutoSomDifferentiate() {
		return autoSomDifferentiate;
	}

	public void setAutoSomDifferentiate(boolean autoSomDifferentiate) {
		this.autoSomDifferentiate = autoSomDifferentiate;
	}

	
	public boolean isGrowthActive() {
	 
		return false;
	}
	
	
	public ArrayList<Integer> getSomGrowthMode() {
		return somGrowthModes;
	}

	/**
	 * use negative values to remove a particular growthmode from the list, i.e. to deactivate a particular growth mode 
	 * 
	 * @param growthModes
	 */
	@SuppressWarnings("unchecked")
	public void setSomGrowthMode(int[] growthModes) {
		// it is not a simple variable, but a bit index that indicates modes
		int gmode;
		for (int i=0;i<growthModes.length;i++){
			gmode = growthModes[i];
			updateGrowthModesIndicators( gmode,1 ) ;
		}
		
		
		// somGrowthMode reflects the most general mode, which includes modes of lower power
		// but for actually retrieving the info, a targeted query has to be called
		ArrayList<Integer> gms = ArrUtilities.changeArraystyle(growthModes); 
		somGrowthModes.addAll(gms) ;
		
		somGrowthModes = ArrUtilities.removeDoubleEntries(somGrowthModes );
		
	}
	
	@SuppressWarnings("unchecked")
	public void setSomGrowthMode(int gmode) {
		 
		somGrowthModes.add(gmode) ;
		somGrowthModes = ArrUtilities.removeDoubleEntries(somGrowthModes );
	}

	public int getSomGrowthControl() {
		return somGrowthControl;
	}

	public void setSomGrowthControl(int growthControl) {
		this.somGrowthControl = growthControl;
	}

	public ArrayList<Double> getSomGrowthControlParams() {
		return somGrowthControlParams;
	}

	public void setSomGrowthControlParams(double[] growthControlParams) {
		ArrayList<Double> controlParams ;
		
		controlParams = new ArrayList<Double>() ;
		
		for (int i=0;i<growthControlParams.length;i++){
			controlParams.add( growthControlParams[i]) ;
		}
		
		this.somGrowthControlParams = controlParams;
	}
	public void setSomGrowthControlParams(ArrayList<Double> somGrowthControlParams) {
		this.somGrowthControlParams = somGrowthControlParams;
	}
	
	
	public int getClustermerge() {
		return clusterMerge;
	}

	public void setClustermerge(int clustermerge) {
		clusterMerge = clustermerge;
	}

	public void setClustersplit(int clustersplit) {
		clusterSplit = clustersplit;
	}

	public int getIntensityForRearrangements() {
		return intensityForRearrangements;
	}

	public void setIntensityForRearrangements(int intensityForRearrangements) {
		this.intensityForRearrangements = intensityForRearrangements;
	}

	public int getConfirmDataReading() {
		return confirmDataReading;
	}

	public void setConfirmDataReading(int confirmDataReading) {
		this.confirmDataReading = confirmDataReading;
	}

	public int getUseVectorModegetDedicated() {
		return useVectorModegetDedicated;
	}

	public void setUseVectorModegetDedicated(int useVectorModegetDedicated) {
		this.useVectorModegetDedicated = useVectorModegetDedicated;
	}

	public int[] getThreadPriorities() {
		return threadPriorities;
	}

	public void setThreadPriorities(int[] threadPriorities) {
		this.threadPriorities = threadPriorities;
	}

	public ArrayList<String> getVariableLabels() {
		return variableLabels;
	}

	public void setVariableLabels(ArrayList<String> variableLabels) {
		this.variableLabels = variableLabels;
	}

	public void setClassifySettings(ClassificationSettings classifySettings) {
		this.classifySettings = classifySettings;
	}

	public void setTargetVariableCandidates(
			ArrayList<String> targetVariableCandidates) {
		this.targetVariableCandidates = targetVariableCandidates;
	}

	
	// ....................................................
	
	
	
	// ....................................................

	
	public void setActualRecordCount(int actualRecordCount) {
		this.actualRecordCount = actualRecordCount;
	}

	public void setMaxSomEpochCount(int maxSomEpochCount) {
		this.maxSomEpochCount = maxSomEpochCount;
	}

	public void setWinningNodesCount(int winnocount) {
		winningNodesCount = Math.max( 1, Math.min( 5,winnocount));
	}

	public int getWinningNodesCount() {
		return winningNodesCount;
	}

	public void setAbsoluteRecordLimit(int count) {
		absoluteRecordLimit = count ;
	}

	public int getAbsoluteRecordLimit() {
		return absoluteRecordLimit  ;
	}

	public SpriteSettings getSpriteSettings() {
		return spriteSettings;
	}

	public void setSpriteSettings(SpriteSettings spriteSettings) {
		this.spriteSettings = spriteSettings;
	}

	public ValidationSettings getValidationSettings() {
		return validationSettings;
	}

	public void setValidationSettings(ValidationSettings validationSettings) {
		this.validationSettings = validationSettings;
	}

	public OptimizerSettings getOptimizerSettings() {
		return optimizerSettings;
	}

	public void setOptimizerSettings(OptimizerSettings optimizerSettings) {
		this.optimizerSettings = optimizerSettings;
	}

	public SpelaSettings getSpelaSettings() {
		return spelaSettings;
	}

	public void setSpelaSettings(SpelaSettings spelaSettings) {
		this.spelaSettings = spelaSettings;
	}

	public SomBagSettings getSomBagSettings() {
		return somBagSettings;
	}

	public void setSomBagSettings(SomBagSettings somBagSettings) {
		this.somBagSettings = somBagSettings;
	}

	
	public void setValidationActive(boolean flag) {
		 
		this.validationSettings.setActivation(flag) ;
	}
	public boolean getValidationActive() {
		return validationSettings.getActivation();
	}
	public boolean isValidationActive() {
		return validationSettings.getActivation();
	}

	public void setValidationParameters(double[] parameters) {
		 
		validationSettings.setParameters(parameters);
	}

	public DataFilter getDataFilter() {
		 
		if (dataFilter==null){
			dataFilter = new  DataFilter(this) ;
		}
		return dataFilter;
	}

	public boolean getSomCrystalization() {
		return somCrystalization;
	}

	public CrystalizationSettings getCrystalSettings() {
		return crystalSettings;
	}

	public void setCrystalSettings(CrystalizationSettings crystalSettings) {
		this.crystalSettings = crystalSettings;
	}

	public DataTransformationSettings getTransformationSettings() {
		return transformationSettings;
	}

	public void setTransformationSettings(
			DataTransformationSettings transformationSettings) {
		this.transformationSettings = transformationSettings;
	}


	public void setDataFilter(DataFilter dataFilter) {
		this.dataFilter = dataFilter;
	}

	public void setSomCrystalization(boolean somCrystalization) {
		this.somCrystalization = somCrystalization;
	}

	public void setSomType(int somtype) {
		somType = somtype;
	}

	public int getSomType() {
		return somType;
	}

	    
	
	public void setTreatmentDesignVariablesByLabel(String[] strings) {
		ArrayList<String> items = new ArrayList<String> ( Arrays.asList(strings)) ; 
		treatmentDesignVariablesRequest.addAll(items) ;
	}
	public void setGroupDesignVariablesByLabel(String[] strings) {
		ArrayList<String> items = new ArrayList<String> ( Arrays.asList(strings)) ; 
		groupDesignVariablesRequest.addAll(items) ;
	}
	
	
	public void setRequestForWhitelistVariablesByLabel(String[] strings) {
		ArrayList<String> items = new ArrayList<String> ( Arrays.asList(strings)) ; 
		whitelistedVariablesRequest.addAll(items) ;
	}

	public ArrayList<String> getWhitelistedVariablesRequest() {
		return whitelistedVariablesRequest;
	}

	public void setWhitelistedVariablesRequest(ArrayList<String> whitelistedVariablesRequest) {
		this.whitelistedVariablesRequest = whitelistedVariablesRequest;
	}

	public ArrayList<String> getTreatmentDesignVariablesRequest() {
		return treatmentDesignVariablesRequest;
	}

	public void setTreatmentDesignVariablesRequest(ArrayList<String> treatmentDesignVariablesRequest) {
		this.treatmentDesignVariablesRequest = treatmentDesignVariablesRequest;
	}

	public ArrayList<String> getGroupDesignVariablesRequest() {
		return groupDesignVariablesRequest;
	}

	public void setGroupDesignVariablesRequest(ArrayList<String> groupDesignVariablesRequest) {
		this.groupDesignVariablesRequest = groupDesignVariablesRequest;
	}

	public void setRequestForBlacklistVariablesByLabel(String[] strings) {
		
		ArrayList<String> items = new ArrayList<String> ( Arrays.asList(strings)) ; 
		blacklistedVariablesRequest.addAll(items) ;
	}

	/**
	 * @return the blacklistedVariablesRequest
	 */
	public ArrayList<String> getBlacklistedVariablesRequest() {
		return blacklistedVariablesRequest;
	}

	/**
	 * @param blacklistedVariablesRequest the blacklistedVariablesRequest to set
	 */
	public void setBlacklistedVariablesRequest( ArrayList<String> blacklist ) {
		this.blacklistedVariablesRequest = blacklist;
	}


 

	public boolean isExtendedDiagnosis() {
		return isExtendedDiagnosis;
	}
	
	/**
	 * performs: </br>
	 * ParetoPopulationExplorer, SomModelDescription, Coarseness, MultiCrossValidation, MetricsStructure
	 *  
	 * @param flag
	 */
	public void setExtendedDiagnosis(boolean flag) {
		isExtendedDiagnosis = flag ;
	}

	public boolean isDetermineRobustModels() {
		return determineRobustModels;
	}

	public void setDetermineRobustModels(boolean flag) {
		this.determineRobustModels = flag;
	}

	public boolean isCheckingSamplingRobustness() {
		return checkingSamplingRobustness;
	}

	public void setCheckingSamplingRobustness(boolean flag) {
		this.checkingSamplingRobustness = flag;
	}

	
	
	public void setCanonicalReduction(boolean flag) {
		
		canonicalReduction = flag;
	}

	public boolean isCanonicalReduction() {
		return canonicalReduction;
	}

	public boolean getPerformCanonicalExploration() {
		return performCanonicalExploration;
	}

	public void setPerformCanonicalExploration(boolean performCanonicalExploration) {
		this.performCanonicalExploration = performCanonicalExploration;
	}

	public boolean isSearchForLinearModels() {
		//
		return false;
	}

	
	public XMLBuilder exportPropertiesAsXBuilder(SettingsTransporter settingsTransporter) {
		
		return settingsTransporter.exportPropertiesAsXBuilder(this) ;
	}

	/**
	 * 
	 * @param mode , there are defined constants in SomFluidProperties:  _CANONIC_BOTTOM, _CANONIC_TOP
	 * @param vn  removals from a given model, 
	 * @param mcn canonic loops
	 */
	public void setCanonicalExploration(int canonicBottom, int vn, int mcn) {
		// use a int[]
		
	}

	public void setCanonicalReductionLimit(int preferredMinimumSize) {
		//
		canonicalReductionLimit = preferredMinimumSize;
	}


	public boolean isActivationOfGrowing() {
		return activationOfGrowing;
	}

	public void setActivationOfGrowing(boolean activationOfGrowing) {
		this.activationOfGrowing = activationOfGrowing;
	}

	public int getCanonicalReductionLimit() {
		return canonicalReductionLimit;
	}

	/**
	 * maximum value is 0.5, if [0.5,1] it will be set to 1-x, if >1 it will be set to 0.2
	 * 
	 * if validation is requested, and the number of records/cases would be too low,
	 * this setting will be overruled by an adaptive value
	 * 
	 * @param value
	 */
	public void setCoverageByCasesFraction(double value) {
		// 
		// 
		if ((value<0) || (value>1)){
			value=0.2;
		}
		
		if ((value>0.5) && (value<=1)){
			value = 1.0-value;
		}
		coverageByCasesFraction = value;
	}

	public double getCoverageByCasesFraction() {
		return coverageByCasesFraction;
	}



	

	
	
}


class Growth implements Serializable{
	
	private static final long serialVersionUID = -2943135581423973293L;
	
	public int metatree = 0 ;    
	public int metacrosslevellinking = 0 ;      
	public int vertical = 0 ;     
	public int horizontal = 0 ;     
	public int vLevels = 1;     
	public int hMaxSize = 10000 ;     

	
	public Growth(){
		
	}


	public int getMetatree() {
		return metatree;
	}


	public void setMetatree(int metatree) {
		this.metatree = metatree;
	}


	public int getMetacrosslevellinking() {
		return metacrosslevellinking;
	}


	public void setMetacrosslevellinking(int metacrosslevellinking) {
		this.metacrosslevellinking = metacrosslevellinking;
	}


	public int getVertical() {
		return vertical;
	}


	public void setVertical(int vertical) {
		this.vertical = vertical;
	}


	public int getHorizontal() {
		return horizontal;
	}


	public void setHorizontal(int horizontal) {
		this.horizontal = horizontal;
	}


	public int getvLevels() {
		return vLevels;
	}


	public void setvLevels(int vLevels) {
		this.vLevels = vLevels;
	}


	public int gethMaxSize() {
		return hMaxSize;
	}


	public void sethMaxSize(int hMaxSize) {
		this.hMaxSize = hMaxSize;
	}
	
}

