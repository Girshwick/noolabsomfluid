package org.NooLab.somfluid.astor.query;

import org.NooLab.somfluid.components.SomQueryTargetIntf;






class SomQuery implements SomQueryIntf{

	SomQueryTargetIntf somlattice;
	SomQueryFactory    sqFactory;
	
	
	// ========================================================================
	public SomQuery(SomQueryFactory sqFactory, SomQueryTargetIntf somlattice){
		
		this.sqFactory = sqFactory;
		this.somlattice = somlattice;
		
	}
	// ========================================================================	


	@Override
	public void setExternalStorage() {
		// TODO Auto-generated method stub
		
	}
	
	 
	


 

}
