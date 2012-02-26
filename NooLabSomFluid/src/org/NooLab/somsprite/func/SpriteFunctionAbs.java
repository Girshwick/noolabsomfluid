package org.NooLab.somsprite.func;


import java.util.ArrayList;

import org.NooLab.somsprite.SpriteFuncIntf;
import org.NooLab.utilities.strings.StringsUtil;
import org.nfunk.jep.EvaluatorVisitor;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.function.*;


/**
 * 
 * use expression parser ALWAYS
 * 
 * 
 */
public class SpriteFunctionAbs  implements SpriteFuncIntf{
	
	public static final double _EMPTY_LINEAR = 0.0;
	public static final double _EMPTY_MULT   = 1.0;
	public static final double _EMPTY_POWER  = 0.0;
	public static final double _EMPTY_LOG    = 1.0;
	public static final double _EMPTY_TRIGON = 0.0;
	

	String funcName="";
	String formula ="";
	
	String expression = " ";
	String[] variables ;
	
	private boolean throwExceptions;
	private JEP jep;
	    
	double[] exValues ;
	
	EvaluatorVisitor ev;
	Node jepNode;
	double missingValue = __MISSING_VALUE;
	
	boolean allowVariablesCountMismatch;
	
	StringsUtil strgutil = new StringsUtil();
	
	// ========================================================================
	public SpriteFunctionAbs( String expression, boolean throwexceptions ){
		
		formula = expression;
		throwExceptions = throwexceptions;
		
		init();
		
	}
	
	private void init( ){
		
		int cc;
		
		
		formula = formula.replace(" and ", " && ");
		formula = formula.replace(" or ", " || ");
		formula = formula.replace(" not", " !");
		formula = formula.replace("mod", "%");
		
		parseExpression() ;
		
		
		SymbolTable  symtab = jep.getSymbolTable() ;
		
		cc = symtab.keySet().size();
		variables = new String[cc] ;
		
		int z=0;
		for (Object key: symtab.keySet()) {
        	// System.out.println("Key : " + key.toString()  + " Value : " + symtab.getValue(key));
			if (z<variables.length){
				variables[z] = key.toString() ;
			}
        	z++;
        }
		 
		
		
	}
	// ========================================================================

	
	@Override
	public double calculate(double... values) throws Exception{
		
		double resultValue= missingValue ,a=0,b=0;
		Object result = null;
		int nv=0;

		// set values of variable
		nv = values.length ;
		
		if ((nv!=variables.length) && (allowVariablesCountMismatch==false)){
			
			throw(new Exception("wrong number of arguments for virtual function -> "+this.funcName +" = "+ this.formula));
		}
		
		
		for (int i=0;i<values.length;i++){
			
			jep.setVarValue( variables[i], values[i]);
		}
		
		if ((nv!=variables.length) && (allowVariablesCountMismatch)){
			//  we have to get the context of the variable, = the function for which it is the argument
			
			// we remove the surplus variables ... hopefully incl their embedding function ...
			
		}
		
		try { 
            
            result = jep.evaluate(jepNode) ;
             
            // Is the result ok?
            if (result!=null) {
                
                resultValue = (Double)result ;
            } else{
            	resultValue = missingValue ;
            	
            }
            
        } catch (Exception e) {
        	String str = "Error while evaluating:\n"+e.getMessage() ;
        }
        
        if (throwExceptions){
        	if (result==null){
        		String vstr="" ;
        		String errStr = "falure in calculating expression <"+formula+"> for values "+vstr ;
        		throw(new Exception(errStr));
        	}
        }
 		return resultValue;
	}

