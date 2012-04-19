package org.NooLab.utilities.datetime.hirondelle;


import java.util.*;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.NooLab.utilities.datetime.hirondelle.Util;

/**
 Represents a decimal amount.

<P>Decimal amounts are typically used to represent two kinds of items :
<ul>
 <li>monetary amounts 
 <li>measurements such as temperature, distance, and so on
 </ul>
 
 <P>Your applications are not obliged to use this class to represent decimal amounts. 
 You may choose to use {@link BigDecimal} instead (perhaps along with an <tt>Id</tt>
 to store a currency, if needed).
 
<P>This class exists for these reasons :
<ul>
 <li>to allow your code to read at a higher level
 <li>to simplify calculations beyond what is available from the {@link BigDecimal} class
 <li>to help you avoid using the floating point types <tt>double</tt> and <tt>float</tt>, 
 which have many <a href='http://www.ibm.com/developerworks/java/library/j-jtp0114/'>pitfalls</a> 
</ul>

 <P><tt>Decimal</tt> objects are immutable.  
 Many operations return new <tt>Decimal</tt> objects. 
 
 <h3>Currency Is Unspecified</h3>
 This class can be used to model amounts of money. 
<P><em>Many will be surprised that this class does not make any reference to currency.</em> 
 The reason for this is adding currency would render this class a poor <em>building block</em>.  
 Building block objects such as <tt>Date</tt>, <tt>Integer</tt>, and so on, are 
 <em>atomic</em>, in the sense of representing a <em>single</em> piece of data.   
 They correspond to a single column in a table, or a single form control. If the currency 
 were included in this class, then it would no longer be atomic, and it could not be 
 treated by WEB4J as any other building block class.
 However, allowing this class to be treated like any other building block class is 
 highly advantageous. 
 
 <P>If a feature needs to explicitly distinguish between <em>multiple</em> currencies 
 such as US Dollars and Japanese Yen, then a <tt>Decimal</tt> object 
 will need to be paired by the caller with a <em>second</em> item representing the 
 underlying currency (perhaps modeled as an <tt>Id</tt>). 
 See the {@link Currency} class for more information.  
 
 <h3>Number of Decimal Places</h3>
 To validate the number of decimals in your Model Objects, 
 call the {@link Check#numDecimalsAlways(int)} or {@link Check#numDecimalsMax(int)} methods.
 
 <P>The {@link #init(RoundingMode, int)} method is called upon startup.
 It takes a parameter which specifies the number of decimal places.
 It is used only for rounding the results of <tt>times</tt> and <tt>div</tt> operations. 
 It is <em>not</em> used to validate the number of decimals in 
 items passed to the <tt>Decimal</tt> constructor.
 
 <h3>Different Numbers of Decimals</h3>
 <P>As usual, operations can be performed on two items having a different number of decimal places. 
 For example, these  operations are valid (using an informal, <em>ad hoc</em> notation) : 
 <PRE>10 + 1.23 = 11.23
10.00 + 1.23 = 11.23
10 - 1.23 = 8.77
(10 > 1.23) => true </PRE> 
 This corresponds to typical user expectations.
  
 <P>Note that {@link #equals(Object)} is unusual in that it is the only method sensitive to the exact 
 number of decimal places, while {@link #eq(Decimal)} is not. That is,  
 <PRE>10.equals(10.00) => false
10.eq(10.00) => true</PRE>
   
 <h3>Results With 'Extra' Decimal Places</h3>
 <P>The <tt>times</tt> and <tt>div</tt> operations are different, since the result
 can have a larger number of decimals than usual. For example, when dealing with US Dollars, the result  
 <PRE>$10.00 x 0.1256 = $1.256</PRE>
 has more than two decimals. In such cases, <em>this class will round 
 results of multiplication and division</em>, using 
 the setting passed to {@link #init(RoundingMode, int)}. 
 This policy likely conforms to the expectations of most end users.
 The {@link #times(long)} method is an exception to this rule.
 
 <P>The {@link #init(RoundingMode, int)} method takes two parameters.
 One controls the rounding policy, and the other controls the number of decimals to use <em>by default</em>.
 When the default number of decimals needs to be overridden, you must use the 
 {@link #changeTimesDivDecimals(int)} method. 
 
 <h3>Terse Method Names</h3>
 Various methods in this class have unusually terse names, such as 
 <tt>lt</tt> for 'less than',  and <tt>gt</tt> for 'greater than', and so on. 
 The intent of such names is to improve the legibility of mathematical 
 expressions.
  
 <P>Example : 
 <PRE> if ( amount.lt(hundred) ) {
     cost = amount.times(price); 
 }</PRE>
 
 <h3>Prefer Decimal to Double</h3>
 The <tt>times</tt> and <tt>div</tt> methods are overloaded to take <tt>int</tt> for 
 round numbers, and <tt>Decimal</tt> or <tt>double</tt> for numbers with a decimal.
 
 <P>In short, the <tt>double</tt> versions are best suited when using
 <em>hard-coded</em> values for  factors and divisors,  while the 
 <tt>Decimal</tt> versions are suited for the (more common) case of using
 values coming from the database or user input.
 
 <P>Using <tt>Decimal</tt> is the preferred form, since there are many pitfalls associated 
 with <tt>double</tt>. The <tt>double</tt> form has been retained since it's 
 more convenient for the caller in some cases, and one of the goals of this class is 
 to allow terse mathematical expressions.
 
 <h3>Extends Number</h3>
 This class extends {@link Number}. An immediate benefit of this is that it allows JSTL's 
 <tt>fmt</tt> tags to render <tt>Decimal</tt> objects in the usual way.
*/
public final class Decimal extends Number implements Comparable<Decimal>, Serializable {
  
