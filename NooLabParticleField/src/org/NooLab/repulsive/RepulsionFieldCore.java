package org.NooLab.repulsive;



import java.lang.reflect.Method;
import java.util.*;

import org.math.array.StatisticSample;

import org.NooLab.chord.CompletionEventMessageCallIntf;

import org.NooLab.repulsive.components.CollectStatistics;
import org.NooLab.repulsive.components.FieldSampler;
 

import org.NooLab.repulsive.components.LimitedNeighborhoodUpdate;
import org.NooLab.repulsive.components.Neighborhood;
 
import org.NooLab.repulsive.components.ActionDescriptor;
import org.NooLab.repulsive.components.ParticleAction;
import org.NooLab.repulsive.components.RepulsionFieldProperties;
import org.NooLab.repulsive.components.SamplingField;
import org.NooLab.repulsive.components.Storage;
import org.NooLab.repulsive.components.SurroundBuffers;
import org.NooLab.repulsive.components.SurroundRetrieval;
import org.NooLab.repulsive.components.data.AreaPoint;
import org.NooLab.repulsive.components.data.DislocationXY;
import org.NooLab.repulsive.components.data.FieldPoint;
import org.NooLab.repulsive.components.data.LineXY;
import org.NooLab.repulsive.components.data.PointXY;
import org.NooLab.repulsive.components.data.SurroundResults;
import org.NooLab.repulsive.components.infra.PhysicsDigester;


import org.NooLab.repulsive.intf.ActiveAreaIntf;
import org.NooLab.repulsive.intf.ParticleDataHandlingIntf;
import org.NooLab.repulsive.intf.RepulsionFieldsSyncEventsIntf;
import org.NooLab.repulsive.intf.Stoppable;
import org.NooLab.repulsive.intf.SurroundRetrievalObserverIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldCoreIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
 
import org.NooLab.repulsive.intf.particles.GraphParticlesIntf;
import org.NooLab.repulsive.intf.particles.ParticlesIntf;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.repulsive.particles.Particles;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;


/**
 * Version 1.00.00 </br>
 * Date  Jan,23rd 2012 </br>
 * Author kwa </br> </br>
 * 
 * This software is under any GPL, free for use and free for change.  </br> </br> </br>
 * 
 * This RepulsionField is a 2D layer of freely ranging particles which repel each other;</br>
 * parameters of this repulsion can be set arbitrarily, yet, there is a default setting that 
 * quickly leads to a stable (approximate) hexagonal arrangement.</br></br>
 * 
 * No general adhesion is defined for the particles so far; however, an interface for arbitrary
 * linkages is prepared;</br>
 *  
 * The main purpose of this RepulsionField is to serve as a base layer for a Self-Organizing Map;</br>
 * Usually, SOMs are implemented on fixed grids; this causes a lot of troubles if "growth" should be 
 * implemented for the SOM. The RepulsoinFIeld is a natural solution for this.</br></br>
 * 
 *  This purpose implies a range of consequences concerning the capabilities of the RepulsionFIeld 
 *  and the particles therein:</br>
 *  - all particles are identified by an index; </br>
 *  - the RepulsionField always knows the location of each particle, such that spatially close
 *    particles can be identified. This "spatial knowledge" of the field is buffered into an object
 *    called "SurroundBuffers", i.e. a retrieval of neighborhoods of any size always take 1-2ms. </br>
 *    This "SurroundBuffer", however, needs to be establishment in the beginning after startup AND after
 *    each change due to adding or removing a particle. </br> </br>
 * 
 * Particles </br> </br>
 * 
 * Particles can be added or removed from the field, particular particles as identified by their index can
 * be split or merged. </br>
 * Particles also contain a list of type "long", which can be used as a pointer to an external catalog of objects,
 * e.g. in the case of a SOM to the nodes. </br> </br> 
 * 
 * 
 * Area </br> </br>
 * 
 * The topology regarding the border is completely provided by the neighborhood object; </br> 
 * it provides the actual "borderMode" and the constants for selecting one.
 * Available are: _BORDER_ALL  = border on all sides of the rectangle, topology: confined space
 *                _BORDER_NONE = no logical borders, topology: torus, "endless" area
 * 
 *  </br> </br>
 * Technical Stuff </br> </br>
 * 
 * Most of the processes are multi-threaded, such that even large fields behave reasonably.
 * (Exception: building the SurroundBuffers!)</br> </br> 
 * 
 * Such the core routines from the outside perspective of this class are "update()" and "doPhysics...()";  </br> 
 * From the inside perspective, "collectStatistics()" is one of the main pillars. </br> 
 * This method is called from a separate thread, "always" running in the background to prevent blocking; </br></br> 
 * 
 * 
 * This class implements a public interface as an API for better usability;
 * yet, the complete API that includes the internal API (public getters/setters within the instantiated object)
 * is much bigger, of course. Most of the methods of this class are related to the API's </br></br>
 * 
 * A rather important feature is that the RepulsionField is ALWAYS accessible, including all buffers and
 * dynamic objects; </br>
 * any update is performed first on a mirrored instance, from where the read-only instance then gets updated in 
 * smooth manner. 
 * 
 * The architecture of this bi-layering is realized like so: </br>
 * 1. a facade is created: same interface, but a copy of 
 *    - the area,
 *    - the collection of the particles
 *    - SurroundBuffers, neighborhood objects
 *     
 * 1. the standard RepulsionField is instantiated through the facade; </br>
 *    the facade offers the sae interface, but for the read-operations requested from the areea
 * 2. once ready, the facade is being created, offering the same calls, but without any own functionality,
 *    except the getParticle and the getSurround </br>
 *    for any other functinoality, commands are routed to the main field and data are retrieved by it.  </br>
 * 3. Finally, the references of full-object and its facade are axchanged for the perspective of the client. </br>
 * 
 * The approach using the facade does not need swapping of instances;
 * </br> </br>
 * 
 * 
 * 
 * 
 * </br> </br>
 * Getting Started </br> </br>
 * 
 * The calling class should implement the interface "RepulsionFieldEventsIntf"; it provides to "events" </br>
    - public void onLayoutCompleted( int flag );  </br>
    - public void onSelectionRequestCompleted(Object results); </br>

   The sequence of startup commands is the following: </br> </br>
       
       RepulsionFieldIntf repulsionField; </br>
       repulsionField = RepulsionField.create(); </br>
       repulsionField.registerEventMessaging(this); </br>
       repulsionField.setInitialLayoutMode(RepulsionField._INIT_LAYOUT_REGULAR);
       repulsionField.setAreaSize( width, height );		
	   repulsionField.init(repulsionField,nbrParticles, energy, repulsion, deceleration, applet.width, applet.height);  </br>  </br>

	   repulsionField.update();  </br></br>
 * 
 *  The update() call needs to be repeated if multi-threading is switched off by  </br>
       repulsionField.useParallelProcesses(0);  </br></br>
       
 *  Note that any access to the methods of the RepulsionField is only through its interface, which is
 *  initially retrieved by the Factory 
 *   
 *   
 *   
 *  </br> </br>

 *  
 *  
 *  
 *  
 */
