/**
 * 
 */
package org.NooLab.somfluid.core.engines.det;


/**
 * @author Administrator
 *
 */
public class SomProfiles {
	
	public double[][] profilesTable;
	public String[] profileVariables; // column headers
	public String[] profileLabels;
	
	public String definitions_sourcefile ="";
	
	public SomProfiles(int rowcount, int colcount){
		
		profilesTable = new double[rowcount][colcount];
		
		profileVariables = new String[colcount];
		profileLabels = new String[rowcount];
	}	
	
	
}
