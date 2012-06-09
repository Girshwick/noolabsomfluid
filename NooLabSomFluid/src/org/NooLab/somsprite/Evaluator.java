package org.NooLab.somsprite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

 
import org.NooLab.somsprite.func.SpriteFuncIntf;
import org.NooLab.somsprite.func.XFunction;

/**
 * 
 * a simple facade class for the actual parser
 * could be realized as plugin with classloader for more flexibility
 * 
 * it creates the classes on startup, and references them through an object map
 * 
 */
public class Evaluator {

	SomSprite somSprite = null;
	
	Map<String,Object> functions = new HashMap<String,Object>() ;
	Map<String,String> expressions = new HashMap<String,String>() ;
	ArrayList<String>  xList = new ArrayList<String>();
	
	ArrayList<String>  variables = new ArrayList<String>() ;
	
	// ========================================================================
	public Evaluator(SomSprite somsprite){
		somSprite = somsprite;
	}
	
	public Evaluator(){
		
	}
	// ========================================================================
	
	public void clear(){
		functions.clear() ;
		expressions.clear() ;
		xList.clear() ;
		variables.clear() ;
	}
	// ------------------------------------------------------------------------
	class Eval{
		
		SpriteFuncIntf func;
		String expressionName;
		double[] arguments ;
		// ................................................
		public Eval(String expressionName, double... arguments){
			
			this.expressionName = expressionName;
			this.arguments = arguments;
		}
		// ................................................
		
		public Object go(){
			Object resultObj = (Object)SpriteFuncIntf.__MISSING_VALUE;
			
			try{


				func = (SpriteFuncIntf) functions.get(expressionName) ;
				
				if (func!=null){
					resultObj = func.calculate(arguments) ;
				}else{
					resultObj = null;
				}
				
			}catch(Exception e){
				String estr = ""+e.getMessage();
				if ((estr!=null) && (estr.contains("topNode parameter is null"))){
					estr = estr+".\n"+"You may check syntax and brackets of the expression!";
				}
				System.err.println(estr+"\nin method <Evaluator.eval()> while calling func.calculate(expression:<"+expressionName+">)..."); // expressionName
			}
			
			return resultObj;
		}
		
		
	} // inner class Eval
	// ------------------------------------------------------------------------
	/** 
	 * essentially the same as "eval()", the difference being just that it is encapsulated into 
	 * an object that is created adhoc: it is safer, but slower
	 *  
	 */
	public Object evalByObj(String expressionName, double... arguments){
		
		return (new Eval(expressionName, arguments)).go();
	}
	
	public Object eval(String expressionName, double... arguments){
	
		SpriteFuncIntf func;
		
		Object resultObj = (Object)SpriteFuncIntf.__MISSING_VALUE;
		
		try{


			func = (SpriteFuncIntf) functions.get(expressionName) ;
			if (func!=null){
				resultObj = func.calculate(arguments) ;
			}else{
				resultObj = null;
			}
			
		}catch(Exception e){
			String estr = ""+e.getMessage();
			if ((estr!=null) && (estr.contains("topNode parameter is null"))){
				estr = estr+".\n"+"You may check syntax and brackets of the expression!";
			}
			System.err.println(estr+"\nin method <Evaluator.eval()> while calling func.calculate(expression:<"+expressionName+">)..."); // expressionName
		}
		
		return resultObj ;
	}
	
	public String[] getArgumentsOfExpr(String expressionName){
		String[] args = new String[0];
		
		 
		SpriteFuncIntf func = (SpriteFuncIntf) functions.get(expressionName) ;
		
		if ((func != null) && (func.getVariables() != null)) {
			int n = func.getVariables().size();
			if (n > 0) {
				args = new String[n];
				for (int i = 0; i < func.getVariables().size(); i++) {
					args[i] = func.getVariables().get(i);
				}
			}
		}

		return args ;
	}

	public String  getExpression(String name){
		String  expr = "";
		 
		expr = expressions.get(name) ;
		
		return expr ;
	}
	
	public String  getExpression(int index){
		
		String xname ="";
		
		xname = xList.get(index);
		
		return xname;
	}
	
	/*
	
	
		func = new Add("a+b+c", false); // false: no exceptions
		func.setMissingValue(-1.011) ;
		
		func.allowVariablesCountMismatch(true); // of course, only if the number is too small!
		
		try {
			
			double v = func.calculate(2, 3,4 );
			System.out.println("v = "+v) ;
			
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		
		
	*/
	public void createFunction( String name, String expression){
		
		SpriteFuncIntf func;
		ArrayList<String> variables ;
		
		if ((expression.length()==0) || (name).length()==0){
			return;
		}
		
		if ((functions==null) || (functions.size()==0) || (functions.containsKey(name)==false)){

			func = new XFunction( expression );
			func.setName(name);
			variables = func.getVariables() ;
			
			functions.put(name, func) ;
			expressions.put(name, expression) ;
			xList.add(name) ;
			 
		}
	}

	
	public void createFunction( String name, String expression, FunctionCohortParameters fcp){
		
		SpriteFuncIntf func;
		 
		if ((expression.length()==0) || (name).length()==0){
			return;
		}
		
		func = new XFunction( expression, fcp);
		
		variables = func.getVariables() ;
		functions.put(name, func) ;
		expressions.put(name, expression) ;
		xList.add(name) ;
	}
	
	/**
	 * 
	 * 
	 * @param name
	 * @param intermediateVar
	 * @param expression
	 * @param avoidValues those values are excluded both for provided variables as well as for intermediate,
	 *        as it is necesary e.g. for log()
	 */
	public void createCompundFunction( String name, String intermediateVar, String expression, double[] avoidValues) {
		
		
	}

	// these functions are not used for dependency checking, but may be used by other functions
	public void createServiceFunction(String string, String string2) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}



/*


http://technojeeves.com/joomla/index.php/free/100-java-application-login-form
==================================================================================

quite interesting: using javascript as angine for evaluation expressions

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


public class Calc {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java Calc <expression>");
            System.exit(1);
        }

        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        System.out.println(engine.eval(args[0]));
    }
}


eval("g=function f(x) { ...code... }");
eval("g(2)");
... which is a kind of closure

*/