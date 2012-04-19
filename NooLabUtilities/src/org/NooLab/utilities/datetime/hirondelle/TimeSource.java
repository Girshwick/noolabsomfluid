package org.NooLab.utilities.datetime.hirondelle;


import java.util.TimeZone;

/**
 Return a possibly-fake value for the system clock.
 
 <P>When testing, it is often useful to use a 
 <a href='http://www.javapractices.com/topic/TopicAction.do?Id=234'>fake system clock</a>, 
 in order to exercise code that uses date logic. When you implement this interface, 
 you are instructing WEB4J classes on what time value they should use as the system clock. 
 This allows your application to <i>share</i> its fake system clock with the framework, 
 so that they can both use the exact same clock.

 <P>See {@link hirondelle.web4j.BuildImpl} for instructions on how to configure an implementation 
 of this interface. 
   
   <P>The following WEB4J framework classes use <tt>TimeSource</tt> :
   <ul>
   <li>{@link hirondelle.web4j.model.DateTime#now(TimeZone)} - returns the current date-time  
   <li>{@link hirondelle.web4j.ui.tag.ShowDate} - displays the current date-time  
   <li>{@link hirondelle.web4j.webmaster.LoggingConfigImpl} - both the name of the logging 
   file and the date-time attached to each logging record are affected
   <li>{@link hirondelle.web4j.webmaster.TroubleTicket} - uses the current date-time
   <li>{@link hirondelle.web4j.Controller} - upon startup, it places the current date-time in application scope
   </ul>
*/
public interface TimeSource {
  
  /** Return the possibly-fake system time, in milliseconds.  */
  long currentTimeMillis();
  
}

