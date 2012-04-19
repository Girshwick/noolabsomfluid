package org.NooLab.somtransform.algo.intf;

import java.util.ArrayList;

 


public class AlgorithmParameters {

	protected ArrayList<AlgorithmParameter> items = new ArrayList<AlgorithmParameter>();

	AlgorithmIntf parentAlgo ;
	
	
	// ------------------------------------------------------------------------
	public AlgorithmParameters( AlgorithmIntf parent ){
		parentAlgo = parent ;
		
	}
	// ------------------------------------------------------------------------


	/**
	 * @return the items
	 */
	public ArrayList<AlgorithmParameter> getItems() {
		return items;
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
	
	
}
