package org.NooLab.stats.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Cluster holding a set of {@link Clusterable} points.
 * @param <T> the type of points that can be clustered
 * @version $Id$
 * @since 2.0
 */
public class Cluster<T extends Clusterable<T>> implements Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = -3442297081515880464L;

    /** The points contained in this cluster. */
    private final List<T> points;

    private int pointLen = -1;
    
    /** Center of the cluster. */
    private final T center;

    /**
     * Build a cluster centered at a specified point.
     * @param center the point which is to be the center of this cluster
     */
    public Cluster(final T center) {
        this.center = center;
        points = new ArrayList<T>();
    }

    /**
     * Add a point to this cluster.
     * @param point point to add
     */
    public void addPoint(final T point) {
        points.add(point);
        pointLen = point.getLength() ;
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

	public int getLength() {
		 
		return pointLen;
	}

	 

}