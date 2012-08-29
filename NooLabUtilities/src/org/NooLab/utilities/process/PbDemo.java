package org.NooLab.utilities.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;

public class PbDemo {

 
    public static void main(String [] args) throws IOException {
    
    	cmd();
    }
    
    /*
     * Runtime.getRuntime().exec("cmd /c start excel.exe");
     * 
     * String fileName = "c:\\temp\\xls\\test2.xls";
        String[] commands = {"cmd", "/c", "start", "\"DummyTitle\"",fileName};
        Runtime.getRuntime().exec(commands);
     * 
     * 
     * Process p=Runtime.getRuntime().exec("javaw -jar D:\\NetBeansProjects\\GetIPAddress\\dist\\GetIPAddress.jar");
     * 
     * 
     * 
     * see jlibs
     * 
     */
    
    public void input(){

    	        try {
    	            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "leaptest.sh");
    	            final Process process = pb.start();

    	            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    	            PrintWriter pw = new PrintWriter(process.getOutputStream());
    	            String line;

    	            while ((line = br.readLine()) != null) {
    	                System.out.println(line);
    	                pw.println("1997");
    	                pw.flush();
    	            }
    	            System.out.println("Program terminated!");
    	        } catch(Exception e) {
    	            e.printStackTrace();
    	        }

    }
    public static void cmd() throws IOException{    
        String[] command = {"CMD", "/C", "dir"};
        ProcessBuilder probuilder = new ProcessBuilder( command );

        //You can set up your work directory
        probuilder.directory(new File("c:/temp"));
        
        Process process = probuilder.start();
        
        //Read out dir output
        InputStream is = process.getInputStream();
        
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        
        System.out.printf("Output of running %s is:\n",  Arrays.toString(command));
        
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        
        //Wait to get exit value
        try {
            int exitValue = process.waitFor();
            
            System.out.println("\n\nExit Value is " + exitValue);
        } catch (InterruptedException e) {
           
            e.printStackTrace();
        }
    }
}