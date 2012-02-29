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
	
	public IniStyleSection get(int index){
		return sections.get(index) ;
	}

	public Vector<IniStyleSection> getSections() {
		return sections;
	}
	
	
}




