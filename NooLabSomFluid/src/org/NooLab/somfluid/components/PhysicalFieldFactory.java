package org.NooLab.somfluid.components;

import org.NooLab.repulsive.RepulsionField;
import org.NooLab.repulsive.RepulsionFieldFactory;
import org.NooLab.repulsive.components.Neighborhood;
import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.utilities.logging.LogControl;
import org.NooLab.utilities.logging.PrintLog;




/**    
 * this factory creates, hosts and delivers a RepulsionField 
 * needs to implement :
 * otherwise this class cast exception will occur
 * Exception in thread "SomModuleInstance" java.lang.ClassCastException: org.NooLab.somfluid.PhysicalFieldFactory cannot be cast to org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf

 */
public class PhysicalFieldFactory {	
	
	RepulsionFieldFactory rfFactory;
	RepulsionField repulsionField; 

	double deceleration = 1.0 ; 
	double repulsion = 1.0 ; 
	double energy = 1.0 ;  
	
	int nbrParticles = 661 ;
	
	int width, height;
	
	boolean initComplete = false;
	private PrintLog out = new PrintLog(2,true);
	
	// ------------------------------------------------------------------------
	public PhysicalFieldFactory(){
		
	}
	// ------------------------------------------------------------------------
	
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
		
		out.delay(200) ;
											out.print(4, "updating field of particles ... ") ;
		for (int i=0;i<1;i++)
		{ // makes it more regular... but the more regular the less the quality of the model
			repulsionField.update();out.delay(10) ;
		} 
		
		while (initComplete==false){
			 out.delay(10) ;
			 if (eventSink==null){
				 initComplete = repulsionField.getInitComplete();
			 }
		}
											
		 
		repulsionField.releaseShakeIt(0,5000 );
		
		initComplete=false; int z=0;
		while ((initComplete==false) && (z<12000)){
			repulsionField.out.delay(10) ;
			z++;
		}
											out.print(3, "updating field of particles done. ") ;
		return (RepulsionFieldIntf)repulsionField;
	}
	
	
	private boolean getInitComplete() {
		 
		return false;
	}
	public RepulsionField getRepulsionField() {
		return repulsionField;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public boolean isInitComplete() {
		return initComplete;
	}

	public void setInitComplete(boolean initComplete) {
		this.initComplete = initComplete;
	}
 
}