public class RepulsionFieldCore implements 	Runnable, 
								        //  the public interface for usage of the RepulsionField
											RepulsionFieldCoreIntf, 
										//  information about states of the SurroundBuffer process
											SurroundRetrievalObserverIntf , 
										//  information issued by NooLabChord's MultiDigester class
											CompletionEventMessageCallIntf {
 

	
	
	
	String name = "rf" ;
	String versionStr = "1.00.00";
 	
	Vector<RepulsionFieldEventsIntf> eventsReceptors = new Vector<RepulsionFieldEventsIntf>();
	RepulsionFieldsSyncEventsIntf internalLayerEvents; 
	
	public Particles particles, frozenParticles;

	int nbrParticles = 0;
	double currentBaselineDensity = -1.0 ;
	double maxDensityDeviationPercent = -1.0;
	double densityPerAcre = -1.0 ; // 1 acre = 100*100 pix :)
	double averageDistance = -1;
	int stabilizationCounter=0;
	double defaultThresholdForDensity = 40.0;
	
	int areaWidth=1, areaHeight=1, areaDepth=0;
	
	int areaSizeAuto = -1;
	double defaultDensity ;
	int autoSizeNodeCounts ;
	
	boolean areaChangedSize=true ;

	int changesInPopulation=0;
	
	private int neighborhoodBorderMode = Neighborhood.__BORDER_ALL ;
	int initialLayoutMode = RepulsionField._INIT_LAYOUT_RANDOM;
	
	double kRadiusFactor = 1.8; // quite critical...
	/** 
	 * especially in relation to this parameter : the lower the more trembling,
	 * because the area of independence increases
	 */
	double repulsion = 2.0; 

	double deceleration = 0.82;
	double energy = 3.5;
	
	
	private boolean adaptiveBehavior= false;
	private double energyCorrection = 1.0;
	private double decelerationCorrection = 1.0;
	private double repulsionCorrection = 1.0;
	// private double maxTravelDistanceCorrection = 1.0;

	private double initialEnergy = energy;
	private double initialDeceleration = deceleration;
	private double initialRepulsionCorrection = repulsionCorrection ;
	private int initialDelayedOnsetMillis;
	boolean restoreInitialMobilityValues = false;
	
	
	
	boolean freezingAllowed=true;
	boolean fieldLayoutFrozen=false;
	boolean isReadyToUse = false ;
	
	long passes = 0L;
	int stepsLimit = -1;
	
	

	boolean differentSize=true, differentColor=true;
	
	int[] selectionColor = RepulsionField._OUT_SELECTCOLOR ;
	int selectionSize = 61;
	boolean hexagonSizedSelection=true;
	
	
	double sizefactor=1.0; 

	int colormode = 1;
	
	long lastPass = 0 , scPasses=0 ;
	double stability = 0.5;
	boolean updateOnlyAroundChangeLocation=false;
	long relocationInterruptDelay = 25000;
	long relocationInterruptDelayOnDel = 5000;
	
	CollectStatistics statisticsCollector ;
	
	Neighborhood neighborhood;
	
	LimitedNeighborhoodUpdate limitedAreaUpdate;
	SurroundRetrieval surroundRetrieval; // a wrapper for get-surround methods
	SurroundBuffers  surroundBuffers; 
	boolean selectionBuffersActivated=true;
	int multipleDeletions=0;
	
	/** 
	 * this object maintains a waiting queue for fast acceptance, requests are buffered 
	 * if issued quickly after one another, and then handled as a batch 
	 */
	ParticleAction particleAction;
	
	/** this is a low resolution "RepulsionField" (typical n=5) to find the patches for sampling*/
	SamplingField samplingField;
	/** the sampler takes the coordinates and calculates the effective samples */
	FieldSampler sampler ;
	
	boolean isRunning=false, startInitiated=false, isStopped=true ;
	int threadcount = 6;

	public int nextThreadCount=0;
	
	PhysicsDigester physicsProcess ;
	
	
	int delayedOnsetMillis =0;
	
	long threadInitTime = 0;
	boolean multiProc=true, completed = false, updateFinished=true;
	int updateCounter=0;
	
	public Thread fieldThrd; 
	private boolean useOfSamplesForStatistics=true;
	
	RepulsionFieldCore rf ;
	static int nestedInstance;
	RepulsionFieldFactory repulsionFieldFactory;
	
	RelocationDurationLimiter relocationDurationLimiter;
	
	boolean fieldIsRandom = false;
	RepulsionFieldProperties rfProperties;
	Storage storage;
	
	ArrUtilities arrutil = new ArrUtilities();
	public PrintLog out = new PrintLog(2,true);
	
	
	
	/** static method to retrieve an interface to the object, it is possible to provide a name for the instance */
	protected static RepulsionFieldCoreIntf create( RepulsionFieldProperties rfProperties ){
		
		RepulsionFieldCoreIntf rfi = (RepulsionFieldCoreIntf) getInstance(rfProperties);
		
		return rfi;
	}
	protected static RepulsionFieldCore create(){
		/*
		 * note that in this way of construction it is not possible to create two different instances
		 * if any data are shared from this method into the fields of the class...
		 * There will be weird overlaps
		 * 
		 * Instead, here we create a purely local instance, export it, and reimport it
		 * through init() into the fields !
		 * 
		 */
		RepulsionFieldCore rfi;
		RepulsionFieldProperties rfProperties = new RepulsionFieldProperties()  ;
		
		rfi = getInstance(rfProperties);
		
		return rfi;
	}
	
	private static RepulsionFieldCore getInstance( RepulsionFieldProperties rfProperties ){
		return (new RepulsionFieldCore( rfProperties ));
	}
	
	public void setFactoryReference(RepulsionFieldFactory rff) {
		repulsionFieldFactory = rff;
	}
	
	public RepulsionFieldFactory getRepulsionFieldFactory() {
		return repulsionFieldFactory;
	}
	public static int getNestedInstance() {
		return nestedInstance;
	}


	public static void setNestedInstance(int nestedInstance) {
		RepulsionFieldCore.nestedInstance = nestedInstance;
	}


	private RepulsionFieldCore( RepulsionFieldProperties properties) {
		rfProperties = properties;
		
		String userdir = rfProperties.fileutil.getUserDir() ;
		RepulsionField._RFUSERDIR = rfProperties.fileutil.createPath( userdir ,"RepulsionField/" );
		
		out.setPrefix("[RF-CORE]");
	}
	
 
	
	public void init( int nbrParticles ){
		
		rf = repulsionFieldFactory.repulsionField.coreInstance;
		init(  nbrParticles, energy, repulsion, deceleration);
	}
	
	
	private String[] prepareInitCommand( String rawCmdString){
	
		String[] cParts = null ;
		String str;
		//  .init("importField:filename=<filename>" );
		
		int p = rawCmdString.indexOf(":");
		
		if (p>0){
			
			cParts = new String[2];
			str = rawCmdString.substring(0,p);
			cParts[0] = str;
			
			str = rawCmdString.substring(p+1,rawCmdString.length());
			str = str.split("=")[1];
			cParts[1] = str ;
		}else{
			// importField
			
			cParts = new String[]{rawCmdString.trim()};
		}
			
		return cParts;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void init( String commandStr) {
		 
		
		
		init( 10);
		String classNameStr;
		String[] cmdParts;
		
		Object[] paramObjects ;
		Method mthd ;
		Class clazz;
		
		if (commandStr.length()>0){
			try {
				
				// .init("importField:filename=<filename>" );
				
				cmdParts = prepareInitCommand(commandStr);
				
				
				classNameStr = this.getClass().getName() ;
				
			    clazz = Class.forName( classNameStr ); // "RepulsionField");
			    
			    // command e.g. "importField"
			    mthd = clazz.getMethod(cmdParts[0] ,new Class[]{String.class});
			      
			    delay(20);
			    paramObjects = new Object[]{""};
			    
			    if ((cmdParts.length>1) && (cmdParts[1].length()>2)){
			    	
			    	paramObjects = new Object[]{cmdParts[1]};
			    	mthd.invoke( this, paramObjects);
			    	
			    }

			    
			    mthd.invoke( this, paramObjects);	
			    
			    
			    

			    
			} catch (SecurityException e) {
				
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			 
			
		}
		
	}
	
	

	public void init(  int nbrParticles,  double energy, double repulsion,  double deceleration){

		rf = repulsionFieldFactory.repulsionField.coreInstance;

		performInitForInstance(  rf, nbrParticles, energy, repulsion, deceleration);
		
		// the new mirror instance that serves as a background engine for offline calculations
		// will be created only upon request by the FieldMirror object through dedicated request 
		
	
	}


	private void performInitForInstance( RepulsionFieldCore rf, int nbrParticles,  double energy, double repulsion,  double deceleration){
		int w, h;
		
		
		 
		w = areaWidth;
		h = areaHeight ;
		
		rf.setAreaSize(w,h);
		
		rf.out.setPrefix("[RF-CORE]");
		
		this.nbrParticles = nbrParticles;
		 
		this.repulsion = repulsion;
		this.deceleration = deceleration;
		this.energy = energy;
		
		initStorage();
		
		
		if (nbrParticles<0){
			
		}else{
			createParticlesPopulation();
		}
		
		rf.multiProc = multiProc;
		
		
		  
		sizefactor=1.0;
		colormode=0;
		if (differentSize){
			sizefactor = 1.5;
		}
		if (differentColor){
			colormode=1;
		}
		
		 
		Runtime runtime = Runtime.getRuntime();
		threadcount = Math.min( 8,Math.max(2,runtime.availableProcessors()-2)); //processorcount
		
		limitedAreaUpdate = new LimitedNeighborhoodUpdate( this, out ) ;
		particleAction = new ParticleAction(rf);
		 
		initialization();
		
		 
 
		if (hexagonSizedSelection){
			selectionSize = adaptSelectionSize( selectionSize);
		}
		
		
		if (nestedInstance==0){ // 0
			
			
			if ( ( (multiProc == false) && (nbrParticles > 101)) ||  
			     ( (nbrParticles > 700) && (multiProc == true)) ){

				if (useOfSamplesForStatistics){
					/*
					 * in order to detect stability of the Field we have to collect some statistics about the
					 * movement of the particles;
					 * if the field is very large, this is a large effort, hence we take only a subsample of
					 * the particles in the field, such that we have 5 patches , from which we sample adaptively
					 * in order to check just 500 items
					 *   
					 */
					sampler = new FieldSampler(this);

					if (samplingField == null) {
						samplingField = new SamplingField(this);
						samplingField.start();
						
					} // already existing ?	 
				}
				
			} // mono proc or large ?
		} // nestedInstance=0 == do not nest!
		else{
			surroundBuffers=null;
		}
		relocationDurationLimiter = new RelocationDurationLimiter(this) ;
		relocationDurationLimiter.start(0);
	}

	public void initialization(){
		
		if (multiProc){
			isRunning=false;
			delay(50);
			fieldThrd = null;
			
			fieldThrd = new Thread(this,"RepulsionField") ;
			threadInitTime = System.currentTimeMillis() ;
		}
		
		
		
		if (selectionBuffersActivated==true){
			surroundBuffers = new SurroundBuffers( this , particles, out) ;
			surroundBuffers.setParentName( this.name+"."+this.getClass().getSimpleName());
				out.print(3, "restart(context:"+this.getName()+")->initialization() : size of particles = "+particles.size());			
		} 

				
		statisticsCollector = new CollectStatistics(this);
		statisticsCollector.setShowStatisticsInfo(false) ;
		surroundRetrieval = new SurroundRetrieval(this); 
		
		neighborhood = new Neighborhood( neighborhoodBorderMode, surroundBuffers,out ) ;
		neighborhood.setAreaSize( areaWidth, areaHeight, areaDepth);
		neighborhood.setParentName( this.name+"."+this.getClass().getSimpleName());
		

		rf = this;
	}
	
	private void createParticlesPopulation() {
	
		Particle p;
		double globRepulsion ;
		// defining the collections
		particles = new Particles(this);
		frozenParticles = new Particles(this);
		
		
		
		double a,b, rf=0.9;
		double[] positions;
		
		if (nbrParticles>1000)rf=1.2;
		
		globRepulsion = repulsion; 
		
		// populating the collection of normal particles
		for (int i = 0; i < nbrParticles; ++i) {
			
			if (fieldIsRandom ){
				repulsion = calculateForcesRandomization(globRepulsion, -0.6,3.4);
			}
			
	
			p = new Particle(areaWidth, areaHeight, kRadiusFactor, nbrParticles, repulsion, sizefactor, colormode);
			if (initialLayoutMode == RepulsionField._INIT_LAYOUT_REGULAR){
				
				
				positions = getRegularPosition(i) ;
				AreaPoint.x = positions[0] ;
				AreaPoint.y = positions[1] ;
				
				a = positions[2];
				b = positions[3];
				
				AreaPoint.x += (Math.random()*((double)areaWidth/(a+1.0)))/(3.5*rf);
				AreaPoint.y += (Math.random()*((double)areaHeight/(b+1.0)))/(3.5*rf);
				
				if ( ((AreaPoint.x>0) && (AreaPoint.x<areaWidth)) &&
					 ((AreaPoint.y>0) && (AreaPoint.y<areaHeight)) ){
					p.x = AreaPoint.x;
					p.y = AreaPoint.y;
				}
			}
			particles.getItems().add(p);
			
			
		}// i->
		repulsion = globRepulsion ;
		
	}
	
	
	private double calculateForcesRandomization( double valueForRandomization, double min, double max){
		double result = valueForRandomization;
		// =(1/(s*SQRT(2*PI()))*EXP(-(B10-m)*(B10-m)/(2*s*s)))
	
		double m,s, rr , rv=1.0;
		
		m= (2.0*min+max)/3.0; //  
		
		rr = rfProperties.getRelativeRandomness(); // by default = 1.0
		result = -1.0 ;

		double[] rvs = org.math.array.StatisticSample.randomNormal(3, m, (m-min)/2.43);
		
		for (int i=0;i<rvs.length;i++){
			rv = rvs[i];
			if ((rv>min) && (rv<max)){
				result = rv;
				break;
			}
		}
		if (result<0){
			result = valueForRandomization;
			rv=1.0;
		}
		result = valueForRandomization * Math.max(rv, min);
		result = Math.min(result, max);
out.print(4,"rnd : "+ (Math.round(rv*1000.0)/1000.0)+ " ,  randomized repulsion : "+(Math.round(result *1000.0)/1000.0)) ; // Math.round( repulsion*1000.0)/1000.0 ) ;		
		// better to use sth like lognormal
		
		
		return result;
	}
	
	
	public void initStorage(){
		
		if (storage!=null){
			return;
		}
		
		storage = new Storage(this);
		// loading & replacing properties, if the file exists
		// && ONLY IF the propertie have not been provided by the user instance via create() !!!
		if (storage.loadRepulsionFieldPropsData()==0){
			rfProperties = storage.getRfProperties() ;
		}else{
			rfProperties.setHomePath("");
			storage.saveRfProperties( rfProperties );
		}
	}
	
	 
	public void registerEventMessaging(Object eventObj){
		
		RepulsionFieldEventsIntf eventsreceptor;
		
		try{
			
			eventsreceptor = (RepulsionFieldEventsIntf) eventObj;
			if (eventsReceptors.contains( eventsreceptor) == false){
				eventsReceptors.add( eventsreceptor );
			}
			
			internalLayerEvents = (RepulsionFieldsSyncEventsIntf)eventObj;
			
		}catch(Exception e){
			
		}
	}

	 
	
	public void update(){
		
 
		if (fieldLayoutFrozen){
			threadcount=0;
			return;
		}
		
		if (updateFinished ==false){
			return;
		}
		
		updateFinished = false ;
		updateCounter++ ;
		
		if (fieldThrd!=null){
			// parallel processing
			start(); // will start only once !
		}else{
			if (multiProc) {
				if (fieldLayoutFrozen==false){
 
					restart();
				}
			}else{
				// single process

				doPhysicsStandard();
				
if (this.name.contains("sampler")){
	// out.print(2, "... doPhysicsStandard(), calling collectStatistics() ... ") ;
	// collectStatistics( particles );
}
				

				if (statisticsCollector!=null){
					statisticsCollector.setWaiting(false);
				}
			}
		}
		
	}
	
	private ArrayList<Integer> getNeighboredParticlesAccelerated(int i){
		 
		double dx,dy ;
		double _vicinity,_repulsion , _repulsionRange;
		
		ArrayList<Integer> neighbors = new ArrayList<Integer>();
		
		_repulsion = repulsion;

		_vicinity = particles.get(i).radius ;
		_vicinity = averageDistance *1.1 ;
		
		_repulsionRange = particles.get(i).getRepulsionRange() ;
		_repulsionRange = 2.2 ;

		for (int j = 0; j < nbrParticles; j++) {
			
			dx = neighborhood.getLinearDistance(particles.get(i).x , particles.get(j).x, areaWidth) ;
			dy = neighborhood.getLinearDistance(particles.get(i).y , particles.get(j).y, areaHeight) ;
			
			// boolean xNotNeighborIJ = Math.abs(dx) > particles.get(i).radius * ((repulsion*repulsionCorrection)*2.2) ;
			// boolean yNotNeighborIJ = Math.abs(dy) > particles.get(i).radius * ((repulsion*repulsionCorrection)*2.2) ;

			boolean xNotNeighborIJ = Math.abs(dx) > _vicinity * ((_repulsion*repulsionCorrection)* _repulsionRange) ;
			boolean yNotNeighborIJ = Math.abs(dy) > _vicinity * ((_repulsion*repulsionCorrection)* _repulsionRange) ;

			if (j == i	|| xNotNeighborIJ || yNotNeighborIJ){ 
				continue;
			}
			neighbors.add(j);
		}
		 
		
		return neighbors;
	}
	
	private ArrayList<Integer> getNeighboredParticlesNative( int i ){
		
		double dx,dy ;
		double _vicinity,_repulsion , _repulsionRange;
		
		ArrayList<Integer> neighbors = new ArrayList<Integer>();
		
		_repulsion = repulsion;

		_vicinity = particles.get(i).radius ;
		_vicinity = averageDistance *1.1 ;
		
		_repulsionRange = particles.get(i).getRepulsionRange() ;
		_repulsionRange = 2.2 ;

		for (int j = 0; j < nbrParticles; j++) {
			
			dx = neighborhood.getLinearDistance(particles.get(i).x , particles.get(j).x, areaWidth) ;
			dy = neighborhood.getLinearDistance(particles.get(i).y , particles.get(j).y, areaHeight) ;
			
			// boolean xNotNeighborIJ = Math.abs(dx) > particles.get(i).radius * ((repulsion*repulsionCorrection)*2.2) ;
			// boolean yNotNeighborIJ = Math.abs(dy) > particles.get(i).radius * ((repulsion*repulsionCorrection)*2.2) ;


			boolean xNotNeighborIJ = Math.abs(dx) > _vicinity * ((_repulsion*repulsionCorrection)* _repulsionRange) ;
			boolean yNotNeighborIJ = Math.abs(dy) > _vicinity * ((_repulsion*repulsionCorrection)* _repulsionRange) ;

			if (j == i	|| xNotNeighborIJ || yNotNeighborIJ){ 
				continue;
			}
			neighbors.add(j);
		}
		 
		
		return neighbors;
	}
	
	private DislocationXY calculateDislocation( int index, ArrayList<Integer> neighbors) {
		
		int i = index,j;
		double dx,dy,scale ,diff , maxDist, distance ;
		double effectiveRepulsion,rp1,rp2 ;
		
		
		DislocationXY dislocation = new DislocationXY();
			
		try{
			

			for (int n=0; n<neighbors.size();n++){
				 
				j = neighbors.get(n);
				
				if (particles.get(j).getIsAlive()<0){
					continue;
				}

				
				rp1 = 1.0 ;
				rp2 = 1.0 ;
				effectiveRepulsion = 1.0 ;
				
				// without request for random forces, te forces remain fully symmetrical
				// ... if not, then we take (simplified) the effective force for particle i as the weighted mean 
				//     between the forces of the two particles 
				if (fieldIsRandom){
					rp1 = particles.get(i).getRepulsion();
					rp2 = particles.get(j).getRepulsion();
					effectiveRepulsion = Math.max(0.1,((2.0*rp1+rp2)/3.0)) ;
				}
				
				
				dx = neighborhood.getLinearDistance(particles.get(i).x , particles.get(j).x, areaWidth) ;
				dy = neighborhood.getLinearDistance(particles.get(i).y , particles.get(j).y, areaHeight) ;

				
				distance = Math.sqrt(dx * dx + dy * dy);
				maxDist = (particles.get(i).radius + particles.get(j).radius);

				diff = maxDist - distance;

				if (diff > 0) {
					scale = diff / maxDist;
					scale = scale * scale;
					dislocation.wt = dislocation.wt + scale;
					scale = scale * (effectiveRepulsion) * (energy * energyCorrection) / distance;
				
					dislocation.x = dislocation.x + (dx * scale);
					dislocation.y = dislocation.y + (dy * scale);
				}
				
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		 
		return dislocation;
	}
	
	class performPhysicsFor{
		int index;
		
		public performPhysicsFor( int index){
			this.index = index;
		}
		
		
		/**
		 * this is either called by a "direct" loop in "doPhysicsStandard()" in simple-thread configuration,
		 * or it is called by the physicsProcess.doParallelPhysics() in the multi-threaded case, which is
		 * continuously triggered by the run-method of this "RepulsionField" object
		 * 
		 * @param id
		 */ 
		public void go( ) {
			int i = index;
			DislocationXY dislocation ;
			
			
			if (particles.get(index).getIsAlive()<0){
				return;
			}
			
			ArrayList<Integer> neighbors = new ArrayList<Integer>();
			 
			
			particles.get(i).vx *= (deceleration * decelerationCorrection) / 200.0;
			particles.get(i).vy *= (deceleration * decelerationCorrection) / 200.0;
				
			 
			// if there is no calculated surround, we calculate it natively
			// note that particles are not "ordered" along the geometrical arrangement, hence we
			// have to cycle through "all" in native mode
			if (neighborhood == null){
				neighbors = getNeighboredParticlesNative(i);
			}else{
				// faster version checks coordinates list in Neighborhood - object
				neighbors = getNeighboredParticlesAccelerated(i);
			}
			/*
			 * dependent on border settings,  "neighbors" also include particles on the opposite edge
			 */
				 
			 
	 
			dislocation = calculateDislocation( i, neighbors ) ;

			
			if (neighborhoodBorderMode == Neighborhood.__BORDER_ALL){
				calculateDislocationWithinBorders( i, dislocation.wt, dislocation.x, dislocation.y);
			}
			
			if (neighborhoodBorderMode == Neighborhood.__BORDER_NONE){
				// calculateDislocationWithinBorders( i, dislocation.wt, dislocation.x, dislocation.y);
				calculateDislocationWithOutBorders( i, neighbors, dislocation.wt, dislocation.x, dislocation.y);
			}
			
			if (i >= particles.size() - 1) {
				updateParticlesByDislocation();

			}
 
		} // go()
		
	} // inner cass performPysicsFor
	
	

	// a small wrapper, needed for external multi-treading mechanism in physicsProcess (class PhysicsDigester)
	public void doPhysicsFor( int i) {

		(new performPhysicsFor(i)).go() ;
	}
	
	private ArrayList<Integer> getCrossBorderItems( int index, ArrayList<Integer> neighbors, int direction){
		ArrayList<Integer> cB_neighbors = new ArrayList<Integer>() ;
		Particle pi, pn ;
		int nIndex;
		
		for (int n=0;n<neighbors.size(); n++){
			
			nIndex = neighbors.get(n);
			pi = particles.get(index) ; 
			pn = particles.get(nIndex) ;
			
			if (direction==1){
				if ((pi.x < pn.x) && 
					(pn.x > (double)areaWidth - 1.2*averageDistance) &&
					(pi.x < 1.2*averageDistance) ){
					cB_neighbors.add(nIndex );
				}
			} // ==1
			
			if (direction==2){
				if ((pn.x < pi.x) && 
					(pi.x > (double)areaWidth - 1.2*averageDistance) &&
					(pn.x < 1.2*averageDistance) ){
					cB_neighbors.add(nIndex );
				}
			} // ==2
			if (direction==3){
				if ((pi.y < pn.y) && 
					(pn.y > (double)areaHeight - 1.2*averageDistance) &&
					(pi.y < 1.2*averageDistance) ){
					cB_neighbors.add(nIndex );
				}
			} // ==3
			
			if (direction==4){
				if ((pn.y < pi.y) && 
					(pi.y > (double)areaHeight - 1.2*averageDistance) &&
					(pn.y < 1.2*averageDistance) ){
					cB_neighbors.add(nIndex );
				}
			} // ==4
			
			 
		}
		
		
		return cB_neighbors;
	}
	
	
	private void calculateDislocationWithOutBorders( int index, ArrayList<Integer> neighbors, double wt,  double fx, double fy){
	
		Particle particle_i = particles.get(index) ;
		
		double dx, dy, distance ,wtd =0.0, scale, diff ;
		double maxDist = particle_i.radius;
		ArrayList<Integer> crossBorderNeighbors;
		DislocationXY dislocation ; 
		double df = 0.8 ;
	
		
	
	    // keep within edges
	
		// left edge
		distance = particle_i.x ; 
		dx = distance;
		 
		diff = maxDist - distance;
		if (diff > 0) {
			scale = diff / maxDist;
			scale = scale * scale;
			wt = wt + scale;
			 
			  
			// are there any items that are close but also near to the opposite RIGHT edge? 
			crossBorderNeighbors = getCrossBorderItems( index, neighbors,1 ); // 1: L->R, 2: R->L, 3: T->B; 4: B->T, 
			if (crossBorderNeighbors.size()>0){
				// calculate influence from particles on the right side	
				// out.print(2, "cross-border adjustment, ...");	 
				dislocation = calculateDislocation( index, crossBorderNeighbors ) ;
			 
				fx =  fx + df * dislocation.x; 
				fy =  fy + df * dislocation.y; 
				wtd = wtd + dislocation.wt; 
			}
		}
		
		// right edge
		dx = particle_i.x - areaWidth;
		distance = -dx;
		diff = maxDist - distance;
		if (diff > 0) {
			scale = diff / maxDist;
			scale = scale * scale;
			wt = wt + scale;
			 
			// are there any items that are close but also near to the opposite LEFT edge?
			crossBorderNeighbors = getCrossBorderItems( index, neighbors,2 );  
			if (crossBorderNeighbors.size()>0){
				// calculate influence from particles on the right side	
				 
				dislocation = calculateDislocation( index, crossBorderNeighbors ) ;
				fx =  fx + df * dislocation.x; 
				fy =  fy + df * dislocation.y; 
				wtd = wtd + dislocation.wt; 
			}
		}
		
		// top edge
		distance = particle_i.y ; 
		dy = distance ;
		 
		diff = maxDist - distance;
		if (diff > 0) {
			scale = diff / maxDist;
			scale = scale * scale;
			wt = wt + scale;
			 
			crossBorderNeighbors = getCrossBorderItems( index, neighbors,3 );  
			if (crossBorderNeighbors.size()>0){
				// calculate influence from particles on the right side	
				 
				dislocation = calculateDislocation( index, crossBorderNeighbors ) ;
				fx =  fx + df * dislocation.x; 
				fy =  fy + df * dislocation.y; 
				wtd = wtd + dislocation.wt; 
			}
		}
	
		// bottom edge
		dy = particle_i.y - areaHeight;
		distance = -dy;
		diff = maxDist - distance;
		if (diff > 0) {
			scale = diff / maxDist;
			scale = scale * scale;
			wt = wt + scale;
			
			crossBorderNeighbors = getCrossBorderItems( index, neighbors,4 );  
			if (crossBorderNeighbors.size()>0){
				// calculate influence from particles on the right side	
				 
				dislocation = calculateDislocation( index, crossBorderNeighbors ) ;
				fx =  fx + df * dislocation.x; 
				fy =  fy + df * dislocation.y; 
				wtd = wtd + dislocation.wt; 
			}
		}
		
		
		 
		if (wt > 0) {
			 
			particle_i.vx = particle_i.vx + fx / ((wt+wtd)/2.0);
			particle_i.vy = particle_i.vy + fy / ((wt+wtd)/2.0);
		}
		 
	}
	private void calculateDislocationWithinBorders( int index, double wt,  double fx, double fy){
	
		Particle particle_i = particles.get(index) ;
		
		double dx, dy, distance , scale, diff ;
		double maxDist = particle_i.radius;
	
	
	    // keep within edges
	
		// left edge
		distance = particle_i.x ; 
		dx = distance;
		 
		diff = maxDist - distance;
		if (diff > 0) {
			scale = diff / maxDist;
			scale = scale * scale;
			wt = wt + scale;
			scale = scale * (energy * energyCorrection) / distance;
			fx = fx + dx * scale;
			 
		}
		
		// right edge
		dx = particle_i.x - areaWidth;
		distance = -dx;
		diff = maxDist - distance;
		if (diff > 0) {
			scale = diff / maxDist;
			scale = scale * scale;
			wt = wt + scale;
			scale = scale * (energy * energyCorrection) / distance;
			fx = fx + dx * scale;
			 
		}
		// top edge
		distance = particle_i.y ; 
		dy = distance ;
		 
		diff = maxDist - distance;
		if (diff > 0) {
			scale = diff / maxDist;
			scale = scale * scale;
			wt = wt + scale;
			scale = scale * (energy * energyCorrection) / distance;
			 
			fy = fy + dy * scale;
		}
	
		// bottom edge
		dy = particle_i.y - areaHeight;
		distance = -dy;
		diff = maxDist - distance;
		if (diff > 0) {
			scale = diff / maxDist;
			scale = scale * scale;
			wt = wt + scale;
			scale = scale * (energy * energyCorrection) / distance;
			 
			fy = fy + dy * scale;
		}
		 
		 
		if (wt > 0) {
			 
			particle_i.vx = particle_i.vx + fx / wt;
			particle_i.vy = particle_i.vy + fy / wt;
		}
		 
		
	}
	private void updateParticlesByDislocation(){

		for (int k = 0; k < nbrParticles; k++) {

			double xpos = particles.get(k).x;
			double ypos = particles.get(k).y;

			double _vx = particles.get(k).vx;
			double _vy = particles.get(k).vy;

			particles.get(k).setMovedDistance(Math.sqrt(_vx * _vx + _vy * _vy));

			xpos = xpos + _vx;
			ypos = ypos + _vy;

			
			double[] spatialpos = neighborhood.adjustSpatialPositionsToBorderSettings( xpos,ypos, particles.get(k).radius, neighborhoodBorderMode);
			xpos = spatialpos[0];
			ypos = spatialpos[1];
			
			particles.get(k).x = xpos;
			particles.get(k).y = ypos;
		}
	}
	/**   
	 * 
	 * this s used if not multithreading should be applied
	 * 
	 */
	private void doPhysicsStandard() {
		
		passes++;
		if ((stepsLimit>0) && (passes>stepsLimit)){
			return;
		}
		// opening an object in its thread just for this round
		/*
		 build a blocker for preventing new objects if the last did not finish
		(new SimpleUpdate()).go();
		*/
		
		for (int i = 0; i < nbrParticles; ++i) {
			doPhysicsFor( i );
		} 
		
		 
		updateFinished = true ;
	}
	
	
	
	class SimpleUpdate implements Runnable{
		
		Thread supThrd;
		boolean completed=true, supIsRunning=false;
		
		public SimpleUpdate(){
			
			if (supIsRunning){
				return;
			}
			
			supThrd = new Thread(this,"supThrd");	
			
			multiProc = false;
			if (statisticsCollector==null){
				statisticsCollector = new CollectStatistics(rf);
				// statisticsCollector.explicitTrigger();
			}
			if (neighborhood == null){
				neighborhood = new Neighborhood( neighborhoodBorderMode, surroundBuffers,out) ;
				// neighborhood.setBorderMode( neighborhoodBorderMode );
			}
		}

		private void perform(){
			for (int i = 0; i < nbrParticles; ++i) {
				
				doPhysicsFor( i );
			}
			completed=true;
			statisticsCollector.isWaiting=false;
			updateFinished = true ;
		}
		
		public void go(){
			if (supIsRunning){
				return;
			}
			
			completed=false ;
			supIsRunning=true;
			
			supThrd.start();
			while (completed==false){
				delay(1);
			}
			
			supIsRunning=false;
		}
		
		@Override
		public void run() {
			perform();
		}
	}
	
	
	/**
	 * this will be called (back) by object CollectStatistics, which runs its own
	 * thread for colecting the sttistics about the locations or the particles in the field
	 * 
	 * 
	 * @param particles
	 */
	public void collectStatistics( Particles particles ){
			 
			double dx ,dy, distance ;
			Particle particle_i, particle_j;
			
			Vector<Neighbor> neighbors = new Vector<Neighbor>();
			double minObsDistance=0.0, movedDistanceSum =0.0,minDistSum=0.0, mds_sqr =0.0, var=0;
			
			if (statisticsCollector==null){
				out.print(3,"collectStatistics() <in RepulsionField: "+this.name+"> :strange call from the abyss?");
				return;
			}
			statisticsCollector.setShowStatisticsInfo(true) ;
			 
			if (fieldLayoutFrozen){
				scPasses=0;
				out.print(3,"frozen !");
				return;
			}
			
			
			int z;
			double t = (3+2*Math.log(2+particles.size()) );
			       if (threadcount>0){t = 2+t/(threadcount) ;}
			       t = Math.round(t*10)/10; 
			       							
			scPasses++;
			// either if we have left out t steps, or
			if (( scPasses <t ) ) {  
				// return;
			}else{
				
				scPasses=0;
			}

											out.print(3,"collecting stats, "+((int)t)+" by-passed...  pass no. "+passes+"  ... ");
			try{
		// before we start with this we should ensure that the neighborhood has worked through its queue of tasks
			z=0;
		// after a few passes, we will know for each particle its neighbors ... 
			for (int i=0;i<particles.size();i++){
				
				particle_i = particles.get(i);
				if (particle_i==null){
					continue;
				}
				
				// is this index included in the list of particles within the sample (if there is one) ?
				if ((sampler != null) && (sampler.getSamplesAvailable())){
					// Particles sp = sampler.getSamplePatchCenterParticles();
					sampler.particleIsInSample(particle_i) ; // performs a simple check on coordinates and radius
				}
				
				
				// 
				neighbors.clear();
				minObsDistance = 9999.0;
				z=0;
				
				// we should not loop through all particles, just through the "neighborhood", if it already exists
				// this save a lot of time for larger collections (>100000)
				int sp = 0;                  //  neighborhood.getLowerBoundForSurround(i,20, particles.size());
				int ep = particles.size() ;  // neighborhood.getUpperBoundForSurround(i,20, particles.size());
				
				for (int j=sp;j<ep;j++){
					
					if (i==j){
						continue;
					}
					particle_j = particles.get(j);
					if (particle_j==null){
						continue;
					}
					dx = particle_i.x - particle_j.x;
					dy = particle_i.y - particle_j.y;
					distance = Math.sqrt(dx * dx + dy * dy);
				
					if (distance < minObsDistance){
						if (neighbors.size()==0){
							neighbors.add(new Neighbor(j,particle_j,distance));
						}else{
							if (neighbors.size()>=19){
								try{
									neighbors.remove(19) ; // 18 items: 2 "layers" around an item in hex pattern
								}catch(Exception e){}
							}
							neighbors.insertElementAt(new Neighbor(j,particle_j,distance), 0);
						}
						minObsDistance = distance ;
						z++;
					} // distance < minObsDistance ?
					
				} // j->
				
				minDistSum += minObsDistance;
				movedDistanceSum += particle_i.getMovedDistance();
				mds_sqr = mds_sqr + (particle_i.getMovedDistance()*particle_i.getMovedDistance()); 
				
				if (neighborhood!=null){
					neighborhood.update(i, particle_i.x,particle_i.y, particle_i.radius);
				}
			    // neighborhood.finalizeQ(5);
			} // i->
			
												out.print(3,"collecting stats: all particles visited... ");
			// out.print(2, "finalizing ");
			neighborhood.finalizeQ();
			movedDistanceSum = movedDistanceSum/particles.size();
			
			
			
			minDistSum = minDistSum/particles.size();
			neighborhood.setAverageDistance(minDistSum) ;
			averageDistance = minDistSum;
												out.print(4,"calculating some parameters ... ");
												
			double v = Math.log(1+ particles.size() );
			var =  mds_sqr - movedDistanceSum*movedDistanceSum ; 
			
			double _rad = particles.get(0).radius;
												out.print(3,"before calculating stability measure (moved distance="+movedDistanceSum+", radius="+_rad+",n-p factor="+v+")... ");
												
			if ((movedDistanceSum>0) && (movedDistanceSum/(0.01+ _rad *v) <0.1)){
				
				String vs1,vs2,vs3,vs0,vs4;
												out.print(4,"calculating stability measure (1)... ");
	
				vs0= ""+  minDistSum;  minDistSum = Math.round(movedDistanceSum*10000.0)/10000.0;    vs0= vs0.substring(0,Math.min(5,vs0.length()));
				vs1= ""+ Math.round(movedDistanceSum*10000.0)/10000.0; 
						 vs1= vs1.substring(0,Math.min(5,vs1.length()));
	                     if (movedDistanceSum < 0.0001){vs1="0.0001";}  
	                     
				vs2= ""+ var/(1.0) ; 
						 vs2= vs2.substring(0,Math.min(5,vs2.length()));
				
				vs4= ""+ ((int)particles.get(0).radius) ; 
						 vs4 = vs4.substring(0,Math.min(4,vs4.length()));
						 						out.print(4,"calculating stability measure (2)... ");
						 // log-N*sqrt-D*sqrt log
				double q = (v * Math.sqrt(minDistSum) * Math.sqrt(v)) + (minDistSum*movedDistanceSum); 
				
							q = Math.round(q*10000.0)/10000.0;
							vs3= ""+(q) ; 
							vs3 = vs3.substring(0,Math.min(5,vs3.length()));
					
												out.print(4,"calculating stability measure (3)... ");
							//out.print(2,"population (n="+particles.size()+") came to rest, radius = "+vs4+" ,  mean distance = "+vs0+" ; movedDistance mean = "+ vs1+"  q = "+vs3);
	
				     if (movedDistanceSum<0.0001){
				    	 movedDistanceSum=0.0001;
				     }
				//double space =  minDistSum - particles.get(1).radius;
				// double spacemove = space * movedDistanceSum;
				// double sm = q * spacemove;
												out.print(4,"calculating stability measure (4)... ");
				if (q<0.0001){q=0.0001;}
				stability = (Math.log(1+nbrParticles))*(Math.log(1+1/q)); 
							stability =Math.round(stability*10000.0)/10000.0 ;
	 						vs4= ""+stability; vs4 = vs4.substring(0,Math.min(5,vs4.length()));
							// out.print(2,"population (n="+particles.size()+") came to rest, radius = "+((int)particles.get(1).radius)+" ,  mean distance = "+vs0+"  movedDistance mean = "+ vs1+"  q = "+vs3 + "   stability = "+vs4);
	 						// out.print(2,"population (n="+particles.size()+") came to rest,  q = "+vs3 + "   stability = "+vs4);
	 											out.print(4,"stability measure calculated, now setting... ");
	 			statisticsCollector.setCurrentMovedDistanceSum( movedDistanceSum);
	 			// here it is alrady the average
	 			if ((movedDistanceSum>3) && (particles.size()<15)){
	 				stability = stability * 10+ 5*Math.random();
	 				out.print(3,"movedDistanceSum = "+movedDistanceSum);
	 			}
			    statisticsCollector.setCurrentStability(stability);
			    /**
			         values ~0.3 -> trembling of some
			                <0.1 -> trembling of many
			                >1   -> fixed
			                >2   -> perfectly fixed 
			     */
			 // 1 acre = 100*100 pix 
			    densityPerAcre = calculateDensity(particles.size(), 0);
			    
			    q = statisticsCollector.getTrendStabilityValue();
			    
			    								if (this.name.contains("sampler")){
			    								    out.print(4,"collecting stats: concluding checks, q="+q+" ... ");
			    								}
			    
			    particles.setAverageDistance(averageDistance);
			    particles.setAverageDensity(densityPerAcre);
			    
			    threadcountController(q);
			    adaptMobilityCorrectionFactors(q);
			    freezeLayout(q);
			    
			    
			}
												out.print(3,"collecting stats: leaving ... ");
			// out.print(2,"mean moved distance : "+movedDistanceSum);
			// check if it is active, then set it to frozen ()
			}catch(Exception e){
				
			}finally{
				out.print(3,"collecting stats: exiting ... ");	
			}
			
		}


	public void increaseStepsCounter( int n){
		passes = passes + n;
	}
	
	 


	
	@Override
	public void onSurroundBufferUpdateCompletion(String name, int size) {

											out.print(4, "Msg received in RepulsionField-main: onSurroundBufferUpdateCompletion("+name+")") ;
		
		// (new CallbackServer()).go(1,1);
		
		if ((eventsReceptors != null) && (eventsReceptors.size()>0)){
			for (int i=0;i<eventsReceptors.size();i++){
				
				eventsReceptors.get(i).onCalculationsCompleted();
			}
		}
		isReadyToUse = true;
		 
		
		if (particleAction.size()>0){
			releaseShakeIt(3);
		}
	}
	
	
	/**
	 * 
	 * an event triggered by SurroundRetrieval.
	 * We need this event mechanism, since SurroundRetrieval decouples by its own thread
	 * 
	 */
	@Override
	public void surroundRetrievalUpdate( SurroundRetrieval Observable, String guid) {
	
		
		out.print(4, "result returned to core from SurroundRetrieval(), result id = "+guid );
		
		// now we can retrieve the results
		SurroundResults result = (SurroundResults) Observable.getResultsByGuid(guid);
				

		if (result == null) {
			return;
		}

		 
		if (result.getParamSet().getTask() >= SurroundRetrieval._TASK_SURROUND_C) {
			particles.selectSurround(result.getParticleIndexes(), result.getParamSet().isAutoselect());
		}

		if (result.getParamSet().getTask() <= SurroundRetrieval._TASK_PARTICLE) {
			result.setParticleIndexes( new int[] { (int) result.particleIndex });
			particles.selectSurround(result.getParticleIndexes(), result.getParamSet().isAutoselect());
			
			if (result.getParamSet().getTask() <= SurroundRetrieval._TASK_PARTICLE) {
				
			}
		}

		if ((eventsReceptors != null) && (eventsReceptors.size()>0)){
			for (int i=0;i<eventsReceptors.size();i++){
				eventsReceptors.get(i).onSelectionRequestCompleted(result);
			}
		}

	}


	
	
	@Override
	public String selectParticleAt(int xpos, int ypos, boolean autoselect) {
		
		String guidStr="";
		int pix;
		
		pix = surroundRetrieval.addRetrieval( xpos, ypos, autoselect);
		
		guidStr = surroundRetrieval.go(pix,SurroundRetrieval._TASK_PARTICLE);
		 
		return guidStr;
		
	}


	/**
	 * we may determine the surround of either with regard to a coordinate
	 * or with regard to a selected particle, which we identify by index;
	 * 
	 * int[]
	 */
	public String getSurround( int xpos, int ypos ,  
			  				   int selectMode, boolean autoselect){
		String guidStr="";
		int pix;
		int surroundN = 7;
		
		if (selectionSize>5){
			surroundN = selectionSize;
		}
		
		// we to outsource the Surround object into a wrapper, then waiting for an event.
		
		pix = surroundRetrieval.addRetrieval( xpos, ypos, surroundN, selectMode, autoselect);
		
		guidStr = surroundRetrieval.go(pix, SurroundRetrieval._TASK_SURROUND_C);
		 
		return guidStr;
	}


	/**
	 *  selectMode=1 : selecting closest "surroundN" items;
	 *  selectMode=2 : selecting closest items within a radius of surroundN   
	 */
	public String getSurround( int index ,   
							   int selectMode,
							   boolean autoselect){
		
		
		String guidStr="";
		int pix;
		int surroundN=7;

		if (selectionSize>5){
			surroundN = selectionSize;
		}
		
		pix = surroundRetrieval.addRetrieval( index, surroundN, selectMode, autoselect);
		
		guidStr = surroundRetrieval.go(pix, SurroundRetrieval._TASK_SURROUND_X);
		 
		return guidStr;
 
	}




	@Override
	public String getParticlesAroundMST( int[] indexes, double thickness,
										 double endPointRatio, boolean autoselect) {
		//  
		String guidStr="";
		int pix;
		
		// pix then will contain the indx to a slot in the collecton of "paramSets"
		pix = surroundRetrieval.addRetrieval( indexes,  thickness, endPointRatio, autoselect);
		
		guidStr = surroundRetrieval.go(pix,SurroundRetrieval._TASK_SURROUND_MST);
		 
		return guidStr;
	}
	
	
	@Override
	public String getParticlesAroundMST( ArrayList<PointXY> points,
									     double thickness, double endPointRatio, boolean autoselect) {
		//  
		return null;
	}
	
	
	@Override
	public String getParticlesWithinConvexHull(int[] indexes, double thickness, int topology, boolean autoselect) {
			
		String guidStr="";
		int pix;
		
		// pix then will contain the indx to a slot in the collecton of "paramSets"
		pix = surroundRetrieval.addRetrieval( indexes,  thickness, topology, autoselect);
		
		guidStr = surroundRetrieval.go(pix,SurroundRetrieval._TASK_SURROUND_MST);
		 
		return guidStr;
	}
	
	
	
	@Override
	public String getMinimumSpanningTree( ArrayList<PointXY> points, boolean autoselect) {
		//  
		return null;
	}
	
	@Override
	public String getParticlesWithinConvexHull(ArrayList<PointXY> points, double thickness, int topology,boolean autoselect) {
		// 
		return null;
	}
	

	
	@Override
	public void selectionSizeDecrease(int mode, double amount) {
		 
		int layerCount;
		
		if (mode<=1){
			 
			layerCount = getLayerCountOfHexPattern(selectionSize);
			if (layerCount>=2){
				selectionSize = calculatePlateletsCountInHexPattern(layerCount-1);
				out.print(2, "selectionSize has been reduced to "+selectionSize+" particles.");
			}
			
		}else{
			
		}
		
	}

	
	@Override
	public void selectionSizeIncrease(int mode, double amount) {
		int layerCount;
		// 1 layer : 6+1, 2 layers = 1+6+10 = 17  1+6+10+17+34 
		mode = 1; amount=1; 
		 
		/*
       sum  layer  %		
		1	6      
		7	12     700   
		19	18     270  
		37	24     190
        61  30     165
        91  36     149
        127        139
		*/
		if (mode<=1){
			
			layerCount = getLayerCountOfHexPattern(selectionSize);
			selectionSize = calculatePlateletsCountInHexPattern(layerCount+1);
			
			out.print(2, "selectionSize has been increased to "+selectionSize+" particles.");
			 
			if (surroundBuffers!=null){
				surroundBuffers.update();
			}
			
		}else{
			
		}
		
	}


	@Override
	public void setShapeOfSelection() {
		// TODO: offer hex , circle (=hex constrained by radius), square
	}



	
	public void deleteParticle( int index){
		
		deleteParticle( new int[]{index});
	}
	
	public void deleteParticle( int[] indexes){
		
		// (new ParticleAction(this)).scheduleForRemoval(index);
		// this object then contains a waiting queue, the items of which then are removed all at once
		// inclusive complete rebuild of surround
		
		// alternative strategy: immediately setting the particle to "invisible=true"
		// such it will not be regarded anywhere
		int index = 0,count ;
		Particle p=null;
		double newradius;
		//int deletedIndex = -1;
		
		int n = nbrParticles-2;
		if (n<=1){
			n=1;
			return  ;
		}else{

			if ((indexes==null) || (indexes.length==0)){
				return  ;
			}
			
			 

			count = indexes.length;
			
			if (count==0){return  ;}
			Arrays.sort(indexes);
			index = indexes[0];
			// there can be problems if deletions overlap, so we wait until it is frozen, or we set it to frozen
			if ((fieldLayoutFrozen==false) || (surroundBuffers.getUpdating()>0)){
				 
				eventsReceptors.get(0).onActionAccepted( RepulsionFieldEventsIntf._FIELDACTION_DEL, 
														 RepulsionFieldEventsIntf._FIELDSTATE_DLY, index) ;
													//   signalling delay...
				
				for (int i = 0; i < count; i++) {
					index = indexes[i];
					if ((index>=0) && (index<particles.size())){
						ActionDescriptor ad = new ActionDescriptor();
						ad.setActionCode( RepulsionFieldEventsIntf._FIELDACTION_DEL );
						ad.setX( particles.get(index).x );
						ad.setY( particles.get(index).y);
						ad.setIndex(index);
						particleAction.add(ad);
						out.print(2, "n="+particleAction.size("d")+"  new particles are waiting to be deleted...");
					}
				} // i->
 
				// as soon as the queue contains an element, a waiting thread is started, which checks the frozen state
				// if then the field is frozen, all items of the queue are handled at once
				return  ;
				 
			} // fieldLayoutFrozen==false ?
			
			
			for (int i = 0; i < count; i++) {
				index = indexes[i];

				// out.print(2, "waiting for possibly active particle (index:"+index+") ending its location update ...");
				p=null;
				if ((index>=0) && (index<=particles.size())){
					p = particles.get(index);
				}
				
				if (p==null){
					continue;
				}
				

				// this is just a data object, used by surround and neighborhood
				// region will be removed upon its usage 
				// limitedAreaUpdate.addRegion( p.x,p.y );

				isReadyToUse = false ;
				
				out.print(2, "physically removing now particle index : "+index+"...");
				particles.remove(index) ; // will propagate its removal down to the surroundbuffers and its bufferitems collection 
				
			} // i->
			
			if (count>10){
				// enforcing a restart
				// surroundBuffers.clear() ;
				// surroundBuffers = null;
			}

			nbrParticles = particles.size();
			
			//int dn = populationSizeBefore - nbrParticles ;
			changesInPopulation++;
		}
	
		newradius = particles.get(0).calculateRadius(nbrParticles);
		
		// possible improvement: NOT the last, but the closest one, take it fro surround before removal		
		particles.get( particles.size()-1).radius = newradius;
		p = particles.get( particles.size()-1);
		
		out.print(2, "particle <"+index+"> removed...");
		
		//
		relocateParticles( p, -1) ;
	
		nbrParticles = particles.size() ;
		
		if (index>=0){
			eventsReceptors.get(0).onActionAccepted( RepulsionFieldEventsIntf._FIELDACTION_DEL, RepulsionFieldEventsIntf._FIELDSTATE_ACC, index) ;
		}
		
		return  ;
	}
	

	public int  addParticles( int x, int y){
		
		return  addParticles( new int[]{x}, new int[]{y});
	}
	
	private void createUnboundedParticle( int x, int y){
		
		Particle p;
		
		if (x<=-3){
			return  ;
		}
		
		p = new Particle(areaWidth, areaHeight, kRadiusFactor, nbrParticles, repulsion, sizefactor, 0);
		
		if ((x>0) && (y>0)){
			if (x>areaWidth) {x = (int) (areaWidth*0.98);}
			if (y>areaHeight){y = (int) (areaHeight*0.98);}
			p.x = x+2 ;
			p.y = y+2 ;
		}
		particles.getItems().add(p);
		
		
		surroundBuffers.introduceParticle(particles.size()-1);
		
		nbrParticles += 1; 
		
		// now we send a message to the facade about tis add-event, providing the particle 
		// just added as a template for updating the structures of the facade
		
		internalLayerEvents.onAddingParticle( this, p, particles.size()-1 );
	}
	
	private void  addDelayedParticles(){
	
		out.print(2,"committing delayed add for <"+particleAction.size("a")+"> particles ...");
		
		isReadyToUse = false ;
		nbrParticles = particles.size() ;
		relocateParticles( particles.get(particles.size()-1) , 1) ;
	}
	
	public int addParticles( int[] x, int[] y){
		
		// (new ParticleAction(this)).scheduleForRemoval(index);
		// this object then contains a waiting queue, the items of which then are removed all at once
		// inclusive complete rebuild of surround

		int count = x.length ;
		 
		
		if (count==0){return -3;}
		if (x.length!=y.length){return -4;}
		
		
		if ((fieldLayoutFrozen==false) || (surroundBuffers.getUpdating()>0)){
			// the field is still relocating the particles, which is a critical operation;
			// hence we have to buffer it: as soon as the field is stable again, 
			
			eventsReceptors.get(0).onActionAccepted( RepulsionFieldEventsIntf._FIELDACTION_ADD, 
													 RepulsionFieldEventsIntf._FIELDSTATE_DLY, 0) ;
												//   signalling delay...
			for (int i = 0; i < count; i++) {
				ActionDescriptor ad = new ActionDescriptor();
				ad.setActionCode( RepulsionFieldEventsIntf._FIELDACTION_ADD );
				ad.setX( x[i] );
				ad.setY( y[i] );
				ad.setIndex(0);
				
				createUnboundedParticle( x[i],y[i]);
				delay(20); // should be slower than the facade process
				particleAction.add(ad);
				// isReadyToUse = false ;
				out.print(2, "n="+particleAction.size("a")+"  new particles are waiting to be added...");
			}
			
			return -3; //
		}
		
		densityPerAcre = calculateDensity(particles.size(), count);
		adaptAreaSizeToDensity(count) ;
		
		if (particleAction.size("a")>0){
			addDelayedParticles();
			return particles.size();
		}
											out.print(2, "particle(s) (n="+count+") adding...");
		int firstNewIndex = particles.size();
		
		for (int i=0;i<count;i++){
			
			createUnboundedParticle( x[i],y[i]);

			
			isReadyToUse = false ;
			
			int cc=1;
			if (count>10)cc=25;
			changesInPopulation = changesInPopulation + cc;
			// releaseShakeIt(int intensity)
		}	
		if (count>10){
			// enforcing a restart
			// surroundBuffers.clear() ;
			// surroundBuffers = null;
		}
		
		 
		nbrParticles = particles.size() ;
		relocateParticles( particles.get(particles.size()-1) , 1) ; 
		
		eventsReceptors.get(0).onActionAccepted( RepulsionFieldEventsIntf._FIELDACTION_ADD, 
				 								 RepulsionFieldEventsIntf._FIELDSTATE_ACC, (particles.size()-1) ) ;

		return (firstNewIndex);
	}

	@Override
	public int addParticles(int count) {
		 
		return addParticles( -1,-1 );
	}

	@Override
	public int splitParticle(int index, ParticleDataHandlingIntf pdataHandler) {
	 
		int x,y;
		Particle particle ;
		
		
		if ((index<0) || (index>particles.size()-1)){
			return -3;
		}
		particle = particles.get(index);
		
		x = (int) (particle.x + ((Math.random()-0.5) * averageDistance/3) + (averageDistance/10));
		y = (int) (particle.y + ((Math.random()-0.5) * averageDistance/3) + (averageDistance/10)) ;
		
		// int n = particles.size();
		
		int pix = addParticles( x, y);
		
		// Particle blossomedParticle = particles.get(pix) ;
		
		// what to do with the data ? ...
		
		
		nbrParticles = particles.size() ;
		
		return pix;
	}


	
	@Override
	public String mergeParticles( int mergeTargetIndex, int[] indexes) {
		int ix;
		double d;
		Particle particle,p;
		Vector<Integer> ixes = new Vector<Integer>();
		
		// check distances
		for (int i=0;i<indexes.length;i++){
			ix = indexes[i];
			if ((ix>=0) && (ix<particles.size())){
				d = distanceBetweenParticles(mergeTargetIndex,ix );
				if (d< 1.3* averageDistance){
					ixes.add(ix) ;
				}
			}
		} // ->
		
		// -------------------------------------
		particle = particles.get(mergeTargetIndex) ;
		
		for (int i=0;i<ixes.size();i++){
			// combining data objects
			p = particles.get(ixes.get(i)) ;
			particle.mergeDataObjects( p.getIndexesOfAllDataObject() );
			
			particle.addParticleLinkages( p.getParticleLinkages()) ;
		} // i->
		
		for (int i=0;i<ixes.size();i++){
			deleteParticle(ixes.get(i));
		}
		nbrParticles = particles.size() ;
		
		return "";
	}
	
	@Override
	public String mergeParticles(int mergeTargetIndex, int swallowedIndex) {
		 
		return mergeParticles( mergeTargetIndex, new int[]{swallowedIndex}) ;
	}

	/**
	 * type=1 -> absolute, type=2 -> relative;
	 */
	@Override
	public void moveParticle( int particleIndex, int type, double xParam, double yParam) {
		double newX, newY;
		Particle particle;
		
		particle = particles.get(particleIndex) ;
		
		if (type>=2){
			newX = particle.x + xParam ;
			newY = particle.y + yParam ;
		} else{
			newX = xParam ;
			newY = yParam ;
		}
		
		particle.x = newX;
		particle.y = newY;
		
		// this respects the TORUS borderless topology 
		updateParticlesByDislocation();
		
		isReadyToUse = false ;
		
		particle.setFrozen(true) ;
		frozenParticles.add(particle) ;
		// we have to freeze this particle, otherwise the movement will be abolished by the forces in the hex grid
		// frozen state of particle will be released on new global frozen state
	}

	// obsolete
	private Object getParametersFromParticlesChangeQueue( String ids){
		
		Object obj=null;
		int[][] addParameters = null ;
		int[] delParameters = null ;
		int n;
		
		n = particleAction.size(ids);
		
		if ((n>0) && (ids.toLowerCase().startsWith("a"))){
		
			addParameters = new int[n][n];
			for (int i=0;i<particleAction.size();i++){
				
				
			}
			obj = addParameters;
		} 
		if ((n>0) && (ids.toLowerCase().startsWith("d"))){
			
			delParameters = new int[n];
			for (int i=0;i<n;i++){
				 
			}
			obj = delParameters;
		} 
		
		
		return obj ;
	}
	
	public void clearData(int index){
		
		Particle particle;
		
		particle = particles.get( index ) ;
		
		particle.clearIndexOfDataObject();
	}
	
	public void transferData( int fromParticleIndex, int toParticleIndex){
		
	}
	
	@Override
	public void insertDataPointer(int particleIndex, long dataPointer) {

		Particle particle;
		
		particle = particles.get(particleIndex) ;
		
		particle.setIndexOfDataObject( dataPointer );
	}


	@Override
	public void removeDataPointer(int particleIndex, long dataPointer) {
		Particle particle;
		
		particle = particles.get(particleIndex) ;
		
		particle.removeIndexOfDataObject( dataPointer ) ;
	}
	
	
	@Override
	public void releaseShakeIt(int intensity) {
		
		(new Shaker(particles,intensity)).go();
	}


	private void relocateParticles( Particle lastOfParticles, int direction){
		
		double chgHistoryCounterThreshold;
		double radius ;
		boolean shake_it; 
		
		if (lastOfParticles==null){
			if (particles.size()==0){
				return;
			}
			lastOfParticles = particles.get(particles.size()-1);
		}
		
		radius = lastOfParticles.radius ;
		
		if ((multiProc) &&(physicsProcess!=null)){ 
			physicsProcess.setBaseDataChanged(true);
		}
		
		 
		
		if (sizefactor==1.0){
			homogenizeSizeOfParticles( radius);
		}
		
		if (surroundBuffers!=null){
			surroundBuffers.setFieldFrozenMessage(false);
		}
		
											out.print(4, "particle adding, field is frozen : "+ fieldLayoutFrozen);
											
		if (fieldLayoutFrozen){
											out.print(4, "relocating particles, changesInPopulation = "+changesInPopulation);
			calculateDensity(particles.size(), particles.size());
			
			chgHistoryCounterThreshold = 100.0*(((double)nbrParticles/(double)(nbrParticles-changesInPopulation))-1.0);
			shake_it = ( (densityPerAcre>10) && 
					     ( (chgHistoryCounterThreshold > 2.5) || (changesInPopulation>11))); // changesInPopulation
			if (shake_it){
				// changesInPopulation will be increased in add/delete
				// shake_it = changesInPopulation > chgHistoryCounterThreshold;
			}
			if (shake_it ){
											out.print(3, "relocating particles, calling shaker...");
				(new Shaker(particles, 6)).go();
				 
			}else{
				setLayoutFrozenState(false);
											out.print(3, "particle adding after freezing, calling restart()..."); 
			    
				if (surroundBuffers!=null){
					surroundBuffers.setToPause(0) ;
				}
			    restart();
			}
			// it will return through the event ""
			// 
			// updateOnlyAroundChangeLocation=true;
			updateNeighborhood(); // we should only update a patch around the coordinates of the new particle
			
		} 
		// limitedNeighborhoodUpdate.populationSizeBefore = particles.size();
		
		relocationDurationLimiter.start( direction );
		
											out.print(3, "particle count : "+ particles.size()+"\n");
	}


	class Shaker implements Runnable{
		int intensity;
		Thread shakThrd;
	
		RestoreInitialValues restoreTask ; 
		Timer rivTimer ;
		
		
		public Shaker( Particles particles, int intensity){
			this.intensity = intensity;
			
			if (intensity<0)intensity=1;
			if (intensity>10)intensity=10;
			
			shakThrd = new Thread(this,"shakThrd") ;
			
			restoreTask = new RestoreInitialValues();
		    rivTimer = new Timer();
	
		}
		
		public Shaker go(){
			shakThrd.start() ;
			return this;
		}
	
		private void releaseShaking(){
			Particle p ;
			double ds,deloc ;
			
			if (fieldLayoutFrozen==false){
				return;
			}
			
			restoreInitialMobilityValues=true;
			initialEnergy = energy;
			initialDeceleration = deceleration ;
			initialRepulsionCorrection = repulsionCorrection ;
			initialDelayedOnsetMillis = delayedOnsetMillis;
			
			deloc = 0.05 + (intensity*0.11);
			
			
			for (int i=0;i<intensity;i++){
				mobilityIncrease();
			}
			
			
			for (int i=0;i<particles.size();i++){
				p = particles.get(i);
					ds = ((Math.random()-0.5)* p.radius* deloc  ) ; 
				p.x += ds  ;
				
					ds = ((Math.random()-0.5)* p.radius*0.6 ) ; 
				p.y += ds;
			}
			
			// fieldLayoutFrozen = false;
			delayedOnsetMillis = 20;
			 
		    rivTimer.schedule(restoreTask, 3100);
		    fieldLayoutFrozen=false ;
			relocateParticles(particles.get(0),0) ;
			 
			changesInPopulation=0;
		}
		
		@Override
		public void run() {
			releaseShaking();
			
		}
	}

	class RestoreInitialValues extends TimerTask {
		RestoreInitialValues() {
		}
	
		public void run() {
			energy = initialEnergy;
			deceleration = initialDeceleration;
			repulsionCorrection = initialRepulsionCorrection;
			delayedOnsetMillis = initialDelayedOnsetMillis ;
			
			out.print(4, "values restored") ;
			
			cancel();
		}
	
	}


	public void setDelayedOnset(int millis){
		delayedOnsetMillis = millis;
		initialDelayedOnsetMillis = delayedOnsetMillis ;
	}

	public void setColorSize(boolean differentsize, boolean differentcolor ){
		differentSize = differentsize; 
		differentColor = differentcolor;
	}
	
	public void useParallelProcesses( int flag ){
		
		multiProc = flag>=1 ;
		if (multiProc){
			if (fieldThrd==null){
				fieldThrd = new Thread(this,"RepulsionField") ;
				threadInitTime = System.currentTimeMillis() ;
				isStopped=false;
			}
		}else{
			if (fieldThrd==null){
				isStopped=true;
				int z=0;
				while ((isRunning) && (z<1000)){
					delay(1);
				}
			}
		}
	}
	
	public void setAdaptiveBehavior(boolean flag){
		adaptiveBehavior = flag;
	}
	
	public void mobilityDecrease() {
		 
		if (energy>0.8){
			energy = energy *0.95; 
		}
		if (deceleration>0.3){
			deceleration = deceleration *0.96; 
		}
		repulsionCorrection = repulsionCorrection* 1.03;
	}

	public void mobilityIncrease() {
		if (energy<8){
			energy = energy * 1.08; 
		}
		if (deceleration<0.9){
			deceleration = deceleration *1.04; 
		}
		repulsionCorrection = repulsionCorrection* 0.97;
	}

	@Override
	public void interrupt() {
		double q = -1;
		
		if (statisticsCollector!=null){
			q = statisticsCollector.getTrendStabilityValue();
		}
		freezeLayout(q);
	}


	public void stopFieldThread(){
		isStopped = true;
		if (fieldThrd==null){
			return;
		}
		while (isRunning){
			delay(1);
		}
		out.print(3, "FieldThr has been stopped.");
		fieldThrd=null;
		
	}


	@Override
	public void run() {
		boolean isCalculating=false;
		isStopped = false;
		
		isRunning = true;
		// out.print(2,"thread has been started...");
		
		try{
	
			while ((isRunning) && (isStopped==false)){
				
				if (isCalculating==false){
					isCalculating = true;
					
					// doPhysics();
					if (System.currentTimeMillis() - threadInitTime > delayedOnsetMillis ){
						   
							if ((threadcount>0) && (particles.size()>10)){
								if (fieldLayoutFrozen==false){
									physicsProcess.doParallelPhysics( particles, threadcount);
								}else{
									delay(100);
								}
								 
							}else{
								doPhysicsStandard();
							}
							
							if ((statisticsCollector!=null) && (statisticsCollector.isWaiting==true)){
								statisticsCollector.setWaiting(false);
								// statisticsCollector.explicitCall();
							} 
						 
					}
					 
					completed=true;
					isCalculating=false;
				}// NOT isCalculating?
				delay(1);
			}// ->
			
		}catch(Exception e){
			e.printStackTrace();
		}  
		isRunning = false;
		out.print(3,"thread has been stopped! *********");
	}

	private void restartSubProcesses(int mode){
		
		CollectStatistics statsCollector;
		if ((neighborhood == null) || (statisticsCollector==null)) {
			mode = 1;
		}
		if (mode>=1){
			isStopped=false;
			
			if (selectionBuffersActivated==true){
				surroundBuffers = new SurroundBuffers( this ,particles, out) ;
				
			} 
			 
			out.print(3, "neighborhood : strictly new start...)");
			if (neighborhood!=null){ neighborhood.stop();} ;
			neighborhood = new Neighborhood(neighborhoodBorderMode,surroundBuffers,out) ;
			neighborhood.setBorderMode( neighborhoodBorderMode );
			 
			statsCollector = new CollectStatistics(this); // rf); // ???
			
			boolean p1,p2;
			out.delay(10) ;

			p1 = neighborhood.isRunning() ;
			p2 = statsCollector.isRunning() ;
			
											out.print(3,"statisticsCollector running (a1): "+p1+" ,  neighborhood running: "+p2) ;
			
			p1 = (neighborhood.getThread() != null) && (neighborhood.getThread().isAlive());
			p2 = (statsCollector.getThread() != null) && (statsCollector.getThread().isAlive());
											out.print(4,"statisticsCollector running (a2): "+p2+" ,  neighborhood running: "+p1) ;
			statisticsCollector = statsCollector ;
		} else{
			out.print(3, "neighborhood : soft restart (2)...)");
			
			neighborhood.restartProcess();
			statisticsCollector.restart();
			
			out.print(3,"statisticsCollector running (b1):");
		}
		
		
		out.delay(50);
		if (statisticsCollector.isRunning()){
			statisticsCollector.isPhysicsProcessActivated=true;
		}
	}

	protected void restart(){
	
 					 
					 
		 
											out.print(3, "re-starting processes (1): fieldThrd ? "+(fieldThrd==null)+
														 ", isRunning: "+isRunning+"  multiProc: "+multiProc+"...") ;
		if ((fieldThrd==null) || (isRunning==false)){
											out.print(3, ">>>>>>  re-starting processes (2), in RepulsionField '"+this.name+"'  ...") ; 
			if (multiProc){ 	
				surroundBuffers.clear() ;
				
				/*
				if (delayedOnsetMillis>500)delayedOnsetMillis = 500;
				
				fieldThrd = new Thread(this,"RepulsionField") ;
				
				threadInitTime = System.currentTimeMillis() ;
			 
				restartSubProcesses(1);
				*/
				initialization();
				delay(300); 
				try{
					fieldThrd.start();
				}catch(Exception e){} 
				
				
				out.print(3, ">>>>>>  field calculation process (and helper processes) has been re-started...") ;
			} else {

				out.print(3, ">>>>>>  re-starting processes (3)...");
				
				restartSubProcesses(0);
				
			} // not: multiproc
		} // fieldThrd==null
		else{
			try{
				out.print(3, ">>>>>>  re-starting processes (4)...");
				restartSubProcesses(1);
				
				fieldThrd.start();
				threadInitTime = System.currentTimeMillis();
				
			}catch(Exception e){
				if (fieldThrd!=null){
					isRunning = false;
					isStopped=true;
					out.delay(50);
					fieldThrd=null;
				}
			}
			
		}
		
	}
	
	protected void start(){
		isStopped=false;
		
		if ((fieldThrd!=null) && (isRunning==false)){
			if (physicsProcess==null){
											out.print(3, "multi processing digester has been started ...");
				physicsProcess = new PhysicsDigester(this);
			} 
			if ((startInitiated==false) && (isRunning==false)){
				startInitiated=true; // for high refresh rates outside, this prevents threadstateexception
				 
				fieldThrd.start();
											out.print(3, "field layout process has been started ...");
				 
			}
			
		}
	}

	public String getName() {
		return name;
	}
	
	public void storeRepulsionField(){
	
		String filename ;
		filename = storage.getRFDataFilename();
		
		storeRepulsionField(filename);
	}
	
	public void storeRepulsionField(String filename){
		int r;
		
		storage.saveRfProperties( rfProperties );
		
		// this also will retrieve basic properties of the field itself, everything will be stored together 
		storage.getFieldStorageContainer().acquireParticles(particles);
		 
		r = storage.storeField(filename) ;
		
		if (r==0){
			out.print(2, "\nstoring field into file "+filename+" succeeded.\n");
		}else{
			out.printErr(2, "storing field into file "+filename+" failed (err="+r+").");
		}
	}	
	
	/** internal persistence mechanism */
	@Override
	public void importField() {
		String filename ;
		
		this.init( 1 );
		
		filename = storage.getRFDataFilename();
		
		performImportOfRepulsionField( filename );
		
	}
	 
	/** persistence using a dedicated user-based filename */
	public void importField(String filename) {
		
		if (filename.length()==0){
			importField();
			return;
		}
		
		int r = storage.loadField(filename);
		
		if (r==0){
			performImportOfRepulsionField( filename );
		}
		
	}
	
	
	private void performImportOfRepulsionField(String filename) {
		int r;
		 
		
		
		// loading the object fieldStorageContainer from default file, e.g. C:/Users/kwa/rf/config/~RepulsionFieldData-app-10000.dat
		r = storage.loadField(filename);
		
		if (r==0){	
			storage.setFieldReference(this);
			
			
			this.stopFieldThread();
			this.stopThreads() ;
			relocationDurationLimiter.supervisionIsRunning=false;
			if (surroundBuffers!=null){
				surroundBuffers.stop(); 
			}
			delay(80);

			if (multiProc){
				fieldThrd = new Thread(this,"RepulsionField") ;
				threadInitTime = System.currentTimeMillis() ;
			}

			// >>>>>>>>>> here the particles are actually imported into the RepulsionField object 
			particles.clear() ;
			storage.actualizeFieldByImport();

			// we need an early estimation of the average density
			// this.averageDistance = Math.sqrt( nbrParticles )/( (double)(areaWidth + areaHeight )/2.0) ;
			
			particleAction = new ParticleAction(rf);

			if (selectionBuffersActivated==true){
				surroundBuffers = new SurroundBuffers( this , particles, out) ;
				
			}
			 
			neighborhood = new Neighborhood( neighborhoodBorderMode,surroundBuffers,out ) ;
			neighborhood.setAreaSize( areaWidth, areaHeight, areaDepth);
			// neighborhood.setBorderMode( neighborhoodBorderMode );

			statisticsCollector = new CollectStatistics(this);
			statisticsCollector.setShowStatisticsInfo(false) ;
			surroundRetrieval = new SurroundRetrieval(this); 
			
			
			
			out.print(2, "field has been populated from storage (n="+particles.size()+"), now adopting and updating dynamic structures...");
			
			relocationDurationLimiter = new RelocationDurationLimiter(this) ;
			relocationDurationLimiter.start(0);
			
											out.print(2, "\nfield has been loaded from file "+filename+".\n");
			
			setDelayedOnset(100);
			
			releaseShakeIt(3);
		}else{
			init( nbrParticles );
			out.printErr(2, "importing field from default storage failed (err="+r+"), started with random configuration");
		}
	}
	
	
 
	
	/** importing just coordinates from a simple text file,</br></br> 
	 *  format, without header (and without quotation marks, of course): </br>
	 *  "x;y"  or "x [TAB] y" 
	 */
	@Override
	public void importCoordinates(String filename) {
		int r;
		r = storage.loadCoordinates(filename);
		
		if (r==0){
			
		}else{
			
		}
	}


	@Override
	public void exportCoordinates(String filename) {
		 
		
	}

	public double getDensityPerAcre() {
		return densityPerAcre;
	}

	public double getAverageDistance() {
		return averageDistance;
	}
	public void setAverageDistance(double averagedistance) {
		averageDistance = averagedistance;
	}
	
	
	public double getkRadiusFactor() {
		return kRadiusFactor;
	}

	public void setkRadiusFactor(double kRadiusFactor) {
		this.kRadiusFactor = kRadiusFactor;
	}
	public double getRepulsionCorrection() {
		return repulsionCorrection;
	}

	public double getSizefactor() {
		return sizefactor;
	}
	public void setSizefactor(double sizefactor) {
		this.sizefactor = sizefactor;
	}


	public void setCompleted(boolean flag) {
		completed = false;
	}


	public void setDelayedOnsetMillis(int delayedOnsetMillis) {
		this.delayedOnsetMillis = delayedOnsetMillis;
	}


	public int getAreaWidth() {
		return areaWidth;
	}


	public int getAreaHeight() {
		return areaHeight;
	}


	public int getAreaDepth() {
		return areaDepth;
	}


	public double getStability() {
		return stability;
	}

	public ActiveAreaIntf getActiveArea() {
		
		return null;
	}


	public RepulsionFieldProperties getRfProperties() {
		return rfProperties;
	}
	public CollectStatistics getStatisticsCollector() {
		return statisticsCollector;
	}


	public Neighborhood getNeighborhood() {
		return neighborhood;
	}

	public SurroundBuffers getSurroundBuffers() {
		return surroundBuffers;
	}


	public LimitedNeighborhoodUpdate getLimitedAreaUpdate() {
		return limitedAreaUpdate;
	}


	public void setPhysicsProcess(PhysicsDigester physicsProcess) {
		this.physicsProcess = physicsProcess;
	}

	public PhysicsDigester getPhysicsProcess() {
		return physicsProcess;
	}

	public SamplingField getSamplingField() {
		return samplingField;
	}

	
	public FieldSampler getSampler() {
		return sampler;
	}


	 
	public RepulsionFieldEventsIntf getEventsReceptor(int index) {
		RepulsionFieldEventsIntf rfe;
		if (index<0){
			index=0;
		}
		
		rfe = eventsReceptors.get(index);
		
		return rfe;
	}

	public Vector<RepulsionFieldEventsIntf> getEventsReceptor() {
		return eventsReceptors;
	}
	
	@Override
	public void processUpdate( Object source, int flag, String str) {
		// by interface CompletionEventMessageCallIntf
		// which is server by PhysicsDigestor == organizer of multithreaded calculations
		if (flag==0){
			updateFinished = true;
			
			passes++;
			if ((stepsLimit>0) && (passes>stepsLimit)){
				interrupt() ;
			}
		}
	}


	@Override
	public void setDynamics( int nbrParticles, double energy, double repulsion, double deceleration) {
		 
		this.energy = energy; 
		this.repulsion = repulsion ;
		this.deceleration = deceleration ;
		this.nbrParticles = nbrParticles;
	}
	
	
	@Override
	public ParticlesIntf getParticles() {
		 
		return (ParticlesIntf) particles;
	}


	public int getNumberOfParticles() {
		return nbrParticles;
	}


	public void setNumberOfParticles(int nbrParticles) {
		this.nbrParticles = nbrParticles;
	}
	
	@Override
	public void setBorderMode(int bordermode) {
		
		neighborhoodBorderMode = bordermode;
		
		if (neighborhood!=null){
			neighborhood.setBorderMode(bordermode) ;
		}else{
			
		}
		
	}
	
	public int getBorderMode() {
		
		return neighborhoodBorderMode ;
	}
	
	
	@Override
	public void setAreaSize(int width, int height) {
		
		if (areaSizeAuto>0){
			if ((width*height)<areaWidth*areaHeight){
				return;
			}
		}
		
		
		if ((areaWidth != width) || (areaHeight != height)){
		
			informParticlesAboutArea(areaWidth,areaHeight);
			// areaWidth0, areaHeight0
			
			areaChangedSize=true;
		}
	
		areaWidth = width ; 
		areaHeight= height ;
		if (height==0){
			out.print(2,"setAreaSize(), height == 0 ???");
		}
	}


	public int[] getAreaSize(){
		int[] as = new int[2] ;
		
		as[0] = areaWidth;
		as[1] = areaHeight;
		
		return as;
	}
	

	public void setAreaHeight(int height) {
		areaHeight = height;
	}
	
	public void setAreaWidth(int width) {
		areaWidth = width;
	}

	@Override
	public void setDefaultDensity( double dvalue ) {
		 
		defaultDensity = dvalue; 
		
		if (defaultDensity>25){
			defaultDensity = 25.0;
		}
		if (defaultDensity<5){
			defaultDensity = 5.0;
		} 
	}
	
	@Override
	public void setAreaSizeAuto(int nodecounttarget) {
		double  aspectRatio = 1.666;
		int w,h ;
		
		autoSizeNodeCounts = nodecounttarget;
		
		if (defaultDensity<0){
			return;
		}
		
		averageDistance = 30;
		
		w = (int)(Math.sqrt(autoSizeNodeCounts)*((1.0+aspectRatio)/2.0) * 30.0) ;
		h = (int)(Math.sqrt(autoSizeNodeCounts)/((1.0+aspectRatio)/2.0) * 30.0) ;
		
		// double d = (100*100)*autoSizeNodeCounts/(w*h);
		
		areaSizeAuto = w*h;
		areaWidth = w ; 
		areaHeight = h ;
		
		
		adaptAreaSizeToDensity( -autoSizeNodeCounts );
		
		areaSizeAuto= areaWidth*areaHeight ;  
		 
	}
	
	@Override
	public void setAreaSizeMin() {
		areaHeight = 300;
		areaWidth  = 400;
	}
	
	public void setFreezingAllowed(boolean freezingAllowed) {
		this.freezingAllowed = freezingAllowed;
	}


	// ------------------------------------------------------------------------
	
	@Override
	public void setInitialLayoutMode(int initialLayout ) {
		initialLayoutMode = initialLayout;
	}

	public int getInitialLayoutMode( ) {
		return initialLayoutMode ;
	}

	public double getRepulsion() {
		return repulsion;
	}

	public void setRepulsion(double repulsion) {
		this.repulsion = repulsion;
	}

	public double getDeceleration() {
		return deceleration;
	}

	public void setDeceleration(double deceleration) {
		this.deceleration = deceleration;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public int getDelayedOnsetMillis() {
		return delayedOnsetMillis;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getVersionStr() {
		 
		return versionStr;
	}
	public int getColormode() {
		return colormode;
	}
	public void setColormode(int colormode) {
		this.colormode = colormode;
	}
	public int getStepsLimit() {
		return stepsLimit;
	}
	
	@Override
	public void setStepsLimit(int steps) {
		 
		stepsLimit = steps ;
		passes=0 ;
	}


	public int getSelectionSize() {
		return selectionSize;
	}


	public void setSelectionSize(int n) {
		this.selectionSize = n;
		
		if (hexagonSizedSelection){
			selectionSize = adaptSelectionSize( selectionSize);
		}
	}


	public boolean isHexagonSizedSelection() {
		return hexagonSizedSelection;
	}
	public void setHexagonSizedSelection(boolean hexagonSizedSelection) {
		this.hexagonSizedSelection = hexagonSizedSelection;
	}

	
	
	public boolean isCompleted() {
		
		boolean rB = completed;
		
		if (multiProc==false){
			rB=true;
		}
		return rB;
	}


	public boolean isUpdateFinished() {
		return updateFinished;
	}


	public boolean isMultiProc() {
		return multiProc;
	}
	public void setMultiProc( boolean flag) {
		multiProc = flag;
	}
	@Override
	public boolean isReadyToUse() {
		 
		return isReadyToUse;
	}
	@Override
	public int getSurroundBuffersUpdateCounter() {
		// not used here -> splitting interfaces into a hierarchy
		return -1;
	}
	
	@Override
	public GraphParticlesIntf getGraphParticles() {
		
		// returns an abridged version of the full particles, 
		// provides access only to graph information (x,y,z, colors)
		return (GraphParticlesIntf) particles;
	}
	
	public boolean isFreezingAllowed() {
		return freezingAllowed;
	}


	public boolean isFieldLayoutFrozen() {
		return fieldLayoutFrozen;
	}


	public boolean isSelectionBuffersActivated() {
		return selectionBuffersActivated;
	}


	public void setSelectionBuffersActivated(boolean selectionBuffersActivated) {
		this.selectionBuffersActivated = selectionBuffersActivated;
	}


	public boolean isUseOfSamplesForStatistics() {
		return useOfSamplesForStatistics;
	}


	public void setUseOfSamplesForStatistics(boolean useOfSamplesForStatistics) {
		this.useOfSamplesForStatistics = useOfSamplesForStatistics;
	}


	public int getNextThreadCount() {
		return nextThreadCount;
	}


	public int getThreadcount() { 
		return threadcount;
	}


	public void setThreadcount(int threadcount) {
		this.threadcount = threadcount;
	}


	public boolean isRunning() {
		return isRunning;
	}


	public boolean isStartInitiated() {
		return startInitiated;
	}


	public boolean isStopped() {
		return isStopped;
	}


	public PrintLog getOut() {
		return out;
	}


	private double[] getRegularPosition( int index ){
		double[] pc = new double[4];
		double aspectratio ,sqs,r1,ra,x,y,ymax;
		int a,b,da,db ;
		
		aspectratio = (double)areaWidth/(double)areaHeight; // e,g. 1.6
		
		sqs = Math.round(Math.sqrt( nbrParticles )); if((sqs-1)>nbrParticles)sqs-=1;		
		a =  (int) Math.round(sqs);
		b =  (int) Math.round(sqs);
		r1 = (double)a/(double)b / aspectratio; 
		boolean done=false;
		int z=0;
		while ((done==false)&&(z<sqs)){
			r1 = (double)(a*1.0)/(double)(b*1.0);
			ra= r1/ aspectratio; da=0;db=0;
			if (ra>1){
				if (nbrParticles>12)da=-1; 
				db=1;
			}
			if (ra<1){
				da=+1; 
				if (nbrParticles>12)db=-1;
			}
			a=a+da;
			b=b+db;
			
			if (a*b<nbrParticles){
				if (da<0)da=0;
				if (db<0)db=0;
				a=a+da;
				b=b+db;
			}else{

				boolean hb = ((nbrParticles<17) && (Math.abs(1-ra)<0.4)) || 
				             (Math.abs(1-ra)<0.1);
				
				if ((hb) ||
					( (a*b==nbrParticles)  )){
					done=true;
				}else{
				 
				}
			}
			z++;
		}
		if (a*b!=nbrParticles){ a =a+1; b=b-1; }
		x = (double)(index % a) * (double)((double)areaWidth/(double)a) + (double)((double)areaWidth/(double)(a*2));
		y =    ((double)(index / (a)) * ((double)areaHeight/(double)b)) + (double)((double)areaHeight/(double)(b*2));
		ymax = ((double)(nbrParticles / (a)) * ((double)areaHeight/(double)b)) + (double)((double)areaHeight/(double)(b*2));
		
if (index>nbrParticles-5){
	z=0;
}
		if (y==ymax){
			int xlast = (nbrParticles % a) * (areaWidth/a) + (areaWidth/(a*2));
			double xc =  (double)areaWidth/(double)xlast ;
			x = (x*xc);
		}
		
		pc[0]=x	;	
		pc[1]=y	;
		pc[2]=a	;	
		pc[3]=b	;
		
		return pc;
	}

	private void adaptAreaSizeToDensity(){
	
		adaptAreaSizeToDensity( 0);
	}

	private void adaptAreaSizeToDensity( int count ){ 
		double avgDens = 0,aspectRatio,threshold  ;
		int aW,aH,   daW,daH, pc;
	 
		avgDens = densityPerAcre;
		
		if (particles.size()<28){
			return;
		}
		pc = 0 ;
		
		if (particles!=null){
			pc = particles.size();
		}
		
		if (areaHeight==0){
			return;
		}
		 
		if ((count>=0) && ((maxDensityDeviationPercent<=0.0) && ((averageDistance>15.0)|| (densityPerAcre<32)))){ 
			return;
		}
		count = Math.abs(count) ;
		
		aspectRatio = (double)areaWidth/ (double)areaHeight; 
		aW = areaWidth  ;
		aH = areaHeight;
		int psz = (int)( pc *0.03)+ count ;
		
		boolean tooSmall = true;
		
		out.print(4, "densityPerAcre "+Math.round(densityPerAcre*100.0)/100.0);
		if (defaultThresholdForDensity<10){
			defaultThresholdForDensity=10;
		}
		threshold = defaultThresholdForDensity ; // usually 40.0
		
		if (currentBaselineDensity<0){
			avgDens = calculateDensity( (pc+count), aW,aH,psz); 
			currentBaselineDensity = avgDens;
		}
		
		if (maxDensityDeviationPercent >0.0){
			avgDens = calculateDensity( (pc+count), aW,aH,psz); 
			double actualDeviationPercent = 100.0*(double)(avgDens/currentBaselineDensity) ;
			
			if (Math.abs(actualDeviationPercent-100.0)<maxDensityDeviationPercent){
				return;
			}
			threshold = currentBaselineDensity * (1.0+maxDensityDeviationPercent);
		}
		/*
		double _radius ;
		if ((particles!=null) && (particles.size()>0)){
			_radius = particles.get(0).radius ;
		}else{
			_radius = 60.0 ;
		}
		*/
		
		int z=0; 
		while ((tooSmall) && (z<particles.size()/3)){
			// int area = aW*aH;
			avgDens = calculateDensity( (pc+count),aW,aH,psz); 
			if (avgDens> threshold){
				daW = (int)Math.round( ((double)aW*1.0) *0.02 );
				daH = (int)Math.round( ((double)aH*1.0) *0.02 / aspectRatio);
				daW = Math.max( daW, 10);
				daH = Math.max( daH, 10);
				aW = aW + daW;
				aH = aH + daH;
				/*
				if (aW < _radius/3.0 * Math.sqrt(particles.size())){
					aW = areaWidth;
					aH = (int)((double)areaWidth/aspectRatio) ;
				}
				*/
			}else{
				tooSmall=false;
			}
			z++;
		}

		// care about aspect ratio
		boolean arMatch=false;
		double ar ,ri,rx,dar ;
		ar=0; z=0;
		while ((arMatch==false) && (z<particles.size()/3)){
			ar = (double)aW/(double)aH;
			aspectRatio = (double)areaWidth / (double)areaHeight;
			
			ri = Math.min(ar,aspectRatio);
			rx = Math.max(ar,aspectRatio);
			
			dar = (ri/rx);
			if ((dar>0.01) && (dar<0.99)){
				if (ri==ar){
					aW=aW+5; aH=aH-5;
				}else{
					aW=aW-5; aH=aH+5;
				}
				avgDens = calculateDensity( (pc+count), aW,aH,psz);
				if (avgDens>threshold){
					aW=aW+5; aH=aH+5;
				}
			}else{
				arMatch=true;
			}
			z++;
		} // ->
		if (densityPerAcre<0){
			densityPerAcre = avgDens;
		}
		changesInPopulation = (int) (pc*0.1);
		areaWidth = aW ;
		areaHeight = aH ;
		 
		eventsReceptors.get(0).onAreaSizeChanged( this, aW, aH);
		out.print(3, "Density has been adjusted: N = "+pc+" , density = "+densityPerAcre+" , average of distance "+ Math.round(averageDistance*100.0)/100.0) ;
				
	}
	
	@Override
	public void setMaxDensityDeviationPercent(double value) {
		
		if ((updateCounter>0) && (currentBaselineDensity>0)){
			currentBaselineDensity = this.densityPerAcre ;
		}
		maxDensityDeviationPercent = value ;
		if (updateCounter>0){
			adaptAreaSizeToDensity();
		}
	}
	
	private double calculateDensity( int particlesCount, int expectedIncrease){
		return  calculateDensity( particlesCount, areaWidth, areaHeight, expectedIncrease);
	}
	
	private double calculateDensity( int particlesCount, int areaWidth, int areaHeight, int expectedIncrease){
		
		double _densityPerAcre = Math.round( ((10000.00*(double)((double)(particlesCount)+expectedIncrease)/((double)areaWidth* (double)areaHeight))) *10000.0)/10000.0;
		
		return _densityPerAcre;
	}
    
	private double distanceBetweenParticles( int index1, int index2){
		double x1,y1,x2,y2;
		
		x1 = particles.get(index1).x ;
		x2 = particles.get(index1).y ;
		y1 = particles.get(index2).x ;
		y2 = particles.get(index2).y ;
		
		double d = Math.sqrt( (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) );
		return d;
	}
	
	private void adaptMobilityCorrectionFactors(double stability){
		
		if (adaptiveBehavior==false){
			return;
		}
		if (stability < 0.05) {
			if (energy > 0.8) {
				energy = energy * 0.85;
			}
			if (deceleration > 0.3) {
				deceleration = deceleration * 0.85;
			}
			repulsionCorrection = repulsionCorrection * 0.95;
		}
		if (stability > 0.25) {
			if (energy<2.3){
				energy = energy * 1.1; 
			}
			if (deceleration<1.1){
				deceleration = deceleration *1.05; 
			}
			repulsionCorrection = repulsionCorrection* 1.05;
		} else {
			if (stability > 0.12) {
				deceleration = 1.0 ; 
				repulsion = 1.0 ; 
				energy = 1.0 ;  
			}
		}
		
	}


	private void homogenizeSizeOfParticles( double newRadius){
		
		for(int i=0;i<particles.size();i++){
			particles.get(i).radius = newRadius;
		}
	}
	
	private void informParticlesAboutArea( int w, int h){
		Particle p;
		
		if (particles==null){
			return;
		}
 		for(int i=0;i<particles.size();i++){
			p = particles.get(i) ;
			p.setHeight(h);
			p.setWidth(w);
		}
	}
	
	
	protected void freezeLayout(double stability  ){
		freezeLayout(stability, 0);
	}
	 
	protected void freezeLayout(double stability, int enforced ){
		
		boolean freezeNow ;
		
		
		if ((enforced==0) && (freezingAllowed==false)){ 
			return;
		}
		if (fieldLayoutFrozen){
			out.print(3,"calling freezing, but layout has already been frozen...");
			return;
		}
		
		if(stability<=0.01)
		{
			out.print(3,"...about to freeze if stable, stability = "+stability);
		}
		
		freezeNow = (stability<=0.0005) || (enforced>=1);
		
		if (particles.size()<15){
			freezeNow = stability<=0.00002;
		}
		if (freezeNow ){ // && (passes>10)
			out.print(3, "\nlayout for field <"+this.name+"> has been frozen (n="+particles.size()+").");
			
			stabilizationCounter++;
			setLayoutFrozenState(true); // fieldLayoutFrozen = true; -> NEVER set it directly, we have to know whether it happens
			 
			if ((currentBaselineDensity<0) && (densityPerAcre>3.0)){
				currentBaselineDensity = this.densityPerAcre ;
			}
			// out.print(2, "stabz # "+stabilizationCounter+" , densityPerAcre "+Math.round(densityPerAcre*100.0)/100.0);
			
			updateFinished = true;
			delayedOnsetMillis = 50;
			
			if ((fieldLayoutFrozen) && (eventsReceptors.get(0)!=null)){
				// we need a threaded decoupling, if the callback call fails for some reason
				// thus we perform the callback in its own class
				(new CallbackServer()).go(0,0);	
			}
			
			stopThreads();
			
			if (restoreInitialMobilityValues){
				// true only after trembling / shaking
				energy = initialEnergy;
				deceleration = initialDeceleration;
				repulsionCorrection = initialRepulsionCorrection;
				// delayedOnsetMillis = initialDelayedOnsetMillis ;
			}
			
			if (multipleDeletions>0){
				surroundBuffers.setFieldFrozenMessage(false); out.delay(80);
				out.print(3, "re-building surroundBuffers ...");
				if (selectionBuffersActivated==true){
					surroundBuffers = new SurroundBuffers( this ,particles, out) ;
				}
			}
			multipleDeletions=0;
			// finally, we update the SurroundBuffers for all particles in a background process
			// while the buffer is essentialy independent and not contained as an object in the particles,
			// each particle contains a pointer to its buffer, 
			// the buffer itself knows whether it is available or not., thus, we can profit even if only part
			// of the whole population has an updated buffer 
			
			// the surroundbuffer objects also maintain the last known frozen coordinate,  
			if (surroundBuffers!=null){
				surroundBuffers.start(); // will start only if it is not yet running
				surroundBuffers.setFieldFrozenMessage(true); // will release the brake ...
			}else{
				if (selectionBuffersActivated==true){
					surroundBuffers = new SurroundBuffers( this ,particles, out) ;

				}
			}
			
		} // freezeNow ?
	
		
	}

	private void setLayoutFrozenState(boolean flag){
		
		// boolean previousState = fieldLayoutFrozen; 
		
		fieldLayoutFrozen = flag; 
		
		for (int i=0;i<frozenParticles.size();i++){
			frozenParticles.get(i).setFrozen(false);
		}
		frozenParticles.clear();
	}
	

	
	void updateNeighborhood( ){ 
		 
		if (neighborhood==null){
			neighborhood = new Neighborhood(neighborhoodBorderMode,surroundBuffers,out) ;
			// neighborhood.setBorderMode( neighborhoodBorderMode );
		}
		
		
			for (int i=0;i<particles.size();i++){
				 
				neighborhood.update(i, particles.get(i).x,particles.get(i).y, particles.get(i).radius);
			}
			neighborhood.finalizeQ();
		
	}

	private int adaptSelectionSize( int cSelSize) {
		int newSelectionSize = cSelSize;
		
		int[] selsizes,dsizes  ;
		int layerCount,p,n4 ;
		
		
		layerCount = getLayerCountOfHexPattern(selectionSize);
		newSelectionSize = calculatePlateletsCountInHexPattern(layerCount);
		
		selsizes = new int[3];
		dsizes = new int[3];
		n4 = calculatePlateletsCountInHexPattern(3);
		if (layerCount>=3){
			
			selsizes[0] = calculatePlateletsCountInHexPattern(layerCount-1);
			selsizes[1] = calculatePlateletsCountInHexPattern(layerCount );
			selsizes[2] = calculatePlateletsCountInHexPattern(layerCount+1);
				
			dsizes[0] = Math.abs( selsizes[0]  - cSelSize);
			dsizes[1] = Math.abs( selsizes[1]  - cSelSize);
			dsizes[2] = Math.abs( selsizes[2]  - cSelSize);
			
			
			p = arrutil.arrayMinPos(dsizes) ;
			double pp = ((double)dsizes[p]/(double)selsizes[p])*100.0;
			if (pp<7.5){
				newSelectionSize = selsizes[p];
			}else{
				if (p>selsizes.length-1)p=selsizes.length-1;
				if (selsizes[p]<140){
					newSelectionSize = selsizes[p+1];
				}else{
					newSelectionSize = cSelSize;
				}
			}
		} // layerCount>=2
		
		n4=n4+0;
		return newSelectionSize;
	}


	// later: put this to NumUtilities
	public int getLayerCountOfHexPattern( int platelets){
		
		int n=0, sp=1, add;
		
		while (sp<platelets){
			n++;
			add = n*6 ;
			sp = sp+add;
		}
		
		return n;
	}


	public int calculatePlateletsCountInHexPattern( int layers){
		int n=0, sp=1, add;
		
		while (n<layers){
			n++;
			add = n*6 ;
			sp = sp+add;
		}
		
		return sp;
	}


	class StopThread implements Runnable{
		Thread sT;
		Thread xt;
		Stoppable xxT;
		
		public StopThread(){
			sT = new Thread (this);
		}
		public void DoIt(Stoppable t){
			xxT = t;
			sT.start();
		}
		private void stopThread(){
			xxT.stop();
		}
		@Override
		public void run() {
			stopThread();
		}
		
	}
	private void stopThreads() {
		 
		if (statisticsCollector!=null){
			statisticsCollector.isPhysicsProcessActivated=false;
		
			(new StopThread()).DoIt(statisticsCollector);
		}
		// (new StopThread()).DoIt(neighborhood);
		
		statisticsCollector = null;
		// neighborhood = null;
		
		isStopped = true;
		
		this.stopFieldThread();
		 
		System.gc();
		
	}


	private void threadcountController(double stability){
		if ((stability<0.05) || (particles.size()<50)) {
			threadcount=1;
			 
		}else{
			if (stability<0.1){
				nextThreadCount = 3;
			}
			if (stability<0.1){
				nextThreadCount = 2;
			}
			if (stability<0.05){
				nextThreadCount = 1;
			}
			
			if (stability>0.25){
				nextThreadCount = 5;
			}
			
		}
		nextThreadCount=5;
	}


	@SuppressWarnings("static-access")
	private void delay( int millis){
		try{
			Thread.currentThread().sleep(millis);
		}catch(Exception e){}
	}

	class CallbackServer implements Runnable{

		int flagvalue, eventindex;
		Thread cbsThrd;
		
		public CallbackServer(){
			out.print(3,"...serving the callback (1)...");
			cbsThrd = new Thread(this,"cbsThrd") ;
		}
		public void go(int fv, int eventix) {
			flagvalue = fv;
			eventindex = eventix;
			cbsThrd.start();
		}
 
		private void send(){
			out.print(3,"...serving the callback (2)...");
			System.out.println();
			
			if (eventsReceptors == null){
				return;
			}
			for (int i=0;i<eventsReceptors.size();i++){
				if (eventindex==0){
					
					eventsReceptors.get(i).onLayoutCompleted(flagvalue);
					if (selectionBuffersActivated == true) {
						if (i==0)
						out.print(2, "...selection buffer started to update, please wait...");	
					}
				} // eventindex=0
				if (eventindex==1){
					
					eventsReceptors.get(i).onCalculationsCompleted();
				}
			}
		}
		@Override
		public void run() {
			send();
		}
		
		
	}


	// ==== RepulsionFieldEventsIntf ==========================================
	 
	public void createRelocationDurationLimiter(){
		// called only by mirror instatiation in FieldMirror
		relocationDurationLimiter = new RelocationDurationLimiter(this);
	}
	public void startRelocationDurationLimiter() {
		
		relocationDurationLimiter.start(0) ;
	}
	public void setRelocationDurationLimiter( RelocationDurationLimiter rdl) {
		this.relocationDurationLimiter = rdl;
	}
	public void setLimitedAreaUpdate( LimitedNeighborhoodUpdate limnup) {
		// called only by mirror instatiation in FieldMirror
		limitedAreaUpdate = limnup;
	}
	public void setParticleAction(ParticleAction pAction) {
		// called only by mirror instatiation in FieldMirror
		particleAction = pAction;
	}
	
	
	public RelocationDurationLimiter getRelocationDurationLimiter() {
		return relocationDurationLimiter;
	}
	public void setFieldIsRandom(boolean flag) {
		
		fieldIsRandom = flag;
	}
	
 
	
	// ========================================================================

	
} // class RepulsionField


class Neighbor{
	
	int index;
	Particle particle;
	double distance ;
	
	public Neighbor( int ix, Particle particle, double distance ){
		this.index = ix ;
		this.particle = particle ;
		this.distance = distance ;
	}
}

 

/**
 * this class runs a thread to supervise schedulaed timers.
 * Timers can overlap, where each timer sits on a dedicated slot in a FiFo list,
 * from where the timers are removed upon completion
 * 
 */
class RelocationDurationLimiter implements Runnable{

	RepulsionFieldCore parent;
	// ---- about the data -------------------------
	
	Vector<FieldPoint> changedCoordinates = new Vector<FieldPoint>(); 
	
	// ---- about the timers -----------------------
	
	long delay = 16000; 
	
	boolean timerIsRunning=false, timerHasBeenStopping=false;
	boolean supervisionIsRunning=false;
	boolean shortenedDelay = false;
	
	long lastScheduling;
	Thread rdlThrd;
	
	// we build a FiFo list for overlapping timers
	Vector<Timer> timers = new Vector<Timer>() ;
	
	public RelocationDurationLimiter( RepulsionFieldCore parent){
		this.parent = parent;
		rdlThrd = new Thread(this,"rdlThrd");
		rdlThrd.start();
		
		if (parent.relocationInterruptDelay>3000){
			delay = parent.relocationInterruptDelay; 
		}
	}
	
	public void start( int d ){
		shortenedDelay = d<0;
		init(null);
	}
	public void start( FieldPoint changeLocation){
		init(changeLocation);
	}
	private void init(FieldPoint changeLocation){
											parent.out.print(3, "starting relocaton interrupt timer (#"+timers.size()+")...");
		if (changeLocation!=null){
			// changedCoordinates.add(changeLocation);
			// not here...
		}
		
		if (System.currentTimeMillis() - lastScheduling < 2000){
			return;
		}
		
		timers.add( new Timer() );              
	 
	 
		lastScheduling = System.currentTimeMillis();
		timerIsRunning=true;
		timers.get(timers.size()-1).schedule(new DelayedReFreezeTask(parent.name), delay);

		parent.out.print(3, "A new relocation interrupt-timer (n="+timers.size()+") has been scheduled for <"+parent.name+">...");

	}

	class DelayedReFreezeTask extends TimerTask {

		
	 
		DelayedReFreezeTask(String objectName) {
			
		}

		/**
		 * When the timer executes, this code is run.
		 */
		public void run() {
			/* Get current date/time and format it for output
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
			String current_time = format.format(date);
			*/

			// freeze only if this is the last of all overlapping timers
			if (timers.size()==1){
				parent.freezeLayout(parent.stability,1);
			}
			timerIsRunning=false;
			
			parent.out.print(4,"relocation timer interrupted the update procedure (<"+parent.name+">).") ;
			timerHasBeenStopping = true;
		}
	}

	@Override
	public void run() {
		parent.out.print(3,"relocation timer supervision is running...");
		try{
			supervisionIsRunning=true;
			while (supervisionIsRunning){ 
				parent.out.delay(100);
				
				if (timerIsRunning==false){
					if (timerHasBeenStopping){
						timerHasBeenStopping = false;
						// remove the oldest one
						parent.out.print(4,"now removing the oldest relocation timer for <"+parent.name+">...");
						if (timers.size()>0){
							timers.set(0,null);
							timers.remove(0);
						}
						parent.out.print(4,"timers running : n="+timers.size());
					}
				}
				parent.out.delay(5);
			}

		}catch(Exception e){}
		parent.out.print(3,"relocation timer supervision has been stopping.");
	}
}


/*
 * 


	@SuppressWarnings("unused")
	private void doMagnetosphere() {
		float force = 1; // attraction/repulsion-coefficient
	
		int numparticles = 9; // default particles
		float dragfactor = 1; // stop speeds from accumumulating too much
		int selectedindex = -1;
	
		for (int i = 0; i < nbrParticles; i++) {
	
			float xaccel = 0, yaccel = 0;
	
			for (int j = 0; j < nbrParticles; j++) {
	
				float ijdist = (float) neighborhood.distance( particles.get(i).x, particles.get(i).y,
															  particles.get(j).x, particles.get(j).y);
				float theta = (float) Math.atan2( particles.get(i).y - particles.get(j).y,
												  particles.get(i).x - particles.get(j).x);
	
				if (ijdist > 20){ // attractive or repulsive forces depending on
									// charges
				
					xaccel += particles.get(i).charge * particles.get(j).charge * (force / (ijdist * ijdist)) * Math.cos(theta);
					yaccel += particles.get(i).charge * particles.get(j).charge * (force / (ijdist * ijdist)) * Math.sin(theta);
				}
	
				else if (ijdist > 0.1f) {
					xaccel += (force / (Math.pow(ijdist, 4))) * Math.cos(theta);
					yaccel += (force / (Math.pow(ijdist, 4))) * Math.sin(theta);
				}
	
				// particles.get(i).xspeed+=xaccel;
				// particles.get(i).yspeed+=yaccel;
			}
			/*
			 * if(particles.get(i).affected) { particles.get(i).move(); }
			 * particles.get(i).render();
			  
		}
	
	}
	
		=========================================================================
		
			private void selectSurround( int[] particleIndexes, boolean autoselect){
		int ix;

		if (particleIndexes==null){
			return;
		}
		if (autoselect){ // do this by multi-digester
			for (int i=0;i<particles.size();i++){
				particles.get(i).setSelected(0) ;
				particles.get(i).resetColor();
			}
			for (int i=0;i<particleIndexes.length;i++){
				ix  = particleIndexes[i] ;
				
				if ((ix>=0) && (ix<particles.size())){ 
					particles.get(ix).setSelected(1) ;
					particles.get(ix).setColor( selectionColor[0],selectionColor[1],selectionColor[2]) ;
				}
			} // i->
		} // autoselect ?
		
		 
	}

 * 
 * 
 */
 