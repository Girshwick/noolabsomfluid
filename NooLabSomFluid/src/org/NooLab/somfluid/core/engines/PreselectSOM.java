package org.NooLab.somfluid.core.engines;


/**
 * 
 * for large maps, it is impossible to check all node explicitly, even if we maintain
 * a buffer that links records to certain areas;
 *  
 * However, if we do not spread the search (and at least implicit) influence
 * across the at least 70..80% of the map; if we would reduce further, 
 * symmetry breaks could occur, but this is not always a bad thing either!!
 * 
 * Anyway, it will accelerate the learning in case of large maps;
 * 
 * In turn, it will also allow to reduce the number of nodes being 
 * explicitly influenced by the insertion of a record to the BMU, since
 * the large map gets always also primed by the small map (in the sense of "colored noise")
 * Limiting the size of the effective vicinity in the large map also contributes to better speed;
 * 
 * The perception mechanism is as follows:
 * 
 * 
 * 
 * Note that this "growth" is not semantically induced, it is purely syntactical in its
 * intention and its releasing forces, yet, their are of course some (rather weak) semantic
 * consequences.
 * 
 * This class takes the same role as DSom, including its sub-classes, yet, it
 * runs with a resolution of only sqrt("side length") 
 * 
 * The PreselectSom is always taken, if the resulting reduced number of nodes rises
 * above  N = (m) * (count of target groups) * (count of variables),
 * such that always 60<N<200
 * 
 */
public class PreselectSOM {

}
