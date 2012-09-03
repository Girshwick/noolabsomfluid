package org.NooLab.somfluid.tasks;



/**
 * 
 * 
 * In SomFluid, there are no direct calls. Instead, everything is delivered as a task.
 * This guarantees, that parallel and overlapping requests by external clients can 
 * be served properly.
 * 
 * A task is a package (as object) consisting of IDs, descriptions, class references,
 * and data objects
 * 
 * The Task Dispatcher maintains a FiFo queue, that is worked through
 * 
 * Task may enter SomFluid via the "wireless" communication interface, through
 * the file system (supervised directory), or calls through the API of the 
 * main class SomFluidIntf 
 * 
 */
public class TaskDispatcher implements Runnable{
	Task task;

	
	public TaskDispatcher(){
		
	}


	@Override
	public void run() {
	 
		
	}
	
	
	
}

