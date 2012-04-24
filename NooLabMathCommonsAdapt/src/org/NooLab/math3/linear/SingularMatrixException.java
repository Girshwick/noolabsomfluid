package org.NooLab.math3.linear;

import org.NooLab.math3.exception.LocalizedFormats;
import org.NooLab.math3.exception.MathIllegalArgumentException;

/**
 * Exception to be thrown when a non-singular matrix is expected.
 *
 * @since 3.0
 * @version $Id$
 */
public class SingularMatrixException extends MathIllegalArgumentException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -4206514844735401070L;

    /**
     * Construct an exception.
     */
    public SingularMatrixException() {
        super(LocalizedFormats.SINGULAR_MATRIX);
    }
}
