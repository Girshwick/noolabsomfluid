package org.NooLab.somfluid;

import org.NooLab.somfluid.clapp.SomAppProperties;
import org.NooLab.somfluid.clapp.SomApplicationEventIntf;





public interface SomFluidFactoryClassifierIntf {

	public void setMessagePort( SomApplicationEventIntf msgCallbackIntf );

	public  <T> Object createTask() ;

	public <T> Object createTask(int instanceType);
	
	
	
	void produce(Object sfTask)  throws Exception ;

	public SomApplicationIntf createSomApplication( SomAppProperties properties) ;
	
	
	
	

}
