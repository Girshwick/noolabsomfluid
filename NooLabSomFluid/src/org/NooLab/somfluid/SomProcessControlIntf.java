package org.NooLab.somfluid;

public interface SomProcessControlIntf {

	public void interrupt(int breaklevel) ;
	
	public int getInterruptRequest() ;

	public void injectVariableSelection() ;
	
	// changing properties via reflection, call by literals onto the fields
	
}
