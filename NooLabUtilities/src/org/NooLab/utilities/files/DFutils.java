package org.NooLab.utilities.files;

 

import java.io.*;

import java.nio.*;

import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

//javax.swing.filechooser.FileSystemView;
// import javax.swing.filechooser.*;

 

import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.datetime.DateTimeValue;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.strings.*;


import org.apache.commons.lang3.*;

// import org.apache.commons.lang.StringUtils;

// LOC 800

// http://www.torsten-horn.de/techdocs/java-io.htm


/*
 * our FileAccessControl allows to use a customizable "lock" for writing  
 * 
 */



public class DFutils extends Thread{
    private static String convertedString ;
	
    Vector<String> nonDataSection ;
    
    Vector<Vector<String> >  nonDataSections = new Vector<Vector<String> >();
    Map<String, Integer> nonDataSectionsMap = new HashMap<String, Integer>();
    
    
    static StringsUtil strgutil = new StringsUtil();
    DirectoryContent dirutil = new DirectoryContent();
    
    // org.NooLab.utilities.logging.PrintLog out ;
    PrintLog  out =  new PrintLog(2,true) ;
    
    
	public void setOut( org.NooLab.utilities.logging.PrintLog outprn) {
		// needs a minimal interface...
		// out = outprn ;
	}


	public String correctWinPath( String path) {
		    byte bytearray[]  = path.getBytes();
		    CharsetDecoder d = Charset.forName("US-ASCII").newDecoder();
		    
		    String return_str="";
		    int i;
		    CharBuffer r;
		    
		    try {
		    	
		      r = d.decode(ByteBuffer.wrap(bytearray));
		      convertedString = r.toString();
		      
		      return_str = convertedString; 
		      
		      for (i=0;i<r.length();i++){
		    	  
		      }
		    }
		    catch(CharacterCodingException e) {
		      return "";
		    }
		    return return_str;
		  }
		  
	  
	  private boolean isDriveLetter(String _path){
		  
		  
		return false;  
	  }
	  
	  private boolean DriveofPathexists(String _path){
		  
		  String _OS = System.getProperty("os.name");
		  return DriveofPathexists(_path, _OS);
	  }
	  
	  private boolean DriveofPathexists(String _path, String oSys){
		  boolean return_value=false;
		  String _drive="",hs1;
		  
		  int i;
		  File[] f ;
		  
		  // get a list of drives 
		  //
		  
		  // FileView fileview = new FileView();

		  oSys = oSys.toLowerCase() ;
		  if (oSys.contains("mac")){
			  
			  
		  }else{
			  if (oSys.contains("win")){
				  
				  
			  }else{
				  
				  _drive = "/";
				  
				  return_value = (_path.startsWith(_drive)) ;
				  return return_value;
					  
			  }
		  }
		  
		  
		  _drive = _path.substring(0,2);
		  
		  try{
			  
			  File drvfil = new File(_drive);
			  _drive = drvfil.getPath(); // AbsolutePath();
			  
			  hs1 = drvfil.getCanonicalPath();
			  
			  if ((_path.indexOf(":")<0) && (oSys.contains("win"))){
				  return true;  // if there is no drive, we can not be wrong...
			  }
			
		  }catch(IOException e){
			  
		  }
		  
		  f = File.listRoots();
		  
		  for (i=0;i<f.length;i++) {
		      hs1 = f[i].getPath();
		      
		      hs1 = (String) hs1.subSequence(0,2);
		      if (hs1.contentEquals(_drive)){
		    	 
		    	 return_value = true;
		    	 break; 
		      }
		      // exists());
		  }
		     
		// JCIFS for files in a network  
		  
		  return return_value;
	  }
	  
	  private byte[] resizeArray( int _new_size,
	                            byte[] original) {
		
		    int length ;
		    byte[] newArray ;
		    
		    if (original==null){
		    	return null ;
		    }
		    length = original.length;
		    newArray = new byte[_new_size];
		    if (_new_size>0){
			      if ( length==_new_size ){
			    	  newArray = original;
			      }
			      else {
			    	  System.arraycopy(original, 0, newArray, 0, length);
			      }
		    }
		    else
		    {
		    	
		    	newArray=null;
		    }
		    return newArray;
		  }
	
	
	private byte[] inserttoByteArrayAt( int p,
	                                    byte[] insertedbytes,
	                                    byte[] original){
		
		byte[] _arr; 
		int z,sz,i;
		     
		z = insertedbytes.length;
		sz = original.length;
		
		_arr = resizeArray( sz+z, original);
		
		for (i=sz;i>p;i--){
			_arr[i] = _arr[i-1];
			
		}
		for (i=0;i<z;i++){
			_arr[i+p]=insertedbytes[i];
		}
		
		return _arr;
	}
	  

	@SuppressWarnings("unused")
	private String PathBytecleaning(String suggestedFilepath){
		String 		hs1,hs2,hs3,return_Str="";
		int 		_initial_Length,k,i,b;
		CharBuffer 	r = null;
	
		byte[] 		bytearray = suggestedFilepath.getBytes(),corrected_bytes; 
		byte  		Byte;
	    CharsetDecoder d = Charset.forName("US-ASCII").newDecoder();
		
		
		 hs3="";
       k=0;
       _initial_Length = suggestedFilepath.length();
        
       return_Str = suggestedFilepath;
       hs1 = suggestedFilepath;
       
       i=0;
       b=bytearray.length;
       while (i<bytearray.length){
       	b=bytearray.length;
       	if (bytearray[i]==0){break;}
       	if (bytearray[i]<32){
       		if (bytearray[i]==9){ 
       			hs1="\\t";
       			corrected_bytes = hs1.getBytes();
       			bytearray = inserttoByteArrayAt( i,corrected_bytes,
       			                                 bytearray);
       			k=k+1;
       		}
       		else{
       			 
       			hs1="\\.";
       			corrected_bytes = hs1.getBytes();
       			corrected_bytes[corrected_bytes.length-1]=(byte)(48+bytearray[i]);
       			bytearray = inserttoByteArrayAt( i,corrected_bytes,
       			                                 bytearray);
       			k=k+1;
       		}

       	}
       	else{
       		
       	}
       	
       	if (i>_initial_Length+k+1){
       		break;
       	}
       	i++;	
		}
       

    	try {
	            r = d.decode( ByteBuffer.wrap(bytearray));
       }
       catch (CharacterCodingException e) {
	            
	            e.printStackTrace();
	            
       }
       finally{
       	
       }
       
      if (r!=null){ 
      	hs1 = r.toString();
      	if (hs1.length()>0){
      		hs1 = hs1.trim().replace("\\","/");
      	}
      }
      
		return_Str=hs1.replace("::",":");
          
					
		return return_Str;
	}
	
