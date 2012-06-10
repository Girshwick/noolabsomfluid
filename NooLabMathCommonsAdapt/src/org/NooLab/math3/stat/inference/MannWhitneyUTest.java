package org.NooLab.math3.stat.inference;

 
import java.util.ArrayList;
import java.util.Arrays;

import org.NooLab.math3.distribution.NormalDistribution;
import org.NooLab.math3.exception.NoDataException;
import org.NooLab.math3.exception.NullArgumentException;
import org.NooLab.math3.random.RandomDataImpl;
import org.NooLab.math3.stat.MissingValue;
import org.NooLab.math3.stat.MissingValueIntf;
import org.NooLab.math3.stat.ranking.NaturalRanking;
import org.NooLab.math3.stat.ranking.TiesStrategy;
import org.apache.commons.math.exception.ConvergenceException;
 

import org.apache.commons.math.stat.ranking.NaNStrategy;
 
import org.apache.commons.math.util.FastMath;

/**
 * 
 * svn.apache.org/viewvc/commons/proper/math/trunk/src/main/java/org/apache/commons/math3/
 * 
 * 
 * An implementation of the Mann-Whitney U test (also called Wilcoxon rank-sum test).
 *
 * @version $Id$
 */
public class MannWhitneyUTest {

    /** Ranking algorithm. */
    private NaturalRanking naturalRanking;
    
    double mwUmax ;
    double pValue = -1 ;
    
    MissingValueIntf missingValues = new MissingValue();

	private double[] referenceData = null;
    
    // ========================================================================
    /**
     * Create a test instance using where NaN's are left in place and ties get
     * the average of applicable ranks. Use this unless you are very sure of
     * what you are doing.
     */
    public MannWhitneyUTest() {
        naturalRanking = new NaturalRanking(NaNStrategy.FIXED, TiesStrategy.AVERAGE );
        naturalRanking.importMissingValueDefinition( missingValues );
    } 

    public MannWhitneyUTest( RandomDataImpl randomdata) {  
        naturalRanking = new NaturalRanking(NaNStrategy.FIXED, TiesStrategy.AVERAGE , randomdata);
        naturalRanking.importMissingValueDefinition( missingValues );
    } 
    
    /**
     * Create a test instance using the given strategies for NaN's and ties.
     * Only use this if you are sure of what you are doing.
     *
     * @param nanStrategy
     *            specifies the strategy that should be used for Double.NaN's
     * @param tiesStrategy
     *            specifies the strategy that should be used for ties
     */
    public MannWhitneyUTest( final NaNStrategy nanStrategy,
                             final TiesStrategy tiesStrategy) {
    	
        naturalRanking = new NaturalRanking(nanStrategy, tiesStrategy);
        naturalRanking.importMissingValueDefinition(missingValues) ;
    }
    
    public void clearData(){
    	if (referenceData!=null){
    		referenceData = new double[0] ;
    	}
    	// naturalRanking does not hold any persistent data 
    }

	/**
	 * if we have many comparisons against a column that remains unchanged, we need to import that one only once
	 * @param tvColData
	 */
	public void setReferenceData(ArrayList<Double> rValues) {
		// 
		double[] refData = new double[rValues.size()];
		
		int n = rValues.size();
		for (int i=0;i<n;i++){
			refData[i] = rValues.get(i);
		}
		setReferenceData(refData);
	}

	public void setReferenceData(double[] rValues) {
		
		int n = rValues.length;
		
		referenceData = new double[n];
		
		ensureDataConformance(referenceData);
		
	}

	/**
	 * Returns the asymptotic <i>observed significance level</i>, or <a href=
	 * "http://www.cas.lancs.ac.uk/glossary_v1.1/hyptest.html#pvalue">
	 * p-value</a>, associated with a <a
	 * href="http://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U"> Mann-Whitney
	 * U statistic</a> comparing mean for two independent samples.
	 * <p>
	 * Let X<sub>i</sub> denote the i'th individual of the first sample and
	 * Y<sub>j</sub> the j'th individual in the second sample. Note that the
	 * samples would often have different length.
	 * </p>
	 * <p>
	 * <strong>Preconditions</strong>:
	 * <ul>
	 * <li>All observations in the two samples are independent.</li>
	 * <li>The observations are at least ordinal (continuous are also ordinal).</li>
	 * </ul>
	 * </p><p>
	 * Ties give rise to biased variance at the moment. See e.g. <a
	 * href="http://mlsc.lboro.ac.uk/resources/statistics/Mannwhitney.pdf"
	 * >http://mlsc.lboro.ac.uk/resources/statistics/Mannwhitney.pdf</a>.</p>
	 *
	 * @param x the first sample
	 * @param y the second sample
	 * @return asymptotic p-value
	 * @throws NullArgumentException if {@code x} or {@code y} are {@code null}.
	 * @throws NoDataException if {@code x} or {@code y} are zero-length.
	 * @throws ConvergenceException if the p-value can not be computed due to a
	 * convergence error
	 * @throws MaxCountExceededException if the maximum number of iterations
	 * is exceeded
	 */
	public double mannWhitneyUTest(final double[] x, final double[] y)
	    																throws 	NullArgumentException, 
	    																		NoDataException,
	    																		ConvergenceException, 
	    																		Exception { // MaxCountExceededException
		// this also removes missing values !
	    // ensureDataConformance(x, y);
	
	    mwUmax = mannWhitneyU(x, y);
	
	    /*
	     * It can be shown that U1 + U2 = n1 * n2
	     */
	    final double Umin = x.length * y.length - mwUmax ;
	
	    double pValue = calculateAsymptoticPValue(Umin, x.length, y.length);
	    if (Double.isNaN(pValue)){
	    	pValue=0.0;
	    }
	    return pValue;
	}

