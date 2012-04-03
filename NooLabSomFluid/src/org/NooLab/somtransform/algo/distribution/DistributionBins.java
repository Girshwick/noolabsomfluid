package org.NooLab.somtransform.algo.distribution;

import java.io.Serializable;
import java.util.ArrayList;



public class DistributionBins  implements Serializable{

	private static final long serialVersionUID = -1533795918239818854L;
	
	/** for each variable one item  */
	public ArrayList<DistributionBin> items = new ArrayList<DistributionBin>(); 
	
	
	
	// ========================================================================
	public DistributionBins(int bcount){
		DistributionBin item;
		
		for (int i=0;i<bcount;i++){
			item = new DistributionBin();
			items.add(item);
		}
		
	}
	// ========================================================================

	public void add( DistributionBin d){
		items.add(d) ;
	}

	public ArrayList<DistributionBin> getDistributionBins() {
		return items;
	}

	public void setDistributionBins(ArrayList<DistributionBin> distributionBins) {
		this.items = distributionBins;
	}
	
	
}
