package org.NooLab.utilities.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.log4j.Level;


/**
 * 
 * a simple wrapper for getMethods()
 * 
 * useful (and used) for retrieving a list of methods that obey to certain constraints:
 *  - belonging to a particular class (by name) 
 *  - belonging to a class that implements a particular interface (by name)
 *  - name matches a simple pattern
 * 
 * we need it for instance, to get a list of all functions offered by an expression parser
 * 
 * 
 */
public class MethodsLister {

	// ========================================================================
	public MethodsLister(){
		
	}
	// ========================================================================	
	
	

	public void context___not_ready___(Object main){
		
		String str, intfName;
		int mint;
		boolean conditionsApply;
		
		
		Class<?> sclass, loadedclass ;
		ClassLoader classLoader; 
		Method[] mm;
		Method m;
		Class[] pcs ;
		Class<?>[] cc ;
		Field[] ff ;
		Field f;
		Class<?> ftyp ;
		
		
		sclass = main.getClass() ;
		
		classLoader = sclass.getClassLoader();
		
		try {
			// the package in which the <sclass> is homed...
			String context = "org.apache.ftpserver.impl";
			loadedclass = classLoader.loadClass(context+".DefaultFtpServer") ;
			
			mm = loadedclass.getMethods();
			cc = loadedclass.getDeclaredClasses();
			ff = loadedclass.getDeclaredFields() ;
			
			for (int i=0;i<mm.length;i++){
				m = mm[i] ;
			}

			for (int i=0;i<cc.length;i++){
				 
			}

			for (int i = 0; i < ff.length; i++) {
				str = ff[i].getName();
				
				conditionsApply = str.contains("LOG");
				
				conditionsApply = true;
				
				if (conditionsApply){
					f = ff[i];
					ftyp = f.getType();
					// = interface org.slf4j.Logger
					intfName = ftyp.getName();

					f.setAccessible(true);

					mint = f.getModifiers(); //18

					Field modifiersField;
					try {

						modifiersField = Field.class.getDeclaredField("modifiers");

						modifiersField.setAccessible(true);

						modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

						//f.set(null, Modifier.PUBLIC);
						// f.set(obj, value);
						
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			/*
			 * pcs = getParameterClasses(parameters); sclass = scope.getClass();
			 * 
			 * method = sclass.getMethod( methodName, pcs );
			 */
		} catch (ClassNotFoundException e) {
			 
			e.printStackTrace();
		}
		 
	}
	 
	
}
