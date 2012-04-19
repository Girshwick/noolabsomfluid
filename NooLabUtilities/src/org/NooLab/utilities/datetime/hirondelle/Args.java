package org.NooLab.utilities.datetime.hirondelle;


import java.util.regex.*;

/**
 Utility methods for common argument validations.

<P>Replaces <tt>if</tt> statements at the start of a method with 
 more compact method calls.
 
 <P>Example use case.
 <P>Instead of :
 <PRE>
 public void doThis(String aText){
   if (!Util.textHasContent(aText)){
     throw new IllegalArgumentException();
   }
   //..main body elided
 }
 </PRE>
 <P>One may instead write :
 <PRE>
 public void doThis(String aText){
   Args.checkForContent(aText);
   //..main body elided
 }
 </PRE>
*/
public final class Args {
  
  /**
   If <code>aText</code> does not satisfy {@link Util#textHasContent}, then 
   throw an <code>IllegalArgumentException</code>.
  
   <P>Most text used in an application is meaningful only if it has visible content.
  */
  public static void checkForContent(String aText){
    if( ! Util.textHasContent(aText) ){
      throw new IllegalArgumentException("Text has no visible content");
    }
  }

  /**
   If {@link Util#isInRange} returns <code>false</code>, then 
   throw an <code>IllegalArgumentException</code>. 
  
   @param aLow is less than or equal to <code>aHigh</code>.
  */
  public static void checkForRange( int aNumber, int aLow, int aHigh ) {
    if ( ! Util.isInRange(aNumber, aLow, aHigh) ) {
      throw new IllegalArgumentException(aNumber + " not in range " + aLow + ".." + aHigh);
    }
  }

  /**
   If <tt>aNumber</tt> is less than <tt>1</tt>, then throw an 
   <tt>IllegalArgumentException</tt>.
  */
  public static void checkForPositive(int aNumber) {
    if (aNumber < 1) {
      throw new IllegalArgumentException(aNumber + " is less than 1");
    }
  }

  /**
   If {@link Util#matches} returns <tt>false</tt>, then 
   throw an <code>IllegalArgumentException</code>. 
  */
  public static void checkForMatch(Pattern aPattern, String aText){
    if ( ! Util.matches(aPattern, aText) ){
      throw new IllegalArgumentException(
        "Text " + Util.quote(aText) + " does not match '" +aPattern.pattern()+ "'"
      );
    }
  }
  
  /**
   If <code>aObject</code> is null, then throw a <code>NullPointerException</code>.
  
   <P>Use cases :
  <pre>
   doSomething( Football aBall ){
     //1. call some method on the argument : 
     //if aBall is null, then exception is automatically thrown, so 
     //there is no need for an explicit check for null.
     aBall.inflate();
    
     //2. assign to a corresponding field (common in constructors): 
     //if aBall is null, no exception is immediately thrown, so 
     //an explicit check for null may be useful here
     Args.checkForNull( aBall );
     fBall = aBall;
     
     //3. pass on to some other method as parameter : 
     //it may or may not be appropriate to have an explicit check 
     //for null here, according the needs of the problem
     Args.checkForNull( aBall ); //??
     fReferee.verify( aBall );
   }
   </pre>
  */
  public static void checkForNull(Object aObject) {
    if ( aObject == null ) {
      throw new NullPointerException();
    }
  }
  
  // PRIVATE //
  private Args(){
    //empty - prevent construction
  }
  
}
