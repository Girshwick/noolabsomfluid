package org.NooLab.somfluid.util;

import java.util.*;

/**
 * {a,b,c} -> {{}, {a}, {b}, {c}, {a, b}, {a, c}, {b, c}, {a,b,c}}
 * 
 * 
 * 
 */
@SuppressWarnings("rawtypes")
public class PowerSet_demo {
	
	public static void main(String[] args) {
		String st[] = { "x", "y", "z", "4", "5", "6", "7", "8", "9", "10", "11" };
		
		LinkedHashSet hashSet = new LinkedHashSet();
		int len = st.length;
		
		int elements = (int) Math.pow(2, len);
		for (int i = 0; i < elements; i++) {
			String str = Integer.toBinaryString(i);
			int value = str.length();
			String pset = str;
			for (int k = value; k < len; k++) {
				pset = "0" + pset;
			}
			LinkedHashSet set = new LinkedHashSet();
			for (int j = 0; j < pset.length(); j++) {
				if (pset.charAt(j) == '1')
					set.add(st[j]);
			}
			hashSet.add(set);
		}
		System.out.println(hashSet.toString().replace("[", "{").replace("]", "}"));
	}
}
