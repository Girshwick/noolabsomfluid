package org.NooLab.math3.analysis.solvers;

/** The kinds of solutions that a {@link BracketedUnivariateSolver
 * (bracketed univariate real) root-finding algorithm} may accept as solutions.
 * This basically controls whether or not under-approximations and
 * over-approximations are allowed.
 *
 * <p>If all solutions are accepted ({@link #ANY_SIDE}), then the solution
 * that the root-finding algorithm returns for a given root may be equal to the
 * actual root, but it may also be an approximation that is slightly smaller
 * or slightly larger than the actual root. Root-finding algorithms generally
 * only guarantee that the returned solution is within the requested
 * tolerances. In certain cases however, in particular for
 * {@link org.apache.commons.math3.ode.events.EventHandler state events} of
 * {@link org.apache.commons.math3.ode.ODEIntegrator ODE solvers}, it
 * may be necessary to guarantee that a solution is returned that lies on a
 * specific side the solution.</p>
 *
 * @see BracketedUnivariateSolver
 * @since 3.0
 * @version $Id$
 */
public enum AllowedSolution {
    /** There are no additional side restriction on the solutions for
     * root-finding. That is, both under-approximations and over-approximations
     * are allowed. So, if a function f(x) has a root at x = x0, then the
     * root-finding result s may be smaller than x0, equal to x0, or greater
     * than x0.
     */
    ANY_SIDE,

    /** Only solutions that are less than or equal to the actual root are
     * acceptable as solutions for root-finding. In other words,
     * over-approximations are not allowed. So, if a function f(x) has a root
     * at x = x0, then the root-finding result s must satisfy s &lt;= x0.
     */
    LEFT_SIDE,

    /** Only solutions that are greater than or equal to the actual root are
     * acceptable as solutions for root-finding. In other words,
     * under-approximations are not allowed. So, if a function f(x) has a root
     * at x = x0, then the root-finding result s must satisfy s &gt;= x0.
     */
    RIGHT_SIDE,

    /** Only solutions for which values are less than or equal to zero are
     * acceptable as solutions for root-finding. So, if a function f(x) has
     * a root at x = x0, then the root-finding result s must satisfy f(s) &lt;= 0.
     */
    BELOW_SIDE,

    /** Only solutions for which values are greater than or equal to zero are
     * acceptable as solutions for root-finding. So, if a function f(x) has
     * a root at x = x0, then the root-finding result s must satisfy f(s) &gt;= 0.
     */
    ABOVE_SIDE;

}
