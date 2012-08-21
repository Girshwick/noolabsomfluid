package org.NooLab.field.fixed;


import org.NooLab.field.FieldParticlesIntf;
import org.NooLab.field.fixed.components.FixedFieldSelectionIntf;
import org.NooLab.field.repulsive.intf.particles.RepFieldParticlesIntf;




public interface FixedFieldWintf extends FixedFieldSelectionIntf{

	public void registerEventMessaging( Object eventObj );

	public void close();

	public void init(int nbrParticles);

	public void update();
	
	public void setBorderMode(int borderAll);


	public int getResolutionFactor();

	public int getPhysicalWidth();

	public double getPhysicalHeight();

	public int getNumberOfParticles();

	
	public FieldParticlesIntf getParticles();
	
	public void setSelectionSize(int _selectionsize);

	public int getSelectionSize();

	
	public String getSurround(int particleIndex, int selectmode, int surroundN, boolean autoselect);

	public String getSurround(int xpos, int ypos, int selectMode, int surroundN, boolean autoselect) ;
	
}
