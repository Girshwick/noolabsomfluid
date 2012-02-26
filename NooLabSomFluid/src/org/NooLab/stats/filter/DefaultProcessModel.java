package org.NooLab.stats.filter;

import org.NooLab.stats.linear.Array2DRowRealMatrix;
import org.NooLab.stats.linear.ArrayRealVector;
import org.NooLab.stats.linear.RealMatrix;
import org.NooLab.stats.linear.RealVector;




/**
 * Default implementation of a {@link ProcessModel} for the use with a
 * {@link KalmanFilter}.
 *
 * @since 3.0
 * @version $Id$
 */
public class DefaultProcessModel implements ProcessModel {
    /**
     * The state transition matrix, used to advance the internal state
     * estimation each time-step.
     */
    private RealMatrix stateTransitionMatrix;

    /**
     * The control matrix, used to integrate a control input into the state
     * estimation.
     */
    private RealMatrix controlMatrix;

    /** The process noise covariance matrix. */
    private RealMatrix processNoiseCovMatrix;

    /** The initial state estimation of the observed process. */
    private RealVector initialStateEstimateVector;

    /** The initial error covariance matrix of the observed process. */
    private RealMatrix initialErrorCovMatrix;

    /**
     * Create a new {@link ProcessModel}, taking double arrays as input
     * parameters.
     *
     * @param stateTransition the state transition matrix
     * @param control the control matrix
     * @param processNoise the process noise matrix
     * @param initialStateEstimate the initial state estimate vector
     * @param initialErrorCovariance the initial error covariance matrix
     */
    public DefaultProcessModel(final double[][] stateTransition,
                               final double[][] control,
                               final double[][] processNoise,
                               final double[] initialStateEstimate,
                               final double[][] initialErrorCovariance) {
        this(new Array2DRowRealMatrix(stateTransition),
                new Array2DRowRealMatrix(control),
                new Array2DRowRealMatrix(processNoise),
                new ArrayRealVector(initialStateEstimate),
                new Array2DRowRealMatrix(initialErrorCovariance));
    }

    /**
     * Create a new {@link ProcessModel}, taking double arrays as input
     * parameters. The initial state estimate and error covariance are omitted
     * and will be initialized by the {@link KalmanFilter} to default values.
     *
     * @param stateTransition the state transition matrix
     * @param control the control matrix
     * @param processNoise the process noise matrix
     */
    public DefaultProcessModel(final double[][] stateTransition,
                               final double[][] control,
                               final double[][] processNoise) {
        this(new Array2DRowRealMatrix(stateTransition),
                new Array2DRowRealMatrix(control),
                new Array2DRowRealMatrix(processNoise), null, null);
    }

    /**
     * Create a new {@link ProcessModel}, taking double arrays as input
     * parameters.
     *
     * @param stateTransition the state transition matrix
     * @param control the control matrix
     * @param processNoise the process noise matrix
     * @param initialStateEstimate the initial state estimate vector
     * @param initialErrorCovariance the initial error covariance matrix
     */
    public DefaultProcessModel(final RealMatrix stateTransition,
                               final RealMatrix control,
                               final RealMatrix processNoise,
                               final RealVector initialStateEstimate,
                               final RealMatrix initialErrorCovariance) {
        this.stateTransitionMatrix = stateTransition;
        this.controlMatrix = control;
        this.processNoiseCovMatrix = processNoise;
        this.initialStateEstimateVector = initialStateEstimate;
        this.initialErrorCovMatrix = initialErrorCovariance;
    }

    /** {@inheritDoc} */
    public RealMatrix getStateTransitionMatrix() {
        return stateTransitionMatrix;
    }

    /** {@inheritDoc} */
    public RealMatrix getControlMatrix() {
        return controlMatrix;
    }

    /** {@inheritDoc} */
    public RealMatrix getProcessNoise() {
        return processNoiseCovMatrix;
    }

    /** {@inheritDoc} */
    public RealVector getInitialStateEstimate() {
        return initialStateEstimateVector;
    }

    /** {@inheritDoc} */
    public RealMatrix getInitialErrorCovariance() {
        return initialErrorCovMatrix;
    }
}
