package org.NooLab.somfluid.core.nodes;

public abstract class ClusterNodeAbs extends BasicNodeAbs{

	long serialID;
	
	
	//
	public ClusterNodeAbs(long serialid){
		super();
		
		serialID = serialid;
		
		super.initializeStructures(serialID);
	}
	
	
	
}
