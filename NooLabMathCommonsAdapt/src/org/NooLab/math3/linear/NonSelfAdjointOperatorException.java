package org.NooLab.math3.linear;

import org.NooLab.math3.exception.LocalizedFormats;
import org.NooLab.math3.exception.MathIllegalArgumentException;

/**
 * Exception to be thrown when a self-adjoint {@link RealLinearOperator}
 * is expected.
 * Since the coefficients of the matrix are not accessible, the most
 * general definition is used to check that A is not self-adjoint, i.e.
 * there exist x and y such as {@code | x' A y - y' A x | >= eps},
 * where {@code eps} is a user-specified tolerance, and {@code x'}
 * denotes the transpose of {@code x}.
 * In the terminology of this exception, {@code A} is the "offending"
 * linear operator, {@code x} and {@code y} are the first and second
 * "offending" vectors, respectively.
 *
 * @version $Id$
 * @since 3.0
 */
public class NonSelfAdjointOperatorException
    extends MathIllegalArgumentException {
    /** Serializable version Id. */
    private static final long serialVersionUID = 1784999305030258247L;

    /** Creates a new instance of this class. */
    public NonSelfAdjointOperatorException() {
        super(LocalizedFormats.NON_SELF_ADJOINT_OPERATOR);
    }
}