package org.NooLab.somsprite.func;

import org.NooLab.somsprite.FunctionCohortParameters;

 



public class XFunction extends SpriteFunctionAbs{

    
	public XFunction( String expression ){
		super(expression, true);
		
	}

	public XFunction(String expression, boolean throwExceptions ) {
		 super(expression,throwExceptions);
		  
	}

	public XFunction(String expression, FunctionCohortParameters fcp) {
		super(expression,fcp, true);
	}
    
	

}
