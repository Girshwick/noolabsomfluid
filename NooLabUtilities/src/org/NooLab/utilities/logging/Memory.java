package org.NooLab.utilities.logging;

import java.util.ArrayList;

public class Memory {

	private static final int MegaBytes = 10241024;
	private static final int KiloBytes = 1024;

	static long freeMemory = -1L; 
	
	static ArrayList<Long> freeMemoryObservations = new ArrayList<Long>();
	static int maxObservationPoints = 100 ; 
	
	
	public static long observe(){
		currentFreeMemory(1);
		return getDifference(1) ;
	}
	
	public static long getDifference(){
		return getDifference(1) ;
	}

	public static long getDifference(int measurePointsBefore){
		int p;
		
		if (freeMemoryObservations.size()<=2){
			return -1;
		}
		p=measurePointsBefore;
		if (p>freeMemoryObservations.size()-1){
			p=freeMemoryObservations.size()-1;
		}
		long v1,v2, dv;
		
		v2= freeMemoryObservations.get(freeMemoryObservations.size()-1);
		v1= freeMemoryObservations.get(freeMemoryObservations.size()-p-1);
		
		dv = v2-v1;
		return dv ;
	}
	
	public static long currentFreeMemory(int format){
		if (format==0){
			freeMemory = Runtime.getRuntime().freeMemory() ;
		}
		if (format==1){
			freeMemory = Runtime.getRuntime().freeMemory() / KiloBytes;
		}
		if (format>=2){
			freeMemory = Runtime.getRuntime().freeMemory() / MegaBytes;
		}	
		
		freeMemoryObservations.add(freeMemory) ;
		
		if (freeMemoryObservations.size()>maxObservationPoints){
			freeMemoryObservations.remove(0) ;
		}
		return freeMemory;
	}
	
	public static void main(String args[]) {

		long freeMemory = Runtime.getRuntime().freeMemory() / KiloBytes;
		long totalMemory = Runtime.getRuntime().totalMemory() / KiloBytes;
		long maxMemory = Runtime.getRuntime().maxMemory() / KiloBytes;

		System.out.println("\nUsed Memory in JVM : " + (maxMemory - freeMemory));
		System.out.println("JVM freeMemory     : " + freeMemory);
		System.out.println("JVM totalMemory also equals to initial heap size of JVM : " + totalMemory);
		System.out.println("JVM maxMemory also equals to maximum heap size of JVM   : "+ maxMemory);

		System.out.println("\ncreating some objects ...\n");
		ArrayList objects = new ArrayList();

		for (int i = 0; i < 10000000; i++) {
			objects.add(("abc " + 10 * 2710));
		}

		freeMemory = Runtime.getRuntime().freeMemory() / KiloBytes;
		totalMemory = Runtime.getRuntime().totalMemory() / KiloBytes;
		maxMemory = Runtime.getRuntime().maxMemory() / KiloBytes;

		System.out.println("Used Memory in JVM : " + (maxMemory - freeMemory));
		System.out.println("freeMemory  in JVM : " + freeMemory);
		System.out.println("totalMemory in JVM shows current size of java heap : " + totalMemory);
		System.out.println("maxMemory   in JVM : " + maxMemory);

	}
}

// Read more:
// http://javarevisited.blogspot.com/2012/01/find-max-free-total-memory-in-java.html#ixzz1qXaOSK5w

