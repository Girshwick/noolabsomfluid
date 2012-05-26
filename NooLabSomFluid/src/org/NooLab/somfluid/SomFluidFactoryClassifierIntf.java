package org.NooLab.somfluid;

import org.NooLab.somfluid.app.SomAppProperties;
import org.NooLab.somfluid.app.SomApplicationEventIntf;





public interface SomFluidFactoryClassifierIntf {

	public void setMessagePort( SomApplicationEventIntf msgCallbackIntf );

	public  <T> Object createTask() ;

	public <T> Object createTask(int instanceType);
	
	
	
	void produce(Object sfTask);

	public SomApplicationIntf createSomApplication( SomAppProperties properties) ;
	
	
	
	

}
