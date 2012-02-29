package org.NooLab.utilities.objects.example;



public class Reloader {

	public static void main(String[] args) 
											throws
    											ClassNotFoundException,
    											IllegalAccessException,
    											InstantiationException {
		ClassLoader parentClassLoader ;
		ReloadingClassLoader classLoader ;
		Class myObjectClass,myProgram ;
		
		MyObjectSuperClass object2 ;
		AnInterface2 object1 ;
		
		
		parentClassLoader = ReloadingClassLoader.class.getClassLoader();
		classLoader = new ReloadingClassLoader(parentClassLoader);
		myObjectClass = classLoader.loadClass("org.NooLab.utilities.objects.example.MyObject");

		object1 = (AnInterface2) myObjectClass.newInstance();

		object2 = (MyObjectSuperClass) myObjectClass.newInstance();

		// create new class loader so classes can be reloaded.
		classLoader = new ReloadingClassLoader(parentClassLoader);
		myObjectClass = classLoader.loadClass("org.NooLab.utilities.objects.example.MyObject");

		object1 = (AnInterface2) myObjectClass.newInstance();
		object2 = (MyObjectSuperClass) myObjectClass.newInstance();

		object1.perform(99) ;
		
		
	}
	
}
