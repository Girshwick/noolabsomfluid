package org.NooLab.math3.function;

import org.NooLab.math3.analysis.UnivariateFunction;
import org.NooLab.math3.util.FastMath;

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