	/**
	 * 
	 * if the suggestedFilepath does not contain a drive, or if the path is
	 * invalid on a particular OS (e.g. due to a drive letter), then the path is adapted
	 * according to the parameter "dirselectMode"
	 * 
	 * 1 = user_home + suggested path
	 * 2 = temp_dir
	 * 3 = dir of classloader = dir of jar + suggested path
	 * 4 = root drive + suggested path
	 * 
	 * @param suggestedFilepath
	 * @param dirselectMode
	 * @return
	 */
	@SuppressWarnings("unused")
	public String prepareFilepath( String suggestedFilepath , int dirselectMode){

		String 	str,return_value="", returned_path="";
		String 	separator,system_pathsep,_OS,javalibPath, _basepath_of_suggestion="" ,
				_absolutePath,hs1 ,hs2,hs3,_path = "",
				user_dir, user_home, temp_dir,_name;
		boolean isWindows=false, isMac=false, isLinux=false;
		int 	i,pw,pw2,pm,p,b,k;
		File 	file,dir;
		 
			 
			
		if (suggestedFilepath.length()==0){ return "";}
		try{
			
			
			suggestedFilepath = suggestedFilepath.replace("\\", "/");
			hs3="";

		 
			
			pw = suggestedFilepath.indexOf(":\\");
			pw2 = suggestedFilepath.indexOf(":/");
			pw2 = suggestedFilepath.indexOf("\\");
				if (pw<=0){
					pw = suggestedFilepath.indexOf("\\");
				}
			if ((pw>0) || (pw2>0)){isWindows=true;}
			
			pm = suggestedFilepath.indexOf("/");
			if ((pm>=0) && (isWindows==false)){isMac=true; };
			
			javalibPath = System.getProperty("java.library.path"); // List of paths to search when loading libraries

			// ":" on MAC, ";" on Windows
			// obviously, this refers to the environment path variable
			system_pathsep = System.getProperty("path.separator"); 
			
			_OS = System.getProperty("os.name");
		 
			if (_OS.toLowerCase().contains("windows")){
				isWindows = true;isMac=false;
			}
			else{
				isMac = true; isWindows = false;
				if ((_OS.toLowerCase().contains("linux")) || (_OS.toLowerCase().contains("unix"))){
					isLinux = true;
				}
			}
			//file = new File("", suggestedFilepath);
			file = new File(suggestedFilepath);

			  _absolutePath = ""; // file.getAbsolutePath();

			 
			user_home= System.getProperty("user.home"); 		//  User's home director)
			// windows :  C:\\Dokumente und Einstellungen\\Administrator
			// Mac     :  /Users/kwa
			// linux   :  /home/user
			
			user_dir = System.getProperty("user.dir"); 			// User's current working directory 
			// sth like:  E:\\java\\workspace\\SensoryDataFeeder\\bin
			//   Mac   :  /Applications
			// linux   :  /home/user/dev/java/workspace/_testers_glue/bin
			temp_dir = System.getProperty("java.io.tmpdir"); 	// systems temp dir
			// sth like   C:\\Temp\\ on Windows
			//    Mac  :   /var/folders/Y4/Y4asdjhsdf56/-Tmp-/   
			//  linux  :  /tmp


          hs1 = PathBytecleaning(suggestedFilepath);
          if (hs1.length()>0){
          	hs1 = hs1.replace("//","/") ;
          }
			hs2=hs1;
			
			suggestedFilepath =hs1;
			
			if (hs1.contentEquals(hs2)==false){
				file = new File("", suggestedFilepath);
			}

			if (_OS.indexOf("Windows")>=0){
				_basepath_of_suggestion ="";
				hs1 = ":";
				File filevar = new File(suggestedFilepath) ;
				boolean hb;
				
				
				hb = (DriveofPathexists(suggestedFilepath, _OS)==false);
				if (hb==false){
					// hb = (_OS.toLowerCase().contains("windows")) && (suggestedFilepath);
				}
				
				if ((hb==false) && (dirselectMode>0) && (_OS.toLowerCase().contains("win"))){
					suggestedFilepath = suggestedFilepath.substring(3, suggestedFilepath.length()) ;
					hb = true;
				}
				if (hb){
					// dependent on mode: 
					
					// 1 = user_home + suggested path
					// 2 = temp_dir
					// 3 = dir of classloader = dir of jar + suggested path
					// 4 = root drive + suggested path
					if (dirselectMode<=1){
						suggestedFilepath = user_home + "/"+suggestedFilepath; 
					}
					if (dirselectMode==2){
						suggestedFilepath = temp_dir + "/"+suggestedFilepath;
					} 
					if (dirselectMode==3){
						
						str = ClassLoader.getSystemClassLoader().getResource(".").getPath();
						// str = RecInstance.class.getClassLoader().getResource(".").getPath() ;
						 
						p = suggestedFilepath.lastIndexOf("/") ;
						if (p>0){
							suggestedFilepath = suggestedFilepath.substring(p+1, suggestedFilepath.length()) ;
						}
						suggestedFilepath = str + "/"+suggestedFilepath;
						
					} 
					if (dirselectMode>=4){
						suggestedFilepath = "C:/" + suggestedFilepath.substring(3,suggestedFilepath.length()).trim();	
					} 
					suggestedFilepath = suggestedFilepath.replace("//", "/");
					
				}
				
				// hs1 = file.getPath();
				
				// hs1 = file.getName();
				
				hs2 = File.separator ;
				if (hs1.contentEquals("\\")){
					hs1 = hs1.replace("\\","/");
				}
				p = hs1.lastIndexOf("/");
				k = hs1.indexOf(":"); // is a drive letter present ?
					if (k<0){
						k = hs1.indexOf("\\\\"); // a server indication
					}
					if (k<0){
						k = hs1.indexOf("//"); // a server indication
						if (k>1){k=-1;}
					}
				if ((p>=0) && (k>0)){ // is it a complete path ? 
					_basepath_of_suggestion = hs1.substring(0, p);
					_path = suggestedFilepath;
					return_value = _path;
				}
				else{
					
					_absolutePath = file.getAbsolutePath().replace("\\","/");;
					_basepath_of_suggestion = _absolutePath;


					
					if (suggestedFilepath.indexOf(":/")<0){
						try {
		                    _path=file.getCanonicalPath();
		                    if (_path.length()>0){
		                    	_name = file.getName();
		                    	p = _path.lastIndexOf(_name);
		                    	if (p>=0){
		                    		_path = _path.substring(0,p-1);
		                    	}
		                    	
		                    }
	                    }
	                    catch (IOException e) {
		                    // TODO Auto-generated catch block
		                    e.printStackTrace();
	                    }						
						
					}
					else{
						_path = suggestedFilepath;
					}
					
					if ((_path.length()==0) ||(_path.indexOf(":")==0)){
						_path = user_home ;
					}
					
					_basepath_of_suggestion = _path;
					
					dir = new File(_basepath_of_suggestion);
					if ( (dir.isFile()) || (!dir.isDirectory())){
						_basepath_of_suggestion = dir.getParent();
						 
					}
					 
					str = file.getAbsolutePath(); str = str.replace("\\", "/") ;
					if ( (str.contentEquals(_basepath_of_suggestion)) || 
						 ( (str+"/").contentEquals(_basepath_of_suggestion)) ){
						returned_path = _basepath_of_suggestion;
					} else{
						if (file.isDirectory()==false){
							returned_path = _basepath_of_suggestion+"/"+file.getName();
						}
					}
					return_value = returned_path;
					
					File fil = new File(return_value);
					return_value = fil.getAbsolutePath();
					
					// return_value = return_value.replace("\\","/");
				}
				
				
				if ( isDriveLetter(_basepath_of_suggestion)==true){
					
				}

			
				// does path exist? 
				dir = new File(_basepath_of_suggestion);
				if (dir.exists()==false){
					dir.mkdirs();
				}
				
			}// end if Windows
			
			
			
			if (isMac){
				
				_basepath_of_suggestion ="";
				hs1 = suggestedFilepath;
				
				// we may encounter a windows path here...
				// so we convert the drive letter into a directory name
				
				pw = suggestedFilepath.indexOf(":\\");
					 if (pw<0){
						 pw = suggestedFilepath.indexOf("\\\\");
						 if (pw<0){
							 pw = suggestedFilepath.indexOf(":/");
						 }	 
					 }
						
				if (pw>0){
					
					/*
					suggestedFilepath = suggestedFilepath.replace(":\\","/");
					suggestedFilepath = suggestedFilepath.replace("\\\\","/");
					suggestedFilepath = suggestedFilepath.replace(":/","/");
					*/
					suggestedFilepath = suggestedFilepath.substring(pw+2,suggestedFilepath.length()) ;
				}

				
				if (suggestedFilepath.indexOf(user_home)<0){
					_path = user_home+ File.separator +suggestedFilepath  ;
					
					_path = _path.replace("/./","/");	
					_path = _path.replace("//","/");
					
					suggestedFilepath= createPath(_path,"/"); // ensures, that the path exists
				}
			    // now sth like: /home/user/dev/java/data/test/mb/config/
				// note, that the "D:/" from a windows path has been replaced by "/home/user/" 
				file = new File(suggestedFilepath);
				_path = file.getPath();
				_path=file.getCanonicalPath();
				
				hs2 = "/bin";
				if (_path.indexOf(hs2)>=0){
					_path="";
				}
				
				if (_path.length()==0){
					_path = user_home + File.separator +  _path ;
				}

				hs1= file.getName();
				p= _path.lastIndexOf(hs1);
				if (p>=0){
					_path=_path.substring(0,p-1);
				}
				
				// does path exist? 
				dir = new File(_path);
				
				if (dir.exists()==false){
					dir.mkdirs();
				}
				
				return_value = _path+ File.separator + hs1;
			}

		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			
		}
		
		return return_value; 
	}
	
	public ArrayList<String> listOfSubDirectories( String basePath , String nameFilter, boolean fullPath){
		return listOfSubDirectories( basePath , nameFilter, fullPath, false) ;
	}
	
	public ArrayList<String> listOfSubDirectories( String basePath , String nameFilter, boolean fullPath, boolean recursive){
		
		ArrayList<String> folderList= new ArrayList<String> ();
		String fstr ;
		
		File[] files = dirutil.getSubDirectories(nameFilter, basePath, recursive);
		
		for (int i=0;i<files.length;i++){
		
			if (fullPath){
				fstr = files[i].getAbsolutePath() ;
			}else{
				fstr = files[i].getPath();
				fstr = files[i].getName();
			}
			
			if (fstr.length()>0){
				fstr = StringsUtil.replaceall(fstr, "\\", "/") ;
 				folderList.add(fstr);
			}
		}
		
		
		return folderList;
	}
	
