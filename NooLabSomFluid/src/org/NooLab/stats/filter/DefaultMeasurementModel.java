package org.NooLab.stats.filter;

import org.NooLab.stats.linear.Array2DRowRealMatrix;
import org.NooLab.stats.linear.RealMatrix;

 

/**
 * Default implementation of a {@link MeasurementModel} for the use with a
 * {@link KalmanFilter}.
 *
 * @since 3.0
 * @version $Id$
 */
public class DefaultMeasurementModel implements MeasurementModel {

    /**
     * The measurement matrix, used to associate the measurement vector to the
     * internal state estimation vector.
     */
    private RealMatrix measurementMatrix;

    /**
     * The measurement noise covariance matrix.
     */
    private RealMatrix measurementNoise;

    /**
     * Create a new {@link MeasurementModel}, taking double arrays as input
     * parameters for the respective measurement matrix and noise.
     *
     * @param measMatrix the measurement matrix
     * @param measNoise the measurement noise matrix
     */
    public DefaultMeasurementModel( final double[][] measMatrix,
            						final double[][] measNoise) {
    	
        this( new Array2DRowRealMatrix(measMatrix), new Array2DRowRealMatrix(measNoise));
    }

    /**
     * Create a new {@link MeasurementModel}, taking {@link RealMatrix} objects
     * as input parameters for the respective measurement matrix and noise.
     *
     * @param measMatrix the measurement matrix
     * @param measNoise the measurement noise matrix
     */
    public DefaultMeasurementModel(final RealMatrix measMatrix,
            final RealMatrix measNoise) {
        this.measurementMatrix = measMatrix;
        this.measurementNoise = measNoise;
    }

    /** {@inheritDoc} */
    public RealMatrix getMeasurementMatrix() {
        return measurementMatrix;
    }

    /** {@inheritDoc} */
    public RealMatrix getMeasurementNoise() {
        return measurementNoise;
    }
}
