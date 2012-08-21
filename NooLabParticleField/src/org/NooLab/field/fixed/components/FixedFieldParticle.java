package org.NooLab.field.fixed.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import org.NooLab.field.FieldHostIntf;
import org.NooLab.field.FieldParticleIntf;
import org.NooLab.field.interfaces.DataObjectIntf;
import org.NooLab.field.repulsive.components.topology.ParticleLinkages;
import org.NooLab.field.repulsive.particles.PColor;
import org.NooLab.field.repulsive.particles.RepulsionFieldParticle;





public class FixedFieldParticle 
								implements 
											Serializable,
											DataObjectIntf,
											FieldParticleIntf{

	private static final long serialVersionUID = -6616870914808686193L;

	transient FieldHostIntf parentField;
	
	double kRadiusFactor = 1.5;
	private int width;
	private int height;
	private int nbrParticles;
	private int colormode;
	private double sizemode;
	private int isAlive;
	private int particleIndex;
	double x;
	double y;

	private long indexOfDataObject;

	private Object dataObject;
	
	// ................................
	PColor mainColor = new PColor(); 
	PColor displayedColor = new PColor();
	transient ParticleLinkages particleLinkages  ; 
	
	private double radius = 5;
	
	
	// ========================================================================
	public FixedFieldParticle( int index, int w, int h,
							   int nbrParticles, 
							   int colormode,
							   FieldHostIntf parent){
		
		parentField = parent ;
			
		width = w; 
		height = h;
		
		
		this.nbrParticles = nbrParticles;
		this.colormode = colormode;
		
		this.isAlive = 1;
		
		particleIndex = index;
		
		initParticle();
	}
	// ========================================================================



	private void initParticle() {
		
		Random random ;
		
		random = parentField.getRandom() ;

		if (random==null){
			random = new Random();
			random.setSeed( 9347 + 97*particleIndex) ;
			
		}
		random.nextGaussian() ;
		
		mainColor.r = (float) Math.random()*0.3f;
		mainColor.g = (float) Math.random();
		mainColor.b = (float) Math.random();
		
		if (mainColor.r<0.22)mainColor.r=0.22f;
		if (mainColor.g<0.22)mainColor.g=0.22f;
		if (mainColor.b<0.22)mainColor.b=0.22f;
		
		
		
		// determining the position, coupling the color to the position
		// hsv = Conversion.rgbToHSV(r, g, b);
		// x = hsv[0] * (float)(width * kRadiusFactor * 0.5 / 360.0);
		// y = hsv[1] * (float)height;  Random random = new Random();
		// Random random = new Random();random.nextDouble()
		
		for (int i=0;i<10;i++)random.nextDouble();
		
		x = (((float) random.nextDouble())*(0.96* width))  + (0.02*width);
		y = (((float) random.nextDouble())*(0.96* height)) + (0.02*height);
			
		// determining the size relative to number and window size
		radius   = calculateRadius(nbrParticles)  ; 
		if ((sizemode!=0) && (sizemode!=1.0)){
			radius += (int)(  Math.round( random.nextDouble()* sizemode * ((int) (radius )) ));
		}											
		 
		if (colormode==0){
			mainColor.r= 0.6f;
			mainColor.g= 0.6f;
			mainColor.b= 0.6f;
		} 
		displayedColor.copy(mainColor) ;
		// ----------------------------------
		
		particleLinkages = new ParticleLinkages(this);
		
		random = null;

		
	}

	public double calculateRadius(int countOfParticles){
		
		double result = width * kRadiusFactor / (4 * Math.sqrt(countOfParticles));
		return result ;
	}


	@Override
	public void setIndexOfDataObject(long index) {
		
		indexOfDataObject = index ;
	}


	@Override
	public void registerDataObject(Object dataobject) {
		
		dataObject = dataobject ;
	}


	@Override
	public long getIndexOfDataObject() {
		
		return indexOfDataObject;
	}



	@Override
	public ArrayList<Long> getIndexesOfAllDataObject() {
		ArrayList<Long> dixes = new ArrayList<Long>();
		
		return dixes;
	}



	@Override
	public void mergeDataObjects(long[] indexes) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void mergeDataObjects(ArrayList<Long> indexes) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void removeIndexOfDataObject(long index) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void clearIndexOfDataObject() {
		// TODO Auto-generated method stub
		
	}



	public double getkRadiusFactor() {
		return kRadiusFactor;
	}



	public void setkRadiusFactor(double kRadiusFactor) {
		this.kRadiusFactor = kRadiusFactor;
	}



	public int getWidth() {
		return width;
	}



	public void setWidth(int width) {
		this.width = width;
	}



	public int getHeight() {
		return height;
	}



	public void setHeight(int height) {
		this.height = height;
	}



	public int getNbrParticles() {
		return nbrParticles;
	}



	public void setNbrParticles(int nbrParticles) {
		this.nbrParticles = nbrParticles;
	}



	public int getColormode() {
		return colormode;
	}



	public void setColormode(int colormode) {
		this.colormode = colormode;
	}



	public double getSizemode() {
		return sizemode;
	}



	public void setSizemode(double sizemode) {
		this.sizemode = sizemode;
	}



	public int getIsAlive() {
		return isAlive;
	}



	public void setIsAlive(int isAlive) {
		this.isAlive = isAlive;
	}



	public int getParticleIndex() {
		return particleIndex;
	}



	public void setParticleIndex(int particleIndex) {
		this.particleIndex = particleIndex;
	}



	public double getX() {
		return (int)x;
	}



	public void setX(double x) {
		this.x = x;
	}



	public double getY() {
		return (int)y;
	}



	public void setY(double y) {
		this.y = y;
	}



	@Override
	public int getindex() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
	
	
}
