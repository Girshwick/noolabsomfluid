package org.NooLab.utilities.net.connex;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Vector;



public class FindFreePort {

	Vector<Integer> reservedPorts = new Vector<Integer> (); 
	int MIN_PORT_NUMBER = 1030 ;
	int MAX_PORT_NUMBER = 65000 ;
	
	public FindFreePort(){
		
	}
	
	public boolean isPortFree( int checkport ){
		boolean rb=false ;
		ServerSocket server ;
		int port = -1 ;
		
		try {
			
			server = new ServerSocket( checkport );

			
			port = server.getLocalPort();
			server.close();

			rb = true;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return rb;
	}
	
	
	public int anyFreePort(){
		ServerSocket server ;
		int port = -1 ;
		
		try {
			
			server = new ServerSocket(0);

			port = server.getLocalPort();
			server.close();

			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return port;
	}
	
	public int getFreePort( int low, int hi) throws IOException{
		
		ServerSocket serverSo = null;
		int port = -1;
		boolean found = false;
		
if (low>49000){
	port=port+0;
}		
		MIN_PORT_NUMBER = low ;
		MAX_PORT_NUMBER = hi ;
		
		try {

			while (found == false) {

				for (int i = low; i < hi; i++) {
					 
					if ((available(i) ) && (reservedPorts.indexOf(i)<0)){
						found = true;
						port = i;
						break ;
					}
					
				} // i-> all ports of interval
			}
			
		} catch (Exception iex) {
			found=false;
		}

		if (found == false) {
			throw new IOException(String.format(
					"Unable to open server in port range(%d-%d)", low, hi));
		}else{
			reservedPorts.add(port) ;
			serverSo=null;
		}

		// At this point if s is null we are helpless

		return port;
	}
	
	private boolean available(int port) {
	    if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
	        throw new IllegalArgumentException("Invalid start port: " + port);
	    }

	    ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	        
	        return true;
	        
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	        	try{
	        		ds.close();
	        	}catch(Exception e){
	        	}
	        }

	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }

	    return false;
	}

	public void releaseFromReservation( int portnumber ){
		int p;
		
		p = reservedPorts.indexOf( portnumber );
		
		if (p>=0){
			reservedPorts.remove(p) ;
		}
		
	}

	public Vector<Integer> getReservedPorts() {
		return reservedPorts;
	}

	public void setReservedPorts(Vector<Integer> reservedPorts) {
		this.reservedPorts = reservedPorts;
	}
	
}
