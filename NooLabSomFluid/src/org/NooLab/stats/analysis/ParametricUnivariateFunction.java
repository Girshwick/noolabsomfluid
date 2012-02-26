package org.NooLab.stats.analysis;

/**
 * An interface representing a real function that depends on one independent
 * variable plus some extra parameters.
 *
 * @since 3.0
 * @version $Id$
 */
public interface ParametricUnivariateFunction {
    /**
     * Compute the value of the function.
     *
     * @param x Point for which the function value should be computed.
     * @param parameters Function parameters.
     * @return the value.
     */
    double value(double x, double ... parameters);

    /**
     * Compute the gradient of the function with respect to its parameters.
     *
     * @param x Point for which the function value should be computed.
     * @param parameters Function parameters.
     * @return the value.
     */
    double[] gradient(double x, double ... parameters);
}
