package tester;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.NooLab.math3.stat.clustering.Cluster;
import org.NooLab.math3.stat.clustering.Clusterable;
import org.NooLab.math3.stat.clustering.EuclideanDoublePoint;
import org.NooLab.math3.stat.clustering.KMeansPlusPlusClusterer;
import org.NooLab.math3.util.RawFileData;

public class KmeansClustering {
 
	
	
	public static void main(String[] args) {
		 
		new test(1);
	}

}


class test{

	// this "EuclideanDoublePoint" knows by itself how to calculate the distance
	KMeansPlusPlusClusterer<EuclideanDoublePoint> kMeans ;
	String[] columnHeaders;
	
	public test( int sourceid){
		
		

		go( sourceid );
	}
	
	private EuclideanDoublePoint[] getData(){
		// ClassA.class
		EuclideanDoublePoint[] points = null;
		
		try{

			URL source = KMeansPlusPlusClusterer.class.getResource("/resource/table.txt");
			File dataFile = new File(source.toURI());

			RawFileData datasrc = new RawFileData( kMeans.getMissingValue() );
			
			points = datasrc.readfromFile( dataFile, true);
			columnHeaders = datasrc.getHeaders();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return points;
	}
	
	private EuclideanDoublePoint[] getSimpleData(){
		
		EuclideanDoublePoint[] points = new EuclideanDoublePoint[7]; 
		
		// we use an index at pos 0, and some blind (just transported, yet not considered) value at pos 4 (last column)...
		points[0] = new EuclideanDoublePoint( new double[] { 1, 1959,265100, 11.6 , 0.6, 0.61 } );
		points[1] = new EuclideanDoublePoint( new double[] { 2, 1960,195200, 12.2 , 0.5, 0.62 } );
		points[2] = new EuclideanDoublePoint( new double[] { 3, 1967,215200, 11.8 , 0.7, 0.51 } );
		points[3] = new EuclideanDoublePoint( new double[] { 4, 159, 275300, 13.8 , 0.3, 0.41 } );
		points[4] = new EuclideanDoublePoint( new double[] { 5, 152, 325400, 12.7 , 0.2, 0.53 } );
		points[5] = new EuclideanDoublePoint( new double[] { 6, 164, 292200, 13.6 , 0.4, 0.44 } );
		points[6] = new EuclideanDoublePoint( new double[] { 7, 652, 295500, 23.7 , 0.4, 0.57 } );

		return points;
	}
	
	private void go( int datasource){
		
		EuclideanDoublePoint[] points ;
		
		
		kMeans = new KMeansPlusPlusClusterer<EuclideanDoublePoint>( (new Random(1234)) );
		
		// or any other value, e.g. -9.90901 , the effect is that a position in the vector is not considered 
		// if one of the vectors contains a MV at that position 
		kMeans.setMissingValue( -1.0 ) ; 
		
		
		if (datasource>=1){
			// the data, e.g. from a table given as double[][] ...
			points = getData();		
			clusterRealWorldData(points);
		}else{
			points = getSimpleData();	
			clusterSimpleData(points);
		}
		
		
		
	}

	private void clusterSimpleData(EuclideanDoublePoint[] points) {
		List<Cluster<EuclideanDoublePoint>> clusters1,clusters2,clusters3,clusters4;

		// now the two following settings that are optional, if not invoked, defaults apply:
		// -> all columns (variables), no missing value
		
		
		// VERY IMPORTANT parameter: here we exlude variables (positoins in vectors) 
		// from calculations like similarity (distance)
		// 
		// it must be applied BEFORE adding the data, 
		// an index column and its exclusion here is MANDATORY for finding the data later via their index (column 0)
		// 
		// example here: col 0 = index column, col 4 = some field, e.g. the target variable
		kMeans.setInactiveColumns( new int[]{0,4,5}) ; 
		 
		
		// not implemented yet: the field for goal oriented clustering (supervised clustering)
		// kMeans.setSupervisionTargetColumn(4) ; 
		

		
		// "quite" important, since variables with large values would dominate, 
		// leading to quite unreasonable results (try it by setting this to "false" and you will see it) 
		kMeans.useNormalizedData( true ) ;   
		
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		

		// now calling the clustering (simple version), which returns the list of clusters (each contains the selected records)
		clusters1 = kMeans.cluster( Arrays.asList(points), 1, 2 );  // expected result: (1,2,3,4,5,6,7)
					displayClusters( clusters1,""  ) ;
		
		clusters2 = kMeans.cluster(Arrays.asList(points), 2, 10 );	// expected result: (1,2,3) (4,5,6,7)
					displayClusters( clusters2,"" ) ;
		
		clusters3 = kMeans.cluster(Arrays.asList(points), 3, 10 ); 	// expected result: (1,2,3) (4,5,6) (7)
					displayClusters( clusters3,"" ) ; 
		 
		// clustering in its extended version 
		
		// provides a description of clusters: variances, covariances, representative record, etc. 
		kMeans.setCalculateDescription( true ) ; 

		kMeans.setClusterCountAutoDetect(true);		
		kMeans.setClusterMinimumSize(2);             // as count of data points 	
		kMeans.setClusterMaximumSize(0.42);			 // as quantil of all data, overrules minimum size be default
		kMeans.setClusterSizeMinimumDominant(true) ; // now the minimum overrules the defined maximum, relevant if robustness is required
		
		clusters4 = kMeans.cluster(Arrays.asList(points), 2, 10 ); // expected result: (1,2,3) (4,5,6) (7)
				displayClusters( clusters4,"Number of clusters has been auto-detected, "+
										   "constraint is maxClusterSize = "+ kMeans.getMaxClusterSize()) ; 
		// TODO: the clusters should know about the record closest to its centroid, 
		//       and some parameters that describe their variability (total, per field)		
		
	}

	private void clusterRealWorldData(EuclideanDoublePoint[] points) {
		List<Cluster<EuclideanDoublePoint>> clusters3 = null,clusters4 = null,clustergroups=null;
		
		
		kMeans.setInactiveColumns( new int[]{0,1,2,6,8,11}) ; 
		 
		
		// the field for goal oriented clustering (supervised clustering)
		// this column will be excluded from distance calculation, i.e. the clustering will not change , 
		// but a dedicated description will be provided 
		kMeans.setSupervisionTargetColumn(11) ; 
		
		
		// "quite" important (=mandatory in real-world applications), since variables with large values would dominate 
		// variables with smaller values, leading to quite unreasonable results  
		kMeans.useNormalizedData( true ) ;   
		
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		 
		 
		// clustering in its extended version 
		

		kMeans.setClusterCountAutoDetect(true);		
		kMeans.setClusterMinimumSize(19);            // as count of data points 	
		kMeans.setClusterMaximumSize(0.27);			 // as quantil of all data, overrules minimum size be default

		kMeans.setClusterMinimumCount(8);            // respects minimum size
		
		// next parameter is not yet implemented (not implemented yet)
		// kMeans.setClusterSizeMinimumDominant(true) ; // now the minimum overrules the defined maximum, relevant if robustness is required
		

		// set to true or 1 for acceleration
		// kMeans.setSerialParallelProc(1); 		 // acceleration is proportional to number of clusters so far
													 // in principle it works, yet, the kmeans is dependent on the order of the records, 
													 // hence the results are not identical across runs

		// provides a description of clusters: variances, covariances, representative record, etc. N.IMPL.Y.
		kMeans.setCalculateDescription( true ) ; 

		kMeans.setDistanceMethod( Clusterable._DISTANCE_COMPOUND );
		
		
		// now happy clustering...
		// kMeans.removeData();
		// clusters3 = kMeans.cluster(Arrays.asList(points), 3, 50 );  
					displayClusters( clusters3, "Number of clusters has been auto-detected, "+
										   		"constraint is maxClusterSize = "+ kMeans.getMaxClusterSize()+"\n") ; 
				
		System.out.println("   - - - - - - ");
		clusters4 = kMeans.optimalSegmentation(Arrays.asList(points), 3, 50 );
		 			displayClusters( clusters4, "Variance controlled segmentation has been performed.\n"+
												"Number of clusters has been auto-detected, "+
												"constraint is maxClusterSize = "+ kMeans.getMaxClusterSize()+"\n") ; 
		System.out.println("   - - - - - - ");

		// determine groups within the list of clusters 
		clustergroups = kMeans.determineClusterGroups(clusters4,3,20) ;
					displayClusters( clustergroups,"");
					
		System.out.println("   - - - - - - ");
		
	}

	
	
	/**
	 * 
	 * simply make print of the clusters to the console
	 * 
	 * @param clusters
	 * @param comment
	 */
	private void displayClusters( List<Cluster<EuclideanDoublePoint>> clusters, String comment) {
		EuclideanDoublePoint cPoint ; 
		List<EuclideanDoublePoint> ps;
		EuclideanDoublePoint cp;
		double v ;
		int[] indexes;
		double[] pvalues ;
		
		if (clusters==null){
			return;
		}
		
		System.out.println();
		if (comment.length()>0){
			System.out.println(comment);
		}
		// System.out.println( kMeans.getPerformedIterations() + " iterations have been performed.\n");
		
		int tvIndex = kMeans.getSupervisionTargetColumnIndex() ;

		 
		int cL = clusters.get(0).getCenter().getLength();

		System.out.print("                                ");
		for (int p=0;p<cL;p++){
			if ((kMeans.getUseIndicator()[p]>0) || (kMeans.getUseIndicator()[p]==-2)){
				System.out.print(columnHeaders[p]+ "     ");
			}
		}
		System.out.println();
		
		for (int i=0; i<clusters.size();i++){
			
			ps = clusters.get(i).getPoints() ;
			
			cPoint = clusters.get(i).getCenter() ;
			cL = cPoint.getLength();
			
			
			System.out.print("Cluster #"+(i+1)+", center values: ");
		
			pvalues = clusters.get(i).getCenter().getValues();  // cPoint.getPoint();
			
			for (int p=0;p<cL;p++){
			
				if ((kMeans.getUseIndicator()[p]>0) || (tvIndex==p)){
					System.out.print(String.format("%10.2f", pvalues[p])+"  ");
				}
			}

			
			indexes = new int[ps.size()];
			
			for (int p=0;p<ps.size();p++){
				indexes[p] = (int) ps.get(p).getPoint()[0]; // retrieving value from index column, usually col 0
			}
			System.out.print("   Records by Index:  ");
			for (int p=0;p<indexes.length;p++){
				System.out.print( indexes[p] + " ");
			}

			System.out.println();
			 
			System.out.print("             denormalized: ");
			      
			for (int p=0;p<cL;p++){
				v = kMeans.deNormalizeValue(pvalues[p],p); 
				
				if ((kMeans.getUseIndicator()[p]>0) || (tvIndex==p)){
					System.out.print(String.format("%10.2f", v)+"  ");
				}
			}
			
			System.out.println();                      //  "
			System.out.println("                     size:   "+ps.size() );
			
			if (kMeans.isCalculateDescription()){
				for (int p=0;p<cL;p++){
					// get standard variation for each of the fields
				}
			} 
			// display normalized variance [(var of var across fields)/(mean of var across fields) ] for cluster

			
			
		} // i-> all clusters
		
		
	}
	
}