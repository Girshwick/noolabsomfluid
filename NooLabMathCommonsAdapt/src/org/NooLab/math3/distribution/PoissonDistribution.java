package org.NooLab.math3.distribution;

import org.NooLab.math3.exception.LocalizedFormats;
import org.NooLab.math3.exception.NotStrictlyPositiveException;
import org.NooLab.math3.util.FastMath;
import org.NooLab.math3.util.MathUtils;

/**
 * Implementation of the Poisson distribution.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Poisson_distribution">Poisson distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/PoissonDistribution.html">Poisson distribution (MathWorld)</a>
 * @version $Id$
 */
public class PoissonDistribution extends AbstractIntegerDistribution {
    /**
     * Default maximum number of iterations for cumulative probability calculations.
     * @since 2.1
     */
    public static final int DEFAULT_MAX_ITERATIONS = 10000000;

    /**
     * Default convergence criterion.
     * @since 2.1
     */
    public static final double DEFAULT_EPSILON = 1e-12;

    /** Serializable version identifier. */
    private static final long serialVersionUID = -3349935121172596109L;

    /** Distribution used to compute normal approximation. */
    private final NormalDistribution normal;

    /** Mean of the distribution. */
    private final double mean;

    /**
     * Maximum number of iterations for cumulative probability. Cumulative
     * probabilities are estimated using either Lanczos series approximation of
     * {@link Gamma#regularizedGammaP(double, double, double, int)}
     * or continued fraction approximation of
     * {@link Gamma#regularizedGammaQ(double, double, double, int)}.
     */
    private final int maxIterations;

    /** Convergence criterion for cumulative probability. */
    private final double epsilon;

    /**
     * Creates a new Poisson distribution with specified mean.
     *
     * @param p the Poisson mean
     * @throws NotStrictlyPositiveException if {@code p <= 0}.
     */
    public PoissonDistribution(double p) throws NotStrictlyPositiveException {
        this(p, DEFAULT_EPSILON, DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Creates a new Poisson distribution with specified mean, convergence
     * criterion and maximum number of iterations.
     *
     * @param p Poisson mean.
     * @param epsilon Convergence criterion for cumulative probabilities.
     * @param maxIterations the maximum number of iterations for cumulative
     * probabilities.
     * @throws NotStrictlyPositiveException if {@code p <= 0}.
     * @since 2.1
     */
    public PoissonDistribution(double p, double epsilon, int maxIterations)
    throws NotStrictlyPositiveException {
        if (p <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.MEAN, p);
        }
        mean = p;
        normal = new NormalDistribution(p, FastMath.sqrt(p));
        this.epsilon = epsilon;
        this.maxIterations = maxIterations;
    }

    /**
     * Creates a new Poisson distribution with the specified mean and
     * convergence criterion.
     *
     * @param p Poisson mean.
     * @param epsilon Convergence criterion for cumulative probabilities.
     * @throws NotStrictlyPositiveException if {@code p <= 0}.
     * @since 2.1
     */
    public PoissonDistribution(double p, double epsilon)
    throws NotStrictlyPositiveException {
        this(p, epsilon, DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Creates a new Poisson distribution with the specified mean and maximum
     * number of iterations.
     *
     * @param p Poisson mean.
     * @param maxIterations Maximum number of iterations for cumulative
     * probabilities.
     * @since 2.1
     */
    public PoissonDistribution(double p, int maxIterations) {
        this(p, DEFAULT_EPSILON, maxIterations);
    }

    /**
     * Get the mean for the distribution.
     *
     * @return the mean for the distribution.
     */
    public double getMean() {
        return mean;
    }

    /** {@inheritDoc} */
    public double probability(int x) {
        double ret;
        if (x < 0 || x == Integer.MAX_VALUE) {
            ret = 0.0;
        } else if (x == 0) {
            ret = FastMath.exp(-mean);
        } else {
            ret = FastMath.exp(-SaddlePointExpansion.getStirlingError(x) -
                  SaddlePointExpansion.getDeviancePart(x, mean)) /
                  FastMath.sqrt(MathUtils.TWO_PI * x);
        }
        return ret;
    }

    /** {@inheritDoc} */
    public double cumulativeProbability(int x) {
        if (x < 0) {
            return 0;
        }
        if (x == Integer.MAX_VALUE) {
            return 1;
        }
        return Gamma.regularizedGammaQ((double) x + 1, mean, epsilon,
                                       maxIterations);
    }

    /**
     * Calculates the Poisson distribution function using a normal
     * approximation. The {@code N(mean, sqrt(mean))} distribution is used
     * to approximate the Poisson distribution. The computation uses
     * "half-correction" (evaluating the normal distribution function at
     * {@code x + 0.5}).
     *
     * @param x Upper bound, inclusive.
     * @return the distribution function value calculated using a normal
     * approximation.
     */
    public double normalApproximateProbability(int x)  {
        // calculate the probability using half-correction
        return normal.cumulativeProbability(x + 0.5);
    }

    /**
     * {@inheritDoc}
     *
     * For mean parameter {@code p}, the mean is {@code p}.
     */
    public double getNumericalMean() {
        return getMean();
    }

    /**
     * {@inheritDoc}
     *
     * For mean parameter {@code p}, the variance is {@code p}.
     */
    public double getNumericalVariance() {
        return getMean();
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always 0 no matter the mean parameter.
     *
     * @return lower bound of the support (always 0)
     */
    public int getSupportLowerBound() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is positive infinity,
     * regardless of the parameter values. There is no integer infinity,
     * so this method returns {@code Integer.MAX_VALUE}.
     *
     * @return upper bound of the support (always {@code Integer.MAX_VALUE} for
     * positive infinity)
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

    /**
     * {@inheritDoc}
     * <p>
     * <strong>Algorithm Description</strong>:
     * <ul>
     *  <li>For small means, uses simulation of a Poisson process
     *   using Uniform deviates, as described
     *   <a href="http://irmi.epfl.ch/cmos/Pmmi/interactive/rng7.htm"> here</a>.
     *   The Poisson process (and hence value returned) is bounded by 1000 * mean.
     *  </li>
     *  <li>For large means, uses the rejection algorithm described in
     *   <quote>
     *    Devroye, Luc. (1981).<i>The Computer Generation of Poisson Random Variables</i>
     *    <strong>Computing</strong> vol. 26 pp. 197-207.
     *   </quote>
     *  </li>
     * </ul>
     * </p>
     *
     * @return a random value.
     * @since 2.2
     */
    @Override
    public int sample() {
        return (int) FastMath.min(randomData.nextPoisson(mean), Integer.MAX_VALUE);
    }
}