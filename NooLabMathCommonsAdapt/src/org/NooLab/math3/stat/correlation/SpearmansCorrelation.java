package org.NooLab.math3.stat.correlation;

import java.util.ArrayList;

import org.NooLab.math3.exception.DimensionMismatchException;
import org.NooLab.math3.exception.LocalizedFormats;
import org.NooLab.math3.exception.MathIllegalArgumentException;
import org.NooLab.math3.exception.NoDataException;
import org.NooLab.math3.linear.BlockRealMatrix;
import org.NooLab.math3.linear.RealMatrix;
import org.NooLab.math3.stat.ranking.NaturalRanking;
import org.NooLab.math3.stat.ranking.RankingAlgorithm;
 
 



/**
 * <p>Spearman's rank correlation. This implementation performs a rank
 * transformation on the input data and then computes {@link PearsonsCorrelation}
 * on the ranked data.</p>
 *
 * <p>By default, ranks are computed using {@link NaturalRanking} with default
 * strategies for handling NaNs and ties in the data (NaNs maximal, ties averaged).
 * The ranking algorithm can be set using a constructor argument.</p>
 *
 * @since 2.0
 * @version $Id$
 */

public class SpearmansCorrelation {

    /** Input data */
    private final RealMatrix data;

    /** Ranking algorithm  */
    private final RankingAlgorithm rankingAlgorithm;

    /** Rank correlation */
    private final PearsonsCorrelation rankCorrelation;

	private double[] referenceData;

	private boolean mvActivated;

	private double mvValue;

	
	// ========================================================================
    /**
     * Create a SpearmansCorrelation with the given input data matrix
     * and ranking algorithm.
     *
     * @param dataMatrix matrix of data with columns representing
     * variables to correlate
     * @param rankingAlgorithm ranking algorithm
     */
    public SpearmansCorrelation(final RealMatrix dataMatrix, final RankingAlgorithm rankingAlgorithm) {
    	
        this.data = dataMatrix.copy();
        this.rankingAlgorithm = rankingAlgorithm;
        rankTransform(data);
        rankCorrelation = new PearsonsCorrelation(data);
    }

    /**
     * Create a SpearmansCorrelation from the given data matrix.
     *
     * @param dataMatrix matrix of data with columns representing
     * variables to correlate
     */
    public SpearmansCorrelation(final RealMatrix dataMatrix) {
    	
        this(dataMatrix, new NaturalRanking());
    }

    /**
     * Create a SpearmansCorrelation without data.
     */
    public SpearmansCorrelation() {
        data = null;
        this.rankingAlgorithm = new NaturalRanking();
        rankCorrelation = null;
    }
    // ========================================================================
    
    /**
	 * if we have many comparisons against a column that remains unchanged, we need to import that one only once
	 * @param tvColData
	 */
	public void setReferenceData(ArrayList<Double> rValues) {
		// 
		referenceData = new double[rValues.size()];
		
		int n = rValues.size();
		for (int i=0;i<n;i++){
			referenceData[i] = rValues.get(i);
		}
	}

	/**
     * Calculate the Spearman Rank Correlation Matrix.
     *
     * @return Spearman Rank Correlation Matrix
     */
    public RealMatrix getCorrelationMatrix() {
        return rankCorrelation.getCorrelationMatrix();
    }

    /**
     * Returns a {@link PearsonsCorrelation} instance constructed from the
     * ranked input data. That is,
     * <code>new SpearmansCorrelation(matrix).getRankCorrelation()</code>
     * is equivalent to
     * <code>new PearsonsCorrelation(rankTransform(matrix))</code> where
     * <code>rankTransform(matrix)</code> is the result of applying the
     * configured <code>RankingAlgorithm</code> to each of the columns of
     * <code>matrix.</code>
     *
     * @return PearsonsCorrelation among ranked column data
     */
    public PearsonsCorrelation getRankCorrelation() {
        return rankCorrelation;
    }

    /**
     * Computes the Spearman's rank correlation matrix for the columns of the
     * input matrix.
     *
     * @param matrix matrix with columns representing variables to correlate
     * @return correlation matrix
     */
    public RealMatrix computeCorrelationMatrix( RealMatrix matrix ) {
    	
        RealMatrix matrixCopy = matrix.copy();
        rankTransform(matrixCopy);
        RealMatrix cmatrix ;
        cmatrix = new PearsonsCorrelation().computeCorrelationMatrix(matrixCopy);
        
        return cmatrix;
    }

    /**
     * Computes the Spearman's rank correlation matrix for the columns of the
     * input rectangular array.  The columns of the array represent values
     * of variables to be correlated.
     *
     * @param matrix matrix with columns representing variables to correlate
     * @return correlation matrix
     */
    public RealMatrix computeCorrelationMatrix(double[][] matrix) {
       return computeCorrelationMatrix(new BlockRealMatrix(matrix));
    }

    public double correlation( ArrayList<Double> yValues) {

		int n = yValues.size();
		double[] y = new double[n] ;
		
		for (int i=0;i<n;i++){
			y[i] = yValues.get(i);
		}
		
		return correlation(y);
    }
    public double correlation( ArrayList<Double> xValues, ArrayList<Double> yValues) {
    	 
    	
		double[] x, y; 
		
		x = new double[xValues.size()];
		y = new double[yValues.size()];
		
		int n = xValues.size();
		for (int i=0;i<n;i++){
			x[i] = xValues.get(i);
		}
		
		n = yValues.size();
		for (int i=0;i<n;i++){
			y[i] = yValues.get(i);
		}
		
    	return correlation( x,y);
    }
    
    public double correlation( double[] y) {
    	
		
		if (referenceData==null){
			throw(new NoDataException());
		}
		double[] x = new double[referenceData.length] ;
		
		System.arraycopy(referenceData, 0, x, 0, x.length) ;
    	return  correlation( x, y) ;
    }
    
    /**
     * Computes the Spearman's rank correlation coefficient between the two arrays.
     *
     * @param xArray first data array
     * @param yArray second data array
     * @return Returns Spearman's rank correlation coefficient for the two arrays
     * @throws DimensionMismatchException if the arrays lengths do not match
     * @throws MathIllegalArgumentException if the array length is less than 2
     */
    public double correlation(final double[] xArray, final double[] yArray) {
    	
        if (xArray.length != yArray.length) {
            throw new DimensionMismatchException(xArray.length, yArray.length);
            
        } else if (xArray.length < 2) {
            
        	throw new MathIllegalArgumentException(LocalizedFormats.INSUFFICIENT_DIMENSION, xArray.length, 2);
        } else {
        	
        	PearsonsCorrelation pec = new PearsonsCorrelation();
        	
        	if (mvActivated){
        		pec.activateMissingValueCheck(mvValue);
        		rankingAlgorithm.activateMissingValueCheck(mvValue);
        		// -> class NaturalRanking implements RankingAlgorithm
        	}
        	
            return pec.correlation( rankingAlgorithm.rank(xArray),
                    				rankingAlgorithm.rank(yArray));
        }
    }

    /**
     * Applies rank transform to each of the columns of <code>matrix</code>
     * using the current <code>rankingAlgorithm</code>
     *
     * @param matrix matrix to transform
     */
    private void rankTransform(RealMatrix matrix) {
        for (int i = 0; i < matrix.getColumnDimension(); i++) {
            matrix.setColumn(i, rankingAlgorithm.rank(matrix.getColumn(i)));
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