package org.NooLab.somfluid.properties;

import java.util.ArrayList;
import java.util.Random;

import org.NooLab.somfluid.OutputSettings;
import org.NooLab.somfluid.SomFluidPluginSettings;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.DataFilter;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somfluid.structures.Variables;

import org.NooLab.utilities.strings.ArrUtilities;
import org.NooLab.utilities.strings.StringsUtil;
import org.NooLab.utilities.xml.XMessageAbs;
import org.NooLab.utilities.xml.XMessageIntf;


import com.jamesmurty.utils.XMLBuilder;

/**
 * 
 * this class reads and writes XML strings or files
 * and interprets them as ModelingSettings-object
 * 
 * This is very important for autonomic systems, and for a separation
 * of GUI and SOM engine
 * 
 */
public class SettingsTransporter  extends XMessageAbs implements XMessageIntf{

	SomFluidProperties sfProperties ;
	private ModelingSettings modelingSettings;
	private ClassificationSettings classifySettings;
	private SomFluidPluginSettings pluginSettings;
	private PersistenceSettings persistenceSettings;
	private DataUseSettings dataUseSettings;
	private OutputSettings outputSettings; 
	
	transient StringsUtil strgutils = new StringsUtil();
	
	
	// ========================================================================
	public SettingsTransporter( SomFluidProperties sfp ){
		sfProperties = sfp;

		modelingSettings = sfProperties.getModelingSettings() ;
		classifySettings = modelingSettings.getClassifySettings() ;
		pluginSettings = sfProperties.getPluginSettings() ;
		persistenceSettings = sfProperties.getPersistenceSettings() ;
		dataUseSettings = sfProperties.getDataUseSettings() ;
		outputSettings = sfProperties.getOutputSettings() ;
		
	}
	// ========================================================================
	
