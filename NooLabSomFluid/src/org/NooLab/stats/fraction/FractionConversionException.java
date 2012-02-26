package org.NooLab.stats.fraction;

import org.NooLab.stats.exception.ConvergenceException;
import org.NooLab.stats.exception.LocalizedFormats;

/**
 * Error thrown when a double value cannot be converted to a fraction
 * in the allowed number of iterations.
 *
 * @version $Id$
 * @since 1.2
 */
public class FractionConversionException extends ConvergenceException {

    /** Serializable version identifier. */
    private static final long serialVersionUID = -4661812640132576263L;

    /**
     * Constructs an exception with specified formatted detail message.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     * @param value double value to convert
     * @param maxIterations maximal number of iterations allowed
     */
    public FractionConversionException(double value, int maxIterations) {
        super(LocalizedFormats.FAILED_FRACTION_CONVERSION, value, maxIterations);
    }

    /**
     * Constructs an exception with specified formatted detail message.
     * Message formatting is delegated to {@link java.text.MessageFormat}.
     * @param value double value to convert
     * @param p current numerator
     * @param q current denominator
     */
    public FractionConversionException(double value, long p, long q) {
        super(LocalizedFormats.FRACTION_CONVERSION_OVERFLOW, value, p, q);
    }

}
