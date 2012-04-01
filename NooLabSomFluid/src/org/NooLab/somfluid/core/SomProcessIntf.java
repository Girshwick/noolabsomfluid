package org.NooLab.somfluid.core;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;

public interface SomProcessIntf {

	
	public SomDataObject getSomDataObject();

	public String getNeighborhoodNodes(int index, int surroundN);

	public SomFluidProperties getSfProperties();

	public LatticePropertiesIntf getLatticeProperties();
	
	public VirtualLattice getSomLattice() ;
	
	public ArrayList<Integer> getUsedVariablesIndices(); 
	
	public void setUsedVariablesIndices(ArrayList<Integer> usedVariables);

	public void clear();
	
}
