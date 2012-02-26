package org.NooLab.stats.clustering;


import java.util.Collection;

/**
 * Interface for points that can be clustered together.
 * @param <T> the type of point that can be clustered
 * @version $Id$
 * @since 2.0
 */
public interface Clusterable<T> {

    /**
     * Returns the distance from the given point.
     *
     * @param p the point to compute the distance from
     * @return the distance from the given point
     */
    double distanceFrom( T p );

    double distanceFrom( T p , int[] inactiveColumns, MissingValue mv );

    public int getLength() ;
    
    public double[] getValues() ; // a conversion from T to double[]
    
    /**
     * Returns the centroid of the given Collection of points.
     *
     * @param p the Collection of points to compute the centroid of
     * @return the centroid of the given Collection of Points
     */
    T centroidOf(Collection<T> p, MissingValue mv);

}