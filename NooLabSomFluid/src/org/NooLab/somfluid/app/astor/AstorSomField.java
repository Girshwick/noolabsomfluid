package org.NooLab.somfluid.app.astor;

import org.NooLab.somfluid.core.engines.det.DSomCore;


/**
 * 
 * 
 * much like DSom
 * 
 *
 */
public class AstorSomField {

	DSomCore dSomCore ;
	
	// ========================================================================
	public AstorSomField(){
		
	}
	// ========================================================================
	
	
	public void perform(){
		
		dSomCore = new DSomCore(this) ;
	}
}
