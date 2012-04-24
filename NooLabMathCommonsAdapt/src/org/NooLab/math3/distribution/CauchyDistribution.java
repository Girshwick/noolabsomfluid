package org.NooLab.math3.distribution;

import org.NooLab.math3.exception.LocalizedFormats;
import org.NooLab.math3.exception.NotStrictlyPositiveException;
import org.NooLab.math3.exception.OutOfRangeException;
import org.NooLab.math3.util.FastMath;

/**
 * Implementation of the Cauchy distribution.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Cauchy_distribution">Cauchy distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/CauchyDistribution.html">Cauchy Distribution (MathWorld)</a>
 * @since 1.1 (changed to concrete class in 3.0)
 * @version $Id$
 */
public class CauchyDistribution extends AbstractRealDistribution {
    /**
     * Default inverse cumulative probability accuracy.
     * @since 2.1
     */
    public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1e-9;
    /** Serializable version identifier */
    private static final long serialVersionUID = 8589540077390120676L;
    /** The median of this distribution. */
    private final double median;
    /** The scale of this distribution. */
    private final double scale;
    /** Inverse cumulative probability accuracy */
    private final double solverAbsoluteAccuracy;

    /**
     * Creates a Cauchy distribution with the median equal to zero and scale
     * equal to one.
     */
    public CauchyDistribution() {
        this(0, 1);
    }

    /**
     * Creates a Cauchy distribution using the given median and scale.
     *
     * @param median Median for this distribution.
     * @param scale Scale parameter for this distribution.
     */
    public CauchyDistribution(double median, double scale) {
        this(median, scale, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    /**
     * Creates a Cauchy distribution using the given median and scale.
     *
     * @param median Median for this distribution.
     * @param scale Scale parameter for this distribution.
     * @param inverseCumAccuracy Maximum absolute error in inverse
     * cumulative probability estimates
     * (defaults to {@link #DEFAULT_INVERSE_ABSOLUTE_ACCURACY}).
     * @throws NotStrictlyPositiveException if {@code scale <= 0}.
     * @since 2.1
     */
    public CauchyDistribution(double median, double scale,
                                  double inverseCumAccuracy) {
        if (scale <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.SCALE, scale);
        }
        this.scale = scale;
        this.median = median;
        solverAbsoluteAccuracy = inverseCumAccuracy;
    }

    /** {@inheritDoc} */
    public double cumulativeProbability(double x) {
        return 0.5 + (FastMath.atan((x - median) / scale) / FastMath.PI);
    }

    /**
     * Access the median.
     *
     * @return the median for this distribution.
     */
    public double getMedian() {
        return median;
    }

    /**
     * Access the scale parameter.
     *
     * @return the scale parameter for this distribution.
     */
    public double getScale() {
        return scale;
    }

    /**
     * {@inheritDoc}
     *
     * For this distribution {@code P(X = x)} always evaluates to 0.
     *
     * @return 0
     */
    public double probability(double x) {
        return 0.0;
    }

    /** {@inheritDoc} */
    public double density(double x) {
        final double dev = x - median;
        return (1 / FastMath.PI) * (scale / (dev * dev + scale * scale));
    }

    /**
     * {@inheritDoc}
     *
     * Returns {@code Double.NEGATIVE_INFINITY} when {@code p == 0}
     * and {@code Double.POSITIVE_INFINITY} when {@code p == 1}.
     */
    @Override
    public double inverseCumulativeProbability(double p) throws OutOfRangeException {
        double ret;
        if (p < 0 || p > 1) {
            throw new OutOfRangeException(p, 0, 1);
        } else if (p == 0) {
            ret = Double.NEGATIVE_INFINITY;
        } else  if (p == 1) {
            ret = Double.POSITIVE_INFINITY;
        } else {
            ret = median + scale * FastMath.tan(FastMath.PI * (p - .5));
        }
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    protected double getSolverAbsoluteAccuracy() {
        return solverAbsoluteAccuracy;
    }

    /**
     * {@inheritDoc}
     *
     * The mean is always undefined no matter the parameters.
     *
     * @return mean (always Double.NaN)
     */
    public double getNumericalMean() {
        return Double.NaN;
    }

    /**
     * {@inheritDoc}
     *
     * The variance is always undefined no matter the parameters.
     *
     * @return variance (always Double.NaN)
     */
    public double getNumericalVariance() {
        return Double.NaN;
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always negative infinity no matter
     * the parameters.
     *
     * @return lower bound of the support (always Double.NEGATIVE_INFINITY)
     */
    public double getSupportLowerBound() {
        return Double.NEGATIVE_INFINITY;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is always positive infinity no matter
     * the parameters.
     *
     * @return upper bound of the support (always Double.POSITIVE_INFINITY)
     */
    public double getSupportUpperBound() {
        return Double.POSITIVE_INFINITY;
    }

    /** {@inheritDoc} */
    public boolean isSupportLowerBoundInclusive() {
        return false;
    }

    /** {@inheritDoc} */
    public boolean isSupportUpperBoundInclusive() {
        return false;
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
