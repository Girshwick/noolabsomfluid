package org.NooLab.repulsive.intf.main;

 

import java.util.ArrayList;

import org.NooLab.repulsive.RepulsionFieldCore;
import org.NooLab.repulsive.components.data.AreaPoint;
import org.NooLab.repulsive.components.data.LineXY;
import org.NooLab.repulsive.components.data.PointXY;
import org.NooLab.repulsive.intf.ParticleDataHandlingIntf;
import org.NooLab.repulsive.intf.particles.GraphParticlesIntf;
import org.NooLab.repulsive.intf.particles.ParticlesIntf;


/**
 * 
 * see {@link RepulsionFieldCore}
 * 
 *
 */
public interface RepulsionFieldBasicIntf {
	
	  
	
	public void init( int nbrParticles );
	public void init( int nbrParticles,  double energy, double repulsion,  double deceleration ) ;
	public void init( String command );

	/** like Java's Observer : the class must implement "RepulsionFieldEventsIntf", then you
	 *  might pass it to this method for registering  */
	public void registerEventMessaging( Object eventObj ); //RepulsionFieldEventsIntf eventsreceptor) ;
	 
	public void setName(String name);
	public String getName();
	
	public String getVersionStr();
	
	 
	// -------------------------------------
	
	public int getSelectionSize() ;
	
	/** setting the number of particles included in a selection, only if <140 it will follow 
	 * exactly the hexagonal pattern */
	public void setSelectionSize(int n) ;
	
	/** 
	 * increase/decrease of the area that will be selected ;
	 * modes: 1= by "layers" (min=1), (not implemented: 2=by physical distance in % (min= avg distance between particles))
	 */
	public void selectionSizeDecrease(int mode, double amount);
	public void selectionSizeIncrease(int mode, double amount);

	/** if TRUE, then the number of selected particles will follow the hexagonal pattern
	 *  up to a total number of 200 particles per selection; if the aount of particles requested
	 *  to get selected is even larger, then the number will be always AS requested. */
	public void setHexagonSizedSelection(boolean hexagonSizedSelection);
	
	/** not implemented */
	public void setShapeOfSelection();
	
	/** returns the interface(d) object of the Particles class, which contains 
	 *  the collection of particles as items </br>
	 *  this is necessary if the particles contain pointers to data objects 
	 */
	public ParticlesIntf getParticles() ;
	
	public GraphParticlesIntf getGraphParticles();

	
	// -------------------------------------

	/** adds a particle at a random location (but the random mode can be set to uniform, or gaussian) */
	public int addParticles( int count);
	
	/** adds one particle at a particular location */
	public int addParticles( int x, int y );
	
	/** adds a bunch of particles at the defined particular locations */
	public int addParticles( int x[], int[] y );
	
	
	/** splits a selected (by "index") particle into 2 */
	public int splitParticle( int index, ParticleDataHandlingIntf pdataHandler );
	
	/** merges at most 7 particles in a single step that are close to each other, in other words, where the
	 *  the distance is less than 1.3 * average distance; </br>
	 *  mergeTargetIndex is the index of the particle "into" which the others will be
	 *  merged (this is relevant for distance criterion) 
	 */
	public String mergeParticles( int mergeTargetIndex, int[] indexes) ;
	public String mergeParticles( int mergeTargetIndex, int swallowedIndex) ;
	
	/** remove the particle with index "index"  */
	public void deleteParticle( int index );

	/**  type=1 -> absolute, type=2 -> relative; movement will be clipped (without reflection) 
	 *   at the borders of the area if there are borders (e.g. in toroidal topology there are no borders) */
	public void moveParticle( int particleIndex, int type , double xParam, double yParam );

	public int getNumberOfParticles() ;
	
	
	/** deleting the pointers to all data objects  */
	public void clearData(int index);		
	
	/** transfers the pointers (they are of type "long"! ) from one particle to another */
	public void transferData( int fromParticleIndex, int toParticleIndex);
	
