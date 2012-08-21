package org.NooLab.somfluid.components;

 

import org.NooLab.field.fixed.FixedFieldFactory;
import org.NooLab.field.fixed.FixedFieldIntf;
import org.NooLab.field.fixed.FixedFieldWintf;
import org.NooLab.field.fixed.components.FixedFieldParticlesIntf;
import org.NooLab.field.interfaces.FixedNodeFieldEventsIntf;
import org.NooLab.field.interfaces.RepulsionFieldEventsIntf;
 
import org.NooLab.field.repulsive.components.Neighborhood;
import org.NooLab.field.repulsive.intf.particles.RepFieldParticlesIntf;
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
	FixedFieldWintf fixedField; 

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
	
	

	public void defineEventMessagingEndpoint( FixedNodeFieldEventsIntf eventSink){
		if (fixedField!=null){
			fixedField.registerEventMessaging( eventSink );
		}   
	}
	
	
	@Override
	public void registerEventMessaging(Object eventSinkObj) {
		 
		defineEventMessagingEndpoint( (FixedNodeFieldEventsIntf) eventSinkObj);
	}

	public FixedFieldWintf  createFixedField( SomFluidFactory sfFactory,  
											 FixedNodeFieldEventsIntf eventSink,
											 int nbrparticles ){ // ,
	 
		int runRequestTester=0,  selszLimit ;
		
	 
		fixedField.init( nbrParticles );
		


		fixedField.setBorderMode( Neighborhood.__BORDER_ALL); // all=rectangle, none=torus (borderless)
		
		// these two parameters can be allowed to auto-adjust by SomFluidProperties
		
		// in contrast to standard SOM we need not to choose a big initial radius (usually half of the SOM size),
		// since we can merge, split and move the particles, which prevents multicenters for very similar contexts

		double rad = 0;
		
		rad = ((int)( (Math.sqrt( nbrParticles )/3.5)) +
		       (((double)fixedField.getPhysicalWidth()/5.0 + (double)fixedField.getPhysicalHeight()/5.0)/2.0))/2.0;
		
		int smside = (int) Math.min( fixedField.getPhysicalWidth()/5.0 ,fixedField.getPhysicalHeight()/5.0 );
		
		if (rad > (double)smside * 0.54){
			rad = (double)smside * 0.54 ;
		}
		
		int _selectionsize = (int) (Math.round((rad * rad)*Math.PI)*0.84)  ;
		
		if ((nbrParticles>500) || (_selectionsize>nbrParticles*0.48)){
			_selectionsize = (int) (nbrParticles*0.48) ; // maxSelectionSize 1000, 5000
		}
		 
		rad = rad + fixedField.getResolutionFactor() ;
		selszLimit = sfFactory.getSfProperties().getRestrictionForSelectionSize();
		
		// this will calculate the next larger number for a hexagonal pattern;
		// i.e. for the actual selection we have to truncate the selected collection
		// which is not a problem, because it is returned in ordered fashion
	
		fixedField.setSelectionSize( _selectionsize ); // will be confined by setSelectionSizeRestriction()
		_selectionsize = fixedField.getSelectionSize() ;
		
		
		
		out.setPrefix("[SomFluid-factory]");
		
		out.print(2, "updating field of particles ... ") ;
		fixedField.update();
		
		 
		initComplete = (fixedField.getParticles().size()>0) ;
		 
		if ((fixedField!=null) && (initComplete)){
			String particltype , gridtype;
			
			particltype = "particles";
			gridtype  = "" ;
			
			int nbp = fixedField.getNumberOfParticles() ;
			out.print(1, "\nInitialization has completed successfully, \n"+
						 "                   now running a grid of type <> using <"+nbp+"> "+particltype+". \n") ;
			                

		}
		return (FixedFieldWintf)fixedField;
	}
	
	


	public FixedFieldWintf getFixedField() {
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
	public FixedFieldParticlesIntf getParticles() {
		 
		return (FixedFieldParticlesIntf) fixedField.getParticles();
	}


	@Override
	public void setSelectionSize(int surroundN) {
		 
		fixedField.setSelectionSize(surroundN) ;
	}

	@Override
	public String getSurround(int particleindex, int selmode, int surroundN, boolean autoselect) {

		// for parallel queries we have to create a compartment
		String gstr = fixedField.getSurround(particleindex, selmode, surroundN,autoselect) ; 
		return gstr; 
	}

	@Override
	public double getAverageDistanceBetweenParticles() {
		 
		return 100;
	}
	
 
}
