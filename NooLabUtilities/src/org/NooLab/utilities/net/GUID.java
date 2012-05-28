package org.NooLab.utilities.net;



import java.util.UUID;

import org.NooLab.utilities.net.connex.NicAddresses;
import org.NooLab.utilities.strings.StringsUtil;






public class GUID {

	/**
	 * 
	 * creates a string like "7dc53df5-703e-49b3-8670-b1c468f47f1f"
	 * @return
	 */
	static public String randomvalue() {
		UUID uuid ; 
		String randomUUIDString ;
		
        uuid = UUID.randomUUID();
        randomUUIDString = uuid.toString();

        /*
        System.out.println("Random UUID String = " + randomUUIDString);
        System.out.println("UUID version       = " + uuid.version());
        System.out.println("UUID variant       = " + uuid.variant());
        */
        
		return randomUUIDString;
    }

	static public String uniquevalue(){
		com.eaio.uuid.UUID uuid ;
		String uuidStr ="" ;
		uuid = new com.eaio.uuid.UUID();
		// System.out.println(u);

		uuidStr = uuid.toString() ;
		
		return uuidStr ;
	}
	
	static public String value( int maxlen , boolean unique) {
		String uuidStr ;
		int nativeLen, d;
		
		uuidStr = randomvalue();
		nativeLen = uuidStr.length() ;
		
		if (nativeLen>maxlen){
			
			
			
		} // = too long ?
		
		return uuidStr;
	}

	/**
	 * 
	 * returns a random Guid, which is extended by the Mac - address of the first found NIC
	 * 
	 * @return
	 */
	static public String extendedGuid(){
		
		String str = randomvalue();
		
		String macnicStr = NicAddresses.getMac();
		if (macnicStr.length()>0){
			macnicStr = StringsUtil.replaceall(macnicStr, ":", "");
			macnicStr = StringsUtil.replaceall(macnicStr, "-", "");
			macnicStr = "-"+macnicStr;
		}
		str = str+macnicStr ;
		
		return str;
	}
}


 