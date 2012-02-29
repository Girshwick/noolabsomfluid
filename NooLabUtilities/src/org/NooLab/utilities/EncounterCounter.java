package org.NooLab.utilities;

import java.util.HashMap;
import java.util.Map;



public class EncounterCounter {

	Map<Long,Integer> cMap= new HashMap<Long,Integer>();
	
	public EncounterCounter(){
		
	}
	
	public int byID( long id){
	
		int result=0 ,vs;
		
		if (cMap.containsKey(id)==false){
			cMap.put(id, 1); result = 1;
		}else{
			vs = cMap.get(id) + 1;
			cMap.put(id, vs); result = vs;
		}
		
		return result;
	}
	
	public void removeSlot(int id){
		
		if (cMap.containsKey(id)){
			cMap.remove(id) ;
		}
		
	}
	
	
}
