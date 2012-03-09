package org.NooLab.utilities.timing;

import org.NooLab.utilities.logging.PrintLog;



/**
 * 
 * this class 
 * 
 *  
 */
public class Watch{
	int processCount;
	watchProcess[] watchProcesses ;
	 
	PrintLog out = new PrintLog(2,true) ; 
	
	// ========================================================================
	public Watch( int processcount){
		
		processCount = processcount ;
		
		watchProcesses = new watchProcess[processCount+1] ;
		  
		
		for (int i=0;i<processCount+1;i++){
			watchProcesses[i] = new watchProcess(i);
			
		}
	}
	// ========================================================================
	
	public void setReportingPeriod( int pid, int periodLen, int atleastTime){
		watchProcesses[pid].reportingPeriod = periodLen;
		watchProcesses[pid].atleastTime =  atleastTime;
		
	}
	
	public void start(int pid){
		watchProcesses[pid].isActive=true;
	}
	
	public void measure(int pid){
		watchProcesses[pid].measure(1) ;
	}
	
	class watchProcess implements Runnable{
		int processId;
		
		boolean isActive=false ;
		
		long lastReportingTime = 0;
		long lastTime;
		long sumOfDeltaT = 0;
		int tickCount=0, mCount=0;
		int reportCount=0;

		int  atleastTime=1000;
		
		int reportingPeriod = 100 ;
		double averagePerPassage = -1.0 ;
		
		boolean reporting=false;
		boolean isRunning=false; 
		Thread wp;
		
		// ............................................
		public watchProcess(int pid){
			processId = pid;
			lastTime = System.currentTimeMillis();
			lastReportingTime = System.currentTimeMillis();
			
			wp = new Thread(this, "wp-"+pid) ; 
			wp.start() ;
		}
		// ............................................
		
		public void measure( int setTick){
			long dt;
			 
			
			dt = System.currentTimeMillis() - lastTime;
			lastTime = System.currentTimeMillis();
			
			sumOfDeltaT = sumOfDeltaT + dt;
			if (setTick>0){
				tickCount++;
			}
			
			if (tickCount % reportingPeriod == 0){
				reporting = true;
				
				lastReportingTime = System.currentTimeMillis() ;
				reportToConsole();
				
				reporting = false;
			}
			 
		};
		
		public void start(){
			wp.start() ;
		}
		public void stop(){
			isRunning = false;
		}
		
		@Override
		public void run() {
			isRunning = true;
			reporting=false;
			long dueTime = 0;
			 
			try{
				
				while (isRunning){
					
					dueTime = System.currentTimeMillis() - lastReportingTime - atleastTime;
					
					if ( (isActive) && (reporting==false) && (dueTime>=0)){
						reporting = true;
						
						
						reportToConsole();
						
						reporting = false;
					}else{
						Thread.sleep(0, 10) ;
					}
					if (isActive==false){
						Thread.sleep(100) ;
					}
					 
				}

			}catch(Exception e){
			}
		}

		private void reportToConsole() {
			
			lastReportingTime = System.currentTimeMillis() ;
			reportCount++;
			averagePerPassage = Math.round( 1000.0*((1.0+(double)sumOfDeltaT)/(1.0+(double)tickCount)))/1000.0;
			out.print(2,"Watch process (report:"+reportCount+", events:"+tickCount+"), avg.time = "+averagePerPassage);
		}

		public int getReportingPeriod() {
			return reportingPeriod;
		}

		public void setReportingPeriod(int reportingPeriod) {
			this.reportingPeriod = reportingPeriod;
		}

		public double getAveragePerPassage() {
			return averagePerPassage;
		}

		public void setAveragePerPassage(double averagePerPassage) {
			this.averagePerPassage = averagePerPassage;
		}

		public int getProcessId() {
			return processId;
		}

		public long getSumOfDeltaT() {
			return sumOfDeltaT;
		}
		
	}
	
}