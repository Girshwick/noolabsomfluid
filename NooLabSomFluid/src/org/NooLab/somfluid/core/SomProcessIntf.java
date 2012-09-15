package org.NooLab.somfluid.core;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.lattice.VirtualLattice;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;

public interface SomProcessIntf {

	
	public SomDataObject getSomDataObject();

	public String getNeighborhoodNodes(int index, int surroundN);

	public SomFluidAppGeneralPropertiesIntf getSfProperties();

	public LatticePropertiesIntf getLatticeProperties();
	
	public VirtualLattice getSomLattice() ;
	
	public void nodeChangeEvent(ExtensionalityDynamicsIntf extensionality, long result);
	
	public ArrayList<Double> getUsageIndicationVector(boolean inclTV) ;
	
	public ArrayList<Integer> getUsedVariablesIndices(); 
	
	public void setUsedVariablesIndices(ArrayList<Integer> usedVariables);

	public void clear();

	
	
}
