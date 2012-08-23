package org.NooLab.somfluid.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.NooLab.somfluid.data.DataSampler;

public class SomDynamicsAbstract {

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

	Growth growth ;
	
	/** for idealization processes */
	CrystalizationSettings crystalSettings;

	
	
	int intensityForRearrangements = -1 ;
	
	

	// sominternals ...................
	int clusterMerge = 1 ;
	/** 
	 * respects the minimal cluster size and minimalSplitSize, whatever comes first;
	 * splitting nodes occurs only in the third pass 
	 */
	int clusterSplit = 1 ;
	/** the minimal size of nodes before they are considered to be split */
	int minimalSplitSize = 15; 
	
	
	private boolean activationOfGrowing;
	private boolean activationOfFolding;

	protected int initialAutoVariableSelection=0 ;
	ArrayList<String> initialVariableSelection = new ArrayList<String>();


	// our central random object for creating random numbers
	transient Random random; // TODO: re-init on loading the settings from xml! 

	transient DataSampler dataSampler;
	
	
	
	// ========================================================================
	public SomDynamicsAbstract(){
		
		crystalSettings = new CrystalizationSettings() ;
		random = new Random();
		random.setSeed(11992288);
		
		growth = new Growth();
	}
	// ========================================================================
	
	

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
		addInitialVariableSelection(varitems);
	}
	public void addInitialVariableSelection(ArrayList<String> varitems) {
		
		if ((varitems!=null) && (varitems.size()>0)){
			initialVariableSelection.addAll(varitems);
		}
	}

	
	// see also the local class "Growth"
	public void setGrowth(Growth growth) {
		this.growth = growth;
	}
	
	public Growth getGrowth() {
		return growth;
	}

	
	public void setActivationOfFolding(boolean flag) {
		activationOfFolding = flag;
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


	public CrystalizationSettings getCrystalSettings() {
		return crystalSettings;
	}


	public void setCrystalSettings(CrystalizationSettings crystalSettings) {
		this.crystalSettings = crystalSettings;
	}


	public int getIntensityForRearrangements() {
		return intensityForRearrangements;
	}


	public void setIntensityForRearrangements(int intensityForRearrangements) {
		this.intensityForRearrangements = intensityForRearrangements;
	}


	public int getClusterMerge() {
		return clusterMerge;
	}


	public void setClusterMerge(int clusterMerge) {
		this.clusterMerge = clusterMerge;
	}


	public int getClusterSplit() {
		return clusterSplit;
	}


	public void setClusterSplit(int clusterSplit) {
		this.clusterSplit = clusterSplit;
	}


	public int getMinimalSplitSize() {
		return minimalSplitSize;
	}


	public void setMinimalSplitSize(int minimalSplitSize) {
		this.minimalSplitSize = minimalSplitSize;
	}


	public boolean isActivationOfFolding() {
		return activationOfFolding;
	}

}


/*


*/