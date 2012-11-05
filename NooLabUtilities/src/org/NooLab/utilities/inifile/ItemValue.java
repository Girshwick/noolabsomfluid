package org.NooLab.utilities.inifile;

import java.io.Serializable;

import org.NooLab.utilities.strings.StringsUtil;

public class ItemValue  implements Serializable{

	int    missingIntVal = -1 ;
	double missingNumVal = -1.0 ;
	String missingStrVal = "" ;
	
	Object obj = null;
	
	public ItemValue( String str){

		obj = str;
		
		determineFormat();
		createRepresentatations();
	}

	public ItemValue( Integer value){

		obj = value;
		
		determineFormat();
		createRepresentatations();
	}
	
	
	public void determineFormat(){
		String cn ;
		
		if (obj!=null){
			cn = obj.getClass().getSimpleName().toLowerCase();
		}
		
		
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
			String cn = obj.getClass().getSimpleName().toLowerCase();
			
			if (cn.startsWith("int")==false){
				if (cn.startsWith("str")){
					String str = (String) obj;
					if ((str!=null) && (str.length()>0) && (StringsUtil.isNumeric(str))){
						ival = Integer.parseInt(str);
					}else{
						ival = missingIntVal;
					}
				}
			}else{
				ival =(int)(Integer)obj;
			}
		}else{
			ival = missingIntVal;
		}
		return ival;
	}

	public int getMissingIntVal() {
		return missingIntVal;
	}

	public void setMissingIntVal(int missingIntVal) {
		this.missingIntVal = missingIntVal;
	}

	public double getMissingNumVal() {
		return missingNumVal;
	}

	public void setMissingNumVal(double missingNumVal) {
		this.missingNumVal = missingNumVal;
	}

	public String getMissingStrVal() {
		return missingStrVal;
	}

	public void setMissingStrVal(String missingStrVal) {
		this.missingStrVal = missingStrVal;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	
}
