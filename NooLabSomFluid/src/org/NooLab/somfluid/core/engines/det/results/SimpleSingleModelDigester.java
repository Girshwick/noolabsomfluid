package org.NooLab.somfluid.core.engines.det.results;


import java.io.Serializable;

import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;




public class SimpleSingleModelDigester extends SomResultHandlerAbs  {

	private static final long serialVersionUID = -2071393529509845994L;


	// ========================================================================
	public SimpleSingleModelDigester( SomHostIntf somHost, SomFluidFactory sfFactory){ // SimpleSingleModel simo) {
		super( somHost, sfFactory );
		
	}
	// ========================================================================
	
	 
	@Override
	public void handlingResults() {
		 
		out.printErr(1, "handling results of simple single modeling...");
		
		
	}

	
	
	
}
