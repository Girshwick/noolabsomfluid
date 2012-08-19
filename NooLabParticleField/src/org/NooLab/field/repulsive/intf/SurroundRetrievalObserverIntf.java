package org.NooLab.field.repulsive.intf;

import org.NooLab.field.repulsive.components.SurroundRetrieval;


public interface SurroundRetrievalObserverIntf {

	public void surroundRetrievalUpdate( SurroundRetrieval Observable, String guid );

	public void onSurroundBufferUpdateCompletion(String name, int size);
	
	public int getSurroundBuffersUpdateCounter();
}
