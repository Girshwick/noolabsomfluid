package org.NooLab.stats.exception;

/**
 * Exception to be thrown when a number is too large.
 *
 * @since 2.2
 * @version $Id$
 */
public class NumberIsTooLargeException extends MathIllegalNumberException {
    /** Serializable version Id. */
    private static final long serialVersionUID = 4330003017885151975L;
    /**
     * Higher bound.
     */
    private final Number max;
    /**
     * Whether the maximum is included in the allowed range.
     */
    private final boolean boundIsAllowed;

    /**
     * Construct the exception.
     *
     * @param wrong Value that is larger than the maximum.
     * @param max Maximum.
     * @param boundIsAllowed if true the maximum is included in the allowed range.
     */
    public NumberIsTooLargeException(Number wrong,
                                     Number max,
                                     boolean boundIsAllowed) {
        this(boundIsAllowed ?
             LocalizedFormats.NUMBER_TOO_LARGE :
             LocalizedFormats.NUMBER_TOO_LARGE_BOUND_EXCLUDED,
             wrong, max, boundIsAllowed);
    }
    /**
     * Construct the exception with a specific context.
     *
     * @param specific Specific context pattern.
     * @param wrong Value that is larger than the maximum.
     * @param max Maximum.
     * @param boundIsAllowed if true the maximum is included in the allowed range.
     */
    public NumberIsTooLargeException(Localizable specific,
                                     Number wrong,
                                     Number max,
                                     boolean boundIsAllowed) {
        super(specific, wrong, max);

        this.max = max;
        this.boundIsAllowed = boundIsAllowed;
    }

    /**
     * @return {@code true} if the maximum is included in the allowed range.
     */
    public boolean getBoundIsAllowed() {
        return boundIsAllowed;
    }

    /**
     * @return the maximum.
     */
    public Number getMax() {
        return max;
    }
}
