package org.NooLab.math3.stat.clustering;

import java.util.ArrayList;
import java.util.List;

import org.NooLab.math3.stat.inference.TestUtils;

public class KMeansAnova <T extends Clusterable<T>>{

	List<Cluster<T>> clusters ;
	int targetColumnIndex = -1;
	int[] useIndicator;
	ArrayList<double[]> fieldStatistics = new ArrayList<double[]>(); 
	double[] pvalues = new double[0];

	private int pointLen=-1;
	                              
	public KMeansAnova(List<Cluster<T>> clusters, int[] useIndicator, int targetColIndex) {
		this.clusters = clusters;
		targetColumnIndex = targetColIndex ;
		this.useIndicator = useIndicator;
	}

	@SuppressWarnings("unchecked")
	public void perform() {
		List <T> points ;
		T point ;
		int clusterCount;
		double v;
		List vclasses ;
		clusterCount = clusters.size() ;
		
		point = clusters.get(0).getPoints().get(0) ;
		pointLen = point.getLength() ;
		
		for (int f=0;f<pointLen;f++){
			fieldStatistics.add(f,null);
		}
		// for all fields 
		for (int f=0;f<pointLen;f++){
			if ((f==targetColumnIndex) || (useIndicator[f]<=0)){
				continue;
			}
			vclasses = new ArrayList();
			// build classes across all cluster = prepare the data
			for ( Cluster<T> cluster: clusters){
				
				points = cluster.getPoints() ;
				double[] classF = new double[points.size()];
				if (points.size()>0){
					// get values from field f
					int fz=0;
					for (T p:points){
						v = p.getValues()[f];
						classF[fz] = v;
						fz++;
					}
				} // points?
				
				vclasses.add( classF);
			}// -> all clusters
			
			// perform ANOVA
			double testIndicator = 0.0;
			double[] fieldStat = new double[3] ;
			double fStatistic = TestUtils.oneWayAnovaFValue(vclasses); // F-value
			double pValue = TestUtils.oneWayAnovaPValue(vclasses);     // P-value

			// To test perform a One-Way Anova test with signficance level set at 0.01 
			// returns a boolean, true means reject null hypothesis
			boolean hb = TestUtils.oneWayAnovaTest(vclasses, 0.01); 	
			if (hb){
				testIndicator = 1.0;
			}
			
			fieldStat[0] = pValue;
			fieldStat[1] = testIndicator;
			fieldStat[2] = fStatistic;
			fieldStatistics.set(f,fieldStat);
		}// f -> all fields
		v=0.0;
	}

	public double[] getPValues() {
		pvalues = new double[fieldStatistics.size()];
		
		for (int i=0;i<fieldStatistics.size();i++){
			if (fieldStatistics.get(i)!=null){
				pvalues[i] = fieldStatistics.get(i)[0];
			}else{
				pvalues[i] = -1.0;
			}
		}
		return pvalues;
	}

	public int getMinPValueIndex() {
		int minix=-1;
		double mipv=99999999.09;
		
		if (pvalues.length>0){
		
			for (int i=0;i<pvalues.length;i++){
				if ((useIndicator[i]>0) && (pvalues[i]>=0) && (mipv>pvalues[i])){
					mipv=pvalues[i];
					minix=i;
				}
			}
		}
		
		return minix;
	}

	public int getMaxPValueIndex() {
		int maxix=-1;
		double mxpv=-1;
		
		if (pvalues.length>0){
			maxix=0;
			for (int i=0;i<pvalues.length;i++){
				if ((useIndicator[i]>0) && (pvalues[i]>=0) && (mxpv<pvalues[i])){
					mxpv=pvalues[i];
					maxix=i;
				}
			}
		}
		
		return maxix;
	}

	public int getCountOfSignificantFields(double pThreshold) {
		int scount=0;
		if (pvalues.length>0){
			
			for (int i=0;i<pvalues.length;i++){
				if ((useIndicator[i]>0) && (pvalues[i]>=0) && (pvalues[i]<pThreshold)){
					scount++;
				}
			}
		}
		
		return scount;
	}

	public int getPointLen() {

		return pointLen;
	}

}















