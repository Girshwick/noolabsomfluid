package org.NooLab.math3.distribution;

import org.apache.commons.math.exception.NumberIsTooLargeException;
import org.apache.commons.math.exception.OutOfRangeException;

/**
 * Base interface for distributions on the reals.
 *
 * @version $Id$
 * @since 3.0
 */
public interface RealDistribution {
    /**
     * For a random variable {@code X} whose values are distributed according
     * to this distribution, this method returns {@code P(X = x)}. In other
     * words, this method represents the probability mass function (PMF)
     * for the distribution.
     *
     * @param x the point at which the PMF is evaluated
     * @return the value of the probability mass function at point {@code x}
     */
    double probability(double x);

    /**
     * Returns the probability density function (PDF) of this distribution
     * evaluated at the specified point {@code x}. In general, the PDF is
     * the derivative of the {@link #cumulativeProbability(double) CDF}.
     * If the derivative does not exist at {@code x}, then an appropriate
     * replacement should be returned, e.g. {@code Double.POSITIVE_INFINITY},
     * {@code Double.NaN}, or  the limit inferior or limit superior of the
     * difference quotient.
     *
     * @param x the point at which the PDF is evaluated
     * @return the value of the probability density function at point {@code x}
     */
    double density(double x);

    /**
     * For a random variable {@code X} whose values are distributed according
     * to this distribution, this method returns {@code P(X <= x)}. In other
     * words, this method represents the (cumulative) distribution function
     * (CDF) for this distribution.
     *
     * @param x the point at which the CDF is evaluated
     * @return the probability that a random variable with this
     * distribution takes a value less than or equal to {@code x}
     */
    double cumulativeProbability(double x);

    /**
     * For a random variable {@code X} whose values are distributed according
     * to this distribution, this method returns {@code P(x0 < X <= x1)}.
     *
     * @param x0 the exclusive lower bound
     * @param x1 the inclusive upper bound
     * @return the probability that a random variable with this distribution
     * takes a value between {@code x0} and {@code x1},
     * excluding the lower and including the upper endpoint
     * @throws NumberIsTooLargeException if {@code x0 > x1}
     */
    double cumulativeProbability(double x0, double x1) throws NumberIsTooLargeException;

    /**
     * Computes the quantile function of this distribution. For a random
     * variable {@code X} distributed according to this distribution, the
     * returned value is
     * <ul>
     * <li><code>inf{x in R | P(X<=x) >= p}</code> for {@code 0 < p <= 1},</li>
     * <li><code>inf{x in R | P(X<=x) > 0}</code> for {@code p = 0}.</li>
     * </ul>
     *
     * @param p the cumulative probability
     * @return the smallest {@code p}-quantile of this distribution
     * (largest 0-quantile for {@code p = 0})
     * @throws OutOfRangeException if {@code p < 0} or {@code p > 1}
     */
    double inverseCumulativeProbability(double p) throws OutOfRangeException;

    /**
     * Use this method to get the numerical value of the mean of this
     * distribution.
     *
     * @return the mean or {@code Double.NaN} if it is not defined
     */
    double getNumericalMean();

    /**
     * Use this method to get the numerical value of the variance of this
     * distribution.
     *
     * @return the variance (possibly {@code Double.POSITIVE_INFINITY} as
     * for certain cases in {@link TDistribution}) or {@code Double.NaN} if it
     * is not defined
     */
    double getNumericalVariance();

    /**
     * Access the lower bound of the support. This method must return the same
     * value as {@code inverseCumulativeProbability(0)}. In other words, this
     * method must return
     * <p><code>inf {x in R | P(X <= x) > 0}</code>.</p>
     *
     * @return lower bound of the support (might be
     * {@code Double.NEGATIVE_INFINITY})
     */
    double getSupportLowerBound();

    /**
     * Access the upper bound of the support. This method must return the same
     * value as {@code inverseCumulativeProbability(1)}. In other words, this
     * method must return
     * <p><code>inf {x in R | P(X <= x) = 1}</code>.</p>
     *
     * @return upper bound of the support (might be
     * {@code Double.POSITIVE_INFINITY})
     */
    double getSupportUpperBound();

    /**
     * Use this method to get information about whether the lower bound
     * of the support is inclusive or not.
     *
     * @return whether the lower bound of the support is inclusive or not
     */
    boolean isSupportLowerBoundInclusive();

    /**
     * Use this method to get information about whether the upper bound
     * of the support is inclusive or not.
     *
     * @return whether the upper bound of the support is inclusive or not
     */
    boolean isSupportUpperBoundInclusive();

    /**
     * Use this method to get information about whether the support is connected,
     * i.e. whether all values between the lower and upper bound of the support
     * are included in the support.
     *
     * @return whether the support is connected or not
     */
    boolean isSupportConnected();

    /**
     * Reseed the random generator used to generate samples.
     *
     * @param seed the new seed
     */
    void reseedRandomGenerator(long seed);

    /**
     * Generate a random value sampled from this distribution.
     *
     * @return a random value.
     */
    double sample();

    /**
     * Generate a random sample from the distribution.
     *
     * @param sampleSize the number of random values to generate
     * @return an array representing the random sample
     * @throws org.apache.commons.math3.exception.NotStrictlyPositiveException
     * if {@code sampleSize} is not positive
     */
    double[] sample(int sampleSize);
}
