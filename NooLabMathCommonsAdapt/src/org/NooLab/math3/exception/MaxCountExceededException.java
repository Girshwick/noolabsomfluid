package org.NooLab.math3.exception;

 

/**
 * Exception to be thrown when some counter maximum value is exceeded.
 *
 * @since 3.0
 * @version $Id$
 */
public class MaxCountExceededException extends MathIllegalStateException {
    /** Serializable version Id. */
    private static final long serialVersionUID = 4330003017885151975L;
    /**
     * Maximum number of evaluations.
     */
    private final Number max;

    /**
     * Construct the exception.
     *
     * @param max Maximum.
     */
    public MaxCountExceededException(Number max) {
        this(LocalizedFormats.MAX_COUNT_EXCEEDED, max);
    }
    /**
     * Construct the exception with a specific context.
     *
     * @param specific Specific context pattern.
     * @param max Maximum.
     * @param args Additional arguments.
     */
    public MaxCountExceededException(Localizable specific,
                                     Number max,
                                     Object ... args) {
        getContext().addMessage(specific, max, args);
        this.max = max;
    }

    /**
     * @return the maximum number of evaluations.
     */
    public Number getMax() {
        return max;
    }
}