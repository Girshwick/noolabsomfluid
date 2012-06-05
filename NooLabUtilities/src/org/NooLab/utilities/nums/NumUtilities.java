package org.NooLab.utilities.nums;

import java.util.Random;

public class NumUtilities {

	Random random=null;
	
	public NumUtilities(){
		
	}
	
	public int isWithInIntervals( double[][] intervals, double testvalue, int mode){
		
		boolean rB=false;
		int result= -1;
		double lower, upper ;
		
		try{
			
			for (int i=0;i<intervals.length;i++){
			
				result= -3;
				lower = intervals[i][0]; 
				upper = intervals[i][1];
				
				rB = isWithin( testvalue, lower, upper, mode) ;
				if (rB){
					result = i;
					break;
				}
			} // i->
			
			
			
		}catch(Exception e){
			result= -7;
			rB=false;
		}
		
		return result ;
	}
	/**
	 * 
	 * mode:
	 * = 0  -> without any border
	 * = -2 -> including only left border 
	 * = -1 -> including only right border 
	 * = 1  -> including both borders 
	 * 
	 * @param intval
	 * @param lower
	 * @param upper
	 * @param mode
	 * @return
	 */
	public boolean isWithin( int intval, int lower, int upper, int mode){
		return isWithin( intval, (double)lower, (double)upper, mode) ;
	}
	
	public boolean isWithin( double intval, double lower, double upper, int mode){
		boolean rb=true;
		
		if (mode>=1){
			rb = (intval>=lower) && (intval<=upper);
		}
		if (mode<=-2){
			rb = (intval>=lower) && (intval<upper);
		}
		if (mode== -1){
			rb = (intval>lower) && (intval<=upper);
		}
		if (mode==0){
			rb = (intval>lower) && (intval<upper);
		}
		
		return rb ;
	}
	
	public boolean isOdd( int intval){
		boolean rb=false;
		
		if (intval%2 != 0){
			rb=true;
		}
		
		return rb;
	}
	public int nextEvenNumber( int intval ){
		int result=intval;
		
		if ((intval>=0) && (intval%2!=0)){
			result = intval + 1;
		}
		
		return result;
	}
	
	public int prevEvenNumber( int intval ){
		int result=intval;
		
		if ((intval>2) && (intval%2!=0)){
			result = intval - 1;
		}
		
		return result;
	}
	
	public int nextOddNumber( int intval ){
		int result=intval;
		
		if ((intval>=0) && (intval%2==0)){
			result = intval + 1;
		}
		return result;
	}
	
	public int prevOddNumber( int intval ){
		int result=intval;
		
		if ((intval>1) && (intval%2==0)){
			result = intval - 1;
		}
		
		return result;
	}

	// better use the stuff from org.jmath ...
	public int randInRangeInc(int min, int max) {
		if (random==null){
			 random = new Random();
			 random.setSeed(453627) ;
		}
        return min + (int) (random.nextDouble() * (max - min));
	}
}
