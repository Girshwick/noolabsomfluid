package org.NooLab.utilities.datetime;

import java.text.SimpleDateFormat;
import java.util.Date;

// LOC 60

/**
 * 
 * content : 1 = date  <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *           2 = time  <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *           3, 13 = "HH:mm:ss.SSS"  <br/> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *           4, 14 = date + {3,13}  <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *           > 10 remove all non-numerical chars  <br/>
 *   <br/>   
 * 
 * mode    : 0 = "dd/MM/yyyy" ; 1 = "yyyyMMdd" ;   <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *           mode=1 is better suited for sorting items  <br/>  <br/>
 * 
import java.util.Calendar; 
import java.text.SimpleDateFormat; 
public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss"; 
 
public static String now() { 
Calendar cal = Calendar.getInstance(); 
SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW); 
return sdf.format(cal.getTime()); 
} 

 */
public class DateTimeValue {

	Date currentDateTime ;
	
	int content; 
	int mode;
	String dateformat = "dd/MM/yyyy";
	
	String replacementChar ="";
	int stripSeparators = 1;
	
	public String DateTimeValue="" ;
	
    public DateTimeValue(int content, int mode){
    	this.content = content; 
    	this.mode = mode;
    	get();
    }
    
    public String get(int _mode){
    	
    	mode = _mode ;
    	return get() ;
    }
    
    public String get(){
    	if (mode<=0){
    		dateformat = "dd/MM/yyyy" ;
    	}
    	if (mode==1){
    		dateformat = "yyyyMMdd" ;
    	}
    	return get(dateformat) ;
    }
    
	public String get(String formatStr) {
		// TODO Auto-generated method stub
	 
    
    	SimpleDateFormat sdfTime;
    	SimpleDateFormat sdfDate;
    	
    	String strTime="" , strDate="";
    	dateformat = formatStr;
    	
    	
    	
    	sdfDate = new SimpleDateFormat(dateformat);
	    sdfTime = new SimpleDateFormat("HH:mm:ss");

	    
	    Date now = new Date();
	    
	    currentDateTime = now ;
	   
	    strDate = sdfDate.format(now);
	    strTime = sdfTime.format(now);
	    
	    // System.out.println("Date: " + strDate);
	    // System.out.println("Time: " + strTime);
	    
	    if (content<=1){
	    	DateTimeValue = strDate;
	    }
	    if ((content==2) || (content-10==2)){
	    	DateTimeValue = strTime;
	    }	    
	    
	    if ((content==3) || (content-10==3)){
	    	sdfTime = new SimpleDateFormat("HH:mm:ss.SSS"); 
	    	strTime = sdfTime.format(now);
	    	
	    	DateTimeValue = strTime;
	    }
	    
	    if ((content==4) || (content-10==4)){
	    	DateTimeValue = strDate + " "+ strTime;  
	    }
	    if ((content>=10) || (content==4)) {
	    	if (stripSeparators>0){
	    		DateTimeValue = DateTimeValue.replace(":", "");
	    		DateTimeValue = DateTimeValue.replace("-", "");
	    		DateTimeValue = DateTimeValue.replace("/", "");
	    		DateTimeValue = DateTimeValue.replace(".", "");
	    		DateTimeValue = DateTimeValue.replace(" ", replacementChar);
	    	}
	    }
	    return DateTimeValue;
	}

    /**
     *<p>
     * takes a datetime string as created, and transformes it into
     * a sequence of characters according to the type of encoding.</p>
     * 
     * encodingtype:<br/>
     * - 1 = integer from by code of chars at positions  <br/>
     * - 2 = 1, encoded into a hex string  <br/>
     * - 3 = 1, + random value  <br/>
     * - 4 = 2, + random value  <br/>
     * - >10 {11,12,13,14}, everything as sum, instead as concatenation of positional code values  <br/>
     * <br/><br/>
     * @param encodingtype
     * @return
     */
    public String getEncoded( int encodingtype){
    	String encStr="";
    	String dtstr = "", csStr="";
    	char ch;
    	int c, cs=0, enctype;
    	
    	try{
    		
    		enctype = encodingtype;
        	if (enctype>10){
        		enctype=enctype-10;
        	}
        	
        	if (enctype==1){
        		
        		dtstr = get();
        		if ((dtstr==null) || (dtstr.length()<=1)){
        		  	content = 13; 
        	    	mode = 1;
        	    	dtstr = get();
        		}
        		for (int i=0;i<dtstr.length();i++){
        			ch = dtstr.charAt(i) ;
        			c = (int)ch; 
        			c= Math.abs(c-47);
        			csStr = csStr + "" + c;
        			cs = cs + c;
        		} // i->
        		
        		if (encodingtype<10){
        			
            		if (csStr.length()>6){
            			csStr = csStr.substring( csStr.length()-5, csStr.length());
            		}        			 
        			
        			if (csStr.length()>0){
        				cs = Integer.parseInt(csStr);
        			} else {
        				cs = 1;
        			}
        			encStr = ""+cs;        			
        		}

        		
        	} // enctype == 1 ?
        	
        	
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	
    	return encStr;
    }
    
    
    // ------------------------------------------------------------------------
	public String getReplacementChar() {
		return replacementChar;
	}

	public DateTimeValue setReplacementChar(String replacement) {
		replacementChar = replacement;
		return this ;
	}

	public int getStripSeparators() {
		return stripSeparators;
	}

	public DateTimeValue setStripSeparators(int stripseparators) {
		stripSeparators = stripseparators;
		return this;
	}

	public Date getCurrentDateTime() {
		return currentDateTime;
	}


	    
}

/*
     see:  http://javatechniques.com/blog/dateformat-and-simpledateformat-examples/
     
{
        // Make a new Date object. It will be initialized to the current time.
        Date now = new Date();

        // See what toString() returns
        System.out.println(" 1. " + now.toString());

        // Next, try the default DateFormat
        System.out.println(" 2. " + DateFormat.getInstance().format(now));

        // And the default time and date-time DateFormats
        System.out.println(" 3. " + DateFormat.getTimeInstance().format(now));
        System.out.println(" 4. " + DateFormat.getDateTimeInstance().format(now));

        // Next, try the short, medium and long variants of the
        // default time format
        System.out.println(" 5. " + DateFormat.getTimeInstance(DateFormat.SHORT).format(now));
        System.out.println(" 6. " + DateFormat.getTimeInstance(DateFormat.MEDIUM).format(now));
        System.out.println(" 7. " + DateFormat.getTimeInstance(DateFormat.LONG).format(now));

        // For the default date-time format, the length of both the
        // date and time elements can be specified. Here are some examples:
          
        System.out.println(" 8. " + DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT).format(now));
        System.out.println(" 9. " + DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.SHORT).format(now));
        System.out.println("10. " + DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG).format(now));
    }
    
    
    
    
*/