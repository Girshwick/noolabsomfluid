package org.NooLab.utilities.datetime.hirondelle;


import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;

 

/**
 Retrieve the {@link TimeZone} stored in any scope under the key 
 {@link hirondelle.web4j.Controller#TIME_ZONE}.
 
 <P>Upon startup, the {@link hirondelle.web4j.Controller} will read in the <tt>DefaultUserTimeZone</tt>
 configured in <tt>web.xml</tt>, and place it in application scope under the key 
 {@link hirondelle.web4j.Controller#TIME_ZONE}, as a {@link TimeZone} object.
 
 <P><em>If desired</em>, the application programmer can also store a user-specific 
 {@link TimeZone} in session scope, <em>under the same key</em>. Thus, 
 this class will first find the user-specific <tt>TimeZone</tt>, overriding the default 
 <tt>TimeZone</tt> stored in application scope. 
 
 <P>If any other behavior is desired, then simply provide an alternate implementation of 
 {@link TimeZoneSource}.
*/
public final class TimeZoneSourceImpl implements TimeZoneSource {

  /** See class comment. */
  public TimeZone get(HttpServletRequest aRequest){
    // return (TimeZone)WebUtil.findAttribute(Controller.TIME_ZONE, aRequest);
	  return null;
  }
  
}
