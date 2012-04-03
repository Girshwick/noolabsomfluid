package org.NooLab.somfluid.util;

/**
 * from: package org.mechaevil.util.Algorithms;
 */

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.NooLab.utilities.logging.PrintLog;
import org.apache.commons.collections.CollectionUtils;

/**
 *  this class is a wrapper for the PowerSet generator; it provides a range of helpful methods
 *   
 *  - drawing randomly with repetitions allowed or not allowed,
 *    (TODO: max n repetitions allowed)
 *  - constraints for accepting a selection, such as size or content of the sets
 *  - selection probability for individual items (NOT for sets!)
 *    such some sets are preferred and others are postponed 
 *
 */
@SuppressWarnings("rawtypes")
public class PowerSetSpringSource {

	PowerSet<Integer> pset ;
	Iterator setIterator ; 
	
	long setSize=0L, constrainedSetSize=0L, setPosIndex=0L;
	
	int limitedAmountOfPreparedSets = -1;
	
	
	ArrayList<String> items = new ArrayList<String>();
	Set<Integer> itemset = new TreeSet<Integer>();
	PowerSetConstraints constraints = new PowerSetConstraints();

	SortedMap<Integer,String> labelPositionMap = new TreeMap<Integer,String>();
	SortedMap<String,Integer> positionLabelMap = new TreeMap<String,Integer>();

	
	/** 
	 * the items of this list refre to the items from which the sets are made
	 *  
	 * the items describe the count of being selected, selection probabilities,
	 * counts of being postponed in case of weighted, random selection
	 * 
	 */
	SelectionProperties selectionProperties = new SelectionProperties();


	boolean removalInactive = false;
	int putativeRemovalPosition=-1;
	
	ArrayList<Integer> visitedGlobalIndexes = new ArrayList<Integer>();
	ArrayList<Integer> remainingConstrainedIndexes = new ArrayList<Integer>();
	
	ArrayList<Set<Integer>> preparedSets = new ArrayList<Set<Integer>>();
	ArrayList<Set<Integer>> expicitlyExcludedSets = new ArrayList<Set<Integer>>();
	
	boolean isPrepared=false;
	Random random = new Random();
	private int preferredLimit = -1;
	private int preferredSizeLimit;
	
	PrintLog out = new PrintLog(2,true);
	// ========================================================================
	@SuppressWarnings("unused")
	public PowerSetSpringSource( ArrayList<String> inItems){
		
		preferredLimit = (int) (inItems.size() * 0.8);
		init(inItems);
		
		random.setSeed(1324356);
		random.nextDouble() ;
	}

	public PowerSetSpringSource(){
		
	}

	public PowerSetSpringSource(int preferredlimit) {
		preferredLimit = preferredlimit;
	}

	private void init(ArrayList<String> inItems){
		if ((inItems!=null) && (inItems.size()>0)){
			items.clear(); items.addAll(inItems);
			generate();
			
			// we have to change all this stuff to Set<Integer> and using a map
			// using long strings is very expensive
			setSize=0;
			// for (Set<String> s : pset) {setSize++;}	
			long setSize = Math.round( Math.pow(2,inItems.size() )) ;
			// int d = (int) (setSize2 -setSize) ;
			
			// System.out.println("total size of power set = "+setSize) ;
			constrainedSetSize = setSize;
			
			generate();
			
			constraints.setMaps( labelPositionMap, positionLabelMap );
		}
		
		
	}
	// ========================================================================
	
	public void setItems( ArrayList<String> inItems){
		init(inItems) ;
	}
	
	// we could prepare from ixLo to ixHi in order to relaease stress to memory...
	private void generate(){
		
		String str;
		
		for (int i = 0; i<items.size(); i++){
			str = items.get(i).trim();
			if (str.length()>0){
				itemset.add(i);
				labelPositionMap.put(i,str);
				positionLabelMap.put(str,i);
			}
		}
		
		for (int i = 0; i<items.size(); i++){
			SelectionProperty sp = new SelectionProperty();
			sp.name = items.get(i) ;
			selectionProperties.add(sp) ;
		}
		
		if (pset!=null){
			pset = null ;
		}
		pset = new PowerSet<Integer>(itemset);
		pset.setRandom(random) ;
		setIterator = pset.iterator();
		
	}
	
	
	/**
	 * we need to prepare for random selections; 
	 * it will be called when needed, that is the first call fo getNextRandom() 
	 * 
	 * TODO: there should be a switch about the preference of comparably small sets, e.g. by prob ratio 3:1
	 *       2: parameters: targeted preferred size, probability ratio 
	 */
	private void prepare(){
		long amount;
		amount = this.setSize;
		prepare(amount);
	}
	
