package org.NooLab.utilities.vm;

public class RuntimeInfo {
	
	
	static Runtime runtime = Runtime.getRuntime();
	// =======================================================================
	public RuntimeInfo(){
		
	}
	// =======================================================================

	
	public static int processorCount(){
		
		return runtime.availableProcessors() ;
	}
	
	
	
}
