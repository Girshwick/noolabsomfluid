package org.NooLab.math3.stat.clustering;

import java.io.Serializable;
import java.util.Collection;

import org.NooLab.math3.stat.MissingValue;
import org.NooLab.math3.stat.MissingValueIntf;
import org.NooLab.math3.util.MathArrays;

 

/**
 * A simple implementation of {@link Clusterable} for points with integer coordinates.
 * @version $Id$
 * @since 2.0
 */
public class EuclideanIntegerPoint implements Clusterable<EuclideanIntegerPoint>, Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 3946024775784901369L;

    /** Point coordinates. */
    private final int[] point;

    /**
     * Build an instance wrapping an integer array.
     * <p>The wrapped array is referenced, it is <em>not</em> copied.</p>
     * @param point the n-dimensional point in integer space
     */
    public EuclideanIntegerPoint(final int[] point) {
        this.point = point;
    }

    /**
     * Get the n-dimensional point in integer space.
     * @return a reference (not a copy!) to the wrapped array
     */
    public int[] getPoint() {
        return point;
    }

    /** {@inheritDoc} */
    public double distanceFrom(final EuclideanIntegerPoint p) {
        return MathArrays.distance(point, p.getPoint());
    }

	 
    @Override
	public double distanceFrom( EuclideanIntegerPoint p, int[] inactiveColumns,
								MissingValueIntf missingValue) {
		 
		return 0;
	}

	@Override
	public double distanceFrom( EuclideanIntegerPoint p, int[] usedColumns,
								MissingValueIntf missingValue, int distanceMethod) {
		 
		return distanceFrom( p, usedColumns,missingValue) ;
	}

	/** {@inheritDoc} */
    public EuclideanIntegerPoint centroidOf(final Collection<EuclideanIntegerPoint> points) {
        int[] centroid = new int[getPoint().length];
        for (EuclideanIntegerPoint p : points) {
            for (int i = 0; i < centroid.length; i++) {
                centroid[i] += p.getPoint()[i];
            }
        }
        for (int i = 0; i < centroid.length; i++) {
            centroid[i] /= points.size();
        }
        return new EuclideanIntegerPoint(centroid);
    }

    public EuclideanIntegerPoint centroidOf(final Collection<EuclideanIntegerPoint> points, int[] usedColumns, MissingValueIntf mv) {
        
       	int v;
    	int[] centroid = new int[getPoint().length];
        int[] mvCount = new int[getPoint().length];
    	
    	for (EuclideanIntegerPoint p : points) {
    		 
    		
            for (int i = 0; i < centroid.length; i++) {
            	// we should care for missing values, which we handle through an object
            	v = p.getPoint()[i];
            	
            	if (mv!=null){
            		if (mv.isMissingValue(v)){
            			v=0;
            			mvCount[i]++ ;  
            		}
            	}
            	centroid[i] += v;
                 
            }
        }
    	
        for (int i = 0; i < centroid.length; i++) {
        	double denomin = points.size() - mvCount[i];
            centroid[i] = (int) (centroid[i] / denomin) ;
        }


    	
        return new EuclideanIntegerPoint(centroid);
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

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hashCode = 0;
        for (Integer i : point) {
            hashCode += i.hashCode() * 13 + 7;
        }
        return hashCode;
    }

    /**
     * {@inheritDoc}
     * @since 2.1
     */
    @Override
    public String toString() {
        final StringBuilder buff = new StringBuilder("(");
        final int[] coordinates = getPoint();
        for (int i = 0; i < coordinates.length; i++) {
            buff.append(coordinates[i]);
            if (i < coordinates.length - 1) {
                buff.append(",");
            }
        }
        buff.append(")");
        return buff.toString();
    }

	 

	@Override
	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double[] getValues() {
		double [] v = new double[ point.length] ;
		
		for (int i=0;i<point.length;i++){
			v[i] = (double)(1.0*point[i]);
		}
		return v;
	}

	@Override
	public long getSId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSId(long id) {
		// TODO Auto-generated method stub
		
	}

	 


}