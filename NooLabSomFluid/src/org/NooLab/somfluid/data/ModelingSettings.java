package org.NooLab.somfluid.data;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;



import org.NooLab.somfluid.core.engines.det.ClassesDictionary;
import org.NooLab.utilities.xml.XmlFileRead;




public class ModelingSettings {

	// object references ..............
	
	// 
	ClassesDictionary classesDictionary;
	DataSampler dataSampler;
	
	// our central random object for creating random numbers
	Random random;
	
	
	// main variables / properties ....
	Variables variables;
	 
	ArrayList<String> targetVariableCandidates = new ArrayList<String> (); 
	String activeTvLabel="" ;
	
	/** 0=not set, 1=TV has been set , -3 == not possible, disabled */
	int targetedModeling = 0 ;
	
	
	int mapwidth ;	
	
	boolean content_sensitive_influence ; 
	
	// sominternals ...................
	int clustermerge = 1 ;
	int clustersplit = 1 ;
	
	Growth growth = new Growth();
	   
	
	// ................................
	// constants ......................


	int minimalNodeSize = 3 ;

	boolean calc_all_variables ;
	
	// feedback, diagnosis, and debug
	int confirmDataReading = -1;
	
	// dependent on user/agent-based & project-wide setting
	int actualRecordCount = 0 ; 
	
	// 1=usagevector is available and will be provided
	int useVectorModegetDedicated = 0; 
	
	boolean autoSomSizing = false ;
	
	// values <=1 -> low priority
	int[] threadPriorities = new int[10] ;
	
	
	
	
	// volatile variables .............
	// only local , for speeding repeated request   .  .  .  .  .  .  .  .  .     
	Vector<String> variableLabels = new Vector<String>() ;
	
	 
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
	public ModelingSettings(){
		
		
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
		
		String filename, path, str ;
		
		
		
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

	 

	 
	public int getMapSideLength() {
	
		return mapwidth;
	}
	
	public void setMapSideLength( int sidelength ) {
		
		mapwidth = sidelength;
	}
	

	
	public boolean content_sensitive_influence() {
		 
		return content_sensitive_influence;
	}

  
	  

	public int  confirmDataReading() {
		 
		return confirmDataReading;
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


	public boolean calc_all_variables() {
		 
		return calc_all_variables;
	}
	
	public void setcalculationtoall() {
		
		calc_all_variables = true;
	}

	public int getTargetedModeling() {
		return targetedModeling;
	}

	public void setTargetedModeling(int targetedModeling) {
		this.targetedModeling = targetedModeling;
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

	public void setDataSampler(DataSampler dataSampler) {
		this.dataSampler = dataSampler;
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

