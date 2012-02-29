package org.NooLab.utilities.nums;

public class NumUtilities {

	public NumUtilities(){
		
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

	public int randInRangeInc(int min, int max) {
        return min + (int) (Math.random() * (max - min));
	}
}
