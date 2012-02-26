package org.NooLab.stats.function;

import org.NooLab.stats.analysis.BivariateFunction;

/**
 * Divide the first operand by the second.
 *
 * @version $Id$
 * @since 3.0
 */
public class Divide implements BivariateFunction {
    /** {@inheritDoc} */
    public double value(double x, double y) {
        return x / y;
    }
}
