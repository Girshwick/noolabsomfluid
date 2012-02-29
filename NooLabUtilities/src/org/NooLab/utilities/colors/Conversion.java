package org.NooLab.utilities.colors;

public class Conversion {

	/**
	 * 
	 * returns an int[] containing h,s,v
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */
	public static int[] rgbToHSV(int r, int g, int b) {
		float rf, gf, bf;
		float[] hsvf;
		int[] hsv = new int[3];
		
		rf = (float)r;
		gf = (float)g;
		bf = (float)b;
		
		hsvf = rgbToHSV(rf,gf,bf);
		
		// h
		hsv[0] = (int)Math.round(hsvf[0]) ;
		
		//s
		hsv[1] = (int)Math.round(hsvf[1]) ;
		
		//v
		hsv[2] = (int)Math.round(hsvf[2]) ;
		
		return hsv;
	}
	
	/**
	 * 
	 * returns an int[] containing h,s,v
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @return
	 */

	public static float[] rgbToHSV(float r, float g, float b) {
		float mmax = r > g ? r : g;
		mmax = mmax > b ? mmax : b;
		float mmin = r < g ? r : g;
		mmin = mmin < b ? mmin : b;
		float v = mmax;
		float s = (mmax != 0) ? (mmax - mmin) / mmax : 0;
		float h = 0;
		if (s == 0) {
			h = 0; // undefined, actually
		} else {
			float d = mmax - mmin;
			if (r == mmax) {
				h = (g - b) / d;
			} else if (g == mmax) {
				h = 2 + (b - r) / d;
			} else if (b == mmax) {
				h = 4 + (r - g) / d;
			}
			h *= 60;
			if (h < 0) {
				h += 360;
			}
		}
		float[] results = new float[3];
		results[0] = h;
		results[1] = s;
		results[2] = v;
		
		return results;
	}

}
