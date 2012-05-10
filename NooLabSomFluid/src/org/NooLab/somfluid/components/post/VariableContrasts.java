package org.NooLab.somfluid.components.post;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.core.engines.det.SomHostIntf;



public class VariableContrasts implements Serializable{
 
	private static final long serialVersionUID = 8820670806194890181L;
	
	ArrayList<VariableContrast> items = new ArrayList<VariableContrast>();

	private int tvColumnIndex;

	private boolean remainsUndefined;

	String resultAsStringTable;
	
	// ========================================================================
	public VariableContrasts(SomHostIntf somHost){
		
	}
	// ------------------------------------------------------------------------

	public int size(){
		return items.size();
	}
	
	public VariableContrast getItemByLabel(String varlabel){
		VariableContrast vci = null;
		String vlabel;
		
		for (int i=0;i<items.size();i++){
			vlabel = items.get(i).variableLabel ;
			if (vlabel.contentEquals(varlabel)){
				vci = items.get(i);
				break;
			}
		}
		
		return vci;
	}
	
	public VariableContrast getItem(int index){
		VariableContrast vci = null;
		
		if ((index>=0) && (index<items.size())){
			vci = items.get(index) ;
		}
		
		return vci;
	}
	
	public ArrayList<VariableContrast> getItems() {
		return items;
	}

	
	public void addItem( VariableContrast vci ) {
		items.add(vci) ;
	}
	
	
	
	
	public String getResultStringTable( ) {
		return resultAsStringTable;
	}
	public void setResultStringTable( String tablestr) {
		resultAsStringTable = tablestr ;
	}

	// ........................................................................

	public void setItems(ArrayList<VariableContrast> items) {
		this.items = items;
	}

	public void setTvIndex(int tvColIndex) {
		tvColumnIndex = tvColIndex;
		
	}

	public int getTvColumnIndex() {
		return tvColumnIndex;
	}

	public void setTvColumnIndex(int tvColumnIndex) {
		this.tvColumnIndex = tvColumnIndex;
	}

	public void setRemainsUndefined(boolean flag) {
		remainsUndefined = flag;
	}

	public boolean isRemainsUndefined() {
		return remainsUndefined;
	}
	
	
	
}
