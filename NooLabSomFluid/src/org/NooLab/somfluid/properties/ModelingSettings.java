package org.NooLab.somfluid.properties;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;



import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.engines.det.ClassesDictionary;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.data.DataSampler;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.utilities.xml.XmlFileRead;




public class ModelingSettings {

	/** 
	 * the number of nodes remains constant, each split must be accompanied by a merge
	 */
	public static final int _SOM_GROWTH_NONE     = -1;
	/** 
	 * number of nodes may change, lattice remains 2D
	 */
	public static final int _SOM_GROWTH_LATERAL  =  1;
	/** 
	 * nodes may outgrow into 3D, replacing the extensional list by a further SOM,
	 * that inherits the connections to the nodes neighborhood;
	 * such an offspring may separate completely later;
	 * it inherits the target variable, and the structure of the profile vector etc...
	 * actually, it represents just a sampling device, 
	*/
	public static final int _SOM_GROWTH_VERTICAL =  3;
	/**
	 * The node may embed adaptively a SOM;
	 * such a SOM remains completely hidden  
	 */
	public static final int _SOM_GROWTH_EMBED    =  5;
	/**
	 * lateral, and local outgrowth into 3D
	 */
	public static final int _SOM_GROWTH_FULLOUT  =  7;
	/**
	 * lateral, local outgrowth into 3D, and embedding
	 */
	public static final int _SOM_GROWTH_FULL     =  9;
	
	
	// object references ..............
	
	// 
	ClassesDictionary classesDictionary;
	DataSampler dataSampler;
	
	ClassificationSettings classifySettings ;
	
	// our central random object for creating random numbers
	Random random;
	
	
	// main variables / properties ....
	Variables variables;
	 
	ArrayList<String> targetVariableCandidates = new ArrayList<String> (); 
	String activeTvLabel="" ;
	
	/** idf true, then "ClassificationSettings" apply */
	boolean targetedModeling = true ; 
	
	int distanceMethod = SimilarityIntf._SIMDIST_ADVSHAPE ;
	
	/** whether the size of the map is adopted to 
	 *  - the number of records, 
	 *  - the number of variables 
	 *  - the task (multi class or not)
	 *  - the relative frequency of targets in data 
	 */
	boolean autoSomSizing = true ;
	
	/** whether the SOM can grow & shrink, applying adding, splitting and merging nodes */
	boolean autoSomDifferentiate = true ;
		
	int somGrowthMode = _SOM_GROWTH_LATERAL;
	 
	
	// sominternals ...................
	int clustermerge = 1 ;
	int clustersplit = 1 ;
	
	Growth growth = new Growth();
	   
	
	// ................................
	// constants ......................


	int minimalNodeSize = 3 ;

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
	Vector<String> variableLabels = new Vector<String>() ;
	private int maxSomEpochCount = 4 ;
	private boolean evolutionaryAssignateSelection = false ;
	private boolean spriteAssignateDerivation = false ;
	
	private int maxL2LoopCount=-1;
	
	private boolean contentSensitiveInfluence;
	
	 
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
	public ModelingSettings(){
		 
		classifySettings = new ClassificationSettings() ;
		
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
		return activeTvLabel;
	}

	public void setActiveTvLabel(String activeTvLabel) {
		this.activeTvLabel = activeTvLabel;
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

	public boolean mergeNodes(){
		return clustermerge>0;
	}
	
	public boolean splitNodes(){
		return clustersplit>0;
	}
	
	
	public int getClusterMerge() {
		return clustermerge;
	}

	public void setClusterMerge(int clustermerge) {
		this.clustermerge = clustermerge;
	}

	public int getClusterSplit() {
		return clustersplit;
	}

	public void setClusterSplit(int clustersplit) {
		this.clustersplit = clustersplit;
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

	public int getMaxL2LoopCount() {
		// TODO Auto-generated method stub
		return maxL2LoopCount;
	}

	public void setMaxL2LoopCount(int maxL2LoopCount) {
		this.maxL2LoopCount = maxL2LoopCount;
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
	
	
}


class Growth{
	
	public int metatree = 0 ;    
	public int metacrosslevellinking = 0 ;      
	public int vertical = 0 ;     
	public int horizontal = 0 ;     
	public int vLevels = 1;     
	public int hMaxSize = 10000 ;     

	
	public Growth(){
		
	}
	
}

