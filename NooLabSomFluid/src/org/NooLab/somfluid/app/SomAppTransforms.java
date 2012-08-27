package org.NooLab.somfluid.app;

import java.util.ArrayList;




public class SomAppTransforms {

	ArrayList<SomAppAlgorithm> nodes = new ArrayList<SomAppAlgorithm>(); 
	
	// ========================================================================
	public SomAppTransforms(SomAppTransformer somAppTransformer ){
		
	}
	// ========================================================================	
	
	

	public ArrayList<SomAppAlgorithm> getNodes() {
		return nodes;
	}
	
	public SomAppAlgorithm getNode(int index){
		return nodes.get(index) ;
	}
	
	public SomAppAlgorithm getNodeBySign(boolean isTarget){
		return null ;
	}
}
