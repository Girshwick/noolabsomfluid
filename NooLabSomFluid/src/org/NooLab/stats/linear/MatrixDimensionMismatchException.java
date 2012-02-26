package org.NooLab.stats.linear;

import org.NooLab.stats.exception.LocalizedFormats;
import org.NooLab.stats.exception.MultiDimensionMismatchException;

/**
 * Exception to be thrown when either the number of rows or the number of
 * columns of a matrix do not match the expected values.
 *
 * @since 3.0
 * @version $Id$
 */
public class MatrixDimensionMismatchException extends MultiDimensionMismatchException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -8415396756375798143L;

    /**
     * Construct an exception from the mismatched dimensions.
     *
     * @param wrongRowDim Wrong row dimension.
     * @param wrongColDim Wrong column dimension.
     * @param expectedRowDim Expected row dimension.
     * @param expectedColDim Expected column dimension.
     */
    public MatrixDimensionMismatchException(int wrongRowDim,
                                            int wrongColDim,
                                            int expectedRowDim,
                                            int expectedColDim) {
        super(LocalizedFormats.DIMENSIONS_MISMATCH_2x2,
              new Integer[] { wrongRowDim, wrongColDim },
              new Integer[] { expectedRowDim, expectedColDim });
    }

    /**
     * @return the expected row dimension.
     */
    public int getWrongRowDimension() {
        return getWrongDimension(0);
    }
    /**
     * @return the expected row dimension.
     */
    public int getExpectedRowDimension() {
        return getExpectedDimension(0);
    }
    /**
     * @return the wrong column dimension.
     */
    public int getWrongColumnDimension() {
        return getWrongDimension(1);
    }
    /**
     * @return the expected column dimension.
     */
    public int getExpectedColumnDimension() {
        return getExpectedDimension(1);
    }
}
