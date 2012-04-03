package org.NooLab.somsprite;

import java.io.Serializable;




public class IndexTuple implements Serializable{

	int[] indexes ;
	double value  ;
	
	public IndexTuple( double value, int... indices){
		this.value = value ;
		
		indexes = new int[indices.length];
		for (int i=0;i<indices.length;i++){
			indexes[i] = indices[i] ;
		}
	}

	// for copying
	public IndexTuple(IndexTuple item) {

		value = item.value;
		indexes = new int[item.indexes.length];
		System.arraycopy( item.indexes, 0, indexes, 0, indexes.length);
	}

	public int getIndexItem(int iindex){
		int ix=-1;
		if ((iindex>=0) && (iindex<indexes.length)){
			ix = indexes[iindex];
		}
		return ix;
	}
	
	public int[] getIndexes() {
		return indexes;
	}

	public void setIndexes(int[] indexes) {
		this.indexes = indexes;
	}
	
	
}


