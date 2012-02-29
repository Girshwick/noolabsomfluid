package org.NooLab.utilities.inifile;

import java.util.Vector;

import org.NooLab.utilities.strings.StringsUtil;
  

public class IniStyleContent {

	IniStyleSections iniSections = new IniStyleSections() ;
	
	Vector<String> lines = new Vector<String>();

	StringsUtil strgutil = new StringsUtil();
	static org.apache.commons.lang3.StringUtils ApacheStrings;	
	
	
	
	public IniStyleContent(String text) {

		  
		splitStr2List();
		
		 

	}

	public IniStyleContent(Vector<String> text) {
 
		lines.addAll(text);
		 
		provideObj( );
		
		parseforSections();

		iniSections = getIniStyleSections();

	}

	private void provideObj( ){
		iniSections.setObj( strgutil );
	}
	
	
	public IniStyleSections getIniStyleSections() {
		return iniSections;
	}

	private void splitStr2List() {
		Vector<String>  textlines = new Vector<String>();
		
		lines.addAll(textlines);
		 
	}

	
	@SuppressWarnings("static-access")
	private void parseforSections() {
		
		IniStyleSection section = null;
		 
		
		boolean sectionActive=false;
		String str,name;
		int  i, n;
		
		
		n = lines.size();

		for (i = 0; i < n; i++) {
			
			str = lines.get(i).trim();
			if ((str.indexOf("[")==0) && (str.indexOf("]")>0)){
				sectionActive = true ;
				section = new IniStyleSection();
				name = ApacheStrings.substringBetween(str, "[", "]").trim();
				section.setName(name);
				section.setObj(strgutil) ;
				iniSections.getSections().add(section);
				 
			} else{
				if (sectionActive==true){
					if ( ( (str.indexOf("[/")==0) && (str.indexOf("]")>0)) || 
					     ( (str.indexOf("[")==0) && (str.indexOf("/]")>0) )){
						sectionActive = false;
						
					} else{
						
					}
					
				}
				if ((sectionActive) && (section!=null)){
					
					section.addItem(str) ;
					
				}
			}
			
			
		} // i -> all lines
		i=0;
	}

}




