package org.NooLab.somfluid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import org.NooLab.field.FieldIntf;
import org.NooLab.somfluid.components.ModelOptimizer;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.variables.VariableContributions;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurface;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.lattice.VirtualLattice;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.somtransform.SomFluidXMLHelper;
import org.NooLab.utilities.datatypes.IndexDistanceIntf;
import org.NooLab.utilities.datetime.DateTimeValue;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.connex.NicAddresses;
import org.NooLab.utilities.strings.ArrUtilities;
import org.NooLab.utilities.strings.StringsUtil;

import com.jamesmurty.utils.XMLBuilder;



public class SomContainer {

	SomObjects somObj;
	
	SomHostIntf somHost ;
	SomDataObject somData;
	SomProcessIntf somProcess;
	String taskGuid ;
	
	ArrayList<String> xmlImage = new ArrayList<String>() ;
	
	ModelOptimizer moz;
	
	transient SomFluidProperties sfProperties;
	transient ClassificationSettings cs;
	
	transient ArrUtilities arrutil = new ArrUtilities();
	transient PrintLog out;
	// ========================================================================
	public SomContainer( SomObjects parent, SomHostIntf somhost, String guid) {
		 
		somObj = parent;
		
		somHost = somhost ;
		somData = somHost.getSomDataObj() ;
		somProcess = somHost.getSomProcess() ;
		taskGuid = guid;
	
		sfProperties = somHost.getSfProperties() ;
		cs = sfProperties.getModelingSettings().getClassifySettings() ;
		
		out = somHost.getSfFactory().getOut() ;
	}
	// ========================================================================
	
	
	public XMLBuilder getSomProjectDescriptionXml(SomFluidXMLHelper xEngine) {

		double v;
		XMLBuilder  spbuilder = xEngine.getXmlBuilder( "project" );
		String datestr, machineID="",engineID="",prjLabel="";
		 
			// ............................................
			spbuilder = spbuilder.e("general");
			
				datestr = (new DateTimeValue(14,0)).setStripSeparators(0).get() ;
			  
			    Vector<String> nics = NicAddresses.getMacs();
			    if (nics.size()>0){
			    	ArrayList<String> _nics = new ArrayList<String>(nics);
			    	String str = ArrUtilities.arr2Text(_nics, "-");
			    	machineID = StringsUtil.replaceall(str, "-","");
			    }
			    
			    SomFluidTask _task = somObj.sfFactory.somFluidModule.somTasks.getItemByGuid(taskGuid) ;
			    prjLabel = sfProperties.getPersistenceSettings().getProjectName();
			    long deltaTsec = (_task.closetime - _task.opentime)/1000;
			    
				spbuilder = spbuilder.e("task").a("id", taskGuid).up() 
									 .e("name").a("label", ""+prjLabel ).up()
								 	 .e("date").a("value", datestr).up() 
								 	 .e("timeneeded").a("sec", ""+deltaTsec).a("h", String.format("%.3f", (double)deltaTsec/3600.0)).up()
								 	 .e("machine").a("id", machineID).up()
								 	 .e("engine").a("id", engineID).up()
								 	 ;
			
			spbuilder = spbuilder.up();
			
			// ............................................
			spbuilder = spbuilder.e("context");

				int somType = somHost.getSfProperties().getSomType() ;
				
				 
				
				spbuilder = spbuilder.e("somtype")
										  .a("typeid", ""+somType) // like SomFluidProperties._SOMTYPE_MONO, or PROB important info for classifier
										  .a("nested", "")
										  .a("description", "");
				
						    
				spbuilder = spbuilder.up();
				// - - - - - - - - - - - - - - - - - -

				spbuilder = spbuilder.e("source")
									      .a("type", "")
									      .a("name", "");
				spbuilder = spbuilder.up();
				// - - - - - - - - - - - - - - - - - -
				
				spbuilder = spbuilder.e("bags").a("count", "0");
				spbuilder = spbuilder.up();
				// - - - - - - - - - - - - - - - - - -
							int tc=0,targetMode = cs.getTargetMode(); // like ClassificationSettings._TARGETMODE_SINGLE   ;
					        String str ="0" ;
							if ((somType == FieldIntf._SOMTYPE_MONO)  ){
								str = ""+targetMode ;  //_TARGETMODE_SINGLE  _TARGETMODE_MULTI = 2, 
							}
							if (targetMode==1){
								tc=1;
							}else{
								tc = cs.getTGdefinition().length ;
							}
							/*  there could be several groups !
							 		TGdefinition[i][0] = min;
									TGdefinition[i][1] = max; 
							 
							 */
							String[] grouplabels = cs.getTGlabels() ;
							String grouplabelStr = arrutil.arr2text(grouplabels, ";") ;
				spbuilder = spbuilder.e("target")
				                         .a("mode", ""+targetMode)
				                         .e("groups").a("count", ""+tc);
				                              for (int t=0;t<tc;t++){
				                            	  spbuilder = spbuilder.e("range").a("index", ""+(t+1)).a("min", "").a("max", "").up() ;
				                              }
				                              spbuilder = spbuilder.up();
				   spbuilder = spbuilder.e("labels").a("list", grouplabelStr).up();     
				spbuilder = spbuilder.up();
				// - - - - - - - - - - - - - - - - - -
				
			spbuilder = spbuilder.up();
			
			// ............................................
			spbuilder = spbuilder.e("content");
				
				spbuilder = spbuilder.e("filter").a("count", "0");
				spbuilder = spbuilder.up();
				// - - - - - - - - - - - - - - - - - -
				spbuilder = spbuilder.e("noise").a("active", "0");
				spbuilder = spbuilder.up();
				// - - - - - - - - - - - - - - - - - -
				
				int nEvoSteps =0;
				if (somHost instanceof ModelOptimizer){
					
					moz = (ModelOptimizer)somHost ;
					nEvoSteps = moz.getEvoMetrices().size() ;
				}
				spbuilder = spbuilder.e("optimizer").a("steps", ""+nEvoSteps );
				spbuilder = spbuilder.up();
				// - - - - - - - - - - - - - - - - - -
				if (somHost instanceof ModelOptimizer){
					moz = (ModelOptimizer)somHost ;
					VariableContributions vcs = moz.getModelDescription().getVariableContributions() ;
					
					spbuilder = spbuilder.e("variables");
						spbuilder = spbuilder.e("contributions");
							int vn= vcs.getItems().size(); // used variables
							double sum=0;
							for (int i=0;i<vn;i++){
								String vlabel = vcs.getItem(i).getVariableLabel();
								v = vcs.getItem(i).getScoreDelta() ;
								sum=sum+v;
								str = String.format("%.3f", v) ;
								spbuilder = spbuilder.e("variable").a("label", vlabel).a("value", str).up();
							}
						spbuilder = spbuilder.up();
						if (vn>0){
							v = sum/(double)vn ;
							str = String.format("%.4f", v);
							spbuilder = spbuilder.e("contribution").a("average", str).up();
						}
					spbuilder = spbuilder.up();

				}
				// - - - - - - - - - - - - - - - - - -
				
			spbuilder = spbuilder.up();
			// ............................................
			 
			spbuilder = spbuilder.e("quality");
				double score = -1.0;
				
				try{
					score = moz.getOutresult().getBestMetric().getActualScore();					
				}catch(Exception e){}
				
				str = String.format("%.3f", score) ;
				spbuilder = spbuilder.e("score").a("value", str);
				spbuilder = spbuilder.up();
				// - - - - - - - - - - - - - - - - - -
				
				
				// validation data and robustness score 
				
			spbuilder = spbuilder.up();
			// ............................................

			spbuilder = spbuilder.e("risk");

			             v = sfProperties.getModelingSettings().getClassifySettings().getEcr() ;
			             str = String.format("%.3f", v) ;
			spbuilder = spbuilder.e("ecr").a("value", str).up();      // 
			spbuilder = spbuilder.e("support").a("value", "0").up();  // minimum size of cluster as defined for modeling  

			spbuilder = spbuilder.up();

			// ............................................
			
			// - - - - - - - - - - - - - - - - - -			
		spbuilder = spbuilder.up();
		
		return spbuilder ;
	}


