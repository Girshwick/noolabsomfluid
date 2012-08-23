package org.NooLab.utilities.resources;

public class ResourceContent {

	// ========================================================================
	public ResourceContent(){
		
	}
	// ========================================================================
	
	public static String getConfigResource( Class clzz, String cfgResourceJarPath, String resourceName ) throws Exception{
		
		String resourcePath = cfgResourceJarPath + resourceName;
		
		String xmlstr = "" ;
		boolean rB;
		
		ResourceLoader rsrcLoader = new ResourceLoader();   
		// rB = rsrcLoader.loadTextResource( this.getClass(), resourcePath  ) ;
		rB = rsrcLoader.loadTextResource( clzz, resourcePath  ) ;
		if (rB){  
			xmlstr = rsrcLoader.getTextResource();
			
		}else{
			throw(new Exception("unable to load resources (create-db-sql-xml)")) ;
		}
		
		return xmlstr ;
	}
}
