package org.NooLab.utilities.inifile;

import java.util.ArrayList;
import java.util.Vector;

import org.NooLab.utilities.strings.StringsUtil;
  

public class IniStyleContent {

	String filetext ;
	
	IniStyleSections iniSections = new IniStyleSections() ;
	
	ArrayList<String> lines = new ArrayList<String>();

	StringsUtil strgutil = new StringsUtil();
	static org.apache.commons.lang3.StringUtils ApacheStrings;	
	
	IniStyleContent isc;
	
	// --------------------------------------------------------------
	public IniStyleContent(String filecontent) {
		 
		filetext = filecontent;
		
	}
	public IniStyleContent() {
		
	}
	
	public IniStyleContent(Vector<String> text) {
 
		lines.addAll(text);
		 
		provideObj( );
		
		parseforSections();

		iniSections = getIniStyleSections();

	}
	// --------------------------------------------------------------

	
	public void getUp(){
		
		splitStr2List();
		
		parseforSections();
	}
	
	public IniStyleContent fromIniText(String text) {
		 
		isc = new IniStyleContent(text); 
		isc.getUp();
		
		return isc;
	}

	public  String section(String sectionName, String key) throws Exception{
		String entryValue = "" ;
		
		entryValue = iniSections.getByName(sectionName).getEntry(key) ;
		
		return entryValue;
	}

	private void provideObj( ){
		iniSections.setObj( strgutil );
	}
	
	
	public IniStyleSections getIniStyleSections() {
		return iniSections;
	}

	private void splitStr2List() {
		ArrayList<String>  textlines = new ArrayList<String>();
		
		filetext = strgutil.replaceAll(filetext, "\n\r", "\n");
		filetext = strgutil.replaceAll(filetext, "\r\n", "\n");
		
		String[] _lines = filetext.split("\n");
		
		for (int i=0;i<_lines.length;i++){
			String str = _lines[i].trim();
			if ((str.length()==0) || (str.indexOf(";")==0) || (str.indexOf("#")==0)){
				
			}else{
				textlines.add(str) ;
			}
		}
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




