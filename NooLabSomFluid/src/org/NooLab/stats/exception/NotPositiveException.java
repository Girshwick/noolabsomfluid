package org.NooLab.stats.exception;

/**
 * Exception to be thrown when the argument is negative.
 *
 * @since 2.2
 * @version $Id$
 */
public class NotPositiveException extends NumberIsTooSmallException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -2250556892093726375L;

    /**
     * Construct the exception.
     *
     * @param value Argument.
     */
    public NotPositiveException(Number value) {
        super(value, 0, true);
    }
    /**
     * Construct the exception with a specific context.
     *
     * @param specific Specific context where the error occurred.
     * @param value Argument.
     */
    public NotPositiveException(Localizable specific,
                                Number value) {
        super(specific, value, 0, true);
    }
}