	public XMLBuilder getSomLatticeDescriptionXml(SomFluidXMLHelper xEngine) {

		XMLBuilder  dsbuilder = xEngine.getXmlBuilder( "lattice" );
		
		// xstr is actually not needed, everything will be contained in the builder
		// String xstr ;

		getXML(dsbuilder); // xstr = 
		
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
		String xstr="",liststr;
		
		int f,nppvr,tvindex ;
		double nppv,v ,nnpv=-1.0;
		String str ;
		  
		// all nodes without data, meta info on the level of lattice, 
		// data source, date
		// bag'ed? 
		
		VirtualLattice somLattice;
		MetaNode node ;
		IntensionalitySurfaceIntf intens ;
		ExtensionalityDynamicsIntf extens ;
		
		ArrayList<Integer> useIndexes ;
		ArrayList<Double> varianceProfile = new ArrayList<Double>();
		
		
		somLattice = somProcess.getSomLattice();
		somData    = somProcess.getSomDataObject() ;
		
		
		// we also need the particle field !! size (x,y,z), coordinates of node
		
		
		int n = somLattice.size() ;
		int rn = somLattice.getDataSize();
		builder = builder.e("description")
		                     .a("nodecount", ""+n)
		                     .a("recordcount", ""+rn).up();
		                  
		builder = builder.up() ;
		
		if ((n>=2) && (rn>3)){
			
			builder = builder.e("nodes");
			
			for (int i=0;i<n;i++){
				
				builder = builder.e("node").a("index", ""+i );

				node = somLattice.getNode(i);
				extens = node.getExtensionality() ;
				intens = new IntensionalitySurface( node.getIntensionality()) ;

				/* for a proper consideration of the risk when applying the model (within 1st order correlation as 0-model) 
				   we need the list of immediate neighbors of the node: max 8, sometimes only 4 nodes,
				   detecting the degree of change in variables as a ranked list (of variables) that
				   would lead to a change in the classification outcome: if a node representing a particular 
				   outcome is completely surrounded by those of opposite outcome the probability for a mis-classification
				   would be increased    
				*/
				
				ArrayList<Integer> nodelist = listofNeighbors( node, i , 9) ;
				liststr = ArrUtilities.arr2Text(nodelist).trim() ;
				liststr = StringsUtil.replaceall(liststr, "  ", " ");
				liststr = StringsUtil.replaceall(liststr, " ", ";");
				
				builder = builder.e("description");
					int nsz = extens.getCount();
					nppv = extens.getPPV();
					nppvr = extens.getPpvRank();
					nnpv = extens.getNPV() ;
					str = String.format("%.7f", nppv) ;
					
					builder = builder.e("ppv").a("value",""+str).up();
					builder = builder.e("ppvrank").a("value",""+nppvr).up();
					
					str = String.format("%.7f", nnpv) ;
					builder = builder.e("npv").a("value",""+str).up();
					
					builder = builder.e("recordcount").a("value",""+nsz).up();
					builder = builder.e("neighbors").a("items", ""+liststr).up() ;
					
				builder = builder.up(); // .e("description");
				
				tvindex = intens.getTargetVariableIndex() ; // not used , must be set
				//intens.getVariablesStr()
				// we have to create an abridged version of the full profile, taking only used variables
				
				useIndexes = (ArrayList<Integer>)somData.getVariables().transcribeUseIndications( intens.getUsageIndicationVector()) ;
				
				for (int k=0;k<useIndexes.size();k++){
					f = useIndexes.get(k) ;
					v = -1.0;
					if (f>=0){
						
						BasicStatisticalDescription bsd = (BasicStatisticalDescription) extens.getStatistics().getFieldValues().get(f);
						if (nsz != bsd.getCount()){
							// calculate statistics for the fields in the node !!
							// is a feature of the somLattice, simply do it for all nodes
							somLattice.establishProperNodeStatistics() ;
							extens = node.getExtensionality() ;
							bsd = (BasicStatisticalDescription) extens.getStatistics().getFieldValues().get(f);
						}
					}
				}
				
				varianceProfile.clear() ; 
				if (nsz>0){
					// ? details about fields (variables) : are there some records ?

					for (int k=0;k<useIndexes.size();k++){
						
						f = useIndexes.get(k) ;
						v = -1.0;

						if (f>=0){
							BasicStatisticalDescription bsd ;

							extens = node.getExtensionality() ;
							bsd = (BasicStatisticalDescription) extens.getStatistics().getFieldValues().get(f);
							v = bsd.getVariance() ; if (Math.abs(v)<0.000000000001){v=0.0;} str = String.format("%.7f", v) ;
						}
						varianceProfile.add( v ) ;
					} // i-> all used fields

				}
				
				//ProfileVectorIntf profile = intens.getProfileVector();
				ProfileVectorIntf shortenedProfile = intens.getAbridgedVersion(useIndexes); // (almost) everything is shortened : values, variableStr, UsageIndicationVector
				          
				// "centroid" of node is given by the means across the fields
				// we may save it as "centroid" vector for simplified access
				builder = builder.e("profile");
				
					str = somObj.xEngine.serializeMonoList( shortenedProfile.getValues() );
					builder = builder.e("values").a("list", str).up();
				
					str = somObj.xEngine.serializeMonoList( shortenedProfile.getVariablesStr() );
					builder = builder.e("variables").a("list", str).up();
					
					str = somObj.xEngine.serializeMonoList( varianceProfile );
					if (str.length()>0){
						builder = builder.e("variances").a("list", str).up();
					}
				
					// write useIndexes to model as well, nodes could differ regarding their metric !
					str = somObj.xEngine.serializeMonoList( useIndexes );
					builder = builder.e("useindexes").a("list", str).up();
					
				str = ""+tvindex;
				
				builder = builder.up(); // .e("profile")
				
				builder = builder.e("tvindex").a("value",""+tvindex).up();
				
				// DEBUG ONLY :  xstr = somObj.xEngine.getXmlStr(builder, false);
				
				
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
	
	
	
	private ArrayList<Integer> listofNeighbors( MetaNode node , int index, int count) {
		
		ArrayList<Integer> ixList = new ArrayList<Integer>();
		int ix ;
		 
		ArrayList<IndexDistanceIntf> ixda = somHost.getSomProcess().getSomLattice().getNeighborhoodNodes(index, count,0) ;
		
		for (int i=0;i<ixda.size();i++){
			ix = ixda.get(i).getIndex() ;
			if (ix!=index){
				ixList.add(ix) ;
			}
		}
		if (ixList.size()>1){
			Collections.sort(ixList);
		}
		return ixList;
	}

}
