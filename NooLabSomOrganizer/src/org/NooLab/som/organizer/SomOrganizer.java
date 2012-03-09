package org.NooLab.som.organizer;

class SomOrganizer implements SomOrganizerIntf{

	SomOrganizerTasks somOrganizerTasks ;
	
	public SomOrganizer(SomOrganizerFactory somOrganizerFactory){
	
		somOrganizerTasks = new SomOrganizerTasks(this)  ;
	}
}
