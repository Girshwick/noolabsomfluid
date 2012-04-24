package org.NooLab.math3.random;

import java.math.BigInteger;

public class Binomial {


	public static BigInteger get(final int N, final int K) {
	    BigInteger ret = BigInteger.ONE;
	    for (int k = 0; k < K; k++) {
	        ret = ret.multiply(BigInteger.valueOf(N-k)).divide(BigInteger.valueOf(k+1));
	    }
	    
	    // System.out.println(binomial(133, 71));
	    
	    return ret;
	}

}


