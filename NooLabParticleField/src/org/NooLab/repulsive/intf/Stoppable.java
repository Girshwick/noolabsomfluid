package org.NooLab.repulsive.intf;

public interface Stoppable {

	public void stop();
	
	public boolean isRunning();
	
	public Thread getThread();
}
