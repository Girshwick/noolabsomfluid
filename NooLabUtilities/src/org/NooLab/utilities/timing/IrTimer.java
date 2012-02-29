package org.NooLab.utilities.timing;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 * this replaces the Java Timer for all situations, where
 * the timeout should be more flexibly interruptible;
 * 
 * 
 *  
 * and the TimerTask
 * 
 */
public class IrTimer {

	TimerTask task;
	DelayFor delayInstance;
	
	
	public IrTimer(){
		delayInstance = new DelayFor(null);
	}

	public void stop(){
	
		delayInstance.setUserbreak(true) ;
	}
	
	public void schedule( TimerTask task, long delayTimeValue) {
		this.task = task;
		
		if (delayTimeValue > System.currentTimeMillis()){
			
			delayInstance.period(delayTimeValue, false);
		}
			
		task.run() ;
	}
	
	
	private void go(){
		
	}
	
	private void test(){
		int timeOut=1000;
		
		new Timer(false).schedule(new TimerTask() {
		      public void run() {
		        System.out.println("Requesting stop of registration thread <RegSender>...");
		        go();
		      }
		    }, timeOut);
		
		 
		
		
	}
	
	
}
