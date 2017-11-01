/*******************************************************************************
 * Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.spiderplan.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.ontology.OntologyAdapter;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.ExpressionTypes.OntologyRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interaction.InteractionConstraint;
import org.spiderplan.representation.expressions.ontology.OntologyExpression;
import org.spiderplan.representation.expressions.programs.IncludedProgram;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.logging.Logger;


/**
 * Pre-processes ontology queries by replacing operators with
 * copies that satisfy the queries (i.e., all query variables
 * are replaced with query results).
 * 
 * TODO: Write support function to run any query and return a list of substitutions
 * as a result.
 * 
 *  
 * @author Uwe Köckemann
 */
public class OntologyPreprocessor extends Module {
		
	private boolean preprocessICs = true;
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public OntologyPreprocessor( String name, ConfigurationManager cM ) {
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
		
		TypeManager tM = core.getTypeManager();
		

		
//		for ( OntologyExpression oE : core.getContext().get(OntologyExpression.class)) {
//			if (  oE.getRelation().equals(OntologyRelation.Prefix) ) {
//
//			} else if ( oE.getRelation().equals(OntologyRelation.Triple) ) {
//
//			}
//		}
		
		if ( verbose ) {
			Logger.msg(getName(), "Loading programs...", 1);
			Logger.depth++;
		}
		Map<Term,IncludedProgram> programLookup = new HashMap<Term, IncludedProgram>();
		for ( IncludedProgram iP : core.getContext().get(IncludedProgram.class) ) {
			programLookup.put(iP.getName(), iP);
			if ( verbose ) Logger.msg(getName(), String.format("Name: %s Path: %s", iP.getName().toString(), iP.getPath()), 1);
		}
		if ( verbose ) Logger.depth--;
		
		if ( verbose ) {
			Logger.msg(getName(), "Going through operators...", 1);
			Logger.depth++;
		}
		
		List<Operator> Onew = new ArrayList<Operator>();
		
		for ( Operator o : core.getOperators() ) {
			if ( verbose ) Logger.msg(getName(), String.format("%s", o.getName().toString()), 1);
			Map<Term,List<OntologyExpression>> tripleLookup = new HashMap<Term, List<OntologyExpression>>();
			Map<Term,List<OntologyExpression>> notLookup = new HashMap<Term, List<OntologyExpression>>();
			Map<Term,List<OntologyExpression>> prefixLookup = new HashMap<Term, List<OntologyExpression>>();
			ConstraintDatabase input = new ConstraintDatabase();
			input.add(o);
			
			for ( OntologyExpression oE : o.getConstraints().get(OntologyExpression.class)) {
				if ( !tripleLookup.containsKey(oE.getOntologyID()) ) {
					List<OntologyExpression> prefixes = new ArrayList<OntologyExpression>();
					List<OntologyExpression> triples = new ArrayList<OntologyExpression>();
					List<OntologyExpression> not = new ArrayList<OntologyExpression>();
					prefixLookup.put(oE.getOntologyID(), prefixes);
					tripleLookup.put(oE.getOntologyID(), triples);
					notLookup.put(oE.getOntologyID(), not);
				}
				
				if (  oE.getRelation().equals(OntologyRelation.Prefix) ) {
					prefixLookup.get(oE.getOntologyID()).add(oE);
				} else if ( oE.getRelation().equals(OntologyRelation.Triple) ) {
					tripleLookup.get(oE.getOntologyID()).add(oE);
				} else if ( oE.getRelation().equals(OntologyRelation.Not) ) {
					notLookup.get(oE.getOntologyID()).add(oE);
				}
			}
			
			for ( OntologyExpression oE : core.getContext().get(OntologyExpression.class) ) {
				if ( tripleLookup.containsKey(oE.getOntologyID())) {
					if (  oE.getRelation().equals(OntologyRelation.Prefix) ) {
						prefixLookup.get(oE.getOntologyID()).add(oE);
					} 
				}
			}
			
			if ( verbose ) {
				Logger.msg(getName(), String.format("Running queries..."), 1);
				Logger.depth++;
			}
			int numOpBefore = core.getOperators().size();
			for ( Term ontologyID : tripleLookup.keySet() ) {
				String ontologyPath = programLookup.get(ontologyID).getPath();
				List<OntologyExpression> prefixes = prefixLookup.get(ontologyID);
				List<OntologyExpression> triples = tripleLookup.get(ontologyID);
				List<OntologyExpression> nots = notLookup.get(ontologyID);
				
				List<Substitution> answer = OntologyAdapter.query(ontologyPath, prefixes, triples, nots);
				
				if ( verbose ) {
					Logger.msg(getName(), String.format("Ontology ID: %s", ontologyID), 1);
					Logger.depth++;
					
					for ( OntologyExpression pre : prefixes ) {
						Logger.msg(getName(), String.format("%s", pre.toString()), 1);
					}
					for ( OntologyExpression trip : triples ) {
						Logger.msg(getName(), String.format("%s", trip.toString()), 1);
					}
					
					Logger.depth--;
					Logger.msg(getName(), String.format("Answers:"), 1);
					Logger.depth++;
					for ( Substitution  sub : answer ) {
						Logger.msg(getName(), String.format("%s", sub.toString()), 1);
					}
					Logger.depth--;
				}
				
				if ( verbose ) {
					Logger.msg(getName(), String.format("Base: %s", o.getName().toString()), 3);
					Logger.depth++;
				}
				
				for ( Substitution sub : answer ) {
					Operator oCopy = o.copy();
					oCopy = (Operator)oCopy.substitute(sub);
					Onew.add(oCopy);
					if ( verbose ) Logger.msg(getName(), String.format("Created: %s", oCopy.getName().toString()), 3);
					
				}
				if ( verbose ) {
					Logger.depth--;
				}
			}
			
			if ( verbose ) Logger.depth--;
			core.setOperators(Onew);
			
			int numOpAfter = core.getOperators().size();
			if ( verbose ) {
				Logger.msg(getName(), String.format("Done. Generated %d operators (from %d).", numOpAfter, numOpBefore), 1);
			}
			
			//ConstraintDatabase expansion = this.queryAndExpand(input, prefixes, triples, tM);
		}
		if ( verbose ) Logger.depth--;
										
		if ( preprocessICs ) {
			if ( verbose ) {
				Logger.msg(getName(), "Going through interaction constraints...", 1);
				Logger.depth++;
			}
			
			List<InteractionConstraint> ICnew = new ArrayList<InteractionConstraint>();
			
			for ( InteractionConstraint ic : core.getContext().get(InteractionConstraint.class) ) {
				if ( verbose ) Logger.msg(getName(), String.format("%s", ic.getName().toString()), 1);
				Map<Term,List<OntologyExpression>> tripleLookup = new HashMap<Term, List<OntologyExpression>>();
				Map<Term,List<OntologyExpression>> notLookup = new HashMap<Term, List<OntologyExpression>>();
				Map<Term,List<OntologyExpression>> prefixLookup = new HashMap<Term, List<OntologyExpression>>();
				ConstraintDatabase input = new ConstraintDatabase();
				input.add(ic);
				
				for ( OntologyExpression oE : ic.getCondition().get(OntologyExpression.class)) {
					if ( !tripleLookup.containsKey(oE.getOntologyID()) ) {
						List<OntologyExpression> prefixes = new ArrayList<OntologyExpression>();
						List<OntologyExpression> triples = new ArrayList<OntologyExpression>();
						List<OntologyExpression> nots = new ArrayList<OntologyExpression>();
						prefixLookup.put(oE.getOntologyID(), prefixes);
						tripleLookup.put(oE.getOntologyID(), triples);
						notLookup.put(oE.getOntologyID(), nots);
					}
					
					if (  oE.getRelation().equals(OntologyRelation.Prefix) ) {
						prefixLookup.get(oE.getOntologyID()).add(oE);
					} else if ( oE.getRelation().equals(OntologyRelation.Triple) ) {
						tripleLookup.get(oE.getOntologyID()).add(oE);
					} else if ( oE.getRelation().equals(OntologyRelation.Not) ) {
						notLookup.get(oE.getOntologyID()).add(oE);
					}
				}
				
				for ( OntologyExpression oE : core.getContext().get(OntologyExpression.class) ) {
					if ( tripleLookup.containsKey(oE.getOntologyID())) {
						if (  oE.getRelation().equals(OntologyRelation.Prefix) ) {
							prefixLookup.get(oE.getOntologyID()).add(oE);
						} 
					}
				}
				
				if ( verbose ) {
					Logger.msg(getName(), String.format("Running queries..."), 1);
					Logger.depth++;
				}
				int numICsBefore = core.getContext().get(InteractionConstraint.class).size();
				for ( Term ontologyID : tripleLookup.keySet() ) {
					String ontologyPath = programLookup.get(ontologyID).getPath();
					List<OntologyExpression> prefixes = prefixLookup.get(ontologyID);
					List<OntologyExpression> triples = tripleLookup.get(ontologyID);
					List<OntologyExpression> nots = notLookup.get(ontologyID);
					
					List<Substitution> answer = OntologyAdapter.query(ontologyPath, prefixes, triples, nots);
					
					if ( verbose ) {
						Logger.msg(getName(), String.format("Ontology ID: %s", ontologyID), 1);
						Logger.depth++;
						
						for ( OntologyExpression pre : prefixes ) {
							Logger.msg(getName(), String.format("%s", pre.toString()), 1);
						}
						for ( OntologyExpression trip : triples ) {
							Logger.msg(getName(), String.format("%s", trip.toString()), 1);
						}
						
						Logger.depth--;
						Logger.msg(getName(), String.format("Answers:"), 1);
						Logger.depth++;
						for ( Substitution  sub : answer ) {
							Logger.msg(getName(), String.format("%s", sub.toString()), 1);
						}
						Logger.depth--;
					}
					
					if ( verbose ) {
						Logger.msg(getName(), String.format("Base: %s", ic.getName().toString()), 3);
						Logger.depth++;
					}
					
					for ( Substitution sub : answer ) {
						InteractionConstraint icCopy = ic.copy();
						icCopy = (InteractionConstraint)icCopy.substitute(sub);
						ICnew.add(icCopy);
						if ( verbose ) Logger.msg(getName(), String.format("Created: %s", icCopy.getName().toString()), 3);
						
					}
					if ( verbose ) {
						Logger.depth--;
					}
				}
				
				if ( verbose ) Logger.depth--;
				core.getContext().removeType(InteractionConstraint.class);
				core.getContext().addAll(ICnew);
				
				int numICsAfter = core.getContext().get(InteractionConstraint.class).size();
				if ( verbose ) {
					Logger.msg(getName(), String.format("Done. Generated %d ICs (from %d).", numICsAfter, numICsBefore), 1);
				}
				
				//ConstraintDatabase expansion = this.queryAndExpand(input, prefixes, triples, tM);
			}
			if ( verbose ) Logger.depth--;
			
		}
		
	
		/**
		 * Possible substitutions don't arrive here for some reason
		 */
		if ( verbose ) Logger.depth--;
		return core;
	}
	
	
	/**
	 * 1) Assemble query
	 * 2) Execute query
	 * 3) For each substitution copy set C perform substitution and add result to returned CDB
	 * @param CDB Constraint database to be substituted and copied for each solution
	 * @param prefixes prefixes used by the query
	 * @param triples the query itself
	 * @param tM type manager to check if solutions use correct domains
	 * @return A constraint database expanded with all the query answers 
	 */
	private ConstraintDatabase queryAndExpand( ConstraintDatabase CDB, List<OntologyExpression> prefixes, List<OntologyExpression> triples, TypeManager tM ) {
		ConstraintDatabase r = new ConstraintDatabase();
		
		/**
		 * Put together query, execute it and add copies of C to results.
		 */
		List<Substitution> subs; 
		
//		for ( Substitution sub : subs ) {
//			ConstraintDatabase cdbCopy = CDB.copy();
//			cdbCopy.substitute(sub);
//			r.add(cdbCopy);
//		}

		
		return r;
	}
}

