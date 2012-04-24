package org.NooLab.math3.fraction;

import java.io.Serializable;

import org.NooLab.math3.Field;
import org.NooLab.math3.FieldElement;
 

/**
 * Representation of the fractional numbers  without any overflow field.
 * <p>
 * This class is a singleton.
 * </p>
 * @see Fraction
 * @version $Id$
 * @since 2.0
 */
public class BigFractionField implements Field<BigFraction>, Serializable  {

    /** Serializable version identifier */
    private static final long serialVersionUID = -1699294557189741703L;

    /** Private constructor for the singleton.
     */
    private BigFractionField() {
    }

    /** Get the unique instance.
     * @return the unique instance
     */
    public static BigFractionField getInstance() {
        return LazyHolder.INSTANCE;
    }

    /** {@inheritDoc} */
    public BigFraction getOne() {
        return BigFraction.ONE;
    }

    /** {@inheritDoc} */
    public BigFraction getZero() {
        return BigFraction.ZERO;
    }

    /** {@inheritDoc} */
    public Class<? extends FieldElement<BigFraction>> getRuntimeClass() {
        return (Class<? extends FieldElement<BigFraction>>) BigFraction.class; //kwa: not sure whether this will work ?
    }
    // CHECKSTYLE: stop HideUtilityClassConstructor
    /** Holder for the instance.
     * <p>We use here the Initialization On Demand Holder Idiom.</p>
     */
    private static class LazyHolder {
        /** Cached field instance. */
        private static final BigFractionField INSTANCE = new BigFractionField();
    }
    // CHECKSTYLE: resume HideUtilityClassConstructor

    /** Handle deserialization of the singleton.
     * @return the singleton instance
     */
    private Object readResolve() {
        // return the singleton instance
        return LazyHolder.INSTANCE;
    }

}