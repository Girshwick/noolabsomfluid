package org.NooLab.somfluid.properties;





public interface SomDynamicsIntf {


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

	
	
	
	
	
}
