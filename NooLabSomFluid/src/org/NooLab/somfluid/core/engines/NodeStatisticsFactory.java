package org.NooLab.somfluid.core.engines;

import java.util.ArrayList;

import org.NooLab.field.FieldIntf;
import org.NooLab.somfluid.core.categories.extensionality.BasicSimpleStatisticalDescription;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.somfluid.util.BasicStatisticalDescriptionIntf;

public class NodeStatisticsFactory {

	static int somType ;
	
	static NodeStatisticsDetailed statisticsDetailed;
	static NodeStatisticsSimpleBasic statisticsBasic;
	
	// ========================================================================
	
	public static void setSomType(int somtype){
		somType = somtype;
	}
	// ========================================================================
	public static NodeStatisticsIntf getStatisticsImpl(int somType) {
		
		if (somType == FieldIntf._SOMTYPE_MONO){
		
			statisticsDetailed = new NodeStatisticsDetailed()  ;
			return statisticsDetailed;
		}
		if (somType == FieldIntf._SOMTYPE_PROB){
			statisticsBasic = new NodeStatisticsSimpleBasic()  ;
			return statisticsBasic;
		}
		return null;
	}

	public  static ArrayList<?> getBasicStatisticalDescription(int somType) {
		
		if (somType == FieldIntf._SOMTYPE_MONO){
			ArrayList<BasicStatisticalDescription> bsd = new ArrayList<BasicStatisticalDescription>() ;
			return  bsd;
		}
		
		if (somType == FieldIntf._SOMTYPE_PROB){
			ArrayList<BasicSimpleStatisticalDescription> bssd = new ArrayList<BasicSimpleStatisticalDescription>() ;
			return  bssd;
		}
		
		return null;
	}

}
