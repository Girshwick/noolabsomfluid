package org.NooLab.utilities.files;

import java.util.ArrayList;
import java.util.Arrays;

import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;

public class TempFileCleaner {

	String basePath;
	String[] nameFilter;
	int hours=10;
	int count=5 ;
	
	DFutils fileutil = new DFutils();
	
	public TempFileCleaner(String basePath, String[] namesnips, int hours, int count) {
		// 
		this.basePath = basePath;
		nameFilter = namesnips;
		this.hours = hours;
		this.count = count;
		
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
			
			ArrayList<String> filenames = dc.completeListofFiles(basePath,filfilter , true);
			filenames.trimToSize() ;
			for (int i=0;i<filenames.size();i++){
				filename = filenames.get(i);
				
				long modTime = fileutil.getFileLastModificationTime(filename) ;
				
				xfile = new IndexDistance(i, (double)(1.0*modTime), filename);
				xdfiles.add(xfile);
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
