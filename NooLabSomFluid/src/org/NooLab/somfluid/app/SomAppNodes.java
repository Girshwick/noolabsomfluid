package org.NooLab.somfluid.app;

import java.util.ArrayList;

import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurface;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.similarity.Similarity;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.nodes.LatticeIntf;
import org.NooLab.utilities.datatypes.IndexDistanceIntf;


/**
 * 
 * 
 * this acts as a surrogate for VirtualLattice;
 * 
 * basically it just allows to address the nodes
 * 
 * 
 * 
 *
 */
public class SomAppNodes implements LatticeIntf{

	ArrayList<SomAppNode> nodes = new ArrayList<SomAppNode>(); 
	
	
	SimilarityIntf similarityConcepts = new Similarity();  
	IntensionalitySurfaceIntf intensionalitySurface = new IntensionalitySurface();
	ExtensionalityDynamicsIntf extensionalityDynamics ;
	
	
	
	// ------------------------------------------------------------------------
	public SomAppNodes(SomAppSomObject somAppSomObject) {
	 
	}
	// ------------------------------------------------------------------------


	public ArrayList<SomAppNode> getNodes() {
		return nodes;
	}
	
	public SomAppNode getNode(int index){
		return nodes.get(index) ;
	}
	
	public SomAppNode getNodeBySign(boolean isTarget){
		return null ;
	}


	// ------------------------------------------------------------------------
	
	@Override
	public ArrayList<IndexDistanceIntf> getNeighborhoodNodes(int index, int nodeCount) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void refreshDataSourceLink() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void spreadVariableSettings() {
		// TODO Auto-generated method stub
		
	}
	
}
