package org.NooLab.math3.analysis.solvers;

import org.NooLab.math3.analysis.UnivariateFunction;




/**
 * Base class for solvers.
 *
 * @version $Id$
 * @since 3.0
 */
public abstract class AbstractUnivariateSolver
    extends BaseAbstractUnivariateSolver<UnivariateFunction>
    implements UnivariateSolver {
    /**
     * Construct a solver with given absolute accuracy.
     *
     * @param absoluteAccuracy Maximum absolute error.
     */
    protected AbstractUnivariateSolver(final double absoluteAccuracy) {
        super(absoluteAccuracy);
    }
    /**
     * Construct a solver with given accuracies.
     *
     * @param relativeAccuracy Maximum relative error.
     * @param absoluteAccuracy Maximum absolute error.
     */
    protected AbstractUnivariateSolver(final double relativeAccuracy,
                                           final double absoluteAccuracy) {
        super(relativeAccuracy, absoluteAccuracy);
    }
    /**
     * Construct a solver with given accuracies.
     *
     * @param relativeAccuracy Maximum relative error.
     * @param absoluteAccuracy Maximum absolute error.
     * @param functionValueAccuracy Maximum function value error.
     */
    protected AbstractUnivariateSolver(final double relativeAccuracy,
                                           final double absoluteAccuracy,
                                           final double functionValueAccuracy) {
        super(relativeAccuracy, absoluteAccuracy, functionValueAccuracy);
    }
}