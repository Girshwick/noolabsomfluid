package org.NooLab.stats.analysis;

/**
 * An interface representing a bivariate real function.
 *
 * @since 2.1
 * @version $Id$
 */
public interface BivariateFunction {
    /**
     * Compute the value for the function.
     *
     * @param x Abscissa for which the function value should be computed.
     * @param y Ordinate for which the function value should be computed.
     * @return the value.
     */
    double value(double x, double y);

}
