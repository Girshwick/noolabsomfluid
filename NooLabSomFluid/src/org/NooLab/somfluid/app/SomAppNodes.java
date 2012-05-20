package org.NooLab.somfluid.app;

import java.util.ArrayList;

public class SomAppNodes {

	ArrayList<SomAppNode> nodes = new ArrayList<SomAppNode>(); 
	
	
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
	
}
