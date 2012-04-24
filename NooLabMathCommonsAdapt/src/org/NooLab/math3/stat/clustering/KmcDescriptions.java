package org.NooLab.math3.stat.clustering;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * calculates and holds descriptions of
 * - fields : in total, for each cluster
 * - relation between fields as covariance matrix, as SVD: in total, for each cluster 
 * - detection of significant changes between correlations when comparing total view and cluster view
 *   -> which fields pair, which cluster 
 * 
 * collecting variance data for fields from clusters (they already know it, since it is calculated on the fly while acquiring data)
 * 
 * --- UseIndicator is respected,
 * --- target variable gets a dedicated treatment 
 * 
 *
 */
public class KmcDescriptions <T extends Clusterable<T>>{

	
	ArrayList<KmClusterDescription> kmcDescription = new ArrayList<KmClusterDescription>();
	
	KMeansPlusPlusClusterer<T> kmc;
	List<Cluster<T>> clusters; 
	AdvancedData<T> advData ;
	
	public KmcDescriptions( KMeansPlusPlusClusterer<T> kmc,
							List<Cluster<T>> clusters, 
							AdvancedData<T> advData) {
		 this.kmc = kmc;
		 this.clusters = clusters;
		 this.advData = advData;
		
		 calculate();
	}

	public void calculate(){
	
		
	}
	
	
	
}
