package org.NooLab.stats.random;

/**
 * This class is a gaussian normalized random generator for scalars.
 * <p>This class is a simple wrapper around the {@link
 * RandomGenerator#nextGaussian} method.</p>
 * @version $Id$
 * @since 1.2
 */

public class GaussianRandomGenerator implements NormalizedRandomGenerator {

    /** Underlying generator. */
    private final RandomGenerator generator;

    /** Create a new generator.
     * @param generator underlying random generator to use
     */
    public GaussianRandomGenerator(final RandomGenerator generator) {
        this.generator = generator;
    }

    /** Generate a random scalar with null mean and unit standard deviation.
     * @return a random scalar with null mean and unit standard deviation
     */
    public double nextNormalizedDouble() {
        return generator.nextGaussian();
    }

}
