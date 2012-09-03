package org.NooLab.somfluid.clapp;

import java.util.ArrayList;

import org.NooLab.somfluid.SomApplicationIntf;




public class SomAppModelCatalog {

	 
	ArrayList<ModelCatalogItem> items = new ArrayList<ModelCatalogItem> ();
	ArrayList<ModelCatalogItem> excludedItems = new ArrayList<ModelCatalogItem> ();; 
	
	// ========================================================================
	public SomAppModelCatalog(SomApplicationIntf somApp, SomAppProperties soappProperties) {
		
		
	}
	// ========================================================================

	public int size() {
		return items.size();
	}
 
	public ModelCatalogItem getItem(int index) {
		ModelCatalogItem item=null;
		
		if ((index>=0) && (index<items.size())){
			item = items.get(index);
		}
		return item;
	}

	
	
	public void add(ModelCatalogItem mcItem) {
		
		items.add(mcItem) ;
	}

	public int getItemIndexByIDs(String packageName, String modelName) {
		int ix,index = -1;
		ModelCatalogItem mcItem ;
		
		for (int i=0;i<items.size();i++){
			
			mcItem = items.get(i);
			
			boolean hb = mcItem.packageName.contentEquals(packageName);
			
			if ((hb) && (excludedItems.indexOf(mcItem)<0)){
				hb = mcItem.modelName.contentEquals(modelName);
			}
			
			if (hb){
				index=i;
				break;
			}
 		}
		
		return index;
	}

	public ModelCatalogItem getItemByModelname(String modelname) {
		ModelCatalogItem mci, mcItem=null;
		
		for (int i=0;i<items.size();i++){
			mci = items.get(i);
			if ((mci.modelName.contentEquals(modelname))  && (excludedItems.indexOf(mci)<0)){
				mcItem = mci;
				break;
			}
		}
		
		return mcItem;
	}
	
	public ModelCatalogItem getItemByModelname(String modelname, String version) {
		ModelCatalogItem mci, mcItem=null;
		
		for (int i=0;i<items.size();i++){
			mci = items.get(i);
			if ((mci.modelName.contentEquals(modelname)) && (excludedItems.indexOf(mci)<0)){
				if (mci.modelVersion.contentEquals(version)){
					mcItem = mci;
					break;
				}
			}
		}
		
		return mcItem;
	}

	public void clearExcludedItems() {
		excludedItems.clear();
	}
	
	public void addToExcludedItems(ModelCatalogItem item) {
		excludedItems.add( item );
		
	}

	public ArrayList<ModelCatalogItem> getExcludedItems() {
		return excludedItems;
	}

	public void setExcludedItems(ArrayList<ModelCatalogItem> excludedItems) {
		this.excludedItems = excludedItems;
	}	
	
}
