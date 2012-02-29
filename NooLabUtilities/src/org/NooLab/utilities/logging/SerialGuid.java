package org.NooLab.utilities.logging;

import org.NooLab.utilities.net.GUID;

public class SerialGuid {

	public SerialGuid(){
		
	}
	
	public static long numericalValue(){
		long sn=0;
		String guid, str="";
		
		guid = GUID.randomvalue() ;
		guid = guid.replace("-", "");
		for (int i=0;i<guid.length();i++){
			char ch = guid.charAt(i) ;
			int cc = (int)ch;
			str = str + "" + cc;
		}
		str = str.substring(0,18);
		sn = Long.parseLong(str);
		
		return sn;
	}
	
}
