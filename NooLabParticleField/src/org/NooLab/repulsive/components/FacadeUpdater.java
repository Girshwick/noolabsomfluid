package org.NooLab.repulsive.components;

import org.NooLab.repulsive.RepulsionFieldCore;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.particles.Particles;
import org.NooLab.utilities.logging.PrintLog;



/**
 * 
 * The facade is a "use-layer" for the core layer of the RepulsionField.
 * Its purpose is to proveide an all-time-buffered access to the selection of
 * surrounds or particles.
 * 
 * It cares espacially for the selectionSize: id advises th core always to calculate
 * one/two layers more than the actual size ("guess-ahead")   
 * 
 * 
 */
public class FacadeUpdater implements Runnable{

	RepulsionFieldCore coreInstance;
	RepulsionFieldIntf rField;
	
	Particles particles ;  
	Neighborhood neighborhood ; 
	SurroundBuffers surroundBuffers;
	
	Thread fupThrd;
	boolean isRunning= false, isFinished=false;
	
	public PrintLog out  ;
	
	
	// ------------------------------------------------------------------------
	public FacadeUpdater( RepulsionFieldIntf rfield, Particles particles, SurroundBuffers sbs, Neighborhood nb, RepulsionFieldCore rfcore){
		
		rField = rfield ;
		coreInstance = rfcore ;
		neighborhood = nb; 
		surroundBuffers = sbs ;
		
		// these particles are the particles to be created/updated...
		this.particles = particles; 
		
		if (surroundBuffers == null) {
			surroundBuffers = new SurroundBuffers(rField, out);
			surroundBuffers.setParentName( coreInstance.getName()+"."+this.getClass().getSimpleName());
		} 
 
		
		if (neighborhood == null) {
			
			neighborhood = new Neighborhood( coreInstance.getBorderMode() , surroundBuffers,out ) ;
			neighborhood.setAreaSize( coreInstance.getAreaWidth(), coreInstance.getAreaHeight(), coreInstance.getAreaDepth() );
			neighborhood.setParentName( coreInstance.getName()+"."+this.getClass().getSimpleName());
		} 
		
		
		out = coreInstance.out ;
		fupThrd = new Thread(this, "fupThrd") ;
		
	}
	// ------------------------------------------------------------------------
	
	// we have to wait for its completion
	public boolean go() {
		isFinished=false;
		
		fupThrd.start() ;
		
		int z=0;
		while ((isFinished==false) && (z<200*180)){
			out.delay(5);
		}
		
		return true;
	}

	private void perform(){

		out.print(2, "start updating the facade layer ... ");

		// Plane !
		if (particles == null) {

			particles = new Particles(coreInstance.particles, surroundBuffers);
			particles.setField( (RepulsionFieldIntf)rField ); 
			particles.setParentName( rField.getClass().getSimpleName()) ;

		} else {
			particles.updateByParticles( coreInstance.particles, surroundBuffers );
		}

		// to important measures about the population , we need for instance to calc the distance to lines etc. in "Coverage" object
		particles.setAverageDistance( coreInstance.particles.getAverageDistance());
		particles.setAverageDensity( coreInstance.particles.getDensity() ) ;
		
		// finally the plane
		neighborhood.updateAsCloneFrom( coreInstance.getNeighborhood() );
		
		// most of the work has already been done by sync'ing the particles, surround buffers have been transferred
		transferSurroundBuffers();

		
		out.print(2, "update of facade layer completed.");
		
		isFinished=true;
	}
	
	private void transferSurroundBuffers(){
		// indicating that no data should be transferred 
		surroundBuffers.updateFromSurroundBuffers( particles, coreInstance.getSurroundBuffers(),0 );
		 
	}
	
	@Override
	public void run() {
		isRunning= true;
		perform();
	}

	public Particles getParticles() {
		return particles;
	}

	public Neighborhood getNeighborhood() {
		return neighborhood;
	}

	public SurroundBuffers getSurroundBuffers() {
		return surroundBuffers;
	}



	

	
	
}
