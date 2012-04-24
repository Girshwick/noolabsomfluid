package org.NooLab.math3.filter;

import org.NooLab.math3.linear.RealMatrix;

 

/**
 * Defines the measurement model for the use with a {@link KalmanFilter}.
 *
 * @since 3.0
 * @version $Id$
 */
public interface MeasurementModel {
    /**
     * Returns the measurement matrix.
     *
     * @return the measurement matrix
     */
    RealMatrix getMeasurementMatrix();

    /**
     * Returns the measurement noise matrix. This method is called by the
     * {@link KalmanFilter} every correct step, so implementations of this
     * interface may return a modified measurement noise depending on current
     * iteration step.
     *
     * @return the measurement noise matrix
     * @see KalmanFilter#correct(double[])
     * @see KalmanFilter#correct(org.apache.commons.math3.linear.RealVector)
     */
    RealMatrix getMeasurementNoise();
}
