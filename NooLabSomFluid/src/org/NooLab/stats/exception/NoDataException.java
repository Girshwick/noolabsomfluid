package org.NooLab.stats.exception;

/**
 * Exception to be thrown when the required data is missing.
 *
 * @since 2.2
 * @version $Id$
 */
public class NoDataException extends MathIllegalArgumentException {

    /** Serializable version Id. */
    private static final long serialVersionUID = -3629324471511904459L;

    /**
     * Construct the exception.
     */
    public NoDataException() {
        this(LocalizedFormats.NO_DATA);
    }
    /**
     * Construct the exception with a specific context.
     *
     * @param specific Contextual information on what caused the exception.
     */
    public NoDataException(Localizable specific) {
        super(specific);
    }
}
