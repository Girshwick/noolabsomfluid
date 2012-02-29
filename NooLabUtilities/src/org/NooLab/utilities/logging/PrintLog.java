package org.NooLab.utilities.logging;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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
	
	int printLevel = 1;
	boolean showTimeStamp;
	boolean onlyShowTimeDelta = false;
	boolean showDateValue = false;
	
	int extraMarkerOnLargeDelta = 0 ;
	
	int countlimit = 10;
	boolean isPrintFileLogging = false; 
	
	String prefix="";
	
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
	
	

	public PrintLog( int printlevel, boolean showtimestamp ){
		
		printLevel = printlevel;
		showTimeStamp = showtimestamp ;
		 
		dt.setStripSeparators(0);
		
		startTime = System.currentTimeMillis();
		
		logfilepath = filutil.getTempDir() ;
		logfilename = filutil.createPath( logfilepath ,logfilename );
	}
	

	
	public void measureTime(){
		measuredTime = System.currentTimeMillis();
		initialTimeStamp = addTimeStamp("",2) ;
 	}

	@SuppressWarnings("static-access")
	public void delay(int millis){
		try {
			Thread.currentThread().yield();
			Thread.currentThread().sleep(millis);
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
		
		if ((msg==null) || (msg.trim().length()==0)){
			return;
		}
		if (LogControl.globeScope <= 1){
			goforit = (level <= printLevel ) ;
		}
		if (LogControl.globeScope >= 2){
			goforit = ( level <= LogControl.Level ) ;
		}
			
		if (goforit){
			if (showTimeStamp){
				if (showTime==true){
					msg = addTimeStamp(msg,2) ;
				}
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
                if ((suppressPrefix==false) && (prefix.trim().length()>0)){
                	String cr="";
                	if (msg.startsWith("\n")){
                		msg = msg.substring(1,msg.length());
                		cr = "\n";
                	}
                	msg = cr + prefix+" "+msg;
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
		}
	}
	
	public void println( int level, String msg){
		prnerr = false;
		linefeed = 1;
		print( level, msg, true) ;
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
				 (incrCounter == 1) ) {

				perc = (double) ((1.0 * (double) incrCounter) / (1.0 * (double) totalcount))
						* ((double) 100.0);

				pstr = String.format("%." + fracdigits + "f", perc);

				if ((addmsg.length() == 0)
						|| (addmsg.trim().indexOf("[!]") != 0)) {
					msg = "   " + pstr + "% completed ...";

					if (addmsg.trim().length() > 0) {
						msg = msg + " ,  " + addmsg;
					}

				} else {
					addmsg = addmsg.replace("[!]", "");
					msg = "   " + pstr + addmsg;
				}

				print(level, msg);
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

		SimpleDateFormat dateFormat = new SimpleDateFormat(“HH:mm:ss”);
		dateFormat.setTimeZone(TimeZone.getTimeZone(“GMT”));
		
		elapsed = currentTime – startTime;

		System.out.println(dateFormat.format(new Date(elapsed)));
	 */
	
	
	public boolean isMeasureTimeDelta() {
		return measureTimeDelta;
	}

	public void setMeasureTimeDelta(boolean measureTimeDelta) {
		this.measureTimeDelta = measureTimeDelta;
	}
	
	public void setPrintLevel(int printLevel) {
		 
		this.printLevel = printLevel;
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
}
