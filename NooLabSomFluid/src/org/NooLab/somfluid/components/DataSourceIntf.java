package org.NooLab.somfluid.components;

import java.util.ArrayList;

import org.NooLab.somfluid.SomDataDescriptor;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.Variables;

public interface DataSourceIntf {

	
	public DataTable getDataTable();
	public DataTable getNormalizedDataTable();
	
	
	public ArrayList<Double> getRecordByIndex(int rIndex, int srcType) ;

	public int getRecordSize() ;
	public int getRecordCount() ;
	public int getColumnCount() ;
	
	public Variables getVariables() ; 
	
	public ArrayList<String> getVariablesLabels() ;
	
	public SomDataDescriptor getSomDataDescriptor() ;
	public void nodeChangeEvent( ExtensionalityDynamicsIntf extensionality, int result);
	
}
