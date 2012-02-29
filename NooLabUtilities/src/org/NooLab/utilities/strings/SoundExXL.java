package org.NooLab.utilities.strings;


/**
 * Soundex - the Soundex Algorithm, as described by Knuth, adapted for multilanguage
 * 
 * <p>
 * This class implements the soundex algorithm as described by Donald Knuth in
 * Volume 3 of <I>The Art of Computer Programming</I>. The algorithm is intended
 * to hash words (in particular surnames) into a small space using a simple
 * model which approximates the sound of the word when spoken by an English
 * speaker. Each word is reduced to a four character string, the first character
 * being an upper case letter and the remaining three being digits. Double
 * letters are collapsed to a single digit.
 * 
 * <h2>EXAMPLES</h2>
 * Knuth's examples of various names and the soundex codes they map to are:
 * <b>Euler, Ellery -> E460 <b>Gauss, Ghosh -> G200 <b>Hilbert, Heilbronn ->
 * H416 <b>Knuth, Kant -> K530 <b>Lloyd, Ladd -> L300 <b>Lukasiewicz, Lissajous
 * -> L222
 * 
 * <h2>LIMITATIONS</h2>
 * As the soundex algorithm was originally used a <B>long</B> time ago in the
 * United States of America, it uses only the English alphabet and
 * pronunciation.
 * <p>
 * As it is mapping a large space (arbitrary length strings) onto a small space
 * (single letter plus 3 digits) no inference can be made about the similarity
 * of two strings which end up with the same soundex code. For example, both
 * "Hilbert" and "Heilbronn" end up with a soundex code of "H416".
 * <p>
 * The soundex() method is static, as it maintains no per-instance state; this
 * means you never need to instantiate this class.
 * 
 * @author Perl implementation by Mike Stok (<stok@cybercom.net>) from the
 *         description given by Knuth. Ian Phillips (<ian@pipex.net>) and Rich
 *         Pinder (<rpinder@hsc.usc.edu>) supplied ideas and spotted mistakes.
 * @author Ian Darwin, http://www.darwinsys.com/ (Java Version)
 * @version $Id: Soundex.java,v 1.9 2004/02/23 00:30:49 ian Exp $
 */
public class SoundExXL {

	static int maxLength = 8 ;
	
	static StringsUtil strgutil = new StringsUtil();
	
	/*
	 * The original encoding ...
	 * 
	 * Implements the mapping from: AEHIOUWYBFPVCGJKQSXZDTLMNR to:
	 *                              00000000111122222222334556
	 */
	public static final char[] MAPPcoarse_original = {
			// A B C D E F G H I J K L M
			'0', '1', '2', '3', '0', '1', '2', '0', '0', '2', '2', '4', '5',
			// N O P W R S T U V W X Y Z
			'5', '0', '1', '2', '6', '2', '3', '0', '1', '0', '2', '0', '2' };

	
	/*
	 * The fine-grained encoding ...
	 *                              
	 * Implements the mapping from: AE OU IJY PBVW GC QKX DTH MN RSZ LF      to:
	 *                              00 11 222 3333 44 555 666 77 888 99
	 *                              A  O  I   B    C  K   D   M  S   L      // exchange H and L ??        
	 */
	public static final char[] MAPnum = {
		// A    B    C    D    E    F    G    H    I    J    K    L    M
		  '0', '3', '4', '6', '0', '9', '4', '6', '2', '2', '5', '9', '7',
		// N    O    P    W    R    S    T    U    V    W    X    Y    Z
		  '7', '1', '3', '2', '8', '8', '6', '1', '3', '3', '5', '2', '8' };

