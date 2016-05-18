package org.spiderplan.causal.planSpacePlanning;

import java.util.ArrayList;
import java.util.List;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.statistics.Statistics;
import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Iterator over resolver for all open goals.
 * Resolvers are created by using plan space planning.
 * 
 * @author Uwe Köckemann
 *
 */
public class OpenGoalResolverIterator extends ResolverIterator {
	
	private PlanSpacePlanningSearch search;
	
	private ConstraintDatabase originalContext;
	
	private TypeManager tM;
	
	private boolean firstTime = true;
	
	private String consistencyCheckerName;
	private Module consistencyChecker = null;
	
	/**
	 * Construct iterator.
	 * 
	 * @param c core whose open goals will be resolved
	 * @param name name used by this iterator
	 * @param cM configuration manager
	 */
	public OpenGoalResolverIterator(Core c, String name, ConfigurationManager cM) {
		super(name, cM);
		
		this.tM = c.getTypeManager();
		
		super.parameterDesc.add( new ParameterDescription("heuristics", "string", "CausalGraph", "Comma-seperated list of heuristics (supported: FastDownward).") );
		
		List<String> heuristicNames = new ArrayList<String>();
		
		if ( cM.hasAttribute(name, "heuristics" )) {
			heuristicNames = cM.getStringList(name, "heuristics");
		} else {
			heuristicNames = new ArrayList<String>();
			heuristicNames.add("HAddReuse");
		}
		
		if ( cM.hasAttribute(name, "consistencyChecker") ) {
			this.consistencyCheckerName = cM.getString(this.getName(), "consistencyChecker" );
			this.consistencyChecker = ModuleFactory.initModule( this.consistencyCheckerName, cM );
		}
		
		originalContext = c.getContext().copy();
		
		search = new PlanSpacePlanningSearch(c.getContext(), c.getOperators(), heuristicNames, c.getTypeManager(), name, verbose, verbosity);
		
	}

	@Override
	public Resolver next( ConstraintDatabase C ) {
		if ( ResolverIterator.killFlag ) {
			return null;
		}
			
		Logger.depth++;
//		search.success = false;
				
		/**
		 * If we come back here after the first call 
		 * -> we assume something is wrong with previous solution
		 */
		if ( !firstTime ) {
			if ( verbose ) { 
				print("Last node was no-good...", 1);
				PlanSpacePlanningNode fpn = (PlanSpacePlanningNode) search.getCurrentNode();
				if ( fpn != null ) { 
					if ( verbose ) print("Last plan length: " + fpn.depth(),1);
//					if ( verbose ) print("Last plan: " + fpn.getPlanList(),1);
				}
			}
						
			if ( keepStats ) Statistics.increment("["+getName()+"] NoGoods");
			prune();
			search.continueSearch();
//			search.done = false;
//			search.success = false;	
		} else {
			firstTime = false;
		}

		while ( !search.isDone() ) {
			if ( keepTimes ) StopWatch.start(msg("Stepping"));
			search.step();
			if ( keepTimes ) StopWatch.stop(msg("Stepping"));			
//			if ( verbose ) printIterationInfo();
//			if ( keepStats ) recordStats();
									
			if ( consistencyChecker != null && !(search.getCurrentNode() == null) ) {
				if ( keepTimes ) StopWatch.start(msg("Incremental consistency check"));

				if ( verbose ) print("Testing partial plan...", 0);

				Resolver planResolver = ((PlanSpacePlanningNode)search.getCurrentNode()).getCombinedResolver();	
						
				if ( planResolver == null ) {
					break;
				}
				
				ConstraintDatabase context = originalContext.copy();
				
				planResolver.apply(context);
				
				Core checkCore = new Core();

				checkCore.setContext(context);
//				checkCore.setPlan(p);
				checkCore.setTypeManager(this.tM);
				checkCore = consistencyChecker.run(checkCore);
				
				boolean consistent = checkCore.getResultingState(consistencyCheckerName).equals(Core.State.Consistent);
				
				if ( !consistent ) {				
					if ( verbose ) print("Partial plan inconsistent.", 0);
					this.prune();
					search.continueSearch();
//					if ( search.is )  {
//						search.done = false;
//					}
//					if ( search.success ) {
//						search.success = false;
//					}
				} else {
					if ( verbose ) print("Partial plan consistent.", 0);
				}
				if ( keepTimes ) StopWatch.stop(msg("Incremental consistency check"));
			}
		}
//		Plan p = null;
		Resolver r = null;
		if ( search.isSuccess() ) {
			if ( verbose ) print("Success", 0);						
//			if ( keepStats ) Statistics.setLong(msg("|\\pi|"), Long.valueOf(p.getActions().size())); 
		
//			System.out.println("Getting combined resolver...");
			r = ((PlanSpacePlanningNode)search.getGoalNode()).getCombinedResolver();
			
//			System.out.println("Substitution of combined resolver: " + r.getSubstitution());
			
//			System.out.println(r);
		
//			search.done = false;
			search.continueSearch();
		} else {
			if ( verbose )  print("Fail", 0);
		}
				
		Logger.depth--;
		return r;
	}
	
	/**
	 * Mark current search node as no-good so that it will not be expanded by
	 * the search.
	 */
	public void prune() {
		search.setNoGood();
	}

}
