package org.NooLab.somfluid.astor;



import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.properties.PersistenceSettings;




public class AstorProperties extends SomFluidProperties{

	private static final long serialVersionUID = 6826459158158913394L;
	
	static AstorProperties asp;

	private SomFluidProperties sfp;
	
	// ========================================================================
	public AstorProperties(){
		super();
		sfp = super.getSfp();
		asp=this;
	}

	public AstorProperties(SomFluidProperties sfProperties){
		super(sfProperties);
		sfp = super.getSfp();
		asp=this;
	}

	public static AstorProperties get() {
		new AstorProperties();
		return asp;
	}
	// ========================================================================
	
	public SomFluidProperties getSomFluidProperties(){
		return super.getInstance() ;
	}
	 
	public PersistenceSettings getPersistenceSettings(){
		return sfp.getPersistenceSettings() ;
	}
	
}
