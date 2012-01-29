package org.NooLab.repulsive.intf;

import org.NooLab.repulsive.components.SurroundRetrieval;


public interface SurroundRetrievalObserverIntf {

	public void surroundRetrievalUpdate( SurroundRetrieval Observable, String guid );

	public void onSurroundBufferUpdateCompletion(String name, int size);
	
	public int getSurroundBuffersUpdateCounter();
}
