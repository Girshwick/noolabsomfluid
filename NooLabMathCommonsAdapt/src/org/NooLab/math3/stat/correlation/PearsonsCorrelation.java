package org.NooLab.math3.stat.correlation;

import org.NooLab.math3.distribution.TDistribution;
import org.NooLab.math3.exception.DimensionMismatchException;
import org.NooLab.math3.exception.LocalizedFormats;
import org.NooLab.math3.exception.MathIllegalArgumentException;
import org.NooLab.math3.exception.NullArgumentException;
import org.NooLab.math3.linear.BlockRealMatrix;
import org.NooLab.math3.linear.RealMatrix;
import org.NooLab.math3.util.FastMath;
import org.apache.commons.math.stat.regression.SimpleRegression;

/**
 * Computes Pearson's product-moment correlation coefficients for pairs of arrays
 * or columns of a matrix.
 *
 * <p>The constructors that take <code>RealMatrix</code> or
 * <code>double[][]</code> arguments generate correlation matrices.  The
 * columns of the input matrices are assumed to represent variable values.
 * Correlations are given by the formula</p>
 * <code>cor(X, Y) = &Sigma;[(x<sub>i</sub> - E(X))(y<sub>i</sub> - E(Y))] / [(n - 1)s(X)s(Y)]</code>
 * where <code>E(X)</code> is the mean of <code>X</code>, <code>E(Y)</code>
 * is the mean of the <code>Y</code> values and s(X), s(Y) are standard deviations.
 *
 * @version $Id$
 * @since 2.0
 */
public class PearsonsCorrelation {

    /** correlation matrix */
    private final RealMatrix correlationMatrix;

    /** number of observations */
    private final int nObs;

	private boolean mvActivated = false;

	private double mvValue;

    /**
     * Create a PearsonsCorrelation instance without data
     */
    public PearsonsCorrelation() {
        super();
        correlationMatrix = null;
        nObs = 0;
    }

    /**
     * Create a PearsonsCorrelation from a rectangular array
     * whose columns represent values of variables to be correlated.
     *
     * @param data rectangular array with columns representing variables
     * @throws IllegalArgumentException if the input data array is not
     * rectangular with at least two rows and two columns.
     */
    public PearsonsCorrelation(double[][] data) {
        this(new BlockRealMatrix(data));
    }

    /**
     * Create a PearsonsCorrelation from a RealMatrix whose columns
     * represent variables to be correlated.
     *
     * @param matrix matrix with columns representing variables to correlate
     */
    public PearsonsCorrelation(RealMatrix matrix) {
        checkSufficientData(matrix);
        nObs = matrix.getRowDimension();
        correlationMatrix = computeCorrelationMatrix(matrix);
    }

    /**
     * Create a PearsonsCorrelation from a {@link Covariance}.  The correlation
     * matrix is computed by scaling the Covariance's covariance matrix.
     * The Covariance instance must have been created from a data matrix with
     * columns representing variable values.
     *
     * @param covariance Covariance instance
     */
    public PearsonsCorrelation(Covariance covariance) {
        RealMatrix covarianceMatrix = covariance.getCovarianceMatrix();
        if (covarianceMatrix == null) {
            throw new NullArgumentException(LocalizedFormats.COVARIANCE_MATRIX);
        }
        nObs = covariance.getN();
        correlationMatrix = covarianceToCorrelation(covarianceMatrix);
    }

    /**
     * Create a PearsonsCorrelation from a covariance matrix.  The correlation
     * matrix is computed by scaling the covariance matrix.
     *
     * @param covarianceMatrix covariance matrix
     * @param numberOfObservations the number of observations in the dataset used to compute
     * the covariance matrix
     */
    public PearsonsCorrelation(RealMatrix covarianceMatrix, int numberOfObservations) {
        nObs = numberOfObservations;
        correlationMatrix = covarianceToCorrelation(covarianceMatrix);

    }

    /**
     * Returns the correlation matrix
     *
     * @return correlation matrix
     */
    public RealMatrix getCorrelationMatrix() {
        return correlationMatrix;
    }

    /**
     * Returns a matrix of standard errors associated with the estimates
     * in the correlation matrix.<br/>
     * <code>getCorrelationStandardErrors().getEntry(i,j)</code> is the standard
     * error associated with <code>getCorrelationMatrix.getEntry(i,j)</code>
     * <p>The formula used to compute the standard error is <br/>
     * <code>SE<sub>r</sub> = ((1 - r<sup>2</sup>) / (n - 2))<sup>1/2</sup></code>
     * where <code>r</code> is the estimated correlation coefficient and
     * <code>n</code> is the number of observations in the source dataset.</p>
     *
     * @return matrix of correlation standard errors
     */
    public RealMatrix getCorrelationStandardErrors() {
        int nVars = correlationMatrix.getColumnDimension();
        double[][] out = new double[nVars][nVars];
        for (int i = 0; i < nVars; i++) {
            for (int j = 0; j < nVars; j++) {
                double r = correlationMatrix.getEntry(i, j);
                out[i][j] = FastMath.sqrt((1 - r * r) /(nObs - 2));
            }
        }
        return new BlockRealMatrix(out);
    }

