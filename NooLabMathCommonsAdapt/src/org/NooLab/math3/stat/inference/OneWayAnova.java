package org.NooLab.math3.stat.inference;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
import org.NooLab.math3.distribution.FDistribution;
import org.NooLab.math3.exception.ConvergenceException;
import org.NooLab.math3.exception.DimensionMismatchException;
import org.NooLab.math3.exception.LocalizedFormats;
import org.NooLab.math3.exception.MaxCountExceededException;
import org.NooLab.math3.exception.NullArgumentException;
import org.NooLab.math3.exception.OutOfRangeException;
 
import org.apache.commons.math.stat.descriptive.summary.Sum;
import org.apache.commons.math.stat.descriptive.summary.SumOfSquares;

import java.util.Collection;

/**
 * Implements one-way ANOVA (analysis of variance) statistics.
 *
 * <p> Tests for differences between two or more categories of univariate data
 * (for example, the body mass index of accountants, lawyers, doctors and
 * computer programmers).  When two categories are given, this is equivalent to
 * the {@link org.apache.commons.math3.stat.inference.TTest}.
 * </p><p>
 * Uses the {@link org.apache.commons.math3.distribution.FDistribution
 * commons-math F Distribution implementation} to estimate exact p-values.</p>
 * <p>This implementation is based on a description at
 * http://faculty.vassar.edu/lowry/ch13pt1.html</p>
 * <pre>
 * Abbreviations: bg = between groups,
 *                wg = within groups,
 *                ss = sum squared deviations
 * </pre>
 *
 * @since 1.2
 * @version $Id$
 */
public class OneWayAnova {

    /**
     * Default constructor.
     */
    public OneWayAnova() {
    }

    /**
     * Computes the ANOVA F-value for a collection of <code>double[]</code>
     * arrays.
     *
     * <p><strong>Preconditions</strong>: <ul>
     * <li>The categoryData <code>Collection</code> must contain
     * <code>double[]</code> arrays.</li>
     * <li> There must be at least two <code>double[]</code> arrays in the
     * <code>categoryData</code> collection and each of these arrays must
     * contain at least two values.</li></ul></p><p>
     * This implementation computes the F statistic using the definitional
     * formula<pre>
     *   F = msbg/mswg</pre>
     * where<pre>
     *  msbg = between group mean square
     *  mswg = within group mean square</pre>
     * are as defined <a href="http://faculty.vassar.edu/lowry/ch13pt1.html">
     * here</a></p>
     *
     * @param categoryData <code>Collection</code> of <code>double[]</code>
     * arrays each containing data for one category
     * @return Fvalue
     * @throws NullArgumentException if <code>categoryData</code> is <code>null</code>
     * @throws DimensionMismatchException if the length of the <code>categoryData</code>
     * array is less than 2 or a contained <code>double[]</code> array does not have
     * at least two values
     */
    public double anovaFValue(final Collection<double[]> categoryData)
        throws NullArgumentException, DimensionMismatchException {

        AnovaStats a = anovaStats(categoryData);
        return a.F;

    }

    /**
     * Computes the ANOVA P-value for a collection of <code>double[]</code>
     * arrays.
     *
     * <p><strong>Preconditions</strong>: <ul>
     * <li>The categoryData <code>Collection</code> must contain
     * <code>double[]</code> arrays.</li>
     * <li> There must be at least two <code>double[]</code> arrays in the
     * <code>categoryData</code> collection and each of these arrays must
     * contain at least two values.</li></ul></p><p>
     * This implementation uses the
     * {@link org.apache.commons.math3.distribution.FDistribution
     * commons-math F Distribution implementation} to estimate the exact
     * p-value, using the formula<pre>
     *   p = 1 - cumulativeProbability(F)</pre>
     * where <code>F</code> is the F value and <code>cumulativeProbability</code>
     * is the commons-math implementation of the F distribution.</p>
     *
     * @param categoryData <code>Collection</code> of <code>double[]</code>
     * arrays each containing data for one category
     * @return Pvalue
     * @throws NullArgumentException if <code>categoryData</code> is <code>null</code>
     * @throws DimensionMismatchException if the length of the <code>categoryData</code>
     * array is less than 2 or a contained <code>double[]</code> array does not have
     * at least two values
     * @throws ConvergenceException if the p-value can not be computed due to a convergence error
     * @throws MaxCountExceededException if the maximum number of iterations is exceeded
     */
    public double anovaPValue(final Collection<double[]> categoryData)
        throws NullArgumentException, DimensionMismatchException,
        ConvergenceException, MaxCountExceededException {

        AnovaStats a = anovaStats(categoryData);
        FDistribution fdist = new FDistribution(a.dfbg, a.dfwg);
        return 1.0 - fdist.cumulativeProbability(a.F);

    }

