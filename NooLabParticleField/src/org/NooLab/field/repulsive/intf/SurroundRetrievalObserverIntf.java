package org.NooLab.field.repulsive.intf;



public interface SurroundRetrievalObserverIntf {

	// public void surroundRetrievalUpdate( FluidFieldSurroundRetrieval Observable, String guid );

	public void surroundRetrievalUpdate( Object Observable, String guid );

	
	public void onSurroundBufferUpdateCompletion(String name, int size);
	
	public int getSurroundBuffersUpdateCounter();
}
