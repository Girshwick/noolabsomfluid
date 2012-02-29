package org.NooLab.utilities.files;


import java.io.*;
  


public final class WriteFileSimple {

	
	public WriteFileSimple(String filename, String content){
		File file;
	    Writer writer = null;

	    try {
        
            file = new File(filename);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
        
	    } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

	  
}

