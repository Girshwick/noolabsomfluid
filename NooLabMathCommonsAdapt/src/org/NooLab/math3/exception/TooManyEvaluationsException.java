package org.NooLab.math3.exception;

/**
 * Exception to be thrown when the maximal number of evaluations is exceeded.
 *
 * @since 3.0
 * @version $Id$
 */
public class TooManyEvaluationsException extends MaxCountExceededException {
    /** Serializable version Id. */
    private static final long serialVersionUID = 4330003017885151975L;

    /**
     * Construct the exception.
     *
     * @param max Maximum number of evaluations.
     */
    public TooManyEvaluationsException(Number max) {
        super(max);
        getContext().addMessage(LocalizedFormats.EVALUATIONS);
    }
}