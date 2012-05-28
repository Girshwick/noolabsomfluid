package org.NooLab.utilities.callback;

import org.NooLab.utilities.logging.PrintLog;



 
/**
 * class for routing progress information from the worker process to the display process. </br> </br>
 * 
 * <b>simple version</b>: &nbsp;&nbsp;&nbsp;&nbsp; counterLimit, and feedback receptor, which must implement ProcessFeedBackIntf</br>
 * parameters: counterLimit, feedbackReceptor  </br> </br>
 * <b>extended version</b>: allows a dedicated range for the progress, e.g. starting at 25% and ending at 64%,
 * which is useful for smoothly displaying overall progress in case of nested loops</br>
 *  parameters: completionBase, completionMax, counterLimit, feedbackReceptor  
 *
 */
public class ProcessFeedBackContainer implements ProcessFeedBackContainerIntf{

	ProcessFeedBackIntf feedbackSink;
	
	int progressCounterLimit = 100;
	double completion = 0.0, displayValue=0.0, rawCompletion=0.0;
	int currCounterValue=0;
	double progressCompletionBase = 0.0;
	double progresscompletionMax = 100.0 ;
	 
	double stepWidth = 10.0; 
	int stepMode = _STEPMODE_RELATIVE;

	String hostingObjectName;
	String messageProcessID;
	
	int displayMode = _DISPLAY_SMOOTH ;
	int displayedDigits = 0;
	String amountTypeStr = ""; // "%", "n"
	
	PrintLog out = new PrintLog(2,false);

	double counterValueOnLastDisplay=0;
	
	// ========================================================================
	/**
	 * 
	 * @param counterLimit       the largest expected value of the loop counter.
	 * @param feedbackReceptor
	 */
	public ProcessFeedBackContainer( int counterLimit, ProcessFeedBackIntf feedbackReceptor ) {

		feedbackSink = feedbackReceptor ;
		
		if (feedbackSink.setOut()!=null){
			out = feedbackSink.setOut();
		}
		
		progressCounterLimit = counterLimit ;
		
		messageProcessID = feedbackSink.getMessageProcessGuid();
	}
	
	/**
	 * 
	 * @param completionBase  lower limit of returned progress: for instance, default = 0; if set to 30.0 the first value returned as completion progress will be 30.
	 *                        this is helpful if there are nested loops, but the completion indicator should proceed smoothly, not starting at =0 for each nested loop.  
	 * @param completionMax   upper limit for scaling the completion progress, default = 100
	 * @param counterLimit    the largest expected value of the loop.
	 * @param somApplication  the object implementing the update interface
	 */
	public ProcessFeedBackContainer(double completionBase, double completionMax, int counterLimit, ProcessFeedBackIntf feedbackReceptor) {
		//
		feedbackSink = feedbackReceptor ;
		progressCompletionBase = completionBase;
		progresscompletionMax = completionMax;
		 
		double p = ((int)(progressCompletionBase / stepWidth)); // e.g. 24.6 / 10 = 2.46 -> 2.0 
		counterValueOnLastDisplay = (p+1) * stepWidth ; // (2.0+1) * stepWidth(10.0) = 30
		
		progressCounterLimit = counterLimit ;
	}

	public void setCurrentProgressValue(int currCounterValue) {
		boolean displayNow=true;
		double p,sectionscale,nextThreshold = 0;
		
		this.currCounterValue = currCounterValue;
		rawCompletion = 100.0*(((double)( (double)currCounterValue/(double)progressCounterLimit ))) ;
		sectionscale = (progresscompletionMax-progressCompletionBase)/100.0 ;
		completion = progressCompletionBase + rawCompletion * sectionscale ;
		
		if (stepMode== _STEPMODE_RELATIVE){
			
			if (completion>0){
				p = ((int)(counterValueOnLastDisplay / stepWidth)); // e.g. 24.6 / 10 = 2.46 -> 2.0 
				double t = (p+1) * stepWidth ; // (2.0+1) * stepWidth(10.0) = 30
				double d = completion - p; // 24.6 - 20.0 = 4.6
				nextThreshold = t;
				if (displayMode==_DISPLAY_SMOOTH){
					 
					displayValue = t ;// 
				}else{
					displayValue = completion;
				}
				displayNow = completion>nextThreshold ;
			}
			
		}else{
			
		}
		
		if (displayNow){
			 
			feedbackSink.update(this) ;
			if (stepMode== _STEPMODE_RELATIVE){
				counterValueOnLastDisplay = nextThreshold;
			}else{
				counterValueOnLastDisplay = currCounterValue;
			}
		}
	}

