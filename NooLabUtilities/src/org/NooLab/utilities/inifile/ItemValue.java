package org.NooLab.utilities.inifile;

public class ItemValue {

	int    missingIntVal = -1 ;
	double missingNumVal = -1.0 ;
	String missingStrVal = "" ;
	
	Object obj = null;
	
	public ItemValue( String str){

		determineFormat();
		createRepresentatations();
		
		obj = str;
	}
	
	public void determineFormat(){
		
	}
	
	public void createRepresentatations(){
		
	}
	
	public Object getValue(){
		return obj;
	}
	
	public String getValueStr(){
		String str="";
		Object obj;
		
		obj = getValue();
		if (obj!=null){
			str =(String)obj;
		}
		return str;
	}
	
	public String getValueNum(){
		String str="";
		Object obj;
		
		obj = getValue();
		if (obj!=null){
			str =(String)obj;
		}
		return str;
	}
	
	public int getValueInt(){
		int ival=0;
		Object obj;
		
		obj = getValue();
		if (obj!=null){
			ival =(int)(Integer)obj;
		}
		return ival;
	}

	
}
