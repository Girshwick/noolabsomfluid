package org.NooLab.utilities.clipboard;

public interface CBoardPropertiesIntf {
	
	public static final int _FOLLOWUP_CLEAR_AFTER_FETCHING = 1;
	
	public static final int _FOLLOWUP_KEEP_AFTER_FETCHING  = 2;
	
	
	
	
	public void setAllowForFetchOnStart(boolean b);

	public boolean getAllowForFetchOnStart();

	public void setBlockIdenticalContent(boolean flag);

	public boolean isBlockIdenticalContent();

	public int getFollowUp();

	public void setFollowUp(int flag);
	
	
	
	

}
