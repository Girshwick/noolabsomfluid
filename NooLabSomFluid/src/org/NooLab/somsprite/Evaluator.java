package org.NooLab.somsprite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

 
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

	SomSprite somSprite ;
	
	Map<String,Object> functions = new HashMap<String,Object>() ;
	Map<String,String> expressions = new HashMap<String,String>() ;
	ArrayList<String> xList = new ArrayList<String>();
	
	
	// ========================================================================
	public Evaluator(SomSprite somsprite){
		somSprite = somsprite;
	}
	// ========================================================================
	
	public double eval(String expressionName, double... arguments){
	
		SpriteFuncIntf func;
		double result = 0.0;
		
		try{
			
			func = (SpriteFuncIntf) functions.get(expressionName) ;
			result = func.calculate(arguments) ;
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		return result ;
	}
	
	public String[] getArgumentsOfExpr(String expression){
		String[] args = new String[0];
		
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
		 
		if ((expression.length()==0) || (name).length()==0){
			return;
		}
		
		func = new XFunction( expression );
		
		functions.put(name, func) ;
		expressions.put(name, expression) ;
		xList.add(name) ;
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