package org.NooLab.somfluid.components;

import org.NooLab.field.FieldIntf;
import org.NooLab.field.interfaces.FixedNodeFieldEventsIntf;
import org.NooLab.field.interfaces.PhysicalGridFieldIntf;
import org.NooLab.field.interfaces.RepulsionFieldEventsIntf;
import org.NooLab.field.repulsive.RepulsionFieldFactory;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.utilities.logging.PrintLog;




/**    
 * this factory creates, hosts and delivers a RepulsionField 
 * needs to implement :
 * otherwise this class cast exception will occur
 * Exception in thread "SomModuleInstance" java.lang.ClassCastException: org.NooLab.somfluid.PhysicalFieldFactory cannot be cast to org.NooLab.repulsive.intf.main.RepulsionFieldEventsIntf

 */
public class PhysicalFieldFactory {	
	
	SomFluidProperties sfProperties ;
	SomFluidFactory sfFactory;
	
	PhysicalGridFieldIntf somfield ;
	RepulsionFieldEventsIntf eventSinkDynGrid;
	FixedNodeFieldEventsIntf eventSinkFixgrid;

	boolean somFieldInitComplete;
	
	double deceleration = 1.0 ; 
	double repulsion = 1.0 ; 
	double energy = 1.0 ;  
	
	int nbrParticles = 661 ;
	
	int width, height;
	
	// boolean initComplete = false;
	
	
	private PrintLog out = new PrintLog(2,true);
	
	// ------------------------------------------------------------------------
	public PhysicalFieldFactory(){
		
	}
	// ------------------------------------------------------------------------
	
	 
	public PhysicalGridFieldIntf createPhysicalField( SomFluidFactory sfFactory,  
													  Object eventSink,
													  int nbrparticles ){  
		
		
		// will be returned, is generic for any type of grid : PhysicalGridFieldIntf
		somfield =null;
		this.sfFactory = sfFactory;
		sfProperties = sfFactory.getSfProperties();
		
		defineEventMessagingEndpoint(eventSink )  ;
		// eventSinkDynGrid = eventSink;
		
		
		int somtype = sfProperties.getSomGridType();
		
		if (somtype == FieldIntf._SOM_GRIDTYPE_FIXED){
			somfield = new PhysicalFixedField( sfFactory, 
					   						   (FixedNodeFieldEventsIntf)eventSink,
					   						   nbrparticles ) ;  
			sfFactory.setPhysicalFieldStarted(1); 
		}
		if (somtype == FieldIntf._SOM_GRIDTYPE_FLUID){
		
			somfield = new PhysicalRepulsionField( sfFactory, 
												   (RepulsionFieldEventsIntf)eventSink,
					  							   nbrparticles )  ;
		}
		
		
		return somfield;
	}
	
	public void defineEventMessagingEndpoint( Object eventSink) {
		
		int somtype = sfProperties.getSomGridType();
		
		// dependent on grid type
		if (somtype == FieldIntf._SOM_GRIDTYPE_FLUID){
			eventSinkDynGrid = (RepulsionFieldEventsIntf)eventSink ;
		}
		if (somtype == FieldIntf._SOM_GRIDTYPE_FIXED){
			eventSinkFixgrid = (FixedNodeFieldEventsIntf)eventSink ;
		}
		if (somfield!=null){
			somfield.registerEventMessaging( eventSink );
		}
		
		
		
	}


	 
	 

	public void setInitComplete(boolean flag) {
		// if (somfield!=null)somfield.setInitComplete(flag);
		/* there is NO push of this information possible, because the routine returning
		 * the somfield did not yet arrive
		 * we either wait here or use a fetch mechanism in PhysicalRepulsionField
		 */
		somFieldInitComplete= flag;
	}
 
}
