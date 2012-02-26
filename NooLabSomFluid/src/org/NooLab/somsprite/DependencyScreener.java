package org.NooLab.somsprite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.NooLab.repulsive.components.data.IndexDistance;
import org.NooLab.somfluid.components.IndexedDistances;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.core.engines.det.SomMapTable;
import org.NooLab.stats.MannWhitneyUTest;
import org.NooLab.stats.clustering.Cluster;
import org.NooLab.stats.clustering.EuclideanDoublePoint;
import org.NooLab.stats.clustering.EuclideanIntegerPoint;
import org.NooLab.stats.clustering.KMeansPlusPlusClusterer;
import org.NooLab.utilities.logging.PrintLog;
// import org.apache.mahout.clustering.fuzzykmeans.*;
 

/**
 * 
 * for all formulas func(a,b):
 *   - check the association between a+TV, b+TV, f(a,b)+TV 
 * 
 * 
 * sbrt.kernel.math.stat.comp 
 * 
 * @author kwa
 *
 */
public class DependencyScreener {

	SomSprite  somSprite;
	
	SomMapTable somMapTable;
	Evaluator evaluator;
	
	IndexedDistances ixdPair = new IndexedDistances();
	
	PrintLog out;
	
	// ========================================================================
	public DependencyScreener(SomSprite somsprite, SomMapTable smt, Evaluator ev) {
		
		somSprite = somsprite;
		somMapTable = smt;
		evaluator = ev;
	
		out = somSprite.out;
	}
	// ========================================================================	
	
