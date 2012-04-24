package org.NooLab.math3.stat.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.NooLab.math3.stat.MissingValue;
import org.NooLab.math3.stat.MissingValueIntf;
import org.NooLab.math3.util.MathArrays;

public class EuclideanDoublePoint implements Serializable,
											 Clusterable<EuclideanDoublePoint> {


	
    /** Serializable version identifier. */
    private static final long serialVersionUID = 3946024775784901369L;

    private long sId = 0;
    
    /** Point coordinates. */
    private double[] point;

    private int length = -1 ; 
    
    /**
     * Build an instance wrapping an integer array.
     * <p>The wrapped array is referenced, it is <em>not</em> copied.</p>
     * @param point the n-dimensional point in integer space
     */
    public EuclideanDoublePoint(final double[] point) {
        this.point = point;
    }

    /** {@inheritDoc} */
    public double distanceFrom( final EuclideanDoublePoint p) {
        return MathArrays.distance(point, p.getPoint());
    }

	@Override
	public double distanceFrom( final EuclideanDoublePoint p, 
								final int[] useIndicators, 
							    MissingValueIntf mv) {
		 
		double distanceValue = MathArrays.distance( point, p.getPoint(), useIndicators, mv );
		
		
		return distanceValue;
	}

	 

	
    @Override
	public double distanceFrom(	EuclideanDoublePoint p, 
								int[] usedColumns,
								MissingValueIntf missingValue, 
								int distanceMethod) {
	
    	
		double distanceValue = missingValue.getValue() ;
		  
		   
		if (distanceMethod== _DISTANCE_EUCLIDEAN){
			distanceValue = MathArrays.distance( point, p.getPoint(), usedColumns, missingValue );
		}
		if (distanceMethod == _DISTANCE_COMPOUND){
			distanceValue = MathArrays.compoundDistance( point, p.getPoint(), usedColumns, missingValue );
		}
		
		return distanceValue;
    }

	/** {@inheritDoc} */
    public EuclideanDoublePoint centroidOf(final Collection<EuclideanDoublePoint> points, int[] usedColumns, MissingValueIntf mv) {
 
    	int k;
    	double v;
    	double[] centroid = new double[getPoint().length];
        int[] mvCount = new int[getPoint().length];
    	 
        // TODO XXX : offer fork to multi-threaded parallel mode if there are many records
        
        // for all points in provided collection
    	for (EuclideanDoublePoint p : points) {
    		 
            for (int i = 0; i < centroid.length; i++) {
            	
            	v = p.getPoint()[i];
            	
            	// we have care for missing values, which we handle through an object
            	if (mv!=null) {
            		if (mv.isMissingValue(v)){
            			v=0;
            			mvCount[i]++ ;  
            		}
            	}
            	// contribution to centroid vector only if used  or the supervising field
            	// note that the supervising field ("target variable") is not used for distance calculations,
            	// yet, here we have to include it because we want to get the average value for the cluster
            	if ((usedColumns[i]>0)   || (usedColumns[i]==-2)){
            		centroid[i] += v;
            	} 
            } // -> all fields
        } // -> all points
    	
        for (int i = 0; i < centroid.length; i++) {
        	double denomin = points.size() - mvCount[i] ;
        	if ((usedColumns[i]>0)  || (usedColumns[i]==-2)){
        		centroid[i] = centroid[i] / denomin ;
        	}else{
        		centroid[i] = 0; // or MV?
        	}
        }
        return new EuclideanDoublePoint(centroid);
    }

    
    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof EuclideanIntegerPoint)) {
            return false;
        }
        final int[] otherPoint = ((EuclideanIntegerPoint) other).getPoint();
        if (point.length != otherPoint.length) {
            return false;
        }
        for (int i = 0; i < point.length; i++) {
            if (point[i] != otherPoint[i]) {
                return false;
            }
        }
        return true;
    }

    
    public int hashCode() {
        int hashCode = 0;
        for (Double i : point) {
            hashCode = hashCode + i.hashCode() * 13 + 7;
        }
        /*
         *  for (Integer i : point) {
            	hashCode += i.hashCode() * 13 + 7;
        	}
         */
        return hashCode;
    }

   
	public String toString() {
	    final StringBuilder buff = new StringBuilder("(");
	    final double[] coordinates = getPoint();
	    for (int i = 0; i < coordinates.length; i++) {
	        buff.append(coordinates[i]);
	        if (i < coordinates.length - 1) {
	            buff.append(",");
	        }
	    }
	    buff.append(")");
	    return buff.toString();
	}

	/**
	 * Get the n-dimensional point in integer space.
	 * @return a reference (not a copy!) to the wrapped array
	 */
	public double[] getPoint() {
	    return point;
	}

	 
	public int getLength() {
		length = point.length ;
		return length;
	}

	@Override
	public double[] getValues() {
		double [] v = new double[ point.length] ;
		
		for (int i=0;i<point.length;i++){
			v[i] = (double)(1.0*point[i]);
		}
		return v;
	}

	public void setValues(double[] values) {
		point = values;
	}

	/**
	 * @return the sId
	 */
	public long getSId() {
		return sId;
	}

	/**
	 * @param sId the sId to set
	 */
	public void setSId(long id) {
		this.sId = id;
	}

	 

	 

 
	 

}