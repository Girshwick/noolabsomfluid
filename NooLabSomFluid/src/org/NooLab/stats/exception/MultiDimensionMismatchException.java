package org.NooLab.stats.exception;

/**
 * Exception to be thrown when two sets of dimensions differ.
 *
 * @since 3.0
 * @version $Id$
 */
public class MultiDimensionMismatchException extends MathIllegalArgumentException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -8415396756375798143L;

    /** Wrong dimensions. */
    private final Integer[] wrong;
    /** Correct dimensions. */
    private final Integer[] expected;

    /**
     * Construct an exception from the mismatched dimensions.
     *
     * @param wrong Wrong dimensions.
     * @param expected Expected dimensions.
     */
    public MultiDimensionMismatchException(Integer[] wrong,
                                           Integer[] expected) {
        this(LocalizedFormats.DIMENSIONS_MISMATCH, wrong, expected);
    }

    /**
     * Construct an exception from the mismatched dimensions.
     *
     * @param specific Message pattern providing the specific context of
     * the error.
     * @param wrong Wrong dimensions.
     * @param expected Expected dimensions.
     */
    public MultiDimensionMismatchException(Localizable specific,
                                           Integer[] wrong,
                                           Integer[] expected) {
        super(specific, wrong, expected);
        this.wrong = wrong.clone();
        this.expected = expected.clone();
    }

    /**
     * @return an array containing the wrong dimensions.
     */
    public Integer[] getWrongDimensions() {
        return wrong.clone();
    }
    /**
     * @return an array containing the expected dimensions.
     */
    public Integer[] getExpectedDimensions() {
        return expected.clone();
    }

    /**
     * @param index Dimension index.
     * @return the wrong dimension stored at {@code index}.
     */
    public int getWrongDimension(int index) {
        return wrong[index];
    }
    /**
     * @param index Dimension index.
     * @return the expected dimension stored at {@code index}.
     */
    public int getExpectedDimension(int index) {
        return expected[index];
    }
}