package org.NooLab.somfluid;

import java.io.Serializable;



public class SomFluidRequestPackage 	implements 
													Serializable,
													SomFluidRequestPackageIntf {

	private static final long serialVersionUID = 4545423849727812646L;
	
	transient SomFluidFactory somFluidFactory;
	String commandString="" ;
	String[] parameters = new String[0] ;
	
	// ------------------------------------------------------------------------
	public SomFluidRequestPackage(SomFluidFactory sfFactory) {
		somFluidFactory = sfFactory ;
		
	}
	// ------------------------------------------------------------------------
	
	@Override
	public void setRequestConcern(String cmdStr) {
		 
		// e.g. : "Lattice::Display"
		commandString = cmdStr;
	}

	public String getCommandString() {
		return commandString;
	}

	public void setCommandString(String commandString) {
		this.commandString = commandString;
	}

	public String[] getParameters() {
		return parameters;
	}

	public void setParameters(String[] parameters) {
		this.parameters = parameters;
	}

}
