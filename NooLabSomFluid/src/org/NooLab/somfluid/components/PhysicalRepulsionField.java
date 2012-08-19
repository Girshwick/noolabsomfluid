package org.NooLab.somfluid.components;

import org.NooLab.field.interfaces.PhysicalGridFieldIntf;
import org.NooLab.field.interfaces.RepulsionFieldEventsIntf;
import org.NooLab.field.repulsive.RepulsionField;
import org.NooLab.field.repulsive.RepulsionFieldFactory;
import org.NooLab.field.repulsive.components.Neighborhood;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.field.repulsive.intf.particles.ParticlesIntf;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.utilities.logging.LogControl;
import org.NooLab.utilities.logging.PrintLog;




/**    
 * 
 * this factory creates, hosts and delivers a RepulsionField 
 * it is the docking site for external dynamic grids
 * 
 * its sister class
 * 
 * needs to implement :
 * otherwise this class cast exception will occur
 * Exception in thread "SomModuleInstance" java.lang.ClassCastException: org.NooLab.somfluid.PhysicalFieldFactory cannot be cast to org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf

 */
public class PhysicalRepulsionField extends ExternalGridFieldAbstract{	
	
	RepulsionFieldFactory rfFactory;
	RepulsionField repulsionField; 

	RepulsionFieldEventsIntf eventSink = null;
	
	double deceleration = 1.0 ; 
	double repulsion = 1.0 ; 
	double energy = 1.0 ;  
	
	boolean initComplete = false;
	
	
	// ========================================================================
	public PhysicalRepulsionField( 	SomFluidFactory sfFactory, 
									RepulsionFieldEventsIntf eventsink, 
									int nbrparticles){
		
		eventSink = eventsink;
		nbrParticles = nbrparticles;
		
		rfFactory = new RepulsionFieldFactory();
		repulsionField = rfFactory.getRepulsionField( ) ;
		
		/*
		 // for testing, 100 = 100ms delay between calls, pres +/. to ac-/decelerate
			rfFactory = new RepulsionFieldFactory("test:RQ=1000", 1); // , LogControl.Level [1 lowest output];
		 */
		
		
		
		createPhysicalField( sfFactory, eventSink, nbrParticles );
	}
	
	// ========================================================================
	
