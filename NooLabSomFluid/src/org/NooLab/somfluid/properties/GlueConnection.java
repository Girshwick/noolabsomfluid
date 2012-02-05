package org.NooLab.somfluid.properties;

import org.NooLab.somfluid.SomFluidProperties;
 

public class GlueConnection {

	public static final int _GLUEX_SOURCE   = 1;
	public static final int _GLUEX_RECEPTOR = 2;
	public static final int _GLUEX_DUAL     = 3;
	
	int glueType=0;
	SomFluidProperties sfProperties;
	
	public GlueConnection(SomFluidProperties somFluidProperties, int gluetype){
		sfProperties = somFluidProperties; 	
		glueType = gluetype ;
	}
	
}