	// ========================================================================
    
    
	public double mannWhitneyU( ArrayList<Double> xValues, 
								ArrayList<Double>  yValues)
																throws 	Exception,
																		NullArgumentException, 
																		NoDataException {
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
		

		return mannWhitneyU( x, y);
	}
	
	public double mannWhitneyU( ArrayList<Double> yValues)	throws 	NullArgumentException, 
																	Exception,
																	NoDataException {
		
		int n = yValues.size();
		double[] y = new double[n] ;
		
		for (int i=0;i<n;i++){
			y[i] = yValues.get(i);
		}
		
		return mannWhitneyU( y );
	}
	
	public double mannWhitneyU( final double[] y)	throws 	Exception,
															NullArgumentException, 
															NoDataException {
		
		if (referenceData==null){
			throw(new NoDataException());
		}
		double[] x = new double[referenceData.length] ;
		
		System.arraycopy(referenceData, 0, x, 0, x.length) ;
		
		return mannWhitneyU( x, y);
	}
    /**
	 * Computes the <a
	 * href="http://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U"> Mann-Whitney U statistic</a> comparing mean for two independent samples possibly of different length.
	 * <p>
	 * This statistic can be used to perform a Mann-Whitney U test evaluating the null hypothesis that 
	 * the two independent samples has equal mean.
	 * </p>
	 * <p>
	 * Let X<sub>i</sub> denote the i'th individual of the first sample and
	 * Y<sub>j</sub> the j'th individual in the second sample. Note that the samples would often have different length.
	 * </p>
	 * <p>
	 * <strong>Preconditions</strong>:
	 * <ul>
	 * <li>All observations in the two samples are independent.</li>
	 * <li>The observations are at least ordinal (continuous are also ordinal).</li>
	 * </ul>
	 * </p>
	 * The problem with this (from apache math commons) is that there is not correction for ties
	 * </br>
	 *
	 * @param x the first sample
	 * @param y the second sample
	 * @return Mann-Whitney U statistic (maximum of U<sup>x</sup> and U<sup>y</sup>)
	 * @throws NullArgumentException if {@code x} or {@code y} are {@code null}.
	 * @throws NoDataException if {@code x} or {@code y} are zero-length.
	 */
	public double mannWhitneyU(final double[] x, final double[] y)
	    															throws 	Exception,
	    																	NullArgumentException, 
	    																	NoDataException {
	
	    // ensureDataConformance(x, y);
	
	    final double[] z = concatenateSamples(x, y);
	    final double[] ranks = naturalRanking.rank(z);
	
	    double sumRankX = 0, sumRankY=0;
	
	    /*
	     * The ranks for x is in the first x.length entries in ranks because x
	     * is in the first x.length entries in z
	     */
	    for (int i = 0; i < x.length; ++i) {
	        sumRankX += ranks[i];
	    }
	    // for (int i = x.length; i<ranks.length;++i) { sumRankY += ranks[i];}
	       
	    
	    
	    /*
	     * U1 = R1 - (n1 * (n1 + 1)) / 2 where R1 is sum of ranks for sample 1,
	     * e.g. x, n1 is the number of observations in sample 1.
	     */
	    double U1 = sumRankX - (double)((double)x.length * ((double)x.length + 1.0)) / 2.0;
	
	    // double U2b = sumRankY - (y.length * (y.length + 1)) / 2;
	    
	    /*
	     * It can be shown that U1 + U2 = n1 * n2
	     */
	    double U2  = x.length * y.length - U1;
	
	    // U2 = Math.min(U2, U2b) ;
	    try {
			pValue = calculateAsymptoticPValue( Math.max(U1,U2),x.length ,y.length);
		} catch (Exception e) {
			throw(new Exception("problem when calculating p-value..."));
		} 
                
                
	    return FastMath.max(U1, U2);
	}

