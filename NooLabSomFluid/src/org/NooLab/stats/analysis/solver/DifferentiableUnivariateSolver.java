package org.NooLab.stats.analysis.solver;

import org.NooLab.stats.BaseUnivariateSolver;
import org.NooLab.stats.analysis.DifferentiableUnivariateFunction;


/**
 * Interface for (univariate real) rootfinding algorithms.
 * Implementations will search for only one zero in the given interval.
 *
 * @version $Id$
 */
public interface DifferentiableUnivariateSolver
    extends BaseUnivariateSolver<DifferentiableUnivariateFunction> {}