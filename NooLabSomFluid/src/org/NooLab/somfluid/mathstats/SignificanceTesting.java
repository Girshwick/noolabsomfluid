package org.NooLab.somfluid.mathstats;


import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.*;



public class SignificanceTesting {

	public static final int _DISTRIBUTION_CHI2 = 1 ;
	
	public SignificanceTesting(){
		
	}

	/**
	 * 1-tailed test
	 * 
	 * @param distributionID
	 * @param testStatistics
	 * @param df
	 * @param alpha
	 * @return
	 */
	public double againstDistribution(int distributionID, double testStatistics, int df ) {
		
		// we have to test whether our value for "testStatistics" is larger than the critical value of the dstribution
		
		ChiSquaredDistribution chi;
		
		chi = new ChiSquaredDistributionImpl( df ) ;
		
		double testalpha = 1.0;
		
		try {
		
			testalpha = 1.0 - chi.cumulativeProbability( testStatistics );

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		 
		
		return testalpha;
	}

		
}
