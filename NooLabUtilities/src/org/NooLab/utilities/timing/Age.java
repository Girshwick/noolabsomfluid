package org.NooLab.utilities.timing;

public class Age {
	
	
	static long timeofDelivery=0;
	static long ageOfExpiry = 0;
	
	
	public Age( long timeofdelivery, long ageofexpiry){
		
		timeofDelivery = timeofdelivery;
		ageOfExpiry = ageofexpiry;
	}
	
	
	public static long getScaledTimeValue( long agevalue, String scale ){
		long factor = 1;
		
		
		scale = scale.toLowerCase();
		 
		
		if ((scale.contains("day")) || (scale.startsWith("d"))){
			factor = 24*60*60*1000 ;
		}

		if ((scale.contains("hour")) || (scale.startsWith("h"))){
			factor = 60*60*1000 ;
		}
		if ((scale.contains("min")) || (scale.startsWith("mi"))){
			factor = 60*1000 ;
		}
		if ((scale.contains("sec")) || (scale.startsWith("s"))){
			factor = 1000 ;
		}
		return factor;
	}
	/**
	 * if the transaction has its own expiry (>0), this will be checked !
	 * Otherwise the provided value will be taken<br/>
	 *  
	 *  scale : sec, min, hour, days
	 *  
	 * @param agevalue
	 * @param scale
	 * @return
	 */
	public boolean isOld( long agevalue, String scale ){
		
		long factor=1;
		
		
		factor = getScaledTimeValue(agevalue, scale);
		
		agevalue = agevalue * factor;
		
		return isOld(agevalue);
	}
	
	public static boolean isOld( long millisAge ){
		boolean rB=false;
		long traxpiry = -1, tdiff;
		long now = System.currentTimeMillis();
		
		traxpiry = ageOfExpiry;
		
		if (timeofDelivery<1001){
			timeofDelivery = System.currentTimeMillis() ;
		}
		
		tdiff = now - timeofDelivery;
		
		if (traxpiry<=0){

			if (timeofDelivery > 1000) {
				if (tdiff > millisAge) {
					rB = true;
				}
			}
		}else{

			if (timeofDelivery > 1000) {
			
				if ( now - timeofDelivery> traxpiry) {
					rB = true;
				}
			}
		
			
		}
		
		return rB;
	}
	
	public static long getAge( int byState ){
		boolean rB=false;
		long millisAge=0 ;
		
		if (timeofDelivery<1001){
			timeofDelivery = System.currentTimeMillis() ;
		}
		millisAge = (System.currentTimeMillis() - timeofDelivery );
		
		if ( byState <= 1 ){
			millisAge = (System.currentTimeMillis() -timeofDelivery );
		}
		if ( byState <= 2 ){
			millisAge = (System.currentTimeMillis() - timeofDelivery );
		}
		
		return millisAge ;
	}
	
	
}