	private void _listOfSubDirectories(){
		
		File dir = new File("directoryName");

		String[] children = dir.list();
		if (children == null) {
		    // Either dir does not exist or is not a directory
		} else {
		    for (int i=0; i<children.length; i++) {
		        // Get filename of file or directory
		        String filename = children[i];
		    }
		}

		// It is also possible to filter the list of returned files.
		// This example does not return any files that start with `.'.
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return !name.startsWith(".");
		    }
		};
		children = dir.list(filter);


		// The list of files can also be retrieved as File objects
		File[] files = dir.listFiles();

		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
		    public boolean accept(File file) {
		        return file.isDirectory();
		    }
		};
		files = dir.listFiles(fileFilter);
	}
	
	
	public int enumerateSubDir(String basedir, String namefilter){
		int n=0;

		File[] files = dirutil.getSubDirectories( namefilter, basedir);
		if (files!=null){
			n = files.length ;
		}
		
		return n;
	}
	
	public int enumerateFiles( String namesfilter, String extension, String dirpath){
		int n = dirutil.enumerateFiles(namesfilter, extension, dirpath ) ;
		return n;
	}

	public ArrayList<String> listofFiles( String namesfilter, String extension, String dirpath){
		return listOfFiles( namesfilter, extension, dirpath);
	}

	static public ArrayList<String> listOfFiles( String namesfilter, String extension, String dirpath){
		ArrayList<String> filenames = new ArrayList<String>();
		
		filenames = DirectoryContent.getFileList(namesfilter, extension, dirpath);
		
		return filenames;
	}

	
	public String createEnumeratedFilename( String dirpath, String namesfilter, String extension, int len){
		String namesnip, ext, enumstr, str, rStr="";
	 	int n, t=0;
		boolean done=false;
		
		
		n = dirutil.enumerateFiles(namesfilter, extension, dirpath ) ;
		 
		while (done==false){
			n = n+t ;
		
			enumstr = "_"+strgutil.leftPad(""+(n+1), "0", len);
			
			str = strgutil.removeFileExtension(namesfilter,extension) ;
			
			rStr = dirpath+"/"+ str + enumstr + extension ;
			
			rStr = rStr.replace("\\/", "/");
			rStr = rStr.replace("//", "/");
			
			File fil = new File(rStr);
			if (fil.exists()){
			  t++;	
			}else{
				done=true;
			}
			
		}
		
		
		return rStr;
	}
	
	public static  String createPath( String pathsnip1, String pathsnip2, boolean onlycheckPath){

		String path ="" ;
		
		try{
			path=".";
			pathsnip2 = StringsUtil.replaceall(pathsnip2, "::", "/");
			pathsnip2 = StringsUtil.replaceall(pathsnip2, ":", "");
			pathsnip2 = StringsUtil.replaceall(pathsnip2, "//", "/");
			
			path = pathsnip1 + "/" + pathsnip2;
			
			// the java built-in "replaceALL" will NOT replace all occurrences...
			path = path.replaceAll("//", "/") ;   path = StringsUtil.replaceall(path, "//", "/");
			path = path.replaceAll("\\\\", "/") ; path = StringsUtil.replaceall(path, "\\\\", "/");
			path = path.replaceAll("\\//", "/") ; path = StringsUtil.replaceall(path, "\\//", "/");
			path = path.replaceAll("//", "/") ;   path = StringsUtil.replaceall(path, "//", "/");
			path = StringsUtil.replaceall(path,"./", "/") ;
			// path = prepareFilepath( path ) ;
			
			int k=pathsnip2.length() ;
			
			String tmp = pathsnip2.trim().substring(k-1,k);
			
			// if there is no trailing slash, we might have a filename
			if (tmp.contentEquals("/")){
				
				File fil = new File(path);
				if (fil.exists()==false){
					fil.mkdirs();
				}
			}
			
			if (onlycheckPath==true){

				File fil = new File(path);
				if (fil.exists()==false){
					path= "";
				}
				
			}
			
		}catch(Exception e){
			path= "";
		}
		
		
		return path ;
	}
	
	/**
	 * 
	 * trick: slash at the end enforces creation of the folders on the path
	 * 
	 * @param pathsnip1
	 * @param pathsnip2
	 * @return
	 */
	public static String createPath( String pathsnip1, String pathsnip2){
		
		return createPath( pathsnip1, pathsnip2, false);
	}

	/**
	 * <p>
	 * creates a string which could serve as a directory (folder) name;<br/> 
	 * if the second string parameter ends with a trailing slash like "path/", and
	 * if it is possible, then the resulting path will be created physically;
	 * if this fails, the method returns an empty string. </p>
	 * 
	 *  <p> 
	 *  note, that there are two flavors of this method; one of them checks if the path can be created,
	 *  and if, it creates it, and if then it does not exist it returns an empty string
	 *  </p>
	 * @param pathsnip1
	 * @param pathsnip2
	 * @return
	 */
	public String createpath( String pathsnip1, String pathsnip2){
		
		return createPath( pathsnip1, pathsnip2, false);
	}

	public String createpath( String pathsnip1, String pathsnip2, boolean onlycheckPath){
		return createPath( pathsnip1, pathsnip2, onlycheckPath);
		
	}
	
	
	/**
	 * stepping up <>
	 * 
	 * @param dir
	 * @param steps
	 * @return
	 */
	public static String getParentDir( String dir, int steps) {
		String path = "";
		File _fdir;
		try{
			
			if (steps<0){
				steps=999; // -> till root
			}
			
			for (int i=0;i<steps;i++){
				path=dir;
				
				try{
					
					if (isWebAddress(dir)){
						path = strgutil.trimm(path, "/") ;
						int p= path.lastIndexOf("/");
						if (p>=3){
							dir = path.substring(0,p);
						}
					}else{
						_fdir = new File(path);
						if (_fdir!=null){
							dir = _fdir.getParent();
						}
					}
					
				}catch(Exception e){
				}
				
			}// i->
			path=dir;
			
		}catch(Exception e){
			
		}
		
		return path;
	}
	
	private static boolean isWebAddress(String pathstr) {
		boolean rB=true;
		
		
		rB = pathstr.toLowerCase().trim().startsWith("http://");
		if (rB==false){
			rB = pathstr.toLowerCase().trim().startsWith("www.");
		}
		if (rB==false){
			rB = pathstr.toLowerCase().trim().startsWith("ftp://");
		}
		if (rB==false){
			rB = pathstr.toLowerCase().trim().startsWith("ftp");
		}
		if (rB==false){
			rB = pathstr.toLowerCase().trim().startsWith("tcp://");
		}
		if (rB==false){
			rB = pathstr.toLowerCase().trim().startsWith("udp://");
		}
		
		
		return rB;
	}


	public String getparentdir( String path ){
		return getParentDir(path);
	}	
	public static String getParentDir( String path ){
		
		String dirname = "";
		File dir;
		char ch;
		
		try{

			dir = new File(path);
			dirname = dir.getParent() ;
			
			ch = dirname.charAt(dirname.length()-1);
			if (ch!='/'){
				dirname = dirname + "/" ;
			}
			
			dirname = strgutil.replaceAll(dirname, "\\", "/");
			dirname = strgutil.replaceAll(dirname, "//", "/");
			
		}catch(Exception e){
			dirname ="" ;
		}
		
		return dirname;
	}
	
	
	
	   /**
   * Generate a temporary file and write an InputStream to it.
   * 
   * @param is
   *            InputStream
   * @return Abstract pathname of the generated temporary file.
   * @throws IOException
   */
	static protected File copyToTemp(InputStream is) throws IOException {
		File t;
		FileOutputStream os;
		int numBytes;

		t = File.createTempFile("idok_temp", null);
		os = new FileOutputStream(t);

		byte[] buf = new byte[4096];

		while (true) {
			numBytes = is.read(buf);
			if (numBytes <= 0) break;
			os.write(buf, 0, numBytes);
		}
		os.close();
		return t;
	}
	
	/** returns the name of a file or a directory without any of its parents' names */
	public String getSimpleName( String filename){
		
		String simpleName = filename ;
		int p1,p2;
		
		p2 = -1 ;
		p1 = simpleName.indexOf("\\");
		if (p1<0){
			p2 = simpleName.indexOf("/");
		}
		
		if (this.fileexists(filename)){
			File fil = new File(filename);
			simpleName = fil.getName() ;
		}else{
			if (p2>=0){
				
			}else{
				simpleName = simpleName.replace("\\", "/");				
			}
			p2 = simpleName.lastIndexOf("/");
			if (p2== simpleName.length()-1){
				simpleName = simpleName.substring(0,simpleName.length()-1) ;
				p2 = simpleName.lastIndexOf("/");
			}
			if (p2>=0){
				simpleName = simpleName.substring(p2+1, simpleName.length()) ;
			}
		}
		
		return simpleName;
	}
	
	/**
	 * User's current working directory<br/>
	 * sth like: Win   E:\\java\\workspace\\SensoryDataFeeder\\bin 
	 *           Mac   ... /Applications
	 */
	public String getUserDir(){
		return System.getProperty("user.dir"); 	 
	}
	

	// http://www.leepoint.net/notes-java/io/30properties_and_preferences/40sysprops/10sysprop.html
	// user.dir=C:\0www-workingnotes\notes-java-working\io\30properties_and_preferences\40sysprops\SysPropList
	// user.home=C:\Documents and Settings\Owner
	public String getUserHomeDir(){
		return System.getProperty("user.home"); 	 
	}
	
	
	public static String gettempdir(){
		return getTempDir();
	}
	/**
	 * the systems tmp dir
	 * 
	 * @return
	 */
	public static String getTempDir(){
		String path ;
		
		path = System.getProperty("java.io.tmpdir") ;
		path = strgutil.replaceAll(path, "\\", "/");
		if (path.charAt(path.length()-1)!='/'){
			path = path+"/" ; 
		}
		
		return path ;
	}
		
	
	public String getTempDir( String basedir, String subdir){
		String path ;
		
		path="";
		
		
		if ((basedir!=null) && (basedir.length()>0) && 
			(basedir.charAt(0)!='.') && (strgutil.indexOfparticles(basedir, "?&%\"'", 0)<0)){
			path = this.createPath(basedir, "tmp/");
			
		}else{
			return getTempDir(subdir);
		}
		return path;
	}
		
	public String getTempDir(String subdir){
		String path ;
		
		path="";
		path = getTempDir() ;
		
		if ((subdir.trim().length()>0)){
			subdir = subdir.replace("..", ".").trim(); 
			subdir = subdir.replace(":", ".").trim(); 
			subdir = subdir.replace("\\\\", ".").trim();
			subdir = subdir.replace("//", "/").trim();
			subdir = strgutil.replaceAll(subdir, "./", "/").trim();
			
			if (subdir.contentEquals(".")==false){
				path = this.createPath(path, subdir);
				
				File fil = new File(path);
				if (fil.exists()==false){
					fil.mkdirs();
				}
				
				if (this.fileexists(path)==false){
					subdir = getTempDir();
					path = strgutil.replaceAll(path, "\\", "/");
					
				}
				if (path.charAt(path.length()-1)!='/'){
					path = path+"/" ; 
				}
			}
		}
			
		return path ;
	}


	public String createTempFilename(String namesnip, String extension){
		
		String fname,tmpfolder = getTempDir();
		
		String idStr = GUID.randomvalue();
		idStr = idStr.replace("-", "").replace(".", "");
		
		if (extension.trim().length()>0){
			if (extension.startsWith(".")==false){
				extension = "."+extension;
			}
		}
		fname = namesnip + idStr + extension;
		fname = this.createpath(tmpfolder, fname) ;
		
		return fname ;
	}

	
	
	
	/**
	 * Create a new temporary directory. Use something like
	 * {@link #recursiveDelete(File)} to clean this directory up since it isn't
	 * deleted automatically
	 * @return  the new directory
	 * @throws IOException if there is an error creating the temporary directory
	 */
	public static File createTempDir(String prefix) throws IOException {
		
		final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
		File newTempDir;
		final int maxAttempts = 9;
		
		int attemptCount = 0;
		
		do {
			attemptCount++;
			if (attemptCount > maxAttempts) {
				throw new IOException("The highly improbable has occurred! Failed to "
						            + "create a unique temporary directory after " + maxAttempts + " attempts.");
			}
			
			String dirName = UUID.randomUUID().toString();
			newTempDir = new File( sysTempDir, prefix+"_"+dirName);
		}
		while (newTempDir.exists());
	
		if (newTempDir.mkdirs()) {
			return newTempDir;
		} else {
			throw new IOException("Failed to create temp dir named " + newTempDir.getAbsolutePath());
		}
	}


	public static File createTempdir(String prefix) throws IOException {
		return createTempDir(prefix);
	}


	public boolean manageBakFile( String filename, boolean eraseAfterCopy ){
		
		return manageBakFile(filename,10,eraseAfterCopy,false);
	}
	
	public boolean manageBakFile( String filename,int maxCopiesEnum ){
		 
		manageBakFile(filename,maxCopiesEnum,false,false);
		return true;
	}
	
	public boolean manageBakFile( String filename ){
		
		return manageBakFile(filename,10,false,false);
	}

	// exists ? -> put to bak file,.. 1=remove original
	/**
	 * <p>
	 * for a provided filename it is checked whether it exists; if yes, then a copy will be created using
	 * a different name, following the scheme + "-"+datestr+".bak"; (datestr only if useDateStr==true) </p>
	 * the parameter "maxCopiesEnum" controls the number of copies allowed.  <br/>
	 * <br/>
	 */
	public boolean manageBakFile( String filename , int maxCopiesEnum, boolean eraseAfterCopy, boolean useDateStr){
		
		String bakfilename,str ,directoryPath = "",extension="";
		boolean rb = false;
		DateTimeValue datecomposer = new DateTimeValue( 14,1);
		String datestr = "";
		Vector<String> files;
															out.print(4, "          ... manageBakFile()");
		try {

			if (useDateStr){
				datestr = datecomposer.get();
			}

		
		
			File fil = new File(filename);

			if (fil.exists() == false) {
				return true;
			}
			if (fil.isFile()){
				String fstr = fil.getName();
				int p = fstr.lastIndexOf(".");
				extension = "."+fstr.substring(p+1,fstr.length()) ;
				extension = extension.replace("..", ".") ;
			}
			
			bakfilename = filename + "-"+datestr+".bak";

			/* 
			if (fil.exists() == false) {
				fil.delete();
			}
			*/
			
			if (maxCopiesEnum==0){
				if (fil.exists()){
					fil.delete();
				}
			}
			if (maxCopiesEnum>0){
				

				// check the particular file list given the filename as filter
				str = fil.getName() ;
				directoryPath = fil.getParentFile().getAbsolutePath();
				
				/*  very time consuming -> switch to avoid that if we have a lot of files...
				files = dirutil.getFileList( str, directoryPath);
				 
				if (files.size() > maxCopiesEnum){
					str = files.get( files.size()-1) ;
					deleteFile(str);
				}
				*/
				File rsrc , rtrg ;
				 											out.print(4, "          ... manageBakFile() - delete previous ");
				if (fileexists(bakfilename)){
					this.deleteFile(bakfilename) ;
				}
				// copyFileN(filename, bakfilename);
															out.print(4, "          ... manageBakFile() - rename op ");
			    rsrc = new File(filename); // backup of this source file.
			    rtrg = new File( bakfilename);
			    rsrc.renameTo( rtrg );
			    											out.print(4, "          ... manageBakFile() - succ.? ");
				rb = fileexists(bakfilename) ;
				
				if (rb){
					// deleteFile(filename);
				}
				// rb = copyTxtFile(filename, bakfilename,1);
			}
			
			if ((directoryPath.length()>0) && (direxists(directoryPath))){
				
				int n = dirutil.enumerateFiles(extension, directoryPath);
				if ((n > maxCopiesEnum + 2) && (maxCopiesEnum>0)){
					reduceFileFolderList( directoryPath,1, extension, maxCopiesEnum+1) ;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
															out.print(4, "          ... manageBakFile() -> leaving ");
		return rb;
	}
	
	
	/**
	 * 
	 * reduces the list of files or folders in a directory by deleting them, using age as the criterion
	 * 
	 * @param filfolders
	 * @param maxCount
	 */
	public void reduceFileFolderList(File[] filfolders,  int ftype, int maxCount) { 
		

		IndexedDistances afx = DirectoryContent.createListByAge(filfolders,ftype,1);
		int minn = 1;
		String fname ;
		if (maxCount>20){
			minn = 3 ;
		}
		if ((ftype==2) || (ftype==3)){
			int d = Math.max(afx.size()-maxCount,0);
			if (maxCount>1)
				for (int i=0;i<d;i++){
					fname = afx.getItem(i).getGuidStr();
					recursivedelete(fname);
			}else{
				recursivedelete(filfolders[0]);
			}
		} //folder, or both?


		if ((ftype==1) || (ftype==3)){
			
			
			minn = 1;
			
			if (maxCount>20){
				minn = 3 ;
			}
			int d = Math.max(afx.size()-maxCount,0);
			if ((maxCount>1) && (afx.size()>0)){
				for (int i=0;i<d;i++){
					fname = afx.getItem(i).getGuidStr();
					File f = new File(fname);
					f.delete() ;
				}
			} 
			
		} // files, or both ?
				
	}
	
	
	
	public static void reduceFolderListByAge(String basepath, int maxRemainCount, String nameSnippet, double ageByDays) {
		
		String fname;
		IndexedDistances afx, sftx;
		IndexDistance sft;
		
		long td,foldertime,thresholdtime , now = System.currentTimeMillis();
		thresholdtime = now - (long)(ageByDays*24*60*60*1000) ;
		
		// the complete list
		
		File[] folders = DirectoryContent.getSubFolders( nameSnippet, basepath) ;
		afx = DirectoryContent.createListByAge(folders,2,1);
		
		if ((folders==null) || (folders.length==0)){
			return;
		}
		
		sftx = new IndexedDistances();
		
		for (int f=0;f<folders.length;f++){  
		
			fname = folders[f].getName() ;
			// by date
			foldertime = folders[f].lastModified() ;
			
			td = thresholdtime - foldertime;
			if (td<0){
				continue ;
			}
			
			// create a structured list from those files
			sftx.add( new IndexDistance(f,(1.0*(double)td), fname));
			
		} // ->

		int minn = 1;

		if (maxRemainCount > 0) {

			if (maxRemainCount > 20) {
				minn = 3;
			}
			int d = Math.max(sftx.size() - maxRemainCount, 0);

			if ((maxRemainCount > 1) && (sftx.size() > 0)) {

				for (int i = 0; i < d; i++) {
					fname = sftx.getItem(i).getGuidStr();
					// fname = createPath(basepath,fname);
					// recursiveDelete(fname); not by name: this creates a new file variable, for which it will not be 
					//                         possible to delete the file, since we have it opened here...
					//                         
					int f = sftx.getItem(i).getIndex();
					recursiveDelete(folders[f]);
				}
			} else {
				if (folders.length > 0) {
					recursiveDelete(folders[0]);
				}

			}
		} // all sub-folders in basepath
		
		fname = "" ;
	}

	/**
	 * 
	 * @param basePath
	 * @param ftype  1=files; 2=folders;  3=both
	 * @param maxCount
	 */
	public static void reduceFileFolderList(String basePath, int ftype, int maxCount) {	
		reduceFileFolderList( basePath, ftype, "", maxCount) ;
	}
	
	public static void reduceFileFolderList(String basePath, int ftype, String extension, int maxCount) {	
		int z;
		
		
		if ((ftype==2) || (ftype==3)){
			File[] folders = DirectoryContent.getSubFolders( extension, basePath) ;
			
			for (int f=0;f<folders.length;f++){  
			
				// by date
				
				IndexedDistances afx = DirectoryContent.createListByAge(folders,2,1);
				int minn = 1;
				String fname ;
				if (maxCount>20){
					minn = 3 ;
				}
				int d = Math.max(afx.size()-maxCount,0);
				if ((maxCount>1) && (afx.size()>0)){
					for (int i=0;i<d;i++){
						fname = afx.getItem(i).getGuidStr();
						recursiveDelete(fname);
					}
				}else{
					recursiveDelete(folders[0]);
				}
			}
		} // folders, or both ?
		
		if ((ftype==1) || (ftype==3)){
			

			ArrayList<String> filenames = DirectoryContent.getFileList("", extension, basePath);
			IndexedDistances afx = DirectoryContent.createListByAge(filenames,1,1);
			
			int minn = 1;
			String fname ;
			if (maxCount>20){
				minn = 3 ;
			}
			int d = Math.max(afx.size()-maxCount,0);
			if ((maxCount>1) && (afx.size()>0)){
				for (int i=0;i<d;i++){
					fname = afx.getItem(i).getGuidStr();
					File f = new File( createPath(basePath,fname));
					f.delete() ;
				}
			} 
		} // files, or both ?
		z=0;
	}
	
	public static boolean recursiveDelete(String foldername) {
		
		File f = new File(foldername);
		return recursiveDelete(f);
	}
	/**
	 * Recursively delete file or directory
	 * @param fileOrDir
	 *          the file or dir to delete
	 * @return
	 *          true if all files are successfully deleted
	 */
	public static boolean recursiveDelete(File fileOrDir) {
		
		if (fileOrDir.isDirectory()) {
			// recursively delete contents
			for (File innerFile : fileOrDir.listFiles()) {
		
				if (!recursiveDelete(innerFile)) {
					return false;
				}
			}
		}

		return fileOrDir.delete();
	}

	public boolean recursivedelete(File fileOrDir){
		return recursiveDelete(fileOrDir);
	}
	public boolean recursivedelete(String fname) {
		
		File f = new File(fname);
		return recursivedelete(f);
	}


	public void renameFile(String oldname,
	                       String newname){
		//Obtain the reference of the existing file
		File oldFile, newfile; 
		
		oldFile = new File(oldname); 
		newfile = new File(newname);
			
		//Now invoke the renameTo() method on the reference, oldFile in this case
		if (fileexists(newname)==true){
			newfile.delete();
		}
		if (fileexists(oldname)==true){
			oldFile.renameTo(newfile);
		}
	}

	public boolean copyFile(String source, String dest) {
		File srcfil, destfil ;
		
		srcfil  = new File(source);
		destfil = new File(dest);
		
		if (destfil.exists()){
			this.deleteFile(dest);
		}
		
		boolean rB = copyFile( srcfil, destfil ) ;
		
		return rB;
	}

	
	public boolean copyFile(File sourceFile, File destFile) {
		boolean rB = false;
		
		FileInputStream fis;
		FileOutputStream fos;
		
		FileChannel source = null;
		FileChannel destination = null;

		String destFilename = "";
		
		try {
			if (!destFile.exists()) {

				destFile.createNewFile();

			}


			try {
				
				fis = new FileInputStream(sourceFile);
				source = fis.getChannel();
				
				fos = new FileOutputStream(destFile);
				destination = fos.getChannel();

				destination.transferFrom(source, 0, source.size());
				
				destFilename = destFile.getAbsolutePath();
				
			} finally {
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
			}

			rB = fileexists(destFilename ); 
				
		} catch (IOException e) {
			System.out.println("\nError while copying file : \n"+sourceFile.getAbsolutePath()+"  \n  to \n"+destFile.getAbsolutePath()+"  \n\n"); 
			e.printStackTrace();
			rB = false;
		}
		return rB;
	}

	public void copyFileN( String filename, String bakfilename){
		File source, dest;
		boolean ok;
		FileOutputStream fileOut;
		FileInputStream fileIn;
		int mode=0;
		
		source = new File(filename);
		dest   = new File(bakfilename);
														   	out.print(4, "before copyFileN()");
		copyFileN(  source,   dest) ;

		if (mode==1){
		// this enforces to wait, until we are allowed to access the target file again
		ok = false;
		while (ok==false){
			try {
				Thread.currentThread().sleep(0,10);
				
				fileOut = new FileOutputStream( bakfilename );
				fileIn  = new FileInputStream( filename );
				
				// this will happen if there is no exception any more
				ok = true;
				
			}catch(Exception e){
			}
			
		} // while not ok
		}
	}

	@SuppressWarnings("static-access")
	public void copyFileN(File source, File dest) {
		
		FileInputStream fi ;
		FileChannel fic ;
		
		FileOutputStream fo = null;
		BufferedOutputStream bout;
		FileChannel foc = null;
		MappedByteBuffer mbuf ;
		
		try{
															out.print(4, "copyFileN() - create & read fi ");
			fi = new FileInputStream(source);
			
			fic = fi.getChannel();

			mbuf = fic.map(FileChannel.MapMode.READ_ONLY, 0, source.length());

			fic.close();
															out.print(4, "copyFileN() - close fi ");
			fi.close();

			
															out.print(4, "copyFileN() - create f.out...");			
			fo = new FileOutputStream(dest);
			 
			foc = fo.getChannel();
			foc.force(true);
															out.print(4, "copyFileN() - now write... ");
			foc.write(mbuf);

															out.print(4, "copyFileN() - f.out written ! ");

		}catch(IOException iex){
		
		}catch(Exception ex){
		
		} finally{
			try{
				
															out.print(4, "copyFileN() - about to close f ... ");
				if (foc!=null){
					foc.close();
				}
				if (fo!=null){
					fo.close();
				}
				Thread.currentThread().sleep(0,20);
				System.gc();
			}catch(Exception pex){
				pex.printStackTrace();
			}
															out.print(4, "copyFileN() - f.nio close. ");
		}
	   
	}
	
	
	private void copyFileN_(File source, File dest) throws IOException {
		if (!dest.exists()) {
			dest.createNewFile();
		}
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(source);
			out = new FileOutputStream(dest);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	public boolean copyFile_ (String source , String target) {
		boolean rb=false ; 
		
		FileReader in ;
		FileWriter out ;

		File inputFile, outputFile;

		
		
	    inputFile = new File(source);
	    outputFile = new File(target);


	    try {
			in = new FileReader(inputFile);
			out = new FileWriter(outputFile);
			int c;

			while ((c = in.read()) != -1){
				out.write(c);
			}

			in.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		
	    return rb;
	}
	
	
	public boolean copyTxtFile( String srcfilename, String trgfilename, int t){
		boolean rb=false;
		FileWriter foswrite;
		BufferedWriter bwriter ;
		File sourceFile;
		BufferedReader reader ;
		String str;
		
		try {
			
			foswrite = new FileWriter(trgfilename, false); // true -> append
			bwriter = new BufferedWriter(foswrite);
			
			sourceFile = new File(srcfilename);
          reader = new BufferedReader(new FileReader(sourceFile));
          
          // TODO this should use a char[] as a buffer, see example below
          // using a buffer of 4096 ...
          
          while ((str = reader.readLine()) != null) {
          	// the reader returns the line without the finalizing LF char
          	bwriter.write(str+"\n");
          }
          
			
			// Close the output stream
			
			
          bwriter.close();
			foswrite.close();
			reader.close() ;
			
			
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		File fil = new File(trgfilename);
		
		rb = fil.exists() ;
		
		return rb ;
	}
	
	/*
	      char[] buf = new char[DEFAULT_BUFFER_SIZE];
		  int n;
			while( (n = in.read(buf)) >= 0 ) {
					out.write( buf, 0, n );
			}

	*/
	public boolean moveFile( String source, String target) {
		boolean rb=false ; 
		File fil;
		
		try {
			
			copyFile(source, target) ;
			
			
		if (this.fileexists(target)){
			if (this.fileexists(source)){
				this.deleteFile(source) ;
			}
		}

		} catch (Exception e) {
			System.out.println("Error while moving file : "+source);
			e.printStackTrace();
		}
		

		return rb;
	}
	
	 
	public void deleteOldFiles( String path, String[] extensions, long oldAgeThreshold ){
		
		ArrayList<String> files;
		String str="";
		File file ;
		Long lastModified ;
		
		try{
		
			files = dirutil.getFileList( extensions[0], path);
			 
			for (int i=0;i<files.size() ;i++){
				str = files.get(i) ;
				
				file = new File( str );
		
				if ((file != null) && ( str.contains("."+extensions[0])) && (file.isFile()) ){
					// Get the last modification information.
					lastModified = file.lastModified();

					if (lastModified < oldAgeThreshold){
						file.delete();
					}
					// Create a new date object and pass last modified information
					// to the date object.
					// Date date = new Date(lastModified);
				}
			}
			
			
		}catch(Exception e){
			// e.printStackTrace();
			System.err.println("Error while deleting files ("+str+"): "+e.getMessage() );
		}
		
		
	}
	
	
	public boolean deleteFile( String filename){
		File fil;
		boolean rb;
		
		fil = new File(filename);
		if (fil.exists()){
			fil.delete();
		}
		
		rb = fil.exists() == false;
			
		return rb;
	}
	
	
	public boolean deleteDir( String directory) {
		boolean rb=false;
		File dir; 
		dir = new File(directory);
		
		rb = deleteDir(dir);
		
		return rb;
	}
	
	public boolean deleteDir( File dir) {
		boolean rb=false;
		 
		
		try{
			
			if (dir.isDirectory()) {
		    	
		        String[] children = dir.list();
		        
		        for (int i=0; i<children.length; i++) {
		        	
		            boolean success = deleteDir(new File(dir, children[i]));
		            if (!success) {
		                return false;
		            }
		        }
		    }
		
			// The directory should now be empty, so try to delete it
			rb = dir.delete();
			
			if ((dir!=null) &&(dir.exists()==true)){
				rb=false;
			}
			
		}catch(Exception e){
			rb = false;
		}
	    

	    
	    return rb;
	}
	
	/**
	 *  remove all directories; <br/>
	 *  do not use * or + in the provided pattern so far...
	 *  <br/><br/>
	 * @param dirnamePattern
	 */
	public void removeFolder( String dirnamePattern ){
		
		
		
	}

	public void removeFolder( Vector<String> dirnames){
		
	}

	
	public void removeFolder( String[] dirnames){
		
	}

	
	
	@SuppressWarnings( "unused")
  private void delay(int _delay_in_msec){ // doesnot work
		try {
			
			 
			// Thread.sleep(_delay_in_msec);
      }
		finally{
			
		}
		
      // catch (InterruptedException e) 
      {
	        
	        // e.printStackTrace();
      }
	}
	
	 
	
	public String getsimplefilenametimestamp(){
		String tmp_str="";
		
	      Calendar c = Calendar.getInstance();
	      c.setTimeInMillis(System.currentTimeMillis());
	      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
	      
	      tmp_str = sdf.format(c.getTime()).trim();
		
	      return tmp_str;
	}
	
	 
	  
	@SuppressWarnings("unused")
	private String[] splitString( String str,
	                             int trimemptyslots,
	                             int removeemptyinner){
		int i;
		boolean done=false;
		String hs1;
		String[]  _arr,return_value;
		Vector<String> _vs=new Vector<String>();
		
		
		
		if ((str==null) || (str.length()==0)){
			return null;
		}
		_arr = str.split("\t");
		
		_vs.setSize(0);

		for (i=0;i<_arr.length;i++){
			_vs.add(_arr[i]);
		}
		_vs.trimToSize();

		i=1;
		while (done==false){
			
			if ((i==1) && (trimemptyslots==1)){
				hs1 = _vs.get(i);
				if ((hs1!=null) && ( hs1.length()>0)){hs1=hs1.trim();}
				if ((hs1!=null) && (hs1.length()==0 )){
					_vs.remove(i);
					i=i-1; 
				}
			}
			if ((i==_vs.size()-1) && (trimemptyslots==1)){ 
				hs1 = _vs.get(i);
				if ((hs1!=null) && ( hs1.length()>0)){hs1=hs1.trim();}
				if ((hs1!=null) && (hs1.length()==0 )){
					_vs.remove(i);
					i=i-2; 
				}
			}
			if ((i>0) && (i<_vs.size()-1) && (removeemptyinner==1)){ 
				hs1 = _vs.get(i);
				if ((hs1!=null) && ( hs1.length()>0)){hs1=hs1.trim();}
				if ((hs1!=null) && (hs1.length()==0 )){
					_vs.remove(i);
					i=i-2; 
				}
			}
				
			if (i>=_vs.size()){ done=true;}
			i=i+1;
			if (i<0){i=0;}
		}
		
		return_value=new String[_vs.size()];
		
		for (i=0;i<_vs.size();i++){
			if (_vs.get(i)!=null){
			return_value[i] = _vs.get(i);
			}
			else{
				return_value[i] = "";	
			}
		}
		
		
		return return_value;
	}
	
	/**
	 * 
	 * "filename" is a path string to a document, i.e. its parent 
	 * will be interpreted as a path
	 * 
	 * @param filename
	 */
	public boolean createDirforDoc(String filename){
	    File file, dir ;
	    boolean exists , rb=false;
	    String parentDirStr;
	    
	    if (filename.length()==0){
	    	return false;
	    }
	    file= new File(filename);
	    
	    parentDirStr = file.getParent() ;
	    
	    createDir( parentDirStr ) ;
	    
	    file= new File(parentDirStr);
	    
	    rb = file.exists();
	    
	    return rb;
	    
	    /*
	    dir = new File(parentDirStr) ;
	    
	    exists = dir.exists();
	    
	    if (!exists) {

	    	dir.mkdirs() ;
	    }
	    */
	}	
	
	
	public void createDir(String filename){
	    File file ;
	    boolean exists ;

	    if (filename==null){
	    	return;
	    }
	    
	    file   = new File(filename);
	    exists = file.exists();
	    
	    if (!exists) {
	    	file.mkdirs() ;
	    }
	  
	}
	
	public boolean pathIsAbsolute( String pathstr) {
		boolean  ib=false;
		File dir ;
		String currentDir, str;
		
		
		try {

			// dir = new File (".");
		    // currentDir =  dir.getCanonicalPath();
			
		    dir = new File(pathstr);
		    
		    File[] fils = dir.listFiles();
		    
			ib = fils!=null;
		} catch (Exception e) {
			ib = false;
		}

		
		
		
		return ib ;
	}	
	
	
	
	public void copyDirectory(File srcDir, File dstDir) throws IOException {
		
	    if (srcDir.isDirectory()) {
	    	
	        if (!dstDir.exists()) {
	            dstDir.mkdirs();
	        }

	        String[] children = srcDir.list();
	        for (int i=0; i<children.length; i++) {
	            copyDirectory( new File(srcDir, children[i]),  new File(dstDir, children[i]));
	        }
	    } else {
	        // This method is implemented in Copying a File
	        copyFile(srcDir, dstDir);
	    }
	}
	
	
	public String[] filesystemRoots(){
		File[] roots ; 
		String[] rootPathNames;
		// UNIX file systems have a single root, `/'. On Windows, each drive is a root. 
		// For example the C drive is represented by the root C:\.

		roots = File.listRoots();
		rootPathNames = new String[roots.length]; 
		
		for (int i=0; i<roots.length; i++) {
			rootPathNames[i] = roots[i].getAbsolutePath() ;
		}

		return rootPathNames;
	}
	/**
	 *  provides simpler access to this routine
	 * 
	 */ 
	public boolean fileexists(String filename){
		boolean rB = false;
		
		if (filename==null)filename="";
		
		File file = new File(filename);
	
		if (file.isDirectory()) {
			rB = false;
		} else {
			rB = fileExists(filename);
		}
		return rB;
	}


	public static boolean fileExists(String filename){
		boolean rB=false;
		
		if ((filename==null) || (filename.length()==0)){
			return rB;
		}
		
		java.io.File file = new java.io.File(filename);

		if (file.isDirectory()) {
			rB = false;
		} else {

			boolean exists = file.exists();
			if (!exists) {
				// It returns false if File or directory does not exist
				rB = false;
			} else {
				// It returns true if File or directory exists
				rB = true;
			}
		} 
		    
		 
		 return rB;
	}
	

	
	public boolean direxists(String foldername){	// 
		
		return folderExists(foldername);
	} 
	
	public static boolean isFile(String filename){
		boolean rB=false;
		
		if ((filename==null) || (filename.length()==0)){
			return rB;
		}
		
		File file = new File(filename);
		
		if (file.isFile()==false){
			return false;
		}
		try {
			
			File fil = new File(filename);
		    rB = fil!=null;
		    fil=null;
		    
		} catch (Exception e) {
			rB = false;
		}
		
		return rB;
	}

	
	public static boolean isFolder(String foldername){
		boolean rB=false;
		
		if ((foldername==null) || (foldername.length()==0)){
			return rB;
		}
		
		File file = new File(foldername);
		
		if (file.isDirectory()==false){
			return false;
		}
		try {

			// dir = new File (".");
		    // currentDir =  dir.getCanonicalPath();
			
			File dir = new File(foldername);
		    
		    File[] fils = dir.listFiles();
		    
		    rB = fils!=null;
		    
		} catch (Exception e) {
			rB = false;
		}
		
		return rB;
	}
	
	public static boolean folderExists(String foldername) {
		boolean rB=false;
		
		if ((foldername==null) || (foldername.length()==0)){
			return rB;
		}
		
		File file = new File(foldername);
		
		if (file.isDirectory()==false){
			return false;
		}
		try {

			// dir = new File (".");
		    // currentDir =  dir.getCanonicalPath();
			
			File dir = new File(foldername);
		    /*
		    File[] fils = dir.listFiles();
		    
		    rB = fils!=null;
		    */
			rB = dir != null;
			
		} catch (Exception e) {
			rB = false;
		}
		
		return rB;
	}
	
	
	public boolean filenameIsUsable(String dataFieldFilename) {
		boolean rB=false;
		String parentDir;
		String testedFilename;
		try{
			
			try{
				if (fileexists(dataFieldFilename)){
					return true;
				}
				
				testedFilename = "~tmp_test.x";
				parentDir = getParentDir(dataFieldFilename);
				if (parentDir.length()==0){
					rB=false;
					return rB;
				}
				
				testedFilename = this.createPath( parentDir,testedFilename );
				
				this.writeFileSimple(testedFilename, ".");
				if (fileexists(testedFilename)){
					deleteFile(testedFilename);
					rB=true;
				}
				
				int p= dataFieldFilename.lastIndexOf("/");
				if (p==0){
					p= dataFieldFilename.lastIndexOf("\\");
				}
				if (p==0){
					rB=false ;
				}else{
					testedFilename = dataFieldFilename.substring(p+1,dataFieldFilename.length()) ;
					if (testedFilename.length()==0){
						rB=false;
					}
				}
				
			}catch(Exception e){
				rB=false;
			}
			
		}catch(Exception e){
			rB=false;
		}
		return rB;
	}	
	
	public long getFileLastModificationTime(String filename){
		File fil;
		Long lastModified ;
		Date date ;
		
	    // Create an instance of file object.
	    fil = new File( filename );
	    // Get the last modification information.
	    lastModified = fil.lastModified();
	 
	    return lastModified ;
	}

	public Date getFileLastModificationDate(String filename){
		File fil;
		Long lastModified ;
		Date date ;
		
	    // Create an instance of file object.
	    fil = new File( filename );
	    // Get the last modification information.
	    lastModified = fil.lastModified();
	
	    // Create a new date object and pass last modified information
	    // to the date object.
	    date = new Date(lastModified);
	
	    // We know when the last time the file was modified.
	    // System.out.println(date);
	    return date ;
	}
	
	public long getFilesize( String filename){
		return  getFileSize(filename);
	}
	
	public static long getFileSize( String filename){
		
		long filsize = -1; 
		File file;
		
		
		file = new File(filename);

		if (file.exists()){
			// Get the number of bytes in the file
			filsize = file.length();
		}
		
		return filsize;
	}

	
	public int isWebDoc(String LocatorStr){
		int result = -1;
		
		
		
		return result;
	}
	
	public boolean fileIsCompressed( String filepath ){
		// TODO: fileIsCompressed() is stub
		return false ;
	}
	
	public boolean writeFileSimple(String filename, String content){
		boolean rb = false;
		
		File file;
	    Writer writer = null;
	     
	    if (filename.length()==0){
	    	return false;
	    }
	    
	    if (content==null){
	    	content="" ;
	    }
	    try {
      
          file = new File(filename);
          		
          createDirforDoc(filename) ;
          
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
          file = new File(filename);
          rb = file.exists() ;
      }
      return rb;
  }
	
	

	private int physicalWriter( BufferedWriter writer , Vector<String> data){
		
		String str ;
		int z=0, colcount = -1,n, c0, cti=0,ct=0,csi=0,cs=0, ck=0,cki=0;
		
		try {

			n = data.size();
			
			for (int i = 0; i < n; i++) {

				str = data.get(i);

				// this serves as a protection against null values in the data,
				// which then
				// allows to reconstruct the correct number of separators
				if ((colcount < 0) || (i < 10)) {
					c0 = StringUtils.countMatches(str, "\t");
					if (c0 > 0) {
						ct = c0 + ct;
						cti++;
					}

					c0 = StringUtils.countMatches(str, " ");
					if (c0 > 0) {
						cs = cs + c0;
						csi++;
					}

					c0 = StringUtils.countMatches(str, ";");
					if (c0 > 0) {
						ck = ck + c0;
						cki++;
					}
					if (i>10){
						colcount = ct/cti;
					}
				}

				if ((str != null) && (str.length() > 1)) {

					if (str.indexOf("\n") < 0) {

						// if (i < n - 1) 
						{
							str = str + "\n";
						}
					}
					writer.write(str); 
					z++;
				}
			}
                                                
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return z;
	}
	/*
	     on binary files ensure that everything has been written:
	     
	     FileOutputStream os = new FileOutputStream("outfilename");
	     FileDescriptor fd = os.getFD();
	     
	     ...
	     
	     fd.sync();
	     
	 */
	public boolean writeFilefromVectorstringTable( String filename, double[][] data){
		boolean rb = false;
		Vector<String> strdata = new Vector<String>();
		
		
		
		return rb;
	}
		
	public int writeFilefromVectorstringTable( String filename, Vector<String> data, boolean append){
		boolean rb = false;
		int writtenRecords = 0;
		
		//FileWriter foswrite;
		BufferedWriter bwriter = null ;
		File file;

	    if ((filename==null) || (filename.length()==0)){
	    	return -1;
	    }
	    try {
      
          file = new File(filename);
          
          createDirforDoc(filename) ;
          
          if ((file.exists()==false) &&(append==true)){
          	append = false ;
          } 
          if (file.exists()==false){
          	writeFileSimple( filename, "") ;
          }
          	
          file = new File(filename);
          bwriter = new BufferedWriter(new FileWriter(file));
           
          writtenRecords = physicalWriter(bwriter,data) ; 

          // we could double check... 
          
	    } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      } finally {
          try {
              
              if (bwriter != null) {
              	bwriter.close();
              }                


          } catch (IOException e) {
              e.printStackTrace();
          }
          file = new File(filename);
          rb = file.exists() ;
      }
      return writtenRecords;
		
	}
	
	

	public Properties readPropertiesByFile( File file){
		// Read properties file.
		FileInputStream fis=null;
		
		Properties properties = new Properties();
		try {
			fis = new FileInputStream(file) ;
					
			properties.load( fis );
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if (fis!=null){
				try {
					fis.close();
				} catch (IOException e) {}
			}
		}
		return properties;
	}
	
	public void writePropertiesByFile( File file, Properties props){
		FileOutputStream fos=null ;
		
		try {
			
			fos = new FileOutputStream(file) ;
			props.store(fos, null);
		    
		} catch (IOException e) {
			e.printStackTrace() ;
		}finally{
			if (fos!=null){
				try {
					fos.close();
				} catch (IOException e) {}
			}
		}
	}
	public Object readObjectFromFile( String filename){
		Object dataObj=null;
		
		int result=-1;
		 
		ObjectInputStream objistream = null ;
		FileInputStream fileIn = null ;
		BufferedInputStream bins = null ;

		try{
			

			if (fileexists(filename)==false){
				return -1 ;
			}
			
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
	
	public int writeFileFromObject( String filename, Object serializableObj){
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
			
			if (fileexists(filename)){
				result = 0;
			}
			
		}catch(Exception e){
			result = -3;
			e.printStackTrace() ;
		}
		return result ;
	}
	
	
	public void getFileforAppends(){
		

	}
	
	/*
	 
	 int result=-1;
		
		


		try{
			
			 										out.print(5,"Writing msgboard transaction data ... ");
			   
	            									 out.print(5,"output streams closed. ");
			
		}catch(Exception e){
			result = -7;
			e.printStackTrace();
		}
		
	 
	 */
	public void appendToFile( String filename, String msg){
		
		boolean append = true;
		PrintWriter pw = null ;
		FileWriter filwrit ;
		File file;
		
		try {

			file = new File( filename );
			filwrit = new FileWriter( file, append);
			
			pw = new PrintWriter( filwrit );
			
			pw.println( msg );
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
		if (pw!=null){
			pw.close();
		}
		
	}
	
	public void appendFiles( String firstfile, String appendedfile){
		
		FileWriter foswrite;
		
		File appendedFile;
		BufferedReader reader ;
		String str;
		
		try {
			// Create file
			foswrite = new FileWriter(firstfile, true); // true -> append
			 
			
	        appendedFile = new File(appendedfile);
	        reader = new BufferedReader(new FileReader(appendedFile));
	        
	        while ((str = reader.readLine()) != null) {
	        	   
	        	foswrite.write(str);
	        }
	        
	        
			
			// Close the output stream
			
			
			foswrite.close();
			reader.close() ;
			
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
	}


	public Vector<String> getFileCommentaries( String filename){
		int index ;
		Vector<String> nds ;
		
		index = nonDataSectionsMap.get( filename);
		
		nds = nonDataSections.get(index) ;
		
		return nds;
	}
	
	public Vector<String> getLastFileCommentaries(){
		return null;
	}
	
	private boolean lineIsCommentary( String str){
		boolean rb=true;
		int[] pos = new int[3] ;
		int p;
		
		
		p = str.trim().indexOf("$")+1;
		if (p-1>=2){
			p=0;
		}
		pos[0] = p;
		
		p = str.trim().indexOf("!")+1;
		if (p-1>=2){
			p=0;
		}
		pos[1] = p;

		p = str.trim().indexOf("#")+1;
		if (p-1>=2){
			p=0;
		}
		pos[2] = p;
		
		p=0;
		for (int i=0;i<pos.length;i++){
			p = p+ pos[i] ;
		}
		
		rb = p>0 ;
		return rb;
	}
	
	
	public ArrayList<String> readplainFileintoItemList( String filename ){
	
		ArrayList<String> filestr = new ArrayList<String>(); 
		BufferedReader reader = null;
	    String str ;

	    
	      
	      
	    try {
      
          
          reader = getLineReaderReference( filename ) ;  
          if (reader==null){
        	  return filestr;
          }
          while ((str = reader.readLine()) != null) {
          	   
          	if (str.trim().length()>0){
          		if ((str.indexOf("@")>0) || ( lineIsCommentary(str)==false)){
          	
          			filestr.add(str.trim()) ;
          		} // commentary line ?
          	}  // string defined ?
          }
          
           
          
	    } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      } finally {
          try {
              if (reader != null) {
              	reader.close();
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
           
      }
		
		
	    
	    return filestr;
	}
	/**
	 * 
	 * This is suitable only for data table files! <br/>
	 * this also creates a map to pointers {@code Map<String,int> ~ Map<filename, index>}, which
	 * point to non-data-sections. {@code Vector<String>}
	 * 
	 * special non-data lines are excluded:  # ! == commentary, $ == command line
	 * @param filename
	 * @return
	 */
	public ArrayList<String> readFileintoVectorstringTable( String filename ){
	
		ArrayList<String> data = new ArrayList<String>(); 
		BufferedReader reader = null;
	    String str ;

	    Vector<String> nonDataSection = new Vector<String>() ;
	      
	      
	      
	    try {
      
          
          reader = getLineReaderReference( filename ) ;  
          
          while ((str = reader.readLine()) != null) {
          	   
          	if ((str.length()>0) && ( lineIsCommentary(str)==false)){
          		data.add(str) ;
          	} else{
          		nonDataSection.add(str);
          	}
          }
          
          if ((nonDataSection != null) && (nonDataSection.size()>0)){
          	nonDataSections.add(nonDataSection) ;
          	nonDataSectionsMap.put( filename, nonDataSections.size()-1 );
          }
          
          
	    } catch (FileNotFoundException e) {
          e.printStackTrace();
      } catch (IOException e) {
          e.printStackTrace();
      } finally {
          try {
              if (reader != null) {
              	reader.close();
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
           
      }
		
	
		
		return data ;
	}
	
	
	public String readFile2String (String path) throws IOException {
	
		MappedByteBuffer bb;
		FileInputStream stream ;
		FileChannel fc ;
		
		stream = new FileInputStream(new File(path));
		try {
			fc = stream.getChannel();
			bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			
			/* Instead of using default, pass in a decoder. */
			
			return Charset.defaultCharset().decode(bb).toString();
			
		} finally {
			if (stream!=null){
				stream.close();
			}
		}
	}

	public String readFile2String ( File file) throws IOException {
		
		MappedByteBuffer bb;
		FileInputStream stream ;
		FileChannel fc ;
		
		stream = new FileInputStream( file );
		try {
			fc = stream.getChannel();
			bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			
			/* Instead of using default, pass in a decoder. */
			
			
			return Charset.defaultCharset().decode(bb).toString();
			
		} finally {
			if (stream!=null){
				stream.close();
			}
		}
	}
	
	public CharBuffer readFile2Chars ( String filepath, long limit) throws Exception {
		
		

		MappedByteBuffer bb;
		FileInputStream stream ;
		FileChannel fc ;
		long size ;
		
		
		stream = new FileInputStream(new File(filepath));
		try {
			fc = stream.getChannel();
			
			size = fc.size();
			
			if (limit>0) {
				if (size>limit){
					size = 1000;
				}
			}	
			
			bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, size);
			
			return Charset.defaultCharset().decode(bb);
		
		} finally {
			if (stream!=null){
				stream.close();
			}
		}
	}
	
	public BufferedReader getLineReaderReference( String filename){
		File file;
		BufferedReader reader = null ;
		
		if ((filename==null) || (filename.length()==0) || fileexists(filename)==false){
			return null;
		}
		file = new File(filename);

		if (file.exists()==false){
			return null;
		}
		try {
		
      	reader = new BufferedReader(new FileReader(file));
      
      } catch (FileNotFoundException e) {
			e.printStackTrace();
		}
      
      return reader ;
	}
	
	
	public String getRecordbyID( BufferedReader reader , int recordpos){ // -1 == last one
	
		String result="" ;
		String str ;
		int z;
		//boolean found = false;
		
		try {

			if (reader != null) {

				z=-1;
				while ((str = reader.readLine()) != null) {
					z++;
					
					if ((recordpos>=0) && (z>=recordpos)){
						
						result = str;

					} // arrived at pos ?
					else{
						if (str.trim().length()>0){
							result = str ;
						}
					}
				} // read ->

			} // reader available ?
			
		}catch(Exception e){
			
			
		}
		
		return result;
		
	}
	
	
	public String performDownload( String webURL, String downloadDir, int linkFollowingLevels){
		String downloadedFilename = "" ;
		
		if ( fileexists(downloadDir)==false){
			createDir(downloadDir);
		}
		
		
		
		
		return downloadedFilename;
	}

	/**
	 * 
	 * 
	 * 
	 * @param basePath
	 * @param subdirPrefix
	 * @param startEnumValue
	 * @param maxCount
	 * @param removeMode
	 * @return
	 */
	public String createEnumeratedSubDir( String basePath, String subdirPrefix, int startEnumValue , int maxCount, int removeMode){
		String createdFolder = "";
		
		String pkgsubdir,packageName;
		int dc; 
		boolean dirOk;
		
		try{
			

			packageName = subdirPrefix; // this.getSimpleName( basePath );
			
			dc = enumerateSubDir(basePath,"") ;
			if (dc<startEnumValue)dc=startEnumValue;
			
			if (dc > maxCount){
				File[] folders = dirutil.getSubDirectories( "", basePath) ;
				
				for (int f=0;f<folders.length;f++){
					if (removeMode == -3){ // by date
						
						reduceFileFolderList( folders, 2, maxCount);
						
					}
					if (removeMode == -2){ // by sorting
						
					}
				}
			}
			
			dirOk=false;

			while (dirOk == false) {
				
				if (packageName.trim().length() > 0) {
					pkgsubdir = packageName;
				} else {

					String fname = createpath(basePath, "" + dc);
					while (direxists(fname)) {
						dc++;
						fname = createpath(basePath, "" + dc);
					} // -> looping until we found an enum that does not exist
					pkgsubdir = "" + dc;

				} 

				createdFolder = createpath(basePath, pkgsubdir+"/"); // creating it..
				// here we store all , ending with slash enforces physical creation of dir
				// we do NOT need a temp dir like this ... : DFutils.createTempDir( SomDataObject._TEMPDIR_PREFIX);
				
				// 
				
				// 
				if (direxists(createdFolder) == false) { // did it fail for other reasons? e.g. is there a file of that name
					dirOk = false;
					dc++;
					packageName= packageName+dc;
					 
				} else {
					int fsz = dirutil.getFileList("", createdFolder).size() ;
					if (fsz==0){
						dirOk = true;
					}else{
						dirOk = false;
						dc++;
						packageName= packageName+dc;
					}
				}
			} // ->
				
		}catch(Exception e){
			createdFolder = createpath(basePath, GUID.randomvalue());
		}
		
		
		return createdFolder;
	}


	public static String getName(String filename) {
		String _filename = filename;
		
		File fil = new File(_filename) ;
		
		if (fil!=null){
			_filename = fil.getName() ;
		}
		
		return _filename;
	}


	public String gettempfile(String filename) {
		return getTempFile(filename);
	}
	
	public static String getTempFile(String filename) {
		// 
		String tmpfilepath="";
		String tdir = getTempDir();
		
		if (filename.length()>0){
			return tmpfilepath;
		}
			
		StringsUtil.replaceall(filename, "\\", "/");
		
		int p = filename.lastIndexOf("/");
		
		if (p>=0){
			filename = filename.substring(p,filename.length());
		}
		if (filename.length()>0){
			tmpfilepath = createPath( tdir , filename) ;
		}
		
		return tmpfilepath ;
	}


}


class PrintLog {

	 
	// =================================

	// object references ..............

	
	// main variables / properties ....
	
	public Vector<String>  diagnosticMsg = new Vector<String>() ;
	
	DateTimeValue dt = new DateTimeValue(3,0);
	boolean measureTimeDelta = true;
	
	int printLevel = 1;
	boolean showTimeStamp;
	int countlimit = 10;
	boolean isPrintFileLogging = false; 
	
	// constants ......................
	
	String logfilepath = "C:/temp/textract.log.txt" ;
	
	// volatile variables .............
	long now,lastTime, deltaTime,startTime , measuredTime, elapsedTime;
	String initialTimeStamp="", lastTimeStamp="";
	
	File prnLogFile ;
	
	// helper objects .................
	
	
	public PrintLog( int printlevel, boolean showtimestamp ){
		
		printLevel = printlevel;
		showTimeStamp = showtimestamp ;
		 
		dt.setStripSeparators(0);
		
		startTime = System.currentTimeMillis();
		
	}
	
	
	public void print( int level, String msg){
		
		print( level, msg, true) ;
		 
	}
	
	
	public void print( int level, String msg, boolean showTime){
		
		if (printLevel >= level){
			
			if (showTimeStamp){
				if (showTime==true){
					msg = addTimeStamp(msg,2) ;
				}
			}
			System.out.println(msg);
			 
		}
	}
	
	

	private String addTimeStamp( String msg, int adhere){
		String dtstr = "";
		String deltaTimeStr = "",cr="" ;
		int crp;
		
		if (measureTimeDelta==true){

			deltaTime = System.currentTimeMillis() - lastTime;
			lastTime = System.currentTimeMillis();
			
			if (deltaTime<1310123616){
				deltaTimeStr = ", " + deltaTime ;
			} 
		}
		crp = msg.indexOf("\n");
		if (crp>0){
			msg = msg.replace("\n"," ");
			cr = "\n" ;
		}
			
		if (adhere<=0){
			msg = msg + " " + dt.get()+deltaTimeStr ;
		}
		if (adhere==1){
			msg = msg + "_" + dt.get() ;
		}
		if (adhere>1){
			msg = msg + "  (" + dt.get()+deltaTimeStr+")" ;
		}
		msg = msg + cr ;

		return msg;
	}
	
	
	
	
}