package org.NooLab.utilities.net.pages;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UrlSimpleRead {
	
    public UrlSimpleRead(){
    	
    }
    
    //DataInputStream dis = new DataInputStream(yahooConnection.getInputStream());
    
    public static String retrieve( String urlstring){
    	
    	return retrieve( urlstring, false);
    }
    
    public static String retrieve( String urlstring, boolean textOnly ){
    	String rawPageStr = "";

    	rawPageStr = retrieveToString( urlstring );
    	
    	if (textOnly){
    		rawPageStr = rawPageStr.replaceAll("\\<.*?\\>", "");
    		rawPageStr = rawPageStr.replaceAll("\n\n","\n");
    	}
    	return rawPageStr ;
    }
    		
    private static String retrieveToString( String urlstring ) {
    	String rawPageStr = "";
    	BufferedReader reader;
     
    	URL url ;
    	String line;
    	
    	try {
            url = new URL( urlstring );
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            
            while ((line = reader.readLine()) != null) {
               // System.out.println(line);
            	if  (line.indexOf("\n")<0){
            		line = line + "\n";
            	}
            	rawPageStr = rawPageStr + line ;
            }
            
            
        } catch (MalformedURLException me) {
            System.out.println("MalformedURLException: " + me);
        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
        }
        
    	return rawPageStr ;
    }
    
    public int retrieveToFile( String urlstring, String targetFile) {
        int resultState = -1;
        
    	BufferedReader reader;
    	BufferedWriter writer ;
    	String line;
    	
    	if ((targetFile==null) || (targetFile.length()<=2)){
    		return resultState;
    	}
    	
    	try {
            URL url = new URL( urlstring );

            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            writer = new BufferedWriter( new FileWriter( targetFile ));
        
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                writer.write(line);
                writer.newLine();
            }

            reader.close();
            writer.close();
            resultState = 0;
            
        } catch (MalformedURLException e) {
        	resultState = -7;
            e.printStackTrace();
        }  catch (IOException e) {
        	resultState = -11;
            e.printStackTrace();
        }
        
        return resultState;
        
    } // retrieve( urlstring, targetFile)
    
    
    
}
 

/*
try {
            URL yahoo = new URL("http://www.yahoo.com/");
            URLConnection yahooConnection = yahoo.openConnection();
            DataInputStream dis = new DataInputStream(yahooConnection.getInputStream());
            String inputLine;

            while ((inputLine = dis.readLine()) != null) {
                System.out.println(inputLine);
            }
            dis.close();
        } catch (MalformedURLException me) {
            System.out.println("MalformedURLException: " + me);
        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
        }
*/