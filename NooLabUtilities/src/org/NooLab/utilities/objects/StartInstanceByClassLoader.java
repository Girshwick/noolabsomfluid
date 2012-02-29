package org.NooLab.utilities.objects;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

/**
 * 
 *  this object creates a new classloader for each program to load/start,
 *  each classloader is running in a separate thread 
 *  
 *  In this way, each object has its own hermetically closed namespace in order
 *  to minimize interactions between objects
 *  
 *  this is important for instance in case of distributed concurrent and
 *  fault-tolerant systems
 * 
 *   
 *
 */
public class StartInstanceByClassLoader implements Runnable{

	Vector<ClassLoader> applicationSpaces = new Vector<ClassLoader>();
	
	Thread loaderThrd;
	Class<?> clazz ;
	String clazzname="",pathToObject="" ;
	
	PrivateClassLoader createdLoader;
	boolean starting = false, started=false;
	
	String firstMethod="" ;
	
	public StartInstanceByClassLoader(){
		
	}

	public PrivateClassLoader getLastCreatedLoader() {
		return createdLoader;
	}

	// guarantee, that it runs only once in a while
	synchronized public ClassLoader addSpace( Class<?> clasz, String pathtoobject){
		
		PrivateClassLoader cl = null;
		String name;
		
		

		clazz = clasz;
		pathToObject = pathtoobject;
		
		clazzname = clazz.getName() ;
		
		starting = false;
		started=false;
		
		loaderThrd = new Thread(clazzname) ;
		loaderThrd.start();
		
		int z=0;
		while ((started==false) && (z<500)){
			cl = createdLoader;
			delay(10); z++;
		}
		
		if (started==false){
			cl=null;
		}else{
			applicationSpaces.add(cl) ;
		}
		return (ClassLoader)cl;
	}
	
	
	@SuppressWarnings("static-access")
	private void delay(int millis){
	
		try{
			Thread.currentThread().sleep(millis) ;
		}catch(Exception e){}
	}
	
	private PrivateClassLoader createLoader() throws ClassNotFoundException, 
	                                                 InstantiationException, 
	                                                 IllegalAccessException, 
	                                                 IllegalArgumentException, 
	                                                 SecurityException, 
	                                                 InvocationTargetException, 
	                                                 NoSuchMethodException {
		
    	Object instance ;
		PrivateClassLoader loader = null;
		// clazz  like : PCL_IntegerPrinterTest.class , i.e. [classname.class], 
		// where [classname] is the name of the main class
		loader = new PrivateClassLoader( clazz.getClassLoader());
			
		String cn = clazzname;
		if (cn.contains(pathToObject)==false){
			cn = pathToObject+"."+clazzname;
		}
		clazz = loader.loadClass( cn);
		 	               //    "org.NooLab.utilities.objects.test" "." "PCL_IntegerPrinterTest"
		
    	instance = clazz.newInstance();
    	
    	Class<?>[] parameterTypes = new Class<?>[0];
    	Object[] parameters = new Object[0];
    	
    	if (firstMethod.length()>0){
			try {

				 clazz.getMethod(firstMethod).invoke(instance, parameters);
				 
			} catch (InvocationTargetException itx) {
				System.err.println("the method \"" + firstMethod + "\" can not be remotely invoked.");
			} catch (NoSuchMethodException nmx) {
				System.err.println("the method \"" + firstMethod + "\" does not exist.");
			} catch ( Exception e){
				e.printStackTrace();
			}
    	}else{
    		clazz.getMethod(firstMethod).invoke(instance);
    	}
		return loader;
	}
	
	public void run() {
		starting = true;
		started = false ;
		
		try {
			
			createdLoader = createLoader();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		starting = false;
		started = true;

	}

	public String getFirstMethod() {
		return firstMethod;
	}

	public void setFirstMethod(String firstMethod) {
		this.firstMethod = firstMethod;
	}

	
	
	
}
