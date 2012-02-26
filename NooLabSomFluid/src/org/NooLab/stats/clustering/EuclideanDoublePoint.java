package org.NooLab.stats.clustering;

import java.io.Serializable;
import java.util.Collection;

import org.NooLab.stats.util.MathArrays;

public class EuclideanDoublePoint implements Serializable,
											 Clusterable<EuclideanDoublePoint> {


    /** Serializable version identifier. */
    private static final long serialVersionUID = 3946024775784901369L;

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
	public double distanceFrom( final EuclideanDoublePoint p, final int[] useIndicators, MissingValue mv) {
		 
		double distanceValue = MathArrays.distance( point, p.getPoint(), useIndicators );
		
		return distanceValue;
	}
	
	 
	
	
    /** {@inheritDoc} */
    public EuclideanDoublePoint centroidOf(final Collection<EuclideanDoublePoint> points, MissingValue mv) {
    	int z=0;
    	double[] centroid = new double[getPoint().length];
        
    	for (EuclideanDoublePoint p : points) {
            for (int i = 0; i < centroid.length; i++) {
            	// we should care for missing values, which we handle through an object
            	
            	centroid[i] += p.getPoint()[i];
                z++;
            }
        }
    	
        for (int i = 0; i < centroid.length; i++) {
            centroid[i] /= points.size();
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

	 

 
	 

}