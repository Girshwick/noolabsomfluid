package org.NooLab.stats.function;

import org.NooLab.stats.analysis.DifferentiableUnivariateFunction;

/**
 * Constant function.
 *
 * @version $Id$
 * @since 3.0
 */
public class Constant implements DifferentiableUnivariateFunction {
    /** Constant. */
    private final double c;

    /**
     * @param c Constant.
     */
    public Constant(double c) {
        this.c = c;
    }

    /** {@inheritDoc} */
    public double value(double x) {
        return c;
    }

    /** {@inheritDoc} */
    public DifferentiableUnivariateFunction derivative() {
        return new Constant(0);
    }
}