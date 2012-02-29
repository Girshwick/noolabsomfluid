package org.NooLab.utilities.logging;

import java.io.FileInputStream;
import java.io.IOException;
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
}
