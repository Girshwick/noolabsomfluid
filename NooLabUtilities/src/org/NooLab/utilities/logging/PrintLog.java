package org.NooLab.utilities.logging;

import java.io.File;
import java.util.ArrayList;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;



import org.NooLab.utilities.datetime.DateTimeValue;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.net.GUID;



 

/**
 * 
 * a helper class, organizing the output to console and to loggers by means of a "printout level"<br/>
 * it also provides a simple means for profiling (measuring execution times between steps within a particular thread)<br/>
 * 
 * we need some shared static object for the level
 */
public class PrintLog {

	 
	// =================================

	// object references ..............

	
	// main variables / properties ....
	
	public Vector<String>  diagnosticMsg = new Vector<String>() ;
	
	DateTimeValue dt = new DateTimeValue(3,0);
	boolean measureTimeDelta = true;
	
	static int printLevel = 1;
	boolean showTimeStamp;
	boolean onlyShowTimeDelta = false;
	boolean showDateValue = false;
	
	int extraMarkerOnLargeDelta = 0 ;
	
	int countlimit = 10;
	boolean isPrintFileLogging = false; 
	
	static String prefix="";
	
	Map<String,String> filters = new TreeMap<String,String>(); 
	
	// constants ......................
	
	String logfilename =  "log.txt" ;
	String logfilepath ="";
	 
	// volatile variables .............
	long now,lastTime, deltaTime,startTime , measuredTime, elapsedTime;
	String initialTimeStamp="", lastTimeStamp="";
	
	private int linefeed = 1;
	private boolean prnerr ;
	boolean suppressPrefix = false;
	
	File prnLogFile ;
	
	// helper objects .................

	DFutils filutil = new DFutils();

	private String collectedPrintOut = "";

	private boolean displayMemory;

	private boolean timeThresholding = false;
	private int delayThresholdingValue = 0;
	private long lastMeasuredTime = 0, dT=0;;

	private TimeAnchorIntf timeAnchor = null;
	
	
	// ========================================================================
	public PrintLog( int printlevel, boolean showtimestamp ){
		init( printlevel, showtimestamp ,"");
	}
	
	public PrintLog(  int printlevel, boolean showtimestamp , String prefix) {
		init( printlevel, showtimestamp , prefix);
	}

	public PrintLog(  int printlevel, String prefix) {
		init( printlevel, false , prefix);
	}
	// ========================================================================
	
	private void init( int printlevel, boolean showtimestamp , String prefix ){
		
		printLevel = printlevel;
		showTimeStamp = showtimestamp ;
		 
		dt.setStripSeparators(0);
		
		startTime = System.currentTimeMillis();
		
		logfilepath = filutil.getTempDir() ;
		logfilename = filutil.createPath( logfilepath ,logfilename );
		
	}
	// ------------------------------------------------------------------------
	public void measureTime(){
		measuredTime = System.currentTimeMillis();
		initialTimeStamp = addTimeStamp("",2) ;
 	}

	
	public void delay(int millis){
		Delay(millis) ;
	}
	
	@SuppressWarnings("static-access")
	public static void Delay(int millis) {
		try {
			Thread.currentThread().yield();
			if (millis>0){
				Thread.currentThread().sleep(millis);
			}else{
				Thread.currentThread().sleep(0, 1);
			}
		} catch (Exception e) {}
	}

	private void calc_elapsed_time(){
		long now;
		
		now = System.currentTimeMillis();
		
		elapsedTime = now - measuredTime;
		lastTimeStamp = addTimeStamp("",2) ; 
	}
	
	public long getElapsedTimeT(){
		calc_elapsed_time();
		return elapsedTime;
	}

	public double getElapsedTimeV(){
		
		double result;
		
		calc_elapsed_time();
		
		result = elapsedTime/1000 ;
		result = Math.round(result*1000)/1000 ;
		
		return result ;
	}
	
	public String getInitialTimeStamp(){
		return initialTimeStamp ;
	}
	
	
	public void showTimeSummary( int level,
		 	                     String msg1 ,    // "program has been started at ", 
						         String msg2 ,
						         String msg3 ) {  // "  time needed: ");
		String msg="" , hs1;
		
		
		hs1 = getInitialTimeStamp();
		hs1 = hs1.replace(")","");
		msg = msg1+ hs1 + msg2+ getElapsedTimeV()+msg3 +")";
		if (printLevel >= level){
			
			 
			System.out.println(msg);
		}
	}
	 
