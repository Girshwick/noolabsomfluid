package org.NooLab.somsprite.func;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import org.NooLab.somsprite.FunctionCohortParameterSet;
import org.NooLab.somsprite.FunctionCohortParameters;
import org.NooLab.somsprite.SpriteFuncIntf;
import org.NooLab.utilities.ArrUtilities;
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
	ArrayList<String> variables = new ArrayList<String>();
	
	private boolean throwExceptions;
	private JEP jep;
	    
	double[] exValues ;
	
	EvaluatorVisitor ev;
	Node jepNode;
	double missingValue = __MISSING_VALUE;
	
	FunctionCohortParameterSet cohortParameterSet;
	
	public FunctionCohortParameterSet getCohortParameterSet() {
		return cohortParameterSet;
	}

	boolean allowVariablesCountMismatch;
	
	StringsUtil strgutil = new StringsUtil();
	
	// ========================================================================
	public SpriteFunctionAbs( String expression, boolean throwexceptions ){
		
		formula = expression;
		throwExceptions = throwexceptions;
		double[] cohortValues = new double[0] ;
		
		init(cohortValues);
		
	}
	
	public SpriteFunctionAbs(String expression, FunctionCohortParameters fcp, boolean throwexceptions) {
		
		FunctionCohortParameterSet cps = cohortParameterSet;
		throwExceptions = throwexceptions;
		formula = expression;
		
		double[] cohortValues ;
		
		cps = fcp.getitem(0);
		// strategy for function cohorts: we define a variable as a vector, 
		// the evaluation will then return also a vector (we need to check the type!) 
		
		int n = cps.getSteps();
		cohortValues = new double[n] ;
		
		cohortValues = ArrUtilities.fillarray( 10, cps.getLo() , cps.getHi()) ; 
		
		cps.cohortValues = cohortValues;
		
		this.cohortParameterSet = cps;
		init(cohortValues);
		
		/*
		 // The returned type will be Vector<Object>
	    assertTrue(res2 instanceof Vector<?>);
	    Vector<Object> vec = (Vector<Object>) res2;
	    // Convert to an array
	    Object[] array = vec.toArray();
	    */
	}

	/*
	 		j.setAllowUndeclared(true);

	 		// switch assignment facilities on
	 		j.setAllowAssignment(true);

	 		// parse assignment equations
	 		try {j.parse("x=3");} catch (ParseException e) {}
	 		// evaluate it - no need to save the value returned
	 		try {j.evaluate();} catch (EvaluationException e) {}

	 		// parse a second equation
	 		try {j.parse("y=2");} catch (ParseException e) {}
	 		try {j.evaluate();} catch (EvaluationException e) {}

	 		// an equation involving the above variables
	 		try {j.parse("x^y");} catch (ParseException e) {}

	 */
	private void init( double[] cohortValues){
		
		int cc;
if (formula.contains("k")){
	cc=0;
}

		formula = formula.replace(" and ", " && "); 
		formula = formula.replace(" or ", " || ");
		formula = formula.replace(" not", " !");
		formula = formula.replace("mod", "%");
		
		formula = formula.replace("power", "pow");
		formula = formula.replace("log10", "log");
		
		formula = formula.replace("<>", " != ");
		parseExpression( cohortValues ) ;
		
 
		
		SymbolTable  symtab = jep.getSymbolTable() ;
		
		cc = symtab.keySet().size();
		//variables = new String[cc] ;
		
		int z=0;
		for (Object key: symtab.keySet()) {
        	// System.out.println("Key : " + key.toString()  + " Value : " + symtab.getValue(key));
			//if (z<variables.length)
			{
				variables.add( key.toString() );
			}
        	z++;
        }
		 
		
		
	}
	private boolean parseExpression( double[] cohortValues) {
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
				if ((str.contains("=")) && (str.contains("!=")==false) && 
					(str.contains(">=")==false) && (str.contains("<=")==false)){
					int peq = str.indexOf("=");
					if (peq>0){
						str = str.substring(peq+1, str.length());
					}
				}
	
	        	//note that "a0.2" will be recognized as a variable, it will not be split !
	        	str = str +" ";
				// regex for removing numerical constants in any form 
				str = str.replaceAll("[-+]?[ (]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?"," ");
	
	        	// remove all arithmet operators, inclusing all brackets ... this remove also any remaining dot : why???
	        	str = str.replaceAll("[{}()\\[\\]\\*+\\-,/\\^\\!\\%<>=\\&|]", " ");
	        	str = strgutil.replaceAll(str, "-", " ");
	        	 
	        	str = str.replace("[", " ").replace("]", " ");
	        	str = str.trim() ;
	        	
	        	// we would need a list of all functions via reflection, then testing against that list
	        	// replace is not suitable, we should do it explicitly via position after toLower()...,
	        	// providing the list of keywords
	
	        	str = str.replace("asinh", " ");
	        	str = str.replace("acosh", " ");
	        	str = str.replace("atanh", " ");
	        	str = str.replace("sinh", " ");
	        	str = str.replace("cosh", " ");
	        	str = str.replace("tanh", " ");
	        	str = str.replace("asin", " ");
	        	str = str.replace("acos", " ");
	        	str = str.replace("atan", " ");
	        	str = str.replace("atanc", " ");
	        	str = str.replace("sin", " ");
	        	str = str.replace("cos", " ");
	        	str = str.replace("cot", " ");
	        	str = str.replace("tan", " ");
	        	str = str.replace("log", " ");
	        	str = str.replace("log10", " ");
	        	str = str.replace("pow", " ");
	        	str = str.replace("exp", " ");
	        	str = str.replace("sqrt", " ");
	        	str = str.replace("pi", " ");
	        	str = str.replace("floor", " ");
	        	str = str.replace("ceil", " ");
	        	str = str.replace("round", " ");
	        	str = str.replace("rint", " ");
	        	str = str.replace("rand", " ");
	        	str = str.replace("if", " ");
	        	str = str.replace("then", " ");
	        	str = str.replace("else", " ");
	        	str = str.replace(";", " ").replace("\n\r", " ").replace("\r\n", " ").replace("\n", " ");
	
	        	str = str.replace("lg", " ");
	        	str = str.replace("log", " ");
	        	str = str.replace("ln", " ");
	
	        	str = str.replace("re", " ");
	        	str = str.replace("im", " ");
	
	        	str = str.replace("sqrt", " ");
	        	str = str.replace("sum", " ");
	        	str = str.replace("mod", " ");
	        	str = str.replace("signum", " ");
	        	str = str.replace("avg", " ");

	        	str = str.replace("abs", " ");
	        	str = str.replace("max", " ");
	        	str = str.replace("min", " ");
	        	
	        	str = str.replace("/", " ");
	        	str = str.replace("=", " ");
	        	
	        	// now our own functions that we have dynamically defined
	        	str = str.replace("spin", " ");
	        	str = str.replace("ellips", " ");
	        	
	        	
	        	str = strgutil.replaceAll(str, "  ", " ").trim();
	        	/*
	        	 sin(x) 
	cos(x) 
	tan(x) 
	asin(x) 
	acos(x) 
	atan(x) 
	atan2(y, x) 
	sec(x) 
	cosec(x) 
	cot(x) 
	sinh(x) 
	cosh(x) 
	tanh(x) 
	asinh(x) 
	acosh(x) 
	atanh(x) 
	
	ln(x) 
	log(x) 
	lg(x) 
	exp(x) 
	pow(x) 
	
	avg(x1,x2,x3,...) 
	min(x1,x2,x3,...) 
	max(x1,x2,x3,...) 
	vsum(x1,x2,x3,...) 
	
	round(x), round(x, p) 
	rint(x), rint(x, p) 
	floor(x) 
	ceil(x) 
	if(cond, trueval, falseval) 
	str(x) 
	abs(x) 
	rand() 
	mod(x,y)= x % y 
	sqrt(x) 
	sum(x,y,...) 
	binom(n, i) 
	signum(x) 
	
	re(c) 
	im(c) 
	cmod(c) 
	arg(c) 
	conj(c) 
	complex(x, y) 
	polar(r, theta) 
	
	left(str, len) 
	right(str, len) 
	mid(str, start, len) 
	substr(str, start, [end]) 
	lower(str) 
	upper(str) 
	len(str) 
	trim(str) 
	
	TODO: allow regex'es for string treatment, including library of common operations
	
	        	 */
    			jep.setAllowUndeclared(true);
    			jep.setAllowAssignment(true);
    			jep.setImplicitMul(true) ;
    			
	        	ArrayList<String> dejaStr = new ArrayList<String>();
	        	
	        	str = strgutil.replaceAll(str, "  ", " ").trim();
	        	
	        	String[] varStrings = str.split(" ") ;
	        	
	        	
	        	for (int i=0;i<varStrings.length;i++){
	        		str = varStrings[i].trim();
	        		if ((str.length()>0) && (dejaStr.indexOf(str)<0) && (str.length()<=2) ){
	        			dejaStr.add(str);
	        			jep.addVariable( varStrings[i], 0);	
	        		}
	        	}
	            
	            /*
	    		SymbolTable  symtab = jep.getSymbolTable() ;
	    		
	    		VariableFactory vf = symtab.getVariableFactory() ;
	    		// vf.createVariable("c") ;
	    		*/
	        	SymbolTable  symtab = jep.getSymbolTable() ;

	        	if ((cohortValues!=null) &&  (cohortValues.length>0)){
	    			
	    			try{
	    				// NEITHER of these declarations work, so we will do it "manually" in the "calculate()" method,
	    				// setting the values for k one after another and collecting the results
	    				if (jep.getVar("k")!=null)
	    					jep.removeVariable("k");
	    				
	    				jep.addVariable("k", new Vector<Object>(ArrUtilities.changeArraystyle(cohortValues)));
	    				String pstr = ArrUtilities.arr2Text(cohortValues,4).replace("  ", " ").replace(" ", ",");
	    				pstr = "["+pstr+"] " ;// k = 
	    				// jep.setVarValue("k", pstr) ;
	    				jep.parse( pstr);
	    				if (dejaStr.indexOf("k")<0)
	    					dejaStr.add("k") ;
	    				
	    			}catch (Exception e) {
	    				
	    			}
	    			  
	    		} // cohortValues ?
	        	
	            jepNode = jep.parse(formula);
	            
	            
	            return true;
	
	        } catch (ParseException e) {
	        	
	            String str = e.getMessage();
	            if (str.contains("<EOF>")){
	            	str = "Error while parsing (check brackets before end of line/expression):\n"+str;
	            }else{
	            	str = "Error while parsing:\n"+str;
	            }
	            System.err.println(str);
	        } catch (Exception e) {
	            String str = "Error while parsing:\n"+e.getMessage();
	        }
	        return false;
	    }

	// ========================================================================

	
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

	@Override
	public Object calculate(double... values) throws Exception{
	// TODO XXX we need a map here, or String... containing sth like a:value	
	// since the order of the variables is not guaranteed... often [b,a,k] instead of [a,b,k]
		
		double a=0,b=0,cohortValue;
		Object result = null, resultValueObj=null;
		int p,z,vp,nv=0,cc;
		boolean hb;
		String str, varstr, dynparameterLabel,pvaluestr;
		ArrayList<Double> cohortResults = null ;
		
		
		
		// set values of variable
		nv = values.length ;
		vp = 0;
		if (cohortParameterSet!=null){
			if (cohortParameterSet.getVarPLabel().length()>0){
				vp = 1;// a = [2, 3, 10] 
			}
		}
		if ((nv!=variables.size()-vp) && (allowVariablesCountMismatch==false)){
			
			throw(new Exception("wrong number of arguments for virtual function -> "+this.funcName +" = "+ this.formula));
		}
		
		Collections.sort(variables) ;
		
		// explicit assignment instead of implicit style that is jsut using the order
		z=0;
		for (int i=0;i<values.length;i++){
			p = i;
			
			if (cohortParameterSet!=null){
				varstr = "";
				
				while ((varstr.length()==0) || (varstr.contentEquals("k"))){
					varstr = variables.get(p+z);
					if (varstr.contentEquals( cohortParameterSet.getVarPLabel() )){
						z++;
					}
				}
				
			}else{
				varstr = variables.get(p);
			}
			
			jep.setVarValue( varstr, values[i]);			
		} // ->
		

		
		
		try { 
            /*
             * it is really bad: no vectors are allowed, at least not in this version;
             * any attempt to stuff a vector into "k" results in an exception "Invalid parameter type"
             * we should change the sources....
             * 
             * but for now, we do it manually 
             * 
             */
			if (cohortParameterSet==null){
				resultValueObj = jep.evaluate(jepNode) ;
			}else{
				
				cohortResults = new ArrayList<Double>() ;
				
				SymbolTable  symtab = jep.getSymbolTable() ;
				/*
				cc = symtab.keySet().size();
				pvaluestr = "";
				
				int z=0;
				for (Object key: symtab.keySet()) {
		        	// System.out.println("Key : " + key.toString()  + " Value : " + symtab.getValue(key));
					varstr = key.toString() ;
					if (varstr.contentEquals( cohortParameterSet.getVarPLabel() )){
						pvaluestr = symtab.getValue(key).toString() ;
						dynparameterLabel = cohortParameterSet.getVarPLabel();
						break;
						// or looping through variables??
					}
					z++;
		        }
				*/
				// get position of dynparameterLabel
				z = variables.indexOf( cohortParameterSet.getVarPLabel());
				 
				for (int i=0;i<cohortParameterSet.cohortValues.length;i++ ){
				
					cohortValue = cohortParameterSet.cohortValues[i] ;
					jep.setVarValue( variables.get(z), cohortValue);
					
					result = jep.evaluate(jepNode) ;
					cohortResults.add((Double)result);
				}
				resultValueObj =(Object)cohortResults ;
				
			}
             
            // Is the result ok?
            if (resultValueObj==null) {
            	resultValueObj = (Object)missingValue ;
            }
            
        } catch (Exception e) {
        	String estr = "Error while evaluating:\n"+e.getMessage() ;
        	System.err.println(estr);
        }
        
        if (throwExceptions){
        	if (resultValueObj==null){
        		String vstr= ArrUtilities.arr2Text(values,3);
        		String errStr = "failure in calculating expression <"+formula+"> for values:"+vstr ;
        		throw(new Exception(errStr));
        	} 
        }
 		return resultValueObj;
	}

	/**
     * Whenever the expression is changed, this method is called.
     * The expression is parsed, and the updateResult() method
     * invoked.
     */
    void exprFieldTextValueChanged() {
        if (parseExpression(null)) {
        	updateResult();
        }
    }
        
	public void setFormulaExpression( String expr){
		formula = expr;
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

