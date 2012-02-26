package org.NooLab.stats.exception;

/**
 * Exception triggered when something that shouldn't happen does happen.
 *
 * @since 2.2
 * @version $Id$
 */
public class MathInternalError extends MathIllegalStateException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -6276776513966934846L;
    /** URL for reporting problems. */
    private static final String REPORT_URL = "https://issues.apache.org/jira/browse/MATH";

    /**
     * Simple constructor.
     */
    public MathInternalError() {
        getContext().addMessage(LocalizedFormats.INTERNAL_ERROR, REPORT_URL);
    }

    /**
     * Simple constructor.
     * @param cause root cause
     */
    public MathInternalError(final Throwable cause) {
        super(cause, LocalizedFormats.INTERNAL_ERROR, REPORT_URL);
    }

    /**
     * Constructor accepting a localized message.
     *
     * @param pattern Message pattern explaining the cause of the error.
     * @param args Arguments.
     */
    public MathInternalError(Localizable pattern, Object ... args) {
        super(pattern, args);
    }
}