package org.NooLab.somfluid.core.nodes;

import java.util.ArrayList;

import org.NooLab.utilities.datatypes.IndexDistanceIntf;

  

public interface LatticeIntf {

	
	public ArrayList<IndexDistanceIntf> getNeighborhoodNodes( int index , int nodeCount, int controlparam );
	
	public void refreshDataSourceLink() ;
	
	public void spreadVariableSettings() ;
	
	public boolean setPreferredInitializationValues(ArrayList<String> pvars, ArrayList<Double> pvalues);
	
	
}
