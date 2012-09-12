package org.NooLab.somtransform.math;


/**
 * some charts here:, esp. Fig2
 * http://www.mathworks.ch/company/newsletters/news_notes/win03/monte_carlo.html
 * 
 * Nelson, R.B. (1999) An Introduction to Copulas, Lecture Notes in Statistics, Vol. 139, Springer-Verlag, 216pp.
 * 
 * 
 * we interpret a multivariate dependency as a sequence of choices, where
 * each choice is base on a particular marginal distribution
 * 
 * For each choice = for each dimension we maintain a transition matrix, that has
 * the respective marginal at both sides !!
 * 
 * then we draw 2 random integers, uniformly distributed, the first describing
 * the start row, the second the number of cycles
 * 
 * yet, the loop changes the MC context after each selection of a column
 * say M1 -> M2 -> M1 -> ... in the bivariate case, the tables are invoked perfectly alternatingly
 *   - or  M1 -> M2 -> M3 -> M4 -> M1 in the quadru-variate case, 
 *   - or  : additionally with a variable number of triggering events towards other transitions
 * 
 * the selection of the context could be itself a random variable (uniform), with rel.freq = 1/n , n=number of variables
 * in this way, all variables will develop a "correlational coupling"
 * 
 * after some loops, the rows are measured for each of the contexts, then
 * additionally gaussian transformed WITHIN the resolution of the histogram which we used to 
 * describe individual variables
 * 
 * important: the transition matrices are already coupled during measurement !!
 *            i.e.  
 * 
 */
public class SurrogatedData {
	
	
	

}
