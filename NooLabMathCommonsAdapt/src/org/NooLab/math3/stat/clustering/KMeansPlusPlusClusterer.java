package org.NooLab.math3.stat.clustering;


import java.math.BigInteger;
import java.util.ArrayList;
 
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
 

import org.NooLab.chord.IndexedItemsCallbackIntf;
import org.NooLab.chord.MultiDigester;
import org.NooLab.math3.exception.ConvergenceException;
import org.NooLab.math3.exception.LocalizedFormats;
import org.NooLab.math3.exception.MathIllegalArgumentException;
import org.NooLab.math3.exception.NumberIsTooSmallException;
import org.NooLab.math3.random.Binomial;
import org.NooLab.math3.stat.MissingValue;
import org.NooLab.math3.stat.MissingValueIntf;
import org.NooLab.math3.util.MathUtils;

import org.apache.commons.math.stat.descriptive.moment.Variance;
 
 

/**
 * Clustering algorithm based on David Arthur and Sergei Vassilvitski k-means++ algorithm.
 * 
 * The clustering algorithm itself is left unchanged as compared to appache commons.math3;
 * Yet, we added the capability to dealwith missing values!
 * 
 * Else, driven by the "theory of clustering practice", we added some properties and 
 * their respective methods, such as 
 *  - useIndicator[] 
 * 	- clusterCountAutoDetect
 *  - maxClusterCount 
 *  - maxClusterSizeQuantil 
 *  - minClusterSize 
 *  - maxClusterSize;
 * 
 * 
 * 
 * @param <T> type of the points to cluster
 * @see <a href="http://en.wikipedia.org/wiki/K-means%2B%2B">K-means++ (wikipedia)</a>
 * @version $Id$
 * @since 2.0
 */
@SuppressWarnings("rawtypes")
public class KMeansPlusPlusClusterer<T extends Clusterable<T>> {

    /** Strategies to use for replacing an empty cluster. */
    public enum EmptyClusterStrategy {

        /** Split the cluster with largest distance variance. */
        LARGEST_VARIANCE,

        /** Split the cluster with largest number of points. */
        LARGEST_POINTS_NUMBER,

        /** Create a cluster around the point farthest from its centroid. */
        FARTHEST_POINT,

        /** Generate an error. */
        ERROR

    }

    /** Random generator for choosing initial centers. */
    private Random random;

    /** Selected strategy for empty clusters. */
    private EmptyClusterStrategy emptyStrategy;

    AdvancedData<T> advData =  new AdvancedData<T>();
    
	KMeansPlusPlusClusterer kmc ;
	
	KmcDescriptions kmcDescriptions;

	// ...................................... added to improve applicability

	/** flag to indicate whether data should be normalized before performing clustering */
	private boolean normalizedData = true;
	
	/** array that indicates whether a particular field (position n the vector) should be regarded 
	 *  in calculating the similarity (distance) between records */
    private  int[] useIndicator = new int[0] ;
    int[] inactivateColumns = new int[0] ;
    
    /** */
    MissingValueIntf missingValue = new MissingValue();

    boolean serialParallelProc = false;
    
    /** 
     * Note that the proposed number of clusters will NOT be the minimum count of clusters as other
     * clustering methods probably would come up with. Given the constraints of minimum/maximum size 
     * of clusters, and a variance criterion and a heuristic based on "residuals" the number of cluster
     * could be larger in order to resolve intermediate assignments (though still under the
     * condition of an "ignorant attitude")
     *   
     */
	private boolean clusterCountAutoDetect = false;

	private int maxClusterCount = -1;

	private double maxClusterSizeQuantil = -1.0;

	private int minClusterSize = 1;

	private int maxClusterSize = -1;

	private int supervisionTargetColumnIndex;

	private boolean calculateDescription;

	private boolean clusterSizeMinimumDominant;

	private int clusterMinimumCount;

	
	int performedIterations = 0;
	int performedReInits = 0;

	private ArrayList<Integer> firstPointIndexes = new ArrayList<Integer>();
	
	AssignmentScorekeeper ask;

	private int distanceMethod = Clusterable._DISTANCE_EUCLIDEAN ;
	
	// PrintLog out = new PrintLog(2,true);

	
    /** Build a clusterer.
     * <p>
     * The default strategy for handling empty clusters that may appear during
     * algorithm iterations is to split the cluster with largest distance variance.
     * </p>
     * @param random random generator to use for choosing initial centers
     */
    // ========================================================================
    public KMeansPlusPlusClusterer(final Random random) {
        this(random, EmptyClusterStrategy.LARGEST_VARIANCE);
        kmc = this;
    }

    /** Build a clusterer.
     * @param random random generator to use for choosing initial centers
     * @param emptyStrategy strategy to use for handling empty clusters that
     * may appear during algorithm iterations
     * @since 2.2
     */
    public KMeansPlusPlusClusterer(final Random random, final EmptyClusterStrategy emptyStrategy) {
        this.random        = random;
        this.emptyStrategy = emptyStrategy;
    }
 
   

	 

	public double deNormalizeValue(double value, int p) {
    	double result ;
    	
    	result = advData.deNormalizeValue(p, value) ;
    	
		return result;
	}

	public void removeData() {
		advData.clear() ;
		advData = new AdvancedData<T>();
		
		firstPointIndexes.clear() ;
		
		kmcDescriptions  = null ;
	}

	
	
	/**
     * Runs the K-means++ clustering algorithm.
     * 
     * The clustering algorithm itself is unchanged as compared to appache commons.math3;
     * Yet, we added the capability to dealwith missing values!
     * 
     * Else, driven by the "theory of clustering practice", we added some properties and 
     * their respective methods, such as 
     * 	- clusterCountAutoDetect
     *  - maxClusterCount 
     *  - maxClusterSizeQuantil 
     *  - minClusterSize 
     *  - maxClusterSize;
     *
     * ..........................................................................................
     * 
     * @param points the points to cluster
     * @param k the number of clusters to split the data into
     * @param numTrials number of trial runs
     * @param maxIterationsPerTrial the maximum number of iterations to run the algorithm
     *     for at each trial run.  If negative, no maximum will be used
     * @return a list of clusters containing the points
     * @throws MathIllegalArgumentException if the data points are null or the number
     *     of clusters is larger than the number of data points
     */
    public List<Cluster<T>> cluster( final Collection<T> points, final int k,
                                     int numTrials, int maxIterationsPerTrial)
                                    						throws MathIllegalArgumentException {

        // at first, we have not found any clusters list yet
        List<Cluster<T>> best = null;
        double bestVarianceSum = Double.POSITIVE_INFINITY;


        List<Cluster<T>> clusters = cluster(points, k, maxIterationsPerTrial);
        
        int n = clusters.get(0).getLength() ;
        defineUseIndicators( n );
        
        // do several clustering trials
        for (int i = 0; i < numTrials-1; ++i) {

            // compute a clusters list
            clusters = cluster(points, k, maxIterationsPerTrial);

            // compute the variance of the current list
            double varianceSum = 0.0;
            for (final Cluster<T> cluster : clusters) {
                if (!cluster.getPoints().isEmpty()) {

                    // compute the distance variance of the current cluster
                    final T center = cluster.getCenter();
                    final Variance stat = new Variance();
                    for (final T point : cluster.getPoints()) {
                        stat.increment(point.distanceFrom(center,useIndicator,missingValue, distanceMethod));
                    }
                    varianceSum += stat.getResult();

                }
            }

            if (varianceSum <= bestVarianceSum) {
                // this one is the best we have found so far, remember it
                best            = clusters;
                bestVarianceSum = varianceSum;
            }

        }

        // return the best clusters list found
        return best;

    }

