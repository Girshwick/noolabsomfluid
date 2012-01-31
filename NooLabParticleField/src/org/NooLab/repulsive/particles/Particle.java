package org.NooLab.repulsive.particles;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.NooLab.repulsive.components.SurroundBuffer;
import org.NooLab.repulsive.components.SurroundBuffers;
import org.NooLab.repulsive.components.topology.ParticleBondsIntf;
import org.NooLab.repulsive.components.topology.ParticleLinkage;
import org.NooLab.repulsive.components.topology.ParticleLinkages;
import org.NooLab.repulsive.intf.DataObjectIntf;
import org.NooLab.repulsive.intf.particles.GraphParticleIntf;
import org.NooLab.repulsive.intf.particles.ParticleStateIntf;

// import org.NooLab.utilities.colors.Conversion;
 

/**
 * TODO:
 *        
 * 
 * 
 */
public 	class Particle implements GraphParticleIntf, ParticleStateIntf, ParticleBondsIntf, DataObjectIntf, Serializable{
	
	
	private static final long serialVersionUID = 7492922749522343802L;

	
	String label="" ;
	
	public double x, y, z, vx, vy,vz, radius;
	   
	PColor mainColor = new PColor(); 
	PColor displayedColor = new PColor();
	 
	
	protected double repulsion;
	protected double attraction ;
	protected double arRatio = 3.2;
	double attractionRange = 1.2;
	double repulsionRange = attractionRange * Math.max( 1.5, arRatio/2.0);
	
	private double kRadiusFactor;
	public int width; 
	public int height ; 
	
	private int nbrParticles; 
	boolean isFrozen=false;
	boolean isActive=false;
	
	// 1=just born, 2=at least once used/referred to, will be set to -1 if it is scheduled to be deleted
	int isAlive= -3;
	
	protected double movedDistance = 0;
	
	int behavior = -1;
	
	private double sizemode;
	private int colormode;
	// ------------------------------------------------------------------------

	int fixedLocation = 0;

	
	// ------------------------------------------------------------------------
	
	
	transient ParticleLinkages particleLinkages  ; 
	int selectedFlag = 0;
	int groupIdentifier = -1;
	
	// ------------------------------------------------------------------------
	
	ArrayList<Long> indexOfDataObject = null ;
	
	transient SurroundBuffer surroundBuffer; // ...its just a pointer, the Particle does not offer any functionality 
	// int surroundBufferIndex; // quite more complicated


	public int charge = 1 ;
	
	// ------------------------------------------------------------------------
	public Particle( int w, int h, double kRadiusFactor,int nbrParticles, double repulsion, double sizemode, int colormode){
		
		width = w; 
		height = h;
		this.kRadiusFactor = kRadiusFactor;
		this.repulsion = repulsion;
		this.nbrParticles = nbrParticles;
		this.colormode = colormode;
		this.sizemode = sizemode;
		this.isAlive = 1;
		initParticle();
	}
	
	// ------------------------------------------------------------------------
	
	
	public Particle(int index, Particle templateparticle, SurroundBuffers sbs) {
		
		transferControlData(templateparticle);
		
		initParticle();
		
		transferBasicData(templateparticle);
		
		if (sbs != null){
			surroundBuffer = new SurroundBuffer(index,sbs);
		
			transferSurroundBuffers( templateparticle );
		}
	}

	
	private void initParticle(){
		

		mainColor.r = (float) Math.random()*0.3f;
		mainColor.g = (float) Math.random();
		mainColor.b = (float) Math.random();
		
		if (mainColor.r<0.22)mainColor.r=0.22f;
		if (mainColor.g<0.22)mainColor.g=0.22f;
		if (mainColor.b<0.22)mainColor.b=0.22f;
		
		
		
		// determining the position, coupling the colr to the position
		// hsv = Conversion.rgbToHSV(r, g, b);
		// x = hsv[0] * (float)(width * kRadiusFactor * 0.5 / 360.0);
		// y = hsv[1] * (float)height;  Random random = new Random();
		// Random random = new Random();random.nextDouble()
		
		x = (((float) Math.random())*(0.96* width))  + (0.02*width);
		y = (((float) Math.random())*(0.96* height)) + (0.02*height);
			
		// determining the size relative to number and window size
		radius = calculateRadius(nbrParticles)  ; 
		if ((sizemode!=0) && (sizemode!=1.0)){
			radius += (int)(  Math.round( Math.random()* sizemode * ((int) (radius )) ));
		}											
		 
		if (colormode==0){
			mainColor.r= 0.6f;
			mainColor.g= 0.6f;
			mainColor.b= 0.6f;
		} 
		displayedColor.copy(mainColor) ;
		// ----------------------------------
		
		particleLinkages = new ParticleLinkages(this);
	}
	
