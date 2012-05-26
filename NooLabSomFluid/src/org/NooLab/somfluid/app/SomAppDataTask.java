package org.NooLab.somfluid.app;

import org.NooLab.somfluid.SomAppDataTaskIntf;
import org.NooLab.utilities.net.GUID;


/**
 * 
 * 
 *
 */
public class SomAppDataTask implements SomAppDataTaskIntf {

	String guidStr = "";
	
	/** either a string containing the table, tabulator separated: it will be parsed,
	 *  or a DataTableObject of the SomFluid - environment */
	
	Object preparedDataTable ;
	
	
	
	// ========================================================================
	public SomAppDataTask(){
		// no parameters should be included in order to be generally applicable
		
		guidStr = GUID.randomvalue();
	}
	// ========================================================================	

	public void setDataObject( Object dataobj ){
		
		preparedDataTable = dataobj;
	}
	
	public String getGuidStr() {
		return guidStr;
	}
	
	
	
	
	
	
	
}