	public void showTimeSummary2( int level,
								  String msg1 ,    // "program has been started at ", 
								  String msg2 ,
								  String msg3 ) {  // "  time needed: ");
		String msg="" , hs1;

		hs1 = addTimeStamp("",2) ;
		hs1 = hs1.replace(")", "");
		msg = msg1 + hs1 + msg2 + getElapsedTimeV() + msg3 + ")";
		if ( printLevel >= level) {

			System.out.println(msg);
		}
	}
	
	
	public String addFilter( String filterForThis){
		String guid = GUID.randomvalue() ;
		filters.put(guid,filterForThis);
		return guid;
	}
	
	public void removeFilter( String guid){
		filters.remove(guid) ;
	}
	
	public void removeAllFilters( String guid){
		filters.clear() ;
	}
	
	private boolean filterMatch(){
		boolean rB=false;
		
		// TODO ...
		
		return rB;
	}
	
	/**
	 * Meaning of Levels<br/>
	 * -1,0= nothing, 1=proc task activity, 2=1+task finishing+time used, 3=diagnostic msg, 
	 *    4= content of variables <br/><br/>
	 * 
	 * @param level the level of the printout, has to be >= global print level in order to allow for printing the msg
	 * @param msg the string to be printed to the console
	 */
	public void print( int level, String msg, boolean showTime){
		boolean goforit=true;
		
		
		if ((displayMemory ==false) && (msg==null) || (msg.trim().length()==0)){
			return;
		}
		if (msg==null)msg="" ;
		
		if (LogControl.globeScope <= 1){
			goforit = (level <= printLevel ) ;
		}
		if (LogControl.globeScope >= 2){
			goforit = ( level <= LogControl.Level ) ;
		}
			
		if (goforit){
			
			long heapSize = Runtime.getRuntime().totalMemory()/(1024);
			long heapFreeSize = Runtime.getRuntime().freeMemory()/(1024);

			
			if (showTimeStamp){
				if (showTime==true){
					msg = addTimeStamp(msg,2) ;
				}
			}
			
			
            if ((suppressPrefix==false) && (prefix.trim().length()>0)){
            	String cr="";
            	if (msg.startsWith("\n")){
            		msg = msg.substring(1,msg.length());
            		cr = "\n";
            	}
            	msg = cr + prefix+" "+msg;
            }
            
            if (displayMemory){
    			msg = msg  + "  (free mem: "+ heapFreeSize+" of "+heapSize+")  " ;
    		}
            
            
			if (prnerr){
				System.err.print(msg);
				if (linefeed > 0) {
					System.err.println();
				}
				
			}else{
if ((msg==null) ){
	 
	return;
}				

                
                if (filters.size()>0){
                	// check the filters map for the prefix, remove if there is a match
                	if (filterMatch()){
                		return;
                	}
                }
				System.out.print(msg);
				if (linefeed > 0) {
					System.out.println();
				}
			}
			log2file(msg);
		} // goforit ?
		
		if ((timeThresholding) && (delayThresholdingValue>5) ){
			timeThresholding = false; // prevent nested re-entry
			now = System.currentTimeMillis() ;
			dT = now - lastMeasuredTime;
			
			if ((dT > delayThresholdingValue) & (dT<133000000)){
				
				if ((timeAnchor!=null) && (lastMeasuredTime>0)){
					timeAnchor.redirection(dT) ;
				} 
			}
			lastMeasuredTime = now ;
			dT=0;
			timeThresholding = true;
		}
	}
	
	public void println( int level, String msg){
		prnerr = false;
		linefeed = 1;
		print( level, msg, true) ;
	}
	
	public static void Print( int level, String msg){
		
		if (printLevel >= level){
			System.out.println(prefix +" "+ msg);
		}
	}
	
	public void print( int level, String msg){
		prnerr = false;
		suppressPrefix = false;
		print( level, msg, true) ;
		 
	}

	public void print( int level, boolean suppressprefix, String msg){
		prnerr = false;
		suppressPrefix = suppressprefix;
		print( level, msg, true) ;
		 
	}
	
	public void printlnErr( int level, String msg){
		linefeed = 1;
		prnerr = true;
		suppressPrefix = false;
		print( level, msg, true) ;
	}
	
	public void printErr( int level, String msg){
		 
		prnerr = true;
		suppressPrefix = false;
		
		if (prnerr==false){
			prnerr = true;
		}
		print( level, msg, true) ;
		 
		
	}
	
	public void print( int level, String msg, int limit){
		suppressPrefix = false;
		
		if (printLevel >= level){
			if (limit <= countlimit){
				if (showTimeStamp){
					msg = addTimeStamp(msg,2) ;
				}
				if (msg.startsWith("\n")){
					prefix = "\n"+prefix;
					msg = msg.substring(1,msg.length());
				}
				System.out.println(prefix +" "+ msg);
			}
		}
	}
	
	
	public void println(  int level , boolean showTime ){
		String msg="\n";
		
		print(level, showTime);
		System.out.print("");
	}

