package org.NooLab.stats.linear;

import org.NooLab.stats.FieldElement;

 

/**
 * Interface defining a visitor for matrix entries.
 *
 * @param <T> the type of the field elements
 * @version $Id$
 * @since 2.0
 */
public interface FieldMatrixPreservingVisitor<T extends FieldElement<?>> {
    /**
     * Start visiting a matrix.
     * <p>This method is called once before any entry of the matrix is visited.</p>
     * @param rows number of rows of the matrix
     * @param columns number of columns of the matrix
     * @param startRow Initial row index
     * @param endRow Final row index (inclusive)
     * @param startColumn Initial column index
     * @param endColumn Final column index (inclusive)
     */
    void start(int rows, int columns,
               int startRow, int endRow, int startColumn, int endColumn);

    /**
     * Visit one matrix entry.
     * @param row row index of the entry
     * @param column column index of the entry
     * @param value current value of the entry
     */
    void visit(int row, int column, T value);

    /**
     * End visiting a matrix.
     * <p>This method is called once after all entries of the matrix have been visited.</p>
     * @return the value that the <code>walkInXxxOrder</code> must return
     */
    T end();
}
