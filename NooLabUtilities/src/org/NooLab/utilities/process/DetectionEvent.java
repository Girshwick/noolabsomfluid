package org.NooLab.utilities.process;

public class DetectionEvent {

	String pattern = "";
	String lastline = "";
	
	long timestamp = 0L;
	
	// ------------------------------------------------------------------------
	public DetectionEvent(){
	}
	// ------------------------------------------------------------------------

	
	public DetectionEvent(String printoutStr, String pattern, long time) {
		// 
		this.pattern = pattern ;
		lastline = printoutStr;
		timestamp= time;
	}


	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getLastline() {
		return lastline;
	}

	public void setLastline(String lastline) {
		this.lastline = lastline;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
