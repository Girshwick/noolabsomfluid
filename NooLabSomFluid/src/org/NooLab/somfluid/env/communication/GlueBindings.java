package org.NooLab.somfluid.env.communication;

import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
 

public class GlueBindings {

	
	int glueType=0;
	SomFluidProperties sfProperties;
	SomFluidFactory sfFactory;

	// ========================================================================
	public GlueBindings( SomFluidFactory factory, SomFluidProperties somFluidProperties){ // , int gluetype
		sfProperties = somFluidProperties; 	
		sfFactory = factory;
		
	}
	// ========================================================================
	
	
	
}