	/**
	 * uses the values for amount and prob ratio to create a further bunch of values
	 */
	public void prepareNextSection(){
		
	}
	
	public void prepare(long amount){
		int z = -1, n = 0;
		
		remainingConstrainedIndexes.clear() ;
		preparedSets.clear();
		
		for (Set<Integer> s : pset) {
			z++;

			// s = pset.getNext(z);
			
			boolean setIsOk = constraints.check(s);
			if (setIsOk) {
				
				// System.out.println("" + n + " of " + z + "  " + s);
				remainingConstrainedIndexes.add(n) ;
				preparedSets.add(s) ;
				n++;
			}else{
if (z%100==0){
	z=z+1-1;
}
				// System.out.println("excluded (index:"+z+"): "+s.toString() );
			}
		}
		out.print(2,"remaining combinations after applying constraints : "+remainingConstrainedIndexes.size()+" of "+z);
		constrainedSetSize = remainingConstrainedIndexes.size() ;
	}


	
	public void test(){
		test();
		constraints.setMinimumLength(3) ;
		constraints.setMaximumLength(6) ;

		constraints.setExcludingItems( new String[]{"C","F"}) ;
		constraints.setMandatoryItems( new String[]{"B","D"}) ;

		constraints.setAllowForPositionalNeighborhoods(1) ;

		printAll();
		
		getNext();
	}
	
	public ArrayList<String> getNextByLength(int expectedLength, int minDeviation, int plusDeviation) {
		boolean found=false;
		int firstRPos=-1;
		ArrayList<String> setAsList = new ArrayList<String>();
		ArrayList<String> firstSetItemList = new ArrayList<String>();
		ArrayList<String> sItemList;
		
		removalInactive = true;
		int pd,md,z=0, observedLen;
		int zLimit ;
		zLimit = (int) (constrainedSetSize * expectedLength *2.3 + 100);
		
			
		while ((found==false) && (z<zLimit)){
			z++;
			sItemList = getNextRandom();
			if (firstSetItemList.size()==0){
				firstSetItemList = sItemList;
				firstRPos = putativeRemovalPosition;
				// TODO not the first, but the closest ...
			}
			observedLen = sItemList.size();
			md=0; pd=0;
			if (expectedLength >= observedLen) {
				md = expectedLength - observedLen;
			}
			if (expectedLength <= observedLen) {
				pd = observedLen - expectedLength; 
			}
			
			if (( md <=minDeviation) && ( pd<=plusDeviation)) {

				if(putativeRemovalPosition>=0){
					int p=putativeRemovalPosition;
					remainingConstrainedIndexes.remove(p);
					// System.out.println("removedc(2): "+putativeRemovalPosition+" ,  remaining positions "+remainingConstrainedIndexes.size());
				}else{
					found=false;
				}
				found=true;
				setAsList = sItemList;
			}
			if ((sItemList==null) ||(sItemList.size()==0) && (found==false)){
				break;
			}
			
		} // -> found ?
		
		if (found==false){
			setAsList = firstSetItemList;
			if ((firstRPos>=0) && (firstRPos<remainingConstrainedIndexes.size())){
				remainingConstrainedIndexes.remove(firstRPos);
			}
		}
		removalInactive = false;
		return setAsList;
	}


	/**
	 * 
	 * this is a sibling to the random selection, it also needs the prepare() and the
	 * remainingConstrainedIndexes
	 * 
	 * @return
	 */
	
