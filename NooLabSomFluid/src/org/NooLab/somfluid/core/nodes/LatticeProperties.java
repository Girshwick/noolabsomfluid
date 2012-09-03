package org.NooLab.somfluid.core.nodes;




public class LatticeProperties implements LatticePropertiesIntf{

	int initialNodeCount = -1 ;// mandatory;
	
	/** mono or prob */
	int somType=0;
	int gridType=0;
	
	private boolean isAssignatesHomogeneous = true;
	
	@Override
	public int getInitialNodeCount() {
		
		return initialNodeCount;
	}

	@Override
	public void setInitialNodeCount(int nodeCount) {
		
		initialNodeCount = nodeCount;
	}

	@Override
	public int getSomType() {
		
		return somType;
	}

	@Override
	public void setSomType( int type) {
		 
		somType = type;
	}

	@Override
	public boolean isAssignatesHomogeneous() {

		return isAssignatesHomogeneous;
	}

	public void setAssignatesHomogeneous(boolean isAssignatesHomogeneous) {
		this.isAssignatesHomogeneous = isAssignatesHomogeneous;
	}

	@Override
	public int getSomGridType() {
		return gridType;
	}

	@Override
	public void setSomGridType(int gridtype) {
		gridType = gridtype;
	}

}
