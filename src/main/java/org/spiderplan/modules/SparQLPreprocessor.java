/*******************************************************************************
 * Copyright (c) 2015 Uwe Köckemann <uwe.kockemann@oru.se>
 *  
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.spiderplan.modules;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.ontology.SparQLQuery;
import org.spiderplan.representation.expressions.programs.IncludedProgram;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.GenericComboIterator;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.stopWatch.StopWatch;


/**
 * Pre-processes SparQL queries by replacing operators with
 * copies that satisfy the queries (i.e., all query variables
 * are replaced with query results).
 * 
 * TODO: Write support function to run any query and return a list of substitutions
 * as a result.
 * 
 * TODO: Pre-process ICs
 *  
 * @author Uwe Köckemann
 */
public class SparQLPreprocessor extends Module {
		
	private boolean preprocessICs = true;
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public SparQLPreprocessor( String name, ConfigurationManager cM ) {
		super(name, cM);
	
		super.parameterDesc.add( new ParameterDescription("preprocessICs", "boolean", "true", "If true interaction constraints are preprocessed and replaced by logical consistent ones.") );
							
		if ( cM.hasAttribute(name, "preprocessICs") ) {
			preprocessICs = cM.getBoolean(name, "preprocessICs");
		}
	}
	
	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
		

 		
		Set<Term> queryIDs = new HashSet<Term>();
		Map<Term,StringBuilder> queries = new HashMap<Term,StringBuilder>(); 
		
		Set<Term> modelIDs = new HashSet<Term>();
		Map<Term,StringBuilder> models = new HashMap<Term,StringBuilder>();
		
		if ( verbose ) {
			Logger.msg(getName(), "Background knowledge (asserted Prolog constraint): ", 2);
		}
		for ( SparQLQuery q : core.getContext().get(SparQLQuery.class)) {
			if ( !queryIDs.contains(q.getQueryID())) {
				queryIDs.add(q.getQueryID());
				queries.put(q.getQueryID(), new StringBuilder());
			}
			if ( !modelIDs.contains(q.getModelID())) {
				modelIDs.add(q.getModelID());
				models.put(q.getModelID(), new StringBuilder());
			}
		}
		
		/**
		 * Add background knowledge that is used:
		 */
		for ( IncludedProgram included : core.getContext().get( IncludedProgram.class ) ) {
			Term programID = included.getName();
			if ( queryIDs.contains(programID)) {
				queries.get(programID).append(included.getCode());
			}
			if ( modelIDs.contains(programID)) {
				models.get(programID).append(included.getCode());
			}
		}
		
		int numBefore = core.getOperators().size();
		
		
		
//		for ( Term programID : queryIDs ) {
		if ( keepTimes ) StopWatch.start(msg("Preprocessing operators"));
		this.saturateOperatorQueries( core.getOperators(), queries, models, core.getTypeManager() );
		if ( keepTimes ) StopWatch.stop(msg("Preprocessing operators"));
//		}

		
		System.out.println("Operators before: " + numBefore + " after: " + core.getOperators().size());
		if ( verbose ) {
			Logger.msg(getName(), "Operators before: " + numBefore + " after: " + core.getOperators().size(), 1);
			for ( Operator o : core.getOperators() ) {
				Logger.msg(getName(), "    - " + o.getName(), 2);
				System.out.println(o.getName());
			}
		}
				
		if ( preprocessICs ) {
			//TODO: Implement
//			if ( verbose ) numBefore = core.getContext().get(InteractionConstraint.class).size();
//			if ( keepTimes ) StopWatch.start(msg("Preprocessing interaction constraints"));
//			for ( Term programID : programIDs ) {
//				yappy.saturateInteractionConstraints(core.getContext(), conCollection.get(programID), programID, core.getTypeManager());
//			}
//			if ( keepTimes ) StopWatch.stop(msg("Preprocessing interaction constraints"));
//			if ( verbose ) {
//				Logger.msg(getName(), "ICs before: " + numBefore + " after: " + core.getContext().get(InteractionConstraint.class).size(), 1);
//				for ( InteractionConstraint ic : core.getContext().get(InteractionConstraint.class) ) {
//					Logger.msg(getName(), "    - " + ic.getName(), 2);
//				}
//			}
		}
		
		
		/**
		 * TODO: change this...
		 */
		Global.initialContext = core.getContext().copy();
		
		/**
		 * Possible substitutions don't arrive here for some reason
		 */
		if ( verbose ) Logger.depth--;
		return core;
	}
	
	private void saturateOperatorQueries( Collection<Operator> O, Map<Term,StringBuilder> queries, Map<Term,StringBuilder> models, TypeManager tM ) {
		List<Operator> addList = new ArrayList<Operator>();
		List<Operator> remList = new ArrayList<Operator>();
		
		for ( Operator o : O ) {
			List<List<Substitution>> opSubs = new ArrayList<List<Substitution>>();
			for ( SparQLQuery sQuery : o.getConstraints().get(SparQLQuery.class) ) {		
				String modelStr = models.get(sQuery.getModelID()).toString();
				String queryStr = queries.get(sQuery.getQueryID()).toString();
				
				Model model = ModelFactory.createDefaultModel();					
		        model.read(new ByteArrayInputStream(modelStr.getBytes()), null);
				
				List <QuerySolution> resultList;
				Query query = QueryFactory.create(queryStr);
				QueryExecution qexec = QueryExecutionFactory.create(query, model);

				List<Substitution> solutionSubs = new ArrayList<Substitution>();
				try {	
					ResultSet resultSet = qexec.execSelect();	
					resultList = ResultSetFormatter.toList(resultSet);
					
					for ( QuerySolution sol : resultList ) {
						System.out.println(sol);
						Substitution solutionSub = new Substitution();
						for ( Term arg : sQuery.getVariables() ) {
//							Term value = Term.createConstant(sol.getResource(arg.toString()).toString());
							Term value = Term.createConstant(sol.getLiteral(arg.toString()).toString());
							solutionSub.add(arg, value);
						}
						solutionSubs.add(solutionSub);
					}
					
				} catch(Exception e) {
					System.err.println(e);
					resultList = null; 
				} finally{ qexec.close();	}
				
				opSubs.add(solutionSubs);
			}
			
			if ( !opSubs.isEmpty() ) {
				remList.add(o);
				GenericComboIterator<Substitution> comboBuilder = new GenericComboIterator<>(opSubs);
				
				for ( List<Substitution> combo : comboBuilder ) {
					boolean goodSub = true;
					Substitution combinedSub = new Substitution();
					for ( Substitution sub : combo ) {
						if ( !combinedSub.add(sub) ) {
							goodSub = false;
							break;
						}				
					}
					if ( goodSub ) {
						Operator oCopy = o.copy();
						oCopy.substitute(combinedSub);
						addList.add(oCopy);
					}
				}
			}
		}
		
		O.removeAll(remList);
		O.addAll(addList);
	}
	

}

