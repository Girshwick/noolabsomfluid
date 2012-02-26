package org.NooLab.stats.function;

import org.NooLab.stats.analysis.DifferentiableUnivariateFunction;
import org.NooLab.stats.analysis.UnivariateFunction;


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
