package org.NooLab.stats.fraction;

import java.io.Serializable;

import org.NooLab.stats.Field;
import org.NooLab.stats.FieldElement;

/**
 * Representation of the fractional numbers field.
 * <p>
 * This class is a singleton.
 * </p>
 * @see Fraction
 * @version $Id$
 * @since 2.0
 */
public class FractionField implements Field<Fraction>, Serializable  {

    /** Serializable version identifier */
    private static final long serialVersionUID = -1257768487499119313L;

    /** Private constructor for the singleton.
     */
    private FractionField() {
    }

    /** Get the unique instance.
     * @return the unique instance
     */
    public static FractionField getInstance() {
        return LazyHolder.INSTANCE;
    }

    /** {@inheritDoc} */
    public Fraction getOne() {
        return Fraction.ONE;
    }

    /** {@inheritDoc} */
    public Fraction getZero() {
        return Fraction.ZERO;
    }

    /** {@inheritDoc} */
    public Class<? extends FieldElement<Fraction>> getRuntimeClass() {
        return Fraction.class;
    }
    // CHECKSTYLE: stop HideUtilityClassConstructor
    /** Holder for the instance.
     * <p>We use here the Initialization On Demand Holder Idiom.</p>
     */
    private static class LazyHolder {
        /** Cached field instance. */
        private static final FractionField INSTANCE = new FractionField();
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