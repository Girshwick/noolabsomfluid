package org.NooLab.math3.analysis.solvers;

import org.NooLab.math3.analysis.UnivariateFunction;

 

/**
 * Interface for (univariate real) root-finding algorithms.
 * Implementations will search for only one zero in the given interval.
 *
 * @version $Id$
 */
public interface UnivariateSolver
    extends BaseUnivariateSolver<UnivariateFunction> {
	
	
}
