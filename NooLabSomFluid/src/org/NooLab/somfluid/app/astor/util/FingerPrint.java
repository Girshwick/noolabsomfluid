package org.NooLab.somfluid.app.astor.util;

import java.util.ArrayList;

import org.NooLab.utilities.strings.ArrUtilities;
import org.math.array.StatisticSample;




public class FingerPrint {

	
	
	StatisticSample sampler ;
	// ========================================================================
	public FingerPrint(StatisticSample sampler){
		this.sampler = sampler;
		if (sampler==null){
			sampler = new StatisticSample(); 
		}
	}
	// ========================================================================
	
	

	public String createFingerprint(int fpLen, int digits, String separator) {
		
		String fpStr="";
		String profileStr ;
		double rndVal ;
		
		ArrayList<Double> profile = new ArrayList<Double>();
		
		
			
			
			for (int i=0;i<fpLen;i++){
				
				rndVal = sampler.getNextUniformRandom();
				profile.add( rndVal ); 
			}
			profileStr = ArrUtilities.arr2Text(profile, digits, separator);

			if (profileStr.endsWith(separator)){
				profileStr = profileStr.substring(0 , profileStr.length()-2) ;
			}
			fpStr = profileStr;
		return fpStr;
	}

}
