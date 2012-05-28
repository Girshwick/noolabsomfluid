package org.NooLab.utilities.callback;

public interface ProcessFeedBackContainerIntf {

	public static final int _STEPMODE_ABSOLUTE = 1;
	public static final int _STEPMODE_RELATIVE = 2;
	
	public static final int _DISPLAY_SMOOTH  = 1;
	public static final int _DISPLAY_TRUEVAL = 2;
	
	
	/** returns the degree of completion */
	public double getCompletionProgress();

	// -----------------------------------------------
	
	/**  */
	public String getMessageProcessID();

	/** returns the name of the class executing the loop */
	public String getHostingObjectName() ;

	// -----------------------------------------------
	
	public void increaseStepWidth(double incVal);

	public void decreaseStepWidth(double decVal);

	// -----------------------------------------------
	
	public void pushDisplay();
	public void pushDisplay(String loopingObj);
	public void pushDisplay(String procId, String loopingObj);

	/**
	 * @param displayMode  _DISPLAY_SMOOTH = rounded to multiples of step width (default), _DISPLAY_TRUEVAL = true value of completion progress;
	 * </br>
	 * constants are available through interface: "ProcessFeedBackContainerIntf." 
	 */
	public void setDisplayMode(int displayMode) ;
	
}
