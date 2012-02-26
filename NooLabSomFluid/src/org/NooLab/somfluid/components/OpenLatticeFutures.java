package org.NooLab.somfluid.components;

import java.util.ArrayList;




public class OpenLatticeFutures {

	ArrayList<LatticeFuture> items = new ArrayList<LatticeFuture>();
	
	
	public OpenLatticeFutures(){
		
	}


	public void add(LatticeFuture f) {
		 
		items.add(f);
	}


	public LatticeFuture getByGuid(String guid) {
		// 
		LatticeFuture item1, item = null; 
		
		int i=0;
		while (i<items.size()){
			
			try{
				item1 = items.get(i);
				if (item1.processGuid.contentEquals(guid)){
					item = item1;
					break;
				}
				
			}catch(Exception e){
				
			}
			i++;
		}
		return item;
	}
	
	public void removeByGuid(String guid){
		
		LatticeFuture f; 
		
		f = getByGuid(guid);
		
		items.remove(f) ;
	}


	public String getItemsStr() {
		String gStr="";
		
		
		
		for(int i=0;i<items.size();i++){
			
			gStr = gStr +items.get(i).processGuid + " ; ";
			
		}
		
		return gStr;
	}
	
	
	
}
