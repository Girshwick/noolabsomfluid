package org.NooLab.stats.exception;

/**
 * Exception to be thrown when a number is too small.
 *
 * @since 2.2
 * @version $Id$
 */
public class NumberIsTooSmallException extends MathIllegalNumberException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -6100997100383932834L;
    /**
     * Higher bound.
     */
    private final Number min;
    /**
     * Whether the maximum is included in the allowed range.
     */
    private final boolean boundIsAllowed;

    /**
     * Construct the exception.
     *
     * @param wrong Value that is smaller than the minimum.
     * @param min Minimum.
     * @param boundIsAllowed Whether {@code min} is included in the allowed range.
     */
    public NumberIsTooSmallException(Number wrong,
                                     Number min,
                                     boolean boundIsAllowed) {
        this(boundIsAllowed ?
             LocalizedFormats.NUMBER_TOO_SMALL :
             LocalizedFormats.NUMBER_TOO_SMALL_BOUND_EXCLUDED,
             wrong, min, boundIsAllowed);
    }

    /**
     * Construct the exception with a specific context.
     *
     * @param specific Specific context pattern.
     * @param wrong Value that is smaller than the minimum.
     * @param min Minimum.
     * @param boundIsAllowed Whether {@code min} is included in the allowed range.
     */
    public NumberIsTooSmallException(Localizable specific,
                                     Number wrong,
                                     Number min,
                                     boolean boundIsAllowed) {
        super(specific, wrong, min);

        this.min = min;
        this.boundIsAllowed = boundIsAllowed;
    }

    /**
     * @return {@code true} if the minimum is included in the allowed range.
     */
    public boolean getBoundIsAllowed() {
        return boundIsAllowed;
    }

    /**
     * @return the minimum.
     */
    public Number getMin() {
        return min;
    }
}
