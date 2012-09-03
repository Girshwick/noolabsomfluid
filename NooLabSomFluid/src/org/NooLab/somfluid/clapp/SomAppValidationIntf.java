package org.NooLab.somfluid.clapp;

import org.NooLab.somfluid.SomApplicationResults;
import org.NooLab.somfluid.components.SomDataObject;



public interface SomAppValidationIntf extends SomApplicationBasicsIntf {

	public SomApplicationResults classify( int observationIndex ) throws Exception;
	
	public void setSomData( SomDataObject somdata) ;
}
