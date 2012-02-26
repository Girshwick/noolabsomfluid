package org.NooLab.stats.linear;

import org.NooLab.stats.exception.LocalizedFormats;
import org.NooLab.stats.exception.MathIllegalArgumentException;

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
