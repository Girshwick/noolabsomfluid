package org.NooLab.math3.stat.ranking;

/**
 * Strategies for handling tied values in rank transformations.
 * <ul>
 * <li>SEQUENTIAL - Ties are assigned ranks in order of occurrence in the original array,
 * for example (1,3,4,3) is ranked as (1,2,4,3)</li>
 * <li>MINIMUM - Tied values are assigned the minimum applicable rank, or the rank
 * of the first occurrence. For example, (1,3,4,3) is ranked as (1,2,4,2)</li>
 * <li>MAXIMUM - Tied values are assigned the maximum applicable rank, or the rank
 * of the last occurrence. For example, (1,3,4,3) is ranked as (1,3,4,3)</li>
 * <li>AVERAGE - Tied values are assigned the average of the applicable ranks.
 * For example, (1,3,4,3) is ranked as (1,2.5,4,2.5)</li>
 * <li>RANDOM - Tied values are assigned a random integer rank from among the
 * applicable values. The assigned rank will always be an integer, (inclusively)
 * between the values returned by the MINIMUM and MAXIMUM strategies.</li>
 * </ul>
 *
 * @since 2.0
 * @version $Id$
 */
public enum TiesStrategy {

    /** Ties assigned sequential ranks in order of occurrence */
    SEQUENTIAL,

    /** Ties get the minimum applicable rank */
    MINIMUM,

    /** Ties get the maximum applicable rank */
    MAXIMUM,

    /** Ties get the average of applicable ranks */
    AVERAGE,

    /** Ties get a random integral value from among applicable ranks */
    RANDOM
}
