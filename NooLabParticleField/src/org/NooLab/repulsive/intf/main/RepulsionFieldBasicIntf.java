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
public interface RepulsionFieldBasicIntf extends RepulsionFieldSelectionIntf{
	
	  
	
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
	

	/** if TRUE, then the number of selected particles will follow the hexagonal pattern
	 *  up to a total number of 200 particles per selection; if the aount of particles requested
	 *  to get selected is even larger, then the number will be always AS requested. */
	public void setHexagonSizedSelection(boolean hexagonSizedSelection);
	
	
	/** returns the interface(d) object of the Particles class, which contains 
	 *  the collection of particles as items </br>
	 *  this is necessary if the particles contain pointers to data objects 
	 */
	public ParticlesIntf getParticles() ;
	

	
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
	
	public void activateGridOptimizer(boolean flag);

	/** default = true, calculatoins are stopped after a stable layout has been found;
	    on adding or deleting a particle, it starts again */
	public void setFreezingAllowed(boolean freezingAllowed) ;
	
	public void setAreaSize( int width, int height) ;
	
	/** the first value is width, the second height*/
	public int[] getAreaSize() ;

	public void setDefaultDensity(double dvalue);
	
	/** for large number of nodes, this is recommended */
	public void setDefaultDensity(double avgDensity, int nodeCount);

	public void setAreaSizeAuto( int nodecounttarget );

	public double getAverageDistanceBetweenParticles();

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