	public ArrayList<String> getNextSimilar( ArrayList<String> templateList, int minDeviations, int posDeviations ) {
		boolean found=false;
		int firstRPos=-1, mind=9999;
		ArrayList<String> setAsList = new ArrayList<String>();
		ArrayList<String> firstSetItemList = new ArrayList<String>();
		ArrayList<String> sItemList;
		
		removalInactive = true;
		int z=0;
		int zLimit ;
		if (templateList!=null){
			zLimit = templateList.size() * templateList.size() *100 + 10+ remainingConstrainedIndexes.size()/2;
		}else{
			zLimit = remainingConstrainedIndexes.size()/2 ; 
		}
			
		while ((found==false) && (z<zLimit)){
			z++;
			sItemList = getNextRandom();
			
			if (firstSetItemList.size()==0){
				firstSetItemList = sItemList;
				firstRPos = putativeRemovalPosition; // is set in getNextRandom
				// TODO not the first, but the closest ...
			}
			
			
			Collection c1 = CollectionUtils.subtract(templateList, sItemList); // if smaller than template
			Collection c2 = CollectionUtils.subtract(sItemList, templateList); // if larger than template
			
			if ((c1.size()<=minDeviations) && (c2.size()<=posDeviations)) {
if (remainingConstrainedIndexes.size()%200==0){
	found=true;
}
				if(putativeRemovalPosition>=0){
					int p=putativeRemovalPosition;
					remainingConstrainedIndexes.remove(p);
					// System.out.println("removedc(2): "+putativeRemovalPosition+" ,  remaining positions "+remainingConstrainedIndexes.size());
				}else{
					found=false;
				}
				found=true;
				setAsList = sItemList;
			}else{
				int d,d1,d2;
				d1 = c1.size() - minDeviations ;
				d2 = c2.size() - posDeviations ;
				d=d1+d2;
				if (mind>d){
					mind=d;
					putativeRemovalPosition = d;
					firstSetItemList = sItemList;
					firstRPos = d;
				}
			} //
			
			if ((sItemList==null) ||(sItemList.size()==0) && (found==false)){
				break;
			}
			
		} // -> found ?
		
		if (found==false){
			setAsList = firstSetItemList;
			if ((firstRPos>=0) && (firstRPos<remainingConstrainedIndexes.size())){
				remainingConstrainedIndexes.remove(firstRPos);
			}
		}
		removalInactive = false;
		return setAsList;
	}
	/**
	 * returns a set by random, repeats are excluded 
	 * 
	 * @return
	 */
	public ArrayList<String> getNextRandom() {
		return getNextRandom( 0 ) ;
	}
	
	private ArrayList<String> getNextRandom( int overRule ) {
		
		ArrayList<String> setAsList = new ArrayList<String>();
		Set<Integer> strset = null;

		boolean setIsOk = false,sProbIsOk=false;
		boolean found=false;
		int p,n,selectedIx=-1;
		String str;
		// 
		
		if (isPrepared==false){
			isPrepared = true;
			prepare() ;
			// will fill the list "remainingConstrainedIndexes"
		}
		int maxlen = constraints.getMaximumLength(); if(maxlen<=0)maxlen=1;
		int k=maxlen;
		if (k<=1){
			k=60;
		}
		long zLimit = remainingConstrainedIndexes.size()*k*k;
		int z=0;
		
		while ((found==false) && (z< zLimit )){
			n = remainingConstrainedIndexes.size();
			p = (int) Math.floor(random.nextDouble()* n) ;
			 
			if ((p >= 0) && (p <= remainingConstrainedIndexes.size())) {

				selectedIx = remainingConstrainedIndexes.get(p);
				strset = preparedSets.get(selectedIx);

				setIsOk = false;
				setIsOk = constraints.check(strset);
				
				if (setIsOk){
					setIsOk = isExpicitlyExcluded(strset)==false;
				}
				// checking the content set for its selection probabilities 
				 
				sProbIsOk = selectionProperties.checkAcceptance( strset ) ;
				
				// overRule=1 if getNext() is called while random selection has been prepared
				if (overRule>=1){
					sProbIsOk=true;	
				}
				
				
				if ((setIsOk) && (sProbIsOk)){
					// apply a geom descendent over positions of limit+ for a given random number
					sProbIsOk = checkSizeAcceptance(strset.size(), preferredSizeLimit) ;
					
				}
				
				if ((setIsOk) && (sProbIsOk)){
					int i=-1;
					putativeRemovalPosition = -1;
					if (removalInactive==false){
						// if we have an higher order selection loop we should not remove it here
						// instead we just save this position as putative removal
						remainingConstrainedIndexes.remove(p);
						// System.out.println("removed(1): "+p+" ,  remaining positions "+remainingConstrainedIndexes.size());
					}else{
						putativeRemovalPosition = p;
					}
					
					for (Integer ival : strset) {
						i++;
						//if ((fp==0) || (selectionProperties.lastFailingPositions.indexOf(i)<0))
						{
						
							str = labelPositionMap.get(ival);
							setAsList.add(str);
							SelectionProperty sp = selectionProperties.getItemByName(str);
							if (sp != null) {
								sp.selectionCount++;
							}
						}
					}

					found = true;
				} // set ok ?
			}
			  
			z++;
		} // -> found?
		if ((found==false) && (z>0)){
			System.out.println("...after <"+z+"> trials no matching set found!");
		}
		z=0;
		return setAsList;
	}
	
