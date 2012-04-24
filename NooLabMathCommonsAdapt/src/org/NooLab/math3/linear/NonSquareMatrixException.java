package org.NooLab.math3.linear;

import org.NooLab.math3.exception.DimensionMismatchException;
import org.NooLab.math3.exception.LocalizedFormats;

/**
 * Exception to be thrown when a square matrix is expected.
 *
 * @since 3.0
 * @version $Id$
 */
public class NonSquareMatrixException extends DimensionMismatchException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -660069396594485772L;

    /**
     * Construct an exception from the mismatched dimensions.
     *
     * @param wrong Row dimension.
     * @param expected Column dimension.
     */
    public NonSquareMatrixException(int wrong,
                                    int expected) {
        super(LocalizedFormats.NON_SQUARE_MATRIX, wrong, expected);
    }
}
