package org.NooLab.somfluid;

import org.NooLab.somfluid.app.SomAppClassifier;
import org.NooLab.somfluid.app.SomAppModelLoader;
import org.NooLab.somfluid.app.SomAppProperties;
import org.NooLab.somfluid.app.SomAppTransformer;
import org.NooLab.somfluid.app.SomModelCatalog;
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
		
		// loading the som model
		soappLoader.loadModel();
		
		// in this way we access it 
		// SomAppClassifier  soc = soappLoader.getSoappClassifier() ;
		// SomAppTransformer sot = soappLoader.getSoappTransformer() ;
		
		// for maintaining meta-description
		// SomModelCatalog smc = soappLoader.getSoappModelCatalog() ; // :)
		

		
	}
	 


	
	public void perform() {
		out.print(2,"performing classification") ;
		
		// reading the data source, into SomDataObject 
		// if not present, we do nothing (waiting just for the incoming data)
		
		// this reading we have to do tolerant against shifts of raster: possibly we have to rearrange it
		
	}


	
}
