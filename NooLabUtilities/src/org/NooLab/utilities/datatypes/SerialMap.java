package org.NooLab.utilities.datatypes;

import java.util.ArrayList;




public class SerialMap {

	ArrayList<SerialMapItemIntf> items = new ArrayList<SerialMapItemIntf> (); 
	
	// ------------------------------------------------------------------------
	public SerialMap(){
		
	}
	// ------------------------------------------------------------------------
	
	
	public SerialMapItem createItem( String name, Object data) throws Exception{
		
		SerialMapItem item;
		
		item = new SerialMapItem();
		
		item.name = name;
		item.data = data ;
		
		
		return item;
	}
	
	
	public SerialMapItemIntf addNewItem( String name, Object data) throws Exception{
		
		SerialMapItem item;
		
		item = createItem( name, data) ;
		addItem((SerialMapItemIntf) item);
		
		return (SerialMapItemIntf) item;
	}
	
	
	public void addItem( SerialMapItemIntf item ){
		
		items.add(item) ;
	}

	public Object getObject( String name) {
		
		SerialMapItem item,ritem=null;
		
		for (int i=0;i<items.size();i++){
			
			item = (SerialMapItem) items.get(i);
			if (item.name.contentEquals(name)){
				ritem = item;
				break;
			}
		}
		
		return ritem;
	}
	
	
	
}
