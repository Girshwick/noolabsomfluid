package org.NooLab.repulsive.components;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
 
import org.NooLab.repulsive.RepulsionFieldCore;
import org.NooLab.repulsive.particles.Particle;
import org.NooLab.repulsive.particles.Particles;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;


/**
 * 
 * provides methods to store and load a fixed RepulsionField via
 * the serializable Object "FieldStorageContainer",
 * 
 * 
 * 
 * 
 */
public class Storage {

		
	RepulsionFieldCore rfield;
	RepulsionFieldProperties rfProperties;
	FieldStorageContainer fieldStorageContainer;
	
	
	String filename ="";
	String dataFieldFilename ="";
	
	DFutils fileutil = new DFutils();
	PrintLog out;
	
	
	public Storage( RepulsionFieldCore rf){
		
		rfield = rf;
		
		rfProperties = rfield.getRfProperties() ;
		
		out = rfield.out ;
		
		fieldStorageContainer = new FieldStorageContainer(this);
	}

	public void setFieldReference(RepulsionFieldCore rf) {
		rfield = rf;
		fieldStorageContainer.setFieldReference(rf);
	}
	
	public void actualizeFieldByImport() {
		
		int sbiCount=0, ix;
		FieldStorageContainer fsc;
		Particles particles;
		Particle particle;
		FieldAreaProperties areaProps;
		SurroundBuffer sbi;
		
		fsc = fieldStorageContainer;
		
		particles = rfield.particles ;
		particles.clear();
		
		int n = fsc.particleStore.size();
		particles.setNbrParticles( n );
			
		for (int i=0;i<n;i++){
			
			particle = fsc.particleStore.get(i); 
			 
			particles.add( particle );
		} // i-> all particles
		
		
		sbiCount = fsc.surroundBufferItems.size() ;
		
		// idd == n ?
		
		// 
		
		for (int i=0;i<sbiCount; i++){
			particle = particles.get(i);
			
			sbi = fsc.surroundBufferItems.get(i) ;
			
			ix = sbi.index ;
			// index OK? == i
			
			particle.importSurroundBuffer(sbi) ;
			
		} // i-> all IDD entries
		
		rfield.getSurroundBuffers().importBufferItems( fsc.surroundBufferItems ) ;
		
		areaProps = fsc.areaProperties ;
		
		
		rfield.setName( areaProps.name ) ;
		
		rfield.setAreaWidth( areaProps.areaWidth ) ;
		rfield.setAreaHeight( areaProps.areaHeight ) ;
		rfield.setNumberOfParticles( areaProps.numberOfParticles ) ;
		
		rfield.setDeceleration( areaProps.deceleration ) ;
		rfield.setEnergy( areaProps.energy ) ;
		rfield.setRepulsion( areaProps.repulsion ) ;
		rfield.setkRadiusFactor(  areaProps.kRadiusFactor ) ;
		
		rfield.setThreadcount ( areaProps.threadcount ) ;
		rfield.setDelayedOnsetMillis( areaProps.delayedOnsetMillis ) ;
		
		rfield.setMultiProc( areaProps.multiProc ) ;
		rfield.setSizefactor( areaProps.sizefactor ) ;
		rfield.setFreezingAllowed( areaProps.freezingAllowed ) ;
		rfield.setStepsLimit( areaProps.stepsLimit ) ;
		rfield.setSelectionSize( areaProps.selectionSize ) ;
		rfield.setUseOfSamplesForStatistics( areaProps.useOfSamplesForStatistics ) ;
		rfield.setColormode( areaProps.colormode ) ;
		 
		rfield.setAverageDistance( areaProps.averageDistance);
  		
		
	}

	

	public RepulsionFieldProperties getRfProperties() {
		
		return rfProperties;
	}





	// ========================================================================
	
	public int loadRepulsionFieldPropsData(){
		int result=-1;
		RepulsionFieldProperties rfprops;
		ObjectInputStream objistream = null ;
		FileInputStream fileIn = null ;
		BufferedInputStream bins = null ;
	
		try{
			
	
			if (fileutil.fileexists(filename)==false){
				filename = getFilename();
				if (fileutil.fileexists(filename)==false){
					return -1 ;
				}
			}
			
			fileIn = new FileInputStream(filename);
			bins = new BufferedInputStream(fileIn);
			objistream = new ObjectInputStream(bins);
	
			rfprops = (RepulsionFieldProperties) objistream.readObject();
												out.print(3, "started: connection to <MsgBoardDataStorage>, file: "+filename+"");
												out.print(5, "Closing all input streams...");
			result = 0;
			rfProperties = rfprops;
			
		}catch(Exception e){
			result = -3;
			e.printStackTrace();
		}finally{
			try{
	
				if (objistream!=null) objistream.close();
				if (bins!=null) bins.close();
				if (fileIn!=null) fileIn.close();
		
			}catch(Exception e){
			}
		}
				
		return result ;
	}





