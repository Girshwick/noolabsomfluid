package org.NooLab.somfluid.core.nodes;

import org.NooLab.somfluid.env.communication.NodeObserverIntf;




public interface MetaNodeIntf extends NodeObserverIntf {

	public long getSerialID();
	
  
	public <T> T getInfoFromNode(Class<T> theClass, int infoID ) throws IllegalAccessException, 
																		InstantiationException  ;
	// such we can define the return the desired info that match the provided class info
	// String string = getInstance(String.class);
	
}