	public void print(  int level , boolean showTime ){
		String msg="\n";
		
		if (printLevel >= level){
			
			if (showTime){
				msg = addTimeStamp(msg,2) ;
				
			}
			if (msg.startsWith("\n")){
				prefix = "\n"+prefix;
				msg = msg.substring(1,msg.length());
			}
			System.out.print(prefix +" "+ msg);
		}
	}

	public void fileLogging(){
		
	}
	
	protected void log2file( String msg ){
		
		String fildatestr="";
		
		try{

			if ( isPrintFileLogging ){
			
				if (filutil.fileexists(logfilepath)==false){
					filutil.writeFileSimple( logfilepath, "") ;
				}
				
				filutil.appendToFile( logfilepath, msg) ;
				
			}
			
		}catch(Exception e){
			
		}
	}
	
	public void printCompletionTime(int level, long starttime, int incrCounter, int totalcount, int threadFactor, boolean displayPercentage, String addmsg) {
		long dT,currtime = System.currentTimeMillis() ;
		
		dT = currtime-starttime;
		if (dT<60000){
			return ;
		}
		
		int remainingSteps = totalcount-incrCounter;
		double avgTimePerStep = (double)dT/((double)(incrCounter));
		
		long remainingTime = (long)Math.round( ((double)remainingSteps*avgTimePerStep/(double)1));// threadFactor
		
		
		int sec = (int) ((remainingTime / 1000) % 60);
		int min = (int) ((remainingTime / (1000 * 60)) % 60);
		int hours = (int) ((remainingTime / (1000 * 60 * 60)) % 24);
		int days = (int) ((remainingTime / (1000 * 60 * 60 * 24)) % 7);
		int weeks = (int) (remainingTime / (1000 * 60 * 60 * 24 * 7));
		
		String str="";
		if (days>0){
			str = days+" days, ";
		}
		String hs,ms,st ;
		
		hs = ""+hours; if (hours<10)hs="0"+hs;
		ms = ""+min;   if (min<10)  ms="0"+ms;
		st = ""+sec;   if (sec<10)  st="0"+st;
		
		str = str + hs+":"+ms+":"+st;
		
		if (displayPercentage){
			double prc = ((double)incrCounter/(double)totalcount)*100 ;
			str = str + "  "+ String.format("%.1f", prc)+"% done (index:"+incrCounter+" of "+totalcount+")";
		}
		println( level, str);
	}
	
	/**
	 * 
	 * if additional message string begins with "[!]", then the built-in message will be completely overwritten
	 * 
	 * @param level
	 * @param inc
	 * @param totalcount
	 * @param stepwidth
	 * @param addmsg
	 */
	public void printprc( int level, int incrCounter, int totalcount , int stepwidth , String addmsg){
		printPrc( level, incrCounter, totalcount , stepwidth , addmsg);
	}
	
	public static void printPrc( int level, int incrCounter, int totalcount , int stepwidth , String addmsg){
		double perc ;
		int fracdigits = 0;
		String pstr, msg="";
		try{
			if (stepwidth<1) {stepwidth=1;};
			if (stepwidth>=(totalcount-1)) {
				stepwidth= (int) Math.round( ((double)(totalcount*1.0))/(0.3335));
			};

			if ( ((incrCounter % stepwidth == 0) && (incrCounter > 10) && (totalcount > 0)) || 
				 (stepwidth == 1) ||
				 (incrCounter <= 0) ) {

				perc = (double) ((1.0 * (double) incrCounter) / (1.0 * (double) totalcount)) * ((double) 100.0);

				pstr = String.format("%." + fracdigits + "f", perc);

				if ((addmsg.length() == 0) || (addmsg.trim().indexOf("[!]") != 0)) {
					
					msg = "   " + pstr + "% completed ";
					if (addmsg.length()==0){
						msg = msg + "..." ;
					}
					if (addmsg.trim().length() > 0) {
						msg = msg + addmsg + " ...  ";
					}

				} else {
					addmsg = addmsg.replace("[!]", "");
					msg = "   " + pstr + addmsg;
				}

				// print(level, msg);
				if (level<= LogControl.Level){
					System.out.println(msg);
				}
			}

		}catch(Exception e){
			// exceptions not relevant here
		}
	
	}
	