  /**
   Set default values for the rounding style, and the maximum number of decimals
   to use when calculating results of <tt>times</tt> and <tt>div</tt> operations.
   
   <P>This method is called by the framework upon startup.
   The recommended rounding style is {@link RoundingMode#HALF_EVEN}, also called 
   <em>banker's rounding</em>. That rounding style introduces the least bias.
   
   @param aRounding defines how all numbers are rounded by this class. This rounding 
   style is set once, and cannot be overridden for individual <tt>Decimal</tt> objects.
   @param aNumDecimalsForTimesDiv number of decimals for results of 
   <tt>times</tt> and <tt>div</tt> operations. Must be 0 or more.
   Taking the example of US Dollars, this setting would usually be '2'.
  */
  public static void init(RoundingMode aRounding, int aNumDecimalsForTimesDiv){
    if ( aNumDecimalsForTimesDiv < 0 ){
      throw new IllegalArgumentException("Number of decimals for times-div operations must be 0 or more. Value: " + Util.quote(aNumDecimalsForTimesDiv));
    }
    ROUNDING = aRounding;
    TIMES_DIV_DECIMALS =  aNumDecimalsForTimesDiv;
  }
  
  /** Return the rounding style passed to the <tt>init</tt> method. */
  public static RoundingMode getRoundingStyle() { return ROUNDING; }
  
  /** Return the number of decimals passed to the <tt>init</tt> method. */
  public static int getTimesDivDecimalsDefault() { return TIMES_DIV_DECIMALS; }
 
  /**
   Full constructor.
   
   @param aAmount required, can be positive or negative.
   Any number of decimals. The value of {@link BigDecimal#scale()} cannot 
   be negative.
  */
  public Decimal(BigDecimal aAmount){
    this(aAmount, TIMES_DIV_DECIMALS);
  }
  
  /**
   Convenience factory method. 
   
   <P>Instead of  : 
   <PRE>Decimal decimal = new Decimal(new BigDecimal("100"));</PRE>
   one may instead use  : 
   <PRE>Decimal decimal = Decimal.from("100");</PRE>
   which is a bit more legible. This is especially useful when you need to define 
   specific minimum and maximum values used in validation.  
  */
  public static Decimal from(String aAmount){
    return new Decimal(new BigDecimal(aAmount));
  }
  
  /**
   Override the default number of decimals retained in <tt>times</tt> and <tt>div</tt> operations.
   
   The <em>default</em> number of decimals retained in <tt>times</tt> and <tt>div</tt> 
   operations is set during the call to {@link #init(RoundingMode, int)}.
   The <tt>changeTimesDivDecimals</tt> method is called when that default is not 
   appropriate.
   
   <P>This method will often be used when modeling physical measurements 
   such as temperature, distance, and so on, where the number of decimals can 
   vary according to context. 
  */
  public Decimal changeTimesDivDecimals(int aTimesDivDecimals){
    return new Decimal(fAmount, aTimesDivDecimals);
  }
  
  /** Return the amount passed to the constructor. */
  public BigDecimal getAmount() { return fAmount; }
  
