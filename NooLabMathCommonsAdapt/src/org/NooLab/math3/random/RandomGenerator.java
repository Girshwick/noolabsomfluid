package org.NooLab.math3.random;


/**
 * Interface extracted from <code>java.util.Random</code>.  This interface is
 * implemented by {@link AbstractRandomGenerator}.
 *
 * @since 1.1
 * @version $Id$
 */
public interface RandomGenerator {

    /**
     * Sets the seed of the underlying random number generator using an
     * <code>int</code> seed.
     * <p>Sequences of values generated starting with the same seeds
     * should be identical.
     * </p>
     * @param seed the seed value
     */
	public void setSeed(int seed);

    /**
     * Sets the seed of the underlying random number generator using an
     * <code>int</code> array seed.
     * <p>Sequences of values generated starting with the same seeds
     * should be identical.
     * </p>
     * @param seed the seed value
     */
    public void setSeed(int[] seed);

    /**
     * Sets the seed of the underlying random number generator using a
     * <code>long</code> seed.
     * <p>Sequences of values generated starting with the same seeds
     * should be identical.
     * </p>
     * @param seed the seed value
     */
    public void setSeed(long seed);

    /**
     * Generates random bytes and places them into a user-supplied
     * byte array.  The number of random bytes produced is equal to
     * the length of the byte array.
     *
     * @param bytes the non-null byte array in which to put the
     * random bytes
     */
    public void nextBytes(byte[] bytes);

    /**
     * Returns the next pseudorandom, uniformly distributed <code>int</code>
     * value from this random number generator's sequence.
     * All 2<font size="-1"><sup>32</sup></font> possible <tt>int</tt> values
     * should be produced with  (approximately) equal probability.
     *
     * @return the next pseudorandom, uniformly distributed <code>int</code>
     *  value from this random number generator's sequence
     */
    public int nextInt();

    /**
     * Returns a pseudorandom, uniformly distributed <tt>int</tt> value
     * between 0 (inclusive) and the specified value (exclusive), drawn from
     * this random number generator's sequence.
     *
     * @param n the bound on the random number to be returned.  Must be
     * positive.
     * @return  a pseudorandom, uniformly distributed <tt>int</tt>
     * value between 0 (inclusive) and n (exclusive).
     * @throws IllegalArgumentException  if n is not positive.
     */
    int nextInt(int n);

    /**
     * Returns the next pseudorandom, uniformly distributed <code>long</code>
     * value from this random number generator's sequence.  All
     * 2<font size="-1"><sup>64</sup></font> possible <tt>long</tt> values
     * should be produced with (approximately) equal probability.
     *
     * @return  the next pseudorandom, uniformly distributed <code>long</code>
     *value from this random number generator's sequence
     */
    long nextLong();

    /**
     * Returns the next pseudorandom, uniformly distributed
     * <code>boolean</code> value from this random number generator's
     * sequence.
     *
     * @return  the next pseudorandom, uniformly distributed
     * <code>boolean</code> value from this random number generator's
     * sequence
     */
    boolean nextBoolean();

    /**
     * Returns the next pseudorandom, uniformly distributed <code>float</code>
     * value between <code>0.0</code> and <code>1.0</code> from this random
     * number generator's sequence.
     *
     * @return  the next pseudorandom, uniformly distributed <code>float</code>
     * value between <code>0.0</code> and <code>1.0</code> from this
     * random number generator's sequence
     */
    float nextFloat();

    /**
     * Returns the next pseudorandom, uniformly distributed
     * <code>double</code> value between <code>0.0</code> and
     * <code>1.0</code> from this random number generator's sequence.
     *
     * @return  the next pseudorandom, uniformly distributed
     *  <code>double</code> value between <code>0.0</code> and
     *  <code>1.0</code> from this random number generator's sequence
     */
    double nextDouble();

    /**
     * Returns the next pseudorandom, Gaussian ("normally") distributed
     * <code>double</code> value with mean <code>0.0</code> and standard
     * deviation <code>1.0</code> from this random number generator's sequence.
     *
     * @return  the next pseudorandom, Gaussian ("normally") distributed
     * <code>double</code> value with mean <code>0.0</code> and
     * standard deviation <code>1.0</code> from this random number
     *  generator's sequence
     */
    double nextGaussian();
}