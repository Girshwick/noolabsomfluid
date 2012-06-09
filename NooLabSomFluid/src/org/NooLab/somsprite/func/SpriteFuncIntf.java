package org.NooLab.somsprite.func;

import java.util.ArrayList;

import org.NooLab.somsprite.FunctionCohortParameterSet;

public interface SpriteFuncIntf {

	public static double __MISSING_VALUE = -9.09090909 ;
	
	public String getName();
	public void   setName(String funcName);
	
	public void   setMissingValue( double missingvalue );
	
	public String getFormula();
	
	public Object calculate( double... values) throws Exception;
	
	public void allowVariablesCountMismatch(boolean flag);
	
	public FunctionCohortParameterSet getCohortParameterSet() ;
	
	public ArrayList<String> getVariables();
}