  /** 
   Return the number of decimals to be retained by <tt>times</tt> and <tt>div</tt> operations.
   
   <P>See {@link #changeTimesDivDecimals(int)} for a way of altering this value from the default. 
  */
  public int getTimesDivDecimals() { return fTimesDivDecimals; }
  
  /** The suffix is needed to distinguish from the public field.  Declared 'early' since compiler complains.*/
  private static final BigDecimal ZERO_BD = BigDecimal.ZERO;

  /** 
   Zero <tt>Decimal</tt> amount.
   
   <P>Like {@link BigDecimal#ZERO}, this item has no explicit decimal. 
   In most cases that will not matter, since only the {@link #equals(Object)} method is sensitive to 
   exact decimals. All other methods, including {@link #eq(Decimal)}, are not sensitive to exact decimals.
  */
  public static final Decimal ZERO = new Decimal(ZERO_BD);
  
  /**
   Return the number of decimals in this value.
   
   <P>For validating the number of decimals in user input, you are highly encouraged to 
   use {@link Check#numDecimalsAlways(int)} or {@link Check#numDecimalsMax(int)}.
  */
  public int getNumDecimals(){
    return fAmount.scale();
  }
  
  /** Return <tt>true</tt> only if the amount is positive. */
  public boolean isPlus(){
    return fAmount.compareTo(ZERO_BD) > 0;
  }
  
  /** Return <tt>true</tt> only if the amount is negative. */
  public boolean isMinus(){
    return fAmount.compareTo(ZERO_BD) <  0;
  }
  
  /** Return <tt>true</tt> only if the amount is zero. */
  public boolean isZero(){
    return fAmount.compareTo(ZERO_BD) ==  0;
  }
  
  /** 
   Equals (insensitive to number of decimals).
   
   <P>That is, <tt>10</tt> and <tt>10.00</tt> are considered equal by this method.
   
   <P>Return <tt>true</tt> only if the amounts are equal.
   This method is <em>not</em> synonymous with the <tt>equals</tt> method, 
   since the {@link #equals(Object)} method is sensitive to the exact number of decimal places.
  */
  public boolean eq(Decimal aThat) {
    return compareAmount(aThat) == 0;
  }

  /** 
   Greater than.
   
   <P>Return <tt>true</tt> only if  'this' amount is greater than
   'that' amount. 
  */
  public boolean gt(Decimal aThat) { 
    return compareAmount(aThat) > 0;  
  }
  
  /** 
   Greater than or equal to.
   
   <P>Return <tt>true</tt> only if 'this' amount is 
   greater than or equal to 'that' amount. 
  */
  public boolean gteq(Decimal aThat) { 
    return compareAmount(aThat) >= 0;  
  }
  
  /** 
   Less than.
   
   <P>Return <tt>true</tt> only if 'this' amount is less than
   'that' amount. 
  */
  public boolean lt(Decimal aThat) { 
    return compareAmount(aThat) < 0;  
  }
  
  /** 
   Less than or equal to.
   
   <P>Return <tt>true</tt> only if 'this' amount is less than or equal to
   'that' amount.  
  */
  public boolean lteq(Decimal aThat) { 
    return compareAmount(aThat) <= 0;  
  }
  
  /** 
   Add <tt>aThat</tt> <tt>Decimal</tt> to this <tt>Decimal</tt>.
  */
  public Decimal plus(Decimal aThat){
    return new Decimal(fAmount.add(aThat.fAmount));
  }

  /** 
   Subtract <tt>aThat</tt> <tt>Decimal</tt> from this <tt>Decimal</tt>. 
  */
  public Decimal minus(Decimal aThat){
    return new Decimal(fAmount.subtract(aThat.fAmount));
  }

  /**
   Sum a collection of <tt>Decimal</tt> objects.
   You are encouraged to use database summary functions 
   whenever possible, instead of this method. 
   
   @param aDecimals collection of <tt>Decimal</tt> objects.
   If the collection is empty, then a zero value is returned.
  */
  public static Decimal sum(Collection<Decimal> aDecimals){
    Decimal sum = new Decimal(ZERO_BD);
    for(Decimal decimal : aDecimals){
      sum = sum.plus(decimal);
    }
    return sum;
  }
  
