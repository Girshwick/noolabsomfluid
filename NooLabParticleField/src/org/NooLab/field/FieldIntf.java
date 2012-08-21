package org.NooLab.field;



public interface FieldIntf {

	public static final int _INSTANCE_TYPE_SOM        = 1;
	public static final int _INSTANCE_TYPE_SPRITE     = 2;
	public static final int _INSTANCE_TYPE_OPTIMIZER  = 3;
	public static final int _INSTANCE_TYPE_TRANSFORM  = 4;
	public static final int _INSTANCE_TYPE_CLASSIFIER = 7;
	
	public static final int _SOM_GRIDTYPE_FIXED = 1 ;
	public static final int _SOM_GRIDTYPE_FLUID = 2 ;

	
	
	
	public int[] getAreaSize();
	public void setAreaSize(int width, int height) ;
	
	public void setAreaHeight(int height) ;
	public void setAreaWidth(int width) ;
	
	public int  getAreaWidth() ;
	public int  getAreaHeight() ;

	public int getBorderMode() ;
	public void setBorderMode(int mode) ;
	
	public double getAverageDistance();
	public double getMinimalDistance();
	
	public FieldParticlesIntf getParticles();
	public int getType();
	

}
