package org.NooLab.math3.exception;

import org.NooLab.math3.util.MathArrays;

/**
 * Exception to be thrown when the a sequence of values is not monotonically
 * increasing or decreasing.
 *
 * @since 2.2 (name changed to "NonMonotonicSequenceException" in 3.0)
 * @version $Id$
 */
public class NonMonotonicSequenceException extends MathIllegalNumberException {
    /** Serializable version Id. */
    private static final long serialVersionUID = 3596849179428944575L;
    /**
     * Direction (positive for increasing, negative for decreasing).
     */
    private final MathArrays.OrderDirection direction;
    /**
     * Whether the sequence must be strictly increasing or decreasing.
     */
    private final boolean strict;
    /**
     * Index of the wrong value.
     */
    private final int index;
    /**
     * Previous value.
     */
    private final Number previous;

    /**
     * Construct the exception.
     * This constructor uses default values assuming that the sequence should
     * have been strictly increasing.
     *
     * @param wrong Value that did not match the requirements.
     * @param previous Previous value in the sequence.
     * @param index Index of the value that did not match the requirements.
     */
    public NonMonotonicSequenceException(Number wrong,
                                         Number previous,
                                         int index) {
        this(wrong, previous, index, MathArrays.OrderDirection.INCREASING, true);
    }

    /**
     * Construct the exception.
     *
     * @param wrong Value that did not match the requirements.
     * @param previous Previous value in the sequence.
     * @param index Index of the value that did not match the requirements.
     * @param direction Strictly positive for a sequence required to be
     * increasing, negative (or zero) for a decreasing sequence.
     * @param strict Whether the sequence must be strictly increasing or
     * decreasing.
     */
    public NonMonotonicSequenceException(Number wrong,
                                         Number previous,
                                         int index,
                                         MathArrays.OrderDirection direction,
                                         boolean strict) {
        super(direction == MathArrays.OrderDirection.INCREASING ?
              (strict ?
               LocalizedFormats.NOT_STRICTLY_INCREASING_SEQUENCE :
               LocalizedFormats.NOT_INCREASING_SEQUENCE) :
              (strict ?
               LocalizedFormats.NOT_STRICTLY_DECREASING_SEQUENCE :
               LocalizedFormats.NOT_DECREASING_SEQUENCE),
              wrong, previous, index, index - 1);

        this.direction = direction;
        this.strict = strict;
        this.index = index;
        this.previous = previous;
    }

    /**
     * @return the order direction.
     **/
    public MathArrays.OrderDirection getDirection() {
        return direction;
    }
    /**
     * @return {@code true} is the sequence should be strictly monotonic.
     **/
    public boolean getStrict() {
        return strict;
    }
    /**
     * Get the index of the wrong value.
     *
     * @return the current index.
     */
    public int getIndex() {
        return index;
    }
    /**
     * @return the previous value.
     */
    public Number getPrevious() {
        return previous;
    }
}
