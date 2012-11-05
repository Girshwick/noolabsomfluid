package org.NooLab.utilities.inifile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import org.NooLab.utilities.strings.StringsUtil;




public class IniStyleSections implements Serializable{

	
	private Vector<IniStyleSection> sections = new Vector<IniStyleSection>();
	
	transient StringsUtil strgutil;
	
	// ------------------------------------------------------------------------
	public IniStyleSections(){
		
	}
	// ------------------------------------------------------------------------
	
	
	protected void setObj( StringsUtil strutil ){
		
		strgutil = strutil;
	}
	
	private void provideObj( IniStyleSection section){
		section.setObj( strgutil );
	}
	
	
	public int size(){
		return sections.size();
	}
	
	/**
	 * returns the matching section as object, matching is exact 
	 * @param name
	 * @return
	 */
	public IniStyleSection getByName(String name){
	
		IniStyleSection section = null ;
		
		for (int i=0;i<sections.size();i++){
			String sectLabel = sections.get(i).name ;
			if (sectLabel.toLowerCase().contentEquals(name)){
				section = sections.get(i) ;
				break;
			}
		}
		
		return section;
	}

	/**
	 * returns the matching section as object, matching is relaxed;
	 * simple wildcard matching with wildcard at the beginning or the end of the pattern
	 * 
	 * @param pattern
	 * @param relaxed,  1=mutual inclusion ; 2=regarding .-sections of the name separately 
	 * @return
	 */
	public ArrayList<IniStyleSection> getByName(String pattern, int relaxed){
		
		boolean hb;
		IniStyleSection section = null ;
		ArrayList<IniStyleSection> foundSections = new ArrayList<IniStyleSection>();
		
		for (int i=0;i<sections.size();i++){
			
			String sectLabel = sections.get(i).name.toLowerCase() ;
			
			hb = (sectLabel.contentEquals(pattern));
			if (hb==false){
				hb = (sectLabel.contains(pattern)) || (pattern.contains(sectLabel));
			}
			if (hb){
				section = sections.get(i) ;
				foundSections.add(section);
			}
		}
		
		return foundSections;
	}

		
		
	public IniStyleSection get(int index){
		return sections.get(index) ;
	}

	public Vector<IniStyleSection> getSections() {
		return sections;
	}

	public Integer getEntryValue(String sectionName, String keyName, int defaultValue) {
		// 
		int entryvalue = defaultValue;
		

		IniStyleSection section = getByName( sectionName );
		
		String phStr = section.getEntry( keyName );
		int ph = section.getInt(phStr);
		
		if ((ph>=0) ){
			entryvalue = ph;
		}
		
		return entryvalue;
	}

	public String getEntryValue(String sectionName, String keyName, String defaultStr) {
		String entryvalue = defaultStr;
		
		IniStyleSection section = getByName( sectionName );
		try{
			
			entryvalue = section.getEntry( keyName );
			
		}catch(Exception e){
			// educated silence...
		}
		
		return entryvalue;
	}

	public float getEntryValue(String sectionName, String keyName, float defaultValue) {
		float entryvalue = defaultValue;
		

		IniStyleSection section = getByName( sectionName );
		
		String phStr = section.getEntry( keyName );
		float ph = section.getFloat(phStr);
		
		if ((ph>=0) ){
			entryvalue = ph;
		}
		
		return entryvalue;
	}


	public void setSections(Vector<IniStyleSection> sections) {
		this.sections = sections;
	}
	
	
}




