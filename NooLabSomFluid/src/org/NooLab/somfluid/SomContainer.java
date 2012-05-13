package org.NooLab.somfluid;

import java.util.ArrayList;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurface;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.somtransform.SomFluidXMLHelper;
import org.NooLab.utilities.logging.PrintLog;

import com.jamesmurty.utils.XMLBuilder;



public class SomContainer {

	SomObjects somObj;
	
	SomHostIntf somHost ;
	SomDataObject somData;
	SomProcessIntf somProcess;
	String taskGuid ;
	
	ArrayList<String> xmlImage = new ArrayList<String>() ;
	
	transient PrintLog out;
	// ========================================================================
	public SomContainer( SomObjects parent, SomHostIntf somhost, String guid) {
		 
		somObj = parent;
		
		somHost = somhost ;
		somData = somHost.getSomDataObj() ;
		somProcess = somHost.getSomProcess() ;
		taskGuid = guid;
		
		out = somHost.getSfFactory().getOut() ;
	}
	// ========================================================================
	
	
	public XMLBuilder getSomDescriptionXml(SomFluidXMLHelper xEngine) {

		XMLBuilder  dsbuilder = xEngine.getXmlBuilder( "som" );
		
		// xstr is actually not needed, everything will be contained in the builder
		String xstr ;

		xstr = getXML(dsbuilder);
		
		dsbuilder = dsbuilder.up(); // for "som"
		
		return dsbuilder;
	}

	 
	
	/**
	 * 
	 * 
	 * 
	 * @param builder
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getXML( XMLBuilder builder ) {
		String xstr="";
		
		int f,nppvr,tvindex ;
		double nppv,v ;
		String str ;
		  
		// all nodes without data, meta info on the level of lattice, 
		// data source, date
		// bag'ed? 
		
		VirtualLattice somLattice;
		MetaNode node ;
		IntensionalitySurfaceIntf intens ;
		ExtensionalityDynamicsIntf extens ;
		
		ArrayList<Integer> useIndexes ;

		
		
		somLattice = somProcess.getSomLattice();
		somData    = somProcess.getSomDataObject() ;
		
		
		// we also need the particle field !! size (x,y,z), coordinates of node
		
		
		int n = somLattice.size() ;
		int rn = somLattice.getDataSize();
		builder = builder.e("description")
		                     .a("nodecount", ""+n)
		                     .a("recordcount", ""+rn).up()
		                 .e("task").a("id", ""+this.taskGuid );
		builder = builder.up() ;
		
		if ((n>=2) && (rn>3)){
			
			builder = builder.e("nodes");
			
			for (int i=0;i<n;i++){
				
				builder = builder.e("node").a("index", ""+i );

				node = somLattice.getNode(i);
				extens = node.getExtensionality() ;
				intens = new IntensionalitySurface( node.getIntensionality()) ;
				
				builder = builder.e("description");
					int nsz = extens.getCount();
					nppv = extens.getPPV();
					nppvr = extens.getPpvRank();
					
					str = String.format("%.7f", nppv) ;
					builder = builder.e("ppv").a("value",""+str).up();
					builder = builder.e("ppvrank").a("value",""+nppvr).up();
					builder = builder.e("recordcount").a("value",""+nsz).up();
					
				builder = builder.up(); // .e("description");
				
				tvindex = intens.getTargetVariableIndex() ; // not used , must be set
				//intens.getVariablesStr()
				// we have to create an abridged version of the full profile, taking only used variables
				
				useIndexes = (ArrayList<Integer>)somData.getVariables().transcribeUseIndications( intens.getUsageIndicationVector()) ;
				
				ProfileVectorIntf profile = intens.getProfileVector();
				ProfileVectorIntf shortenedProfile = intens.getAbridgedVersion(useIndexes); // (almost) everything is shortened : values, variableStr, UsageIndicationVector
				          
				// "centroid" of node is given by the means across the fields
				// we may save it as "centroid" vector for simplified access
				builder = builder.e("profile");
				
					str = somObj.xEngine.serializeMonoList( shortenedProfile.getValues() );
					builder = builder.e("values").a("list", str).up();
				
					str = somObj.xEngine.serializeMonoList( shortenedProfile.getVariablesStr() );
					builder = builder.e("variables").a("list", str).up();
					
					str = somObj.xEngine.serializeMonoList( useIndexes );
					builder = builder.e("useindexes").a("list", str).up();
					
				str = ""+tvindex;
				
				builder = builder.up(); // .e("profile")
				
				if ((tvindex>=0) && ( useIndexes.indexOf(tvindex)<0)){
					useIndexes.add(tvindex ) ;
				}
				// write useIndexes to model as well, nodes could differ regarding their metric !
				
				builder = builder.e("size").a("value",""+nsz).up();
				builder = builder.e("tvindex").a("value",""+tvindex).up();
				
xstr = somObj.xEngine.getXmlStr(builder, false);
				
				builder = builder.e("variables");	
				f=-1;
				for (int k=0;k<useIndexes.size();k++){
					builder = builder.e("variable").a("index", ""+k );
					f = useIndexes.get(k) ;
					 
					if (f>=0){
						
						BasicStatisticalDescription bsd = extens.getStatistics().getFieldValues().get(f);
						if (nsz != bsd.getCount()){
							// calculate statistics for the fields in the node !!
							// is a feature of the somLattice, simply do it for all nodes
							somLattice.establishProperNodeStatistics() ;
							extens = node.getExtensionality() ;
							bsd = extens.getStatistics().getFieldValues().get(f);
						}
						// ensure that exported transformation models includes the mean, max, min of raw values !!! (for normalization), also the NVE encoding
						// 
						v = bsd.getMean() ; str = String.format("%.7f", v) ;
						builder = builder.e("mean").a("value",""+str).up();
						
					 
						builder = builder.e("count").a("value",""+bsd.getCount()).up();
						
						v = bsd.getVariance() ; if (Math.abs(v)<0.000000000001){v=0.0;} str = String.format("%.7f", v) ;
						builder = builder.e("variance").a("value",""+str).up();
						
						// TODO: missing values per variable !!!
						if (f==tvindex){
							str="1";
						}else{
							str="0";
						}
						builder = builder.e("targetvariable").a("is",""+str).up();
					}
					builder = builder.up(); // .e("variable").a(index, i)
				} // i-> all used fields
				builder = builder.up(); // .e("variables");
				builder = builder.up(); // .e("node").a(index, i)
			} // i -> all nodes
			builder = builder.up(); // .e("nodes");
			
			xstr = somObj.xEngine.getXmlStr(builder, false);
		} // large enough (fields. nodes) ?
		else{
			xstr = "" ;
		}
		return xstr;
	}

}
