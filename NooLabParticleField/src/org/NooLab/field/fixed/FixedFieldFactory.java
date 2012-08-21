package org.NooLab.field.fixed;

import org.NooLab.field.repulsive.RepulsionField;
import org.NooLab.field.repulsive.components.RepulsionFieldProperties;
import org.NooLab.utilities.logging.PrintLog;

public class FixedFieldFactory {

	FixedField fixedField;
	FixedFieldFactory ffFactory;
	
	FixFProperties fixFProperties; 
	
	
	PrintLog out = new PrintLog(2,true) ;
	
	// ========================================================================
	public FixedFieldFactory(){
		init();	
	}
	// ========================================================================
	
	
	private void init(){
		
		out.setPrefix("[Field-factory]") ;
		
		fixFProperties = new FixFProperties() ;
		ffFactory = this;
		produce(fixFProperties);
	}
		
	public FixedFieldFactory(FixFProperties fixFProperties){
		 
		 produce(fixFProperties);
	}
	 
	
	private void produce(FixFProperties fixFProperties){

		fixedField = new FixedField(ffFactory);
		
	}
	public FixedField getFixedField() {
		 
		return fixedField;
	}

}
