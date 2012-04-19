package org.NooLab.somtransform.algo.util;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/*  http://joda-time.sourceforge.net/userguide.html#Accessing_fields
*/
public class DateConversion {
	 
	// 
	 
	ArrayList<String> possibleFormatStrings = new ArrayList<String>();
	
	ArrayList<SimpleDateFormat> sdfs = new ArrayList<SimpleDateFormat>();
	
	Locale locale = Locale.ENGLISH ;
	
	
	LocalDate baseDate = new LocalDate(1850, 1, 1);
	
	int year,month, day, weekday, serialDateValue;
	
	// ========================================================================
	public DateConversion(){
	
		// 
		initPossibleFormats();
	}
	// ========================================================================
	
	
	private void prepareFormatStr(){
		
		/*
		 
		 Date and Time Pattern  					Result  
		 
			"yyyy.MM.dd G 'at' HH:mm:ss z"  		2001.07.04 AD at 12:08:56 PDT  
			"EEE, MMM d, ''yy"  					Wed, Jul 4, '01  
			"h:mm a"  								12:08 PM  
			"hh 'o''clock' a, zzzz"  				12 o'clock PM, Pacific Daylight Time  
			"K:mm a, z"  							0:08 PM, PDT  
			"yyyyy.MMMMM.dd GGG hh:mm aaa"  		02001.July.04 AD 12:08 PM  
			"EEE, d MMM yyyy HH:mm:ss Z"  			Wed, 4 Jul 2001 12:08:56 -0700  
			"yyMMddHHmmssZ"  						010704120856-0700  
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ"  			2001-07-04T12:08:56.235-0700  

		 
		 */
		possibleFormatStrings.add( "d/M/yy" ) ;
		possibleFormatStrings.add( "d.M.yy" ) ;
		possibleFormatStrings.add( "d-M-yy" ) ;
		possibleFormatStrings.add( "d.MMMM yy" ) ;
		possibleFormatStrings.add( "ddd d.MMM yy" ) ;
		possibleFormatStrings.add( "ddd, d.MMM yy" ) ;
		possibleFormatStrings.add( "yy/M/d" ) ;
		possibleFormatStrings.add( "yy-M-d" ) ;

		possibleFormatStrings.add( "d-M-yyyy" ) ;
		possibleFormatStrings.add( "d.M.yyyy" ) ;
		possibleFormatStrings.add( "MMMM d, yyyy" ) ;
		possibleFormatStrings.add( "d.MMMM yyyy" ) ;
		possibleFormatStrings.add( "d/M/yyyy" ) ;
		possibleFormatStrings.add( "ddd d.MMM yyyy" ) ;
		possibleFormatStrings.add( "ddd, d.MMM yyyy" ) ;
	}
	
	private void initPossibleFormats(){
		
		Date date ;
		
		try{
			
			prepareFormatStr() ;
			
			// date = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).parse(mystring);
			
			for (int i=0;i<possibleFormatStrings.size();i++){
				String fs = possibleFormatStrings.get(i) ;
				sdfs.add( new SimpleDateFormat( fs, locale) ) ;	
			}
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
	}
	
	
	public int convert( String dateAsString ){
		
		int result = -1;
		
		Calendar cdate = new GregorianCalendar();
		SimpleDateFormat sdf ;
		Date date = null;
		long datevalue = 0 ;
		DateTime dt ;
		Period period ;
		
		// LocalDate today = new LocalDate();
		LocalDate jDate;
		
		for (int i=0;i<sdfs.size();i++){
			
			sdf = sdfs.get(i) ;

			try{
				
				date = sdf.parse(dateAsString);
				cdate.setTime( date );
				
				year  = cdate.get( Calendar.YEAR ) ;
				month = cdate.get( Calendar.MONTH ) ;
				day   = cdate.get( Calendar.DAY_OF_MONTH ) ;
				
				weekday = cdate.get( Calendar.DAY_OF_WEEK ) ;
				
				dt = new DateTime(cdate);
				
									// jDate = new LocalDate(year, month+1, day);
				
				jDate = new LocalDate(cdate); // year,month,day
									// period = new Period(baseDate, jDate, PeriodType.yearMonthDay());
				period = new Period(baseDate, jDate, PeriodType.days() );
				
				
				serialDateValue = period.getDays() ;
				if (serialDateValue>0){
					result = 0;
					break;
				}
			}catch(Exception e){
			}

		}
		
		return result ;
	}
		
	public void test(){	
		try{
			

			Calendar mydate = new GregorianCalendar();
			String mystring = "January 2, 2010";
			Date thedate = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).parse(mystring);
			
			mydate.setTime(thedate);
			
			//breakdown
			System.out.println("mydate -> "+mydate);
			System.out.println("year   -> "+mydate.get(Calendar.YEAR));
			System.out.println("month  -> "+mydate.get(Calendar.MONTH));
			System.out.println("dom    -> "+mydate.get(Calendar.DAY_OF_MONTH));
			System.out.println("dow    -> "+mydate.get(Calendar.DAY_OF_WEEK));
			System.out.println("hour   -> "+mydate.get(Calendar.HOUR));
			System.out.println("minute -> "+mydate.get(Calendar.MINUTE));
			System.out.println("second -> "+mydate.get(Calendar.SECOND));
			System.out.println("milli  -> "+mydate.get(Calendar.MILLISECOND));
			System.out.println("ampm   -> "+mydate.get(Calendar.AM_PM));
			System.out.println("hod    -> "+mydate.get(Calendar.HOUR_OF_DAY));
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		

	}


	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}


	/**
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}


	/**
	 * @return the day
	 */
	public int getDay() {
		return day;
	}


	/**
	 * @return the weekday
	 */
	public int getWeekday() {
		return weekday;
	}


	/**
	 * @return the serialDateValue
	 */
	public int getSerialDateValue() {
		return serialDateValue;
	}
}