	public void addSetAsExpicitlyExcluded( Set<Integer> checkedset) {
		
		
	}
	
	public void addStringSetAsExpicitlyExcluded( ArrayList<String> strings) {
		Set<String> strset = new TreeSet<String>() ;
		
		strset.addAll(strings);
		addStringSetAsExpicitlyExcluded(strset) ;
	}

	public void addStringSetAsExpicitlyExcluded( Set<String> xset) {
		Set<Integer> cset = new TreeSet<Integer>() ;
		
		// translate by using map
		addSetAsExpicitlyExcluded(cset);
	}

	private boolean isExpicitlyExcluded(Set<Integer> checkedset) {
		boolean rB=false;
		
		for ( Set<Integer> xset: expicitlyExcludedSets){
			
			if ((xset.containsAll(checkedset)) && (checkedset.containsAll(xset))){
				rB=true;
				break;
			}
			
		}
		
		
		return rB;
	}

	private boolean checkSizeAcceptance(int actualsize, int preferredSizeLimit2) {
		boolean rB=true;
		
		if (actualsize > preferredSizeLimit){
			
		}
			
		return rB;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> getNext() {
		
		ArrayList<String> setAsList = new ArrayList<String>();
		Set<Integer> strset = null;
		
		// () does not work properly: hasNext=false, but next nevertheless works
		/*if ((setIterator.hasNext()==false) || (setPosIndex >= setSize)) {
			generate();
			for (int i = 0; i < setPosIndex; i++) {
				strset = (Set<String>) setIterator.next();
			}
		}
		*/
		
		if (remainingConstrainedIndexes!=null){
			
			setAsList = getNextRandom(1);
			return setAsList;
		}
		
		boolean setIsOk = false;
		int z=-1;
		
		while ((setIsOk==false) && (setPosIndex<=setSize)){
			
			z++;
			setIsOk = true;
			
			// strset = pset.getNext(z);
			
			strset = (Set<Integer>)setIterator.next();
			setIsOk = constraints.check( strset );
			
			setPosIndex++;
		} // -> setIsOk ?
		
		if (setIsOk){
			for (Integer ival:strset){
				
				String str = labelPositionMap.get(ival);
				setAsList.add(str) ;
			}
		}
		
		return setAsList;
	}


	/**
	 * if we choose randomly we have a list of prepared sets which we have to clean
	 * this needs not to be adapted, because we anyway check whether a particular set applies
	 * 
	 * @param str
	 */
	public void removeSetsByItem(String str){
		
		constraints.addExcludingItem(str) ;
	}
	public void removeSetsByItems(ArrayList<String > strings){
		
		constraints.addExcludingItems( strings) ;
	}
	
	/**
	 * the same order is expected as it is given by the itemset
	 * 
	 * @param sprobs
	 */
	public void setSelectionProbabilities( ArrayList<Double> sprobs){
		
		for (int i=0;i<sprobs.size();i++){
			setSelectionProbability( items.get(i), sprobs.get(i) );
		}
	}
	
	public void setSelectionProbabilities( double sprobs){
		for (int i=0;i<items.size();i++){
			setSelectionProbability( items.get(i), sprobs );
		}
	}
	public void setSelectionProbabilities( double[] sprobs){
		
		for (int i=0;i<sprobs.length;i++){
			setSelectionProbability( items.get(i), sprobs[i] );
		}
	}

	public void setSelectionProbability( String item, double sprobs){
		SelectionProperty sp;
		
		sp = selectionProperties.getItemByName(item) ;
		
		if (sp!=null){
			sp.selectionProbability = sprobs ;
		}
	}
	/**
	 * the same order is expected as it is given by the itemset
	 * 
	 * if sets are selected randomly, this max is not absolute, but if this threshold will be arrived,
	 * the selection probability will be lowered by 10% on each further call
	 * @param sprobs
	 */
	public void setMaxSelectionCounts( ArrayList<Integer> scounts){
		
		
		for (int i=0;i<scounts.size();i++){
			setMaxSelectionCount( items.get(i), scounts.get(i) );
		}
		
	}
	public void setMaxSelectionCount( String item, int scount){
		SelectionProperty sp;
		
		sp = selectionProperties.getItemByName(item) ;
		
		if (sp!=null){
			sp.maxSelectionCount = scount ;
		}
	}
	public void setMaxSelectionCounts(int scount){
		for (int i=0;i<items.size();i++){
			setMaxSelectionCount( items.get(i), scount );
		}
	}
	
	
	
	public void setPreferredSizeLimit(int sizelimit) {
		
		preferredSizeLimit = sizelimit;
	}

	public long remainingItems(){
		
		return  (setSize - setPosIndex);
	}
	
	public void resetPosition(){
		generate();
	}
	
	public ArrayList<ArrayList<String>> getAll(){
		ArrayList<ArrayList<String>> sets = new ArrayList<ArrayList<String>>();
	
		
		int z=0,n=0;
		
		for (Set<Integer> ival : pset) {
			z++;
			String s = labelPositionMap.get(ival) ;
			boolean setIsOk = constraints.check( ival );
			if (setIsOk){
				n++;
				System.out.println(""+n+" of "+z +"  "+s);
			}
		}
		return sets;
	}
	
	public void printAllLabels(){
		// translate integer sets back to list of strings using the map  labelPositionMap
	}
	
	public void printAll(){
		int z=0,n=0;
		
		for (Set<Integer> ival : pset) {
			z++;
			boolean setIsOk = constraints.check( ival );
			if (setIsOk){
				n++;
				System.out.println(""+n+" of "+z +"  "+ival);
			}
		}
	
	}

	/**
	 * @return the itemset
	 */
	public Set<Integer> getItemset() {
		return itemset;
	}
	/**
	 * @param itemset the itemset to set
	 */
	public void setItemset(Set<Integer> itemset) {
		this.itemset = itemset;
	}
	public void activateConstraints(int flag) {
		
		constraints.setActive( flag>=1) ;
		
	}
	/**
	 * @return the constraints
	 */
	public PowerSetConstraints getConstraints() {
		return constraints;
	}
	
	
	class SelectionProperties{
		
		ArrayList<SelectionProperty> spItems = new ArrayList<SelectionProperty> ();
		ArrayList<Integer> lastFailingPositions = new ArrayList<Integer>();
		
		
		public SelectionProperties(){
			
		}


		public boolean checkAcceptance(Set<Integer> strset) {
			boolean rB=false;
			double rv, minProb=1.0001,supinfProb=0.0;
			SelectionProperty sp;
			int pp=0,pos;
			String str;
			
			lastFailingPositions.clear();
			
			rv = random.nextDouble() ;
			pos=-1;
			for (Integer ival: strset) {
				  
				pos++;
				str = labelPositionMap.get(ival) ;
				sp = getItemByName(str);
				
				if (sp!=null){ 
					if (rv < sp.selectionProbability){

						if (sp.maxSelectionCount>=1){
							if (sp.selectionCount>sp.maxSelectionCount){
								// sp.selectionProbability = sp.selectionProbability*0.9; 
							}
							
						}
						
						// rB=hb;
					}else{
						if (sp.selectionProbability<minProb)minProb=sp.selectionProbability;
						if (sp.selectionProbability>supinfProb)supinfProb=sp.selectionProbability;
						pp++;
						sp.postponeCount++;
						// all of this set ?
						// rB=false;
						
						lastFailingPositions.add(pos) ;
					}
				}else{
					rB=true;
				}
				 
			}// ->
			
			if (lastFailingPositions.size()==0){
				rB=true;
			}
			if ((rv<supinfProb) && (lastFailingPositions.size()>0)) {
				pp=0; rB=true;
				lastFailingPositions.clear();
			}else{
				
			}
			
			if (pp>0){
				rB=false;
			}
			 
			 
			return rB;
		}

		
		public void add(SelectionProperty sp) {
			spItems.add(sp) ;
		}

		public SelectionProperty get(int index) {
			return spItems.get(index);
		}
		
		public SelectionProperty getItemByName(String item) {
			SelectionProperty sp=null;
			for (int i=0;i<spItems.size();i++){
				if (spItems.get(i).name.contentEquals(item)){
					sp = spItems.get(i);
					break;
				}
			}
			return sp;
		}
		
		
		public void clear(){
			spItems.clear() ;
		}

		

		
		
	}
	
	
	class SelectionProperty{
		
		String name = "" ;
		
		int maxSelectionCount = -1;
		
		/** only meaningful if repeats are allowed (default: not allowed) */
		int selectionCount=0; 
		/** only meaningful if probability < 1 and random selection is active */
		int postponeCount=0;
		
		double selectionProbability = 1.0 ;
		
		// --------------------------------------------------------------------
		public SelectionProperty(){
			
		}
		// --------------------------------------------------------------------
		
		/**
		 * @return the selectionCount
		 */
		public int getSelectionCount() {
			return selectionCount;
		}

		/**
		 * @param selectionCount the selectionCount to set
		 */
		public void setSelectionCount(int selectionCount) {
			this.selectionCount = selectionCount;
		}

		/**
		 * @return the postponeCount
		 */
		public int getPostponeCount() {
			return postponeCount;
		}

		/**
		 * @param postponeCount the postponeCount to set
		 */
		public void setPostponeCount(int postponeCount) {
			this.postponeCount = postponeCount;
		}

		/**
		 * @return the selectionProbability
		 */
		public double getSelectionProbability() {
			return selectionProbability;
		}

		/**
		 * @param selectionProbability the selectionProbability to set
		 */
		public void setSelectionProbability(double selectionProbability) {
			this.selectionProbability = selectionProbability;
		}
		
		
	}
	/*
	private void _test(){
		
		
		for (int i = 0; i < 8; i++){
			char chr = (char) (i + 'A');
			String str = Character.toString(chr);
			items.add(str);
		}
		generate();
		
	}
	*/
	public static void main(String[] args) {
		new PowerSetSpringSource();
	}
}



class PowerSet<E> implements Iterator<Set<E>>, Iterable<Set<E>> {
	
