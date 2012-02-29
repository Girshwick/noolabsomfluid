package org.NooLab.utilities.callback;

import java.lang.reflect.InvocationTargetException;


public class RealCallBackTester {

	static CallBackTest_2 testinstance;
	
	public static void main(String[] args) {
		 testinstance = new CallBackTest_2();
	
	}
}


class CallBackTest_2 {
	
	
	public CallBackTest_2() {
		try {

			testCallBack();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * this example does NOT demonstrate a callback, it just remotely invokes a method in another class
	 * 
	 * in order to be a true callback, the TestClass should call a method in the main class,
	 * e.g. 
	 */
    public void testCallBack() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        
    	CallbackFactory call, callback  ;
    	TestClass testClass ;
    	
    	testClass = new TestClass();
        
    	call = new CallbackFactory( testClass, "hello");
    	call.invoke();
    	call.invoke("Fred");

    	// note that we have three different parameter signatures for the method performCore()!!
    	// which is resolved by the callback factory (despite the serious problems of java with primitives like int/Integer)!
    	testClass.setReference( this, "performCore") ;
    	
    	// of course, we can invoke the remote method directly,... as said, the above is not a callback... 
    	testClass.perform("2") ;
    	
    	
    	 
    }

	public void performCore(){
		System.out.println("being lazy...");
	}
	
    // this method is now really called back from the child process;
    // of course, in order to do so, the dependent class needs some reference
	public String performCore(int c, String s){
		String str;
		
		str = ""+c+":"+s ;
		System.out.println("The center is working hard on ("+str+")!");
		
		str = "42:"+s+"\\"+s ;
		return str;
	}
	public void performCore(String s, int c){
		System.out.println("The center is working extra hard on ("+s+":"+c+")!");
	}

}

class TestClass {

	Object mainClass ;
	String methodName;

	// on defining the reference here (=compile time), we do NOT need to know about 
	// the type of the hosting class, where the method is contained
	public void setReference( Object hostclass, String methodname){
		mainClass = hostclass;
		methodName = methodname ;
	}
	
    public void hello() {
        System.out.println("Hello World");
    }

    public void hello(String name) {
        System.out.println("Hello " + name);
    }
    
    
	public void perform(String c){
		CallbackFactory indirectCall ;
		int v;
		String str;
		
		System.out.println(""+c+" : working...");
		
		try { 
			v = Integer.parseInt(c) ;
			
			indirectCall = new CallbackFactory( mainClass , methodName);

			System.out.print("\nsignature 1 :   ");
			// this now should call performCore(), which is doing sth additional with our data
			str = (String)indirectCall.invoke(v,"abc");
			// note, that the factory has to select the correct method
			
			System.out.println( "                results on caller's side : "+str);

	  	     System.out.print("\nsignature 2 :   ");
			indirectCall.invoke();
			
			System.out.print("signature 3 :   ");
			indirectCall.invoke("xyz", 5);
			/*
			 * all together, the trick with reflection allows to avoid extra interfaces;
			 * a drawback of this is, that only String variables seem to work 
			 * 
			 */

		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} 
	}
	
}

// 