package org.NooLab.math3.analysis;



/**
 * An interface representing a univariate real function.
 * <br/>
 * When a <em>user-defined</em> function encounters an error during
 * evaluation, the {@link #value(double) value} method should throw a
 * <em>user-defined</em> unchecked exception.
 * <br/>
 * The following code excerpt shows the recommended way to do that using
 * a root solver as an example, but the same construct is applicable to
 * ODE integrators or optimizers.
 *
 * <pre>
 * private static class LocalException extends RuntimeException {
 *     // The x value that caused the problem.
 *     private final double x;
 *
 *     public LocalException(double x) {
 *         this.x = x;
 *     }
 *
 *     public double getX() {
 *         return x;
 *     }
 * }
 *
 * private static class MyFunction implements UnivariateFunction {
 *     public double value(double x) {
 *         double y = hugeFormula(x);
 *         if (somethingBadHappens) {
 *           throw new LocalException(x);
 *         }
 *         return y;
 *     }
 * }
 *
 * public void compute() {
 *     try {
 *         solver.solve(maxEval, new MyFunction(a, b, c), min, max);
 *     } catch (LocalException le) {
 *         // Retrieve the x value.
 *     }
 * }
 * </pre>
 *
 * As shown, the exception is local to the user's code and it is guaranteed
 * that Apache Commons Math will not catch it.
 *
 * @version $Id$
 */
public interface UnivariateFunction {
    /**
     * Compute the value of the function.
     *
     * @param x Point at which the function value should be computed.
     * @return the value of the function.
     * @throws IllegalArgumentException when the activated method itself can
     * ascertain that a precondition, specified in the API expressed at the
     * level of the activated method, has been violated.
     * When Commons Math throws an {@code IllegalArgumentException}, it is
     * usually the consequence of checking the actual parameters passed to
     * the method.
     */
    double value(double x);
}