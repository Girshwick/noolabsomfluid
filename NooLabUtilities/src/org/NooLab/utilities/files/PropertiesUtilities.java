package org.NooLab.utilities.files;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;



public class PropertiesUtilities{

	
	
	/**
	 * Reads a "properties" file, and returns it as a Map 
	 * (a collection of key/value pairs).
	 * 
	 * @param filename  The properties filename to read.
	 * @param delimiter The string (or character) that separates the key 
	 *                  from the value in the properties file.
	 * @return The Map that contains the key/value pairs.
	 * @throws Exception
	 */
	public  Map<String, String> readPropertiesFileAsMap(String filename, String delimiter) throws Exception {
		
		int delimPosition;
		String key, value, line;;
		Map<String, String> map = new HashMap();
		BufferedReader reader ;
		
		
		reader = new BufferedReader(new FileReader(filename));
		
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() == 0)
				continue;
			if (isComment(line.trim().charAt(0)))
				continue;

			delimPosition = line.indexOf(delimiter);

			key = line.substring(0, delimPosition).trim();
			value = line.substring(delimPosition + 1).trim();

			map.put(key, value);
		}
		reader.close();
		return map;
	}
	
	private boolean isComment( char c ){
		// '#' ';'
		boolean rB = false;
		
		rB = (c=='#') || (c==';') || (c=='!');
		
		return rB;
	}

}
