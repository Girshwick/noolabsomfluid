package org.NooLab.utilities.files;

import java.util.ArrayList;
import java.util.Arrays;

import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.strings.StringsUtil;

public class TempFileCleaner {

	String basePath;
	String[] nameFilter;
	int hours=10;
	int count=5 ;
	
	ArrayList<String> excludedFolders = new ArrayList<String> ();
	
	DFutils fileutil = new DFutils();
	StringsUtil strgutil = new StringsUtil (); 
	// ========================================================================
	public TempFileCleaner( String basePath, String[] namesnips, 
							int hours, int count) {
		// 
		this.basePath = basePath;
		nameFilter = namesnips;
		this.hours = hours;
		this.count = count;
		
	}
	// ========================================================================
	
	/**
	 * folders are relative to basePath!
	 */
	public void setExcludedFolders( ArrayList<String> folders){
		excludedFolders.clear();
		if ((folders!=null) && (folders.size()>0)){
			excludedFolders.addAll(folders);
		}
	}
	
	public void setExcludedFolders( String[] folders){
		
		ArrayList<String> dirs = new ArrayList<String>(Arrays.asList(folders));
		setExcludedFolders(dirs) ;
	}
	
	
	public void addExcludedFolder( String folder){
		excludedFolders.add(folder);
	}
	
	
	public void go(){
		new Process();
	}
	
	class Process implements Runnable{

		Thread tfcPrc;
		
		public Process(){
			tfcPrc = new Thread (this,"tfcPrc");
			tfcPrc.start(); 
		}
		
		private void clean( String extension){
			
			// fileutil	  D:\data\iTexx\app\MuseGui\config

			String filename ;
			IndexedDistances xdfiles = new IndexedDistances ();
			IndexDistance xfile ;
		
			
			
			
			DirectoryContent dc = new DirectoryContent();
			ArrayList<String> filfilter   = new ArrayList<String>();
			filfilter.add( extension) ;
			
			// does not work correctly... also returns files without extension
			ArrayList<String> filenames = dc.completeListofFiles( basePath,filfilter , true);
			filenames.trimToSize() ;
			
			for (int i=0;i<filenames.size();i++){
			
				filename = filenames.get(i);
				filename = strgutil.replaceAll(filename, "\\", "/");
				
				if (fileMatchesExcludeds(filfilter, filename) == false ){
					continue;
				}
				
				long modTime = fileutil.getFileLastModificationTime(filename) ;
				if (fileMatchesExcludeds(excludedFolders, filename) ){
int k;
k=0;
				}
if (filename.toLowerCase().indexOf("common")>0){
int k;
k=0;
}
				if (fileMatchesExcludeds(excludedFolders,filename)==false){

					xfile = new IndexDistance(i, (double)(1.0*modTime), filename);
					xdfiles.add(xfile);
				}
			}
			
			xdfiles.sort(-1); // the oldest files == smaller time values are at higher index values
			int n=xdfiles.size();
			
			long timeInf = System.currentTimeMillis() - (long)(hours*3600*1000) ;
			
			for (int i=0;i<xdfiles.size();i++){
				xfile = xdfiles.getItem(i) ;
				if ((xfile.getDistance()<timeInf) || (i>count)){
					filename = xfile.getGuidStr() ;
					fileutil.deleteFile(filename) ;
				}
			}
			xdfiles.clear();
		}
		
		private boolean fileMatchesExcludeds(ArrayList<String> excludings, String filename) {
			// 
			boolean rB=false;
			
			for (int i=0;i<excludings.size();i++){
				String pathsnip = excludings.get(i);
				pathsnip = pathsnip.replace("*", "");
				rB = filename.toLowerCase().indexOf(pathsnip)>=0;
				if (rB){
					break;
				}
			}
			
			return rB;
		}

		@Override
		public void run() {
			// 
			
			for (int i=0;i<nameFilter.length;i++){
				if (nameFilter[i].trim().length()>1){
					clean(nameFilter[i]);
				}
			}
			count=0;
			clean("hs_err");
		}
		
		
		
	}
}
