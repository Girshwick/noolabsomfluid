package org.NooLab.somfluid.app;

import java.util.ArrayList;

import org.NooLab.somfluid.SomApplicationIntf;




public class SomAppModelCatalog {

	
	ArrayList<ModelCatalogItem> items = new ArrayList<ModelCatalogItem> (); 
	
	// ========================================================================
	public SomAppModelCatalog(SomApplicationIntf somApp, SomAppProperties soappProperties) {
		
		
	}
	// ========================================================================

	public void add(ModelCatalogItem mcItem) {
		
		items.add(mcItem) ;
	}

	public int getItemIndexByIDs(String packageName, String modelName) {
		int ix,index = -1;
		ModelCatalogItem mcItem ;
		
		for (int i=0;i<items.size();i++){
			
			mcItem = items.get(i);
			
			boolean hb = mcItem.packageName.contentEquals(packageName);
			
			if (hb){
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
			if (mci.modelName.contentEquals(modelname)){
				mcItem = mci;
				break;
			}
		}
		
		return mcItem;
	}
	
	
	
}
