package org.NooLab.somfluid.components;

import org.NooLab.repulsive.RepulsionField;
import org.NooLab.repulsive.RepulsionFieldFactory;
import org.NooLab.repulsive.components.Neighborhood;
import org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;



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
	
	// ------------------------------------------------------------------------
	public PhysicalFieldFactory(){
		
	}
	// ------------------------------------------------------------------------
	
	public RepulsionFieldIntf createPhysicalField( RepulsionFieldEventsIntf eventSink, int nbrparticles ){
	 
		int runRequestTester=0;
		
		// TODO: properties offered by static create(s) and interface
		//       which then are part of SomFluidProperties 
		
		if (runRequestTester>0){
			// for testing, 100 = 100ms delay between calls, pres +/. to ac-/decelerate
			rfFactory = new RepulsionFieldFactory("test:RQ=1000");
			repulsionField = rfFactory.getRepulsionField() ;
			
		}else{
			// simple call 
			repulsionField = (new RepulsionFieldFactory()).getRepulsionField() ;
		}
		
		
		repulsionField.useParallelProcesses(0); // set to 0 for debugging
		
		repulsionField.registerEventMessaging( eventSink );
		repulsionField.setName("somfluid-app") ;
		repulsionField.setColorSize(false, true);

		repulsionField.setInitialLayoutMode(RepulsionField._INIT_LAYOUT_REGULAR);
		
		nbrParticles = nbrparticles;
		
		if (nbrParticles<100){
			repulsionField.setAreaSizeMin();
		} else{
			repulsionField.setDefaultDensity(12);
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
		int _selectionsize = (int) (1.4 * (Math.sqrt( nbrParticles )/2.0)) ;
		
		if (_selectionsize>2000){
			_selectionsize = 2000 ; // maxSelectionSize 1000, 5000
		}
		
		repulsionField.setSelectionSize( _selectionsize ); 
		
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
		
		
		repulsionField.setDelayedOnset(1000);
		
		return (RepulsionFieldIntf)repulsionField;
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
 
}