	/**
	 * dependent on request, exports all settings into an XML or into a serialized object
	 * @param format 0=sub sections like ModellingSettings are in different files; 1=all properties in one xml 
	 */
	public String exportProperties( int format ) {
		
		String xmlstr = "" ;
		
		XMLBuilder builder, sfgBuilder, duxBuilder, msxBuilder, csxBuilder, psxBuilder, plugxBuilder, outxBuilder ;

		// persistenceSettings  outputSettings pluginSettings modelingSettings
		try {
 
			
			builder = getXmlBuilder( "somfluidproperties" ); 
			
			msxBuilder = modelingSettings.exportPropertiesAsXBuilder(this) ;
			
			psxBuilder = persistenceSettings.exportPropertiesAsXBuilder(this) ; 
			plugxBuilder = pluginSettings.exportPropertiesAsXBuilder(this) ; 
			duxBuilder = dataUseSettings.exportPropertiesAsXBuilder(this) ;
			outxBuilder = outputSettings.exportPropertiesAsXBuilder(this) ;
			
			sfgBuilder = generalSomFluidPropertiesAsXBuilder();
			
			
			if (sfgBuilder!=null){
				builder = builder.importXMLBuilder(sfgBuilder) ;
			}
			if (msxBuilder!=null){
				builder = builder.importXMLBuilder(msxBuilder) ;
			}
			
			if (psxBuilder!=null){
				builder = builder.importXMLBuilder(psxBuilder) ;
			}
			if (plugxBuilder!=null){
				builder = builder.importXMLBuilder(plugxBuilder) ;
			}

			builder.up();
			
			xmlstr = getXmlStr(builder, true);
			 
			
		}catch(Exception e){
		}
		
		
		
		return xmlstr;
	}
	
	
	/**
	 * 
	 * @param sfProperties
	 * @param xmlString
	 */
	public void importSomFluidPropertiesFromXml( SomFluidProperties sfProperties, String xmlString ){
		int vi;
		double v;
		boolean vb;
		String str;
		
		
		setContentRoot("somfluidproperties") 	;
		
					       str = getSpecifiedInfo(xmlString,"//somfluidproperties/general/glueType", "value");
				       	   vi = getInt(str,0) ; // getList(str, Double.class);
		sfProperties.setGlueType( vi );

		
	       					str = getSpecifiedInfo(xmlString,"//somfluidproperties/general/sourceType", "value");
	       					vi = getInt(str,0) ; 
        sfProperties.setSourceType( vi );

		/*
				sfxBuilder = sfxBuilder.e("glueType").a("value", ""+ sfProperties.getGlueType() ).up() ;
				sfxBuilder = sfxBuilder.e("sourceType").a("value", ""+ sfProperties.getSourceType() ).up() ;
				
				sfxBuilder = sfxBuilder.e("dataSrcFilename").a("value", ""+ sfProperties.getDataSrcFilename() ).up() ;
				sfxBuilder = sfxBuilder.e("dataUptakeControl").a("value", ""+ sfProperties.getDataUptakeControl() ).up() ;
				sfxBuilder = sfxBuilder.e("somType").a("value", ""+ sfProperties.getSomType() ).up() ;
				
				sfxBuilder = sfxBuilder.e("initialNodeCount").a("value", ""+ sfProperties.getInitialNodeCount() ).up() ;
				sfxBuilder = sfxBuilder.e("messagingActive").a("value", ""+ sfProperties.getMessagingActive() ).up() ;
				sfxBuilder = sfxBuilder.e("multithreadedProcesses").a("value", ""+ sfProperties.getMultithreadedProcessing() ).up() ;
				sfxBuilder = sfxBuilder.e("restrictionForSelectionSize").a("value", ""+ sfProperties.getRestrictionForSelectionSize() ).up() ;
				sfxBuilder = sfxBuilder.e("extendingDataSourceEnabled").a("value", ""+ sfProperties.isExtendingDataSourceEnabled() ).up() ;

				 				n = 0;
				 				if (sfProperties.getAbsoluteFieldExclusions()!=null){
				 					n= sfProperties.getAbsoluteFieldExclusions().size();
				 				}
				sfxBuilder = sfxBuilder.e("absoluteFieldExclusions")
												.a("count",""+n)
												.a("value", ""+serializeMonoList( sfProperties.getAbsoluteFieldExclusions()) ).up() ;  
				 
				sfxBuilder = sfxBuilder.e("absoluteFieldExclusionsMode").a("value", ""+ sfProperties.getAbsoluteFieldExclusionsMode() ).up() ;
				sfxBuilder = sfxBuilder.e("showSomProgressMode").a("value", ""+ sfProperties.getShowSomProgressMode() ).up() ; 
				sfxBuilder = sfxBuilder.e("isPluginsAllowed").a("value", ""+ strgutils.booleanize( sfProperties.isPluginsAllowed() )).up() ;
				sfxBuilder = sfxBuilder.e("algorithmsConfigPath").a("value", ""+ sfProperties.getAlgorithmsConfigPath() ).up() ;
				sfxBuilder = sfxBuilder.e("multiProcessingLevel").a("value", ""+ sfProperties.getMultiProcessingLevel() ).up() ;
				sfxBuilder = sfxBuilder.e("systemRootDir").a("value", ""+ sfProperties.getSystemRootDir() ).up() ;
		 
		 */
	}
	

	
	
