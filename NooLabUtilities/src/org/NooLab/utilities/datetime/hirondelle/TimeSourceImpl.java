package org.NooLab.utilities.datetime.hirondelle;

/**
Default implementation of {@link TimeSource}.

<P> Simply returns the normal system time, without alteration. 
If you don't define your own {@link TimeSource}, then this 
default implementation will automatically be used by WEB4J. 
*/
public final class TimeSourceImpl  implements TimeSource {

 /** Return {@link System#currentTimeMillis()}, with no alteration. */
 public long currentTimeMillis() {
   return System.currentTimeMillis();
 }
 
}
