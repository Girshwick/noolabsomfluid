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
	
	/**
	 * dependent on request, exports all settings into an XML or into a serialized object
	 * @param format 0=xml, 1=serialization of properties object 
	 */
	public String exportProperties( int format) {
		
		return "";
	}
	
	/**
	 *   
	 * @param xmlSettings  // might be an URL or the XML itself
	 * 
	 */
	public SomFluidProperties importProperties(String xmlSettings ) { 
		
		return null;
	}
	
}