	private boolean parseExpression() {
        // reload the standard variable table (with pi, e, and i)
		/*
			varFact = jep.getVariableFactory();
			new StandardVariableTable()
        	jep.setComponent();
        */
        try {
        	
        	jep = new JEP();
        	
        	ev = jep.getEvaluatorVisitor();
        	jep.addStandardFunctions() ;
        	
        	// we can only parse if we have set the variables, so we have to remove everything
        	// in order to get the variables
        	String str ;
        	str = formula.trim()+" "; // blank necessary to find nums in the beginning
if ( (str.contains("log(0.01 + a + b)")) ||
	 (str.contains("1.0 + (a-b)/(2.0*(a+b))")) ||
	 (str.contains("1.0 + (a-b)/(2.0*(a+b))"))
	 ){
	
	int k;
	k=0;
}
        	//note that "a0.2" will be recognized as a variable, it will not be split !
        	str = str +" ";
			// regex for removing numerical constants in any form 
			str = str.replaceAll("[-+]?[ (]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?"," ");

        	// remove all arithmet operators, inclusing all brackets ... this remove also any remaining dot : why???
        	str = str.replaceAll("[{}()\\[\\]\\*+-/\\^\\!\\%<>=\\&|]", " ");
        	  
        	 
        	str = str.replace("[", " ").replace("]", " ");
        	str = str.trim() ;
        	
        	// we would need a list of all functions via reflection, then testing against that list
        	// replace is not suitable, we should do it explicitly via position after toLower()...,
        	// providing the list of keywords
        	str = str.replace("sin", " ");
        	str = str.replace("cos", " ");
        	str = str.replace("asin", " ");
        	str = str.replace("acos", " ");
        	str = str.replace("log", " ");
        	str = str.replace("log10", " ");
        	str = str.replace("pow", " ");
        	str = str.replace("exp", " ");
        	str = str.replace("pi", " ");
        	str = str.replace("floor", " ");
        	str = str.replace("ceil", " ");
        	str = str.replace("round", " ");
        	str = str.replace("if", " ");
        	str = str.replace("then", " ");
        	str = str.replace("else", " ");
        	str = str.replace(";", " ").replace("\n\r", " ").replace("\r\n", " ").replace("\n", " ");
        	
        	str = strgutil.replaceAll(str, "  ", " ").trim();
        	
        	String[] varStrings = str.split(" ") ;
        	ArrayList<String> dejaStr = new ArrayList<String>();
        	
        	for (int i=0;i<varStrings.length;i++){
        		str = varStrings[i].trim();
        		if ((str.length()>0) && (dejaStr.indexOf(str)<0)){
        			dejaStr.add(str);
        			jep.addVariable( varStrings[i], 0);	
        		}
        	}
            
            /*
    		SymbolTable  symtab = jep.getSymbolTable() ;
    		
    		VariableFactory vf = symtab.getVariableFactory() ;
    		// vf.createVariable("c") ;
    		*/
        	
            jepNode = jep.parse(formula);
            
            
            return true;

        } catch (ParseException e) {
            String str = "Error while parsing:\n"+e.getMessage();
        } catch (Exception e) {
            String str = "Error while parsing:\n"+e.getMessage();
        }
        return false;
    }

    /**
     * Whenever the expression is changed, this method is called.
     * The expression is parsed, and the updateResult() method
     * invoked.
     */
    void exprFieldTextValueChanged() {
        if (parseExpression()) {
        	updateResult();
        }
    }
        
	public void setFormulaExpression( String expr){
		formula = expr;
	}
	
	private void updateResult() {
		 Object result = null;

	        // Get the value
	        try {
	            result = jep.getValueAsObject();

	            // Is the result ok?
	            if (result!=null) {
	                String rStr = result.toString();
	            } 
	            
	        } catch (Exception e) {
	        	String str = "Error while evaluating:\n"+e.getMessage() ;
	        }

	}
	// ----------------------------------------------------
	@Override
	public String getName() {
		return funcName;
	}

	@Override
	public void setName(String funcName) {
		this.funcName = funcName;
	}


	public String getExpression() {
		return expression;
	}


	@Override
	public void setMissingValue(double missingvalue) {
		missingValue = missingvalue;
		
	}


	public double getMissingValue() {
		return missingValue;
	}

	@Override
	public void allowVariablesCountMismatch(boolean flag) {
		 
		allowVariablesCountMismatch = flag;
	}
	
}

