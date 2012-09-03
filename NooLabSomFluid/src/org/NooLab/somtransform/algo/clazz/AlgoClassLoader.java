package org.NooLab.somtransform.algo.clazz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;



public class AlgoClassLoader extends ClassLoader {

	String classFilePath = "";

	public AlgoClassLoader(ClassLoader parent) {
		super(parent);
	}

	public Class loadClass(String name) throws ClassNotFoundException {

		String classFileName = "";
		String url;
		URL myUrl;
		URLConnection connection ;
		InputStream input ;
		ByteArrayOutputStream buffer ; 
		int data ;

		
		// if(!"reflection.MyObject".equals(name)){
		if (!"reflection.MyObject".equals(name)) {

			return super.loadClass(name);
		}

		try {
			// String url =
			// "file:C:/data/projects/tutorials/web/WEB-INF/classes/reflection/MyObject.class";
			
			url = "file:"+classFilePath+classFileName ;
			myUrl = new URL(url);
			connection = myUrl.openConnection();
			input = connection.getInputStream();
			buffer = new ByteArrayOutputStream();
			data = input.read();

			while (data != -1) {
				buffer.write(data);
				data = input.read();
			}

			input.close();

			byte[] classData = buffer.toByteArray();

			return defineClass("reflection.MyObject", classData, 0,
					classData.length);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}