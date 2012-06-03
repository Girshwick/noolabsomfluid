package org.NooLab.utilities.files;



import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.strings.*;
 



/**
 * 
 * 
 * 
 *
 */
public class DirectoryContent extends Observable  {
	 
	 
	
	Vector<String> filenames = new Vector<String>() ;
	
	File[] files ;
	File[] subdirectories ;
	 
	int limitCount = -1;
	
	String descriptionFile = "";
	
	boolean stopped = false;
	int totals=0;
	int interval = -1 ;
	
	int diagnosticPrintOut = 0;
	
	StringsUtil strgutil = new StringsUtil();
	 
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	

	public DirectoryContent( int printlevel) {
		diagnosticPrintOut = printlevel;
	}

	public DirectoryContent() {
		 
	}
 
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	

	 
	public void setupObservation( Observer observingclass ){
		
		_initObservation(observingclass,-1) ;
	}
	public void setupObservation( Observer observingclass, int intervall){
			
		_initObservation(observingclass,intervall) ;		
	}
	
	private void _initObservation(Observer observingclass, int intervall){
		interval = intervall;
		addObserver(observingclass);
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	
	
	
	public int getLimitCount() {
		return limitCount;
	}

	public void setLimitCount(int limitCount) {
		this.limitCount = limitCount;
	}

	public void setDiagnosticPrintOut(int diagnosticPrintOut) {
		this.diagnosticPrintOut = diagnosticPrintOut;
	}

	public int enumerateFiles( String namefilter, String extension, String directoryPath) {
		int n;
		
		filenames.clear() ;
		n = getFileList( namefilter, extension, directoryPath).size() ;
		
		return n;

	}
	
	public int enumerateFiles( String namefilter, String directoryPath) {
		 
		return enumerateFiles( namefilter, "", directoryPath) ;
	}
	
	public Vector<String> getFileList( String namefilter, String directoryPath) {
		
		return getListOfFileList( namefilter, "", directoryPath);
	}
	
		
	public static ArrayList<String> getFileList( final String namefilter, final String extension, String directoryPath) {
		String[] children;
		
		File tmpffil;
		String filename, str ,fname, tmp ;
		FilenameFilter filter ;
		int k,p;
		boolean hb;
		ArrayList<String> filenames = new ArrayList<String>() ;
		
		directoryPath = StringsUtil.replaceall(directoryPath, "\\", "/")+"/";
		directoryPath = StringsUtil.replaceall(directoryPath, "//", "/") ;
		
		filenames.clear() ;
		
		File dir = new File(directoryPath);
		/*
		children = dir.list();
		if (children == null) {
			// Either dir does not exist or is not a directory
		} else {
			
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				// filename = children[i];
			}
		}
		 */
		// It is also possible to filter the list of returned files.
		// This example does not return any files that start with `.'.
		filter = new FilenameFilter() {
										 public boolean accept(File dir, String name) {
											 boolean hb = true;
											 	hb = !name.startsWith(".");
											 	if ((hb) && (namefilter.length()>0)){ 
											 		hb = name.contains(namefilter) ;  
											 	}
											 	if ((hb) && (extension.length()>0)){ 
											 		hb = name.endsWith(extension) ;  
											 	}
											 	
											 	
											 	return hb ;
										 }
									  };
									  
       // filter = new FFilter( filfilter, dirfilter );
        
									 
		children = dir.list(filter);
		 
		k = directoryPath.length();
		if (k>1){
			tmp = directoryPath.trim().substring(k - 1, k);

			if (tmp.contentEquals("/")==false) {
				directoryPath = directoryPath+"/" ;
			}
		}
		
		for (int i=0;i<children.length;i++){
			// stuff path to the string
			str = children[i] ;
			
			hb = str.toLowerCase().contains(extension.toLowerCase()) ;
			if (hb){
				str = directoryPath + str ;
			} else{
				str="";
			}
			children[i] = str;
		}
		
		 
		
		if ((children!=null) && (children.length>0)){
			filenames = new ArrayList<String>(Arrays.asList(children));
		}
		
		
		int i=filenames.size()-1 ;
		while (i>=0){
			
			if ((filenames.get(i)==null) || (filenames.get(i).trim().length()==0)){
				filenames.remove(i);
			}
			i--;
		}
	    return filenames;
	}
	
	public Vector<String> getListOfFileList( String namefilter, String extension, String directoryPath) {
		String[] children;
		
		File tmpffil;
		String filename, str ,fname, tmp;
		FilenameFilter filter ;
		int k,p;
		boolean hb;
		
		
		directoryPath = strgutil.replaceAll(directoryPath, "\\", "/")+"/";
		directoryPath = strgutil.replaceAll(directoryPath,"//","/" ) ;
		
		filenames.clear() ;
		
		File dir = new File(directoryPath);
		/*
		children = dir.list();
		if (children == null) {
			// Either dir does not exist or is not a directory
		} else {
			
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				// filename = children[i];
			}
		}
		 */
		// It is also possible to filter the list of returned files.
		// This example does not return any files that start with `.'.
		filter = new FilenameFilter() {
										 public boolean accept(File dir, String name) {
											 boolean hb = true;
											 	hb = !name.startsWith(".");
											 	
											 	return hb ;
										 }
									  };
									  
       // filter = new FFilter( filfilter, dirfilter );
        
									 
		children = dir.list(filter);
		 
		k = directoryPath.length();
		if (k>1){
			tmp = directoryPath.trim().substring(k - 1, k);

			if (tmp.contentEquals("/")==false) {
				directoryPath = directoryPath+"/" ;
			}
		}
		
		for (int i=0;i<children.length;i++){
			// stuff path to the string
			str = children[i] ;
			
			hb = str.toLowerCase().contains(extension.toLowerCase()) ;
			if (hb){
				str = directoryPath + str ;
			} else{
				str="";
			}
			children[i] = str;
		}
		
		 
		
		if ((children!=null) && (children.length>0)){
			filenames = new Vector<String>(Arrays.asList(children));
		}
		if (filenames==null){
			filenames = new Vector<String>();
		}
		
		int i=filenames.size()-1 ;
		while (i>=0){
			
			if ((filenames.get(i)==null) || (filenames.get(i).trim().length()==0)){
				filenames.remove(i);
			}
			i--;
		}
	    return filenames;
	}
	
	public static File[] getSubFolders( String namefilter, String directoryPath) {
		
		String[] children;
		FileFilter fileFilter;
		
		String filename;
		final String filter= namefilter;
		 
		File[] subdirectories;
		
		File dir = new File(directoryPath);

		children = dir.list();
		if (children == null) {
			// Either dir does not exist or is not a directory
		} else {
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				filename = children[i];
			}
		}

		 
		
		// This filter only returns directories
		fileFilter = new FileFilter() {  
													public boolean accept(File file) {
														
														boolean hb=false;
														String _filter, pathname = file.getName();
														// exclude path ... n.i.y.
														_filter = filter;
														
														hb = file.isDirectory();
														if (hb){
															if (filter.contentEquals("*")){
																_filter="";
															}
															if ((_filter.length()>0)){
																hb = (pathname.indexOf(_filter)>=0);
															}
														}
				
														return hb ;
													}
								 
												 };
	    subdirectories = dir.listFiles(fileFilter);
	    return subdirectories;
	}
	
