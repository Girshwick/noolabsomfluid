package org.NooLab.somfluid.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;


/**
 * 
 * simply handling loading / saving objects, (also XML files?) 
 * 
 */
public class ContainerStorageDevice {

	
	PrintLog out = new PrintLog (2,true) ;
	DFutils fileutil = new DFutils();
	
	// ========================================================================
	public ContainerStorageDevice(){
		
		out.setPrefix("[SomFluid-store]");
	}
	// ========================================================================	
	
	
	public ContainerStorageDevice(PersistenceSettings ps, FileOrganizer fileorg, int tableobject) {
		// TODO Auto-generated constructor stub
	}


	public int storeObject( Object dataObj, String filename) {
		int result=-1;
		
		FileOutputStream fileOut;
		BufferedOutputStream bout;
		ObjectOutputStream objout ;

		if (filename.length()==0){
			return -2;
		}

		
		
		if (fileutil.fileexists(filename) ){
			boolean hb = fileutil.manageBakFile( filename , 100, true, true);
		}

			
		try{
			
			 										out.print(5,"Writing object data ... "+filename);
			    fileOut = new FileOutputStream( filename );
			    bout = new BufferedOutputStream(fileOut);
	            objout = new ObjectOutputStream(bout);

	            									 
	            objout.writeObject( dataObj );

	            									 
	            objout.close();
	            bout.close();
	            fileOut.close();
	            result=0 ;
	            									 out.print(5,"output streams closed. ");
			    if (DFutils.fileExists(filename)==false){
			    	result = -3;
			    }
		}catch(Exception e){
			result = -7;
			e.printStackTrace();
		}
		return result ;
	}


	public Object loadStoredObject(String filename) {
		
		Object dataObj=null;
		
		int result=-1;
		 
		ObjectInputStream objistream = null ;
		FileInputStream fileIn = null ;
		BufferedInputStream bins = null ;

		try{
			

			if (fileutil.fileexists(filename)==false){
				return null ;
			}
			
			fileIn = new FileInputStream(filename);
			bins = new BufferedInputStream(fileIn);
			objistream = new ObjectInputStream(bins);

			dataObj =   objistream.readObject();
		 
												out.print(5, " Closing all input streams...");
			result = 0;
			
		}catch(Exception e){
			result = -3;
			dataObj=null;
			// e.printStackTrace(); 
			out.printErr(1, "Crashing File Read Error in loadStoredObject(), \n"+
							"file "+filename+"\n"+
							"message = "+e.getMessage()) ;
		}finally{
			try{

				if (objistream!=null) objistream.close();
				if (bins!=null) bins.close();
				if (fileIn!=null) fileIn.close();
		
			}catch(Exception e){
				dataObj=null;
			}
		}
		return dataObj;
	}


	 

}
