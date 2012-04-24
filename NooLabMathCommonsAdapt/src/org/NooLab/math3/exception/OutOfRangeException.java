package org.NooLab.math3.exception;

/**
 * Exception to be thrown when some argument is out of range.
 *
 * @since 2.2
 * @version $Id$
 */
public class OutOfRangeException extends MathIllegalNumberException {
    /** Serializable version Id. */
    private static final long serialVersionUID = 111601815794403609L;
    /** Lower bound. */
    private final Number lo;
    /** Higher bound. */
    private final Number hi;

    /**
     * Construct an exception from the mismatched dimensions.
     *
     * @param wrong Requested value.
     * @param lo Lower bound.
     * @param hi Higher bound.
     */
    public OutOfRangeException(Number wrong,
                               Number lo,
                               Number hi) {
        this(LocalizedFormats.OUT_OF_RANGE_SIMPLE, wrong, lo, hi);
    }

    /**
     * Construct an exception from the mismatched dimensions with a
     * specific context information.
     *
     * @param specific Context information.
     * @param wrong Requested value.
     * @param lo Lower bound.
     * @param hi Higher bound.
     */
    public OutOfRangeException(Localizable specific,
                               Number wrong,
                               Number lo,
                               Number hi) {
        super(specific, wrong, lo, hi);
        this.lo = lo;
        this.hi = hi;
    }

    /**
     * @return the lower bound.
     */
    public Number getLo() {
        return lo;
    }
    /**
     * @return the higher bound.
     */
    public Number getHi() {
        return hi;
    }
}
