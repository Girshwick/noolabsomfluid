package org.NooLab.somfluid.properties;




import org.NooLab.utilities.ArrUtilities;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.components.Coarseness;
import org.NooLab.somfluid.components.DataFilter;
import org.NooLab.somfluid.components.MetricsStructure;
import org.NooLab.somfluid.components.MultiCrossValidation;
import org.NooLab.somfluid.components.ParetoPopulationExplorer;
import org.NooLab.somfluid.components.SomModelDescription;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.engines.det.ClassesDictionary;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.data.DataSampler;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.properties.CrystalizationSettings;
import org.NooLab.somfluid.properties.OptimizerSettings;
import org.NooLab.somfluid.properties.SomBagSettings;
import org.NooLab.somfluid.properties.SpelaSettings;
import org.NooLab.somfluid.properties.SpriteSettings;
import org.NooLab.somfluid.properties.ValidationSettings;
import org.NooLab.utilities.xml.XmlFileRead;





public class ModelingSettings implements Serializable{

	private static final long serialVersionUID = -464934381782562062L;
	
	/** 
	 * the number of nodes remains constant, each split must be accompanied by a merge
	 */
	public static final int _SOM_GROWTH_NONE      = -1;
	
	public static final int _SOM_GROWTH_PRESELECT =  2;
	
	/** 
	 * number of nodes may change, lattice remains 2D
	 */
	public static final int _SOM_GROWTH_LATERAL   =  2;
	
	/** 
	 * nodes may outgrow into 3D, replacing the extensional list by a further SOM,
	 * that inherits the connections to the nodes neighborhood;
	 * such an offspring may separate completely later;
	 * it inherits the target variable, and the structure of the profile vector etc...
	 * actually, it represents just a sampling device, 
	*/
	public static final int _SOM_GROWTH_VERTICAL  =  3;
	/**
	 * The node may embed adaptively a SOM;
	 * such a SOM remains completely hidden  
	 */
	public static final int _SOM_GROWTH_EMBED     =  5;
	/**
	 * lateral, and local outgrowth into 3D
	 */
	public static final int _SOM_GROWTH_FULLOUT   =  7;
	/**
	 * lateral, local outgrowth into 3D, and embedding
	 */
	public static final int _SOM_GROWTH_FULL      =  9;
	
	/**
	 * applies any, based on default values;
	 */
	public static final int _SOM_GROWTH_CTRL_AUTO  = 21;
	
	// object references ..............
	
	// 
	ClassesDictionary classesDictionary;
	transient DataSampler dataSampler;

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

	/** for idealization processes */
	CrystalizationSettings crystalSettings;
	
	/** e.g. parameters about NVE */
	DataTransformationSettings transformationSettings ;
	
	
	
	// our central random object for creating random numbers
	Random random;
	
	
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
	int somGrowthMode = _SOM_GROWTH_NONE;
	/** if not "auto",, then it will require parameters to be defined and stored to the list "somGrowthControlParams" */
	
	int somGrowthControl = _SOM_GROWTH_CTRL_AUTO;
	/** these parameters control the growth, they include (in this order):  ;
	 *  use -3.0 if you do not want to apply a certain dimension
	 */
	ArrayList<Double> somGrowthControlParams = new ArrayList<Double>() ;
	boolean activationOfGrowing = true ;
	 
	// sominternals ...................
	int clusterMerge = 1 ;
	/** 
	 * respects the minimal cluster size and minimalSplitSize, whatever comes first;
	 * splitting nodes occurs only in the third pass 
	 */
	int clusterSplit = 1 ;
	/** the minimal size of nodes before they are considered to be split */
	int minimalSplitSize = 15; 
	
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
	
	
	
	Growth growth = new Growth();
	   
	int restrictionForSelectionSize = -1;

	int minimalNodeSize = 3 ;

	/** 1=single winner, 2+ multiple winners (influence degrades) */
	int winningNodesCount = 1; 
	
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
	 
	
	// values <=1 -> low priority
	int[] threadPriorities = new int[10] ;
	
	 
	
	// volatile variables .............
	// only local , for speeding repeated request   .  .  .  .  .  .  .  .  .     
	ArrayList<String> variableLabels = new ArrayList<String>() ;
	private int maxSomEpochCount = 4 ;
	private boolean evolutionaryAssignateSelection = false ;
	private boolean spriteAssignateDerivation = false ;
	
