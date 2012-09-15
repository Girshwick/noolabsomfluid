package org.NooLab.somfluid;

import org.NooLab.structures.InstanceProcessControlIntf;

class SomProcessControl implements InstanceProcessControlIntf{


	SomFluidFactory sfFactory;
	
	int interruptRequest=0;
	
	// ========================================================================
	public SomProcessControl( SomFluidFactory sf){
		sfFactory = sf;
		
	}
	// ========================================================================

	
	public void setInterruptRequest(int interruptRequest) {
		this.interruptRequest = interruptRequest;
	}
	
	@Override
	public void interrupt(int breaklevel) {
		interruptRequest++ ;
		
		// sfFactory.interrupt(); not a direct call, instead, the interface is queried by the relevant locations 
	}
 
	@Override
	public void injectVariableSelection() {
		
		
	}



	public int getInterruptRequest() {
		return interruptRequest;
	}


	@Override
	public void close(int exitcode) {
		// TODO Auto-generated method stub
		
	}



	
	
	
}
