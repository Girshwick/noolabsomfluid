package org.NooLab.utilities.objects;


import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/*

see also about reloading of classes

http://aktuell.de.selfhtml.org/artikel/java/classloader/

http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html


*/

public class PrivateClassLoader extends ClassLoader {

    /**
     * Parent ClassLoader passed to this constructor
     * will be used if this ClassLoader can not resolve a
     * particular class.
     *
     * @param parent Parent ClassLoader
     *              (may be from getClass().getClassLoader())
     */
    public PrivateClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Loads a given class from .class file just like
     * the default ClassLoader. This method could be
     * changed to load the class over network from some
     * other server or from the database.
     *
     * @param name Full class name
     */
    private Class<?> getClass(String name)
        									throws ClassNotFoundException {
    	
        // We are getting a name that looks like
        // javablogging.package.ClassToLoad
        // and we have to convert it into the .class file name
        // like javablogging/package/ClassToLoad.class
        String file = name.replace('.', File.separatorChar) + ".class";
        
        byte[] b = null;
        try {
            // This loads the byte code data from the file
            b = loadClassData(file);
            // defineClass is inherited from the ClassLoader class
            // and converts the byte array into a Class
            Class<?> c = defineClass(name, b, 0, b.length);
            resolveClass(c);
            return c;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Every request for a class passes through this method.
     * If the requested class is in "javablogging" package,
     * it will load it using the
     * {@link CustomClassLoader#getClass()} method.
     * If not, it will use the super.loadClass() method
     * which in turn will pass the request to the parent.
     *
     * @param name
     *            Full class name
     */
    @Override
    public Class<?> loadClass(String name)
        									throws ClassNotFoundException {
    	
        	System.out.println("loading class '" + name + "'");
        
        	if (name.startsWith("javablogging.")) {
        		return getClass(name);
        	}
        	return super.loadClass(name);
    }

    /**
     * Loads a given file (presumably .class) into a byte array.
     * The file should be accessible as a resource, for example
     * it could be located on the classpath.
     *
     * @param name File name to load
     * @return Byte array read from the file
     * @throws IOException Is thrown when there
     *               was some problem reading the file
     */
    private byte[] loadClassData(String name) throws IOException {
        // Opening the file
    	InputStream stream ;
    	int size ;
    	byte[] buff;
    	DataInputStream inStream; 
    	
    	
    	stream = getClass().getClassLoader().getResourceAsStream(name);
        
        size = stream.available();
        buff = new byte[size];
        
        inStream = new DataInputStream(stream);
        // Reading the binary data
        inStream.readFully(buff);
        inStream.close();

        return buff;
    }
}

 
