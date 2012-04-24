package org.NooLab.math3.analysis.solvers;

import org.NooLab.math3.analysis.DifferentiableUnivariateFunction;
import org.NooLab.math3.analysis.UnivariateFunction;

 
/**
 * Provide a default implementation for several functions useful to generic
 * solvers.
 *
 * @version $Id$
 * @since 3.0
 */
public abstract class AbstractDifferentiableUnivariateSolver
    extends BaseAbstractUnivariateSolver<DifferentiableUnivariateFunction>
    implements DifferentiableUnivariateSolver {
    /** Derivative of the function to solve. */
    private UnivariateFunction functionDerivative;

    /**
     * Construct a solver with given absolute accuracy.
     *
     * @param absoluteAccuracy Maximum absolute error.
     */
    protected AbstractDifferentiableUnivariateSolver(final double absoluteAccuracy) {
        super(absoluteAccuracy);
    }

    /**
     * Construct a solver with given accuracies.
     *
     * @param relativeAccuracy Maximum relative error.
     * @param absoluteAccuracy Maximum absolute error.
     * @param functionValueAccuracy Maximum function value error.
     */
    protected AbstractDifferentiableUnivariateSolver(final double relativeAccuracy,
                                                         final double absoluteAccuracy,
                                                         final double functionValueAccuracy) {
        super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy);
    }

    /**
     * Compute the objective function value.
     *
     * @param point Point at which the objective function must be evaluated.
     * @return the objective function value at specified point.
     * @throws org.apache.commons.math3.exception.TooManyEvaluationsException
     * if the maximal number of evaluations is exceeded.
     */
    protected double computeDerivativeObjectiveValue(double point) {
        incrementEvaluationCount();
        return functionDerivative.value(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setup(int maxEval, DifferentiableUnivariateFunction f,
                         double min, double max, double startValue) {
        super.setup(maxEval, f, min, max, startValue);
        functionDerivative = f.derivative();
    }
}