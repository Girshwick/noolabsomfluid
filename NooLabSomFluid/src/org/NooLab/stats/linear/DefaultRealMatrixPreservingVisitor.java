package org.NooLab.stats.linear;

/**
 * Default implementation of the {@link RealMatrixPreservingVisitor} interface.
 * <p>
 * This class is a convenience to create custom visitors without defining all
 * methods. This class provides default implementations that do nothing.
 * </p>
 *
 * @version $Id$
 * @since 2.0
 */
public class DefaultRealMatrixPreservingVisitor implements RealMatrixPreservingVisitor {
    /** {@inheritDoc} */
    public void start(int rows, int columns,
                      int startRow, int endRow, int startColumn, int endColumn) {
    }

    /** {@inheritDoc} */
    public void visit(int row, int column, double value) {}

    /** {@inheritDoc} */
    public double end() {
        return 0;
    }
}
