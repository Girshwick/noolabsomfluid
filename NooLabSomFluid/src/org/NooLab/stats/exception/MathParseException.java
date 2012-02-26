package org.NooLab.stats.exception;

/**
 * Class to signal parse failures.
 *
 * @since 2.2
 * @version $Id$
 */
public class MathParseException extends MathIllegalStateException
								implements ExceptionContextProvider {
    /** Serializable version Id. */
    private static final long serialVersionUID = -6024911025449780478L;

    /**
     * @param wrong Bad string representation of the object.
     * @param position Index, in the {@code wrong} string, that caused the
     * parsing to fail.
     * @param type Class of the object supposedly represented by the
     * {@code wrong} string.
     */
    public MathParseException(String wrong,
                              int position,
                              Class<?> type) {
        getContext().addMessage(LocalizedFormats.CANNOT_PARSE_AS_TYPE,
                                wrong, Integer.valueOf(position), type.getName());
    }

    /**
     * @param wrong Bad string representation of the object.
     * @param position Index, in the {@code wrong} string, that caused the
     * parsing to fail.
     */
    public MathParseException(String wrong,
                              int position) {
        getContext().addMessage(LocalizedFormats.CANNOT_PARSE,
                                wrong, Integer.valueOf(position));
    }
}
