package org.NooLab.math3.function;

import org.NooLab.math3.analysis.BivariateFunction;

/**
 * Add the two operands.
 *
 * @version $Id$
 * @since 3.0
 */
public class Add implements BivariateFunction {
    /** {@inheritDoc} */
    public double value(double x, double y) {
        return x + y;
    }
}
