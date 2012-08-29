package org.NooLab.utilities.inifile;

import java.util.Vector;

import org.NooLab.utilities.strings.StringsUtil;




public class IniStyleSections {

	
	private Vector<IniStyleSection> sections = new Vector<IniStyleSection>();
	
	StringsUtil strgutil;
	
	
	public IniStyleSections(){
		
	}
	
	protected void setObj( StringsUtil strutil ){
		
		strgutil = strutil;
	}
	
	private void provideObj( IniStyleSection section){
		section.setObj( strgutil );
	}
	
	
	public int size(){
		return sections.size();
	}
	
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
	
	public IniStyleSection get(int index){
		return sections.get(index) ;
	}

	public Vector<IniStyleSection> getSections() {
		return sections;
	}
	
	
}




