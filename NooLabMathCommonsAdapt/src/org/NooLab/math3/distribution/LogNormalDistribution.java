package org.NooLab.math3.distribution;

import org.NooLab.math3.exception.LocalizedFormats;
import org.NooLab.math3.exception.NotStrictlyPositiveException;
import org.NooLab.math3.exception.NumberIsTooLargeException;
import org.NooLab.math3.util.FastMath;

/**
 * Implementation of the log-normal (gaussian) distribution.
 *
 * <p>
 * <strong>Parameters:</strong>
 * {@code X} is log-normally distributed if its natural logarithm {@code log(X)}
 * is normally distributed. The probability distribution function of {@code X}
 * is given by (for {@code x > 0})
 * </p>
 * <p>
 * {@code exp(-0.5 * ((ln(x) - m) / s)^2) / (s * sqrt(2 * pi) * x)}
 * </p>
 * <ul>
 * <li>{@code m} is the <em>scale</em> parameter: this is the mean of the
 * normally distributed natural logarithm of this distribution,</li>
 * <li>{@code s} is the <em>shape</em> parameter: this is the standard
 * deviation of the normally distributed natural logarithm of this
 * distribution.
 * </ul>
 *
 * @see <a href="http://en.wikipedia.org/wiki/Log-normal_distribution">
 * Log-normal distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/LogNormalDistribution.html">
 * Log Normal distribution (MathWorld)</a>
 *
 * @version $Id$
 * @since 3.0
 */
public class LogNormalDistribution extends AbstractRealDistribution {
    /** Default inverse cumulative probability accuracy. */
    public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1e-9;

    /** Serializable version identifier. */
    private static final long serialVersionUID = 20120112;

    /** &radic;(2 &pi;) */
    private static final double SQRT2PI = FastMath.sqrt(2 * FastMath.PI);

    /** &radic;(2) */
    private static final double SQRT2 = FastMath.sqrt(2.0);

    /** The scale parameter of this distribution. */
    private final double scale;

    /** The shape parameter of this distribution. */
    private final double shape;

    /** Inverse cumulative probability accuracy. */
    private final double solverAbsoluteAccuracy;

    /**
     * Create a log-normal distribution using the specified scale and shape.
     *
     * @param scale the scale parameter of this distribution
     * @param shape the shape parameter of this distribution
     * @throws NotStrictlyPositiveException if {@code shape <= 0}.
     */
    public LogNormalDistribution(double scale, double shape)
        throws NotStrictlyPositiveException {
        this(scale, shape, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }

    /**
     * Create a log-normal distribution using the specified scale, shape and
     * inverse cumulative distribution accuracy.
     *
     * @param scale the scale parameter of this distribution
     * @param shape the shape parameter of this distribution
     * @param inverseCumAccuracy Inverse cumulative probability accuracy.
     * @throws NotStrictlyPositiveException if {@code shape <= 0}.
     */
    public LogNormalDistribution(double scale, double shape,
        double inverseCumAccuracy) throws NotStrictlyPositiveException {
        if (shape <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.SHAPE, shape);
        }

        this.scale = scale;
        this.shape = shape;
        this.solverAbsoluteAccuracy = inverseCumAccuracy;
    }

    /**
     * Create a log-normal distribution, where the mean and standard deviation
     * of the {@link NormalDistribution normally distributed} natural
     * logarithm of the log-normal distribution are equal to zero and one
     * respectively. In other words, the scale of the returned distribution is
     * {@code 0}, while its shape is {@code 1}.
     */
    public LogNormalDistribution() {
        this(0, 1);
    }

    /**
     * Returns the scale parameter of this distribution.
     *
     * @return the scale parameter
     */
    public double getScale() {
        return scale;
    }