    /**
     * Performs an ANOVA test, evaluating the null hypothesis that there
     * is no difference among the means of the data categories.
     *
     * <p><strong>Preconditions</strong>: <ul>
     * <li>The categoryData <code>Collection</code> must contain
     * <code>double[]</code> arrays.</li>
     * <li> There must be at least two <code>double[]</code> arrays in the
     * <code>categoryData</code> collection and each of these arrays must
     * contain at least two values.</li>
     * <li>alpha must be strictly greater than 0 and less than or equal to 0.5.
     * </li></ul></p><p>
     * This implementation uses the
     * {@link org.apache.commons.math3.distribution.FDistribution
     * commons-math F Distribution implementation} to estimate the exact
     * p-value, using the formula<pre>
     *   p = 1 - cumulativeProbability(F)</pre>
     * where <code>F</code> is the F value and <code>cumulativeProbability</code>
     * is the commons-math implementation of the F distribution.</p>
     * <p>True is returned iff the estimated p-value is less than alpha.</p>
     *
     * @param categoryData <code>Collection</code> of <code>double[]</code>
     * arrays each containing data for one category
     * @param alpha significance level of the test
     * @return true if the null hypothesis can be rejected with
     * confidence 1 - alpha
     * @throws NullArgumentException if <code>categoryData</code> is <code>null</code>
     * @throws DimensionMismatchException if the length of the <code>categoryData</code>
     * array is less than 2 or a contained <code>double[]</code> array does not have
     * at least two values
     * @throws OutOfRangeException if <code>alpha</code> is not in the range (0, 0.5]
     * @throws ConvergenceException if the p-value can not be computed due to a convergence error
     * @throws MaxCountExceededException if the maximum number of iterations is exceeded
     */
    public boolean anovaTest(final Collection<double[]> categoryData,
                             final double alpha)
        throws NullArgumentException, DimensionMismatchException,
        OutOfRangeException, ConvergenceException, MaxCountExceededException {

        if ((alpha <= 0) || (alpha > 0.5)) {
            throw new OutOfRangeException(
                    LocalizedFormats.OUT_OF_BOUND_SIGNIFICANCE_LEVEL,
                    alpha, 0, 0.5);
        }
        return anovaPValue(categoryData) < alpha;

    }

    /**
     * This method actually does the calculations (except P-value).
     *
     * @param categoryData <code>Collection</code> of <code>double[]</code>
     * arrays each containing data for one category
     * @return computed AnovaStats
     * @throws NullArgumentException if <code>categoryData</code> is <code>null</code>
     * @throws DimensionMismatchException if the length of the <code>categoryData</code>
     * array is less than 2 or a contained <code>double[]</code> array does not contain
     * at least two values
     */
    private AnovaStats anovaStats(final Collection<double[]> categoryData)
        throws NullArgumentException, DimensionMismatchException {

        if (categoryData == null) {
            throw new NullArgumentException();
        }

        // check if we have enough categories
        if (categoryData.size() < 2) {
            throw new DimensionMismatchException(
                    LocalizedFormats.TWO_OR_MORE_CATEGORIES_REQUIRED,
                    categoryData.size(), 2);
        }

        // check if each category has enough data and all is double[]
        for (double[] array : categoryData) {
            if (array.length <= 1) {
                throw new DimensionMismatchException(
                        LocalizedFormats.TWO_OR_MORE_VALUES_IN_CATEGORY_REQUIRED,
                        array.length, 2);
            }
        }

        int dfwg = 0;
        double sswg = 0;
        Sum totsum = new Sum();
        SumOfSquares totsumsq = new SumOfSquares();
        int totnum = 0;

        for (double[] data : categoryData) {

            Sum sum = new Sum();
            SumOfSquares sumsq = new SumOfSquares();
            int num = 0;

            for (int i = 0; i < data.length; i++) {
                double val = data[i];

                // within category
                num++;
                sum.increment(val);
                sumsq.increment(val);

                // for all categories
                totnum++;
                totsum.increment(val);
                totsumsq.increment(val);
            }
            dfwg += num - 1;
            double ss = sumsq.getResult() - sum.getResult() * sum.getResult() / num;
            sswg += ss;
        }
        double sst = totsumsq.getResult() - totsum.getResult() *
            totsum.getResult()/totnum;
        double ssbg = sst - sswg;
        int dfbg = categoryData.size() - 1;
        double msbg = ssbg/dfbg;
        double mswg = sswg/dfwg;
        double F = msbg/mswg;

        return new AnovaStats(dfbg, dfwg, F);
    }

    /**
        Convenience class to pass dfbg,dfwg,F values around within AnovaImpl.
        No get/set methods provided.
    */
    private static class AnovaStats {

        /** Degrees of freedom in numerator (between groups). */
        private final int dfbg;

        /** Degrees of freedom in denominator (within groups). */
        private final int dfwg;

        /** Statistic. */
        private final double F;

        /**
         * Constructor
         * @param dfbg degrees of freedom in numerator (between groups)
         * @param dfwg degrees of freedom in denominator (within groups)
         * @param F statistic
         */
        private AnovaStats(int dfbg, int dfwg, double F) {
            this.dfbg = dfbg;
            this.dfwg = dfwg;
            this.F = F;
        }
    }

}
