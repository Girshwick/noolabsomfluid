package org.NooLab.somideals;


/**
 * 
 * Background: 
 * 
 * simulating data as surrogates is very important in idea finding, as surrogates
 * try to keep the relations without noise, non-linearity and hence also without folds
 * and complexity.
 * Thus, it is a "structural" analogy making, which is smoothing the expression space.
 * Since there is a tendency for linearization, it is a necessary component to create
 * concepts that could be combined by means of logics  
 * 
 * 
 * 
 * this class organizes a recursive loop for distilling prototypical patterns
 * 
 * this is accomplished by a loop made from 
 *    - som learning
 *    - extracting profiles
 *    - simulating data around profiles using (normal,lognormal) 
 *      - surrogates (1-model)
 *      - randoms    (0-model)
 *    - reducing map size,
 *    - ... som learning ...
 * 
 * additionally, a binning of the profile values could be performed into [2..3(5)] classes 
 * as a result...
 * 
 * the thing is called Koborov lattic set... there is a package foing this called SSJ
 * 
 * 
 * SomIdals may work on any kind of SomLattice : mono [target, astore], prob,
 * 
 * multiple correlated data:
 * rom wiki on "Cholesky decomposition ", chp. "Monte Carlo simulation"

The Cholesky decomposition is commonly used in the Monte Carlo method for simulating systems 
with multiple correlated variables: The correlation matrix is decomposed, to give the 
lower-triangular L. Applying this to a vector of uncorrelated samples, u, produces a 
sample vector Lu with the covariance properties of the system being modeled.[2]

 * 
 * Daubechies wavelets of order 6 were employed to generate surrogate data
 * 
 * 
 * Schreiber and Schmitz (2000), Constrained Randomisation of Time
Series for Nonlinearity Tests, Nonlinear Dynamics and Statistics.

Schreiber and Schmitz (2000), Surrogate Time Series, Physica D, 143,
346-382.


 * 
 *
 *
 *


 */
public class SomIdeals {

}