	public static final char[] MAP = {
		// A    B    C    D    E    F    G    H    I    J    K    L    M
		  'A', 'B', 'C', 'D', 'A', 'F', 'C', 'D', 'I', 'I', 'K', 'L', 'M',
		// N    O    P    W    R    S    T    U    V    W    X    Y    Z
		  'M', 'O', 'B', '2', 'S', 'S', 'D', 'O', 'B', 'B', 'K', 'I', 'S' };
	    // missing : Q
	/*
	 * additionally, we encode fixed combinations like CH GH PH SCH SH etc... 
	 * 
	 */	
	public static final String[] snippedMap = {
		
 	                                          } ;
	
	
	static char[] usedMap = MAP;
	
	
	public static String transform(String string, double cropratio, int minLen) {
		String str = string ;
		
		if (string.length()>minLen){
			
		}
		
		
		return transform(str);
	}
	/**
	 * Convert the given String to its Soundex code.
	 * 
	 * @return null If the given string can't be mapped to Soundex.
	 */
	public static String transform(String string) {
		char m='?';
		String t,insertstr,s ;
		StringBuffer res ;
		String str = ""; 
		
		char c = 0,c2,cL = '?', prev = '?';
		int i = 0, rL;
		
		
		try{
			
			s = string ;
			
			// Algorithm works on uppercase (mainframe era).
			t = s.toUpperCase();
			t = t.replace("Ö", "OE");
			t = t.replace("Ü", "AE");
			t = t.replace("Ä", "UE");
			
			res = new StringBuffer();
			

			// Main loop: find up to <maxLength> chars that map.
			for (i = 0; i < t.length(); i++) {

				c = t.charAt(i) ;
				
				c2 = ' ';
				if (i<t.length()-1){
					c2 = t.charAt(i+1) ;
				}
				
				if ((c == 'Q') && (c2=='U')){
					t = strgutil.setposition(t,i+1,""); // del the H 
				}
				
				if (c == 'Q'){
					c = 'K' ;
				}

				if ((c == 'X') ){
					c = 'K' ; 
					insertstr = Character.toString(c)+"S";
					t = strgutil.setposition(t,i,insertstr); // using string allows to transmit an empty string = deleting a position
					
				}
				if (c == 'ß'){c='S';};
				
				if ((c == 'S') && (c2=='H')){
					c = 'G' ; 
					insertstr = Character.toString(c);
					t = strgutil.setposition(t,i,insertstr); // using string allows to transmit an empty string = deleting a position
					t = strgutil.setposition(t,i+1,""); 
				}
				if ((c == 'N') && (c2=='G')){
					c = 'G' ;
					insertstr = Character.toString(c);
					t = strgutil.setposition(t,i,insertstr); 
					t = strgutil.setposition(t,i+1,""); 
				}
				if ((c == 'I') && (c2=='O')){
					c = 'O' ;
					insertstr = Character.toString(c);
					t = strgutil.setposition(t,i,insertstr); 
					t = strgutil.setposition(t,i+1,""); 
				}

				if ((c == 'E') && ((c2=='A') || (c2=='I'))){
					c = 'E' ;
					insertstr = Character.toString(c);
					t = strgutil.setposition(t,i,insertstr); 
					t = strgutil.setposition(t,i+1,""); 
				}
				if ((c == 'C') && (c2=='H')){
					s = strgutil.setposition(t,i+1,""); // del the H 
				}
				
				// Check to see if the given character is alphabetic.
				// Text is already converted to uppercase. Algorithm
				// only handles ASCII letters, do NOT use Character.isLetter()!
				// Also, skip double letters.
				if (c >= 'A' && c <= 'Z' && c != prev) {
					prev = c;

					// First char is installed unchanged, for sorting.
					if (i == 0)
						res.append(c);
					else {
						m = usedMap[c - 'A'];
						//if (m != '0') {
						if ( m != cL){
							res.append(m);
						}
					}
				} // (c >= 'A' && c <= 'Z' , not a double letter ?
				
				
				if (res.length() >= maxLength){
					break;
				}
				cL = m;
			} // i -> 
			
			rL = res.length();
			
			  
			str = res.toString() ;
			
			// replacements shoud be done inline , => remove stringbuffer and work with string only !!!
			str = str.replace("TD","T");
			str = str.replace("DT","D");
			str = str.replace("SDS","SD");
			str = str.replace("SCD","SC");
			str = str.replace("AI","A");
			
			
			if (rL <=1){
				return "";
			}else{
				//c = str.charAt( str.length()-1) ;
				
				for (i = rL; i < 3; i++) {
					str = str + str.substring(str.length());
				}
			}
			
		}catch(Exception e){
			System.out.println("\nstring : "+string+"  i : "+i+"  c : "+c+"  \n") ;
			e.printStackTrace() ;
		}
		
		return str;
	}
}