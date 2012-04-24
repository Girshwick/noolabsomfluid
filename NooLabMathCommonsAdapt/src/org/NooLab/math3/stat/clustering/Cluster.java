package org.NooLab.math3.stat.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.NooLab.math3.stat.MissingValueIntf;

/**
 * Cluster holding a set of {@link Clusterable} points.
 * @param <T> the type of points that can be clustered
 * @version $Id$
 * @since 2.0
 */
public class Cluster<T extends Clusterable<T>> implements Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = -3442297081515880464L;

    /** an id value for more easy referencing just this cluster... */
    int numID = -1;
    
    /** The points contained in this cluster. */
    private final List<T> points;

    private int pointLen = -1;
    
    /** Center of the cluster. */
    private T center;

    int distanceMethod = Clusterable._DISTANCE_EUCLIDEAN;     
    
    /** the representative point expressed as the global index to the list of points  */
    int representativePointIndex = -1;
    
    RepresentativePoint representativePoint ;
    
    /** translates from localIndex -> globalIndex */
    SortedMap<Integer,Integer> globalIndexMap = new TreeMap<Integer,Integer>();
    	
    /** the variance data of the fields across all points */
	private ArrayList<FieldVariance> fieldVariances ;
	
	/** the variance of the field variances */
	private double varianceOfVariances = -1;
	
	/** the variability score across all fields */
	private double variabilityScore = -1;
	
    // ========================================================================
    /**
     * Build a cluster centered at a specified point.
     * @param center the point which is to be the center of this cluster
     */
    public Cluster(final T center, int distancemethod ) {
        this.center = center;
        points = new ArrayList<T>();
        distanceMethod = distancemethod ;
    }
    // ========================================================================
    
    
    /**
     * Add a point to this cluster. 
     * actually, don't use that: very naive, only to keep backward compatibility ot apache's original version!
     * @param point point to add
     */
    public void addPoint(final T point) {
    	addPoint(  point, -1, null, null); 
    }


    /**
     * 
     * for parallel processing we need a small init procedure 
     * that prepares the fieldVariances before actually starting the assignments/adds
     * 
     */
    
    public void initForSPP(final T point,int[] useIndicator, MissingValueIntf missingValue){
    	
    	pointLen = point.getLength() ;
        if (fieldVariances == null){
        	fieldVariances = new ArrayList<FieldVariance>();
        	for (int i=0;i<pointLen;i++){
        		fieldVariances.add( new FieldVariance() ) ;
        	}
        	representativePoint = new RepresentativePoint(useIndicator,missingValue) ;
        } // first one?
    }
    
    /**
     * Add a point to this cluster and provide the global index of that record.
     * The global index is required to determine the indx of the representative point;
     * 
     * 
     * @param point
     * @param globalIndex
     * @param useIndicator needed to calculate the variance across fields = the normalized variance of the cluster;
     *                     if it is not (properly) defined, all data points will be taken
     *                     note, however, that then the variances are extremely influenced by the absolute values of the data points 
     */
    synchronized public void addPoint(final T point, int globalIndex, int[] useIndicator, MissingValueIntf missingValue) {
    	int k;
    	int localIndex=-3,currFieldIndex=-3;
    	
    	points.add(point);
        pointLen = point.getLength() ;
        
        try{
        	

            localIndex = points.size()-1;
            
            globalIndexMap.put(localIndex, globalIndex) ;
            

            if (fieldVariances == null){
            	fieldVariances = new ArrayList<FieldVariance>();
            	for (int i=0;i<pointLen;i++){
            		fieldVariances.add( new FieldVariance() ) ;
            	}
            	representativePoint = new RepresentativePoint(useIndicator,missingValue) ;
            } // first one?
     
            // observe the field values: we maintain a measurement object for each of the fields
            for (int i=0;i<pointLen;i++){
            	
if ((localIndex>5) && (useIndicator[i]==-2) && (point.getValues()[i]>0)){
	k=0;
}
				currFieldIndex = i;
            	boolean hb = (useIndicator==null) || (useIndicator.length!=pointLen) || (useIndicator[i]>0) || (useIndicator[i]==-2);
            	if (hb){
            		double v = point.getValues()[i];
            		fieldVariances.get(i).observeValue(v);
            	}
            }
            
            determineVarianceOfVariances(useIndicator,missingValue);
            
            representativePoint.update( point, localIndex);
            
        }catch(Exception e){
        	 
        	System.err.println("localIndex="+localIndex+", globalIndex="+globalIndex+", currFieldIndex="+currFieldIndex) ;
        	
        	e.printStackTrace();
        	System.exit(-3) ;
        }
        
        
    }
    
    class RepresentativePoint{
    	
    	MissingValueIntf missingValue;
    	int[] useIndicator;
    	
    	double minCenterDistance = 999999999.09;
    	int currentBestLocalIndex = -1;
    	
    	public RepresentativePoint( int[] useIndicator, MissingValueIntf missingValue) {
    		this.useIndicator = useIndicator;
    		this.missingValue = missingValue;
		}

		public void update(  T point, int index ){
			
    		double d;
    		
    		try{

        		d = point.distanceFrom( center,useIndicator, missingValue, distanceMethod );
       		 
        		if (minCenterDistance>d){
        			minCenterDistance = d;
        			currentBestLocalIndex = index;
        			
        			representativePointIndex = globalIndexMap.get(currentBestLocalIndex) ;
        		}

    		}catch(Exception e){
    			System.out.println("cluster ("+this.toString()+") update() for index "+index);
    			e.printStackTrace();
    			System.exit(-3);
    		}
    	}
    }
	

    public void removeDataPoints(){
    	
    	points.clear() ;
    	representativePoint = null;
    	varianceOfVariances = -1;
    	fieldVariances.clear();
    	fieldVariances = null;
    	globalIndexMap.clear() ;
    }
    
    /**
     * calculates the variance across the variances of the fields, 
     * and also a variability score, that reflects the absolute value of the vairances
     * 
     */
    private void determineVarianceOfVariances(int[] useIndicator, MissingValueIntf missingValue) {
    	
    	FieldVariance fieldVar ;
    	double v, sv=0, qsv=0 , stdsum=0,nv;
    	int z=0;
    	
    	for (int i=0;i<pointLen;i++){
    		
        	boolean hb;
        	// note that here we have to exclude the target variable! (tv -> useIndicator[i]==-2)
        	hb = (useIndicator==null) || (useIndicator.length!=pointLen) || (useIndicator[i]>0) ;
        	
        	if (hb){

        		fieldVar = fieldVariances.get(i);
        		v = fieldVar.getVariance();
        		sv = sv+v ;
        		qsv = qsv+ v*v;
        	
        		stdsum = stdsum + 2*fieldVar.getStdDev()/(double)fieldVar.n ;
        		z++;
        	} // not a mv, not excluded ?
        } // all fields in centroid vector
    	nv = 1.0*z;
    	if (z!=0)varianceOfVariances = qsv /nv - (sv/nv)*(sv/nv) ;
    	
    	if (z!=0)variabilityScore = sv/nv * varianceOfVariances + stdsum/nv;
	}

	class FieldVariance{
    	double sum=0, qsum=0;
    	int n, zeroCounts=0;
    	double mean=-1,variance = -1, stdev=-1, coeffOfVariance = -1;
    	
    	public FieldVariance(){
    		
    	}
    	
		public void observeValue(double v) {
			double nv;
			
			sum = sum+v;
			qsum = qsum + v*v; 
			n = points.size() ;
			
			nv = 1.0*n ;
			if (v==0)zeroCounts++;
			mean = sum/nv ;
			if (mean>0)variance = qsum /nv - (mean)*(mean) ;
			stdev = Math.sqrt( variance ) ;
			if (mean>0)coeffOfVariance = variance/(mean) ;
		}
    	
    	public double getVariance(){
    		return variance;
    	}
    	
    	public double getStdDev(){
    		 
    		return stdev;
    	}
    	
    }
    
    /**
     * Get the points contained in the cluster.
     * @return points contained in the cluster
     */
    public List<T> getPoints() {
        return points;
    }

    /**
     * Get the point chosen to be the center of this cluster.
     * @return chosen cluster center
     */
    public T getCenter() {
        return center;
    }

	/**
	 * @param center the center to set
	 */
	public void setCenter(T center) {
		this.center = center;
	}


	public int getLength() {
		 
		return pointLen;
	}


	public double getVarianceOfField( int index){
		double v = -1;
		FieldVariance fieldVar ;
		
		if ((fieldVariances!=null) && (index<fieldVariances.size()) && (index>=0)){
			fieldVar = fieldVariances.get(index);
			v = fieldVar.variance ;
		}
		return v;
	}

	public double getMeanOfField( int index){
		double v = -1;
		
		if ((fieldVariances!=null) && (index<fieldVariances.size()) && (index>=0)){
			v = fieldVariances.get(index).mean;
		}
		return v;
	}
	public double getStdDevOfField( int index){
		double v = -1;
		
		if ((fieldVariances!=null) && (index<fieldVariances.size()) && (index>=0)){
			v = fieldVariances.get(index).stdev;
		}
		return v;
	}
	public double getCoVarOfField( int index){
		double v = -1;
		
		if ((fieldVariances!=null) && (index<fieldVariances.size()) && (index>=0)){
			v = fieldVariances.get(index).coeffOfVariance;
		}
		return v;
	}
	
	public double getVarOfVar() {
		return varianceOfVariances ;
	}
	public double getVariabilityScore() {
		return variabilityScore ;
	}
	
	   
	
	public int getRepresentativePoint(){
		 
		return representativePointIndex;
	}
 

}