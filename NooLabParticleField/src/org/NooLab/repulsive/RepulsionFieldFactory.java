package org.NooLab.repulsive;

import org.NooLab.repulsive.components.RepulsionFieldProperties;
import org.NooLab.repulsive.intf.main.RepulsionFieldIntf;

public class RepulsionFieldFactory {

	RepulsionField repulsionField;
	
	RepulsionFieldProperties rfProperties;
	
	RepulsionFieldFactory rfFactory;
	
	// ========================================================================
	public RepulsionFieldFactory(){
		
		rfProperties = new RepulsionFieldProperties() ;
		rfFactory = this;
		produce(rfProperties);
	}
		
	public RepulsionFieldFactory(RepulsionFieldProperties rfProperties){
		 
		 produce(rfProperties);
	}
	 
	
	private void produce(RepulsionFieldProperties rfProperties){

		
		

		repulsionField = new RepulsionField(rfFactory);
		
		
	}
	// ========================================================================
	
	
	public RepulsionFieldFactory getFactory(){
		
		return this;
	}
	
	public RepulsionField  getRepulsionField(){
		
		return repulsionField ;
	}

	public RepulsionFieldProperties getRfProperties() {
		return rfProperties;
	}

	public RepulsionFieldFactory getRfFactory() {
		return rfFactory;
	}

	public void setRepulsionField(RepulsionFieldIntf repulsionField) {
		this.repulsionField = (RepulsionField) repulsionField;
	}
}