	public void go(){
	
		double dr;
		int[] depVarIndex ;
		String[] depVar = new String[3];
		double[][] depVarValues = new double[5][somMapTable.values.length];
		
		ArrayList<int[]> depVarIndexes = new ArrayList<int[]>() ;
		
		int tvindex = somMapTable.tvIndex ;
		
		try{
			
			// we separate the tasks in a very trivial way;
			// yet, this supports parallel processing of the list
			dr=0;
			// potential problem here: not all functions are symmetric for a,b
			for (int a=0;a<somMapTable.values[0].length-1;a++ ){
				if (a==tvindex){
					continue;
				}
				 
				for (int b=a+1;b<somMapTable.values[0].length;b++ ){
					if (b==tvindex){
						continue;
					}
  
					depVarIndex = new int[3] ;
					depVarIndex[0] = a;
					depVarIndex[1] = b;
					depVarIndex[2] = somMapTable.tvIndex ; 
					depVarIndexes.add(depVarIndex) ;
					
				} 
			} // b->
			
			// TODO: we have to provide the TV values too !!!
			for (int i=0;i<depVarIndexes.size();i++){
				depVarIndex = depVarIndexes.get(i) ;
				
				depVar[0] = somMapTable.variables[ depVarIndex[0] ];
				depVar[1] = somMapTable.variables[ depVarIndex[1] ];
				
				int va = depVarIndex[0];
				int vb = depVarIndex[1];
				
				// get columns
				for (int r=0;r<somMapTable.values.length;r++){
					// get column a
					depVarValues[0][r] = somMapTable.values[r][va] ;
					// get column b
					depVarValues[1][r] = somMapTable.values[r][vb] ;
					// get column b
					depVarValues[2][r] = somMapTable.values[r][somMapTable.tvIndex] ;
					// TODO: we need also a binarized version of the TV, into 2 groups or n groups
					
				} // all rows in exrtacted columns
				
				// this returns a relative improvement of the association
				
				AssociationDifferential ad = new AssociationDifferential();
				dr = ad.check(depVarValues);
				
				
				if (dr>1){
					// collect this association, 
					IndexDistance ixd = new IndexDistance( va,vb, dr ) ;
					ixdPair.getItems().add(ixd);
				}
				
			} // i-> all pairs
			
			
			ixdPair.sort() ;
			
			// reduce for similar, only 3 items per pair
			
			reduceListOfCandidates(ixdPair);

			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		 
	}

	private void reduceListOfCandidates(IndexedDistances ixdPair) {
		 
		
	}
	
	class AssociationDifferential{
		
		
		public AssociationDifferential(){
			
		}
		
		public double check( double[][] depVarValues ){
			double potentialImprovement = 0.0 ;
			double v1,v2,fv,fmax ,fmin ;
			int nex ;
			String expressionName;
			double[] measure;
			
			
			v1=0;
			
			// all expressions
			nex = evaluator.xList.size() ;
			
			for (int f=0;f<nex;f++){

				expressionName = evaluator.getExpression(f);
				
				fmax = -1.0 ;fmin=9999999999.9;
				
				for (int i=0;i<depVarValues[0].length;i++){
				
					v1 = depVarValues[0][i];
					v2 = depVarValues[1][i];
				
					fv = evaluator.eval(expressionName, v1,v2) ;

					depVarValues[3][i] = fv ;
							if (fmax<fv )fmax = fv;
							if (fmin>fv )fmin = fv;
				
				} // i-> all values
				// normalize;
				if ((fmax==fmin) || (fmax<=0.0) || (fmax-fmin==0.0)){
					continue;
				}
				for (int i=0;i<depVarValues[0].length;i++){
					fv = depVarValues[3][i] ;
					
					fv = (fv-fmin)/(fmax-fmin) ;
					
					depVarValues[3][i] = fv ;
				}
				measure = new double[3] ;
				// now the statistics ...
				// 1. Mann-Whitney ...
				measure[0] = calculateAlignments(depVarValues);
				
				// 2. correlation
				measure[1] = calculateCorCovar(depVarValues);
				
				measure[2] = 0.0;
				int tm = somSprite.modelingSettings.getClassifySettings().getTargetMode();
				if (tm == ClassificationSettings._TARGETMODE_MULTI){
					// in this case, the improvement of the association needs to be measured by 
					// means of bivariate clustering; the improvement measure reflects the change 
					// of the ratio of variance intera vs inter (center) vs total
					// we use simple knn, controlled by numbers of cluster from TV (or reasonably chosen
					measure[2] = calculateMultiGroupAlignment( depVarValues );
				}
				
				// dependent on the target mode (1 group or many, discrete or corr), 
				// we apply different weightings for the measure
				potentialImprovement = estimateImprovement( measure ) ;
				
			} // f-> all expressions
			
			
			
			// we return the maximum
			return potentialImprovement ;
		}

		private double estimateImprovement(double[] measure) {
			 
			return 0;
		}

		private double calculateAlignments(double[][] depVarValues) {
			double rating = 0;
			double[] ms = new double[3] ;
			// 
			// Class WilcoxonSignedRankTestImpl
			// http://commons.apache.org/math/userguide/stat.html#a1.4_Simple_regression
			// http://www.jsc.nildram.co.uk/api/index.html
		
			// TestUtils.pairedTTest(sample1, sample2, alpha)
			int n = depVarValues[0].length;
			n=n+1-1;
			MannWhitneyUTest mwu = new MannWhitneyUTest();
			// this creates the value for the U-statistic
			// ms[0] = mwu.mannWhitneyU( depVarValues[0], depVarValues[2]) ;
			
			
			try {
				ms[0] = mwu.mannWhitneyUTest( depVarValues[0], depVarValues[2]) ;
				ms[1] = mwu.mannWhitneyUTest( depVarValues[1], depVarValues[2]) ;
				ms[2] = mwu.mannWhitneyUTest( depVarValues[3], depVarValues[2]) ;
			
				if ((ms[2]>0.9) && (ms[2]>ms[0]) && (ms[2]>ms[1])){
					rating = (ms[2]*ms[2]+ms[2])/(ms[1]+ms[0]) ;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return rating;
		}

		/**
		 * 
		 * actually, not the correlation is interesting, but the residuals are even more interesting
		 * 
		 * @param depVarValues
		 * @return
		 */
		private double calculateCorCovar( double[][] depVarValues ) {
			double rating = 0.0;
			double[] ms = new double[3] ;
			
			
			return rating;
		}

		 
		private double calculateMultiGroupAlignment(double[][] depVarValues) {
			double rating = 0.0;
			int n;
			
			// this "EuclideanDoublePoint" knows by itself how to calculate the distance
			KMeansPlusPlusClusterer<EuclideanDoublePoint> kMeans ;
			
			kMeans = new KMeansPlusPlusClusterer<EuclideanDoublePoint>( (new Random(1234)) );
			
			// now two settings that are optional, if not invoked, defaults apply:
			// -> all columns (variables), no missing value
			kMeans.setInactiveColumns( new int[]{0,4}) ; // before adding the data, MANDATORY for finding the data later via their index
			kMeans.setMissingValue( -1.0 ) ; // or any other value, e.g. -9.90901 , the effect is that a position is not considered if one of the vectors contains a MV at that position 
			kMeans.useNormalizedData( true ) ;   // quite important, since variables with large values would contribute much more to the sorting  
					
			EuclideanDoublePoint[] points = new EuclideanDoublePoint[4]; 
			                                                         
			// we use an index at pos 0, and some blind value at pos 4...
			points[0] = new EuclideanDoublePoint(new double[] { 1,1959,325100, 11.2, 0.6 });
			points[1] = new EuclideanDoublePoint(new double[] { 2,1960,325100, 14.5, 0.5 });
			points[2] = new EuclideanDoublePoint(new double[] { 3,159,325100, 18.8 , 0.3});
			points[3] = new EuclideanDoublePoint(new double[] { 4,152,325100, 12.7 , 0.2});
						
			
			
			List<Cluster<EuclideanDoublePoint>> clusters1 = kMeans.cluster( Arrays.asList(points), 1, 10 );

			List<Cluster<EuclideanDoublePoint>> clusters2 = kMeans.cluster(Arrays.asList(points), 2, 10 );
			
			List<Cluster<EuclideanDoublePoint>> clusters3 = kMeans.cluster(Arrays.asList(points), 3, 10 );
			
			n = clusters1.size() ;
			
			// assertEquals(1, clusters.size()); 
			 
			
			return rating ;
		}
		
	} // inner class
	// ..............................................................
	 
	

	public IndexedDistances getListOfCandidatePairs() {
		 
		return ixdPair;
	}
	

}
