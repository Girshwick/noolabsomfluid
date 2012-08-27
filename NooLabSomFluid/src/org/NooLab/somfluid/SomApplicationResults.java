package org.NooLab.somfluid;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.core.engines.det.results.ResultAspect;



public class SomApplicationResults implements Serializable{

	private static final long serialVersionUID = 1355259211143688224L;
	
	public ArrayList<Integer> topEcrNodes;

	public ArrayList<ResultAspect> aspects = new ArrayList<ResultAspect>();

	
	
	// ========================================================================
	public SomApplicationResults(){
		
	}
	// ========================================================================	



	public ArrayList<Integer> getTopEcrNodes() {
		return topEcrNodes;
	}



	public void setTopEcrNodes(ArrayList<Integer> topEcrNodes) {
		this.topEcrNodes = topEcrNodes;
	}



	public ArrayList<ResultAspect> getAspects() {
		return aspects;
	}



	public void setAspects(ArrayList<ResultAspect> aspects) {
		this.aspects = aspects;
	}
	
	
	
}