    public double[] ensureDataConformance( double[] x )
															throws 	NullArgumentException, 
																	NoDataException {
    	
        if (x == null)   {
                throw new NullArgumentException();
            }
            // should be changed to <=1, 8 ?
            if (x.length == 0 ) {
                throw new NoDataException();
            }
            
            if ((missingValues!=null ) && (missingValues.isActive() )){
            	ArrayList<Double> xlist = new ArrayList<Double>();
             

            	for (int i=0;i<x.length;i++){
            		
            		if ( missingValues.isMissingValue( x[i]) == false){
            			xlist.add( x[i] ); 
            		}
            	}
            	 
            	
            	if (xlist.size()<x.length){
            		x = new double[ xlist.size()] ;
            		for (int i=0;i<x.length;i++){
            			x[i] = xlist.get(i);
            		}
            		
            	}
            	 
            } // mv?
            
            // again checking
            if (x.length == 0 ) {
                    throw new NoDataException();
            }
            return x;
    }
    
    
	/**
     * Ensures that the provided arrays fulfills the assumptions.
     *
     * @param x first sample
     * @param y second sample
     * @throws NullArgumentException if {@code x} or {@code y} are {@code null}.
     * @throws NoDataException if {@code x} or {@code y} are zero-length.
     */
    private void ensureDataConformance( double[] x,  double[] y)
        															throws 	NullArgumentException, 
        																	NoDataException {

        if (x == null ||
            y == null) {
            throw new NullArgumentException();
        }
        // should be changed to <=1, 8 ?
        if (x.length == 0 || y.length == 0) {
            throw new NoDataException();
        }
        
        if ((missingValues!=null ) && (missingValues.isActive() )){
        	ArrayList<Double> xlist = new ArrayList<Double>();
        	ArrayList<Double> ylist = new ArrayList<Double>();

        	for (int i=0;i<x.length;i++){
        		
        		if ( missingValues.isMissingValue( x[i]) == false){
        			xlist.add( x[i] ); 
        		}
        	}
        	for (int i=0;i<y.length;i++){
        		if ( missingValues.isMissingValue( y[i]) == false){
        			ylist.add( y[i] ); 
        		}
        	}
        	
        	if (xlist.size()<x.length){
        		x = new double[ xlist.size()] ;
        		for (int i=0;i<x.length;i++){
        			x[i] = xlist.get(i);
        		}
        		
        	}
        	if (ylist.size()<y.length){
        		y = new double[ ylist.size()] ;
        		for (int i=0;i<y.length;i++){
        			y[i] = ylist.get(i) ;
        		}
        	}
        } // mv?
        
        // again checking
        if (x.length == 0 || y.length == 0) {
                throw new NoDataException();
        }
    }

    /** Concatenate the samples into one array.
     * @param x first sample
     * @param y second sample
     * @return concatenated array
     */
    private double[] concatenateSamples(final double[] x, final double[] y) {
    	
        final double[] z = new double[x.length + y.length];

        System.arraycopy(x, 0, z, 0, x.length);
        System.arraycopy(y, 0, z, x.length, y.length);

        return z;
    }

    /**
     * @param Umin smallest Mann-Whitney U value
     * @param n1 number of subjects in first sample
     * @param n2 number of subjects in second sample
     * @return two-sided asymptotic p-value
     * @throws ConvergenceException if the p-value can not be computed
     * due to a convergence error
     * @throws MaxCountExceededException if the maximum number of
     * iterations is exceeded
     */
    private double calculateAsymptoticPValue(final double Umin,
                                             final int n1,
                                             final int n2)
        													throws 	ConvergenceException, 
        													Exception { // MaxCountExceededException

		double n1n2prod, pValue = 0,z,EU,VarU;
		try {

			n1n2prod = (double) n1 * (double) n2;
			pValue = 0.0;

			// http://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U#Normal_approximation

			EU = (double) n1n2prod / 2.0;
			VarU = (double) (n1n2prod * (double) (n1 + n2 + 1.0)) / 12.0;
			double stdev = FastMath.sqrt(VarU);
			double varu = Math.max(0,VarU);
			if (varu==0){
				z = 1000.0;
			}else{
				z = (Umin - EU) / stdev;
			}

			NormalDistribution standardNormal = new NormalDistribution(0, 1);

			pValue = standardNormal.cumulativeProbability(z/2.0);

			if (Double.isNaN(pValue)){
				pValue = 0.0;
			}
		} catch (Exception e) {
			pValue = -1.0;
		}

		return pValue;
    }

    // ------------------------------------------------------------------------
    /**
     * if another one than the default is required
     */
    public void importMissingValueDefinition(MissingValueIntf mv) {
		missingValues = mv;
		naturalRanking.importMissingValueDefinition( missingValues );
	}

	public double getMannWhitneyUvalue() {
		return mwUmax;
	}

	public double getpValue() {
		return pValue;
	}

}
