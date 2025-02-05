package org.NooLab.math3.filter;

import org.NooLab.math3.linear.RealMatrix;
import org.NooLab.math3.linear.RealVector;
 

/**
 * Defines the process dynamics model for the use with a {@link KalmanFilter}.
 *
 * @since 3.0
 * @version $Id$
 */
public interface ProcessModel {
    /**
     * Returns the state transition matrix.
     *
     * @return the state transition matrix
     */
    RealMatrix getStateTransitionMatrix();

    /**
     * Returns the control matrix.
     *
     * @return the control matrix
     */
    RealMatrix getControlMatrix();

    /**
     * Returns the process noise matrix. This method is called by the
     * {@link KalmanFilter} every predict step, so implementations of this
     * interface may return a modified process noise depending on current
     * iteration step.
     *
     * @return the process noise matrix
     * @see KalmanFilter#predict()
     * @see KalmanFilter#predict(double[])
     * @see KalmanFilter#predict(RealVector)
     */
    RealMatrix getProcessNoise();

    /**
     * Returns the initial state estimation vector.
     * <p>
     * Note: if the return value is zero, the Kalman filter will initialize the
     * state estimation with a zero vector.
     * </p>
     *
     * @return the initial state estimation vector
     */
    RealVector getInitialStateEstimate();

    /**
     * Returns the initial error covariance matrix.
     * <p>
     * Note: if the return value is zero, the Kalman filter will initialize the
     * error covariance with the process noise matrix.
     * </p>
     *
     * @return the initial error covariance matrix
     */
    RealMatrix getInitialErrorCovariance();
}