	/**
	 * we could do it by means that are built in to Java... 
	 * yet, we need particular & explicit control about the info written into the XML, 
	 * and this storage should be independent from the structure of the object
	 * 
	 * @return
	 */
	private XMLBuilder generalSomFluidPropertiesAsXBuilder(){
		
		XMLBuilder sfxBuilder;
		int n;
		
		// TODO: don't forget the file reference to the settings.xml of sub-domains !!!!
		
		sfxBuilder = getXmlBuilder( "general" ); 
					
				sfxBuilder = sfxBuilder.e("glueType").a("value", ""+ sfProperties.getGlueType() ).up() ;
				sfxBuilder = sfxBuilder.e("sourceType").a("value", ""+ sfProperties.getSourceType() ).up() ;
				
				sfxBuilder = sfxBuilder.e("dataSrcFilename").a("value", ""+ sfProperties.getDataSrcFilename() ).up() ;
				sfxBuilder = sfxBuilder.e("dataUptakeControl").a("value", ""+ sfProperties.getDataUptakeControl() ).up() ;
				sfxBuilder = sfxBuilder.e("somType").a("value", ""+ sfProperties.getSomType() ).up() ;
				
				sfxBuilder = sfxBuilder.e("initialNodeCount").a("value", ""+ sfProperties.getInitialNodeCount() ).up() ;
				sfxBuilder = sfxBuilder.e("messagingActive").a("value", ""+ sfProperties.getMessagingActive() ).up() ;
				sfxBuilder = sfxBuilder.e("multithreadedProcesses").a("value", ""+ sfProperties.getMultithreadedProcessing() ).up() ;
				sfxBuilder = sfxBuilder.e("restrictionForSelectionSize").a("value", ""+ sfProperties.getRestrictionForSelectionSize() ).up() ;
				sfxBuilder = sfxBuilder.e("extendingDataSourceEnabled").a("value", ""+ sfProperties.isExtendingDataSourceEnabled() ).up() ;

				 				n = 0;
				 				if (sfProperties.getAbsoluteFieldExclusions()!=null){
				 					n= sfProperties.getAbsoluteFieldExclusions().size();
				 				}
				sfxBuilder = sfxBuilder.e("absoluteFieldExclusions")
												.a("count",""+n)
												.a("value", ""+serializeMonoList( sfProperties.getAbsoluteFieldExclusions()) ).up() ;  
				 
				sfxBuilder = sfxBuilder.e("absoluteFieldExclusionsMode").a("value", ""+ sfProperties.getAbsoluteFieldExclusionsMode() ).up() ;
				sfxBuilder = sfxBuilder.e("showSomProgressMode").a("value", ""+ sfProperties.getShowSomProgressMode() ).up() ; 
				sfxBuilder = sfxBuilder.e("isPluginsAllowed").a("value", ""+ strgutils.booleanize( sfProperties.isPluginsAllowed() )).up() ;
				sfxBuilder = sfxBuilder.e("algorithmsConfigPath").a("value", ""+ sfProperties.getAlgorithmsConfigPath() ).up() ;
				sfxBuilder = sfxBuilder.e("multiProcessingLevel").a("value", ""+ sfProperties.getMultiProcessingLevel() ).up() ;
				sfxBuilder = sfxBuilder.e("systemRootDir").a("value", ""+ sfProperties.getSystemRootDir() ).up() ;
	 
		sfxBuilder.up() ;
		
		return sfxBuilder;
	}
	
	public XMLBuilder exportPropertiesAsXBuilder(ClassificationSettings classificationSettings) {
		XMLBuilder csxBuilder ;
		int n=0,m=0;
		ClassificationSettings cs = classificationSettings;
		
		csxBuilder = getXmlBuilder( "classification" );
		
					
				csxBuilder = csxBuilder.e("activeTargetVariable").a("value", ""+ cs.getActiveTargetVariable() ).up() ;
				csxBuilder = csxBuilder.e("targetMode").a("value", ""+ cs.getTargetMode() ).up() ;
				csxBuilder = csxBuilder.e("automaticTargetGroupDefinition").a("value", ""+ booleanize(cs.isAutomaticTargetGroupDefinition()) ).up() ;
				csxBuilder = csxBuilder.e("ecr").a("value", ""+ numerize(cs.getEcr(),7) ).up() ;
					         if (cs.getECRs()!=null){
					        	 n = cs.getECRs().length ;
					         }
				csxBuilder = csxBuilder.e("ecrs")
											.a("count",""+n)
											.a("value", ""+ serialize( cs.getECRs()) ).up() ;
				csxBuilder = csxBuilder.e("preferredSensitivity").a("value", ""+ cs ).up() ;
				csxBuilder = csxBuilder.e("capacityAsSelectedTotal").a("value", ""+ cs ).up() ;
				csxBuilder = csxBuilder.e("isEcrAdaptationAllowed").a("value", ""+ cs ).up() ;
				csxBuilder = csxBuilder.e("maxTypeIcount").a("value", ""+ cs ).up() ;
				csxBuilder = csxBuilder.e("maxTypeIIcount").a("value", ""+ cs ).up() ;
							 if (cs.getTGlabels()!=null){
					        	 n = cs.getTGlabels().length ;
					         }
				csxBuilder = csxBuilder.e("tglabels")
											.a("count",""+n)
											.a("value", serialize( cs.getTGlabels()) ).up() ;
							 n=0;m=0;
							 if (cs.getTGdefinition()!=null){
					        	 n = cs.getTGdefinition().length ;
					        	 if (n>0){
					        		 m = cs.getTGdefinition()[0].length ;
					        	 }
					         }
				csxBuilder = csxBuilder.e("tgdefinition").a("value", ""+ cs ) 
											.a("ncount",""+n).a("mcount",""+m)
											.a("value", serialize( cs.getTGdefinition()) ).up() ;
				csxBuilder = csxBuilder.e("").a("value", ""+ cs ).up() ;
				csxBuilder = csxBuilder.e("").a("value", ""+ cs ).up() ;
				
		
		return csxBuilder;
	}