  /**
   Multiply this <tt>Decimal</tt> by an integral factor.
   
   <P>The number of decimals in the return value is the same as 
   the number of decimals of 'this' <tt>Decimal</tt>. For example,
   <PRE>10 x 2 = 20
  10.00 x 2 = 20.00</PRE>
   This conforms to most user's expectations. 
   This behavior is slightly different from the other <tt>times</tt> methods.
  */
  public Decimal times(long aFactor){  
    BigDecimal factor = new BigDecimal(aFactor);
    BigDecimal newAmount = fAmount.multiply(factor);
    return new Decimal(newAmount);
  }
  
  /**
   Multiply this <tt>Decimal</tt> by an non-integral factor (having a decimal point).
   
   <P>The number of decimals of the result is taken from 
   {@link #getTimesDivDecimals()}.  
  */
  public Decimal times(Decimal aFactor){
    BigDecimal newAmount = fAmount.multiply(aFactor.getAmount());
    newAmount = newAmount.setScale(fTimesDivDecimals, ROUNDING);
    return  new Decimal(newAmount);
  }
  
  /**
   Multiply this <tt>Decimal</tt> by an non-integral factor (having a decimal point).
   
   <P>The number of decimals of the result is taken from 
   {@link #getTimesDivDecimals()}.
   
   Consider using {@link #times(Decimal)} as the preferred alternative.   
  */
  public Decimal times(double aFactor){
    return times(asDecimal(aFactor));
  }
  
  /**
   Divide this <tt>Decimal</tt> by an integral divisor.
   
   <P>The number of decimals of the result is taken from 
   {@link #getTimesDivDecimals()}.  
  */
  public Decimal div(long aDivisor){
    BigDecimal divisor = new BigDecimal(aDivisor);
    BigDecimal newAmount = fAmount.divide(divisor, fTimesDivDecimals, ROUNDING);
    return new Decimal(newAmount);
  }

  /**
   Divide this <tt>Decimal</tt> by an non-integral divisor.
   
   <P>The number of decimals of the result is taken from 
   {@link #getTimesDivDecimals()}.  
  */
  public Decimal div(Decimal aDivisor){  
    BigDecimal newAmount = fAmount.divide(aDivisor.getAmount(), fTimesDivDecimals, ROUNDING);
    return new Decimal(newAmount);
  }
  
  /**
   Divide this <tt>Decimal</tt> by an non-integral divisor.
   
   <P>The number of decimals of the result is taken from 
   {@link #getTimesDivDecimalsDefault()}.
     
   Consider using {@link #div(Decimal)} as the preferred alternative.   
  */
  public Decimal div(double aDivisor){  
    return div(asDecimal(aDivisor));
  }

  /** Return the absolute value of the amount. */
  public Decimal abs(){
    return isPlus() ? this : times(-1);
  }
  
  /** Return the amount x (-1). */
  public Decimal negate(){ 
    return times(-1); 
  }
  
  /**
   Round to an integer.
   
   <P>Uses {@link #getRoundingStyle()}.
  */
  public Decimal round(){
    BigDecimal amount = fAmount.setScale(0, ROUNDING);
    return new Decimal(amount);
  }

  /**
   Round to 0 or more decimal places.
   
   <P>Uses {@link #getRoundingStyle()}.
  */
  public Decimal round(int aNumberOfDecimals){
    if( aNumberOfDecimals < 0 ){
      throw new IllegalArgumentException("Number of decimals is negative: " + Util.quote(aNumberOfDecimals));
    }
    BigDecimal amount = fAmount.setScale(aNumberOfDecimals, ROUNDING);
    return new Decimal(amount);
  }
  
  /**
   Renders this <tt>Decimal</tt> in a style suitable for debugging. 
    
   <P>Returns the amount in the format defined by {@link BigDecimal#toPlainString()}. 
  */
  public String toString(){
    return fAmount.toPlainString();
  }
  
  /**
   Equals (sensitive to number of decimals). 
   
   <P>That is, <tt>10</tt> and <tt>10.00</tt> are <em>not</em> 
   considered equal by this method.
   
   <P>This implementation imitates {@link BigDecimal#equals(java.lang.Object)}, 
   which is also sensitive to the number of decimals (or 'scale').
   
   See {@link #eq(Decimal)} as well.
  */
  public boolean equals(Object aThat){
    if (this == aThat) return true;
    if (! (aThat instanceof Decimal) ) return false;
    Decimal that = (Decimal)aThat;
    //the object fields are never null :
    boolean result = (this.fAmount.equals(that.fAmount) );
    return result;
  }
  
