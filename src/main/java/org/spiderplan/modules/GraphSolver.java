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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.AbstractTypedGraph;
import org.spiderplan.minizinc.MiniZincAdapter;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverCombination;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes.GraphRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.graph.GraphConstraint;
import org.spiderplan.representation.graph.DirectedGraph;
import org.spiderplan.representation.graph.UndirectedGraph;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.visulization.GraphFrame;

/**
 * Handles {@link Expression}s of type {@link GraphConstraint}.
 * 
 * Does not yet support backtracking over its decisions.
 * This will become much easier after a general overhaul of the way
 * conflicts are resolved.
 * 
 * @author Uwe Köckemann
 *
 */
public class GraphSolver extends Module implements SolverInterface { 
	
	private ResolverIterator resolverIterator = null;
	private ConstraintDatabase originalContext = null;
	
	String minizincBinaryLocation = "minizinc"; 
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public GraphSolver(String name, ConfigurationManager cM) {
		super(name, cM);
		
		
		super.parameterDesc.add( new ParameterDescription("binaryLocation", "string", "minizinc", "Set minizink binary location.") );
		
		if ( cM.hasAttribute(name, "binaryLocation")  ) {
			minizincBinaryLocation = cM.getString(this.name, "binaryLocation");
		}
	}

	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
				
		if ( core.getInSignals().contains("FromScratch") ) {
			Logger.msg(getName(),"Running FromScratch", 0);
			core.getInSignals().remove("FromScratch");
			resolverIterator = null;
		}
		
		boolean isConsistent = true;
		
		if ( resolverIterator == null ) {
			SolverResult result = testAndResolve(core);
			originalContext = core.getContext().copy();
			if ( result.getState().equals(State.Searching) ) {
				resolverIterator = result.getResolverIterator();
			} else if (  result.getState().equals(State.Inconsistent)  ) {
				isConsistent = false;
			}
		} 
			
		if ( isConsistent ) {
			if ( resolverIterator == null ) {
				core.setResultingState( getName(), State.Consistent );
			} else {
				Resolver r = resolverIterator.next();
				if ( r == null ) {
					core.setResultingState( getName(), State.Inconsistent );
				} else {
					ConstraintDatabase cDB = originalContext.copy();
					r.apply(cDB);
					core.setContext(cDB);
					core.setResultingState(getName(), State.Consistent);
				}
			}
		} else {
			core.setResultingState( getName(), State.Inconsistent );
		} 
		