	/**
	 * this does NOT export the settings which are necessary for applying the model !
	 * ...it exports much more...
	 *  
	 * @param modelingSettings
	 * @return
	 */
	public XMLBuilder exportPropertiesAsXBuilder( ModelingSettings modelingSettings ){
	 
		XMLBuilder msBuilder, csxBuilder;
		
		ModelingSettings ms = modelingSettings ; 
		SomBagSettings bags = modelingSettings.somBagSettings ;
		ValidationSettings vast = modelingSettings.validationSettings ;
		CrystalizationSettings cryst = modelingSettings.crystalSettings ;
		OptimizerSettings ops = modelingSettings.optimizerSettings ;
			
			
		msBuilder = getXmlBuilder( "Modeling" );
		 
		 
			//  ...........................................
				csxBuilder = classifySettings.exportPropertiesAsXBuilder(this) ; 
				if (csxBuilder!=null){
					msBuilder = msBuilder.importXMLBuilder(csxBuilder) ;
				}
				
			//  ...........................................	
				msBuilder = msBuilder.e("Optimizer");
								msBuilder = msBuilder
												.e("maxStepsAbsolute").a("value", ""+ops.maxStepsAbsolute ).up()
												.e("atLeastWithoutChange").a("value", ""+ops.atLeastWithoutChange ).up()
												.e("stopAtNormalizedQuality").a("value", ""+numerize(ops.stopAtNormalizedQuality,4) ).up()
												.e("activeTimeLimitation").a("value", booleanize(ops.activeTimeLimitation ) ).up()
												.e("timeLimitationPerStep").a("value", ""+ops.timeLimitationPerStep ).up()
												.e("minimalNumberOfCases").a("value", ""+ops.minimalNumberOfCases ).up()
												.e("samplesRatio").a("value", ""+numerize(ops.samplesRatio,7) ).up()
												.e("maxAvgVariableVisits").a("value", ""+ops.maxAvgVariableVisits ).up()
												.e("durationHours").a("value", ""+ numerize(ops.durationHours,7) ).up()
												.e("shortenedFirstCycleAllowed").a("value", booleanize(ops.isShortenedFirstCycleAllowed()) ).up()
											.up();
				msBuilder = msBuilder.up();
				
				 
				msBuilder = msBuilder.e("SomBag");
				 
				msBuilder = msBuilder.up();

				msBuilder = msBuilder.e("Validation");
								// vast
				msBuilder = msBuilder.up();

				
				msBuilder = msBuilder.e("Crystalization");
				 				// cryst
				msBuilder = msBuilder.up();
			//  ...........................................
				
			msBuilder = msBuilder.e("general");
			
			
					msBuilder = msBuilder
									.e("variablescount").a("value", ""+ ms.variables.size() ).up()   
									.e("targetVariableCandidates").a("value", ""+ serializeMonoList(ms.targetVariableCandidates) ).up()  
									.e("activeTvLabel").a("value", ""+ ms.activeTvLabel ).up() 
									.e("tvTargetGroupLabelColumnHeader").a("value", ""+ ms.tvTargetGroupLabelColumnHeader ).up() 
									.e("targetedModeling").a("value", ""+ booleanize(ms.targetedModeling) ).up()  
									.e("distanceMethod").a("value", ""+ ms.distanceMethod ).up()  
									.e("maxMissingValuePortionPerNode").a("value", ""+ numerize( ms.maxMissingValuePortionPerNode,7) ).up()  
									.e("defaultDistanceContributionBetweenMV").a("value", ""+ numerize( ms.defaultDistanceContributionBetweenMV,7) ).up()  
									.e("autoSomSizing").a("value", ""+ booleanize( ms.autoSomSizing ) ).up()  
									.e("autoSomDifferentiate").a("value", ""+ booleanize( ms.autoSomDifferentiate ) ).up()  
									.e("somCrystalization").a("value", ""+ booleanize( ms.somCrystalization ) ).up()  
									.e("somGrowthMode").a("value", ""+ serializeMonoList(ms.somGrowthModes) ).up() 
									.e("somGrowthControl").a("value", ""+ ms.somGrowthControl ).up() 
									.e("somGrowthControlParams").a("value", ""+ serializeMonoList(ms.somGrowthControlParams) ).up()  
									.e("activationOfGrowing").a("value", ""+ booleanize(ms.activationOfGrowing) ).up()  
									.e("clusterMerge").a("value", ""+ ms.clusterMerge ).up()  
									.e("clusterSplit").a("value", ""+ ms.clusterSplit ).up()  
									.e("minimalSplitSize").a("value", ""+ ms.minimalSplitSize ).up()  
									.e("intensityForRearrangements").a("value", ""+ ms.intensityForRearrangements ).up()  
									.e("restrictionForSelectionSize").a("value", ""+ ms.restrictionForSelectionSize ).up()  
									.e("minimalNodeSize").a("value", ""+ ms.minimalNodeSize ).up()  
									.e("winningNodesCount").a("value", ""+ ms.winningNodesCount ).up()  
									.e("absoluteRecordLimit").a("value", ""+ ms.getAbsoluteRecordLimit() ).up()  
									.e("somType").a("value", ""+ ms.getSomType() ).up()  
									.e("calculateAllVariables").a("value", ""+ booleanize(ms.calculateAllVariables ) ).up()  
									.e("confirmDataReading").a("value", ""+ ms.confirmDataReading ).up()  
									.e("actualRecordCount").a("value", ""+ ms.actualRecordCount ).up()
									.e("useVectorModegetDedicated").a("value", ""+ ms.useVectorModegetDedicated ).up()
									.e("maxL2LoopCount").a("value", ""+ ms.getMaxL2LoopCount() ).up()
									.e("initialAutoVariableSelection").a("value", ""+ ms ).up()
									.e("blacklistedVariablesRequest").a("value", ""+ serializeMonoList(ms.getBlacklistedVariablesRequest() ) ).up()
									.e("initialVariableSelection").a("value", ""+ serializeMonoList(ms.initialVariableSelection ) ).up()
									.e("contentSensitiveInfluence").a("value", ""+ booleanize(ms.getContentSensitiveInfluence() ) ).up()
									.e("isExtendedDiagnosis").a("value", ""+ booleanize(ms.isExtendedDiagnosis() ) ).up()
									.e("determineRobustModels").a("value", ""+ booleanize(ms.isDetermineRobustModels() ) ).up()
									.e("checkingSamplingRobustness").a("value", ""+ booleanize(ms.isCheckingSamplingRobustness() ) ).up()
									.e("canonicalReduction").a("value", ""+ booleanize(ms.isCanonicalReduction() ) ).up()
									.e("performCanonicalExploration").a("value", ""+ booleanize(ms.getPerformCanonicalExploration() ) ).up()
									;
									//  DataFilter dataFilter;
			msBuilder = msBuilder.up();	
			 
		msBuilder.up() ;
		
		return msBuilder;
	}
	
	
	public String template( String action){ 
		 
		String xmlstr = "" ;
		XMLBuilder builder ;

		
		try {
 
			
			builder = getXmlBuilder( "messageboard" ).a( "name", "spela").a("role", "" ); 
			
			builder.e("subscription")
			
					.up() ;
			
			  
			
			xmlstr = getXmlStr(builder, true);
			 
			
		}catch(Exception e){
		}
		
		return xmlstr;
	
	}
	
	/**
	 *   
	 * @param xmlSettings  // might be an URL or the XML itself
	 * 
	 */
	public SomFluidProperties importProperties(String xmlSettings ) { 
		
		return null;
	}
	
}
