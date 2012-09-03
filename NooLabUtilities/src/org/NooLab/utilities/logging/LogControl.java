package org.NooLab.utilities.logging;


import org.apache.log4j.*;


public class LogControl {

	/** 0=completely off, 2= normal, 3=debug,  4=fine debug  5=all <br/>;
	 * these levels will be translated to java.logger levels  
	 */
	public static int Level = 2;
	
	/** 1=local levels are valid, 2 = global LogControl level is valid */
	public static int globeScope = 2;
	
	public static org.apache.log4j.Level loggerLevel = org.apache.log4j.Level.FATAL;
	
	public static final CreateLogging cLogger = new CreateLogging();

	public static void setLevel(int vi) {
		Level = vi;
		CreateLogging.setDebugLevel( Level );
	} 
	
	
}
