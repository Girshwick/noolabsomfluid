package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;

import org.NooLab.repulsive.components.data.IndexDistance;



/**
 * 
 * a buffer structure for the assignment recordID (in table) -> BMU candidate node index
 * for each record, we store several index values, to which we then refer   
 * 
 * @author kwa
 *
 */
public class BmuBuffer {

	DSom dSom;
	int tableRowCount;
	int bufferSurroundSize = -1;
	
	int bufferSize = 3 ;
	// a list of rows, that contain some index values
	ArrayList<ArrayList<IndexDistance>> recordBmuLinks;
	
	
	// ========================================================================
	public BmuBuffer( DSom dsom, int count){
		
		tableRowCount = count;
		
		dSom = dsom ;
		
		init();
		
		
	}
	// ========================================================================
	
		private void init() {
			// 
			ArrayList<IndexDistance> indexList;
			recordBmuLinks = new ArrayList<ArrayList<IndexDistance>>();
			IndexDistance ixDist;
			
			for (int i=0;i<tableRowCount;i++){
				
				indexList = new ArrayList<IndexDistance>();
				// for (int k=0;k<bufferSize;k++){ }
				recordBmuLinks.add( indexList );
			}
			
		}

		public void add(int dataRowIndex, IndexDistance indexDistance) {
		//  
		
		}
		
		
		// ................................................
		public int getBufferSurroundSize() {
			return bufferSurroundSize;
		}

		public void setBufferSurroundSize(int surroundSize) {
			this.bufferSurroundSize = surroundSize;
		}

		public int getBufferSize() {
			return bufferSize;
		}

		public void setBufferSize(int buffersize) {
			this.bufferSize = buffersize;
		}

		public int getTableRowCount() {
			return tableRowCount;
		}

		public ArrayList<ArrayList<IndexDistance>> getRecordBmuLinks() {
			return recordBmuLinks;
		}

		public void isBufferavailable(int dataRowIndex) {
			 
			
		}

		 
	
	
	
	
}


	
