package org.NooLab.math3.linear;

import org.NooLab.math3.FieldElement;

/**
 * Default implementation of the {@link FieldMatrixPreservingVisitor} interface.
 * <p>
 * This class is a convenience to create custom visitors without defining all
 * methods. This class provides default implementations that do nothing.
 * </p>
 *
 * @param <T> the type of the field elements
 * @version $Id$
 * @since 2.0
 */
public class DefaultFieldMatrixPreservingVisitor<T extends FieldElement<T>>
    implements FieldMatrixPreservingVisitor<T> {
    /** Zero element of the field. */
    private final T zero;

    /** Build a new instance.
     * @param zero additive identity of the field
     */
    public DefaultFieldMatrixPreservingVisitor(final T zero) {
        this.zero = zero;
    }

    /** {@inheritDoc} */
    public void start(int rows, int columns,
                      int startRow, int endRow, int startColumn, int endColumn) {
    }

    /** {@inheritDoc} */
    public void visit(int row, int column, T value) {}

    /** {@inheritDoc} */
    public T end() {
        return zero;
    }
}
