package org.NooLab.som.organizer;

import org.NooLab.som.organizer.communication.GlueSourceAdaptor;

public class SomOrganizerFactory {

	static SomOrganizerFactory somOrgFactory;
	
	SomOrganizer somOrganizer;
	
	GlueSourceAdaptor glueAdaptor;
	
	
	
	// ========================================================================
	private SomOrganizerFactory(){
		init();
	}
	
	public static SomOrganizerFactory createOrganizer(){

		somOrgFactory = new SomOrganizerFactory();
		
		return somOrgFactory;
	}
	
	private void init(){
		glueAdaptor = new GlueSourceAdaptor( this);
		somOrganizer = new SomOrganizer( this );
	}
	// ========================================================================
	
	
	
}
