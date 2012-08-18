package org.NooLab.field.repulsive.particles;

import java.io.Serializable;

public class PColor implements Serializable{
 
	public float r, g, b, transparency;

	public void copy(PColor icolor) {
	
		r = icolor.r;
		g = icolor.g;
		b = icolor.b;
	}

	public float getR() {
		return r;
	}

	public void setR(float r) {
		this.r = r;
	}

	public float getG() {
		return g;
	}

	public void setG(float g) {
		this.g = g;
	}

	public float getB() {
		return b;
	}

	public void setB(float b) {
		this.b = b;
	}

	public float getTransparency() {
		return transparency;
	}

	public void setTransparency(float transparency) {
		this.transparency = transparency;
	}

	public int getIntG(){
		 
		return (int)( g*255.0); 
	}	 

	public int getIntB(){
		 
		return (int)( b*255.0);  
	}	 
	
	public int getIntR() {
		 
		return (int)( r*255.0);  
	}	
}
