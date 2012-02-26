package org.NooLab.stats.function;

import org.NooLab.stats.analysis.BivariateFunction;

/**
 * Multiply the two operands.
 *
 * @version $Id$
 * @since 3.0
 */
public class Multiply implements BivariateFunction {
    /** {@inheritDoc} */
    public double value(double x, double y) {
        return x * y;
    }
}
