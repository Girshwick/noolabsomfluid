package org.NooLab.som.organizer;

import org.NooLab.som.project.SomProject;

/**
 * 
 * this one actually dispatches requests to the actors
 * 
 * 
 */
public class SomOrganizerTask {

	SomProject somProject;
	
	
	public SomOrganizerTask(){
		
	}
	
	public SomProject createTask(){
		somProject = new SomProject();
		
		return somProject;
	}
	public SomProject createTask( SomProject somprj ){
		somProject = somprj;
		
		return somProject;
	}
	
	
}