	private void transferBasicData( Particle templateparticle ){
		x = templateparticle.x;
		y = templateparticle.y;
		z = templateparticle.z;
		behavior = templateparticle.behavior;
		arRatio = templateparticle.arRatio;
		attraction = templateparticle.attraction;
		attractionRange = templateparticle.attractionRange  ;
		charge = templateparticle.charge  ;
		displayedColor = templateparticle.displayedColor  ;
		mainColor = templateparticle.mainColor  ;
		fixedLocation = templateparticle.fixedLocation  ;
		isActive = templateparticle.isActive  ;
		isAlive = templateparticle.isAlive;
		isFrozen = templateparticle.isFrozen  ;
		label = templateparticle.label  ;
		groupIdentifier = templateparticle.groupIdentifier   ;
		repulsionRange = templateparticle.repulsionRange   ;
		selectedFlag = templateparticle.selectedFlag   ;
	}

	private void transferControlData( Particle templateparticle ){
		
		width = templateparticle.width   ;
		height = templateparticle.height   ; 
		kRadiusFactor = templateparticle.kRadiusFactor    ;
		nbrParticles = templateparticle.nbrParticles    ;
		repulsion = templateparticle.repulsion    ;
		sizemode = templateparticle.sizemode    ;
		colormode = templateparticle.colormode    ;
	}

	/**
	 * 
	 * transfer data NOT references; </br>
	 * uses Arrays.copy()
	 * 
	 * @param templateparticle
	 */
	private void transferSurroundBuffers( Particle templateparticle ){
		int err=1;
		try{
			if (templateparticle.surroundBuffer != null) {
									
				if (surroundBuffer==null){
					surroundBuffer = new SurroundBuffer( templateparticle.surroundBuffer.getIndex(), 
														 templateparticle.surroundBuffer.getBuffersContext() );
				}    						//           is this the correct SurroundBuffers Context?
				int[] indexes = templateparticle.surroundBuffer.getIndexes();
				double[] distances = templateparticle.surroundBuffer.getDistances();
				if (distances==null){
					templateparticle.surroundBuffer.setDistances( new double[0]) ;
					surroundBuffer.setDistances( new double[0]) ;
				}
				surroundBuffer.setIndexes(Arrays.copyOf(indexes, indexes.length));

				surroundBuffer.setDistances(Arrays.copyOf(distances, distances.length));
				surroundBuffer.setSize(surroundBuffer.getIndexes().length);
			}
		}catch(Exception e){
			String str="?";
			if (surroundBuffer!=null){
				str = surroundBuffer.getParent().toString();
			}
			System.err.println("critical error in transferSurroundBuffers() for particle "+label+
							   "\n       surroundBuffer : "+surroundBuffer+
							   "\n		 its parent     : "+str);
			 
			e.printStackTrace();
		}
	}
	
	public void setDataAsCloneOf(Particle srcParticle) {
		 
		transferSurroundBuffers( srcParticle );
		
		if ( srcParticle.indexOfDataObject != null){
			indexOfDataObject = new ArrayList<Long> (srcParticle.indexOfDataObject );
		}
	}
	
	

	public double calculateRadius(int countOfParticles){
		double result = width * kRadiusFactor / (4 * Math.sqrt(countOfParticles));
		return result ;
	}
	public boolean isFrozen() {
		return isFrozen;
	}

