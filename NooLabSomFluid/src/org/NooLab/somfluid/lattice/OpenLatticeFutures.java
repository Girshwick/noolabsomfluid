package org.NooLab.somfluid.lattice;

import java.util.ArrayList;





public class OpenLatticeFutures {

	private ArrayList<LatticeFuture> items = new ArrayList<LatticeFuture>();
	
	
	public OpenLatticeFutures(){
		
	}


	public void add(LatticeFuture f) {
		 
		getItems().add(f);
	}


	public LatticeFuture getByGuid(String guid) {
		// 
		LatticeFuture item1, item = null; 
		
		int i=0;
		while (i<getItems().size()){
			
			try{
				item1 = getItems().get(i);
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
		
		getItems().remove(f) ;
	}


	public String getItemsStr() {
		String gStr="";
		
		
		
		for(int i=0;i<getItems().size();i++){
			
			gStr = gStr +getItems().get(i).processGuid + " ; ";
			
		}
		
		return gStr;
	}


	public void setItems(ArrayList<LatticeFuture> items) {
		this.items = items;
	}


	public ArrayList<LatticeFuture> getItems() {
		return items;
	}
	
	
	
}
