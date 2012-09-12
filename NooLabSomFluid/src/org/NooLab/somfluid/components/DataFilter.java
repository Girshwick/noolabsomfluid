package org.NooLab.somfluid.components;

import java.io.Serializable;

import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.properties.ModelingSettings;


public class DataFilter implements Serializable{

	private static final long serialVersionUID = 2588764350213284361L;

	// transient SomFluidFactory sfFactory;
	transient ModelingSettings modelingSettings;
	
	BooleanTable boolTable ;
	
	
	// ========================================================================
	public DataFilter( ModelingSettings modset) {
		 
		modelingSettings = modset ;
		
		boolTable = new BooleanTable();
	}
	// ========================================================================

	public void addFilter( String variableLabel, double num,  String operator, int bTableRow, int bTableCol, boolean active) {

		boolTable.addBooleanCondition("");
	}

}
