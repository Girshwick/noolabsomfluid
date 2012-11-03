package org.NooLab.utilities.process;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;





public class RememberAllWrittenTextPrintStream extends PrintStream {

    private static final String newLine = System.getProperty("line.separator");

    private final StringBuffer sb = new StringBuffer();
    private final PrintStream original;

    ArrayList<String> detectorPatterns = new ArrayList<String>();
    
	private ConsoleDetectorInformIntf detectorClient;

    // ========================================================================
    public RememberAllWrittenTextPrintStream(PrintStream original) {
    	super(original);
    	
        this.original = original;
    }
    // ========================================================================
    
	public void setDetectorCallback( ConsoleDetectorInformIntf detectorClient) {
		this.detectorClient = detectorClient;
	}

	public void addDetectorPattern(String pattern) {
		
		detectorPatterns.add(pattern);
		//detectorClient
	}
	// ------------------------------------------------------------------------
	
	public String getAllWrittenText() {
	    return sb.toString();
	}

	public void print(double d) {
        sb.append(d);
        original.print(d);
    }

    public void print(String s) {
        sb.append(s);
        original.print(s);
    }

    public void println(String s) {
        sb.append(s).append(newLine);
        original.println(s);
        
        if (detectorClient!=null){
        	for (int i=0;i<detectorPatterns.size();i++){
        		int p =  (s.indexOf(detectorPatterns.get(i)));
        		if (p>=0){
        			detectorClient.setDetectionEvent( new DetectionEvent( s,
        					 											  detectorPatterns.get(i),
        																  System.currentTimeMillis()) );		
        		}
        	}
        }
		
    }

    public void println() {
        sb.append(newLine);
        original.println();
    }

    public PrintStream printf(String s, Object... args) {
    	
        sb.append( String.format(s, args) );
        return original.printf(s, args);
    }


    public char charAt(int index) {
		return newLine.charAt(index);
	}

	public int codePointAt(int index) {
		return newLine.codePointAt(index);
	}

	public int codePointBefore(int index) {
		return newLine.codePointBefore(index);
	}

	public int codePointCount(int beginIndex, int endIndex) {
		return newLine.codePointCount(beginIndex, endIndex);
	}

	public int compareTo(String anotherString) {
		return newLine.compareTo(anotherString);
	}

	public int compareToIgnoreCase(String str) {
		return newLine.compareToIgnoreCase(str);
	}

	public String concat(String str) {
		return newLine.concat(str);
	}

	public boolean contains(CharSequence s) {
		return newLine.contains(s);
	}

	public boolean contentEquals(CharSequence cs) {
		return newLine.contentEquals(cs);
	}

	public boolean contentEquals(StringBuffer sb) {
		return newLine.contentEquals(sb);
	}

	public boolean endsWith(String suffix) {
		return newLine.endsWith(suffix);
	}

	public boolean equals(Object anObject) {
		return newLine.equals(anObject);
	}

	public boolean equalsIgnoreCase(String anotherString) {
		return newLine.equalsIgnoreCase(anotherString);
	}

	public byte[] getBytes() {
		return newLine.getBytes();
	}

	public byte[] getBytes(Charset charset) {
		return newLine.getBytes(charset);
	}

	public void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) {
		newLine.getBytes(srcBegin, srcEnd, dst, dstBegin);
	}

	public byte[] getBytes(String charsetName)
			throws UnsupportedEncodingException {
		return newLine.getBytes(charsetName);
	}

	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		newLine.getChars(srcBegin, srcEnd, dst, dstBegin);
	}

	public int hashCode() {
		return newLine.hashCode();
	}

	public int indexOf(int ch, int fromIndex) {
		return newLine.indexOf(ch, fromIndex);
	}

	public int indexOf(int ch) {
		return newLine.indexOf(ch);
	}

	public int indexOf(String str, int fromIndex) {
		return newLine.indexOf(str, fromIndex);
	}

	public int indexOf(String str) {
		return newLine.indexOf(str);
	}

	public String intern() {
		return newLine.intern();
	}

	public boolean isEmpty() {
		return newLine.isEmpty();
	}

	public int lastIndexOf(int ch, int fromIndex) {
		return newLine.lastIndexOf(ch, fromIndex);
	}

	public int lastIndexOf(int ch) {
		return newLine.lastIndexOf(ch);
	}

	public int lastIndexOf(String str, int fromIndex) {
		return newLine.lastIndexOf(str, fromIndex);
	}

	public int lastIndexOf(String str) {
		return newLine.lastIndexOf(str);
	}

	public int length() {
		return newLine.length();
	}

	public boolean matches(String regex) {
		return newLine.matches(regex);
	}

	public int offsetByCodePoints(int index, int codePointOffset) {
		return newLine.offsetByCodePoints(index, codePointOffset);
	}

	public boolean regionMatches(boolean ignoreCase, int toffset, String other,
			int ooffset, int len) {
		return newLine.regionMatches(ignoreCase, toffset, other, ooffset, len);
	}

	public boolean regionMatches(int toffset, String other, int ooffset, int len) {
		return newLine.regionMatches(toffset, other, ooffset, len);
	}

	public String replace(char oldChar, char newChar) {
		return newLine.replace(oldChar, newChar);
	}

	public String replace(CharSequence target, CharSequence replacement) {
		return newLine.replace(target, replacement);
	}

	public String replaceAll(String regex, String replacement) {
		return newLine.replaceAll(regex, replacement);
	}

	public String replaceFirst(String regex, String replacement) {
		return newLine.replaceFirst(regex, replacement);
	}

	public String[] split(String regex, int limit) {
		return newLine.split(regex, limit);
	}

	public String[] split(String regex) {
		return newLine.split(regex);
	}

	public boolean startsWith(String prefix, int toffset) {
		return newLine.startsWith(prefix, toffset);
	}

	public boolean startsWith(String prefix) {
		return newLine.startsWith(prefix);
	}

	public CharSequence subSequence(int beginIndex, int endIndex) {
		return newLine.subSequence(beginIndex, endIndex);
	}

	public String substring(int beginIndex, int endIndex) {
		return newLine.substring(beginIndex, endIndex);
	}

	public String substring(int beginIndex) {
		return newLine.substring(beginIndex);
	}

	public char[] toCharArray() {
		return newLine.toCharArray();
	}

	public String toLowerCase() {
		return newLine.toLowerCase();
	}

	public String toLowerCase(Locale locale) {
		return newLine.toLowerCase(locale);
	}

	public String toString() {
		return newLine.toString();
	}

	public String toUpperCase() {
		return newLine.toUpperCase();
	}

	public String toUpperCase(Locale locale) {
		return newLine.toUpperCase(locale);
	}

	public String trim() {
		return newLine.trim();
	}



}