	public String getMessageProcessID(){
		return messageProcessID;
	}
	public double getCompletionProgress(){
		
		if (amountTypeStr.length()==0){
			if (stepMode== _STEPMODE_RELATIVE){
				amountTypeStr = "%";
			}else{
				amountTypeStr = "# of " + progressCounterLimit;
			}
		}
		
		amountTypeStr = amountTypeStr.replace("#", ""+currCounterValue); 
		return completion;
	}
	
	/**
	 * increases width of steps of progress updates:
	 * - if mode = absolute then max will be progressCounterLimit/2
	 * - if mode = relative (default) then max will be 50%
	 * 
	 */
	@Override
	public void increaseStepWidth(double incVal) {
		 
		
		if (stepMode == _STEPMODE_RELATIVE){
			stepWidth = stepWidth * (100.0+incVal)/100.0 ;
			if (stepWidth > 50.0){
				stepWidth = 50.0;
			}
		}
		
		if (stepMode == _STEPMODE_ABSOLUTE){
			stepWidth = stepWidth +incVal ;
			if (stepWidth > ((double)progressCounterLimit)/2.01){
				stepWidth = Math.round((double)progressCounterLimit/2.0);
			}
		}
	
	}

	@Override
	public void decreaseStepWidth(double decVal) {
		
		if (stepMode == _STEPMODE_RELATIVE){
			stepWidth = stepWidth * (100.0-decVal)/100.0 ;
			if (stepWidth<0.1){
				stepWidth = 0.1 ;
			}
		}
		
		if (stepMode == _STEPMODE_ABSOLUTE){
			stepWidth = stepWidth - decVal ;
			if (stepWidth < 1.0){
				stepWidth = 1.0;
			}
		}
	
	}

	@Override
	public void pushDisplay() {
		 
		out.print(2, "completed : " + String.format("%."+displayedDigits+"f",displayValue)+ amountTypeStr); // "%"
	}

	@Override
	public void pushDisplay(String loopingObj) {
		 
		out.print(2, loopingObj+" completed : " + String.format("%."+displayedDigits+"f",displayValue)+amountTypeStr);		
	}

	@Override
	public void pushDisplay(String procId, String loopingObj) {
		
		out.print(2, loopingObj+" ("+procId+"), completed : " + String.format("%."+displayedDigits+"f",displayValue)+amountTypeStr);
	}

	public String getHostingObjectName() {
		return hostingObjectName;
	}

	public double getCompletion() {
		return completion;
	}

	public void setCompletion(double completion) {
		this.completion = completion;
	}

	public void setHostingObjectName(String hostingObjectName) {
		this.hostingObjectName = hostingObjectName;
	}

	public int getProgressCounterLimit() {
		return progressCounterLimit;
	}

	public void setProgressCounterLimit(int progressCounterLimit) {
		this.progressCounterLimit = progressCounterLimit;
	}

	public double getProgressCompletionBase() {
		return progressCompletionBase;
	}

	public void setProgressCompletionBase(double progressCompletionBase) {
		this.progressCompletionBase = progressCompletionBase;
	}

	public double getProgresscompletionMax() {
		return progresscompletionMax;
	}

	public void setProgresscompletionMax(double progresscompletionMax) {
		this.progresscompletionMax = progresscompletionMax;
	}

	public int getDisplayMode() {
		return displayMode;
	}

	/**
	 * 
	 * @param displayMode  _DISPLAY_SMOOTH = rounded to multiples of step width (default), _DISPLAY_TRUEVAL = true value of completion proress 
	 */
	public void setDisplayMode(int displayMode) {
		this.displayMode = displayMode;
	}

	public void setMessageProcessID(String messageProcessID) {
		this.messageProcessID = messageProcessID;
	}

	public double getStepWidth() {
		return stepWidth;
	}

	public void setStepWidth(double stepWidth) {
		this.stepWidth = stepWidth;
	}

	public int getStepMode() {
		return stepMode;
	}

	public void setStepMode(int stepMode) {
		this.stepMode = stepMode;
	}

	public void setHostingObject(String simpleObjName) {
		hostingObjectName = simpleObjName;
		
	}

 

	
}
