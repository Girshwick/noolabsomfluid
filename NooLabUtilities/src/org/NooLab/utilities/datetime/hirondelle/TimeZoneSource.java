package org.NooLab.utilities.datetime.hirondelle;

 
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;

/**
 Return the {@link TimeZone} associated with a given request.

 <P>See {@link hirondelle.web4j.BuildImpl} for important information on how this item is configured. 
 {@link hirondelle.web4j.BuildImpl#forTimeZoneSource()} 
 returns the configured implementation of this interface. See {@link TimeZoneSourceImpl} for a default implementation. 
 This interface is similar to {@link LocaleSource}, and is used in much the same way.
 
 <P>In general, a {@link TimeZone} is used for two distinct operations :
<ul>
 <li>render {@link java.util.Date} objects
 <li>parse user input into a <tt>Date</tt> object
</ul>

 <P>By default, a JRE will perform such operations using the <em>implicit</em> value returned by
 {@link TimeZone#getDefault()}. The main reason for defining this interface is to 
 provide an alternative to this mechanism, since it is inappropriate for most server applications.
 
 <P><i>For your Actions, the fastest way to access the time zone is usually via {@link ActionImpl#getTimeZone()}.</i>
 
 <P>The <tt>TimeZone</tt> returned by this interface is used by WEB4J for : 
<ul>
 <li>user response messages containing dates
 <li>presenting <tt>ResultSets</tt> as reports with {@link hirondelle.web4j.database.Report}
 <li>displaying dates with {@link hirondelle.web4j.ui.tag.ShowDate}
 <li>populating forms
 <li>parsing form entries 
</ul>

 <P>A very large number of policies can be defined by implementations of this interface. 
 Possible sources of <tt>TimeZone</tt> information include :
<ul>
 <li>a single setting in <tt>web.xml</tt>, place into application scope upon startup 
 <li>an object stored in session scope
 <li>a request parameter
 <li>a request header
 <li>a cookie
</ul>

 <h3>Java versus Databases</h3> 
 <P>Java always represents dates internally using the number of milliseconds from its epoch. In Java, a 
 {@link java.util.Date} is always an unambiguous instant. When parsing and formatting dates, it will always use 
 a {@link TimeZone} (either implicity or explicitly). On the other hand, it is often that the case that 
 a database column storing a date does <em>not</em> store dates internally in an unambiguous way. For example, 
 many dates are stored as just '<tt>05-31-2007 06:00</tt>', for example, without any time zone information.
 
 <P>If that is the case, then there is a mismatch : constructing a {@link java.util.Date} out of many 
 database columns will <em>require</em> a {@link TimeZone} to be specified, either explicitly or implicitly.
 See {@link java.sql.ResultSet#getDate(int, java.util.Calendar)}, 
 {@link java.sql.PreparedStatement#setDate(int, java.sql.Date, java.util.Calendar)}, and related methods.
 
 <P>The storage of dates in a database is <em>not</em> handled by this interface. That is 
 treated as a separate issue. 
 
 <h3>web.xml</h3>
 There are two settings related to time zones in <tt>web.xml</tt>. The two settings correspond to two 
 distinct ideas : the time zone appropriate for dates <em>presented</em> to the end user, and the time zone in 
 which the date is <em>stored</em>.
 
 <P>The <tt>DefaultUserTimeZone</tt> setting is used by {@link TimeZoneSourceImpl}. 
 For applications that use only a single time zone, then this setting is used to specify that time 
 zone. It provides independence of the default JRE time zone, which will vary according to the server location.
 For applications that use more than one time zone, then this same setting can be reinterpreted as the 
 <em>default</em> time zone, which can be overridden by implementations of this interface.
 
 <P>The <tt>TimeZoneHint</tt> setting is used by the WEB4J data layer to indicate the time zone in  
 which a date should be stored. If specified, this setting is communicated to the underlying 
 database driver using {@link java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)}
 and {@link java.sql.ResultSet#getTimestamp(int, java.util.Calendar)}. 
*/
public interface TimeZoneSource {
  
  /** Return a {@link TimeZone} corresponding to a given underlying request.  */
   public TimeZone get(HttpServletRequest aRequest);

}

