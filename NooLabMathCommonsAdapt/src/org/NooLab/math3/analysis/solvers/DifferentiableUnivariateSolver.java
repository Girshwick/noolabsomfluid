package org.NooLab.math3.analysis.solvers;

import org.NooLab.math3.analysis.DifferentiableUnivariateFunction;


/**
 * Interface for (univariate real) rootfinding algorithms.
 * Implementations will search for only one zero in the given interval.
 *
 * @version $Id$
 */
public interface DifferentiableUnivariateSolver
    extends BaseUnivariateSolver<DifferentiableUnivariateFunction> {}