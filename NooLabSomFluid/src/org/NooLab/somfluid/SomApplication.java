package org.NooLab.somfluid;

import org.NooLab.somfluid.app.SomAppClassifier;
import org.NooLab.somfluid.app.SomAppModelLoader;
import org.NooLab.somfluid.app.SomAppProperties;
import org.NooLab.utilities.logging.PrintLog;

 


/**
 * 
 * is hosting references to all required SomApp objects
 * 
 * 
 * TODO: 
 *        - provide possibility for collecting feed back 
 * 
 *
 */
class SomApplication implements SomApplicationIntf {

	SomFluidFactory sfFactory ; 
	SomFluid somFluid;
	SomAppProperties soappProperties ; 
	SomFluidTask sfTask;
	
	SomAppModelLoader soappLoader;
	
	transient PrintLog out;
	
	// ========================================================================
	public SomApplication( 	SomFluid somfluid, SomFluidTask task, SomFluidFactory factory ){
		
		somFluid = somfluid;
		sfFactory = factory ;
		sfTask = task;
		
		soappProperties = sfFactory.getSomAppProperties() ;
		
		out = sfFactory.out ;
	}
	// ========================================================================	




	public SomApplication( SomFluidFactory factory, SomAppProperties properties) {
		
		sfFactory = factory ;
		somFluid = sfFactory.somFluidModule ; 
		
		soappProperties = sfFactory.getSomAppProperties() ;
		out = sfFactory.out ;
	}




	public void loadModel() {
		
		out.print(2,"loading model...") ;
		
		soappLoader = new SomAppModelLoader(this,soappProperties);
		
		soappLoader.loadModel();
		
		SomAppClassifier soc = soappLoader.getSoappClassifier() ;
		soappLoader.getSoappTransformer() ;
		
		soappLoader.getSoappModelCatalog() ; // :)
		

		
	}
	 


	
	public void perform() {
		out.print(2,"performing classification") ;
		
		
		
	}


	
}
