package org.spiderplan.modules.solvers;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.domain.Substitution;

/**
 * Takes resolver and heuristic value to allow sorting resolvers by 
 * heuristic value.
 * 
 * @author Uwe Köckemann
 *
 */
public class ResolverHeuristic implements Comparable<ResolverHeuristic> {

	Resolver r;
	Long h;
	
	/**
	 * @param r
	 * @param heuristicValue
	 */
	public ResolverHeuristic( Resolver r, Long heuristicValue ) {
		this.r = r;
		this.h = heuristicValue;
	}
	
	/**
	 * Return the resolver.
	 * @return the resolver
	 */
	public Resolver getResolver() { return r; }
	public Long getHeuristicValue() { return h; }
	
	@Override
	public int compareTo(ResolverHeuristic rComp) {
		return (int)(this.h - rComp.h);
	}
	
	@Override
	public String toString() {
		return "Heuristic value: "+h+"\nResolver: " + r;
	}
}
