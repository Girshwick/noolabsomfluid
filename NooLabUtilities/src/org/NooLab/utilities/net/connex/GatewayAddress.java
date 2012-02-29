package org.NooLab.utilities.net.connex;


import com.hoodcomputing.natpmp.*;
import java.net.Inet4Address;
 
import java.net.*;
 
import java.net.InetAddress;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.StringTokenizer;

public class GatewayAddress {

	public GatewayAddress(){
		
	}
	
	public static String get(){
		
		String ipAddressStr = "" ;
		
		
		return ipAddressStr;
	}
	
	// using miniuPnP
	
	//....
	
	// if NatPmP is supported by the router... consumes a lot of time...
	public static void NatPmPDevice() {
	        NatPmpDevice pmpDevice = null;

	        try {
	            // To find the device, simply construct the class. An exception is
	            // thrown if the device cannot be located or if the network is not
	            // RFC1918.
	            // When the device is constructed, you have to tell it whether you
	            // want it to automatically shutdown with the JVM or if you'll take
	            // the responsibility of shutting it down yourself. Refer to the
	            // constructor documentation for the details. In this case, we'll
	            // let it shut down with the JVM.
	            pmpDevice = new NatPmpDevice(true);

	            // The next step is always to determine the external address of
	            // the device. This is done by constructing the request message
	            // and enqueueing it.
	            ExternalAddressRequestMessage extAddr = new ExternalAddressRequestMessage(null);
	            pmpDevice.enqueueMessage(extAddr);

	            // In this example, we want to purposefully wait until the queue is
	            // empty. It is possible to receive notification when the operation
	            // is complete. Refer to the documentation for the
	            // ExternalAddressRequestMessage constructor.
	            pmpDevice.waitUntilQueueEmpty();

	            // We can try and get the external address to determine if the
	            // gateway is functional.
	            // This may throw an exception if there was an error receiving the
	            // response. The method getResponseException() would also return an
	            // exception object in this case, if you prefer avoiding using
	            // try/catch for logic.
	            Inet4Address extIP = extAddr.getExternalAddress();
	            
	            // Now, we can set up a port mapping. Refer to the javadoc for
	            // the parameter values. This message sets up a TCP redirect from
	            // a gateway-selected available external port to the local port
	            // 5000. The lifetime is 120 seconds. In implementation, you would
	            // want to consider having a longer lifetime and periodically sending
	            // a MapRequestMessage to prevent it from expiring.
	            MapRequestMessage map = new MapRequestMessage(true, 5000, 0, 120, null);
	            pmpDevice.enqueueMessage(map);
	            pmpDevice.waitUntilQueueEmpty();

	            // Let's find out what the external port is.
	            int extPort = map.getExternalPort();
	            
	            // All set!
	            
	            // Please refer to the javadoc if you run into trouble. As always,
	            // contact a developer on the SourceForge project or post in the
	            // forums if you have questions.
	        } catch (NatPmpException ex) {
	        	ex.printStackTrace();
	        }
	    }
}


class getNetworkSystemOutput{
	  
    public getNetworkSystemOutput()   throws SocketException {
    	
    }
     
 
	
}

class NetworkUtils {

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

/*

/*
 * This file is part of jNAT-PMPlib.
 *
 * jNAT-PMPlib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jNAT-PMPlib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jNAT-PMPlib.  If not, see <http://www.gnu.org/licenses/>.
 */



/**
 * This class provides an example that describes how this library is used.
 * @author flszen
 
    /**
     * This method demonstrates how to use the library to work with a NAT-PMP
     * gateway.
     
  
 

*/