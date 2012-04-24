package org.NooLab.math3.exception;

 
 

/**
 * Base class for all exceptions that signal a mismatch between the
 * current state and the user's expectations.
 *
 * @since 2.2
 * @version $Id$
 */
public class MathIllegalStateException extends IllegalStateException
    implements ExceptionContextProvider {
    /** Serializable version Id. */
    private static final long serialVersionUID = -6024911025449780478L;
    /** Context. */
    private final ExceptionContext context;

    /**
     * Simple constructor.
     *
     * @param pattern Message pattern explaining the cause of the error.
     * @param args Arguments.
     */
    public MathIllegalStateException(Localizable pattern,
                                     Object ... args) {
        context = new ExceptionContext(this);
        context.addMessage(pattern, args);
    }

    /**
     * Simple constructor.
     *
     * @param cause Root cause.
     * @param pattern Message pattern explaining the cause of the error.
     * @param args Arguments.
     */
    public MathIllegalStateException(Throwable cause,
                                     Localizable pattern,
                                     Object ... args) {
        super(cause);
        context = new ExceptionContext(this);
        context.addMessage(pattern, args);
    }

    /**
     * Default constructor.
     */
    public MathIllegalStateException() {
        this(LocalizedFormats.ILLEGAL_STATE);
    }

    /** {@inheritDoc} */
    public ExceptionContext getContext() {
        return context;
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return context.getMessage();
    }

    /** {@inheritDoc} */
    @Override
    public String getLocalizedMessage() {
        return context.getLocalizedMessage();
    }
}