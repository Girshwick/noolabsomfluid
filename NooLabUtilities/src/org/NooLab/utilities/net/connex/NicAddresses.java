package org.NooLab.utilities.net.connex;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.util.*;



public class NicAddresses {

	public NicAddresses(){
		
	}
	
	private static Vector<String> getNicRecords(){
		// returns bags/records containing name, ip-addresses, and hardware mac
		
		
		return null ;
	}
	
	
	public static Vector<String> getMacs(){
		Vector<String> macIds = new Vector<String>();
	
		Vector<String> ipAddresses ;
		List<NetworkInterface> niclist ;
		NetworkInterface ifc;
		String str, ps;
		boolean hb;
		byte[] nicmac;
		int vb;
		
		
		try{
			
			niclist = Collections.list(NetworkInterface.getNetworkInterfaces() );
			
			for (int i = 0; i < niclist.size(); i++) {

				ifc = niclist.get(i);

				if (ifc.isUp()) {

					nicmac = ifc.getHardwareAddress();
					// 192.168.0.10 -> E0-F8-47-02-45-16
					// nicmac:        [-32, -8, 71, 2, 69, 22]
				 
					str="";
					
					
					for (int s=0;s<nicmac.length;s++){
						vb = nicmac[s];
						if (vb<0){ vb = 16*16+vb; };
						
						ps = Integer.toHexString( vb );
						if (vb<10){ps="0"+ps;}
						str = str + ps;
						
						// alternatively : ps = Integer.toString( ( nicmac[s] & 0xff ) + 0x100, 16 /* radix */ ).substring( 1 );
						
						if (s<nicmac.length-1){
							str = str+"-";
						}
					} // s->
					
					// !!! ifc.getByInetAddress(addr)
					if (str.length()>0){
						macIds.add(str);
						String name = ifc.getDisplayName() ;
						ipAddresses = getIPaddressesByIFC(ifc);
						int n = ipAddresses.size();
						// we could associate it...
					}
				} // isUp ?
			} // i->
		}catch(Exception e){
			
		}
		
		
		
		return macIds;
	} // 
	
	private static Vector<String> getIPaddressesByIFC( NetworkInterface ifc){
		
		Vector<String> ipAddresses = new Vector<String>(); 
		
		List<InetAddress> addrlist ; 
		InetAddress addr;
		String str;
		boolean hb;
		IpAddressValidator ipValid = new IpAddressValidator();
		
		try{
			
			
			if (ifc.isUp()) {

				
				addrlist =  Collections.list( ifc.getInetAddresses() );
				
				for( int k=0;k<addrlist.size();k++ ){
					addr = addrlist.get(k) ;
					
					// addrList.add(addr);
					str = addr.getHostAddress() ;
					hb = addr.isReachable(50);
					// name:lo (Software Loopback Interface 1) [/0:0:0:0:0:0:0:1, /127.0.0.1]
					if (hb){
						hb = addr.isSiteLocalAddress();
					}
					// if (hb)
					{
						hb = (ipValid.validate(str)) && (str.contentEquals("127.0.0.1")==false);
					}
					if (hb){
						ipAddresses.add(str);
					}
					// name:net4 (Broadcom 802.11n Network Adapter)  [/fe80:0:0:0:393b:5a86:7919:7b68%11, /192.168.0.10]
				}
			}
			
		}catch(Exception e){
			
		}
		
		
		return ipAddresses;
	}
	public static Vector<String> get(){

		Vector<String> ipAddresses = new Vector<String>(); 
		
		List<NetworkInterface> niclist ;
		List<InetAddress> addrlist ; 
		NetworkInterface ifc;
		InetAddress addr;
		String str;
		boolean hb;
		IpAddressValidator ipValid = new IpAddressValidator();
		
		try{
			
			niclist = Collections.list(NetworkInterface.getNetworkInterfaces() );
			
			for (int i = 0; i < niclist.size(); i++) {

				ifc = niclist.get(i);

				if (ifc.isUp()) {

					
					addrlist =  Collections.list( ifc.getInetAddresses() );
					
					for( int k=0;k<addrlist.size();k++ ){
						addr = addrlist.get(k) ;
						
						// addrList.add(addr);
						str = addr.getHostAddress() ;
						hb = addr.isReachable(50);
						// name:lo (Software Loopback Interface 1) [/0:0:0:0:0:0:0:1, /127.0.0.1]
						if (hb){
							hb = addr.isSiteLocalAddress();
						}
						// if (hb)
						{
							hb = (ipValid.validate(str)) && (str.contentEquals("127.0.0.1")==false);
						}
						if (hb){
							ipAddresses.add(str);
						}
						// name:net4 (Broadcom 802.11n Network Adapter)  [/fe80:0:0:0:393b:5a86:7919:7b68%11, /192.168.0.10]
					}
				}
			}
		}catch(Exception e){
			
		}
		
		return ipAddresses;
	}
}



