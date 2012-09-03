package org.NooLab.somfluid.astor.stream;

import java.util.Observable;
import java.util.Observer;

import org.NooLab.itexx.comm.intf.SomTexxObservationIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.astor.SomAstorFrameIntf;



/**
 * 
 * organizes the collection of data into the current table structure,
 * including using the SomTransformer for online transformation
 * 
 * sources are DB, file
 * 
 * it reads periodically the sources, or it gets informed (as observer) by 
 * instances like Texx (through objects that are formatted as SomTexxObservationIntf), 
 * which are able to create a randomgraph vector,  
 *
 * SomDataStreamer maintains 
 * 
 */
public class SomDataStreamer 
								extends 
											Observable 
								implements 
											SomTexxObservationIntf, 
											SomDataStreamerIntf {

	
	SomAstorFrameIntf somAstorFrame ; // points to SomAssociativeStorage
	SomFluidProperties sfProperties ;
	
	// ========================================================================
	public SomDataStreamer( SomAstorFrameIntf somastor, SomFluidProperties sfProps) {
		somAstorFrame = somastor ;
		sfProperties = sfProps;
		
		
	}
	// ========================================================================
	
	
	
	
	
	
	
	// ----------------------------------------------------
	class Receiver implements Observer{

		
		
		
		@Override
		public void update(Observable arg0, Object arg1) {
			// TODO Auto-generated method stub
			
		}
		
		
	} // class Receiver
	// ----------------------------------------------------
	
}
