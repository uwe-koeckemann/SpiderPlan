package org.spiderplan.causal.pocl;

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

public class OpenGoalResolverIterator extends ResolverIterator {
	
	private POCLSearch search;
	
	private ConstraintDatabase originalContext;
	
	private TypeManager tM;
	
	private boolean firstTime = true;
	
	private String consistencyCheckerName;
	private Module consistencyChecker = null;
	
//	private int prunedNoGoods = 0;

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
		
		search = new POCLSearch(c.getContext(), c.getOperators(), heuristicNames, c.getTypeManager(), name, verbose, verbosity);
		
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
				POCLNode fpn = (POCLNode) search.getCurrentNode();
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

				Resolver planResolver = ((POCLNode)search.getCurrentNode()).getCombinedResolver();	
						
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
			
			r = ((POCLNode)search.getCurrentNode()).getCombinedResolver();
			
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
	
	public void prune() {
		search.setNoGood();	// Meaning: there is no need to expand the current node...
	}

}