	public File[] getSubDirectories( String namefilter, String directoryPath) {
		
		String[] children;
		FileFilter fileFilter;
		
		String filename;
		final String filter= namefilter;
		 
		
		File dir = new File(directoryPath);

		children = dir.list();
		if (children == null) {
			// Either dir does not exist or is not a directory
		} else {
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				filename = children[i];
			}
		}

		 
		
		// This filter only returns directories
		fileFilter = new FileFilter() {  
													public boolean accept(File file) {
														
														boolean hb=false;
														String _filter, pathname = file.getName();
														// exclude path ... n.i.y.
														_filter = filter;
														
														hb = file.isDirectory();
														if (hb){
															if (filter.contentEquals("*")){
																_filter="";
															}
															if ((_filter.length()>0)){
																hb = (pathname.indexOf(_filter)>0);
															}
														}
				
														return hb ;
													}
								 
												 };
	    subdirectories = dir.listFiles(fileFilter);
	    return subdirectories;
	}
	
	public String getDescriptionFile() {
		return descriptionFile;
	}

	public void setDescriptionFile(String descriptionFile) {
		this.descriptionFile = descriptionFile;
	}

	class FFilter implements FilenameFilter{

		
		
		Vector<String> filefilters = new Vector<String> () ;
		Vector<String> dirfilters  = new Vector<String> () ;
		Vector<String> fileRegexFilter = new Vector<String>() ;
		Vector<String> dirRegexFilter  = new Vector<String>() ;
		
		
		boolean anyPositiveDefsDir;
		boolean anyPositiveDefsFil;
		boolean anyNegativeDefsDir;
		boolean anyNegativeDefsFil;
		
		
		public FFilter( Vector<String> filters , Vector<String> dirfilters){
			int p ;
			
			this.filefilters = filters ;
			this.dirfilters  = dirfilters ;

			p = dirfilters.indexOf("*");
			if (p>=0){
				dirfilters.remove(p) ;
			}

			p = filefilters.indexOf("*");
			if (p>=0){
				filefilters.remove(p) ;
			}

			
			anyPositiveDefsDir = checkforpositives(dirfilters) ; 
			anyPositiveDefsFil = checkforpositives(filefilters) ;  
			
			anyNegativeDefsDir = checkfornegatives(dirfilters) ;
			anyNegativeDefsFil = checkfornegatives(filefilters) ;
			
			// translate filters into regex'es, if they contain \ [*] [+] or escaped " 
		}
		
		private boolean checkforpositives( Vector<String> filterstr){
			boolean rb = false;
			int i,p;
			String str;
			
			for(i=0;i<filterstr.size();i++){
				str = filterstr.get(i) ;
				if ( (str.indexOf("[!]")<0) ){
					rb = true;
					break;
				}
			}
				
			return rb;
		}
		private boolean checkfornegatives( Vector<String> filterstr){
			boolean rb = false;
			int i,p;
			String str;
			
			for(i=0;i<filterstr.size();i++){
				str = filterstr.get(i).trim() ;
				if ( (str.indexOf("[!]")>=0) ){
					rb = true;
					break;
				}
			}
				
			return rb;
		}
		
		
		
		
		public boolean accept(File fil_dir, String name) {

			boolean accepted = true;
			String ext = "";
			int p=0;

            accepted = !name.startsWith(".");
            if (!accepted){
            	return accepted ;
            }

            
			// java.isDirectory() does not work properly, it recognizes .mp3 files as a directory !!!
			p = name.indexOf(".");
if (name.contentEquals("_a")){
	p=p+0;
}

			if ( (p<0) && (fil_dir.isDirectory()) ) {
				
				
				
				if (dirfilters.size()>0){

					
					
					
					if (accepted) {
						if ((anyPositiveDefsDir) &&(dirfilters.size() > 0)) {

							accepted = (dirfilters.indexOf(name) >= 0);
						}
					}
					if (accepted) {
						accepted = (dirfilters.indexOf("[!]" + name) < 0);
					}
					
					// check for regex'es
					if (accepted) {
						if (dirRegexFilter.size()>0){
							
						}
					}
					
				} // any dirfilters around?
				else{
					
				}
			} else {
				// a file !

				accepted = !name.startsWith(".");

				if ((accepted) && (filefilters.size() > 0)) {
					
					p = name.lastIndexOf(".");
					if (p > 0) {
						ext = name.substring(p, name.length());
					}

					if (accepted) {
						if (ext.length() > 0) {
							accepted = (filefilters.indexOf(ext) >= 0);
						}
					} // already accepted -> next test

					if (accepted) {
						if (ext.length() > 0) {
							accepted = (filefilters.indexOf("[!]" + ext) < 0);
						}
					} // already accepted -> next test

					// check for name snips
					if (accepted) {

					}

					// check for regex'es
					if (accepted) {

					}

				} // filefilters.size()>0
				else{
					
				}
			} // not a directory ?

			return accepted;
		}
		
	}
	
	public Vector<String> completeListofFiles( ){
		
		
		// descriptionFile = settings.getResourceDescriptionsPath() + settings.getDefaultResourceDescriptionFile() ;
		
		return completeListofFiles( descriptionFile );
		 
	}
	
	/**
	 * 
	 * expects a full file path for descriptionFile
	 * 
	 * @param descriptionFile
	 * @return
	 */
	public Vector<String> completeListofFiles( String descriptionFile ){
		// resourceDescriptionsPath
	
		
		
		return null;
	}
	
	/**
	 * 
	 * 
	 * @param directory
	 * @param filterStr a Vector list of filters, either extensions (then start with "."), name snips, or regex'es
	 * 
	 * @param recurse
	 * @return
	 */
	public Vector<String> completeListofFiles( String directory, Vector<String> filterStr, boolean recurse){
		Vector<String> filenames = new Vector<String>() ;
		
		filenames = completeListofFiles( directory, filterStr, null, recurse) ;
		
		return filenames;
	}
	
	
	public Vector<String> completeListofFiles( Vector<String> directories, Vector<String> filfilter, Vector<String> dirfilter, boolean recurse){
		Vector<String> filenames = new Vector<String>() ;
		Vector<String> ff;
		
		for (int i=0;i<directories.size();i++){
			
			try{
				ff=null;
				
				ff = completeListofFiles( directories.get(i) , filfilter, dirfilter, recurse);
				
			}catch(Exception e){
				ff=null;
			}
			
			if (ff!=null){
				filenames.addAll( ff ) ;
			}
			
		} // i -> all directories
		
		 
		
		return filenames;
	}
	
	public Vector<String> completeListofFiles( String directory, Vector<String> filfilter, Vector<String> dirfilter, boolean recurse){
		File[] files;
		File dir ;
		FilenameFilter filter =null ;
		 
		// ................................................
		
		dir = new File(directory) ;
		
		filter = new FFilter( filfilter, dirfilter );
		  
		// this will run recursive, if requested
		files = listFilesAsArray( dir, filter, recurse);
		
		for (int i=0;i<files.length;i++){
			filenames.add(files[i].getAbsolutePath());
		}
		
		return filenames;
	}
		
		
	public Vector<String> getListofCollectedFiles() {
		return filenames;
	}

	public File[] listFilesAsArray( File directory, FilenameFilter filter, boolean recurse){
		File[] arr ;
		File[] files;
		Collection<File> cfiles ;
		
		// ................................................
		
		cfiles = listFiles(directory, filter, recurse);
 		
		arr = new File[cfiles.size()];
		files = cfiles.toArray(arr);
		
		setChanged();
	    notifyObservers( files.length );
	      
		return files;
	}
	

	private Collection<File> listFiles( File directory, FilenameFilter filter, boolean recurse) {

		File[] entries = new File[0]; // null-safe
		
		// List of files / directories
		Vector<File> files = new Vector<File>();
		
		
		if (stopped==true){
			return files;
		}

	      
		// Get files / directories in the directory
		entries = directory.listFiles();
		
		// Go over entries
		for (File entry : entries){
 
			// If there is no filter or the filter accepts the 
			// file / directory, add it to the list
			if (filter == null || filter.accept( directory, entry.getName())) {
				
				if (entry.isDirectory()){
					if (recurse){
						// System.out.println( entry.getName() );
						files.addAll(listFiles(entry, filter, recurse));
					}
					
				}
				if (entry.isDirectory()==false){
					files.add(entry);
					incCount();
					if (stopped){
						break ;
					}
				}
			}
			
		
		}
		
		// Return collection of files
		return files;		
	}
	
	
	private void incCount(){
		
		totals++;
		if ((limitCount>0) && (totals > limitCount)){
			stopped = true;
		}
		
		if ((interval>0) && (totals==1)){
			if (diagnosticPrintOut>=3) {
		      setChanged();
		      notifyObservers(totals );
			}
		}
		
		if (totals % interval == 0){
			if (diagnosticPrintOut >=3 ) {
				setChanged();
				notifyObservers(totals);
			}
		}

		
	}

	
	// ========================================================================
	
	public void defineDirWatcher(){
		/* thats for java7 !!!
		Path dir = "";
		try {
		    WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		
		} catch (IOException x) {
		    System.err.println(x);
		}
		*/
	}
	
	// ========================================================================
	interface P {
		public boolean accept(String t);
	}
	
	@SuppressWarnings({ "unused" })
	private void findZip(String f, InputStream in, P p, List<String> r) throws IOException {
		
		ZipInputStream zin = new ZipInputStream(in);

		ZipEntry en;
		while ((en = zin.getNextEntry()) != null) {
			if (p.accept(en.getName())){
				r.add(f + "!" + en);
			}
			if (isZip(en.getName())){
				findZip(f + "!" + en, zin, p, r);
			}
		}
	}

	static String[] ZIP_EXTENSIONS = { ".zip", ".jar", ".war", ".ear" };

	static boolean isZip(String t) {
		for (int i = 0; i < ZIP_EXTENSIONS.length; i++) {
			if (t.endsWith(ZIP_EXTENSIONS[i])) {
				return true;
			}
		}
		return false;
	}


	private String createPath( String pathsnip1, String pathsnip2 ){
		String path ="" ;
		 boolean onlycheckPath = false;
		 
		try{
			path=".";
			pathsnip2 = strgutil.replaceAll(pathsnip2, "::", "/");
			pathsnip2 = strgutil.replaceAll(pathsnip2, ":", "");
			pathsnip2 = strgutil.replaceAll(pathsnip2, "//", "/");
			
			path = pathsnip1 + "/" + pathsnip2;
			
			path = path.replaceAll("//", "/") ;
			path = path.replaceAll("\\\\", "/") ;
			path = path.replaceAll("\\//", "/") ;
			path = path.replaceAll("//", "/") ; 
			path = strgutil.replaceAll(path,"./", "/") ;
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

	public IndexedDistances createlistbyage(File[] folders, int ftype ,int sortDirection) {
		return createListByAge( folders,ftype, sortDirection);
	}
	
	public static IndexedDistances createListByAge(File[] folders, int ftype , int sortDirection) {
		
		IndexedDistances ixds = new IndexedDistances();
		IndexDistance ixd;
		File f;
		long lastModified;
		
		try{
			
			for (int i=0;i<folders.length;i++){
				f = folders[i];
				if ((ftype==1) && (f.isFile())){
					lastModified = f.lastModified();
					ixd = new IndexDistance(i, (double)(1.0*lastModified),f.getName());
					ixds.add(ixd);
				}else{
					if ((ftype == 2) && (f.isDirectory())) {
						lastModified = f.lastModified();
						ixd = new IndexDistance(i, (double) (1.0 * lastModified), f.getName());
						ixds.add(ixd);
					}else{
						if (ftype == 3) {
							lastModified = f.lastModified();
							ixd = new IndexDistance(i, (double) (1.0 * lastModified), f.getName());
							ixds.add(ixd);
						}
					}
				}
			}
			
			ixds.sort(sortDirection) ;
		}catch(Exception e){
			
		}
		
		return ixds;
	}

	public static IndexedDistances createListByAge(ArrayList<String> filenames, int ftype, int sortDirection) {
		
		File[] files = new File[filenames.size()] ;
		for (int i=0;i<filenames.size();i++){
			files[i] = new File(filenames.get(i)) ; 
		}
		
		return createListByAge( files, ftype, sortDirection) ;
	}
	 
	
	
}



