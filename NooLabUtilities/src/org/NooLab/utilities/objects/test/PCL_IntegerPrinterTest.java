package org.NooLab.utilities.objects.test;

import org.NooLab.utilities.objects.PrivateClassLoader;

 

//refers to:  PrivateClassLoader
public class PCL_IntegerPrinterTest {

	  /**
     * This main method shows a use of our CustomClassLoader for
     * loading some class and running it. All the objects referenced
     * from the IntegerPrinter class will be loaded with
     * our CustomClassLoader.
     */
    public static void main(String[] args) throws Exception {
    	
    	PrivateClassLoader loader ;
    	Object instance ;
    	Class<?> clazz;
    	String path ;
    	
    	loader = new PrivateClassLoader( PCL_IntegerPrinterTest.class.getClassLoader());
        
    	// should not be hard-coded, instead, we should use getPackage() from class
    	// path = getPath(); this.getClass().getPackage() ;
    	// yet, "this" is not possible in a static context !!
    	
    	// path = this.getClass().getPackage().getName() ;
    	// System.out.println(""+path);
    	
    	clazz = loader.loadClass("org.NooLab.utilities.objects.test.PCL_IntegerPrinter");
        
    	instance = clazz.newInstance();
        clazz.getMethod("runMe").invoke(instance);
        
    }
    
 
    
}
