package org.NooLab.utilities.callback;

import java.lang.reflect.InvocationTargetException;



public class CallBackTest {
	
    
	/*
	 * this example does NOT demonstrate a callback, it just remotely invokes a method in another class
	 * 
	 * in order to be a true callback, the TestClass should call a method in the main class
	 */
    public void testCallBack() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        
    	CallbackFactory callBack ;
    	TestClass testClass ;
    	
    	testClass = new TestClass();
        callBack = new CallbackFactory(testClass, "hello");
       
        callBack.invoke();
        callBack.invoke("Fred");
    }

    class TestClass {
        public void hello() {
            System.out.println("Hello World");
        }

        public void hello(String name) {
            System.out.println("Hello " + name);
        }
    }
    
    // ==================================
    public class RealTestClass {
    	
    	public void perform(){
    		
    		 System.out.println("working..");
    	}
    }
}