class _NetworkUtils {

	  private final static int MACADDR_LENGTH = 17;
	  private final static String WIN_OSNAME = "Windows";
	  private final static String WIN_MACADDR_REG_EXP =
	   "^[0-9A-F]{2}-[0-9A-F]{2}-[0-9A-F]{2}-[0-9A-F]{2}-[0-9A-F]{2}-[0-9A-F]{2}$";
	  private final static String WIN_MACADDR_EXEC = "ipconfig /all";

	  
	  public final static String getGateWayAddress(){
		  return "";
	  }

	  public final static String getMacAddress() throws IOException {
		  
	    String os = System.getProperty("os.name");
	    try {
	      if (os.startsWith(WIN_OSNAME)) {
	         return winMacAddress(winIpConfigCommand());
	      }
	      // other OS left as an exercise !
	      //   LINUX -->    else if (os.startsWith("Linux")) { ...
	      //                ...
	      //                Process p = Runtime.getRuntime().exec("ifconfig");
	      //   MAC OSX -->  else if(os.startsWith("Mac OS X")) { ...
	      //                ...
	      //                Process p = Runtime.getRuntime().exec("ifconfig");
	      else {
	         throw new IOException("OS not supported : " + os);
	      }
	    }
	    catch(ParseException e) {
	      e.printStackTrace();
	      throw new IOException(e.getMessage());
	    }
	  }

	  private final static String winMacAddress(String ipConfigOutput) throws ParseException {
		  
	    String localHost = null;
	    try {
	      localHost = InetAddress.getLocalHost().getHostAddress();
	    }
	    catch(java.net.UnknownHostException ex) {
	      ex.printStackTrace();
	      throw new ParseException(ex.getMessage(), 0);
	    }

	    StringTokenizer tokenizer = new StringTokenizer(ipConfigOutput, "\n");
	    String lastMacAddress = null;

	    while(tokenizer.hasMoreTokens()) {
	      String line = tokenizer.nextToken().trim();

	      // see if line contains IP address
	      if (line.endsWith(localHost) && lastMacAddress != null) {
	         return lastMacAddress;
	      }

	      // see if line contains MAC address
	      int macAddressPosition = line.indexOf(":");
	      if(macAddressPosition <= 0){
	    	  continue;
	      }

	      String macAddressCandidate = line.substring(macAddressPosition + 1).trim();
	      if (winIsMacAddress(macAddressCandidate)) {
	         lastMacAddress = macAddressCandidate;
	         continue;
	      }
	    }

	    ParseException ex = new ParseException("cannot read MAC address from [" + ipConfigOutput + "]", 0);
	    ex.printStackTrace();
	    throw ex;
	  }


	  private final static boolean winIsMacAddress(String macAddressCandidate) {
		  
	    if (macAddressCandidate.length() != MACADDR_LENGTH)    return false;
	    if (!macAddressCandidate.matches(WIN_MACADDR_REG_EXP)) return false;
	    return true;
	  }


	  private final static String winIpConfigCommand() throws IOException {
		  
	    Process p = Runtime.getRuntime().exec(WIN_MACADDR_EXEC);
	    InputStream stdoutStream = new BufferedInputStream(p.getInputStream());

	    StringBuffer buffer= new StringBuffer();
	    for (;;) {
	       int c = stdoutStream.read();
	       if (c == -1) break;
	          buffer.append((char)c);
	    }
	    String outputText = buffer.toString();
	    stdoutStream.close();
	    return outputText;
	  }


	  public void get() {
	    try {
	      System.out.println("MAC ADDRESS");
	      System.out.println("  OS          : " + System.getProperty("os.name"));
	      System.out.println("  IP/Localhost: " + InetAddress.getLocalHost().getHostAddress());
	      System.out.println("  MAC Address : " + getMacAddress());
	    }
	    catch(Throwable t) {
	      t.printStackTrace();
	    }
	  }
}
