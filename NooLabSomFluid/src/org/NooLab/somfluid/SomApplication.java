package org.NooLab.somfluid;

import org.NooLab.somfluid.app.SomAppProperties;






/**
 * 
 * is hosting references to all required SomApp objects
 * 
 * 
 *
 */
class SomApplication implements SomApplicationIntf {

	SomFluidFactory sfFactory ; 
	SomAppProperties sfaProperties ; 
	
	
	
	
	// ========================================================================
	public SomApplication( 	SomFluidFactory factory, 
							SomAppProperties properties ){
		
		sfFactory = factory ;
		sfaProperties = properties ; 
	}
	// ========================================================================	
	
	
	
	
	
}
