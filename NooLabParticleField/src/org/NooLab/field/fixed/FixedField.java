package org.NooLab.field.fixed;


import java.util.ArrayList;
import java.util.Random;

import org.NooLab.field.FieldHostIntf;
import org.NooLab.field.FieldIntf;
import org.NooLab.field.FieldParticlesIntf;
import org.NooLab.field.fixed.components.FixedFieldCollectStatistics;
import org.NooLab.field.fixed.components.FixedFieldParticle;
import org.NooLab.field.fixed.components.FixedFieldParticles;
import org.NooLab.field.fixed.components.FixedFieldSurroundRetrieval;
import org.NooLab.field.fixed.components.GridNode;
import org.NooLab.field.fixed.components.GridSubstrate;
import org.NooLab.field.interfaces.FixedNodeFieldEventsIntf;
import org.NooLab.field.interfaces.RepulsionFieldEventsIntf;
import org.NooLab.field.repulsive.RepulsionField;
import org.NooLab.field.repulsive.components.FluidFieldSurroundRetrieval;
import org.NooLab.field.repulsive.components.ParticleGrid;
import org.NooLab.field.repulsive.components.data.SurroundResults;
import org.NooLab.field.repulsive.intf.ParticleDataHandlingIntf;
import org.NooLab.field.repulsive.intf.RepulsionFieldsSyncEventsIntf;
import org.NooLab.field.repulsive.intf.particles.RepFieldParticlesIntf;
import org.NooLab.field.repulsive.particles.RepulsionFieldParticles;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.nums.NumUtilities;
import org.NooLab.utilities.strings.ArrUtilities;
import org.math.array.StatisticSample;



/**
 * 
 * 
 * 
 * 
 *
 */
