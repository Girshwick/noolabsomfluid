package org.NooLab.somfluid.components;

import org.NooLab.field.fixed.FixedField;
import org.NooLab.field.fixed.FixedFieldFactory;
import org.NooLab.field.fixed.FixedFieldIntf;
import org.NooLab.field.interfaces.FixedNodeFieldEventsIntf;
import org.NooLab.field.interfaces.RepulsionFieldEventsIntf;
import org.NooLab.field.repulsive.RepulsionField;
import org.NooLab.field.repulsive.RepulsionFieldFactory;
import org.NooLab.field.repulsive.components.Neighborhood;
import org.NooLab.field.repulsive.intf.main.RepulsionFieldIntf;
import org.NooLab.field.repulsive.intf.particles.ParticlesIntf;
import org.NooLab.somfluid.SomFluidFactory;





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
public class PhysicalFixedField extends ExternalGridFieldAbstract{	
	
	FixedFieldFactory rfFactory;
	FixedField fixedField; 

	FixedNodeFieldEventsIntf eventSink = null;
	
	double deceleration = 1.0 ; 
	double repulsion = 1.0 ; 
	double energy = 1.0 ;  
	
	boolean initComplete = false;
	
	
	// ========================================================================
	public PhysicalFixedField( 	SomFluidFactory sfFactory, 
								FixedNodeFieldEventsIntf eventsink, 
								int nbrparticles){
		
		eventSink = eventsink;
		nbrParticles = nbrparticles;
		
		rfFactory = new FixedFieldFactory();
		fixedField = rfFactory.getFixedField( ) ;
		
		/*
		 // for testing, 100 = 100ms delay between calls, pres +/. to ac-/decelerate
			rfFactory = new RepulsionFieldFactory("test:RQ=1000", 1); // , LogControl.Level [1 lowest output];
		 */
		
		
		
		createFixedField( sfFactory, eventSink, nbrParticles );
	}
	
	// ========================================================================
	
	public void defineEventMessagingEndpoint( RepulsionFieldEventsIntf eventSink){
		if (fixedField!=null){
			fixedField.registerEventMessaging( eventSink );
		}
	}
	
	
	public FixedFieldIntf  createFixedField( SomFluidFactory sfFactory,  
											 FixedNodeFieldEventsIntf eventSink,
											 int nbrparticles ){ // ,
	 
		int runRequestTester=0,  selszLimit ;
		
	 
		fixedField.init( nbrParticles );
		


		fixedField.setBorderMode( Neighborhood.__BORDER_ALL); // all=rectangle, none=torus (borderless)
		
		// these two parameters can be allowed to auto-adjust by SomFluidProperties
		
		// in contrast to standard SOM we need not to choose a big initial radius (usually half of the SOM size),
		// since we can merge, split and move the particles, which prevents multicenters for very similar contexts
		double rad = (int)( (Math.sqrt( nbrParticles )/3.5));
		
		int _selectionsize = (int) (Math.round((rad * rad)*Math.PI)*0.82)  ;
		
		if ((nbrParticles>500) && (_selectionsize>nbrParticles*0.4)){
			_selectionsize = (int) (nbrParticles*0.4) ; // maxSelectionSize 1000, 5000
		}
		 
		selszLimit = sfFactory.getSfProperties().getRestrictionForSelectionSize();
		
		
		// this will calculate the next larger number for a hexagonal pattern;
		// i.e. for the actual selection we have to truncate the selected collection
		// which is not a problem, because it is returned in ordered fashion
	
		fixedField.setSelectionSize( _selectionsize ); // will be confined by setSelectionSizeRestriction()
		_selectionsize = fixedField.getSelectionSize() ;
		
		 
		
		initComplete=false;
		fixedField.setDelayedOnset(50);
		out.setPrefix("[SomFlmuid-factory]");
		
		out.delay(200) ;
											out.print(2, "updating field of particles ... ") ;
		 fixedField.update();
			 
		 
		if ((fixedField!=null) && (initComplete)){
			String particltype , gridtype;
			
			particltype = "particles";
			gridtype  = "" ;
			
			int nbp = fixedField.getNumberOfParticles() ;
			out.print(1, "\nInitialization has completed successfully, \n"+
						 "now running a grid of type <> using <"+nbp+"> "+particltype+". \n") ;


		}
		return (FixedFieldIntf)fixedField;
	}
	
	
	public FixedFieldIntf getFixedField() {
		return fixedField;
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
		
		fixedField.close();
	}
	
	@Override
	public int getNumberOfParticles() {
		int n=-1;
		n = fixedField.getNumberOfParticles() ;
		return n;
	}

	@Override
	public ParticlesIntf getParticles() {
		 
		return fixedField.getParticles();
	}


	@Override
	public void setSelectionSize(int surroundN) {
		 
		fixedField.setSelectionSize(surroundN) ;
	}

	@Override
	public String getSurround(int particleindex, int i, boolean b) {
		 
		return fixedField.getSurround(0, 0, true) ; // ?????
	}

	@Override
	public double getAverageDistanceBetweenParticles() {
		 
		return 100;
	}
	
 
}
