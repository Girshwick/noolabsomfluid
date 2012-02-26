package org.NooLab.stats.random;


/** This interface represents a random generator for whole vectors.
 *
 * @since 1.2
 * @version $Id$
 *
 */

public interface RandomVectorGenerator {

    /** Generate a random vector.
     * @return a random vector as an array of double.
     */
    double[] nextVector();

}