	public void setFrozen(boolean isfrozen) {
		this.isFrozen = isfrozen;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isactive) {
		this.isActive = isactive;
	}

	public int getIsAlive() {
		return isAlive;
	}

	public void setIsAlive(int isAlive) {
		this.isAlive = isAlive;
	}

	public void setMovedDistance(double value) {
		movedDistance = value;
	}

	public double getMovedDistance() {
		return movedDistance;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void resetColor() {
		displayedColor.copy(mainColor) ;
	}
	public void setColor(int ir, int ig, int ib) {
		setColor(ir, ig, ib, 0) ;
	}
		 
	public void setColor(int ir, int ig, int ib, int transparency) {
		displayedColor.r =  (float)ir/255 ;
		displayedColor.g =  (float)ig/255 ;
		displayedColor.b =  (float)ib/255 ;
		displayedColor.transparency = (float)transparency/255; 
	}

	public void setColor(float ir, float ig, float ib, float transparency) {
		displayedColor.r = ir ;
		displayedColor.g = ig ;
		displayedColor.b = ib ;
		displayedColor.transparency = (float)transparency/255; 
	}
	public int[] getColors() {
		int[] cv = new int[4];
		
		
		cv[0] = (int)displayedColor.r * 255;
		cv[1] = (int)displayedColor.g * 255;
		cv[2] = (int)displayedColor.b * 255;
		cv[3] = (int)displayedColor.transparency * 255;
		return cv;
	}

	public int getR(){
		return (int)(displayedColor.r*255.0);
	}


	public int getG(){
		return (int)(displayedColor.g*255.0);
	}


	public int getB(){
		return (int)(displayedColor.b*255.0);
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public int getGroupIdentifier() {
		return groupIdentifier;
	}


	public void setGroupIdentifier(int groupIdentifier) {
		this.groupIdentifier = groupIdentifier;
	}


	public double getRepulsion() {
		return repulsion;
	}


	public void setRepulsion(double repulsion) {
		this.repulsion = repulsion;
		attraction = repulsion / arRatio;
		attractionRange = repulsionRange * Math.max( 1.5, arRatio/2.0);
	}

	public double getAttraction() {
		return attraction;
	}


	public void setAttraction(double attraction) {
		this.attraction = attraction;
	}


	public double getAttractionRange() {
		return attractionRange;
	}


	public void setAttractionRange(double attractionRange) {
		this.attractionRange = attractionRange;
	}


	public double getRepulsionRange() {
		return repulsionRange;
	}


	public void setRepulsionRange(double repulsionRange) {
		this.repulsionRange = repulsionRange;
	}


	@Override
	public void setMaxDistanceToParticleIndex(int index, double maxDistance) {
		
		ParticleLinkage p = new ParticleLinkage();
		
		p.linkIsDirectedTowards = index;
		p.maxDistance = maxDistance;
		
		
	}

	@Override
	public void setMinDistanceToParticleIndex(int index, double minDistance) {
		
		
	}

	@Override
	public void setMinDistanceToAnyParticle(double minDistance) {
		
		
	}

	@Override
	public void unselect() {
		// this is for the GraphParticles perspective...
		selectedFlag = 0;
		resetColor() ;
	}
	
	@Override
	public void setLocationFixed(int flag) {
		if (flag<-1)flag=-1;
		if (flag>1)flag=1;
		
		fixedLocation = flag;
	}


	@Override
	public void setSelected(int flag) {
		
		selectedFlag = flag ;
	}

	@Override
	public boolean isSelected() {
		
		return selectedFlag>0;
	}

	@Override
	public int getSelected() {
		
		return selectedFlag;
	}
 

	public PColor getMainColor() {
		return mainColor;
	}


	public void setMainColor(PColor mainColor) {
		this.mainColor = mainColor;
	}


	public PColor getDisplayedColor() {
		return displayedColor;
	}


	public void setDisplayedColor(PColor color) {
		this.displayedColor = color;
	}
	
	/** range for color values [0..255] */
	public void setDisplayedColor( int[] dcolors) {
		double rangefactor =255.0;
		
		if (colormode==1){
			
		}
		if ((dcolors[0]>1) || (dcolors[0]>1) ||(dcolors[0]>1)){
			 
		}
		displayedColor.r = (float)(dcolors[0] / rangefactor) ;
		displayedColor.g = (float)(dcolors[1] / rangefactor) ;
		displayedColor.b = (float)(dcolors[2] / rangefactor) ;
	}
	
	/** range for color values (float)[0..1] */
	public void setDisplayedColor( float[] displayedColors) {
		displayedColor.r = displayedColors[0]  ;
		displayedColor.g = displayedColors[1]  ;
		displayedColor.b = displayedColors[2]  ;
	}
	
	public int getFixedLocation() {
		return fixedLocation;
	}


	public void setFixedLocation(int fixedLocation) {
		this.fixedLocation = fixedLocation;
	}


	public int getSelectedFlag() {
		return selectedFlag;
	}


	public void setSelectedFlag(int selectedFlag) {
		this.selectedFlag = selectedFlag;
	}


	public ParticleLinkages getParticleLinkages() {
		return particleLinkages;
	}


	public void setParticleLinkages(ParticleLinkages linkages) {
		this.particleLinkages = linkages;
	}

	public void addParticleLinkages(ParticleLinkages pLinkagesIn) {
		
	}

	@Override
	public void setIndexOfDataObject(long index) {
		if (indexOfDataObject==null){
			indexOfDataObject = new ArrayList<Long>();
		}
		indexOfDataObject.add( index);
	}

	public void clearIndexOfDataObject() {
		if (indexOfDataObject==null){
			return;
		}
		indexOfDataObject.clear();
		indexOfDataObject = null;
	}
	
	public void removeIndexOfDataObject(long index) {
		if (indexOfDataObject==null){
			return;
		}
		if (indexOfDataObject.indexOf(index)>=0){
			indexOfDataObject.remove( index);
		}
	}
	
	public long getIndexOfDataObject() {
		long result = -1;
		
		if ((indexOfDataObject!=null) && (indexOfDataObject.size()>0)){
			indexOfDataObject = new ArrayList<Long>();
		}
		indexOfDataObject.get(0);
		
		return result;
	}

	public ArrayList<Long> getIndexesOfAllDataObject() {
		return indexOfDataObject;
	}

	
	
	public void mergeDataObjects( long[] indexes){
		if (indexOfDataObject==null){
			indexOfDataObject = new ArrayList<Long>();
		}
		if ((indexes==null) || indexes.length==0){
			return;
		}
		for (int i=0;i<indexes.length;i++){
			indexOfDataObject.add( indexes[i]);
		}
	}
	public void mergeDataObjects(ArrayList<Long> indexes ) {
		if (indexOfDataObject==null){
			indexOfDataObject = new ArrayList<Long>();
		}
		if ((indexes==null) || indexes.size()==0){
			return;
		}
		for (int i=0;i<indexes.size();i++){
			indexOfDataObject.addAll(indexes);
		}
		
	}
	
	
	@Override
	public void registerDataObject(Object dataObject) {
		
	}


	public SurroundBuffer getSurroundBuffer() {
		return surroundBuffer;
	}


	public void setSurroundBuffer(SurroundBuffer sb) {
		this.surroundBuffer = sb ;
	}
	
	public void importSurroundBuffer(SurroundBuffer sb ) {
		this.surroundBuffer = new SurroundBuffer( sb.getIndex(), sb.getBuffersContext() )  ;
		surroundBuffer.importSurrounding( sb.getIndexes(), sb.getDistances() );
	}

	public void registerBufferReference(SurroundBuffer sb) {
		if (surroundBuffer!=null){
			surroundBuffer.clear();
		}
		surroundBuffer = null;
		surroundBuffer = sb;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public double getRadius() {
		return radius;
	}

	public int getBehavior() {
		return behavior;
	}

	public int getCharge() {
		return charge;
	}



 
	
	
}









