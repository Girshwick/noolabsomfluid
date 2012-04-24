package org.NooLab.math3.function;

import org.NooLab.math3.analysis.DifferentiableUnivariateFunction;
import org.NooLab.math3.analysis.UnivariateFunction;


/**
 * Inverse function.
 *
 * @version $Id$
 * @since 3.0
 */
public class Inverse implements DifferentiableUnivariateFunction {
    /** {@inheritDoc} */
    public double value(double x) {
        return 1 / x;
    }

    /** {@inheritDoc} */
    public UnivariateFunction derivative() {
        return new UnivariateFunction() {
            /** {@inheritDoc} */
            public double value(double x) {
                return -1 / (x * x);
            }
        };
    }
}
