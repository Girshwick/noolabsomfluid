package org.NooLab.math3.exception;

/**
 * Interface for accessing the context data structure stored in Commons Math
 * exceptions.
 *
 * @version $Id$
 */
public interface ExceptionContextProvider {
    /**
     * Gets a reference to the "rich context" data structure that allows to
     * customize error messages and store key, value pairs in exceptions.
     *
     * @return a reference to the exception context.
     */
    ExceptionContext getContext();

}
