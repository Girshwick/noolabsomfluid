package org.NooLab.somfluid.storage;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

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
	
	
	public int storeObject( Object dataObj, String filename) {
		int result=-1;
		
		FileOutputStream fileOut;
		BufferedOutputStream bout;
		ObjectOutputStream objout ;

		if (filename.length()==0){
			return -2;
		}

		
		
		if (fileutil.fileexists(filename) ){
			boolean hb = fileutil.manageBakFile( filename , 10, true, true);
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

}
