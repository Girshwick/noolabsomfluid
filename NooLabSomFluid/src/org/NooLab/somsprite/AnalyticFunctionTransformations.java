package org.NooLab.somsprite;

import java.util.ArrayList;




public class AnalyticFunctionTransformations implements AnalyticFunctionTransformationsIntf{

	
	ArrayList<AnalyticFunctionSpriteImprovement> items ;

	
	
	// ========================================================================
	public AnalyticFunctionTransformations(){
		
	}
	// ========================================================================	



	/**
	 * @return the items
	 */
	public ArrayList<AnalyticFunctionSpriteImprovement> getItems() {
		return items;
	}



	/**
	 * @param items the items of type "AnalyticFunctionSpriteImprovement" to set
	 */
	public void setItems(ArrayList<AnalyticFunctionSpriteImprovement> items) {
		this.items = items;
	}
	
	
	
	
}
