package org.NooLab.somtransform.algo.intf;

import java.io.Serializable;
import java.util.ArrayList;

 


public class AlgorithmParameters implements Serializable{


	private static final long serialVersionUID = -2171692417021968266L;
	

	protected ArrayList<AlgorithmParameter> items = new ArrayList<AlgorithmParameter>();

	transient AlgorithmIntf parentAlgo ;


	transient private boolean recalculationBlocked = false;
	
	
	// ------------------------------------------------------------------------
	public AlgorithmParameters( AlgorithmIntf parent ){
		parentAlgo = parent ;
		
	}
	
	/** for cloning , incl. on the level of the items */
	public AlgorithmParameters(AlgorithmParameters inParams) {
		
		parentAlgo = inParams.parentAlgo ;
		recalculationBlocked = inParams.recalculationBlocked ;
		
		for (int i=0;i<inParams.items.size();i++){
			
			AlgorithmParameter item = new AlgorithmParameter(inParams.items.get(i)) ;
			items.add(item) ;
		} // i->
		
		
	}

	
	// ------------------------------------------------------------------------


	

	public void clear() {
	
		items.clear();
	}
	
	
	public int size() {
		return items.size();
	}
	
	/**
	 * @return the items
	 */
	public ArrayList<AlgorithmParameter> getItems() {
		return items;
	}

	public AlgorithmParameter getItem(int index) {
		return items.get(index);
	}	 
	
	public String getLabel(String idSnip) {
		String resultStr="" ,istr ;
		String[] strparts ;
		int n=0;
		
		for (int i=0;i<items.size();i++){
			
			istr = items.get(i).label.trim() ;
			strparts = new String[]{istr} ;
			if (istr.contains(":")){
				strparts = istr.split(":") ;
				if (strparts[0].toLowerCase().contains(idSnip.toLowerCase())){
					resultStr = strparts[1] ;
					break ;
				}
			}
		}
		
		return resultStr;
	}


	public void add(AlgorithmParameter algoparam) {
		 
		items.add(algoparam) ;
	}


	public void setItems(ArrayList<AlgorithmParameter> items) {
		this.items = items;
	}


	public void setRecalculationBlocked(boolean flag) {
		recalculationBlocked = flag;
	}


	public boolean isRecalculationBlocked() {
		return recalculationBlocked;
	}


	






	
	
}