	private E[] arr = null;
	private BitSet bset = null;
	int index;
	int preferredLimit=-1;
	Random random;
	private ArrayList<Set<E>> knownSets = new ArrayList<Set<E>>();
	
	
	@SuppressWarnings("unchecked")
	public PowerSet(Set<E> set) {
		arr = (E[]) set.toArray();
		bset = new BitSet(arr.length + 1);
		// preferredLimit = preferredlimit; , int preferredlimit
	}

	public Set<E> getNext(int z) {
		index = z;
		
		return next();
	}

	@Override
	public boolean hasNext(){
		return !bset.get(arr.length);
	}

	@Override
	public Set<E> next() {
		
		Set<E> returnSet = new TreeSet<E>();
		double t=0.8;
		int k=0;
		// this could be optimized by applying the size constraints (esp. max length) already here
		// for large number of variables we need not to create the whole set (which will be astronomically high
		
		k=0;
		for (int i = 0; i < arr.length; i++) {
			if (bset.get(i)){
				returnSet.add(arr[i]);
			}
			/*
			 if ((preferredLimit>1) && (returnSet.size()>preferredLimit)){
			 
				t = t*0.96;
				double p = random.nextDouble() ;
				if (p>t){
					
					if (setIsKnown(returnSet)==false){
						knownSets.add(returnSet);
						break;
					}
				}
			}
			*/
		}
		// increment bset
		for (int i = 0; i < bset.size(); i++) {
			if (!bset.get(i)) {
				bset.set(i);
				break;
			} else
				bset.clear(i);
		}

		
		return returnSet;
	}

	private boolean setIsKnown(Set<E> checkedSet) {
		boolean rB=false;
		for ( Set<E> s: knownSets){
			if ( (s.containsAll(checkedSet)) && (checkedSet.containsAll(s))){
				rB=true;
				break;
			}
		}
		return rB;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not Supported!");
	}

	@Override
	public Iterator<Set<E>> iterator() {
		return this;
	}

	/**
	 * @param random the random to set
	 */
	public void setRandom(Random random) {
		this.random = random;
	}
}
