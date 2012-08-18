package org.NooLab.field.repulsive.components.data;

public class RequestBorder {

	public static final int _TYPE_VALUES = 1;
	public static final int _TYPE_ARRAY  = 2;
	
	public String requestGuid = "0";
	
	public int type = -1 ;
	
	public int upperBorder;
	public int lowerBorder;
	
	public int[] excludedParticleIndexes = new int[0] ;
	
	public RequestBorder(){
		
	}

	public boolean indexIsAccessible(int ccPIndex, int nMax) {
		boolean rB=true;
		 
		if (type==_TYPE_VALUES){
			
			if (lowerBorder>0){
				rB = ccPIndex >= lowerBorder;
			}
			if ((upperBorder>0) && (upperBorder<nMax)){
				rB = ccPIndex <= upperBorder;
			}
			
		}
		if (type>=_TYPE_ARRAY){
			// TODO: int array of the index values of excluded particles
			
			
		}
		

		return false;
	}
	
	
}
