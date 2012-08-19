package tester;

import org.NooLab.field.repulsive.RepulsionField;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.utilities.logging.PrintLog;

public class RequestTester implements Runnable{

	RepulsionFieldIntf rField;
	int rqDelay = 1000;

	boolean isRunning =false;
	
	Thread reqtThrd;
	
	
	public RequestTester( RepulsionFieldIntf rf, int rqdelay ){
		
		rField = rf;
		rqDelay = rqdelay;
		
		reqtThrd = new Thread(this,"reqtThrd") ; 
		reqtThrd.start();
	}

	
	public void stop(){
		isRunning=false;
	}
	
	@Override
	public void run() {
		boolean isWorking=false;
		int n,ix;
		String requestGuid;
		
		isRunning = true;
		
		try{
			
			while (isRunning){
				
				if ((isWorking==false) && (rField.isReadyToUse())){
					isWorking=true;
					
					n = rField.getParticles().size()-1;
					ix = (int)(Math.random() * (double)(n*1.0))+1 ;
					
					try{
					
						requestGuid = rField.getSurround( ix,  1, true);
						
					}catch(Exception e){}
					
					isWorking=false;
				}
				
				PrintLog.Delay(rqDelay);
			} // ->
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void increaseFrequency(){
		if (rqDelay<40){
			rqDelay = rqDelay-2;
		}else{
			rqDelay = (int)(rqDelay*0.95);
		}
		if (rqDelay<=5){
			rqDelay=2;
		}
	}
	public void decreaseFrequency(){
		if (rqDelay<50){
			rqDelay++;
		}else{
			rqDelay = (int)(rqDelay*1.05);
		}
	}
	
}
