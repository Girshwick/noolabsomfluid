package org.NooLab.stats.function;

import org.NooLab.stats.analysis.BivariateFunction;

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
