package org.NooLab.utilities.net.connex;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *  
 *  
 *
 */
public class CheckIp {
	
	public static boolean DEBUG = false;
	public static int CHECKPORT = 80;

	 
	static public int isReachable(String host){
		return isReachable(host,CHECKPORT );
	}
	static public int isReachable(String host, int port){
		
		long start = System.currentTimeMillis();
	    
		try {
		
			Socket socket = new Socket(host, port);
			socket.close();
			
	    } catch (ConnectException e){
	    	
	    	String ex = e.toString();
	    	
	    	if (ex.contains("Connection refused")){
	    		
	    		long end = System.currentTimeMillis()-start;
	    		if (DEBUG)System.out.println("online, indirekt ermittelt");
	    		return (int)end;
	    		
	    	} else {
	    		
	    		if (DEBUG) System.out.println("offline");
	    		return -1;
	    		
	    	}
	    	
	    } catch (UnknownHostException e) {
			
	    	if (DEBUG) System.out.println("offline");
    		return -1;
			
		} catch (IOException e) {
			
			if (DEBUG) System.out.println("offline");
    		return -1;
    		
		}  catch ( Exception e) {
			e.printStackTrace();
		}
		
		long end = System.currentTimeMillis()-start;
		if (DEBUG)System.out.println("online");
		return (int)end;
	}
	
	public static void main(String[] args) {
		System.out.println(new CheckIp().isReachable("127.0.0.1"));
		System.out.println(new CheckIp().isReachable("www.google.de"));
		System.out.println(new CheckIp().isReachable("www.dieadressegibtsgarnicht.de"));
	}
	
}
