package org.NooLab.utilities.datetime.hirondelle;


import org.NooLab.utilities.datetime.hirondelle.DateTime;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 Convert text into a {@link DateTime} or {@link Date} object, and vice versa.
 
 <P>See {@link hirondelle.web4j.BuildImpl} for important information on how this item is configured. 
 {@link hirondelle.web4j.BuildImpl#forDateConverter()} 
 returns the configured implementation of this interface.
 
 <P>This interface has methods which occur in pairs, one for a {@link DateTime}, and one for a {@link Date}.
 The term 'date' used below refers to generically to both of these classes. WEB4J recommeds 
 {@link DateTime} as the preferred class for representing dates. 
 
 <P>The application programmer's implementation of this interface is used when WEB4J needs to 
 build a date from user input, or format an existing date. Here is an  
 <a href="http://www.javapractices.com/apps/fish/javadoc/src-html/hirondelle/web4j/config/DateConverterImpl.html">example implementation</a>.
 
 <P><b><em>Design Notes</em></b><br>
 Here are some forces concerning dates in Java web applications :
<ul>
 <li>usually, users expect date input formats to be the same for all forms.
 <li>an application may have only dates, only date-times, or a mixture of the two.
 <li>user input of a date or date-time may be with a single control, or with many controls. For example, 
 the date may be entered with one control, and the hours and minutes with a second and third control, respectively.
 (WEB4J works well with the one-control-only style. Using more than one control is still possible, since the application 
 programmer always has access to the underlying request parameters. But, when using WEB4J, 
 that style involves slightly more work for the application programmer.)
 <li><span class="highlight">the needs of the eye differ from the needs of the hand.</span> That is, a date format 
 which is easy to <em>read</em> is usually not easy to <em>enter</em> into a control. Thus, applications should support <em>two</em> formats for 
 dates : a <em>hand-friendly</em> format (used for input), and an <em>eye-friendly</em> format, used for 
 presentation <em>and</em> for input as well (allowing user input with an eye-friendly format is 
 important for forms which change existing data).
 <li>date formats may differ between Locales. For example, <tt>'01-31-2006'</tt> is natural for English speakers 
 (January 31, 2006), while <tt>'31-01-2006'</tt> is more natural for French speakers (le 31 janvier 2006). 
 <li>for <em>parsing</em> tasks, {@link java.text.SimpleDateFormat} is mediocre, and perhaps even untrustworthy. It should be used with great care.
 One should also be aware that it's not thread-safe.
 <li>if a new {@link Locale} is added to an application, then parsing and formatting of a date should not fail. 
 Instead, reasonable defaults should be defined for unknown <tt>Locale</tt>s.
</ul>
*/
public interface DateConverterIntf {

  /**
   Parse textual user input of a "hand-friendly" format into a {@link Date} object.
   
   <P>A hand-friendly format might be <tt>'01312006'</tt>, while an eye-friendly format might be 
   <tt>'Jan 31, 2006'</tt>.
   <P>The implementation must return <tt>null</tt> when the user input cannot be 
   successfully parsed into a {@link Date}. It is recommended that the implementation 
   have reasonable default behaviour for unexpected {@link Locale}s.
   <P>This method is called by {@link RequestParser}.
   
   @param aInputValue user input value, as returned by {@link hirondelle.web4j.model.ConvertParam#filter(String)} 
   (always has content).
   @param aLocale is obtained from the configured {@link LocaleSource}, and passed to this method
   by the framework.
   @param aTimeZone is obtained from the configured {@link TimeZoneSource}, and passed to this method
   by the framework.
  */
  Date parseHandFriendly(String aInputValue, Locale aLocale, TimeZone aTimeZone);
  
  /**
   Parse textual user input of an "eye-friendly" format into a {@link Date} object.
   
   <P>A hand-friendly format might be <tt>'01312006'</tt>, while an eye-friendly format might be 
   <tt>'Jan 31, 2006'</tt>.
   <P>The implementation must return <tt>null</tt> when the user input cannot be 
   successfully parsed into a {@link Date}. It is recommended that the implementation 
   have reasonable default behaviour for unexpected {@link Locale}s.
   <P>This method is called by {@link RequestParser}.
   
   @param aInputValue user input value, as returned by {@link hirondelle.web4j.model.ConvertParam#filter(String)} 
   (always has content).
   @param aLocale is obtained from the configured {@link LocaleSource}, and passed to this method
   by the framework.
   @param aTimeZone is obtained from the configured {@link TimeZoneSource}, and passed to this method
   by the framework.
  */
  Date parseEyeFriendly(String aInputValue, Locale aLocale, TimeZone aTimeZone);
  
  /**
   Format a {@link Date} into an eye-friendly, legible format.
    
   <P>The implementation must return an empty <tt>String</tt> when the {@link Date} is null.
   It is recommended that the implementation have reasonable default behaviour for unexpected {@link Locale}s.
   <P>The framework will call this method when presenting listings using 
   {@link hirondelle.web4j.database.Report}, and when presenting a Model Object in a form 
   for a "change" operation.  
   <P>This method is called by {@link Formats}.
   
   @param aDate to be presented to the user in a legible format
   @param aLocale is obtained from the configured {@link LocaleSource}, and passed to this method
   by the framework.
   @param aTimeZone is obtained from the configured {@link TimeZoneSource}, and passed to this method
   by the framework.
   @return text compatible with {@link #parseEyeFriendly(String, Locale, TimeZone)}
  */
  String formatEyeFriendly(Date aDate, Locale aLocale, TimeZone aTimeZone);

  /**
    Parse textual user input of a "hand-friendly" format into a {@link DateTime} object.
    
    <P>A hand-friendly format might be <tt>'01312006'</tt>, while an eye-friendly format might be 
    <tt>'Jan 31, 2006'</tt>.
    <P>The implementation must return <tt>null</tt> when the user input cannot be 
    successfully parsed into a {@link DateTime}. It is recommended that the implementation 
    have reasonable default behaviour for unexpected {@link Locale}s.
    <P>This method is called by {@link RequestParser}.
    
    @param aInputValue user input value, as returned by {@link hirondelle.web4j.model.ConvertParam#filter(String)} 
    (always has content).
    @param aLocale is obtained from the configured {@link LocaleSource}, and passed to this method
    by the framework.
  */
  DateTime parseHandFriendlyDateTime(String aInputValue, Locale aLocale);
  
  /**
    Parse textual user input of an "eye-friendly" format into a {@link DateTime} object.
    
    <P>A hand-friendly format might be <tt>'01312006'</tt>, while an eye-friendly format might be 
    <tt>'Jan 31, 2006'</tt>.
    <P>The implementation must return <tt>null</tt> when the user input cannot be 
    successfully parsed into a {@link DateTime}. It is recommended that the implementation 
    have reasonable default behaviour for unexpected {@link Locale}s.
    <P>This method is called by {@link RequestParser}.
    
    @param aInputValue user input value, as returned by {@link hirondelle.web4j.model.ConvertParam#filter(String)} 
    (always has content).
    @param aLocale is obtained from the configured {@link LocaleSource}, and passed to this method
    by the framework.
  */
  DateTime parseEyeFriendlyDateTime(String aInputValue, Locale aLocale);
  
  /**
    Format a {@link DateTime} into an eye-friendly, legible format.
     
    <P>The implementation must return an empty <tt>String</tt> when the {@link DateTime} is null.
    It is recommended that the implementation have reasonable default behaviour for unexpected {@link Locale}s.
    <P>The framework will call this method when presenting listings using 
    {@link hirondelle.web4j.database.Report}, and when presenting a Model Object in a form 
    for a "change" operation.  
    <P>This method is called by {@link Formats}.
    
    @param aDateTime to be presented to the user in a legible format
    @param aLocale is obtained from the configured {@link LocaleSource}, and passed to this method
    by the framework.
    @return text compatible with {@link #parseEyeFriendlyDateTime(String, Locale)}
  */
  String formatEyeFriendlyDateTime(DateTime aDateTime, Locale aLocale);
  
  
  
}

