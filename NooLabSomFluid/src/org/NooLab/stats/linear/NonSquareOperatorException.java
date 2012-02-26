package org.NooLab.stats.linear;

import org.NooLab.stats.exception.DimensionMismatchException;
import org.NooLab.stats.exception.LocalizedFormats;

/**
 * Exception to be thrown when a square linear operator is expected.
 *
 * @since 3.0
 * @version $Id$
 */
public class NonSquareOperatorException extends DimensionMismatchException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -4145007524150846242L;

    /**
     * Construct an exception from the mismatched dimensions.
     *
     * @param wrong Row dimension.
     * @param expected Column dimension.
     */
    public NonSquareOperatorException(int wrong, int expected) {
        super(LocalizedFormats.NON_SQUARE_OPERATOR, wrong, expected);
    }
}