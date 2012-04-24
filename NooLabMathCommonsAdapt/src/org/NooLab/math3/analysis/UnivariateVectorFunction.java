package org.NooLab.math3.analysis;

/**
 * An interface representing a univariate vectorial function.
 *
 * @version $Id$
 * @since 2.0
 */
public interface UnivariateVectorFunction {

    /**
     * Compute the value for the function.
     * @param x the point for which the function value should be computed
     * @return the value
     */
    double[] value(double x);

}