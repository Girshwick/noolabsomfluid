package org.NooLab.math3.analysis.solvers;

import org.NooLab.math3.analysis.UnivariateFunction;
import org.NooLab.math3.exception.MaxCountExceededException;
import org.NooLab.math3.exception.NoBracketingException;
import org.NooLab.math3.exception.TooManyEvaluationsException;
import org.NooLab.math3.util.Incrementor;
import org.NooLab.math3.util.MathUtils;




/**
 * Provide a default implementation for several functions useful to generic
 * solvers.
 *
 * @param <FUNC> Type of function to solve.
 *
 * @version $Id$
 * @since 2.0
 */
public abstract class BaseAbstractUnivariateSolver<FUNC extends UnivariateFunction>
    implements BaseUnivariateSolver<FUNC> {
    /** Default relative accuracy. */
    private static final double DEFAULT_RELATIVE_ACCURACY = 1e-14;
    /** Default function value accuracy. */
    private static final double DEFAULT_FUNCTION_VALUE_ACCURACY = 1e-15;
    /** Function value accuracy. */
    private final double functionValueAccuracy;
    /** Absolute accuracy. */
    private final double absoluteAccuracy;
    /** Relative accuracy. */
    private final double relativeAccuracy;
    /** Evaluations counter. */
    private final Incrementor evaluations = new Incrementor();
    /** Lower end of search interval. */
    private double searchMin;
    /** Higher end of search interval. */
    private double searchMax;
    /** Initial guess. */
    private double searchStart;
    /** Function to solve. */
    private FUNC function;

    /**
     * Construct a solver with given absolute accuracy.
     *
     * @param absoluteAccuracy Maximum absolute error.
     */
    protected BaseAbstractUnivariateSolver(final double absoluteAccuracy) {
        this(DEFAULT_RELATIVE_ACCURACY,
             absoluteAccuracy,
             DEFAULT_FUNCTION_VALUE_ACCURACY);
    }

    /**
     * Construct a solver with given accuracies.
     *
     * @param relativeAccuracy Maximum relative error.
     * @param absoluteAccuracy Maximum absolute error.
     */
    protected BaseAbstractUnivariateSolver(final double relativeAccuracy,
                                               final double absoluteAccuracy) {
        this(relativeAccuracy,
             absoluteAccuracy,
             DEFAULT_FUNCTION_VALUE_ACCURACY);
    }

    /**
     * Construct a solver with given accuracies.
     *
     * @param relativeAccuracy Maximum relative error.
     * @param absoluteAccuracy Maximum absolute error.
     * @param functionValueAccuracy Maximum function value error.
     */
    protected BaseAbstractUnivariateSolver(final double relativeAccuracy,
                                               final double absoluteAccuracy,
                                               final double functionValueAccuracy) {
        this.absoluteAccuracy = absoluteAccuracy;
        this.relativeAccuracy = relativeAccuracy;
        this.functionValueAccuracy = functionValueAccuracy;
    }

    /** {@inheritDoc} */
    public int getMaxEvaluations() {
        return evaluations.getMaximalCount();
    }
    /** {@inheritDoc} */
    public int getEvaluations() {
        return evaluations.getCount();
    }
    /**
     * @return the lower end of the search interval.
     */
    public double getMin() {
        return searchMin;
    }
    /**
     * @return the higher end of the search interval.
     */
    public double getMax() {
        return searchMax;
    }
    /**
     * @return the initial guess.
     */
    public double getStartValue() {
        return searchStart;
    }
    /**
     * {@inheritDoc}
     */
    public double getAbsoluteAccuracy() {
        return absoluteAccuracy;
    }
    /**
     * {@inheritDoc}
     */
    public double getRelativeAccuracy() {
        return relativeAccuracy;
    }
    /**
     * {@inheritDoc}
     */
    public double getFunctionValueAccuracy() {
        return functionValueAccuracy;
    }

    /**
     * Compute the objective function value.
     *
     * @param point Point at which the objective function must be evaluated.
     * @return the objective function value at specified point.
     * @throws TooManyEvaluationsException if the maximal number of evaluations
     * is exceeded.
     */
    protected double computeObjectiveValue(double point)
        throws TooManyEvaluationsException {
        incrementEvaluationCount();
        return function.value(point);
    }

    /**
     * Prepare for computation.
     * Subclasses must call this method if they override any of the
     * {@code solve} methods.
     *
     * @param f Function to solve.
     * @param min Lower bound for the interval.
     * @param max Upper bound for the interval.
     * @param startValue Start value to use.
     * @param maxEval Maximum number of evaluations.
     */
    protected void setup(int maxEval,
                         FUNC f,
                         double min, double max,
                         double startValue) {
        // Checks.
        MathUtils.checkNotNull(f);

        // Reset.
        searchMin = min;
        searchMax = max;
        searchStart = startValue;
        function = f;
        evaluations.setMaximalCount(maxEval);
        evaluations.resetCount();
    }

    /** {@inheritDoc} */
    public double solve(int maxEval, FUNC f, double min, double max, double startValue) {
        // Initialization.
        setup(maxEval, f, min, max, startValue);

        // Perform computation.
        return doSolve();
    }

    /** {@inheritDoc} */
    public double solve(int maxEval, FUNC f, double min, double max) {
        return solve(maxEval, f, min, max, min + 0.5 * (max - min));
    }

    /** {@inheritDoc} */
    public double solve(int maxEval, FUNC f, double startValue) {
        return solve(maxEval, f, Double.NaN, Double.NaN, startValue);
    }

    /**
     * Method for implementing actual optimization algorithms in derived
     * classes.
     *
     * @return the root.
     * @throws TooManyEvaluationsException if the maximal number of evaluations
     * is exceeded.
     * @throws NoBracketingException if the initial search interval does not bracket
     * a root and the solver requires it.
     */
    protected abstract double doSolve()
        throws TooManyEvaluationsException, NoBracketingException;

    /**
     * Check whether the function takes opposite signs at the endpoints.
     *
     * @param lower Lower endpoint.
     * @param upper Upper endpoint.
     * @return {@code true} if the function values have opposite signs at the
     * given points.
     */
    protected boolean isBracketing(final double lower,
                                   final double upper) {
        return UnivariateSolverUtils.isBracketing(function, lower, upper);
    }

    /**
     * Check whether the arguments form a (strictly) increasing sequence.
     *
     * @param start First number.
     * @param mid Second number.
     * @param end Third number.
     * @return {@code true} if the arguments form an increasing sequence.
     */
    protected boolean isSequence(final double start,
                                 final double mid,
                                 final double end) {
        return UnivariateSolverUtils.isSequence(start, mid, end);
    }

    /**
     * Check that the endpoints specify an interval.
     *
     * @param lower Lower endpoint.
     * @param upper Upper endpoint.
     * @throws org.apache.commons.math3.exception.NumberIsTooLargeException
     * if {@code lower >= upper}.
     */
    protected void verifyInterval(final double lower,
                                  final double upper) {
        UnivariateSolverUtils.verifyInterval(lower, upper);
    }

    /**
     * Check that {@code lower < initial < upper}.
     *
     * @param lower Lower endpoint.
     * @param initial Initial value.
     * @param upper Upper endpoint.
     * @throws org.apache.commons.math3.exception.NumberIsTooLargeException
     * if {@code lower >= initial} or {@code initial >= upper}.
     */
    protected void verifySequence(final double lower,
                                  final double initial,
                                  final double upper) {
        UnivariateSolverUtils.verifySequence(lower, initial, upper);
    }

    /**
     * Check that the endpoints specify an interval and the function takes
     * opposite signs at the endpoints.
     *
     * @param lower Lower endpoint.
     * @param upper Upper endpoint.
     * @throws org.apache.commons.math3.exception.NoBracketingException if
     * the function has the same sign at the endpoints.
     */
    protected void verifyBracketing(final double lower,
                                    final double upper) {
        UnivariateSolverUtils.verifyBracketing(function, lower, upper);
    }

    /**
     * Increment the evaluation count by one.
     * Method {@link #computeObjectiveValue(double)} calls this method internally.
     * It is provided for subclasses that do not exclusively use
     * {@code computeObjectiveValue} to solve the function.
     * See e.g. {@link AbstractDifferentiableUnivariateSolver}.
     */
    protected void incrementEvaluationCount() {
        try {
            evaluations.incrementCount();
        } catch (MaxCountExceededException e) {
            throw new TooManyEvaluationsException(e.getMax());
        }
    }
}
