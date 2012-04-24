package org.NooLab.math3.function;

import org.NooLab.math3.analysis.BivariateFunction;

/**
 * Subtract the second operand from the first.
 *
 * @version $Id$
 * @since 3.0
 */
public class Subtract implements BivariateFunction {
    /** {@inheritDoc} */
    public double value(double x, double y) {
        return x - y;
    }
}
