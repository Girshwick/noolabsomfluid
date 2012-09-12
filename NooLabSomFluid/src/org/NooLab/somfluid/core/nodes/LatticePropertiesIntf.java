package org.NooLab.somfluid.core.nodes;

public interface LatticePropertiesIntf {

	
	
	public int getInitialNodeCount() ;
	
	public void setInitialNodeCount(int nodeCount) ;

	public int getSomGridType(); // fluid or fixed
	public void setSomGridType(int gridtype); 
	
	/** mono or prob */ 
	public int getSomType();
	
	/** mono or prob */ 
	public void setSomType( int type) ;

	public boolean isAssignatesHomogeneous();
	
	
	
	
}
