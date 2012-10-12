package org.NooLab.utilities.inifile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.NooLab.utilities.strings.StringsUtil;

public class IniStyleSection {

	/** the whole line */
	ArrayList<String> sectionitems = new ArrayList<String>();
	
	/** all the keys*/
	ArrayList<String> sectionKeys = new ArrayList<String>();
	
	Map<String,ItemValue> mappedItems = new HashMap<String,ItemValue>();
	
	String name="" ;
	
	int parseMode = 0; // 0=lazy, 1=strict
	
	
	StringsUtil strgutil;
	
	public IniStyleSection(){
		
	}
	
	protected void setObj( StringsUtil strutil ){
		
		strgutil = strutil;
	}
	
	public void setName(String str){
		name = str;
	}
	
	public String getName(){
		return name;
	}
	
	public void addItem(String str){
		int p= -1;
		String iL = "",iV;
		ItemValue itemvalue;
		
		
		
		p = str.indexOf("=");
		 
		if ( p>0 ){ // strict
			iL = str.substring(0,p);
			iV = str.substring(p+1, str.length()).trim() ;
			iV = strgutil.trimm(iV, "=");
			iV = strgutil.cleanInternal(iV);
			itemvalue = new ItemValue(iV);
			mappedItems.put(iL, itemvalue) ;
			 
			
		}
		{
			if (( p==0) || ( parseMode==0 )){ // lazy
				str = strgutil.replaceAll(str, " =", "=");
				str = strgutil.replaceAll(str, "= ", "=");
				sectionitems.add( str ) ;
				
				if (iL.length()>0){
					sectionKeys.add(iL) ;
				}
			}
		}

		
	}

	
	
	public String getEntry( String taglabel ){
		String itemvalue="" , itemtxt;
		 
		
		int p;
		
		for (int i=0;i<sectionitems.size();i++){
			itemtxt = sectionitems.get(i) ;
			
			if (mappedItems.size()==0){
				p = itemtxt.indexOf("=");
				if ((p > 0) || (parseMode == 1)) { // strict

				} else {
					if ((p == 0) || (parseMode == 0)) { // lazy
						itemvalue = itemtxt ;
					}
				}
			} else{
				if (mappedItems.containsKey(taglabel)){
					itemvalue = mappedItems.get(taglabel).getValueStr();
				}
			}
		}
		
		return itemvalue;
	}
	
	public ArrayList<String> getSimpleEntries(){
		ArrayList<String> simple_entries = new ArrayList<String>() ;
		String str;
		
		for (int i=0;i<sectionitems.size();i++){

			str = sectionitems.get(i) ;
			
			if (str.indexOf("=")<0){
				simple_entries.add( str );
			}
		}
		
		return simple_entries;
	}

	public ArrayList<String> getEntries(){
		return sectionitems;
	}

	public Map<String,ItemValue> getAllMappedEntries(){
		return  mappedItems;
	}
	
	public Map<String,ItemValue> getMappedEntries(String pattern){
		// TODO not ready !!
		return  mappedItems;
	}
	
	
	public int size(){
		return sectionitems.size();
	}
	
	public String get(int index){
		return sectionitems.get(index) ;
	}
	
	
	public int getParseMode() {
		return parseMode;
	}

	public void setParseMode(int parseMode) {
		this.parseMode = parseMode;
	}

	public void setStrgutil(StringsUtil strgutil) {
		this.strgutil = strgutil;
	}
}

	