    /**
	     * Runs the K-means++ clustering algorithm.
	     *
	     * @param points the data "points" to cluster
	     * @param k_numberOfClusters the number of clusters to split the data into
	     * @param maxIterations the maximum number of iterations to run the algorithm
	     *     for.  If negative, no maximum will be used
	     * @return a list of clusters containing the points
	     * @throws MathIllegalArgumentException if the data points are null or the number
	     *     of clusters is larger than the number of data points
	     */
	    @SuppressWarnings("unchecked")
		public List<Cluster<T>> cluster( final Collection<T> points, 
	    								 final int k_numberOfClusters,
	                                     final int maxIterations)     throws  MathIllegalArgumentException {
	    	
	    	
	    	int changes = -1, csize,salientPointIndex ;
	    	int inactiveN ;
	    	int lastEnlargeAttemptCCount=-1;
	    	int k = k_numberOfClusters;
	    	boolean resizeKMeans, autoClusterCount;
	    	
	        List<Cluster<T>> clusters, initialclusters ;
	
	        // ................................................
	        
	        if ((clusterCountAutoDetect==false) && (clusterMinimumCount>this.maxClusterCount)){
	        	clusterMinimumCount=maxClusterCount;
	        	if (maxClusterCount>=3){
	        		clusterMinimumCount=maxClusterCount-1 ;
	        	}
	        }
	        autoClusterCount = this.clusterCountAutoDetect ;
	        if (k>0){
	        	clusterCountAutoDetect = false;
	        	int cmc = clusterMinimumCount;
	        	int cisz = minClusterSize ; 	
	    		double cxsz = maxClusterSizeQuantil ;
	
	        	cluster( points, -3, 15 );
	        	removeData() ;
	        	clusterMinimumCount = cmc;
	        	clusterCountAutoDetect = autoClusterCount ;
	        	
	        	minClusterSize = cisz; 	
	        	maxClusterSizeQuantil = cxsz;
	    		
	        }
	        k = Math.abs(k) ;
	        
	        // sanity checks
	        MathUtils.checkNotNull(points);
	
	        // number of clusters has to be smaller or equal the number of data points
	        if (points.size() < k) {
	            throw new NumberIsTooSmallException(points.size(), k, false);
	        }
	        
	        inactiveN = this.inactivateColumns.length;
	        
	        // determine the maximum size of clusters expressed as N,
	        int nd = (int) Math.round( ((double)points.size()) * maxClusterSizeQuantil );
	        setMaxClusterSize(nd);
	         
	        // create the initial clusters
	        int hk=k;
	        if (clusterCountAutoDetect){
	        	int vn  = ((List<T>)points).get(0).getLength() - inactiveN;
	        	hk = heuristicalDeterminationOfInitialClusterCount( points.size(), vn);
	        }
	        
	         
	        initialclusters = chooseInitialCenters(points, hk, null, -1, random);
	        clusters = initialclusters ;
	      
	        // create an array containing the latest assignment of a point to a cluster
	        // no need to initialize the array, as it will be filled with the first assignment
	        int[] assignments = new int[points.size()];
	        changes = -1;
	        boolean clusterConditions = false ;
	// out.print(2, "start...");
	        while (clusterConditions==false){
	        	
	        	performedIterations++;
	        	clusters = initialclusters ;
	
				assignPointsToClusters( clusters, (Collection<T>) advData.getRawPoints(), assignments);
	
				final int maxIter = (maxIterations < 0) ? Integer.MAX_VALUE : maxIterations;
	
	
				// iterate through updating the centers until we're done
				int count = 0;
				while ((count < maxIter) && ((changes<0) || (changes>=1) )){
	
					boolean emptyCluster = false;
					List<Cluster<T>> newClusters = new ArrayList<Cluster<T>>();
					 
					
					for (Cluster<T> cluster : clusters) {
	
						final T newCenter;
						if (cluster.getPoints().isEmpty()) {
							switch (emptyStrategy) {
							case LARGEST_VARIANCE: // that's the default value
								newCenter = getPointFromLargestVarianceCluster(clusters);
								break;
							case LARGEST_POINTS_NUMBER: // not suitable, too greedy
								newCenter = getPointFromLargestNumberCluster(clusters);
								break;
							case FARTHEST_POINT:
								newCenter = getFarthestPoint(clusters);
								break;
							default:
								throw new ConvergenceException( LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
							}
							emptyCluster = true;
						} else {  
							newCenter = cluster.getCenter().centroidOf( cluster.getPoints(), useIndicator, missingValue);
						}
						
						Cluster<T> addedCluster = new Cluster<T>(newCenter, distanceMethod ) ;
						newClusters.add( addedCluster );
						
						// early break
						if (maxClusterSize>=2){
							csize = addedCluster.getPoints().size();
							if (csize>maxClusterSize){
								clusterConditions=false;
								break ;
							}
						}
						
					 
					} // -> all clusters
					
					
					
						changes = assignPointsToClusters( newClusters, (Collection<T>) advData.getRawPoints(), assignments);
					
						if (((count<=5) &&(changes> points.size()*0.09)) || ((count>5) && (changes> points.size()*0.15))){
							// we start with a different initialization, 
							// we should choose one that is neither too large nor too small, and where the variance is smaller than in others
							
							salientPointIndex = getSalientPoint(newClusters);
							initialclusters = chooseInitialCenters(points, hk, null, salientPointIndex, random);
					        newClusters.clear();
					        clusters = initialclusters ;
					        assignPointsToClusters( clusters, (Collection<T>) advData.getRawPoints(), assignments);
					        performedReInits++;
					        // System.out.println("changes after last iteration : "+changes+" , KMeans++ will be reinitialized with a salient record.");
						}else{
							clusters = newClusters; 
						}
						// if there were no more changes in the point-to-cluster assignment
						// and there are no empty clusters left, return the current clusters
						if ((clusterConditions) && (changes == 0 && !emptyCluster)) {
							break;
							// we first have to check the sizes (if requested)
							// return clusters;
						}
	
						count++;
						performedIterations++;
				} // -> maxIter
				
				// System.out.println("changes after last iteration : "+changes);
				
if (clusters.size()==7){
	nd=0;
}
				
				// 
				resizeKMeans = false;
				clusterConditions = true;
				if ((maxClusterSize>=2) && (autoClusterCount)){
					
					for (Cluster<T> cluster : clusters) {
						csize = cluster.getPoints().size();
					
						if (csize>maxClusterSize){
							resizeKMeans = true;
							clusterConditions = false;
							break;
						}
					}
							
				}else{
					resizeKMeans = false;
	    			clusterConditions = true;
	 			} // maxClusterSize ? 
	 
	
				// System.out.println("cmc: "+clusterMinimumCount+" , n clusters: "+clusters.size()) ;
				if ((clusterConditions) && (resizeKMeans==false))
				{
					
					System.gc();
					
					if ((clusterMinimumCount>=2) && (clusterMinimumCount>clusters.size())){
						// System.out.println("ccount too small");
	 
						if (lastEnlargeAttemptCCount != clusters.size()){
							
							lastEnlargeAttemptCCount = clusters.size();
							if (performedIterations > 0.4*maxIterations){
								performedIterations = (int) (0.4*maxIterations) ;
							}
						  
							int mincsize = 99999999;
							for (Cluster<T> cluster : clusters) {
								csize = cluster.getPoints().size();
								if (mincsize>csize){
									mincsize = csize;
								}
							}
							if (mincsize>minClusterSize)
							{
								clusterConditions=false;
								resizeKMeans = true;
							}
						 
						}else{
							// System.out.println("ccount too small, but last attempt failed.");
						}
						 
					} // clusterMinimumCount
	
				}
				
				if ((resizeKMeans) && (performedIterations < 2*maxIterations)){
	
	                	// we raise the number of clusters by 1 and reinitialize the k-means
	                	// in a dedicated manner, that is, using the current N-1 unaffected centers and the 2 new centers
							
					clusterConditions = false ;
		            	// splitclusters = adaptClustersize( cluster ) ;
		                			
	                	// now add 1 cluster, create the new initialization, 
	                hk++;
	                performedIterations = (int) (performedIterations * 0.6);
	                
	                salientPointIndex = getSalientPoint(clusters);
	                initialclusters  = chooseInitialCenters(points, hk, initialclusters, salientPointIndex, random);
						   
						// re-init this
					assignments = new int[points.size()];
					changes = -1;
						
					if (hk>points.size()){
						clusterConditions = true;
						resizeKMeans = false;
					}			
				}
				
	
				
				if (performedIterations > 2*maxIterations){
					clusterConditions = true;
				}
	        } // -> clusterConditions = true
	 
	         
			if (minClusterSize>2){
				// if all clusters are ok, no changes will be applied
				ClusterMerger cm = new ClusterMerger(); 
				cm.establishMinimumSizedClusters(clusters);
				if (cm.isChanged()){
					clusters = cm.getClusters();
				}
			} // minClusterSize ?
			
	 		if (calculateDescription){
				kmcDescriptions = new KmcDescriptions( this,clusters, advData );
				// this is necessary for the ANOVA?
			}
	// out.print(2, "finished"); 	 
	        return clusters;
	    }

	/**
	 * 
	 * after each clustering, it  			
	 * performs an ANOVA for each of the fields (across all clusters), and removing the least non-significant,
	 * until there are only 3 fields left, or all fields are significant, or the overall significance drops
	 * 
	 * 
	 * @param points
	 * @param k_numberOfClusters
	 * @param maxIterations
	 * @return
	 * @throws MathIllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	public List<Cluster<T>> optimalSegmentation( final Collection<T> points, 
			 									 final int k_numberOfClusters,
			 									 final int maxIterations)     throws  MathIllegalArgumentException {
	
		List<Cluster<T>>  clusters = null;
		double[] pValues ;
		double cpv,ts;
		int pvMaxIx,  fieldCount;
		int[] originalUseIndicator ;
		boolean done=false;


		originalUseIndicator = new int[useIndicator.length];
		System.arraycopy(useIndicator, 0, originalUseIndicator, 0, useIndicator.length);
		
		// loop for adapting the useIndicator[]
		while (done==false){
			done = true;
			// create the map for translating the indexes
			// cluster() selects only a subset of fields into the cluster points
			// thus any index query to a cluster point needs to be adapted 
			// ixT.clear();
			// createIndexMap( ixT, useIndicator);
			this.removeData();
			performedIterations=0;
			// 
			clusters = cluster( points,k_numberOfClusters,maxIterations) ;
		
			
			KMeansAnova anova = new KMeansAnova(clusters,useIndicator,supervisionTargetColumnIndex);
			anova.perform();
			
			pValues = anova.getPValues();
			// int pvMinIx = anova.getMinPValueIndex();
			pvMaxIx = anova.getMaxPValueIndex();
			int signCount = anova.getCountOfSignificantFields(0.08);
			
			if (pvMaxIx>=0){
				fieldCount = anova.getPointLen();
				
				if (fieldCount>3){
					cpv = pValues[pvMaxIx];
					if (signCount > fieldCount*0.4){
						ts = 0.049 ;
					}else{
						ts = 0.08;
					}
					if (cpv>ts){
						System.gc();
						useIndicator[pvMaxIx] = 0;
						done=false;
						// System.out.println("field removed: "+pvMaxIx+" (p-value was "+String.format("%.3f", cpv)+"), new field count = "+(fieldCount-1));
					} // not significant ?
				} // fieldCount > 3 ?
				
				
			} // pvMinIx ?
			
		} // ->
		
		return clusters;
	}
	
	
	 

	/* 
	// note that the original version does not provide a constructor where one can put just the means and variances! 

	double[] classA = {93.0, 103.0, 95.0, 101.0, 91.0, 105.0, 96.0, 94.0, 101.0 };
	double[] classB =
	   {99.0, 92.0, 102.0, 100.0, 102.0, 89.0 };
	double[] classC =
	   {110.0, 115.0, 111.0, 117.0, 128.0, 117.0 };

	List classes = new ArrayList();
	classes.add(classA);
	classes.add(classB);
	classes.add(classC);
	          
	// Then you can compute ANOVA F- or p-values associated with the null hypothesis 
	// that the class means are all the same using a OneWayAnova instance or TestUtils methods:

	double fStatistic = TestUtils.oneWayAnovaFValue(classes); // F-value
	double pValue = TestUtils.oneWayAnovaPValue(classes);     // P-value

	// To test perform a One-Way Anova test with signficance level set at 0.01 
	// returns a boolean, true means reject null hypothesis
	TestUtils.oneWayAnovaTest(classes, 0.01); 
	
	*/


	@SuppressWarnings("unchecked")
	public List<Cluster<T>> determineClusterGroups( List<Cluster<T>> clusters, 
																	   int k_numberOfCGroups, 
																	   int maxIterations)    throws  MathIllegalArgumentException {
		Collection<T> points = new ArrayList<T>();
		T center, point = null;
		double v;
		
		if ((clusters==null) || (clusters.size()<2*k_numberOfCGroups)){
			return null;
		}
		try{
			
			int z=1;
			// create data table from clusters
			for (Cluster<T> cluster:clusters){
				
				center = cluster.getCenter() ;
				// int pointlen = center.getLength() ;
				double[] cdata = center.getValues() ;
				
				for (int p=0;p<cdata.length ; p++){
					v = deNormalizeValue(cdata[p],p); 
					cdata[p] = v;
				}
				
				cdata[0] = z;
				point = (T) new EuclideanDoublePoint( cdata );
				points.add(point) ;
				z++;
			}
			
			this.removeData();
			
			this.clusterCountAutoDetect = false;
			this.minClusterSize = 2;
			this.clusterMinimumCount = 2;
			this.calculateDescription = false;
			this.maxClusterSize = minClusterSize*3;
			this.performedIterations=0;
			
			clusters = cluster( points,k_numberOfCGroups,maxIterations) ;
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return clusters;
	}

	class ClusterMerger{
    	
    	List<Cluster<T>> clusters = new ArrayList<Cluster<T>>() ;
    	boolean changed = false;
    	
    	/**
		 * @return the changed
		 */
		public boolean isChanged() {
			return changed;
		}


		public ClusterMerger(){
    		
    	}

    	
    	public List<Cluster<T>> getClusters() {
    		return clusters ;
		}


		public void establishMinimumSizedClusters( List<Cluster<T>> inClusters) {
    		//
    		boolean clusterConditions ;
    		int csize ;
    		double distance, minDistance ;
    		
    		List <T> cpoints;
    		Cluster<T> bestMatchCluster = null, candidateDissolveCluster=null;
    		
    		List<Cluster<T>> smallClusters = new ArrayList<Cluster<T>>();
    		changed = false;
    		
    		clusterConditions = false;
    		while (clusterConditions==false){

    			clusterConditions = true;

    			/*
    			 * step 1: collect all clusters that are too small
    			 * step 2: for these clusters decide which one to dissolve 
    			 *         2.1. for all small clusters determine the expected change 
    			 *              (only the positive part) of variance = average distance of records from small clusters 
    			 *                                                     to centroid of target clusters
    			 *         2.2. select the cluster with smallest such avg distance
    			 *         2.3. dissolve that cluster by sending records to the winner clusters
    			 */

    			for (Cluster<T> cluster : inClusters) {
    				csize = cluster.getPoints().size();
    			
    				if (csize<minClusterSize){
    					clusterConditions = false;
    					smallClusters.add(cluster) ;
    				}
    			} // -> check all clusters

    			double minAvgCollectionDistance = 999999999.09;
    			
    			// now we are calculating the min of the min = sup inf d(x,y) 
    			for (Cluster<T> scluster : smallClusters) {
    				
    				cpoints = scluster.getPoints() ;
    				
    				double minDistancesSum = 0;
    				for (T point : cpoints) {
    				
    					minDistance = 999999999.09;
    					// determine minimal distance by comparing to all clusters
    					for (Cluster<T> cluster : inClusters) {
    						if (cluster != scluster){
    							T center = cluster.getCenter().centroidOf( cluster.getPoints(), useIndicator, missingValue);
    							distance = point.distanceFrom( center,useIndicator,missingValue, distanceMethod ) ;
    							if (minDistance>distance){
    								minDistance = distance ;
    								bestMatchCluster = cluster ;
    							}
    						}
    					}
    					
    					minDistancesSum = minDistancesSum + minDistance; 
    					
    				} // -> all points of selected small cluster
    			
    				if (minAvgCollectionDistance>minDistancesSum){
    					minAvgCollectionDistance = minDistancesSum;
    					candidateDissolveCluster = bestMatchCluster ;
    				}
    				
    			} // -> all small clusters
    			
    			
    			if ((candidateDissolveCluster!=null) && (candidateDissolveCluster.getPoints().size()>0) && 
    				((double)candidateDissolveCluster.getPoints().size()< (double)maxClusterSize*0.72)){
    				
    				clusterConditions = false ;
    				
    				cpoints = candidateDissolveCluster.getPoints() ;
    				boolean pointsDissolved = false;
    				int z=0;
    				
    				for (T point : cpoints) {
    					minDistance = 9999999.09 ;
    					bestMatchCluster = null ;
    					
    					if (point != null){
    						
    						for (Cluster<T> cluster : inClusters) {
        						if ((cluster != candidateDissolveCluster) && (cluster.getPoints().size()<maxClusterSize)){
        					
        							T center = cluster.getCenter().centroidOf( cluster.getPoints(), useIndicator, missingValue);
        							distance = point.distanceFrom( center, useIndicator, missingValue, distanceMethod ) ;
        							if (minDistance>distance){
        								minDistance = distance ;
        								bestMatchCluster = cluster ;
        							}
        						} // cluster ok as candidate ?
        					} // checking all cluster as candidates for a new home of this point
    						
    					} // point != null ?
    					
    					
    					if (bestMatchCluster!=null){
    						
    						if (bestMatchCluster.getPoints().size()<maxClusterSize){
    							pointsDissolved = true;

        						bestMatchCluster.addPoint(point,z,useIndicator,missingValue) ;
        						// update centroid vector
        						T newCenter = bestMatchCluster.getCenter().centroidOf( bestMatchCluster.getPoints(), useIndicator, missingValue);
        						
        						bestMatchCluster.setCenter(newCenter) ;

    							point=null;
    							
    						} // bestMatchCluster.size() has available slots ?
    					} // bestMatchCluster ?
    					z++;
    				} // all points

    				// remove the dissolved cluster
    				
    				if (pointsDissolved){

        				for (int i=inClusters.size()-1;i>=0;i--){
        					if (inClusters.get(i)==candidateDissolveCluster ){
        						inClusters.remove(i);
        						break;
        					}
        				}

    				}
    				candidateDissolveCluster = null;
    				//
    				changed = true;
    				
    			}else{
    				clusterConditions = true;
    				clusters = inClusters ;
    				break ;
    			}
    			
    			// put all the records of this cluster into the other ones, 
    			// one record after the other
    		} // -> clusterConditions ?

    			
    			
    	} // establishMinimumSizedClusters() 

		
    } // inner class ClusterMerger
	
    
	
	private int getSalientPoint(List<Cluster<T>> clusters) {
		int chosenPointIndex = -1;
		
		int csize, minCSize= 99999999, maxCSize = -1;
		double cVar, minCVariance = 999999999999.09;
		Cluster<T> selectedC = null;
		
		try{
			cVar=0;
			for (Cluster<T> cluster : clusters) {
				csize = cluster.getPoints().size();
				if (minCSize>csize)minCSize=csize;
				if (maxCSize<csize)maxCSize=csize;
			}
	
			cVar=0;
			for (Cluster<T> cluster : clusters) {
				csize = cluster.getPoints().size();
				if ((clusters.size()>2) && (csize!=minCSize) && (csize!=maxCSize)){
					// get variance
					cVar = cluster.getVariabilityScore();
					cVar = cluster.getVarOfVar() ;
					if (cVar>=0){
						if (minCVariance>cVar){
							minCVariance = cVar;
							selectedC = cluster ;
						}
					}
				}else{
					if (clusters.size()<=2){
						selectedC = cluster ;
						break;
					}
				}
			}
			if (selectedC == null){
				// even the first one is better than none
				selectedC = clusters.get(0) ;
			}
			cVar=0;
			if (selectedC != null){
				// get the point that is closest to the center
				 
				chosenPointIndex = selectedC.getRepresentativePoint();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return chosenPointIndex;
	}



	/**
     * Adds the given points to the closest {@link Cluster}.
     *
     * @param <T> type of the points to cluster
     * @param clusters the {@link Cluster}s to add the points to
     * @param points the points to add to the given {@link Cluster}s
     * @param assignments points assignments to clusters
     * @return the number of points assigned to different clusters as the iteration before
     */
  
	@SuppressWarnings("hiding")
	private <T extends Clusterable<T>> int assignPointsToClusters(  final List<Cluster<T>> clusters, 
        				   						  					Collection<T> points,
        				   						  					final int[] assignments) {
    	
        int assignedDifferently = 0;
        int pointIndex = 0,z=0;
        Cluster<T> cluster =null;
        
        
        
         
        if ((serialParallelProc) && (points.size()>200)){
        	// organizing class has to wait
        	 
        	 
	    	Object[] pointsArray;
	    	
	    	for (Cluster<T> c: clusters){
	    		
	    		for (final T p : points) { // a pseudo loop, just for accessing the iterator of the collection
	    			c.initForSPP( p,useIndicator,missingValue );
	    			break;
	    		}
	    	}
	    	
	    	ask = new AssignmentScorekeeper(assignments); 
	    	
	    	pointsArray = points.toArray( ) ;
	    	
            PointAssignmentsDigester pointsAssignDigester = new PointAssignmentsDigester(  clusters, pointsArray) ;
            pointsAssignDigester.doParallelAssignments(points.size()) ;
        	
            assignedDifferently = ask.assignedDifferently ;
            // assignments = ask.assignments ;
             
        }else{
        	for (final T p : points) {
        	
        		int clusterIndex = getNearestCluster(clusters, p);
        		if (clusterIndex != assignments[pointIndex]) {
        			assignedDifferently++;
        		}

        		cluster = clusters.get(clusterIndex);
        		cluster.addPoint(p,z, useIndicator,missingValue);
        		assignments[pointIndex++] = clusterIndex;
        		z++;
        	}// -> all points
        } // 
        
        int n = clusters.get(0).getLength() ;
        if (this.useIndicator.length==0){
        	defineUseIndicators( n );
        }
        
        return assignedDifferently;
    }

    /**
     * 
     * assigns a single point
     * 
     *
     */
    class PointAssignmentHelper{
    	Object pointObj;
    	List<Cluster<T>> clusters;
    	AssignmentScorekeeper ask;
    	
    	 
        int pointIndex = 0 ;
        Cluster<T> cluster =null;
        
    	@SuppressWarnings("unchecked")
		public PointAssignmentHelper( Object clistObj, Object pointObj, AssignmentScorekeeper ask){
    		this.pointObj = pointObj;
    		clusters = (List<Cluster<T>>)clistObj ;
    		this.ask = ask;
    	}
    	
    	
		@SuppressWarnings("unchecked")
		/**
		 * this 
		 */
		public void assignSelectedPoint(int id, int pid) {

			T point = (T) pointObj;

			// int n = point.getValues().length;
			// double v = point.getValues()[0];

			// 
    		// System.out.println("item id : "+id+"");
			
			int clusterIndex = getNearestCluster(clusters, point);
			
    		if (clusterIndex != ask.assignments[ id ]) {
    			ask.assignedDifferently++; // an object is needed that is mor epersistent than this class
    		}
    		// we need to synchronize each cluster separately by a dynamic list of compartments that add points
    		
    		// clusterFeeders.get(clusterIndex).addPoint( point,id, useIndicator,missingValue);  
    		
    		cluster = clusters.get(clusterIndex);
    		// this method is sync'ed, thus the acceleration is dependent on the number of clusters,
    		// acceleration is only if number of clusters > number of processors 
    		cluster.addPoint( point,id, useIndicator,missingValue); 
    		
    		ask.setAssignment(id, clusterIndex);
    		 
		}
    }
    
    class AssignmentScorekeeper{
    	int[] assignments;
    	int assignedDifferently = 0;
    	
    	public AssignmentScorekeeper( int[] assignments ){
    		this.assignments = assignments;
    	}
    	
    	synchronized public void setAssignment(int id,int clusterIndex){
    		assignments[id] = clusterIndex;
    	}
    	public int getClusterIndex( int id){
    		return assignments[ id ];
    	}
    	public void setClusterIndex( int id, int clusterIndex){
    		assignments[ id ] = clusterIndex;
    	}
    }
    /**
     * 
     * we also need a dynamic array of independent cluster updaters, i.e. per cluster 1 updater
     * encapsuled into a class and individually synchronized
     *
     */
     @SuppressWarnings("hiding")
	class PointAssignmentsDigester <T extends Clusterable<T>>  implements IndexedItemsCallbackIntf{
    	
    	MultiDigester digester=null ;
    	int threadcount=1;
    	
    	Object[] pointsArray;
    	Object clistObj;
    	
   

    	public PointAssignmentsDigester( Object clistObj, Object[] pointsArray ){
    		 
    		this.pointsArray = pointsArray;
    		this.clistObj = clistObj;
    	
    	}
    	
    	public void doParallelAssignments( int pointsCount ){ 
    		 
    		Runtime runtime = Runtime.getRuntime();
    		threadcount = Math.min( 8,Math.max(2,runtime.availableProcessors()-2)); 
    		
    		
    		// providing also right now the callback address (=this class)
    		// the interface contains just ONE routine: perform()
    		if ((digester==null) ){
    			
    			digester = new MultiDigester(threadcount, (IndexedItemsCallbackIntf)this ) ;
    			digester.setPriority(7);
    			    			
    		// note, that the digester need not to know "anything" about our items, 
    		// just the amount of items we would like to work on.
    		// the digester then creates simply an array of indices, which then point to the actual items,
    		// which are treated outside (below) !

    			digester.prepareItemSubSets( pointsCount,0 );
    			// subset 1 : [0, 6, 12, 18, 24, 30, 36, 42, 48, 54, 60 ...
    			// subset 2 : [1, 7, 13, 19, 25, 31, 37, 43, 49, 55, 61 ...
    			// ...
    		}else{
    			// will also take any change of "threadcount"
    			digester.reset();
    		}

    		
    		digester.execute() ;
    		 
    		
    	}
    	
    	/**
    	 * 
    	 * this now is being called in parallel form the digester.execute()
    	 */
		@Override
		public void perform( int processID, int id ) {
			// this calls the routine outside in the embedding class
			// update point of index id, id refers to a global index
			 
			// System.out.println( "process : "+processID+" ,  item id : "+id);
			// System.out.print( "process : "+processID+" ");
			
			Object pointObj = pointsArray[id] ;
			(new PointAssignmentHelper( clistObj,pointObj,ask )).assignSelectedPoint(id, processID) ;
			
			// do not set a breakpoint in multithreaded mode, eclipse will crash...
		}
    	
    	
    	
    }
    /**
     * Use K-means++ to choose the initial centers.
     *
     * @param <T> type of the points to cluster
     * @param points the points to choose the initial centers from
     * @param k the number of centers to choose
     * @param random random generator to use
     * @return the initial centers
     */
    @SuppressWarnings({ "unchecked", "hiding" })
	private <T extends Clusterable<T>> List<Cluster<T>>  chooseInitialCenters( final Collection<T> points, final int k, 
																			   final List<Cluster<T>> knownCenters,
																			   final int salientPointIndex,
																			   final Random random) {

        // Convert to list for indexed access. Make it unmodifiable, since removal of items
        // would screw up the logic of this method.
        final List<T> pointList = Collections.unmodifiableList(new ArrayList<T> (points));

        // The number of points in the list.
        final int numPoints = pointList.size();

        // Set the corresponding element in this array to indicate when
        // elements of pointList are no longer available.
        final boolean[] taken = new boolean[numPoints];

        // The resulting list of initial centers.
        final List<Cluster<T>> resultSet = new ArrayList<Cluster<T>>();

        
		// we need a refresh due to Java's type erasure...
        AdvancedData<T> advData = kmc.advData;
       
        
        if (useIndicator.length==0){
        	defineUseIndicators( pointList.get(0).getLength() );
        }
        
        advData.setUseIndicator(useIndicator);
        advData.setMissingValue(missingValue);
        advData.useNormalizedData( normalizedData );
        
        if (advData.getNumPoints() != numPoints){
        	advData.setOutdated(true) ;
        }
        
        if (advData.isOutdated()){
			advData.clear();

			for (int i = 0; i < numPoints; i++) {
				advData.addPoint(pointList.get(i));
			}

			advData.normalize();
			advData.setOutdated(false);
			advData.setNumPoints(numPoints);
		}
		
        // Choose one center uniformly at random from among the data points.
        int firstPointIndex = random.nextInt(numPoints);
        
        // the results are extremely sensitve to this choice, which of course is garbage.
        if (salientPointIndex>=0){
        		// firstPointIndex = 886 ;
        		firstPointIndex = salientPointIndex ;
        }
        
        // System.out.println("firstPointIndex : "+firstPointIndex);
        
        firstPointIndexes.add(firstPointIndex) ;
        
        final T firstPoint = advData.getPoint(firstPointIndex);
        
        resultSet.add(new Cluster<T>(firstPoint, distanceMethod ));

        // Must mark it as taken
        taken[firstPointIndex] = true;

        // To keep track of the minimum distance squared of elements of
        // pointList to elements of resultSet.
        final double[] minDistSquared = new double[numPoints];

        if ((useIndicator.length==0) && (inactivateColumns.length>0)){
        	 defineUseIndicators( firstPoint.getLength());
        }
        // Initialize the elements.  
        // Since the only point in resultSet is firstPoint, this is very easy.
        // and also: TODO we make it parallel if there are more than 10000 records
        
        // TODO XXX : offer fork to multi-threaded parallel mode if there are many records           
        for (int i = 0; i < numPoints; i++) {
            if (i != firstPointIndex) { // That point isn't considered
                double d = firstPoint.distanceFrom( advData.getPoint(i),useIndicator, missingValue, distanceMethod );
                minDistSquared[i] = d*d;         //  pointList.get(i);
            }
        }

        while (resultSet.size() < k) {

            // Sum up the squared distances for the points in pointList not
            // already taken.
            double distSqSum = 0.0;

            for (int i = 0; i < numPoints; i++) {
                if (!taken[i]) {
                    distSqSum += minDistSquared[i];
                }
            }

            // Add one new data point as a center. Each point x is chosen with
            // probability proportional to D(x)2
            final double r = random.nextDouble() * distSqSum;

            // The index of the next point to be added to the resultSet.
            int nextPointIndex = -1;

            // Sum through the squared min distances again, stopping when
            // sum >= r.
            double sum = 0.0;
            for (int i = 0; i < numPoints; i++) {
                if (!taken[i]) {
                    sum += minDistSquared[i];
                    if (sum >= r) {
                        nextPointIndex = i;
                        break;
                    }
                }
            }

            // If it's not set to >= 0, the point wasn't found in the previous
            // for loop, probably because distances are extremely small.  
            // Just pick the last available point.
            if (nextPointIndex == -1) {
                for (int i = numPoints - 1; i >= 0; i--) {
                    if (!taken[i]) {
                        nextPointIndex = i;
                        break;
                    }
                }
            }

            // We found one.
            if (nextPointIndex >= 0) {

                final T p = advData.getPoint(nextPointIndex);
                           // pointList.get(nextPointIndex)

                resultSet.add(new Cluster<T> (p, distanceMethod ));

                // Mark it as taken.
                taken[nextPointIndex] = true;

                if (resultSet.size() < k) {
                    // Now update elements of minDistSquared.  We only have to compute
                    // the distance to the new center to do this.
                    for (int j = 0; j < numPoints; j++) {
                        // Only have to worry about the points still not taken.
                        if (!taken[j]) {
                            double d = p.distanceFrom( advData.getPoint(j),useIndicator, missingValue, distanceMethod );
                            double d2 = d * d;      // pointList.get(j)
                            if (d2 < minDistSquared[j]) {
                                minDistSquared[j] = d2;
                            }
                        }
                    }
                }

            } else {
                // None found --
                // Break from the while loop to prevent an infinite loop.
                break;
            }
        }

        return resultSet;
    }

    /**
     * Get a random point from the {@link Cluster} with the largest distance variance.
     *
     * @param clusters the {@link Cluster}s to search
     * @return a random point from the selected cluster
     */
    private T getPointFromLargestVarianceCluster(final Collection<Cluster<T>> clusters) {

        double maxVariance = Double.NEGATIVE_INFINITY;
        Cluster<T> selected = null;
        
        /*
         * we my apply two different strategies, dependent on the balance of the cluster filing,
         * from both measures we calc a score to decide... yet, approximately it would look like this
         *  - balanced, #clusters >5 : process per cluster
         *  - unbalanced, #clusters <=5  : spawning indexes of records across processes
         * 
         */
        for (final Cluster<T> cluster : clusters) {
            if (!cluster.getPoints().isEmpty()) {

                // compute the distance variance of the current cluster
                final T center = cluster.getCenter();
                final Variance stat = new Variance();
                
                
                for (final T point : cluster.getPoints()) {
                    stat.increment(point.distanceFrom(center,useIndicator, missingValue, distanceMethod ));
                }
                final double variance = stat.getResult();

                // select the cluster with the largest variance
                if (variance > maxVariance) {
                    maxVariance = variance;
                    selected = cluster;
                }

            }
        }

        // did we find at least one non-empty cluster ?
        if (selected == null) {
            throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
        }

        // extract a random point from the cluster
        final List<T> selectedPoints = selected.getPoints();
        return selectedPoints.remove(random.nextInt(selectedPoints.size()));

    }

    /**
     * Get a random point from the {@link Cluster} with the largest number of points
     *
     * @param clusters the {@link Cluster}s to search
     * @return a random point from the selected cluster
     */
    private T getPointFromLargestNumberCluster(final Collection<Cluster<T>> clusters) {

        int maxNumber = 0;
        Cluster<T> selected = null;
        for (final Cluster<T> cluster : clusters) {

        	// nothing ot optimize here...
        	
            // get the number of points of the current cluster
            final int number = cluster.getPoints().size();

            // select the cluster with the largest number of points
            if (number > maxNumber) {
                maxNumber = number;
                selected = cluster;
            }

        }

        // did we find at least one non-empty cluster ?
        if (selected == null) {
            throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
        }

        // extract a random point from the cluster
        final List<T> selectedPoints = selected.getPoints();
        return selectedPoints.remove(random.nextInt(selectedPoints.size()));

    }

    /**
     * Get the point farthest to its cluster center
     *
     * @param clusters the {@link Cluster}s to search
     * @return point farthest to its cluster center
     */
    private T getFarthestPoint(final Collection<Cluster<T>> clusters) {

        double maxDistance = Double.NEGATIVE_INFINITY;
        Cluster<T> selectedCluster = null;
        int selectedPoint = -1;
        
        for (final Cluster<T> cluster : clusters) {

            // get the farthest point
            final T center = cluster.getCenter();
            final List<T> points = cluster.getPoints();
            
            for (int i = 0; i < points.size(); ++i) {
            
            	final double distance = advData.getPoint(i).distanceFrom(center,useIndicator, missingValue, distanceMethod );
            	
                if (distance > maxDistance) { // points.get(i)
                    maxDistance     = distance;
                    selectedCluster = cluster;
                    selectedPoint   = i;
                }
            }

        }

        // did we find at least one non-empty cluster ?
        if (selectedCluster == null) {
            throw new ConvergenceException(LocalizedFormats.EMPTY_CLUSTER_IN_K_MEANS);
        }

        return selectedCluster.getPoints().remove(selectedPoint);

    }

    /**
     * Returns the nearest {@link Cluster} to the given point
     *
     * @param <T> type of the points to cluster
     * @param clusters the {@link Cluster}s to search
     * @param point the point to find the nearest {@link Cluster} for
     * @return the index of the nearest {@link Cluster} to the given point
     */
    @SuppressWarnings("hiding")
	private <T extends Clusterable<T>> int getNearestCluster(final Collection<Cluster<T>> clusters, final T point) {
        double minDistance = Double.MAX_VALUE;
        int clusterIndex = 0;
        int minCluster = 0;
        
        for (final Cluster<T> c : clusters) {
        	
            final double distance = point.distanceFrom( c.getCenter(),useIndicator, missingValue, distanceMethod );
            if (distance < minDistance) {
                minDistance = distance;
                minCluster = clusterIndex;
            }
            clusterIndex++;
        }
        return minCluster;
    }

    /**
     * this has to be called ONCE, but only subsequent to the reading of the data
     */
    private  void defineUseIndicators( int colcount){
    	
    	if (colcount<0){
    		return;
    	}
    	useIndicator = new int[colcount];
    	
    	for (int i=0;i<useIndicator.length;i++){
    		useIndicator[i]=1;
    	}
    	if (inactivateColumns!=null){
    		for (int i=0;i<inactivateColumns.length;i++){
    			if ((inactivateColumns[i]>=0) && (inactivateColumns[i]<colcount)){
    				useIndicator[ inactivateColumns[i] ] = -1; // 
    			}
    		}
    	}
    	if ((supervisionTargetColumnIndex>=0) && (supervisionTargetColumnIndex<useIndicator.length)){
    		useIndicator[ supervisionTargetColumnIndex] = -2;
    	}
    	
    }
    
    private int heuristicalDeterminationOfInitialClusterCount( int recordCount, int vectorLen ) {
    	
    	int clusterCount=2;
    	int dataBasedN = 2 , structureBasedN = 2;
    	double irg;
    	BigInteger noRepeatscombiN = BigInteger.ONE ;
    	
    	// a more advanced approach would use PCA
    	
    	if (vectorLen>3){
    		// n!/(n-k)!
    		noRepeatscombiN = Binomial.get(vectorLen, vectorLen-2) ; 
    		dataBasedN = 1 + (int)Math.round( Math.log( noRepeatscombiN.doubleValue()) );
    	}
    	irg = (double)recordCount/(double)vectorLen;
    	if (irg>5){
    		structureBasedN = 1 + (int)Math.round( irg + Math.log10( recordCount));
    	}else{
    		structureBasedN = 1 + (int)Math.round( irg );
    	}
    	clusterCount = Math.max(2,(Math.min(structureBasedN, dataBasedN)) );
    	
    	if (maxClusterCount>=2){
    		if (clusterCount>maxClusterCount){
    			clusterCount = maxClusterCount;
    		}
    	}
		return clusterCount;
	}

	/**
	 * @return the kmcDescriptions
	 */
	public KmcDescriptions getKmcDescriptions() {
		return kmcDescriptions;
	}

	/**
	 * @return the missingValue
	 */
	public MissingValueIntf getMissingValue() {
		return missingValue;
	}

	public void setInactiveColumns() {
    	inactivateColumns = new int[0];
    }
	public void setInactiveColumns(int[] deactivatedColumns) {
		
		inactivateColumns = new int[ deactivatedColumns.length ];
		System.arraycopy( deactivatedColumns, 0, inactivateColumns, 0, deactivatedColumns.length) ;
		
	}

	public void importMissingValueDefinitions(MissingValueIntf  mv) {
		missingValue = mv;
	}
	
	public void setMissingValue(double _mv) {
		missingValue.setValue(_mv) ;
		missingValue.setActive(true);
	}

	// not realized so far, requires bookkeeping... of min,max per field, in order to provide retranslation
	public void useNormalizedData(boolean flag) {
		 normalizedData = flag;
	}
	public boolean useNormalizedData() {
		 return normalizedData ;
	}

	public void setUseIndicator(int[] useindicator) {
		useIndicator = useindicator;
	}

	public int[] getUseIndicator() {
		return useIndicator;
	}

	public void setClusterCountAutoDetect(boolean flag, int maxCCount) {
		clusterCountAutoDetect = flag ;
		maxClusterCount = maxCCount;
	}
	
	/**
	 * auto-detection of cluster count uses a constraint-based heuristics similar to fuzzy c-means
	 * constraints are max number of clusters and minimum size of clusters
	 * 
	 * @param flag
	 */
	public void setClusterCountAutoDetect(boolean flag) {
		clusterCountAutoDetect = flag ;
	}

	/**
	 * @return the maxClusterCount
	 */
	public int getMaxClusterCount() {
		return maxClusterCount;
	}

	/**
	 * @param maxClusterCount the maxClusterCount to set
	 */
	public void setMaxClusterCount(int maxClusterCount) {
		this.maxClusterCount = maxClusterCount;
	}

	/**
	 * @return the clusterCountAutoDetect
	 */
	public boolean isClusterCountAutoDetect() {
		return clusterCountAutoDetect;
	}

	public void setClusterMinimumSize(int minsize) {
		 
		minClusterSize = minsize;
	}

	public void setClusterMaximumSize(double maxsizequantil) {
		
		maxClusterSizeQuantil = maxsizequantil;
	}

	/**
	 * @return the maxClusterSizeQuantil
	 */
	public double getMaxClusterSizeQuantil() {
		return maxClusterSizeQuantil;
	}

	/**
	 * @param maxClusterSizeQuantil the maxClusterSizeQuantil to set
	 */
	public void setMaxClusterSizeQuantil(double maxClusterSizequantil) {
		maxClusterSizeQuantil = maxClusterSizequantil;
	}

	/**
	 * @return the minClusterSize
	 */
	public int getMinClusterSize() {
		return minClusterSize;
	}

	/**
	 * @param minClusterSize the minClusterSize to set
	 */
	public void setMinClusterSize(int minclustersize) {
		minClusterSize = minclustersize;
	} 

	public int getMaxClusterSize() {
		return maxClusterSize;
	}

	/**
	 * @param maxClusterSize the maxClusterSize to set
	 */
	public void setMaxClusterSize(int maxclustersize) {
		maxClusterSize = maxclustersize;
	}

	public void setSupervisionTargetColumn(int colIndex) {

		supervisionTargetColumnIndex = colIndex ;
		if ((useIndicator!=null) && (colIndex<useIndicator.length) && (colIndex>=0)){
			useIndicator[colIndex]=-2;
		}
	}

	/**
	 * @return the supervisionTargetColumnIndex
	 */
	public int getSupervisionTargetColumnIndex() {
		return supervisionTargetColumnIndex;
	}

	/**
	 * @param supervisionTargetColumnIndex the supervisionTargetColumnIndex to set
	 */
	public void setSupervisionTargetColumnIndex(int colIndex) {
		supervisionTargetColumnIndex = colIndex;
	}

	public void setCalculateDescription(boolean flag) {
		calculateDescription = flag;
		
	}
 
	public boolean isCalculateDescription() {
		return calculateDescription;
	}

	public void setClusterSizeMinimumDominant(boolean flag) {
		 
		clusterSizeMinimumDominant = flag;
	}

	/**
	 * @return the clusterSizeMinimumDominant
	 */
	public boolean isClusterSizeMinimumDominant() {
		return clusterSizeMinimumDominant;
	}

 

	/**
	 * @return the performedIterations
	 */
	public int getPerformedIterations() {
		return performedIterations;
	}

	/**
	 * @return the normalizedData
	 */
	public boolean isNormalizedData() {
		return normalizedData;
	}

	/**
	 * @return the performedReInits
	 */
	public int getPerformedReInits() {
		return performedReInits;
	}

	/**
	 * @return the firstPointIndexes
	 */
	public ArrayList<Integer> getFirstPointIndexes() {
		return firstPointIndexes;
	}

	public void setClusterMinimumCount(int value) {
		clusterMinimumCount = value;
	}

	/**
	 * @return the clusterMinimumCount
	 */
	public int getClusterMinimumCount() {
		return clusterMinimumCount;
	}

	/**
	 * @return the serialParallelProc
	 */
	public boolean isSerialParallelProc() {
		return serialParallelProc;
	}

	/**
	 * @param serialParallelProc the serialParallelProc to set
	 */
	public void setSerialParallelProc(boolean flag) {
		this.serialParallelProc = flag;
	}
	public void setSerialParallelProc(int flag) {
		this.serialParallelProc = flag>=1;
	}

	public void setDistanceMethod(int distancemethod) {

		distanceMethod = distancemethod;
		
	}

	/**
	 * @return the distanceMethod
	 */
	public int getDistanceMethod() {
		return distanceMethod;
	}
}