package org.NooLab.utilities.datatypes;

import java.io.Serializable;
import java.util.ArrayList;




public class SerialMap implements Serializable{

	private static final long serialVersionUID = -1579996560391619523L;

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
		
		item.activationStatus = 0;
		
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


	public ArrayList<SerialMapItemIntf> getItems() {
		return items;
	}


	public void setItems(ArrayList<SerialMapItemIntf> items) {
		this.items = items;
	}
	
	
	
}
