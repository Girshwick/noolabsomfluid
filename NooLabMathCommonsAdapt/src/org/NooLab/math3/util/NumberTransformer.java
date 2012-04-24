package org.NooLab.math3.util;

import org.NooLab.math3.exception.MathIllegalArgumentException;

/**
 * Subclasses implementing this interface can transform Objects to doubles.
 * @version $Id$
 *
 * No longer extends Serializable since 2.0
 *
 */
public interface NumberTransformer {

    /**
     * Implementing this interface provides a facility to transform
     * from Object to Double.
     *
     * @param o the Object to be transformed.
     * @return the double value of the Object.
     * @throws MathIllegalArgumentException if the Object can not be transformed into a Double.
     */
    double transform(Object o) throws MathIllegalArgumentException;
}