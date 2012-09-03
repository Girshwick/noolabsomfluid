package org.NooLab.somfluid.lattice.cache;


/**
 * 
 * 
 * this takes a given lattice and creates a much smaller one, which
 * operates on the same data as a pre-fetch mechanism for finding the
 * best-matching unit
 * 
 * Such, a hierarchical structure is derived, which we may conceive as a
 * vertical parallelization.
 * Using many processors for screening the nodes would be a horizontal parallelization
 * Of course, the two multiply!
 * 
 * This particularly pays if the node count grows beyond 5'000+
 * 
 * The size of the pre-fetch som is : 6 + SQRT() * LN(LN())
 * 
   The scaling of the nodes is approximately this one:		

						main map		pre-fetch map		  optimistic    worst  
   target 	SQRT	LN(LN())			effective size	     acceleration   case
    size                                                       pop size 
 
	100 	10.00	1.53	15.27		10	100		9	81		1.23		1.09
	200 	14.14	1.67	23.58		14	196		10	100		1.96		1.67
	300 	17.32	1.74	30.16		17	289		11	121		2.39		2.03
	400 	20.00	1.79	35.81		20	400		11	121		3.31		2.65  
	500 	22.36	1.83	40.85		22	484		12	144		3.36		2.78
	1000	31.62	1.93	61.12		31	961		13	169		5.69		4.36  
	1500	38.73	1.99	77.06		38	1444	14	196		7.37		5.5  
	2000	44.72	2.03	90.71		44	1936	15	225		8.60		6.4
	2500	50.00	2.06	102.86		50	2500	16	256		9.77		7.27  
	3000	54.77	2.08	113.94		54	2916	16	256		11.39		8.1  
	10000	100.00	2.22	222.03		100	10000	20	400		25.00		16.0  
	20000	141.42	2.29	324.26		141	19881	24	576		34.52		22.4  
	50000	223.61	2.38	532.49		223	49729	29	841		59.13		36.2  
	100000	316.23	2.44	772.69		316	99856	33	1089	91.70    	52.2

	in other words, for node count > 1000 it is more effective than horizontal parallelization
	
 *
 *  The pre-fetch could work in a reverse organization if it gets embedded into
 *  a given grid. In this case, it is a "post-fetch", less contributing to speed, but
 *  to differentiation power
 *  
 *  Embedding into a given grid means that the original layer turns into some kind of
 *  abstraction
 *
 *
 */
public class PrefetchLattice {

}
