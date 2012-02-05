package org.NooLab.repulsive.components;

import org.NooLab.repulsive.RepulsionFieldCore;
import org.NooLab.repulsive.components.data.PointXY;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.repulsive.particles.Particle;
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
	
	int beyondIndexValue = -1;
	
	PointXY[] boundingBox = new PointXY[2];
	public PrintLog out  ;
	
	
	// ------------------------------------------------------------------------
	
	public FacadeUpdater(RepulsionFieldIntf rfield, int indexForUpdate, double x, double y, Particles particles, SurroundBuffers sbs, Neighborhood nb, RepulsionFieldCore rfcore){ 
		
		double w = rField.getAreaSize()[0];
		double h = rField.getAreaSize()[1];
		double ad = nb.averageDistance ;
		
		boundingBox[0] = new PointXY( x-ad, y-ad);
		boundingBox[1] = new PointXY( x+ad, y+ad);
		
		surroundBuffers = sbs;
		
	}

	public FacadeUpdater( RepulsionFieldIntf rfield, Particles particles, SurroundBuffers sbs, Neighborhood nb, RepulsionFieldCore rfcore){
		
		rField = rfield ;
		coreInstance = rfcore ;
		neighborhood = nb; 
		surroundBuffers = sbs ;
		
		out = coreInstance.out ;
		
		boundingBox[0] = new PointXY(0,0);
		boundingBox[1] = new PointXY(rField.getAreaSize()[0],rField.getAreaSize()[1]);
		
		// these particles are the particles to be created/updated...
		this.particles = particles; 
		
		if (surroundBuffers == null) {
			surroundBuffers = new SurroundBuffers(rField, out);
			surroundBuffers.setParentName( coreInstance.getName()+"."+this.getClass().getSimpleName());
			
		} 
		surroundBuffers.selectionSize = coreInstance.getSurroundBuffers().selectionSize ;
		
		if (neighborhood == null) 
		{
			
			neighborhood = new Neighborhood( coreInstance.getBorderMode() , surroundBuffers,out ) ;
			neighborhood.setAreaSize( coreInstance.getAreaWidth(), coreInstance.getAreaHeight(), coreInstance.getAreaDepth() );
			neighborhood.setParentName( coreInstance.getName()+"."+this.getClass().getSimpleName());
		} 
		
		
		
		fupThrd = new Thread(this, "fupThrd") ;
		
	}
	// ------------------------------------------------------------------------
	
	public boolean go() {
		return go(-1);
	}
	
	// we have to wait for its completion
	public boolean go( int beyondIndex) {
		isFinished=false;
		
		this.beyondIndexValue = beyondIndex;
		
		fupThrd.start() ;
		
		int z=0;
		while ((isFinished==false) && (z<200*180)){
			out.delay(5);
		}
		
		return true;
	}

	
	private void perform(){

		out.print(2, "start updating the facade layer ... ");

		// 
		if (particles == null) {  
			// first time, after start
			
			// this creates a copy of all particles using "new Particle()" and transferring field values
			// hence it is NOT just the copy of the references!
											out.print(2,"facade updater, copying particles...");
											
			particles = new Particles( coreInstance.particles, surroundBuffers);
											out.print(2,"facade updater, particles copied.  ");
			
			// setting the reference to the facade field
			particles.setField( (RepulsionFieldIntf)rField );
			
			particles.setParentName( rField.getClass().getSimpleName()) ;

		} else { 
											out.print(2,"facade updater, updating particles...");
			// after adding, deleting, moving, trembling, ...
			// this changes the size of the particles field 
			// (which is currently only in this updater, the facade does not know about these particles here so far!)
			particles.updateByParticles( coreInstance.particles, surroundBuffers, beyondIndexValue );
			
			
											out.print(2,"facade updater, updating particles done.");
			if (beyondIndexValue>0){
				// TODO
				// we not only have to add the particle, but we also have to update the surround buffer of
				// all its neighbours... for that we need an abbreviation, a pointer that extends the buffer
				// by those particles which are in wait state ("about to be added")
				// surroundBuffers.exportBuffer(index,...);
				// as long as the particle is not accessible for that buffer retrieval, in those 
				// we perform this in surroundBuffers.updateSurroundExtension() which is called from particles.updateByParticles()
			}
		}

		// two important measures about the population , we need for instance to calc the distance to lines etc. in "Coverage" object
		particles.setAverageDistance( coreInstance.particles.getAverageDistance());
		particles.setAverageDensity( coreInstance.particles.getDensity() ) ;
		
		// finally the plane, takng the reference, but this "neighborhood" is only in this updater so far..
		int r = neighborhood.updateAsCloneFrom( coreInstance.getNeighborhood() );
		
		if (r==0){
			// most of the work has already been done by sync'ing the particles, surround buffers have been transferred
			transferSurroundBuffers();
			surroundBuffers.clearSurroundExtension();
			out.print(2, "update of facade layer completed.");
		}
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

	public SurroundBuffers getSyncedSurroundBuffers( Particles particles){
		 
		neighborhood.particles = particles;
		surroundBuffers.particles = particles;
		
		int n = particles.size();
		
		for (int i=0;i<surroundBuffers.bufferItems.size();i++){
			
			if (i<n){
				particles.get(i).setSurroundBuffer( surroundBuffers.bufferItems.get(i) );
			}
		}
		
		return surroundBuffers;
	}
	

	
	
}
