package org.NooLab.field.fixed;


import org.NooLab.field.interfaces.FixedNodeFieldEventsIntf;
import org.NooLab.field.repulsive.intf.particles.ParticlesIntf;



/**
 * 
 * 
 * 
 * 
 *
 */
public class FixedField 
							implements 
							            // towards the wrapper and use by other libraries
										FixedFieldWintf,
										// within this package
										FixedFieldIntf,
										// "event" callbacks to the outside
										FixedNodeFieldEventsIntf{

	
	 
	
	
	
	private int numberOfParticles;

	// ========================================================================
	FixedField(){
		
	}
	// ========================================================================

	
	
	
	public void registerEventMessaging(FixedNodeFieldEventsIntf eventSink) {
		
		
	}

	public void init(int nbrParticles) {
		// TODO Auto-generated method stub
		
	}

	public void setBorderMode(int borderAll) {
		// TODO Auto-generated method stub
		
	}

	public void setSelectionSize(int _selectionsize) {
		// TODO Auto-generated method stub
		
	}

	public int getSelectionSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setDelayedOnset(int i) {
		// TODO Auto-generated method stub
		
	}

	public void update() {
		// TODO Auto-generated method stub
		
	}

	public int NumberOfParticles() {
		 
		return numberOfParticles;
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public ParticlesIntf getParticles() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSurround(int i, int j, boolean b) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onSelectionRequestCompleted(Object results) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onAreaSizeChanged(Object observable, int width, int height) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onActionAccepted(int action, int state, Object param) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void statusMessage(String msg) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onCalculationsCompleted() {
		// TODO Auto-generated method stub
		
	}




	public int getNumberOfParticles() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	 
	// ------------------------------------------------------------------------
	
	
	
	
	
}