    /**
     * Returns a matrix of p-values associated with the (two-sided) null
     * hypothesis that the corresponding correlation coefficient is zero.
     * <p><code>getCorrelationPValues().getEntry(i,j)</code> is the probability
     * that a random variable distributed as <code>t<sub>n-2</sub></code> takes
     * a value with absolute value greater than or equal to <br>
     * <code>|r|((n - 2) / (1 - r<sup>2</sup>))<sup>1/2</sup></code></p>
     * <p>The values in the matrix are sometimes referred to as the
     * <i>significance</i> of the corresponding correlation coefficients.</p>
     *
     * @return matrix of p-values
     * @throws org.apache.commons.math3.exception.MaxCountExceededException
     * if an error occurs estimating probabilities
     */
    public RealMatrix getCorrelationPValues() {
        TDistribution tDistribution = new TDistribution(nObs - 2);
        int nVars = correlationMatrix.getColumnDimension();
        double[][] out = new double[nVars][nVars];
        for (int i = 0; i < nVars; i++) {
            for (int j = 0; j < nVars; j++) {
                if (i == j) {
                    out[i][j] = 0d;
                } else {
                    double r = correlationMatrix.getEntry(i, j);
                    double t = FastMath.abs(r * FastMath.sqrt((nObs - 2)/(1 - r * r)));
                    out[i][j] = 2 * tDistribution.cumulativeProbability(-t);
                }
            }
        }
        return new BlockRealMatrix(out);
    }


    /**
     * Computes the correlation matrix for the columns of the
     * input matrix.
     *
     * @param matrix matrix with columns representing variables to correlate
     * @return correlation matrix
     */
    public RealMatrix computeCorrelationMatrix(RealMatrix matrix) {
    	
        int nVars = matrix.getColumnDimension();
        RealMatrix outMatrix = new BlockRealMatrix(nVars, nVars);
        
        for (int i = 0; i < nVars; i++) {
            for (int j = 0; j < i; j++) {
              double corr = correlation(matrix.getColumn(i), matrix.getColumn(j));
              outMatrix.setEntry(i, j, corr);
              outMatrix.setEntry(j, i, corr);
            }
            outMatrix.setEntry(i, i, 1d);
        }
        return outMatrix;
    }

    /**
     * Computes the correlation matrix for the columns of the
     * input rectangular array.  The colums of the array represent values
     * of variables to be correlated.
     *
     * @param data matrix with columns representing variables to correlate
     * @return correlation matrix
     */
    public RealMatrix computeCorrelationMatrix(double[][] data) {
       return computeCorrelationMatrix(new BlockRealMatrix(data));
    }

    /**
     * Computes the Pearson's product-moment correlation coefficient between the two arrays.
     *
     * </p>Throws IllegalArgumentException if the arrays do not have the same length
     * or their common length is less than 2</p>
     *
     * @param xArray first data array
     * @param yArray second data array
     * @return Returns Pearson's correlation coefficient for the two arrays
     * @throws DimensionMismatchException if the arrays lengths do not match
     * @throws MathIllegalArgumentException if there is insufficient data
     */
    public double correlation(final double[] xArray, final double[] yArray) {
    	
    	boolean takeIt;
        SimpleRegression regression = new SimpleRegression();
        
        if (xArray.length != yArray.length) {
            throw new DimensionMismatchException(xArray.length, yArray.length);
        } else if (xArray.length < 2) {
        	
            throw new MathIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, xArray.length, 2);
        } else {
        	
            for(int i=0; i<xArray.length; i++) {
            	takeIt = true;
            	if (mvActivated){
            		if ((xArray[i]==mvValue) || (yArray[i]==mvValue)){
            			takeIt = false;
            		}
            	}
            	if (takeIt){
            		regression.addData(xArray[i], yArray[i]);
            	}
            }
            return regression.getR();
        }
    }

    /**
     * Derives a correlation matrix from a covariance matrix.
     *
     * <p>Uses the formula <br/>
     * <code>r(X,Y) = cov(X,Y)/s(X)s(Y)</code> where
     * <code>r(&middot,&middot;)</code> is the correlation coefficient and
     * <code>s(&middot;)</code> means standard deviation.</p>
     *
     * @param covarianceMatrix the covariance matrix
     * @return correlation matrix
     */
    public RealMatrix covarianceToCorrelation(RealMatrix covarianceMatrix) {
        int nVars = covarianceMatrix.getColumnDimension();
        RealMatrix outMatrix = new BlockRealMatrix(nVars, nVars);
        for (int i = 0; i < nVars; i++) {
            double sigma = FastMath.sqrt(covarianceMatrix.getEntry(i, i));
            outMatrix.setEntry(i, i, 1d);
            for (int j = 0; j < i; j++) {
                double entry = covarianceMatrix.getEntry(i, j) /
                       (sigma * FastMath.sqrt(covarianceMatrix.getEntry(j, j)));
                outMatrix.setEntry(i, j, entry);
                outMatrix.setEntry(j, i, entry);
            }
        }
        return outMatrix;
    }

    /**
     * Throws IllegalArgumentException of the matrix does not have at least
     * two columns and two rows
     *
     * @param matrix matrix to check for sufficiency
     * @throws MathIllegalArgumentException if there is insufficient data
     */
    private void checkSufficientData(final RealMatrix matrix) {
        int nRows = matrix.getRowDimension();
        int nCols = matrix.getColumnDimension();
        if (nRows < 2 || nCols < 2) {
            throw new MathIllegalArgumentException(LocalizedFormats.INSUFFICIENT_ROWS_AND_COLUMNS,
                                                   nRows, nCols);
        }
    }

	public void activateMissingValueCheck(double mvc) {
		mvActivated = true ;
		mvValue = mvc ;
	}
	public void deactivateMissingValueCheck() {
		mvActivated = false;
	}

}
