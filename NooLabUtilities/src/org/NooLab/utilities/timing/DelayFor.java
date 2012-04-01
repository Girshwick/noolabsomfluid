package org.NooLab.utilities.timing;


/**
 * 
 * 
 * 
 */
public class DelayFor {

	int minStepWidth = 10;
	boolean userbreak=false;
	boolean visibleFeedback = true ;
	boolean isRunning = false;
	DelayNotificationIntf callBack;
	
	
	public DelayFor( DelayNotificationIntf callback ){
		callBack = callback ;
	}

	public DelayFor(){
		callBack = null ;
	}
	
	@SuppressWarnings("static-access")
	private void simpleWait( long millis){
		try{
			Thread.currentThread().sleep(millis) ;
		}catch(Exception e){}
	}
	
	public void period( long totalTime, boolean visiblefeedback){
		visibleFeedback = visiblefeedback;
		period(totalTime,1,1);
	}

	public void period( long totalTime){
		long stepp = 100;
		if (totalTime*0.6<stepp)stepp=stepp/10;
		period(totalTime,stepp,0) ;
	}

	public void period( long totalTime, int stepping){
		int abs ;
		
		if (stepping<=100){
			abs=0;
		}else{
			abs=1;
		}
		
		period(totalTime,stepping,abs) ;
	}
	
	
	public void period( long totalTime, long stepping, int absolute){
		
		long stepCount = -1, stepWidth = minStepWidth ;
		
		
		if (stepping<0){
			stepping=0;
		}
		if (stepping>totalTime-1){
			stepping=totalTime-1;
		}
		
		if ((stepping<2) && (absolute==1)){
			stepping = minStepWidth;
		}
		
		if ((stepping<2) && (absolute==0)){
			stepping= 2;
		}
		
		
		if (absolute==0){ // relative
			// e.g. totalTime=2500, stepping=10 relative steps => stepCount = 10 
			stepCount = stepping;
			if (stepping<100){
				stepping = Math.round( 100/ stepping) ;
				stepCount = stepping;
			}
		} else{ 
			stepCount = Math.max( 1, Math.round( totalTime/stepping ));
		}
		
		stepWidth = Math.max( minStepWidth, Math.round(totalTime/stepCount)) ;
		
		int z=0;
		long starttime = System.currentTimeMillis() ;
		
		while ((z<stepCount) && (userbreak==false)){
			z++;
			isRunning = true;
			simpleWait(stepWidth) ;
			if ((System.currentTimeMillis() - starttime)>totalTime){
				break;
			}
			if (callBack!=null){
				double perc = 100.0*Math.round( 1000.0 * (double)(z*1.0)/(double)(stepCount*1.0)  )/((double)1000.0) ;
				
				if (visibleFeedback){
					callBack.setPercentageOfWaitingTime(perc);
				}
			}
		}
		z=0;
		isRunning = false;
	}

	public void setUserbreak(boolean stopwaiting) {
		 
		setUserbreak(stopwaiting ,0);
	}
	public void setUserbreak(boolean stopwaiting, int completeFirst) {
		this.userbreak = stopwaiting;
		if (completeFirst>0){
			while (isRunning ){
				simpleWait(2);
			}
		}
	}	
	
}