	public void defineEventMessagingEndpoint( RepulsionFieldEventsIntf eventSink){
		if (repulsionField!=null){
			repulsionField.registerEventMessaging( eventSink );
		}
	}
	
	
	public RepulsionFieldIntf createPhysicalField( SomFluidFactory sfFactory,  RepulsionFieldEventsIntf eventSink,int nbrparticles ){ // ,
	 
		int runRequestTester=0,  selszLimit ;
		
		// TODO: properties offered by static create(s) and interface
		//       which then are part of SomFluidProperties 
		
		if (runRequestTester>0){
			// for testing, 100 = 100ms delay between calls, pres +/. to ac-/decelerate
			rfFactory = new RepulsionFieldFactory("test:RQ=1000", 1); // , LogControl.Level);
			
			int pp=0;
			if (sfFactory.getSfProperties().isMultithreadedProcesses()){
				pp=1;
			}
			repulsionField = rfFactory.getRepulsionField() ;
			
		}else{
			// simple call 
			rfFactory = new RepulsionFieldFactory();
			repulsionField = rfFactory.getRepulsionField() ;
		}
		 
		// repulsionField.useParallelProcesses(pp); // set to 0 for debugging
		repulsionField.useParallelProcesses(0);
		
		defineEventMessagingEndpoint( eventSink);
		repulsionField.registerEventMessaging( eventSink );
		
		repulsionField.setName("somfluid-app") ;
		repulsionField.setColorSize(false, true);

		repulsionField.setInitialLayoutMode(RepulsionField._INIT_LAYOUT_REGULAR);
		
		
		nbrParticles = nbrparticles;
		
		if (nbrParticles<100){
			repulsionField.setAreaSizeMin();
		} else{
			repulsionField.setDefaultDensity(12, nbrParticles);
			repulsionField.setAreaSizeAuto( nbrParticles );	
			width  = repulsionField.getAreaSize()[0] +10;
			height = repulsionField.getAreaSize()[1] +10;
		}
	 	
		repulsionField.setMaxDensityDeviationPercent( 15.0 );
		// setting basic parameters for the dynamic behavior of the particles
		// will be used if there is nothing to load
		
		repulsionField.setDynamics( nbrParticles, energy, repulsion, deceleration );
		repulsionField.setBorderMode( Neighborhood.__BORDER_ALL); // all=rectangle, none=torus (borderless)
		
		// these two parameters can be allowed to auto-adjust by SomFluidProperties
		
		// in contrast to standard SOM we need not to choose a big initial radius (usually half of the SOM size),
		// since we can merge, split and move the particles, which prevents multicenters for very similar contexts
		double rad = (int)( (Math.sqrt( nbrParticles )/3.5));
		
		int _selectionsize = (int) (Math.round((rad * rad)*Math.PI)*0.82)  ;
		
		if ((nbrParticles>500) && (_selectionsize>nbrParticles*0.4)){
			_selectionsize = (int) (nbrParticles*0.4) ; // maxSelectionSize 1000, 5000
		}
		 
		selszLimit = sfFactory.getSfProperties().getRestrictionForSelectionSize();
		
		if (selszLimit>11){
			repulsionField.setSelectionSizeRestriction(selszLimit) ;
		}else{
			repulsionField.setSelectionSizeRestriction(-1) ;
		}

		// this will calculate the next larger number for a hexagonal pattern;
		// i.e. for the acual selection we have to truncate the selected collection
		// which is not a problem, because it is returned in ordered fashion
	
		repulsionField.setSelectionSize( _selectionsize ); // will be confined by setSelectionSizeRestriction()
		_selectionsize = repulsionField.getSelectionSize() ;
		
		// this populates the field, set "nbrParticles" to 0 if you will import coordinates or a field
		// can be set to auto-adapt
		repulsionField.init( nbrParticles );
		
		// importing set of coordinates from last auto-save , or from given filename 
		// any particle existing before will be removed;
		// if no file will be found, it will be initialized randomly
		
		// repulsionField.init( "importField" );
		// or with filename:  
		// repulsionField.init("importField:filename=C:/Users/kwa/rf/config/~RepulsionFieldData-app-10000.dat" );
		// C:/Users/kwa/rf/config/~RepulsionFieldData-app-10000.dat
		
		// or alternatively in two steps:
		// repulsionField.init(repulsionField,1 );
		// repulsionField.importField();
		
		initComplete=false;
		repulsionField.setDelayedOnset(50);
		out.setPrefix("[SomFlmuid-factory]");
		
		out.delay(200) ;
											out.print(2, "updating field of particles ... ") ;
		for (int i=0;i<1;i++)
		{ // makes it more regular... but the more regular the less the quality of the model
			repulsionField.update();
			out.delay(10) ;
		} 
		
		int z=0;
		while ((initComplete==false) && (z<2000)){
			 out.delay(10) ;
			 if (eventSink==null){
				 initComplete = repulsionField.getInitComplete();
			 }
			 if (initComplete==false){
				 initComplete = sfFactory.getFieldFactory().somFieldInitComplete ;
			 }
			 if (initComplete==false){
				 out.delay(5);
			 }
		}
										
		 
		repulsionField.releaseShakeIt(0,5000 );
		
		z=0;
		while ((initComplete==false) && (z<3000)){
			repulsionField.out.delay(10) ;
			
			 if (eventSink==null){
				 initComplete = repulsionField.getInitComplete();
			 }
			 if (initComplete==false){
				 initComplete = sfFactory.getFieldFactory().somFieldInitComplete ;
			 }
			z++;
		}
		
		if (initComplete){
			sfFactory.setPhysicalFieldStarted(1);
		}
		                                    String str="";
		                                    
		                                    if (initComplete==false){
		                                    	str = "still not ";
		                                    }
											out.print(2, "...updating field of particles "+str+"done. ") ;
											if (initComplete==false){
												out.printErr(1, "...calculation of initial arrangement will be interrupted. \n") ;
												// send an interrupt...
												repulsionField.interrupt();
												
												z=0;
												while ((initComplete==false) && (z<1000)){
													out.delay(10) ;
													z++;
												}
												if (initComplete==false){
													out.printErr(1, "...initialization could not be completed... stopping. \n") ;
													System.exit(-1);
												}
												 
											}
			
		if ((repulsionField!=null) && (initComplete)){
			String particltype , gridtype;
			
			particltype = "particles";
			gridtype  = "" ;
			
			int nbp = repulsionField.getNumberOfParticles() ;
			out.print(1, "\nInitialization has completed successfully, \n"+
						 "now running a grid of type <> using <"+nbp+"> "+particltype+". \n") ;


		}
		return (RepulsionFieldIntf)repulsionField;
	}
	
	
	public RepulsionField getRepulsionField() {
		return repulsionField;
	}

	public boolean getInitComplete() {
		 
		return initComplete;
	}
	public boolean isInitComplete() {
		return initComplete;
	}

	public void setInitComplete(boolean initComplete) {
		this.initComplete = initComplete;
	}

	@Override
	public void close() {
		
		repulsionField.close();
	}
	
	@Override
	public int getNumberOfParticles() {
		int n=-1;
		n = repulsionField.getNumberOfParticles() ;
		return n;
	}

	@Override
	public ParticlesIntf getParticles() {
		 
		return repulsionField.getParticles();
	}

	@Override
	public double getAverageDistanceBetweenParticles() {
		 
		return repulsionField.getAverageDistanceBetweenParticles();
	}

	@Override
	public void setSelectionSize(int surroundN) {
		 
		repulsionField.setSelectionSize(surroundN) ;
	}

	@Override
	public String getSurround(int particleindex, int i, boolean b) {
		 
		return repulsionField.getSurround(0, 0, true) ; // ?????
	}
	
 
}