		if ( verbose ) Logger.depth--;
		return core;
	}

	@Override
	public SolverResult testAndResolve(Core core) {
		
		boolean isConsistent = true;
		
		Collection<GraphConstraint> C = core.getContext().get(GraphConstraint.class);
		
		Collection<Term> directedGraphs = new HashSet<Term>();
		Collection<Term> undirectedGraphs = new HashSet<Term>();
		
		HashMap<Term,AbstractGraph<Term,String>> graphs = new HashMap<Term,AbstractGraph<Term,String>>();
		HashMap<Term,Integer> edgeIDcounter = new HashMap<Term, Integer>();
		
		HashMap<Term,HashMap<Term,Integer>> varCapacities = new HashMap<Term, HashMap<Term,Integer>>();
		
		List<List<Resolver>> resolverLists = new ArrayList<List<Resolver>>();
		
		/**
		 * Create Graphs
		 */
		Term r;
		if ( verbose ) Logger.msg(getName(), "Creating graphs...", 1);
		for ( GraphConstraint gC : C ) {
			r = gC.getConstraint();
			if ( gC.getRelation().equals(GraphRelation.Directed) ) { //r.getUniqueName().equals("directed/1") ) {
				if ( verbose ) Logger.msg(getName(), "    " + gC, 2);
				directedGraphs.add(r.getArg(0));
				graphs.put(r.getArg(0), new DirectedGraph<Term, String>());
				edgeIDcounter.put(r.getArg(0), 0);
				varCapacities.put(r.getArg(0), new HashMap<Term, Integer>());
			}  else if ( gC.getRelation().equals(GraphRelation.Undirected) ) {
				if ( verbose ) Logger.msg(getName(), "    " + gC, 2);
				undirectedGraphs.add(r.getArg(0));
				graphs.put(r.getArg(0), new UndirectedGraph<Term, String>());
				edgeIDcounter.put(r.getArg(0), 0);
				varCapacities.put(r.getArg(0), new HashMap<Term, Integer>());
			} else if ( gC.getRelation().equals(GraphRelation.Capacity) ) {
				if ( verbose ) Logger.msg(getName(), "    " + gC, 2);
				varCapacities.get(r.getArg(0)).put(r.getArg(1), Integer.valueOf(r.getArg(2).toString()));
			}
		}
		/**
		 * Add edges and vertices
		 */
		for ( GraphConstraint gC : C ) {
			r = gC.getConstraint();
			Term graph = r.getArg(0);
			AbstractGraph<Term, String> G;
			
			G = graphs.get(graph);
			if ( G == null ) {
				throw new IllegalStateException("Graph " + graph + " not declared as a graph: Use (directed "+ graph + ") or (undirected " + graph + ") to fix this.");
			}
			
			if ( gC.getRelation().equals(GraphRelation.Vertex) ) {
				if ( verbose ) Logger.msg(getName(), "    Adding: " + gC, 2);
				G.addVertex(r.getArg(1));
			}  else if (gC.getRelation().equals(GraphRelation.Edge) ) {
				if ( verbose ) Logger.msg(getName(), "    Adding: " + gC, 2);
				int edgeID = edgeIDcounter.get(graph).intValue();
				G.addEdge("["+edgeID+"] " + r.getArg(3).toString(), r.getArg(1), r.getArg(2));
				edgeIDcounter.put(graph, Integer.valueOf(edgeID+1));
			}
		}
		
		for ( GraphConstraint gC : C ) {
			if ( gC.getRelation().equals(GraphRelation.Draw) ) {
				
				AbstractTypedGraph<Term, String> G = (AbstractTypedGraph<Term, String>)graphs.get(gC.getConstraint().getArg(0));
				
				Map<String,String> edgeLabels = new HashMap<String, String>();
				 
				for ( String edge : G.getEdges() ) {
					edgeLabels.put(edge,edge.split("] ")[1]); // Remove edge ID "[x] " from edge name
				}
				
				new GraphFrame<Term,String>(G, null,  gC.getConstraint().getArg(0).toString(), GraphFrame.LayoutClass.FR, edgeLabels);
			}	
		}
		
		/**
		 * Check if all graphs fulfill their constraints
		 */
		if ( verbose ) Logger.msg(getName(), "Testing constraints..." , 1);
		for ( GraphConstraint gC : C ) {
			if ( !isConsistent ) {
				break;
			}
		
			r = gC.getConstraint();

			Term graph = r.getArg(0);
			if ( gC.getRelation().equals(GraphRelation.HasEdge)) {
				Term V_from = r.getArg(1);
				Term V_to = r.getArg(2);
				Term label = r.getArg(3);
				
				AbstractGraph<Term, String> G = graphs.get(graph);
				
				boolean foundEdge = false;

				if ( G.getOutEdges(V_from) != null ) {	
					for ( String edge : G.getOutEdges(V_from) ) {
						if ( G.getEndpoints(edge).getSecond().equals(V_to) ) {
							Term edgeLabel = Term.parse(edge.split("] ")[1]);
							if ( edgeLabel.equals(label)) {
								foundEdge = true;
								break;
							}
						}
					}
				}
				
				if ( !foundEdge ) {
					isConsistent = false;
				}
				
			} else if ( gC.getRelation().equals(GraphRelation.Path) ) {
				if ( verbose ) Logger.msg(getName(), "    Checking: " + gC, 1);
				AbstractGraph<Term, String> G = graphs.get(graph);
				
				Term V_from = r.getArg(1);
				Term V_to = r.getArg(2);
				HashSet<Term> connected = new HashSet<Term>();
				connected.add(V_from);
				int connectedSizeBefore = -1;
				while ( connectedSizeBefore != connected.size() ) {
					connectedSizeBefore = connected.size();
					for ( Term V_prime : connected ) {
						connected.addAll(G.getSuccessors(V_prime));
					}
					if ( connected.contains(V_to) ) {
						break;
					}
				}
				if ( !connected.contains(V_to) ) {
					if ( verbose ) Logger.msg(getName(), "        Unsatisfiable.", 1);
					isConsistent = false;
				} else {
					if ( verbose ) Logger.msg(getName(), "        Found path.", 1);
				}				
			} else if ( gC.getRelation().equals(GraphRelation.ShortestPath) ) { //gC.getConstraint().getUniqueName().equals("shortest-path/5") ) { 
				/**
				 * TODO: edge representation is bad, what do to with non-ground edges? get path
				 */
				if ( verbose ) Logger.msg(getName(), "    Checking: " + gC, 1);
				
				Term pathVar = gC.getConstraint().getArg(3);
				Term costVar = gC.getConstraint().getArg(4);
				
//				if ( !(pathVar.isVariable() && costVar.isVariable()) ) {
//					if ( verbose ) Logger.msg(getName(), "        Unsatisfiable: " + pathVar + " and " + costVar + " need to be variables.", 1);
//					isConsistent = false;
//					continue;
//				}
				
				AbstractGraph<Term, String> G = graphs.get(graph);
				
				Map<Term, Term> spanningTree = new HashMap<Term,Term>();
				
				Map<Term,Integer> cost = new HashMap<Term,Integer>();
								
				for ( Term v : G.getVertices() ) {
					cost.put(v, Integer.MAX_VALUE);
				}
				Term V_from = r.getArg(1);
				cost.put(V_from, 0);
				
				Term V_to = r.getArg(2);
				List<Term> fringe = new ArrayList<Term>();
				fringe.addAll(G.getVertices());

				while ( !fringe.isEmpty() ) {
					int minExitCost = Integer.MAX_VALUE;
					Term argMin = null;
					for ( Term v : fringe ) {
						if ( cost.get(v) < minExitCost ) {
							minExitCost = cost.get(v);
							argMin = v;
						}
					}
					fringe.remove(argMin);
					
					for ( String edge : G.getOutEdges(argMin) ) {
						Term neighbor = G.getOpposite(argMin, edge);
						if ( fringe.contains(neighbor) ) {
							int edgeCost = Integer.valueOf(edge.split("] ")[1]).intValue();
							int newCost = cost.get(argMin)+edgeCost;
							if ( newCost < cost.get(neighbor).intValue() ) {
								cost.put(neighbor, newCost);
								spanningTree.put(neighbor, argMin);
							}
						}
					}
				}
				
				List<Term> path = new ArrayList<Term>();
				Term current = V_to;
				while ( !current.equals(V_from) ) {
					path.add(current);
					current = spanningTree.get(current);
					if ( current == null ) {
						isConsistent = false; 
						break;
					}
				}
				path.add(V_from);
				Collections.reverse(path);
				
				Term pathTerm = Term.createComplex("list", path.toArray(new Term[path.size()]));
				Term costTerm = Term.createInteger(cost.get(V_to).intValue());
				
				if ( !isConsistent ) {
					if ( verbose ) Logger.msg(getName(), "        Unsatisfiable: No path found.", 1);
				} else {
					if ( verbose ) Logger.msg(getName(), "        Found path: " + pathTerm + " with cost " + costTerm, 1);
					Substitution theta = new Substitution();
					if ( costVar.isVariable() ) {
						theta.add(costVar, costTerm);
					} else { 
						if ( !costVar.equals(costVar) ) {
							if ( verbose ) Logger.msg(getName(), "        Unsatisfiable: Cost " + costTerm + " does not match " + costVar + ".", 1);
							isConsistent = false;
							continue;
						}
					}
					if ( pathVar.isVariable() ) {
						theta.add(pathVar, pathTerm);	
					} else { 
						if ( !pathVar.equals(pathVar) ) {
							if ( verbose ) Logger.msg(getName(), "        Unsatisfiable: Path " + pathTerm + " does not match "	 + pathVar + ".", 1);
							isConsistent = false;
							continue;
						}
					}
					
					if ( !theta.isEmpty() ) {
						Resolver res = new Resolver(theta);
						List<Resolver> rList = new ArrayList<Resolver>();
						rList.add(res);
						resolverLists.add(rList);
					}
				}
			} else if ( gC.getRelation().equals(GraphRelation.DAG) ) { 
				if ( verbose ) Logger.msg(getName(), "    Checking: " + gC, 1);
				if ( undirectedGraphs.contains(graph) ) { // An undirected graph cannot be a DAG
					isConsistent = false;
					break;
				} else {
					DirectedGraph<Term, String> G = (DirectedGraph<Term, String>)graphs.get(graph);
					
					for ( Term V : G.getVertices() ) {
						HashSet<Term> connected = new HashSet<Term>();
						connected.add(V);
						int connectedSizeBefore = -1;
						while ( connectedSizeBefore != connected.size() ) {
							HashSet<Term> newVertices = new HashSet<Term>(); 
							connectedSizeBefore = connected.size();
							for ( Term V_prime : connected ) {
								if ( G.getOutEdges(V_prime) != null ) {
									if ( G.getOutEdges(V_prime).contains(V)) {
										isConsistent = false;
										break;
									} 
									newVertices.addAll(G.getSuccessors(V_prime));
									
								}								
							}
							connected.addAll(newVertices);
							if ( !isConsistent ) {
								break;
							}
						}
						if ( !isConsistent ) {
							break;
						}
					}
				}
			} else if ( gC.getRelation().equals(GraphRelation.Flow) ) { 
				if ( verbose ) Logger.msg(getName(), "    Checking: " + gC, 1);
				if ( undirectedGraphs.contains(graph) ) { // An undirected graph cannot be a flow	
					isConsistent = false;					
					break;
				} else {
					DirectedGraph<Term, String> G = (DirectedGraph<Term, String>)graphs.get(graph);
					
					ArrayList<Term> vars = new ArrayList<Term>();
					ArrayList<ArrayList<ArrayList<Term>>> inequalities = new ArrayList<ArrayList<ArrayList<Term>>>();
					
					int maxOutput = 0;
					
//					Term source = new Term("superSourceVertex");
					Set<Term> fringe = new HashSet<Term>();
					
					for ( Term V : G.getVertices() ) {
						if ( G.getInEdges(V).size() == 0 && G.getOutEdges(V).size() > 0  ) {
							fringe.add(V);
						} 
					}
					
					String program = "";
					Set<String> varDefs = new HashSet<String>();
					
					boolean change = true;
					
					while ( change ) {
						change = false;
						
						Set<Term> newV = new HashSet<Term>();
						for ( Term V : fringe ) {
							for ( Term V_prime : G.getSuccessors(V) ) {
								if ( !fringe.contains(V_prime) ) {
									change = true;
									newV.add(V_prime);
									
									if ( G.getInEdges(V_prime).size() > 0 && G.getOutEdges(V_prime).size() > 0 ) {
										
										ArrayList<ArrayList<Term>> inequality = new ArrayList<ArrayList<Term>>(2);
										inequality.add(new ArrayList<Term>());
										inequality.add(new ArrayList<Term>());
										
										int edgeValue;
										Term edgeValueTerm;
										boolean isInteger = false;
										boolean isVariableTerm;

										for ( String edge : G.getInEdges(V_prime) ) {
											String edgeValueStr = edge.split("] ")[1];
											
											edgeValueTerm = null;
											isVariableTerm = false;
											isInteger = false;
											
											try {
												edgeValue = Integer.valueOf(edgeValueStr);
												edgeValueTerm = Term.createInteger(edgeValue);
												isInteger = true;
											} catch ( NumberFormatException e ) { 	
												
											}
											
											if ( !isInteger ) {
												isVariableTerm = true;
												edgeValueTerm = Term.createVariable(edgeValueStr);
											} 
											if ( !isInteger && !isVariableTerm ) {
												throw new IllegalStateException("Term " + edgeValueStr + " of edge " + edge + " into " + V_prime + " is neither integer nor variable term.");
											} else {
												if ( !isInteger ) {
													Integer cap = varCapacities.get(graph).get(edgeValueTerm);
													varDefs.add( "var 0.."+cap.toString()+" : " + MiniZincAdapter.makeMiniZincCompatible(edgeValueTerm) + ";" ); // var 0..600: X_01;
													if ( !vars.contains(edgeValueTerm) ) { 
														vars.add(edgeValueTerm);
													}
												}
												
												inequality.get(0).add( edgeValueTerm );
											}
										}
										int sumOutput = 0;
										for ( String edge : G.getOutEdges(V_prime) ) {											
											String edgeValueStr = edge.split("] ")[1];
											
											edgeValueTerm = null;
											isVariableTerm = false;
											isInteger = false;
											
											try {
												edgeValue = Integer.valueOf(edgeValueStr);
												edgeValueTerm = Term.createInteger(edgeValue);
												sumOutput += edgeValue;
												isInteger = true;
											} catch ( NumberFormatException e ) { 	
												
											}
											
											if ( !isInteger ) {
												isVariableTerm = true;
												edgeValueTerm = Term.createVariable(edgeValueStr);
											} 
											
											if ( !isInteger && !isVariableTerm ) {
												throw new IllegalStateException("Term " + edgeValueStr + " of edge " + edge + " into " + V_prime + " is neither integer nor variable term.");
											} else {
												if ( !isInteger ) {
													Integer cap = varCapacities.get(graph).get(edgeValueTerm);
													varDefs.add( "var 0.."+cap.toString()+" : " + MiniZincAdapter.makeMiniZincCompatible(edgeValueTerm) + ";" ); // var 0..600: X_01;
													if ( !vars.contains(edgeValueTerm) ) { 
														vars.add(edgeValueTerm);
													}
												}
												
												inequality.get(1).add( edgeValueTerm );
											}
										}
										
										if ( sumOutput > maxOutput ) {
											maxOutput = sumOutput;
										}	
										
										String eqStr = "constraint " + MiniZincAdapter.makeMiniZincCompatible(inequality.get(0).get(0));
										for ( int i = 1 ; i < inequality.get(0).size() ; i++ ) {
											eqStr += " + " + MiniZincAdapter.makeMiniZincCompatible(inequality.get(0).get(i)); 
										}
										eqStr += " == " + MiniZincAdapter.makeMiniZincCompatible(inequality.get(1).get(0));
										for ( int i = 1 ; i < inequality.get(1).size() ; i++ ) {
											eqStr += " + " + MiniZincAdapter.makeMiniZincCompatible(inequality.get(1).get(i)); 
										}
										eqStr += ";\n";
										program += eqStr;
										
										
										inequalities.add(inequality);
									}
								}
							}
						}
						fringe.addAll(newV);
					}
					
					if ( vars.isEmpty() ) {
						continue;		// Nothing to do here...
					}
					
					for ( String varDef : varDefs ) {
						program = varDef + "\n" + program;
					}
						program += "\nsolve satisfy;\n\noutput [\n\t\"" + MiniZincAdapter.makeMiniZincCompatible(vars.get(0)) + "/\",show(" + MiniZincAdapter.makeMiniZincCompatible(vars.get(0)) + ")";
					
					
					for ( int i = 1 ; i < vars.size() ; i++ ) {
						program += ",\"," + MiniZincAdapter.makeMiniZincCompatible(vars.get(i)) + "/\",show(" + MiniZincAdapter.makeMiniZincCompatible(vars.get(i)) + ")";
					}
					program += "\n];\n";
					
					
					if ( verbose ) {
						Logger.msg(getName(),"Resulting program:\n"+ program, 4);
					}
					 
					Collection<Substitution> subst = MiniZincAdapter.runMiniZinc(minizincBinaryLocation,program,true);

					if ( subst == null ) {
						if ( verbose ) Logger.msg(getName(),"        Unsatisfiable."+ program, 1);
						isConsistent = false;
						break;
					} else {
						if ( verbose ) Logger.msg(getName(),"        Found flow."+ program, 1);
						List<Resolver> l = new ArrayList<Resolver>();
						for ( Substitution s : subst ) {
							Resolver res = new Resolver(s);
							l.add(res);
						}
						if ( !l.isEmpty() ) {
							resolverLists.add(l);
						}
					}
				}
			}
		}		
		
		
		
		State state;
		ResolverIterator resolverIterator = null;
				
		if ( !resolverLists.isEmpty() ){
			state = State.Searching;
			resolverIterator = new ResolverCombination(resolverLists, this.getName(), this.cM);
		} else if ( isConsistent ) {
			state = State.Consistent;
		} else {
			state = State.Inconsistent;
		}
		return new SolverResult(state,resolverIterator);
	}
}
