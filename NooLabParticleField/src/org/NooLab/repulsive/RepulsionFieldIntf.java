package org.NooLab.repulsive;

 
import org.NooLab.repulsive.intf.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.particles.ParticlesIntf;


/**
 * 
 * see {@link RepulsionField}
 * 
 *
 */
public interface RepulsionFieldIntf {
	
	

	/** 
	 * before calling this, one has to get the instance via the static call [].create(0)</br>
	 * if nbrParticles <0 then the last known storage will be loaded;</br>
	 * if using the the short version, you may consider setting the dynamics via setDynamics() before
	 */
	public void init( RepulsionFieldIntf rfi, int nbrParticles );
	public void init( RepulsionFieldIntf rfi, int nbrParticles,  double energy, double repulsion,  double deceleration ) ;
	public void init( RepulsionFieldIntf rfi, String command );
	
	public void registerEventMessaging(RepulsionFieldEventsIntf eventsreceptor) ;
	
	public void setName(String name);
	
	public String getVersionStr();
	
	// -------------------------------------

	/** getting a list of index values that point to the particles which build a surround located
	 * around the particle closest to the provided coordinates;
	 * alternatively, if the index is known, the index can be used directly (which save a bit of efforts) */
	public String getSurround( int xpos, int ypos , int selectMode, boolean autoselect) ;
	public String getSurround( int index , int selectMode, boolean autoselect);

	/** getting the particle which is closest to the provided coordinates, results return via event */
	public String selectParticleAt(int xpos, int ypos, boolean autoselect);
	
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
	
	public String getParticlesOnLineBetween( int index1, int index2, double channelWidth, boolean autoselect );
	public String getParticlesOnLineBetween( double x1, double y1, double x2,double y2, double channelWidth, boolean autoselect );
	
	// -------------------------------------
	
	/** returns the interface(d) object of the Particles class, which contains 
	 *  the collection of particles as items </br>
	 *  this is necessary if the particles contain pointers to data objects 
	 */
	public ParticlesIntf getParticles() ;
	
	// public ActiveAreaIntf getActiveArea();
	
	// -------------------------------------

	/** adds a particle at a random location (but the random mode can be set to uniform, or gaussian) */
	public int addParticles( int count);
	
	/** adds one particle at a particular location */
	public int addParticles( int x, int y );
	
	/** adds a bunch of particles at the defined particular locations */
	public int addParticles( int x[], int[] y );
	
	
	/** splits a selected (by "index") particle into 2 */
	public int splitParticle( int index );
	
	/** merges at most 7 particles in a single step that are close to each other, in other words, where the
	 *  the distance is less than 1.3 * average distance; </br>
	 *  mergeTargetIndex is the index of the particle "into" which the others will be
	 *  merged (this is relevant for distance criterion) 
	 */
	public int mergeParticles( int mergeTargetIndex, int[] indexes) ;
	public int mergeParticles( int mergeTargetIndex, int swallowedIndex) ;
	
	/** remove the particle with index "index"  */
	public int deleteParticle( int index );

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

	/** default = true, calculatoins are stopped after a stable layout has been found;
	    on adding or deleting a particle, it starts again */
	public void setFreezingAllowed(boolean freezingAllowed) ;
	
	public void setAreaSize( int width, int height) ;
	
	/** the first value is width, the second height*/
	public int[] getAreaSize() ;
	
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

	/** indicates the completion of a single step, where 1 step = recalculating the whole field once*/
	public boolean isUpdateFinished();
	
	public void setColorSize( boolean differentsize, boolean differentcolor );

	/** in multi-threaded mode completely stopping the main thread, causing the switch to single process mode */
	public void stopFieldThread();

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
	
}
