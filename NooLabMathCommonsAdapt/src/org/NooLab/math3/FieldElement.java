package org.NooLab.math3;


/**
 * Interface representing <a href="http://mathworld.wolfram.com/Field.html">field</a> elements.
 * @param <T> the type of the field elements
 * @see Field
 * @version $Id$
 * @since 2.0
 */
public interface FieldElement<T> {

    /** Compute this + a.
     * @param a element to add
     * @return a new element representing this + a
     */
    T add(T a);

    /** Compute this - a.
     * @param a element to subtract
     * @return a new element representing this - a
     */
    T subtract(T a);

    /**
     * Returns the additive inverse of {@code this} element.
     * @return the opposite of {@code this}.
     */
    T negate();

    /** Compute n &times; this. Multiplication by an integer number is defined
     * as the following sum
     * <center>
     * n &times; this = &sum;<sub>i=1</sub><sup>n</sup> this.
     * </center>
     * @param n Number of times {@code this} must be added to itself.
     * @return A new element representing n &times; this.
     */
    T multiply(int n);

    /** Compute this &times; a.
     * @param a element to multiply
     * @return a new element representing this &times; a
     */
    T multiply(T a);

    /** Compute this &divide; a.
     * @param a element to add
     * @return a new element representing this &divide; a
     * @exception ArithmeticException if a is the zero of the
     * additive operation (i.e. additive identity)
     */
    T divide(T a) throws ArithmeticException;

    /**
     * Returns the multiplicative inverse of {@code this} element.
     * @return the inverse of {@code this}.
     */
    T reciprocal();

    /** Get the {@link Field} to which the instance belongs.
     * @return {@link Field} to which the instance belongs
     */
    Field<T> getField();
}
