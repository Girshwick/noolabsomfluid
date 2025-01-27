package org.NooLab.somfluid;

import org.NooLab.somfluid.clapp.SomAppProperties;




public interface SomAppFactoryClientIntf {

	// SomFluidRequestPackageIntf createRequestPackage();
	
	public SomApplicationIntf createSomApplication( SomAppProperties properties) ;
	
	public String runSomApplication();

	public <T> Object createTask(int instanceType);

	public  void produce( Object sfTask ) throws Exception ;
	
}
