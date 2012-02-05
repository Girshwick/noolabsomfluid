package org.NooLab.somfluid.env.communication;

import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;



public class LatticeProperties implements LatticePropertiesIntf{

	int initialNodeCount = -1 ;// mandatory;
	
	
	@Override
	public int getInitialNodeCount() {
		
		return initialNodeCount;
	}

	@Override
	public void setInitialNodeCount(int nodeCount) {
		
		initialNodeCount = nodeCount;
	}

}