  public int hashCode(){
    if ( fHashCode == 0 ) {
      fHashCode = HASH_SEED;
      fHashCode = HASH_FACTOR * fHashCode + fAmount.hashCode(); 
    }
    return fHashCode;
  }
  
  /**
   Implements the {@link Comparable} interface. 
   
   <P>Recommended that you use the other methods such as {@link #eq(Decimal)}, {@link #lt(Decimal)}, and 
   so on, since they have greater clarity and concision.
  */
  public int compareTo(Decimal aThat) {
    final int EQUAL = 0;
    
    if ( this == aThat ) return EQUAL;

    //the object field is never null 
    int comparison = this.fAmount.compareTo(aThat.fAmount);
    if ( comparison != EQUAL ) return comparison;

    return EQUAL;
  }
  
  /** 
   Required by {@link Number}.
      
   <P><em>Use of floating point data is highly discouraged.</em> 
   This method is provided only because it's required by <tt>Number</tt>. 
  */
  @Override public double doubleValue() {
    return fAmount.doubleValue();
  }
  
  /** 
   Required by {@link Number}.
      
   <P><em>Use of floating point data is highly discouraged.</em> 
   This method is provided only because it's required by <tt>Number</tt>. 
  */
  @Override public float floatValue() {
    return fAmount.floatValue();
  }

  /** Required by {@link Number}. */
  @Override  public int intValue() {
    return fAmount.intValue();
  }
  
  /** Required by {@link Number}. */
  @Override public long longValue() {
    return fAmount.longValue();
  }
  
  // PRIVATE //
  
  /** 
   The decimal amount. 
   Never null. 
   @serial 
  */
  private BigDecimal fAmount;
  
  private final int fTimesDivDecimals;
  
  /**
   The default rounding style to be used if no currency is passed to the constructor.
   See {@link BigDecimal}. 
  */ 
  private static RoundingMode ROUNDING;
  
  /**
   Maximum number of decimals for all monies. 
   This provides a way of rounding results of times and div operations. 
    For example, if a result of a multiplication or division is $10.25366,
    it will be rounded to $10.25 implicitly by this class (if this setting is '2').
  */
  private static int TIMES_DIV_DECIMALS;
  
  /** @serial */
  private int fHashCode;
  private static final int HASH_SEED = 23;
  private static final int HASH_FACTOR = 37;

  private Decimal(BigDecimal aAmount, int aTimesDivDecimals){
    fAmount = aAmount;
    fTimesDivDecimals = aTimesDivDecimals;
    validateState();
  }
  
  /**
   Determines if a deserialized file is compatible with this class.
  
   Maintainers must change this value if and only if the new version
   of this class is not compatible with old versions. See Sun docs
   for <a href=http://java.sun.com/products/jdk/1.1/docs/guide
   /serialization/spec/version.doc.html> details. </a>
  
   Not necessary to include in first version of the class, but
   included here as a reminder of its importance.
  */
  private static final long serialVersionUID = 7526471155622776147L;

  /**
   Always treat de-serialization as a full-blown constructor, by
   validating the final state of the de-serialized object.
  */  
  private void readObject(
    ObjectInputStream aInputStream
  ) throws ClassNotFoundException, IOException {
    //always perform the default de-serialization first
    aInputStream.defaultReadObject();
    //defensive copy for mutable date field
    //BigDecimal is not technically immutable, since its non-final
    fAmount = new BigDecimal( fAmount.toPlainString() );
    //ensure that object state has not been corrupted or tampered with maliciously
    validateState();
  }

  private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
    //perform the default serialization for all non-transient, non-static fields
    aOutputStream.defaultWriteObject();
  }  

  private void validateState(){
    if( fAmount == null ) {
      throw new IllegalArgumentException("Amount cannot be null");
    }
    if (fAmount.scale() < 0){
      throw new IllegalArgumentException("Amount has scale that is less than zero: " + Util.quote(fAmount.scale()));
    }
  }
  
  /** Ignores scale: 0 same as 0.00 */
  private int compareAmount(Decimal aThat){
    return this.fAmount.compareTo(aThat.fAmount);
  }
  
  private Decimal asDecimal(double aDouble){
    //this is the recommended way of building a BigDecimal from a double.
    BigDecimal result =  BigDecimal.valueOf(aDouble);
    return new Decimal(result);
  }
}
