package org.NooLab.math3.random;

import org.NooLab.math3.util.FastMath;

/**
 * This class implements a normalized uniform random generator.
 * <p>Since it is a normalized random generator, it generates values
 * from a uniform distribution with mean equal to 0 and standard
 * deviation equal to 1. Generated values fall in the range
 * [-&#x0221A;3, +&#x0221A;3].</p>
 *
 * @since 1.2
 *
 * @version $Id$
 */

public class UniformRandomGenerator implements NormalizedRandomGenerator {

    /** Square root of three. */
    private static final double SQRT3 = FastMath.sqrt(3.0);

    /** Underlying generator. */
    private final RandomGenerator generator;

    /** Create a new generator.
     * @param generator underlying random generator to use
     */
    public UniformRandomGenerator(RandomGenerator generator) {
        this.generator = generator;
    }

    /** Generate a random scalar with null mean and unit standard deviation.
     * <p>The number generated is uniformly distributed between -&sqrt;(3)
     * and +&sqrt;(3).</p>
     * @return a random scalar with null mean and unit standard deviation
     */
    public double nextNormalizedDouble() {
        return SQRT3 * (2 * generator.nextDouble() - 1.0);
    }

}
