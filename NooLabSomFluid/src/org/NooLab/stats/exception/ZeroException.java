package org.NooLab.stats.exception;

/**
 * Exception to be thrown when zero is provided where it is not allowed.
 *
 * @since 2.2
 * @version $Id$
 */
public class ZeroException extends MathIllegalNumberException {

    /** Serializable version identifier */
    private static final long serialVersionUID = -1960874856936000015L;

    /**
     * Construct the exception.
     */
    public ZeroException() {
        this(LocalizedFormats.ZERO_NOT_ALLOWED);
    }

    /**
     * Construct the exception with a specific context.
     *
     * @param specific Specific context pattern.
     * @param arguments Arguments.
     */
    public ZeroException(Localizable specific, Object ... arguments) {
        super(specific, 0, arguments);
    }
}
