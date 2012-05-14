package org.NooLab.somfluid.app;



/**
 * 
 * 
 * 
 *
 */
public class SomAppProperties implements SomAppPropertiesIntf{

	static SomAppProperties sclappProperties ;
	
	
	// ========================================================================
	public SomAppProperties(String sourceForProperties){
		
	}
	// ========================================================================	
	
	public static SomAppProperties getInstance(String sourceForProperties) {
		
		sclappProperties = new SomAppProperties(sourceForProperties) ;
		
		return sclappProperties;
	}

}
