package org.NooLab.math3.distribution;

import org.NooLab.math3.exception.LocalizedFormats;
import org.NooLab.math3.exception.NumberIsTooLargeException;

/**
 * Implementation of the uniform integer distribution.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Uniform_distribution_(discrete)"
 * >Uniform distribution (discrete), at Wikipedia</a>
 *
 * @version $Id$
 * @since 3.0
 */
public class UniformIntegerDistribution extends AbstractIntegerDistribution {
    /** Serializable version identifier. */
    private static final long serialVersionUID = 20120109L;

    /** Lower bound (inclusive) of this distribution. */
    private final int lower;

    /** Upper bound (inclusive) of this distribution. */
    private final int upper;

    /**
     * Creates a new uniform integer distribution using the given lower and
     * upper bounds (both inclusive).
     *
     * @param lower Lower bound (inclusive) of this distribution.
     * @param upper Upper bound (inclusive) of this distribution.
     * @throws NumberIsTooLargeException if {@code lower >= upper}.
     */
    public UniformIntegerDistribution(int lower, int upper) throws NumberIsTooLargeException {
        if (lower >= upper) {
            throw new NumberIsTooLargeException(
                            LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                            lower, upper, false);
        }
        this.lower = lower;
        this.upper = upper;
    }

    /** {@inheritDoc} */
    public double probability(int x) {
        if (x < lower || x > upper) {
            return 0;
        }
        return 1.0 / (upper - lower + 1);
    }

    /** {@inheritDoc} */
    public double cumulativeProbability(int x) {
        if (x < lower) {
            return 0;
        }
        if (x > upper) {
            return 1;
        }
        return (x - lower + 1.0) / (upper - lower + 1.0);
    }

    /**
     * {@inheritDoc}
     *
     * For lower bound {@code lower} and upper bound {@code upper}, the mean is
     * {@code 0.5 * (lower + upper)}.
     */
    public double getNumericalMean() {
        return 0.5 * (lower + upper);
    }

    /**
     * {@inheritDoc}
     *
     * For lower bound {@code lower} and upper bound {@code upper}, and
     * {@code n = upper - lower + 1}, the variance is {@code (n^2 - 1) / 12}.
     */
    public double getNumericalVariance() {
        double n = upper - lower + 1;
        return (n * n - 1) / 12.0;
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is equal to the lower bound parameter
     * of the distribution.
     *
     * @return lower bound of the support
     */
    public int getSupportLowerBound() {
        return lower;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is equal to the upper bound parameter
     * of the distribution.
     *
     * @return upper bound of the support
     */
    public int getSupportUpperBound() {
        return upper;
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

    /** {@inheritDoc} */
    @Override
    public int sample() {
        return randomData.nextInt(lower, upper);
    }
}