	public String addTimeStamp( String msg, int adhere){
		String dtstr = "",finalDTstr;
		String deltaTimeStr = "",cr="" ,msgWICR,startingCR, endingCR ;
		int crp;
		
		msgWICR = msg;
		
		if (msg==null){
			return "";
		}
		startingCR="";
		endingCR ="" ;
		
		if (msg.startsWith("\n")){
			startingCR = "\n";
		}
		if (msg.endsWith("\n\r")){
			endingCR = "\n\r";
		}
		if (msg.endsWith("\n")){
			endingCR = "\n";
		}
		
		msg = msg.trim() ;
		
		if (measureTimeDelta==true){

			deltaTime = System.currentTimeMillis() - lastTime;
			lastTime = System.currentTimeMillis();
			
			if (deltaTime<1310123616){
				deltaTimeStr = ", " + deltaTime ;
			} 
		}
		 
			
		finalDTstr = dt.get();
		if (onlyShowTimeDelta){
			
			finalDTstr = "";
			deltaTimeStr = "dt = "+ deltaTime + " ms";
			
			if ((extraMarkerOnLargeDelta < deltaTime) && (extraMarkerOnLargeDelta >0)){
				deltaTimeStr = deltaTimeStr + "   <<<<<<<<<<<    <<<<<<<<<<<    <<<<<<<<<<<  " ;
			}
		}
		
		if (adhere<=0){
			msg = msg + " " + finalDTstr +deltaTimeStr ;
		}
		if (adhere==1){
			msg = msg + "_" + finalDTstr ;
		}
		if (adhere>1){
			msg = msg + "  (" + finalDTstr+deltaTimeStr+")" ;
		}
		msg = startingCR + msg + endingCR  ;

		return msg;
	}
	/*

	  	long startTime = System.currentTimeMillis();
		long currentTime = System.currentTimeMillis();

		SimpleDateFormat dateFormat = new SimpleDateFormat(�HH:mm:ss�);
		dateFormat.setTimeZone(TimeZone.getTimeZone(�GMT�));
		
		elapsed = currentTime � startTime;

		System.out.println(dateFormat.format(new Date(elapsed)));
	 */
	
	
	public boolean isMeasureTimeDelta() {
		return measureTimeDelta;
	}

	public void setMeasureTimeDelta(boolean measureTimeDelta) {
		this.measureTimeDelta = measureTimeDelta;
	}
	
	public void setPrintLevel(int printlevel) {
		 
		printLevel = printlevel;
	}

	public void setLogControlLevel(int level) {
		 
		LogControl.Level = level;
	}

	public int getPrintlevel(){
		int _level;
		
		_level = printLevel;
		
		if (LogControl.globeScope >= 2){
			_level = LogControl.Level ;
		}
		
		return _level;
	}


	public boolean isShowDateValue() {
		return showDateValue;
	}


	public void setShowDateValue(boolean showDateValue) {
		this.showDateValue = showDateValue;
	}


	public void setShowTimeStamp(boolean showTimeStamp) {
		this.showTimeStamp = showTimeStamp;
	}


	public void setOnlyShowTimeDelta(boolean onlyShowTimeDelta) {
		this.onlyShowTimeDelta = onlyShowTimeDelta;
	}


	public void setExtraMarkerOnLargeDelta(int extraMarkerOnLargeDelts) {
		this.extraMarkerOnLargeDelta = extraMarkerOnLargeDelts;
	}


	public boolean isPrintFileLogging() {
		return isPrintFileLogging;
	}


	public void setPrintFileLogging(boolean isPrintFileLogging) {
		this.isPrintFileLogging = isPrintFileLogging;
		
		if (isPrintFileLogging){
			fileLogging();
		}
	}


	public void setCountlimit(int countlimit) {
		this.countlimit = countlimit;
	}


	public int getLinefeed() {
		return linefeed;
	}


	public void setLinefeed(int linefeed) {
		this.linefeed = linefeed;
	}


	/**  returns the full filepath to the file, which logs the console print outs */
	public String getLogfilepath() {
		return logfilepath;
	}


	public void setLogfilepath(String logfilepath) {
		this.logfilepath = logfilepath;
	}



	public String getPrefix() {
		return prefix;
	}



	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
 
	public void resetPrintOutCollection() {
		
		collectedPrintOut = "" ;
	}
 
	public void collectPrintOut(String cOutStr) {

		collectedPrintOut = collectedPrintOut + cOutStr + "\n"; 
	}



	public String getPrintOutCollection() {
		
		return collectedPrintOut;
	}

	public void setDisplayMemory(boolean flag) {
		
		displayMemory = flag;
	}

	public void setTimeThresholdingAnchor(TimeAnchorIntf callbackClass) {
		//  
		timeAnchor =  callbackClass ;
	}

	public void setTimeThresholding( boolean flag, int delayValue) {
		lastMeasuredTime = 0;
		timeThresholding = flag;
		delayThresholdingValue = delayValue ;
	}

	public void resetTimeThresholding() {
		lastMeasuredTime = System.currentTimeMillis() ;
	}


}