	/** 
	 * the level 2 loop in modeling is concerned about the selection of features;
	 * we may use evolutionary optimization together with the derivation of additional assignates 
	 * 
	 * SpriteAssignateDerivation() 
	 * 
	 * further description are to be found here: 
	 * http://theputnamprogram.wordpress.com/2011/12/21/technical-aspects-of-modeling/      
	 */
	private int maxL2LoopCount = -1;
	
	private boolean contentSensitiveInfluence;
	
	transient public ArrUtilities arrutil = new ArrUtilities();

	private DataFilter dataFilter;

	private ArrayList<String> blacklistedVariablesRequest = new ArrayList<String>();

	private int initialAutoVariableSelection=0 ;
	ArrayList<String> initialVariableSelection = new ArrayList<String>();

	private boolean isExtendedDiagnosis = false;

	private boolean determineRobustModels = true;

	private boolean checkingSamplingRobustness;
		

	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
	public ModelingSettings(){
		 
		classifySettings = new ClassificationSettings(this) ;
		
		spriteSettings = new SpriteSettings (this) ;
		optimizerSettings = new OptimizerSettings (this) ;
		spelaSettings = new SpelaSettings(this) ;
		
		somBagSettings = new SomBagSettings(this) ;
		validationSettings = new ValidationSettings(this) ;
		
		crystalSettings = new CrystalizationSettings(this) ;
		
		/** e.g. parameters about NVE */
		transformationSettings = new DataTransformationSettings(this)  ;
		
		setRandomSeed();
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

	private void updateGrowthModesIndicators(int growthMode, int i) {
		// TODO Auto-generated method stub
		
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
		if (activeTvLabel.length()==0){
			
			if (variables.getTvColumnIndex()>=0){
				activeTvLabel = variables.getItem(variables.getTvColumnIndex()).getLabel() ;
			}
			if ((activeTvLabel.length()==0) && (variables!=null)){
				int ix = variables.getIndexByLabel( activeTvLabel ) ;
				activeTvLabel = variables.getItem(ix).getLabel() ;
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

	public Growth getGrowth() {
		return growth;
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

	public int getSomGrowthMode() {
		return somGrowthMode;
	}

	/**
	 * use negative values to remove a particular growthmode from the list, i-e- to deactivate a particular growth mode 
	 * 
	 * @param growthMode
	 */
	public void setSomGrowthMode(int growthMode) {
		// it is not a simple variable, but a bit index that indicates modes
		updateGrowthModesIndicators( growthMode,1 ) ;
		
		// somGrowthMode reflects the most general mode, which includes modes of lower power
		// but for actually retrieving the info, a targeted query has to be called
		somGrowthMode = growthMode;
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
	// see also the local class "Growth"
	public void setGrowth(Growth growth) {
		this.growth = growth;
	}
	
	public void setActivationOfGrowing(boolean flag) {
		activationOfGrowing = flag;
	}
	public boolean isActivationOfGrowing() {
		return activationOfGrowing;
	}
	public boolean getActivationOfGrowing() {
		return activationOfGrowing;
	}
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

	public DataFilter getDataFilter(SomFluidFactory factory) {
		 
		if (dataFilter==null){
			dataFilter = new  DataFilter(factory,this) ;
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

	public DataFilter getDataFilter() {
		return dataFilter;
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

	public void setInitialAutoVariableSelection(int mode) {
		// 
		initialAutoVariableSelection = mode;
	}

	/**
	 * @return the initialAutoVariableSelection
	 */
	public int getInitialAutoVariableSelection() {
		return initialAutoVariableSelection;
	}

	public void setInitialVariableSelection(String[] strings) {
		
		initialVariableSelection = new ArrayList<String>( Arrays.asList(strings));
		
	}
	/**
	 * @return the initialVariableSelection
	 */
	public ArrayList<String> getInitialVariableSelection() {
		return initialVariableSelection;
	}

	/**
	 * @param initialVariableSelection the initialVariableSelection to set
	 */
	public void setInitialVariableSelection(
			ArrayList<String> initialVariableSelection) {
		this.initialVariableSelection = initialVariableSelection;
	}

	public void addInitialVariableSelection(String[] strings) {
		
		ArrayList<String> varitems = new ArrayList<String>( Arrays.asList(strings));
		initialVariableSelection.addAll(varitems);
	}

	public void setExpectedVariety(String varLabel, int nFreq) {
		//  
		
	}

	public boolean isExtendedDiagnosis() {
		return isExtendedDiagnosis;
	}
	/**
	 * perfrms: </br>
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

