package org.NooLab.utilities.clipboard;

public class CBoardProperties implements CBoardPropertiesIntf{

	
	
	boolean blockIdenticalContent = true;
	private int followUp = 1; // clear by default
	private boolean allowForFetchOnStart;
	private boolean nilAction=false;

	
	
	// ========================================================================
	public CBoardProperties (){
		
	}
	// ========================================================================	

	@Override
	public void setFollowUp(int flag) {
		followUp = flag;
	}
	
	@Override
	public int getFollowUp() {
		return followUp;
	}

	@Override
	public boolean isBlockIdenticalContent() {
		return blockIdenticalContent;
	}

	@Override
	public void setBlockIdenticalContent(boolean flag) {
		this.blockIdenticalContent = flag;
	}

	@Override
	public void setAllowForFetchOnStart(boolean flag) {
		// 
		allowForFetchOnStart = flag;
	}

	@Override
	public  boolean getAllowForFetchOnStart() {
		// 
		return allowForFetchOnStart;
	}

	public void setNilAction(boolean flag) {
		nilAction = flag;
	}

	public boolean isNilAction() {
		return nilAction;
	}
	
}
