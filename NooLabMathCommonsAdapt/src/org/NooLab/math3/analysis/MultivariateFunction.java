package org.NooLab.math3.analysis;

/**
 * An interface representing a multivariate real function.
 *
 * @version $Id$
 * @since 2.0
 */
public interface MultivariateFunction {

    /**
     * Compute the value for the function at the given point.
     *
     * @param point Point at which the function must be evaluated.
     * @return the function value for the given point.
     * @throws org.apache.commons.math3.exception.DimensionMismatchException
     * if the parameter's dimension is wrong for the function being evaluated.
     * @throws  org.apache.commons.math3.exception.MathIllegalArgumentException
     * when the activated method itself can ascertain that preconditions,
     * specified in the API expressed at the level of the activated method,
     * have been violated.  In the vast majority of cases where Commons Math
     * throws this exception, it is the result of argument checking of actual
     * parameters immediately passed to a method.
     */
    double value(double[] point);
}