public class FixedField 
							implements 
							            // towards the wrapper and use by other libraries
										FixedFieldWintf,
										// some basic stuff for all fields
										FieldIntf,
										// "event" callbacks to the outside
										FixedNodeFieldEventsIntf,
										FieldHostIntf,
										Runnable{

	
	private static final int _INIT_LAYOUT_SQUARE = 1;
	
	FixedFieldFactory ffFactory ;
	FixFProperties ffProperties ;
	
	FixedFieldParticles particles = new FixedFieldParticles();
	GridSubstrate grid ;
	
	FixedNodeFieldEventsIntf eventsreceptor;
	FixedFieldSurroundRetrieval surroundRetrieval;
	
	private int numberOfParticles;
	private int borderMode;
	private int selectionSize;
	
	StatisticSample statsSampler; // from adapted JMathTools which allows for an external seed
	private int areaWidth;
	private int areaHeight;
	private boolean areaChangedSize;

	private int neighborhoodBorderMode = ParticleGrid.__BORDER_ALL ;
	int initialLayoutMode = _INIT_LAYOUT_SQUARE;
	
	
	private Thread fieldThrd;
	private long threadInitTime;

	
	Random random;
	StatisticSample sampler;
	
	PrintLog out;
	ArrUtilities arrutil = new ArrUtilities();
	NumUtilities numutil = new NumUtilities();
	
	// ========================================================================
	FixedField(FixedFieldFactory factory){
		
		ffFactory = factory;
		ffProperties = ffFactory.fixFProperties ;
		
		out = ffFactory.out;
		// registerEventMessaging( this ) ;
		
		
		random = new Random() ;
		sampler = new StatisticSample(9349);
		random.setSeed(9349);
	}
	// ========================================================================

	
	
	
	public void registerEventMessaging(Object eventObj) {
		
		eventsreceptor = (FixedNodeFieldEventsIntf)eventObj;
	}


	public FixedNodeFieldEventsIntf getEventsreceptor() {
		 
		return eventsreceptor;
	}
	
	public void init(int nbrParticles) {
	 
		statsSampler = new StatisticSample(9438);
		
		grid = new GridSubstrate( this,nbrParticles);
		
		grid.getListOfActiveNodes() ;
		
		grid.createParticles();
	}

	public void close() {
		// clear arrays, stop threads
		
	}
	
	// ------------------------------------------
	
	public void stopFieldThread(){
		
	}
	
	public void startFieldThread(){
		initialization();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	private void initialization(){
		fieldThrd = new Thread(this,"RepulsionField") ;
		threadInitTime = System.currentTimeMillis() ;
		
		FixedFieldCollectStatistics statisticsCollector = new FixedFieldCollectStatistics(this); 
		// FixedFieldSurroundRetrieval surroundRetrieval = new FixedFieldSurroundRetrieval();
	}


	@Override
	public int getType() {
		return FieldIntf._SOM_GRIDTYPE_FIXED;
	}




	public GridSubstrate getGrid() {
		return grid;
	}




	/**
	 * adding (field with border) or inserting (torus) n columns
	 * at a given node;
	 * in case of adding, iCol, 
	 * 
	 */
	public void grow(int nUnits, int iCol, int iRow){

	}
	
	
	
	// ------------------------------------------


	public void setSelectionSize(int _selectionsize) {
		
		selectionSize = _selectionsize ;
	}

	public int getSelectionSize() {

		return selectionSize;
	}

	public void setDelayedOnset(int value) {
	}

	public void update() {
		// TODO Auto-generated method stub
		
	}

 

	public int getNumberOfParticles() {
	
		return numberOfParticles;
	}
	
	// ------------------------------------------------------------------------
	
	@Override
	public double getAverageDistance() {
		// TODO Auto-generated method stub
		return 0;
	}




	@Override
	public double getMinimalDistance() {
		// TODO Auto-generated method stub
		return 0;
	}




	public FieldParticlesIntf getParticles() {
		
		return (FieldParticlesIntf) particles;
	}

	
	
	// ========================================================================

	class SurroundRetrievalHandler{
	
		
		
	}

	// ========================================================================

	
	@Override
	public void onSelectionRequestCompleted(Object results) {
		
		
	}
	
	
	@Override
	public void onAreaSizeChanged(Object observable, int width, int height) {
		
		
	}
	
	@Override
	public void onActionAccepted(int action, int state, Object param) {
		
		
	}
	@Override
	public void statusMessage(String msg) {
		
		
	}
	@Override
	public void onCalculationsCompleted() {
		
		// routing to event sink, should be done in a forked process
		eventsreceptor.onCalculationsCompleted() ;
	}




	@Override
	public void selectionSizeDecrease(int mode, double amount) {
		int layerCount;
		
		if (mode<=1){
			 
			layerCount = numutil.getLayerCountOfHexPattern(selectionSize);
			if (layerCount>=2){
				selectionSize = numutil.calculatePlateletsCountInHexPattern(layerCount-1);
				out.print(2, "selectionSize has been reduced to "+selectionSize+" particles.");
			}else{
				selectionSize = 7;
				out.print(2, "selectionSize has been set to "+selectionSize+" particles.");
			}
		}else{
		}

		
	}




	@Override
	public void selectionSizeIncrease(int mode, double amount) {
		int layerCount;
		// 1 layer : 6+1, 2 layers = 1+6+10 = 17  1+6+10+17+34 
		mode = 1; amount=1; 
		 
		/*
       sum  layer  %		
		1	6      
		7	12     700   
		19	18     270  
		37	24     190
        61  30     165
        91  36     149
        127        139
		*/
		if (mode<=1){
			
			layerCount = numutil.getLayerCountOfHexPattern(selectionSize);
			selectionSize = numutil.calculatePlateletsCountInHexPattern(layerCount+1);
			
			applySelectionSizeRestrictions();
			
			out.print(2, "selectionSize has been increased to "+selectionSize+" particles.");
			/* 
			if (surroundBuffers!=null){
				surroundBuffers.update();
			}
			*/
			
		}else{
			
		}
	}


	private int adaptSelectionSize( int cSelSize) {
		int newSelectionSize = cSelSize;
		
		int[] selsizes,dsizes  ;
		int layerCount,p,n4 ;
		
		
		layerCount = numutil.getLayerCountOfHexPattern(selectionSize);
		newSelectionSize = numutil.calculatePlateletsCountInHexPattern(layerCount);
		
		applySelectionSizeRestrictions();
		cSelSize = selectionSize ;
			
		selsizes = new int[3];
		dsizes = new int[3];
		n4 = numutil.calculatePlateletsCountInHexPattern(3);
		if (layerCount>=3){
			
			selsizes[0] = numutil.calculatePlateletsCountInHexPattern(layerCount-1);
			selsizes[1] = numutil.calculatePlateletsCountInHexPattern(layerCount );
			selsizes[2] = numutil.calculatePlateletsCountInHexPattern(layerCount+1);
				
			dsizes[0] = Math.abs( selsizes[0]  - cSelSize);
			dsizes[1] = Math.abs( selsizes[1]  - cSelSize);
			dsizes[2] = Math.abs( selsizes[2]  - cSelSize);
			
			 
			p = ArrUtilities.arrayMinPos(dsizes) ;
			double pp = ((double)dsizes[p]/(double)selsizes[p])*100.0;
			if (pp<7.5){
				newSelectionSize = selsizes[p];
			}else{
				if (p>selsizes.length-1)p=selsizes.length-1;
				if (selsizes[p]<140){
					newSelectionSize = selsizes[p+1];
				}else{
					newSelectionSize = cSelSize;
				}
			}
		} // layerCount>=2
		
		n4=n4+0;
		return newSelectionSize;
	}


	public void applySelectionSizeRestrictions() {
		// 
		if (ffProperties==null)ffProperties = ffFactory.fixFProperties ;
		if (ffProperties.isRestrictSelectionSize() ){
			int selsz = ffProperties.getSelectionSizeRestriction();
			if ((selsz<numberOfParticles*0.6) && (selsz>11)){
				selectionSize= selsz;
			}
		}
	}
	
	public void setRestrictSelectionSize(boolean flag) {
		
		if (ffProperties==null)ffProperties = ffFactory.fixFProperties ;
		ffProperties.setRestrictSelectionSize(flag) ;
		
	}


	@Override
	public int addParticles(int count) {
		// TODO Auto-generated method stub
		/*
		 * if less than w or h -> 1 band, otherwise 2 bands
		 * proceed to add(x,y) , where x,y are on the right, bottom "border"
		 */
		return 0;
	}




	@Override
	public int addParticles(int x, int y) {
		// TODO Auto-generated method stub
		
		/*
		 * first choice is to insert it nearby x,y
		 * note that at x or y a whole band has to be inserted
		 * x or y: dependent on requested vs desired aspect ratio 
		 */
		
		return 0;
	}


	@Override
	public int addParticles(int[] x, int[] y) {
		// TODO Auto-generated method stub
		return 0;
	}




	@Override
	public int splitParticle(int index, ParticleDataHandlingIntf pdataHandler) {
		/*
		 * there are always 5 physical "strings"=empty columns between populated strings=columns
		 * from those we use only the mid one, such we can move particles around without collisions
		 *  
		 * the exact position of the new particle depends on free space and on similarity to
		 * those nodes of the neighbor strings
		 * 
		 *  minimum resolution factor is 6 !!
		 * 
		 */
		return 0;
	}




	@Override
	public String mergeParticles(int mergeTargetIndex, int[] indexes) {
		// 
		return null;
	}




	@Override
	public String mergeParticles(int mergeTargetIndex, int swallowedIndex) {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public void deleteParticle(int index) {
		deleteParticle( new int[]{index});
	}
	
	public void deleteParticle( int[] indexes){

	
	}




	@Override
	public void moveParticle(int particleIndex, int type, double xParam, double yParam) {
		// 
		
	}



	/**
	 * 
	 */
	@Override
	public String selectParticleAt(int xpos, int ypos, boolean autoselect) {
		// 
		
		boolean found = false, limitreached=false;
		
		/*
		 * 
		 */
		while ((found==false) && (limitreached==false)){
			
			
			break ;
		}
		return null;
	}


	

	/**
	*  selectMode=1 : selecting closest "surroundN" items;
	*  selectMode=2 : selecting closest items within a radius of surroundN   
	*/
	public String getSurround(int particleIndex, int selectmode, int surroundN, boolean autoselect) {
		
		
		String guidStr = "";
		if (surroundN<8){
			surroundN = 8;
		}
	
		if (selectionSize > 6) {
			surroundN = selectionSize;
		}
	
		// note that in case of fixed grids, we do not need a separate thread
		// we do it online because it is quite fast
		// nevertheless we need a fork into an object for allowing parallel requests
	
		guidStr =  (new FixedFieldSurroundRetrieval(this,surroundN, 1, autoselect))
		                           .get(particleIndex, FixedFieldSurroundRetrieval._TASK_SURROUND_X);
		
		
		// ArrayList<SurroundResults> rQueue = somLattice.getSelectionResultsQueue(); 
		// rQueue.add( clonedResults );
		
		return guidStr;
	
	}
 
 
	@Override
	public String getSurround(int xpos, int ypos, int selectMode, int surroundN, boolean autoselect) {

		String guidStr = "";
		int pix;
		
		if (surroundN<8){
			surroundN = 8;
		}

		if (selectionSize > 6) {
			surroundN = selectionSize;
		}

		// we are going to outsource the Surround object into a wrapper,
		// then waiting for an event.

		guidStr = (new FixedFieldSurroundRetrieval(this, surroundN, selectMode, autoselect))
										.get(xpos, ypos, FixedFieldSurroundRetrieval._TASK_SURROUND_X);
		// _TASK_SURROUND_C
		return guidStr;
	}

	
	@Override
	public int getResolutionFactor() {
		
		return grid.getGridResolutionFactor() ;
	}




	@Override
	public int getPhysicalWidth() {
		
		return grid.getFactorization()[2];
	}




	@Override
	public double getPhysicalHeight() {
		
		return grid.getFactorization()[3];
	}


	// ------------------------------------------------------------------------
	
	public void setAreaSize(int width, int height) {
		
		
		
		
		if ((areaWidth != width) || (areaHeight != height)){
		
			informParticlesAboutArea(areaWidth,areaHeight);
			// areaWidth0, areaHeight0
			
			areaChangedSize=true;
		}
	
		areaWidth = width ; 
		areaHeight= height ;
		if (height==0){
			out.print(2,"setAreaSize(), height == 0 ???");
		}
	}


	private void informParticlesAboutArea(int areaWidth2, int areaHeight2) {
		// TODO Auto-generated method stub
		
	}




	public int[] getAreaSize(){
		int[] as = new int[2] ;
		
		as[0] = areaWidth;
		as[1] = areaHeight;
		
		return as;
	}
	

	public int getBorderMode() {
		return borderMode;
	}

	@Override
	public void setBorderMode( int mode) {
		
		borderMode = mode;
	}




	public int getAreaWidth() {
		return areaWidth;
	}

	public int getAreaHeight() {
		return areaHeight;
	}

	public void setAreaHeight(int height) {
		areaHeight = height;
	}
	
	public void setAreaWidth(int width) {
		areaWidth = width;
	}


	public void createParticleForGridNode(GridNode g) {
		FixedFieldParticle particle;
		
		//
		
		particle = new FixedFieldParticle( g.getIndex(),areaWidth ,areaHeight ,numberOfParticles,0,this );
		
		particle.setX( g.getX());
		particle.setY( g.getY());
		
		g.setHasNode(1);
		g.setNodeIsActive(1);
		
		particles.add(particle) ;
		
		numberOfParticles = particles.size() ;
	}




	public FixedFieldFactory getFfFactory() {
		return ffFactory;
	}
	@Override
	public StatisticSample getStatsSampler() {
		
		return sampler;
	}
	@Override
	public Random getRandom() {
		
		return random;
	}




	public PrintLog getOut() {
		return out;
	}
	

 
	
	
	
}


