package org.NooLab.somfluid.properties;

import org.NooLab.somfluid.SomFluidProperties;

/**
 * 
 * this class reads and writes XML strings or files
 * and interpretes them as ModelingSettings-object
 * 
 * This is very important for autonomic systems, and for a separation
 * of GUI and SOM engine
 * 
 */
public class SettingsTransporter {

	SomFluidProperties sfProperties ;
	
	// ========================================================================
	public SettingsTransporter( SomFluidProperties sfp ){
		sfProperties = sfp;
		
	}
	// ========================================================================
	
	
	public String export() {
		
		return "";
	}
	
	
}
