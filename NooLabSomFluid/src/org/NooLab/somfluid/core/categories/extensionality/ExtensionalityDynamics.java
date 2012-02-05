package org.NooLab.somfluid.core.categories.extensionality;

import java.util.ArrayList;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataTable;


/**
 * in-process preparations of dynamic variables that are derived from 
 * the actual list represented by the node, e.g.: 
 * - correlations between vars, 
 * - statistical properties
 * - any other procedural discrimination. like 
 *   > embedded ANN
 *   > embedded tiny SOM
 * 
 * this can be used to evaluate blocks or series of records within the SOM itself,
 * and in a dynamic manner, without the need to return to the transformer layer
 * for that purpose.
 * 
 * One could even thin about an embedded capability for transformation; 
 * this does not change the logical distinction between outer loop and inner loop
 * in learning from data, yet.
 * It just provides a more elegant and a much more faster approach for the integration 
 * of the loops.
 * 
 * 
 * 
 * 
 * 
 */
public class ExtensionalityDynamics implements ExtensionalityDynamicsIntf{

	SomDataObject somData;
	ArrayList<Integer> listOfRecords;
	
	// ========================================================================
	public ExtensionalityDynamics( SomDataObject somdata){
		
		somData = somdata;
	}
	// ========================================================================
	
	public void getListOfRecordsAsTable( ArrayList<Integer> records){
		listOfRecords = records;
	}

	 
	
	
	
}
