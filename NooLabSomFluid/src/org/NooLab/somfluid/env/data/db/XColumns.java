package org.NooLab.somfluid.env.data.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
 



public class XColumns {

	ArrayList<XColumn> items = new ArrayList<XColumn>(); 
	
	
	public XColumns(){
		
	}


	public ArrayList<XColumn> getItems() {
		return items;
	}



	/*
	 *  <item id="1" name="id" quality="id" sema="id" /> 
        <item id="2" name="contextid" quality="id" sema="context" /> 
        <item id="3" name="docid" quality="id" sema="document" />
        <item id="4" name="wordlabel" quality="label" sema="word" />
	 */
	
	/**
	 * NOT READY TO USE !!!
	 * 
	 * @param table        the name of the table, if =="", then for all
	 * @param scope        0=all; 1=core; 2=extension
	 * @param criterion    0=name; 1=quality; 2=sema
	 * @param ccondition   the string value of the criterion, such as "id", "document", "word" 
	 * @return
	 */
	public ArrayList<String> getLabels( String table, int scope, int criterion, String ccondition  ) {
		ArrayList<String> flabels = new ArrayList<String>();

	
		return flabels;
	}
	 /**
	  * 
	  * @param table  the name of the table, if =="", then for all 
	  * @param scope  0=all; 1=core; 2=extension
	  * @return
	  */
	public ArrayList<String> getLabels( String table, int scope  ) {
		
		ArrayList<String> flabels = new ArrayList<String>();
		XColumn item;
		boolean hb;
		
		for (int i=0;i<items.size();i++){
			item = items.get(i);
			
			if (item.table.contentEquals(table)){
				hb = true;
				if (scope>0){
					if (scope==1){
						if (item.domain.contentEquals("core")==false){
							hb=false;
						}
					}
					if (scope==2){
						if (item.domain.contentEquals("extension")==false){
							hb=false;
						}
					}
				}
				if (hb){
					String str = item.name;
					flabels.add(str) ;
				}
			}
		}// ->
		
		return flabels;
	}
	
	public int size(){
		return items.size();
	}
 

	@SuppressWarnings("unchecked")
	public void sort(int direction){
		
		if (direction < -0.5){
			direction=-1;
		}else{
			if (direction>= 0.5){
				direction= 1;
			}else{
				if ((direction>-0.5) && (direction<0.5))direction = 0;
			}
		}
		
		Collections.sort( items, new xcComparator(direction));
	}

	class xcComparator implements Comparator{

		int criterion;
		int direction=0;
		
		/** 1 = smallest first, -1 = largest first*/
		public xcComparator(int dir){
			direction = dir;
			criterion = 1;
		}
		/** criterion : 1=distance, 2=index, 3=secondary index, 4=string */
		public xcComparator(int criterion, int dir){
			direction = dir;
			this.criterion = criterion;
		}
		
		@Override
		public int compare(Object obj1, Object obj2) {
			
			int result = 0;
			XColumn ixd2,ixd1;
			double v1=0,v2=0 ;

			ixd1 = (XColumn)obj1;
			ixd2 = (XColumn)obj2;

			if (criterion<=1){
				v1 = ixd1.pos ;
				v2 = ixd2.pos ;
			}

			if (criterion==2){
				v1 = ixd1.index ;
				v2 = ixd2.index ;
			}
			 
			
			
			if (direction>=0){
				if (v1>v2){
					result = 1;
				}else{
					if (v1<v2){
						result = -1 ;
					}
				}
			}else{
				if (v1>v2){
					result = -1;
				}else{
					if (v1<v2){
						result = 1 ;
					}
				}
				
			}
			
			return result;
		}
		
	}
}
