package org.NooLab.somtransform.algo.distribution;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.core.engines.det.DSom;




public class Distributions implements Serializable{
	
	private static final long serialVersionUID = 5578071380583844718L;

	
	/** for each variable one item  */
	ArrayList<EmpiricDistribution> items = new ArrayList<EmpiricDistribution>(); 
	
	

	// ========================================================================
	public Distributions(DSom dsom){
		
	}
	// ========================================================================
	
	public void add( EmpiricDistribution d){
		items.add(d) ;
	}

	public ArrayList<EmpiricDistribution> getDistributions() {
		return items;
	}

	public void setDistributions(ArrayList<EmpiricDistribution> distributions) {
		this.items = distributions;
	}
	
	
	
	
}
