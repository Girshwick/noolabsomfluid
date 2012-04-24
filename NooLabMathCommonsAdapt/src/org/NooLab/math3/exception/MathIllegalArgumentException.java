package org.NooLab.math3.exception;

 
/**
 * Base class for all preconditions violation exceptions.
 * In most cases, this class should not be instantiated directly: it should
 * serve as a base class to create all the exceptions that have the semantics
 * of the standard {@link IllegalArgumentException}.
 *
 * @since 2.2
 * @version $Id$
 */
public class MathIllegalArgumentException extends IllegalArgumentException
    implements ExceptionContextProvider {
    /** Serializable version Id. */
    private static final long serialVersionUID = -6024911025449780478L;
    /** Context. */
    private final ExceptionContext context;

    /**
     * @param pattern Message pattern explaining the cause of the error.
     * @param args Arguments.
     */
    public MathIllegalArgumentException(Localizable pattern,
                                        Object ... args) {
        context = new ExceptionContext(this);
        context.addMessage(pattern, args);
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