	/**
	 * in case of a SOM, usually 1 particle is just 1 node; actually, the particle does not know anything
	 * to what kind of entities the data object index refers to (it may point to a list of heterogenous objects)
	 * yet, it is possible here to collect several nodes in a single particle 
	 * 
	 * @param particleIndex
	 * @param dataPointer
	 */
	public void insertDataPointer( int particleIndex, long dataPointer);

	public void removeDataPointer( int particleIndex, long dataPointer);
	
	
	// -------------------------------------
	
	public double getRepulsion()   ;
	/** if repulsion is high, particles mutually enforce their position more strongly;
	 *  if it is low, then more trembling may be observes, possibly resulting in never finding a stable configuration */
	public void setRepulsion(double repulsion)   ;

	public double getDeceleration()   ;

	public void setDeceleration(double deceleration) ;  
	/** see {@link setEnergy} */
	public double getEnergy()   ;
	/** the movement energy of the particles */
	public void setEnergy(double energy)  ; 

	public int getDelayedOnsetMillis()  ; 

	public void setDelayedOnset(int delayedOnsetMillis) ;
	
	/** default = true, recommended (yet, Eclipse hangs when debugging in multi-threaded mode) */
	public void useParallelProcesses( int flag );
	
	public boolean isMultiProc();
	
	public void setMultiProc( boolean flag );

	/** default = true, calculatoins are stopped after a stable layout has been found;
	    on adding or deleting a particle, it starts again */
	public void setFreezingAllowed(boolean freezingAllowed) ;
	
	public void setAreaSize( int width, int height) ;
	
	/** the first value is width, the second height*/
	public int[] getAreaSize() ;

	public void setDefaultDensity(double dvalue);

	public void setAreaSizeAuto( int nodecounttarget );
	
	public void setAreaSizeMin();
	
	// -------------------------------------

	/** starting the calculation; if in multi-threaded mode, this needs to be called only once;
	 * in single process mode, this ust triggers a single step */
	public void update();
	
	/** limiting the recalculation of the complete field to [steps] steps */
	public void setStepsLimit(int steps) ;
	
	/** stopping the calculation as if it would have been frozen */
	public void interrupt();
	
	public void mobilityDecrease();
	
	public void mobilityIncrease();
	
	/** intensity is from 1..10 */
	public void releaseShakeIt(int intensity);
	
	public void setAdaptiveBehavior(boolean flag);
	 
	/** in case of multi-threaded and separated processes, this returns always true on direct call,
	 *  in decoupled processes use the event onLayoutCompleted() through interface RepulsionFieldEventsIntf;
	 *  i.e. implement this interface for the class (context) which also is calling update()
	 */
	public boolean isCompleted();

	public boolean isReadyToUse();
	
	
	/** indicates the completion of a single step, where 1 step = recalculating the whole field once*/
	public boolean isUpdateFinished();
	
	public void setColorSize( boolean differentsize, boolean differentcolor );

	/** in multi-threaded mode completely stopping the main thread, causing the switch to single process mode */
	// public void stopFieldThread();

	/** whether starting with a random scattering of the particles (=default), or almost regular;
	    starting regular shortens the time needed to find a stable configuration */
	public void setInitialLayoutMode(int initLayoutRegular);

	// -------------------------------------

	/** internal persistence mechanism */
	public void importField() ;

	/** persistence using a dedicated user-based filename */
	public void importField(String filename);

	/** importing just coordinates from a simple text file,</br></br> 
	 *  format, without header (and without quotation marks, of course): </br>
	 *  "x;y"  or "x [TAB] y" 
	 */
	public void importCoordinates( String filename );
	
	public void exportCoordinates( String filename );
	
	public void storeRepulsionField();
	
	public void storeRepulsionField(String filename);

	public void setDynamics( int nbrParticles, double energy, double repulsion, double deceleration);
	
	public void setBorderMode(int borderNone);
	
	public void setMaxDensityDeviationPercent(double d);

	
	
	
	
	
}
