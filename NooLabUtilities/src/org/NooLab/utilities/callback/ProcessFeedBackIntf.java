package org.NooLab.utilities.callback;

import org.NooLab.utilities.logging.PrintLog;




public interface ProcessFeedBackIntf {

	public void update( ProcessFeedBackContainerIntf processFeedBackContainer );

	public String getMessageProcessGuid();
	
	public PrintLog setOut();
}
