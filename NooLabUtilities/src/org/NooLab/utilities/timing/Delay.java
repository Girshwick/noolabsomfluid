package org.NooLab.utilities.timing;


public class Delay{

	private Delay(){
		
	}
	
	@SuppressWarnings("static-access")
	public void simple( long millis){
		try{
			Thread.currentThread().sleep(millis) ;
		}catch(Exception e){}
	}
	
 
}
