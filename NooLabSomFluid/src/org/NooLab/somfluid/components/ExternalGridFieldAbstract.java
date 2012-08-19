package org.NooLab.somfluid.components;

import org.NooLab.field.interfaces.PhysicalGridFieldIntf;
import org.NooLab.utilities.logging.PrintLog;

public abstract class ExternalGridFieldAbstract  implements PhysicalGridFieldIntf{
	
	
	PhysicalGridFieldIntf intfReference;
	
	int nbrParticles = 661 ;
	
	int width, height;
	
	
	protected PrintLog out = new PrintLog(2,true);
	
	// ========================================================================
	public ExternalGridFieldAbstract(){
		
	}
	// ========================================================================
	
	

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
