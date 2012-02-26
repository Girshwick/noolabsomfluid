package org.NooLab.stats.function;

import org.NooLab.stats.analysis.DifferentiableUnivariateFunction;

/**
 * Identity function.
 *
 * @version $Id$
 * @since 3.0
 */
public class Identity implements DifferentiableUnivariateFunction {
    /** {@inheritDoc} */
    public double value(double x) {
        return x;
    }

    /** {@inheritDoc} */
    public DifferentiableUnivariateFunction derivative() {
        return new Constant(1);
    }
}