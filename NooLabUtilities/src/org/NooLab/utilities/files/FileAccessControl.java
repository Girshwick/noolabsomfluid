package org.NooLab.utilities.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.NooLab.utilities.net.GUID;


/**
 * 
 * this class provides means to control the access to a file from different
 * processes, and even different JVMs, beyond  java.nio.channels.FileLock
 * 
 * there is also the option to wait (for a particular amount of time), or to return
 * 
 * requests to access the file are handled as a FiFo 
 *
 * the provided filename
 *
 */
public class FileAccessControl extends Observable {
	// =================================

	// object references ..............

	public static final int FAC_granted  = 0;
	public static final int FAC_denied   = -17;
	public static final int FAC_locked   = 7;
	public static final int FAC_valid    = 5;
	public static final int FAC_sharedP  = 10;
	public static final int FAC_expired  = 20;

	String lockFileName = "~lck-";
	
	// main variables / properties ....

	File faccFile;
	
	Map<String,Object> locks = new HashMap<String,Object>() ;
	
	
	// constants ......................
	
	

	// volatile variables .............
	Lock lock=null;
	
	
	// helper objects .................
	 


	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	public FileAccessControl( String filename , long expectedDuration){
		init(filename);
	}
	
	public FileAccessControl( String filename ){
		init(filename);
	}
	
	private void init( String filename ){
		File file;
		 
		String guid , path;
		file = new File(filename);
		
		if (file.exists()==false){
			
		}else{
			// file:: -> /hibernation/receptor/xrcatalog.xml
			if ((file.isFile()) && (file.isDirectory()==false)){
				path = file.getParent() ;
			}else{
				path = filename;
			}
			path = path.replace("\\", "/");
			path = path + "/" ;
			
			filename = file.getName() ;
			
			guid = GUID.randomvalue() ;
			lock = new Lock(guid,path,filename);
			if (lock.granted() ){
				faccFile = file;
			}else{
				faccFile=null;
			}
		}
		
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	
	public File getFile(){
		return faccFile;
	}
	
	public int getLockState(){
		return 0;
	}
	
	public void close(){
		try{
			if (lock!=null){
				lock.release();
				faccFile = null;
			}
		}catch(Exception e){
			e.printStackTrace() ;
		}
	}
	
}

// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


class Lock implements Serializable{
  
	private static final long serialVersionUID = 898858970529609020L;

	transient String lockFileName = "~lck-";
		
	String guid  ;
	 
	boolean valid  = true ;
	boolean shared = false ;
	
	boolean lockIsGranted = false;
	
	long startTime = System.currentTimeMillis() ; 
	
	int lockState = -99;
	
	transient String fullpath ;
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	public Lock(String guid, String path, String filename){
		 
		this.guid = guid;
		  
		lockIsGranted = manage(path, filename);
		
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public boolean granted(){
		return lockIsGranted ;
	}
	private boolean manage(String path, String filename) {
		File file;
		boolean hb=true;
		long locktime,td;
		Lock localLock=null;
		
		fullpath = (path + "/" + filename).replace("//", "/");
		file = new File(fullpath);
		if (file.exists() == false) {
			return true;
		}

		fullpath = (path + "/" + lockFileName + serialVersionUID).replace("//","/");
		file = new File(fullpath);
		if (file.exists()) {
			hb = false;

			// is it expired/shared/invalid -> hb:=true
			
			localLock = load(fullpath) ;
			locktime = localLock.startTime ;
			td = (System.currentTimeMillis()-locktime) ;
			
			if (td>1000){
				hb = true;
				file.delete() ;
			}
		}
		
		if (hb){
			createLock(fullpath);
		}
		if (localLock!=null){
			locktime = localLock.startTime ; // just for debug
		}
		return hb;
	}
	
	private void createLock( String lockfilename ){
		save(lockfilename, this);
	}
	
	private void save( String filename, Object obj){
		
		writeFileFromObject( filename, obj);
		
	}
	
	private Lock load(String filename){
		return (Lock)readObjectFromFile(filename) ;
	}
	
	
	private int writeFileFromObject( String filename, Object serializableObj){
		int result = -1;
		
		FileOutputStream fileOut;
		BufferedOutputStream bout;
		ObjectOutputStream objout ;
		
		try{
			
			fileOut = new FileOutputStream(filename);
			bout = new BufferedOutputStream(fileOut);
			objout = new ObjectOutputStream(bout);

			objout.writeObject(serializableObj);

			try{
				objout.close();
				bout.close();
				fileOut.close();
			}catch(Exception e){
			}
			 
			
		}catch(Exception e){
			result = -3;
			e.printStackTrace() ;
		}
		return result ;
	}
	
	private Object readObjectFromFile( String filename){
		Object dataObj=null;
		
		int result=-1;
		 
		ObjectInputStream objistream = null ;
		FileInputStream fileIn = null ;
		BufferedInputStream bins = null ;

		try{
			 
			fileIn = new FileInputStream(filename);
			bins = new BufferedInputStream(fileIn);
			objistream = new ObjectInputStream(bins);

			dataObj =   objistream.readObject();
												
			result = 0;
			
		}catch(Exception e){
			result = -3;
			dataObj=null;
			// e.printStackTrace();
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
	
	public void release(){
		File file;
		file = new File(fullpath);
		if (file.exists()) {
			file.delete() ;
		}
	}
	
	public boolean isShared(){
		return false;
	}
    
	public boolean isValid(){
		return true;
	}


	public void setValid(boolean valid) {
		this.valid = valid;
	}


	public void setShared(boolean shared) {
		this.shared = shared;
	}


	public String getGuid() {
		return guid;
	}


	public void setGuid(String guid) {
		this.guid = guid;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public int getLockState() {
		return lockState;
	}


	public void setLockState(int lockState) {
		this.lockState = lockState;
	}
}

/*

FileInputStream in = new FileInputStream(file);
try {
    java.nio.channels.FileLock lock = in.getChannel().lock();
    try {
        Reader reader = new InputStreamReader(in, charset);
        ...
    } finally {
        lock.release();
    }
} finally {
    in.close();
}

==============================================================

try {
    // Get a file channel for the file
    File file = new File("filename");
    FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

    // Use the file channel to create a lock on the file.
    // This method blocks until it can retrieve the lock.
    FileLock lock = channel.lock();

    // Try acquiring the lock without blocking. This method returns
    // null or throws an exception if the file is already locked.
    try {
        lock = channel.tryLock();
    } catch (OverlappingFileLockException e) {
        // File is already locked in this thread or virtual machine
    }

    // Release the lock
    lock.release();

    // Close the file
    channel.close();
} catch (Exception e) {
}


=======================
Release Java file lock in Windows

This is a known Bug in Java on Windows, please see Bug #4715154

Just write to ByteArrayOutputStream the usual way. 
When finished, just call its toByteArray() method to grab the byte[]

System.gc() 
	


*/