package org.NooLab.math3.distribution;

import org.NooLab.math3.exception.LocalizedFormats;
import org.NooLab.math3.exception.NotStrictlyPositiveException;
import org.NooLab.math3.exception.OutOfRangeException;
import org.NooLab.math3.util.ArithmeticUtils;
import org.NooLab.math3.util.FastMath;

/**
 * <p>
 * Implementation of the Pascal distribution. The Pascal distribution is a
 * special case of the Negative Binomial distribution where the number of
 * successes parameter is an integer.
 * </p>
 * <p>
 * There are various ways to express the probability mass and distribution
 * functions for the Pascal distribution. The present implementation represents
 * the distribution of the number of failures before {@code r} successes occur.
 * This is the convention adopted in e.g.
 * <a href="http://mathworld.wolfram.com/NegativeBinomialDistribution.html">MathWorld</a>,
 * but <em>not</em> in
 * <a href="http://en.wikipedia.org/wiki/Negative_binomial_distribution">Wikipedia</a>.
 * </p>
 * <p>
 * For a random variable {@code X} whose values are distributed according to this
 * distribution, the probability mass function is given by<br/>
 * {@code P(X = k) = C(k + r - 1, r - 1) * p^r * (1 - p)^k,}<br/>
 * where {@code r} is the number of successes, {@code p} is the probability of
 * success, and {@code X} is the total number of failures. {@code C(n, k)} is
 * the binomial coefficient ({@code n} choose {@code k}). The mean and variance
 * of {@code X} are<br/>
 * {@code E(X) = (1 - p) * r / p, var(X) = (1 - p) * r / p^2.}<br/>
 * Finally, the cumulative distribution function is given by<br/>
 * {@code P(X <= k) = I(p, r, k + 1)},
 * where I is the regularized incomplete Beta function.
 * </p>
 *
 * @see <a href="http://en.wikipedia.org/wiki/Negative_binomial_distribution">
 * Negative binomial distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/NegativeBinomialDistribution.html">
 * Negative binomial distribution (MathWorld)</a>
 * @version $Id$
 * @since 1.2 (changed to concrete class in 3.0)
 */
public class PascalDistribution extends AbstractIntegerDistribution {
    /** Serializable version identifier. */
    private static final long serialVersionUID = 6751309484392813623L;
    /** The number of successes. */
    private final int numberOfSuccesses;
    /** The probability of success. */
    private final double probabilityOfSuccess;

    /**
     * Create a Pascal distribution with the given number of successes and
     * probability of success.
     *
     * @param r Number of successes.
     * @param p Probability of success.
     * @throws NotStrictlyPositiveException if the number of successes is not positive
     * @throws OutOfRangeException if the probability of success is not in the
     * range [0, 1]
     */
    public PascalDistribution(int r, double p)
        throws NotStrictlyPositiveException, OutOfRangeException {
        if (r <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SUCCESSES,
                                           r);
        }
        if (p < 0 || p > 1) {
            throw new OutOfRangeException(p, 0, 1);
        }

        numberOfSuccesses = r;
        probabilityOfSuccess = p;
    }

    /**
     * Access the number of successes for this distribution.
     *
     * @return the number of successes.
     */
    public int getNumberOfSuccesses() {
        return numberOfSuccesses;
    }

    /**
     * Access the probability of success for this distribution.
     *
     * @return the probability of success.
     */
    public double getProbabilityOfSuccess() {
        return probabilityOfSuccess;
    }

    /** {@inheritDoc} */
    public double probability(int x) {
        double ret;
        if (x < 0) {
            ret = 0.0;
        } else {
            ret = ArithmeticUtils.binomialCoefficientDouble(x +
                  numberOfSuccesses - 1, numberOfSuccesses - 1) *
                  FastMath.pow(probabilityOfSuccess, numberOfSuccesses) *
                  FastMath.pow(1.0 - probabilityOfSuccess, x);
        }
        return ret;
    }

    /** {@inheritDoc} */
    public double cumulativeProbability(int x) {
        double ret;
        if (x < 0) {
            ret = 0.0;
        } else {
            ret = Beta.regularizedBeta(probabilityOfSuccess,
                    numberOfSuccesses, x + 1);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * For number of successes {@code r} and probability of success {@code p},
     * the mean is {@code r * (1 - p) / p}.
     */
    public double getNumericalMean() {
        final double p = getProbabilityOfSuccess();
        final double r = getNumberOfSuccesses();
        return (r * (1 - p)) / p;
    }

    /**
     * {@inheritDoc}
     *
     * For number of successes {@code r} and probability of success {@code p},
     * the variance is {@code r * (1 - p) / p^2}.
     */
    public double getNumericalVariance() {
        final double p = getProbabilityOfSuccess();
        final double r = getNumberOfSuccesses();
        return r * (1 - p) / (p * p);
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always 0 no matter the parameters.
     *
     * @return lower bound of the support (always 0)
     */
    public int getSupportLowerBound() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is always positive infinity no matter the
     * parameters. Positive infinity is symbolized by {@code Integer.MAX_VALUE}.
     *
     * @return upper bound of the support (always {@code Integer.MAX_VALUE}
     * for positive infinity)
     */
    public int getSupportUpperBound() {
        return Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     *
     * The support of this distribution is connected.
     *
     * @return {@code true}
     */
    public boolean isSupportConnected() {
        return true;
    }
}