package org.NooLab.math3.stat.clustering;


import java.util.Collection;

import org.NooLab.math3.stat.MissingValue;
import org.NooLab.math3.stat.MissingValueIntf;

/**
 * Interface for points that can be clustered together.
 * @param <T> the type of point that can be clustered
 * @version $Id$
 * @since 2.0
 */
public interface Clusterable<T> {

	public static final int _DISTANCE_EUCLIDEAN = 0;
	public static final int _DISTANCE_COMPOUND  = 1;
	
    /**
     * Returns the distance from the given point.
     *
     * @param p the point to compute the distance from
     * @return the distance from the given point
     */
    double distanceFrom( T p );

    /** Returns the distance from the given point, respects missing values and the use indicators  */
    double distanceFrom( T p , int[] usedColumns, MissingValueIntf missingValue );

    double distanceFrom( T p , int[] usedColumns, MissingValueIntf missingValue,int distanceMethod );
    
    
    /** total number of fields in the data vector (no distinction is made whether the field is used or not) */
    public int getLength() ;
    
    public double[] getValues() ; // a conversion from T to double[]
    
    /**
     * Returns the centroid of the given Collection of points.
     *
     * @param p the Collection of points to compute the centroid of
     * @return the centroid of the given Collection of Points
     */
    public T centroidOf(Collection<T> p, int[] usedColumns, MissingValueIntf missingValue);

    /**  a serial ID of the point */
    public long getSId();
    
	/**  a serial ID of the point */
    public void setSId(long id) ;
    
}