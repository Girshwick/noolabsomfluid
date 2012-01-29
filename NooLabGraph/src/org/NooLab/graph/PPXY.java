package org.NooLab.graph;

public class PPXY implements PPointXYIntf{

	private static final long serialVersionUID = 6971445467286097555L;

	PointXYIntf[] ppointXY = new PXY[2];
	
	
	public PPXY(){
		
	}
 

	public PPXY(PointXYIntf[] pointXYIntfs) {
		ppointXY = pointXYIntfs;
	}


	@Override
	public PointXYIntf[] getPpointXY() {
		
		return ppointXY;
	}
 

	@Override
	public void setPpointXY(PointXYIntf[] ppXY) {
		ppointXY = ppXY;
		
	}


	
}
