package org.NooLab.stats.fraction;

import java.io.Serializable;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import org.NooLab.stats.exception.LocalizedFormats;
import org.NooLab.stats.exception.NullArgumentException;


/**
 * Common part shared by both {@link FractionFormat} and {@link BigFractionFormat}.
 * @version $Id$
 * @since 2.0
 */
public abstract class AbstractFormat extends NumberFormat implements Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = -6981118387974191891L;

    /** The format used for the denominator. */
    protected NumberFormat denominatorFormat;

    /** The format used for the numerator. */
    protected NumberFormat numeratorFormat;

    /**
     * Create an improper formatting instance with the default number format
     * for the numerator and denominator.
     */
    protected AbstractFormat() {
        this(getDefaultNumberFormat());
    }

    /**
     * Create an improper formatting instance with a custom number format for
     * both the numerator and denominator.
     * @param format the custom format for both the numerator and denominator.
     */
    protected AbstractFormat(final NumberFormat format) {
        this(format, (NumberFormat) format.clone());
    }

    /**
     * Create an improper formatting instance with a custom number format for
     * the numerator and a custom number format for the denominator.
     * @param numeratorFormat the custom format for the numerator.
     * @param denominatorFormat the custom format for the denominator.
     */
    protected AbstractFormat(final NumberFormat numeratorFormat,
                             final NumberFormat denominatorFormat) {
        this.numeratorFormat   = numeratorFormat;
        this.denominatorFormat = denominatorFormat;
    }

    /**
     * Create a default number format.  The default number format is based on
     * {@link NumberFormat#getNumberInstance(java.util.Locale)}. The only
     * customization is the maximum number of BigFraction digits, which is set to 0.
     * @return the default number format.
     */
    protected static NumberFormat getDefaultNumberFormat() {
        return getDefaultNumberFormat(Locale.getDefault());
    }

    /**
     * Create a default number format.  The default number format is based on
     * {@link NumberFormat#getNumberInstance(java.util.Locale)}. The only
     * customization is the maximum number of BigFraction digits, which is set to 0.
     * @param locale the specific locale used by the format.
     * @return the default number format specific to the given locale.
     */
    protected static NumberFormat getDefaultNumberFormat(final Locale locale) {
        final NumberFormat nf = NumberFormat.getNumberInstance(locale);
        nf.setMaximumFractionDigits(0);
        nf.setParseIntegerOnly(true);
        return nf;
    }

    /**
     * Access the denominator format.
     * @return the denominator format.
     */
    public NumberFormat getDenominatorFormat() {
        return denominatorFormat;
    }

    /**
     * Access the numerator format.
     * @return the numerator format.
     */
    public NumberFormat getNumeratorFormat() {
        return numeratorFormat;
    }

    /**
     * Modify the denominator format.
     * @param format the new denominator format value.
     * @throws NullArgumentException if {@code format} is {@code null}.
     */
    public void setDenominatorFormat(final NumberFormat format) {
        if (format == null) {
            throw new NullArgumentException(LocalizedFormats.DENOMINATOR_FORMAT);
        }
        this.denominatorFormat = format;
    }

    /**
     * Modify the numerator format.
     * @param format the new numerator format value.
     * @throws NullArgumentException if {@code format} is {@code null}.
     */
    public void setNumeratorFormat(final NumberFormat format) {
        if (format == null) {
            throw new NullArgumentException(LocalizedFormats.NUMERATOR_FORMAT);
        }
        this.numeratorFormat = format;
    }

    /**
     * Parses <code>source</code> until a non-whitespace character is found.
     * @param source the string to parse
     * @param pos input/output parsing parameter.  On output, <code>pos</code>
     *        holds the index of the next non-whitespace character.
     */
    protected static void parseAndIgnoreWhitespace(final String source,
                                                   final ParsePosition pos) {
        parseNextCharacter(source, pos);
        pos.setIndex(pos.getIndex() - 1);
    }

    /**
     * Parses <code>source</code> until a non-whitespace character is found.
     * @param source the string to parse
     * @param pos input/output parsing parameter.
     * @return the first non-whitespace character.
     */
    protected static char parseNextCharacter(final String source,
                                             final ParsePosition pos) {
         int index = pos.getIndex();
         final int n = source.length();
         char ret = 0;

         if (index < n) {
             char c;
             do {
                 c = source.charAt(index++);
             } while (Character.isWhitespace(c) && index < n);
             pos.setIndex(index);

             if (index < n) {
                 ret = c;
             }
         }

         return ret;
    }

    /**
     * Formats a double value as a fraction and appends the result to a StringBuffer.
     *
     * @param value the double value to format
     * @param buffer StringBuffer to append to
     * @param position On input: an alignment field, if desired. On output: the
     *            offsets of the alignment field
     * @return a reference to the appended buffer
     * @see #format(Object, StringBuffer, FieldPosition)
     */
    @Override
    public StringBuffer format(final double value,
                               final StringBuffer buffer, final FieldPosition position) {
        return format(Double.valueOf(value), buffer, position);
    }


    /**
     * Formats a long value as a fraction and appends the result to a StringBuffer.
     *
     * @param value the long value to format
     * @param buffer StringBuffer to append to
     * @param position On input: an alignment field, if desired. On output: the
     *            offsets of the alignment field
     * @return a reference to the appended buffer
     * @see #format(Object, StringBuffer, FieldPosition)
     */
    @Override
    public StringBuffer format(final long value,
                               final StringBuffer buffer, final FieldPosition position) {
        return format(Long.valueOf(value), buffer, position);
    }

}