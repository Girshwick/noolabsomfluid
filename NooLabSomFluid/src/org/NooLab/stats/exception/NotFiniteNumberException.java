package org.NooLab.stats.exception;

/**
 * Exception to be thrown when a number is not finite.
 *
 * @since 3.0
 * @version $Id$
 */
public class NotFiniteNumberException extends MathIllegalNumberException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -6100997100383932834L;

    /**
     * Construct the exception.
     *
     * @param wrong Value that is infinite or NaN.
     * @param args Optional arguments.
     */
    public NotFiniteNumberException(Number wrong,
                                    Object ... args) {
        this(LocalizedFormats.NOT_FINITE_NUMBER, wrong, args);
    }

    /**
     * Construct the exception with a specific context.
     *
     * @param specific Specific context pattern.
     * @param wrong Value that is infinite or NaN.
     * @param args Optional arguments.
     */
    public NotFiniteNumberException(Localizable specific,
                                    Number wrong,
                                    Object ... args) {
        super(specific, wrong, args);
    }
}
