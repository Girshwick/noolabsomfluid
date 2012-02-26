package org.NooLab.stats;

import org.NooLab.stats.analysis.UnivariateFunction;

 

/**
 * Interface for (univariate real) root-finding algorithms.
 * Implementations will search for only one zero in the given interval.
 *
 * @version $Id$
 */
public interface UnivariateSolver
    extends BaseUnivariateSolver<UnivariateFunction> {
	
	
}
