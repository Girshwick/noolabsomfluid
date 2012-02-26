package org.NooLab.stats.function;

import org.NooLab.stats.analysis.DifferentiableUnivariateFunction;
import org.NooLab.stats.analysis.ParametricUnivariateFunction;
import org.NooLab.stats.analysis.UnivariateFunction;
import org.NooLab.stats.exception.DimensionMismatchException;
import org.NooLab.stats.exception.NullArgumentException;
import org.NooLab.stats.util.FastMath;


/**
 * <a href="http://en.wikipedia.org/wiki/Sigmoid_function">
 *  Sigmoid</a> function.
 * It is the inverse of the {@link Logit logit} function.
 * A more flexible version, the generalised logistic, is implemented
 * by the {@link Logistic} class.
 *
 * @version $Id$
 * @since 3.0
 */
public class Sigmoid implements DifferentiableUnivariateFunction {
    /** Lower asymptote. */
    private final double lo;
    /** Higher asymptote. */
    private final double hi;

    /**
     * Usual sigmoid function, where the lower asymptote is 0 and the higher
     * asymptote is 1.
     */
    public Sigmoid() {
        this(0, 1);
    }

    /**
     * Sigmoid function.
     *
     * @param lo Lower asymptote.
     * @param hi Higher asymptote.
     */
    public Sigmoid(double lo,
                   double hi) {
        this.lo = lo;
        this.hi = hi;
    }

    /** {@inheritDoc} */
    public UnivariateFunction derivative() {
        return new UnivariateFunction() {
            /** {@inheritDoc} */
            public double value(double x) {
                final double exp = FastMath.exp(-x);
                if (Double.isInfinite(exp)) {
                    // Avoid returning NaN in case of overflow.
                    return 0;
                }
                final double exp1 = 1 + exp;
                return (hi - lo) * exp / (exp1 * exp1);
            }
        };
    }

    /** {@inheritDoc} */
    public double value(double x) {
        return value(x, lo, hi);
    }

    /**
     * Parametric function where the input array contains the parameters of
     * the logit function, ordered as follows:
     * <ul>
     *  <li>Lower asymptote</li>
     *  <li>Higher asymptote</li>
     * </ul>
     */
    public static class Parametric implements ParametricUnivariateFunction {
        /**
         * Computes the value of the sigmoid at {@code x}.
         *
         * @param x Value for which the function must be computed.
         * @param param Values of lower asymptote and higher asymptote.
         * @return the value of the function.
         * @throws NullArgumentException if {@code param} is {@code null}.
         * @throws DimensionMismatchException if the size of {@code param} is
         * not 2.
         */
        public double value(double x, double ... param) {
            validateParameters(param);
            return Sigmoid.value(x, param[0], param[1]);
        }

        /**
         * Computes the value of the gradient at {@code x}.
         * The components of the gradient vector are the partial
         * derivatives of the function with respect to each of the
         * <em>parameters</em> (lower asymptote and higher asymptote).
         *
         * @param x Value at which the gradient must be computed.
         * @param param Values for lower asymptote and higher asymptote.
         * @return the gradient vector at {@code x}.
         * @throws NullArgumentException if {@code param} is {@code null}.
         * @throws DimensionMismatchException if the size of {@code param} is
         * not 2.
         */
        public double[] gradient(double x, double ... param) {
            validateParameters(param);

            final double invExp1 = 1 / (1 + FastMath.exp(-x));

            return new double[] { 1 - invExp1, invExp1 };
        }

        /**
         * Validates parameters to ensure they are appropriate for the evaluation of
         * the {@link #value(double,double[])} and {@link #gradient(double,double[])}
         * methods.
         *
         * @param param Values for lower and higher asymptotes.
         * @throws NullArgumentException if {@code param} is {@code null}.
         * @throws DimensionMismatchException if the size of {@code param} is
         * not 2.
         */
        private void validateParameters(double[] param) {
            if (param == null) {
                throw new NullArgumentException();
            }
            if (param.length != 2) {
                throw new DimensionMismatchException(param.length, 2);
            }
        }
    }

    /**
     * @param x Value at which to compute the sigmoid.
     * @param lo Lower asymptote.
     * @param hi Higher asymptote.
     * @return the value of the sigmoid function at {@code x}.
     */
    private static double value(double x,
                                double lo,
                                double hi) {
        return lo + (hi - lo) / (1 + FastMath.exp(-x));
    }
}
