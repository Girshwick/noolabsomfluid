package org.NooLab.repulsive.components;

import org.NooLab.repulsive.RepulsionField;
import org.NooLab.repulsive.intf.RepulsionFieldEventsIntf;




/**
 * 
 * Basic Principle
 * 
 * 1. 2 layers: 
 *      (1) top-zero layer, read-only for interfaced access, 
 *      (2) mirror layer for calculations, no access through interface
 *        
 * 2. mirror ALWAYS must assume that number of particles are equal 
 * 
 * 3. events through interfaced are first collected here, then routed to the external receptor
 * 
 * 4. Flow of Data
 * 
 * loading from file or basic initialization is FIRST in top-zero-layer,
 * the mirror layer get updated from that, then the mirror start calculating,
 * and will then send back the data 
 * 
 * 
 * 
 * Flow of Commands
 * 
 * add, delete, move will be performed in top-zero layer WITHOUT recalculation, 
 * calcs are then performed on the mirror; in a final step
 * thus, the interfaced methods are just wrappers, which open objects (here) for doing the job
 * 
 * 
 * 
 */
public class FieldMirror implements RepulsionFieldEventsIntf{

	RepulsionField mainRF, mirrorRF ;
	
	public FieldMirror(RepulsionField main, RepulsionField mirror ){
		
		mainRF = main;
		mirrorRF = mirror ; 
	}

	 

	public void startingupMirror() {
		 int upp=0;
		 
		 
		 
		         if (mainRF.isMultiProc()){
		        	 upp=1; 
		         }
		mirrorRF.useParallelProcesses( upp );  
		
		mirrorRF.registerEventMessaging( this );
		
		mirrorRF.setName("app") ;
		mirrorRF.setColorSize(false, true);
		mirrorRF.setInitialLayoutMode(RepulsionField._INIT_LAYOUT_REGULAR);
		mirrorRF.setAreaSize( mainRF.getAreaWidth(), mainRF.getAreaHeight() );		
		
		// setting basic parameters for the dynamic behavior of the particles
		// will be used if there is nothing to load
		mirrorRF.setDynamics( mainRF.getNumberOfParticles(), mainRF.getEnergy(), mainRF.getRepulsion(), mainRF.getDeceleration() );
		mirrorRF.setBorderMode( Neighborhood.__BORDER_ALL);  
		
		/*
		 * NO init() here, we first need the particles before starting the proceses;
		 */

		
	}


	
	// ==== RepulsionFieldEventsIntf ==========================================
	
	@Override
	public void onLayoutCompleted(int flag) {
		
	}

	@Override
	public void onSelectionRequestCompleted(Object results) {
		
	}

	@Override
	public void onAreaSizeChanged(Object observable, int width, int height) {
		
	}

	@Override
	public void onActionAccepted(int action, int state, Object param) {
		
	}

	@Override
	public void statusMesage(String msg) {
		
	}
	// ========================================================================
	
	
	
	
}
