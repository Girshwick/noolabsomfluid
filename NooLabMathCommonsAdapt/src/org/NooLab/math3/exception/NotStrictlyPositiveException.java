package org.NooLab.math3.exception;

/**
 * Exception to be thrown when the argument is negative.
 *
 * @since 2.2
 * @version $Id$
 */
public class NotStrictlyPositiveException extends NumberIsTooSmallException {

    /** Serializable version Id. */
    private static final long serialVersionUID = -7824848630829852237L;

    /**
     * Construct the exception.
     *
     * @param value Argument.
     */
    public NotStrictlyPositiveException(Number value) {
        super(value, 0, false);
    }
    /**
     * Construct the exception with a specific context.
     *
     * @param specific Specific context where the error occurred.
     * @param value Argument.
     */
    public NotStrictlyPositiveException(Localizable specific,
                                        Number value) {
        super(specific, value, 0, false);
    }
}
