package org.NooLab.utilities.objects.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 
 * thats from Jakob Jenkov
 * http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html
 * 
 * the technique is rather important for plugins... 
 * 
 */
public class ReloadingClassLoader extends ClassLoader{

    public ReloadingClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
    	
    	String urlstr ;
    	URL myUrl ;
    	URLConnection connection ;
    	InputStream input ;
    	ByteArrayOutputStream buffer ;
    	
    	
    	
        if(!"reflection.MyObject".equals(name))
                return super.loadClass(name);

        try {
            urlstr = "file:C:/data/projects/tutorials/web/WEB-INF/classes/reflection/MyObject.class";
            myUrl = new URL(urlstr);
            
            connection = myUrl.openConnection();
            input = connection.getInputStream();
            buffer = new ByteArrayOutputStream();
            int data = input.read();

            while(data != -1){
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            return defineClass("reflection.MyObject", classData, 0, classData.length);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace(); 
        }

        return null;
    }

}

 