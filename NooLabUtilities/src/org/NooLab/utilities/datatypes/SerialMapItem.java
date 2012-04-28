package org.NooLab.utilities.datatypes;

import java.io.Serializable;



public class SerialMapItem implements Serializable,SerialMapItemIntf{
	
	private static final long serialVersionUID = 1017690980575503835L;

	int index = -1;
	String name = "";
	Object data ;

	private String groupLabel;
	
	// ========================================================================
	public SerialMapItem(){
		
	}
	// ========================================================================
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public void setGroupLabel(String grouplabel) {
		groupLabel = grouplabel;
		
	}

	public String getGroupLabel() {
		return groupLabel;
	}
	
	
	
	
}
