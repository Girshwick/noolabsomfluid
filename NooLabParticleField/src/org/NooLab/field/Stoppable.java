package org.NooLab.field;

public interface Stoppable {

	public void stop();
	
	public boolean isRunning();
	
	public Thread getThread();
}