    /**
     * Returns the shape parameter of this distribution.
     *
     * @return the shape parameter
     */
    public double getShape() {
        return shape;
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

    /**
     * {@inheritDoc}
     *
     * For scale {@code m}, and shape {@code s} of this distribution, the PDF
     * is given by
     * <ul>
     * <li>{@code 0} if {@code x <= 0},</li>
     * <li>{@code exp(-0.5 * ((ln(x) - m) / s)^2) / (s * sqrt(2 * pi) * x)}
     * otherwise.</li>
     * </ul>
     */
    public double density(double x) {
        if (x <= 0) {
            return 0;
        }
        final double x0 = FastMath.log(x) - scale;
        final double x1 = x0 / shape;
        return FastMath.exp(-0.5 * x1 * x1) / (shape * SQRT2PI * x);
    }

    /**
     * {@inheritDoc}
     *
     * For scale {@code m}, and shape {@code s} of this distribution, the CDF
     * is given by
     * <ul>
     * <li>{@code 0} if {@code x <= 0},</li>
     * <li>{@code 0} if {@code ln(x) - m < 0} and {@code m - ln(x) > 40 * s}, as
     * in these cases the actual value is within {@code Double.MIN_VALUE} of 0,
     * <li>{@code 1} if {@code ln(x) - m >= 0} and {@code ln(x) - m > 40 * s},
     * as in these cases the actual value is within {@code Double.MIN_VALUE} of
     * 1,</li>
     * <li>{@code 0.5 + 0.5 * erf((ln(x) - m) / (s * sqrt(2))} otherwise.</li>
     * </ul>
     */
    public double cumulativeProbability(double x)  {
        if (x <= 0) {
            return 0;
        }
        final double dev = FastMath.log(x) - scale;
        if (FastMath.abs(dev) > 40 * shape) {
            return dev < 0 ? 0.0d : 1.0d;
        }
        return 0.5 + 0.5 * Erf.erf(dev / (shape * SQRT2));
    }

    /** {@inheritDoc} */
    @Override
    public double cumulativeProbability(double x0, double x1)
        throws NumberIsTooLargeException {
        if (x0 > x1) {
            throw new NumberIsTooLargeException(LocalizedFormats.LOWER_ENDPOINT_ABOVE_UPPER_ENDPOINT,
                                                x0, x1, true);
        }
        if (x0 <= 0 || x1 <= 0) {
            return super.cumulativeProbability(x0, x1);
        }
        final double denom = shape * SQRT2;
        final double v0 = (FastMath.log(x0) - scale) / denom;
        final double v1 = (FastMath.log(x1) - scale) / denom;
        return 0.5 * Erf.erf(v0, v1);
    }

    /** {@inheritDoc} */
    @Override
    protected double getSolverAbsoluteAccuracy() {
        return solverAbsoluteAccuracy;
    }

    /**
     * {@inheritDoc}
     *
     * For scale {@code m} and shape {@code s}, the mean is
     * {@code exp(m + s^2 / 2)}.
     */
    public double getNumericalMean() {
        double s = shape;
        return FastMath.exp(scale + (s * s / 2));
    }

    /**
     * {@inheritDoc}
     *
     * For scale {@code m} and shape {@code s}, the variance is
     * {@code (exp(s^2) - 1) * exp(2 * m + s^2)}.
     */
    public double getNumericalVariance() {
        final double s = shape;
        final double ss = s * s;
        return (FastMath.exp(ss) - 1) * FastMath.exp(2 * scale + ss);
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always 0 no matter the parameters.
     *
     * @return lower bound of the support (always 0)
     */
    public double getSupportLowerBound() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is always positive infinity
     * no matter the parameters.
     *
     * @return upper bound of the support (always
     * {@code Double.POSITIVE_INFINITY})
     */
    public double getSupportUpperBound() {
        return Double.POSITIVE_INFINITY;
    }

    /** {@inheritDoc} */
    public boolean isSupportLowerBoundInclusive() {
        return true;
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

    /** {@inheritDoc} */
    @Override
    public double sample()  {
        double n = randomData.nextGaussian(0, 1);
        return FastMath.exp(scale + shape * n);
    }
}