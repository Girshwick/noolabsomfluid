package org.NooLab.utilities.strings;

import java.security.MessageDigest;

public class CryptoMD5 {


	private final char[] hexChars = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	
	public String MD5(String str) {
		MessageDigest md ;
		String hxStr;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			hxStr = hexStringFromBytes(md.digest());
			
			return hxStr;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public  String MD5(byte[] source) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(source);
			return hexStringFromBytes(md.digest());
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public byte[] MD5bytes(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			return md.digest();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String hexStringFromBytes(byte[] b) {
		String hex = "";
		int msb;
		int lsb = 0;
		int i;

		// MSB maps to idx 0
		for (i = 0; i < b.length; i++) {
			msb = ((int) b[i] & 0x000000FF) / 16;
			lsb = ((int) b[i] & 0x000000FF) % 16;
			hex = hex + hexChars[msb] + hexChars[lsb];
		}
		return (hex);
	}

}
