package org.NooLab.repulsive.intf.particles;



public interface ParticleStateIntf {


	public String getLabel();
	public void setLabel(String label) ;
	
	public int getGroupIdentifier();
	public void setGroupIdentifier(int groupIdentifier);
	
	
	public void setSelected(int flag );
	public boolean isSelected();
	public int getSelected();
	
	public void setLocationFixed(int flag);
	public boolean isFrozen() ;
	public void setFrozen(boolean isfrozen)  ;
	
	
	
	
}
