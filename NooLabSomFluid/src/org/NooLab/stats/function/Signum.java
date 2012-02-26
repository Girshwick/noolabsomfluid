package org.NooLab.stats.function;

import org.NooLab.stats.analysis.UnivariateFunction;
import org.NooLab.stats.util.FastMath;

/**
 * {@code signum} function.
 *
 * @version $Id$
 * @since 3.0
 */
public class Signum implements UnivariateFunction {
    /** {@inheritDoc} */
    public double value(double x) {
        return FastMath.signum(x);
    }
}