	public int saveRfProperties(RepulsionFieldProperties rfProps) {
	
		int result=-1;
		
		FileOutputStream fileOut;
		BufferedOutputStream bout;
		ObjectOutputStream objout ;

		rfProperties = rfProps;
		
		if (fileutil.fileexists(filename)==false){
			filename = getFilename();
			fileutil.writeFileSimple(filename, ".");
			if (fileutil.fileexists(filename)==false){
				return -1;
			}else{
				fileutil.deleteFile(filename) ;
			}
		}
	
		
		try{
			
			 										out.print(5,"Writing msgboard data-object physically (1)... ");
			    fileOut = new FileOutputStream( filename );
			    bout = new BufferedOutputStream(fileOut);
	            objout = new ObjectOutputStream(bout);

	            									 out.print(5,"Writing msgboard data-object physically (2)... ");
	            objout.writeObject( rfProperties );

	            									 out.print(5,"closing all output streams... ");
	            objout.close();
	            bout.close();
	            fileOut.close();
	            result=0 ;
	            									 out.print(5,"output streams closed. ");

			
		}catch(Exception e){
			result = -3;
			e.printStackTrace() ;
		}
		
		
		return result ;
	}





	public FieldStorageContainer getFieldStorageContainer() {
		return fieldStorageContainer;
	}


	public String getFilename(){
		
		filename = fileutil.createPath( rfProperties.configPath, "rfield.cfg") ;
		
		return filename;
	}
	
	public String getRFDataFilename(){
		String filename ="" ;
		String vstr ;
		
		
		
		if (dataFieldFilename.length()==0){
			vstr = rfield.getVersionStr().replace(".", "").trim();
			filename = "~RepulsionFieldData-"+rfield.getName().trim().replace(" ", "")+"-" + vstr+ ".dat";
			
			dataFieldFilename = filename;
		}
		
		if (dataFieldFilename.toLowerCase().contains( rfProperties.configPath.toLowerCase() )==false){
			if (fileutil.filenameIsUsable(dataFieldFilename)==false){
				dataFieldFilename = fileutil.createPath( rfProperties.configPath, dataFieldFilename) ;
			}
		}
		
		
		return dataFieldFilename;
	}
	
	public void setRFDataFilename(String filename){
		
	}
	
	
	// ========================================================================

	public int loadField(String filename) {
		// the actualization of the object by data is done by method "actualizeFieldByImport()" (see above)
		int r=-1;
		Object obj = readTransactionDataObj(filename);
		
		if (obj!=null){
			
			fieldStorageContainer = (FieldStorageContainer)obj;
			r=0;
			
		}
		
		return r;
	}
	
	public int storeField(String filename) {
		
		int r = storeTransactionDataObj(fieldStorageContainer, filename);
		
		return r;
	}
	
	public int loadCoordinates(String filename2) {
		 
		return -1;
	}





	private Object readTransactionDataObj( String filename) {
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
			out.printErr(1, "Crashing File Read Error in MsgBoardDataStorage.readTransactionDataObj(), \n"+
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
	
	private int  storeTransactionDataObj( Object dataObj, String filename) {
		int result=-1;
		
		FileOutputStream fileOut;
		BufferedOutputStream bout;
		ObjectOutputStream objout ;

 
		try{
			
			 										out.print(5,"Writing msgboard transaction data ... "+filename);
			    fileOut = new FileOutputStream( filename );
			    bout = new BufferedOutputStream(fileOut);
	            objout = new ObjectOutputStream(bout);

	            									 
	            objout.writeObject( dataObj );

	            									 
	            objout.close();
	            bout.close();
	            fileOut.close();
	            result=0 ;
	            									 out.print(5,"output streams closed. ");
			    if (fileutil.fileexists(filename)==false){
			    	result = -3;
			    }
		}catch(Exception e){
			result = -7;
			e.printStackTrace();
		}
		return result ;
	}















 
	
	
}
