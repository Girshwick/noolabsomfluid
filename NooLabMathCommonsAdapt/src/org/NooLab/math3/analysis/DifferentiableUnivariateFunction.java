package org.NooLab.math3.analysis;

/**
 * Extension of {@link UnivariateFunction} representing a differentiable univariate real function.
 *
 * @version $Id$
 */
public interface DifferentiableUnivariateFunction
    extends UnivariateFunction {

    /**
     * Returns the derivative of the function
     *
     * @return  the derivative function
     */
    UnivariateFunction derivative();

}