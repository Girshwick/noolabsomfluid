package org.NooLab.utilities.logging;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class AppProperties {

	
	public static void test(){
		Logger log = Logger.getLogger("MyClass");
		try {
			FileInputStream fis = new FileInputStream("p.properties");
			LogManager.getLogManager().readConfiguration(fis);
			log.setLevel(Level.FINE);
			log.addHandler(new java.util.logging.ConsoleHandler());
			log.setUseParentHandlers(false);

			log.info("starting myApp");
			fis.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	// load from the class path
    public static Properties load(String propsName) throws Exception {
        Properties props = new Properties();
        URL url = ClassLoader.getSystemResource(propsName);
        props.load(url.openStream());
        return props;
    }
    
    
    public Properties loadFromStartupFolder(String propsName) throws IOException{
    	
    	java.util.Properties props = new java.util.Properties();
    	String path = getClass().getProtectionDomain().getCodeSource().
    	   getLocation().toString().substring(6);
    	java.io.FileInputStream fis = new java.io.FileInputStream(new java.io.File( path + "/"+propsName));
    	props.load(fis);
    	fis.close();
    	
		return props;
    }
    
}
