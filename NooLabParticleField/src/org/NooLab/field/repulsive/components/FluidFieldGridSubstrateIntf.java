package org.NooLab.field.repulsive.components;

import org.NooLab.field.FieldGridSubstrateIntf;
import org.NooLab.field.repulsive.RepulsionFieldCore;



public interface FluidFieldGridSubstrateIntf extends FieldGridSubstrateIntf{

	
	public int[][] getGrid();
    
	public RepulsionFieldCore getRfCore();

	public Object[] getColMaps();

	public Object getRowMap();

	public int getColMapsPositions(int r, int particleIndex);

	public int getRowMapPosition(int particleIndex);

}
