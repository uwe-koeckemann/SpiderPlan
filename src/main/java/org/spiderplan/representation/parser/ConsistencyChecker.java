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
package org.spiderplan.representation.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.domain.NewObject;
import org.spiderplan.representation.expressions.domain.VariableDomainRestriction;
import org.spiderplan.representation.expressions.graph.GraphConstraint;
import org.spiderplan.representation.expressions.misc.CustomConstraint;
import org.spiderplan.representation.expressions.prolog.PrologConstraint;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.expressions.temporal.SimpleDistanceConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.Type;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.stopWatch.StopWatch;

public class ConsistencyChecker {
	
	public static boolean verbose = true;
	public static boolean keepTimes = false;
	public static boolean ignoreWarnings = false;
	
	private int numChars = 80;
	private int warnings = 0;
	
	public boolean check( Core c ) {
		warnings = 0;
		
		ConstraintDatabase allCons = new ConstraintDatabase();
		//allCons.addAll(c.getConstraints());
		allCons.addAll(c.getContext());
		
		if ( keepTimes ) StopWatch.start("Checking types");
		if ( !checkTypes( c.getTypeManager() ) ) {
			if ( keepTimes ) StopWatch.stop("Checking types");
			return false;
		}
		if ( keepTimes ) StopWatch.stop("Checking types");
		
		if ( keepTimes ) StopWatch.start("Checking type domains");
		if ( !checkTypeDomains( c.getContext(), c.getTypeManager() ) ) {
			if ( keepTimes ) StopWatch.stop("Checking type domains");
			return false;
		}
		if ( keepTimes ) StopWatch.stop("Checking type domains");
		
		if ( keepTimes ) StopWatch.start("Checking operator signatures");
		if ( !checkOperatorSignatures( c.getOperators(), c.getTypeManager() ) ) {
			if ( keepTimes ) StopWatch.stop("Checking operator signatures");
			return false;
		}
		if ( keepTimes ) StopWatch.stop("Checking operator signatures");
		
		if ( keepTimes ) StopWatch.start("Checking operator variables");
		if ( !checkOperatorVariables( c.getOperators() ) ) {
			if ( keepTimes ) StopWatch.stop("Checking operator variables");
			return false;
		} 		
		if ( keepTimes ) StopWatch.stop("Checking operator variables");
		
		if ( keepTimes ) StopWatch.start("Checking operator intervals");
		if ( !checkLabels( c.getOperators() ) ) {
			if ( keepTimes ) StopWatch.stop("Checking operator intervals");
			return false;
		}
		if ( keepTimes ) StopWatch.stop("Checking operator intervals");
		  
//		if ( keepTimes ) StopWatch.start("Checking operator relational consistency");
//		if ( !relationalConsistency( c.getOperators(), c.getContext(), c.getTypeManager() ) ) {
//			if ( keepTimes ) StopWatch.stop("Checking operator relational consistency");
//			return false;			
//		}
//		if ( keepTimes ) StopWatch.stop("Checking operator relational consistency");
		
		if ( keepTimes ) StopWatch.start("Checking operator temporal consistency");
		if ( !operatorTemporalConsistency( c.getOperators(), c.getContext(), c.getTypeManager() ) ) {
			if ( keepTimes ) StopWatch.stop("Checking operator temporal consistency");
			return false;
		}
		if ( keepTimes ) StopWatch.stop("Checking operator temporal consistency");
		
		if ( verbose ) {
			System.out.println("Done! There where no errors and " + warnings + " warnings.");
		}
		return true;
	}
	
	/**
	 * Check if objects are in their domains.
	 * @param tM
	 * @return
	 */
	private boolean checkTypeDomains( ConstraintDatabase init, TypeManager tM ) {
		if ( verbose ) {
			System.out.print(getDottedString("Domain membership",numChars));
		}

		ArrayList<Statement> all = new ArrayList<Statement>();
		all.addAll(init.get(Statement.class));

		for ( Statement s : all ) {
			for ( int i = 0 ; i < s.getVariable().getNumArgs(); i++ ) {
				Term term = s.getVariable().getArg(i);
				if ( term.isGround() ) {
					Type t = tM.getPredicateTypes(s.getVariable().getUniqueName(), i);
	
					if ( t == null ) {
						System.out.println("[FAIL]");
						System.err.println( "ERROR: Unknown type of " + s.getVariable().getUniqueName() + " at " + i );
						return false;
					}	
					
					if ( !t.getClass().getSimpleName().equals("FloatType")  && !t.getDomain().contains(s.getVariable().getArg(i))) {
						System.out.println("[FAIL]");
						System.err.println( "ERROR: Statement: " + s + " has value " + s.getVariable().getArg(i) + " which is not part of domain of type " + t.getName());
						return false;
					}
				}
			}
			
			Type t = tM.getPredicateTypes(s.getVariable().getUniqueName(), -1);
			if ( s.getValue().isGround() && !t.getDomain().contains(s.getValue())) {
				System.out.println("[FAIL]");
				System.err.println( "ERROR: Statement: " + s + " has value " + s.getValue() + " which is not part of domain of type " + t.getName());
				return false;
			}
		}
		
		return true;
	}
		
	/**
	 * Check type names for reserved names that may cause problems
	 * @return
	 */
	private boolean checkTypes( TypeManager tM ) {
		if ( verbose ) {
			System.out.print(getDottedString("Types",numChars));
		}
		
		ArrayList<Term> forbidden = new ArrayList<Term>();
		HashMap<Term,String> reasons = new HashMap<Term,String>();
		
		forbidden.add(Term.createConstant("integer"));		
		reasons.put(Term.createConstant("integer"),"Reserved name in YAP Prolog solver. ");

		
		for ( Term tName : tM.getTypeNames() ) {
			if ( forbidden.contains(tName) ) {
				if ( verbose ) {
					System.out.println("[FAIL]");
					System.err.println( "ERROR: Forbidden type name \""+tName+"\". Reason: " + reasons.get(tName));
				}
				return false;
			}
		}
		if ( verbose ) {
			System.out.println("[OK]");
		}
		return true;
	}
	
	/**
	 * Check operator signatures
	 * @return
	 */
	private boolean checkOperatorSignatures( Collection<Operator> O, TypeManager tM ) {
		if ( verbose ) {
			System.out.println("Operator signatures:");
		}
		
		for ( Operator o : O ) {
			if ( verbose ) {
				System.out.print(getDottedString(o.getName().toString(),numChars));
			}
			Atomic varName = o.getName(); // tM.getVariableFromLookUp(o.getName().name());
			if ( !tM.hasVariable(varName.getUniqueName()) ) {
				if ( verbose ) {
					System.out.println("[FAIL]");
					System.err.println("ERROR: No signature defined for operator.");
					System.err.println("Signatures are used to define types of an operators arguments. There are two ways to do this:");
					System.err.println("(1) Add the signature as a variable with \"var\".\nExample: var Move(agent,location,location);\n");
					System.err.println("(2) Add types directly to the name in the operator definition. \nExample: operator Move(A - agent, L1 - location, L2 - location) = { ...");
				}
				return false;
			} else {
				if ( varName.getNumArgs() != o.getName().getNumArgs() ) {
					if ( verbose ) {
						System.out.println("[FAIL]");
						System.err.println("ERROR: Found signature of operator, but it has a different arity.");
					}
					return false;
				}
			}
			if ( verbose ) {
				System.out.println("[OK]");
			}
		}
		
		return true;
	}
	
	/**
	 * Every variable has to occur at least twice and has to be connected to the outside by also occurring as an argument
	 * or being part of a relational constraint. 
	 * @return
	 */
	private boolean checkOperatorVariables( Collection<Operator> O ) {
		if ( verbose ) {
			System.out.println("Operator variables:");
		}
		for ( Operator o : O ) {
			if ( verbose ) {
				System.out.print(getDottedString(o.getName().toString(),numChars));
			}
			
			// Connected with either arguments or part of relational constraints
			ArrayList<String> connectedVars = new ArrayList<String>();
			// Not part of arguments and will not be grounded by background knowledge
			ArrayList<String> disconnectedVars = new ArrayList<String>();
			
			for ( int i = 0 ; i < o.getName().getNumArgs() ; i++ ) {
				Term t = o.getName().getArg(i);
				for ( Term v : t.getVariables() ) {
					connectedVars.add(v.toString());
				}
			}
			
			for ( Statement s : o.getPreconditions() ) {
				for ( int i = 0 ; i < s.getVariable().getNumArgs() ; i++ ) {
					Term t = s.getVariable().getArg(i);
					for ( Term v : t.getVariables() ) {
						disconnectedVars.add(v.toString());
					}
				}
				for ( Term v : s.getValue().getVariables() ) {
					disconnectedVars.add(v.toString());
				}
					
			}
			for ( Statement s : o.getEffects() ) {
				for ( int i = 0 ; i < s.getVariable().getNumArgs() ; i++ ) {
					Term t = s.getVariable().getArg(i);
					for ( Term v : t.getVariables() ) {
						disconnectedVars.add(v.toString());
					}
				}
				for ( Term v : s.getValue().getVariables() ) {
					disconnectedVars.add(v.toString());
				}
			}
			
			ArrayList<String> allVars = new ArrayList<String>();
			ArrayList<String> atLeastOccurTwiceVars = new ArrayList<String>();
			
			
			for ( Expression c : o.getConstraints() ) {
				if ( c instanceof PrologConstraint ) {
					
					PrologConstraint rC = (PrologConstraint)c;
					
					for ( int i = 0 ; i < rC.getRelation().getNumArgs() ; i++ ) {
						Term t = rC.getRelation().getArg(i);
						for ( Term v : t.getVariables() ) {
							connectedVars.add(v.toString());
						}
					}
				} else if ( c instanceof AllenConstraint ) {
					AllenConstraint tC = (AllenConstraint)c;
					
					for ( int i = 0 ; i < tC.getNumBounds() ; i++ ) { 
						Interval ival = tC.getBound(i); 
						for ( Term v : ival.getLowerTerm().getVariables() ) {
							disconnectedVars.add(v.toString());
						}
						for ( Term v : ival.getUpperTerm().getVariables() ) {
							disconnectedVars.add(v.toString());
						}
					}	
				}  else if ( c instanceof SimpleDistanceConstraint ) {
					SimpleDistanceConstraint tC = (SimpleDistanceConstraint)c;
					
						for ( Term v : tC.getBound().getLowerTerm().getVariables() ) {
							disconnectedVars.add(v.toString());
						}
						for ( Term v : tC.getBound().getUpperTerm().getVariables() ) {
							disconnectedVars.add(v.toString());
						}	
				} else if ( c instanceof VariableDomainRestriction ) {
					VariableDomainRestriction tC = (VariableDomainRestriction)c;
					
					disconnectedVars.add(tC.getVariable().toString());
					
					for ( Term val : tC.getDomain() ) {
						disconnectedVars.add(val.toString());
					}	
				}  else if ( c instanceof CustomConstraint ) {
					CustomConstraint crc = (CustomConstraint)c;
					
					for ( int i = 0 ; i < crc.getRelation().getNumArgs() ; i++ ) {
						Term t = crc.getRelation().getArg(i);
						for ( Term v : t.getVariables() ) {
							connectedVars.add(v.toString());
						}
					}
				} else if ( c instanceof NewObject ) {
					NewObject cC = (NewObject)c;
					for ( Term varTerm : cC.getVariableTerms() ) {
						connectedVars.add(varTerm.toString());
					}
				} else if ( c instanceof GraphConstraint ) {
					GraphConstraint cC = (GraphConstraint)c;
					for ( Term varTerm : cC.getVariableTerms() ) {
						connectedVars.add(varTerm.toString());
					}
				} else {
					for ( Term varTerm : c.getVariableTerms() ) {
						connectedVars.add(varTerm.toString());
					}
				}
			}
			
			for ( String v : disconnectedVars ) {
				if ( !connectedVars.contains(v) ) {
					if ( verbose ) {
						System.out.println("[FAIL]");
						System.err.println( "ERROR: Variable '" + v + "' is disconnected. Connect it by making it an argument of the operator." );
					}
					return false;
				}
				
				if ( !allVars.contains(v) ) {
					allVars.add(v);
				} else {
					if ( !atLeastOccurTwiceVars.contains(v) ) {
						atLeastOccurTwiceVars.add(v);
					}
				}
			}
				
			for ( String v : connectedVars ) {
				if ( !allVars.contains(v) ) {
					allVars.add(v);
				} else {
					if ( !atLeastOccurTwiceVars.contains(v) ) {
						atLeastOccurTwiceVars.add(v);
					}
				}
			}
			
			for ( String v : allVars ) {
				if ( !atLeastOccurTwiceVars.contains(v) ) {
					if ( verbose ) {
						System.err.println( "WARNING: Variable '" + v + "' only occurs once. This means it may be useless or a second reference is missing." );
					}
				}
			}
			if ( verbose ) {
				System.out.println("[OK]");
			}
		}
		return true;
	}	
	
	/**
	 * Every variable has to occur at least twice and has to be connected to the outside by also occurring as an argument
	 * or being part of a relational constraint. 
	 * @return
	 * @throws UnknownThing 
	 * @throws NonGroundThing 
	 */
	private boolean operatorTemporalConsistency( Collection<Operator> O, ConstraintDatabase context, TypeManager tM ) {
		Operator oCopy = null;
		if ( verbose ) {
				System.out.println("Temporal consistency of isolated operators:");
		}			
		for ( Operator o : O ) {
			if ( verbose ) {
				System.out.print(getDottedString(o.getName().toString(),numChars));
			}
//			System.out.println(o);
			
			oCopy = o.copy();
					
//			boolean temporalConstraintsGround = true;
			
			ConstraintDatabase check = new ConstraintDatabase();
			
			for ( Statement p : oCopy.getPreconditions() ) {
				check.add(p);
			}
//			check.addConstraints(oCopy.getPreconditions());
			for ( Statement e : oCopy.getEffects() ) {
				check.add(e);
			}
//			check.addStatements(oCopy.getEffects());
			check.addAll(oCopy.getConstraints());
			check.add(oCopy.getNameStateVariable());

			
//			for ( AllenConstraint tC : check.get(AllenConstraint.class) ) {
//				temporalConstraintsGround &= tC.isGround();
//			}
			
//			if ( temporalConstraintsGround ) {
			IncrementalSTPSolver csp = new IncrementalSTPSolver(0,Global.MaxTemporalHorizon);
			boolean consistent;
			consistent = csp.isConsistent(check, tM);
			
			if ( verbose && consistent ) {
				System.out.println("[OK]");
			} else if ( !consistent ) {
				csp.debug = true;
				csp.isConsistent(check, tM);
				System.out.println("[FAIL]");
			}
//			} else {
//				if ( !ignoreWarnings ) {
//					System.err.println("[WARNING] Substitution " + oCopy.getSubstitution() + " leaves non-ground temporal constraint (probably problem with backgorund knowledge).");
//				}
//			}				
		}

		
		return true;
	}	
	
	private boolean checkLabels( Collection<Operator> O ) {
		if ( verbose ) {
			System.out.println("Temporal intervals:");
		}
		
		for ( Operator o : O ) {
			if ( verbose ) {
				System.out.print(getDottedString(o.getName().toString(),numChars));
			}
			ArrayList<String> statementsLabels = new ArrayList<String>();
			ArrayList<String> tcLabels = new ArrayList<String>();
			
			for ( Statement s : o.getPreconditions() ) {
				statementsLabels.add(s.getKey().toString());
			}
			for ( Statement s : o.getEffects() ) {
				statementsLabels.add(s.getKey().toString());
			}
			
			for ( Expression c : o.getConstraints() ) {
				if ( c instanceof AllenConstraint ) {
					AllenConstraint tC = (AllenConstraint)c;	
					tcLabels.add(tC.getFrom().toString());
					
					if ( tC.isBinary() ) {
						tcLabels.add(tC.getTo().toString());
					}
				} else if ( c instanceof SimpleDistanceConstraint ) {
					SimpleDistanceConstraint tC = (SimpleDistanceConstraint)c;	
					tcLabels.add(tC.getFrom().toString());
					tcLabels.add(tC.getTo().toString());
				}				
			}
			
			for ( String s : statementsLabels ) {
				if ( !s.contains("key") && !tcLabels.contains(s) ) {
					if ( verbose ) {
						System.out.println("[FAIL]");
						System.err.println( "ERROR: Label '" + s + "' has no associated temporal constraint." );
					}
					
					return false;
				}
			}
			if ( verbose ) {
				System.out.println("[OK]");
			}
		}
		
		return true;
	}	
	
//	private boolean relationalConsistency( Collection<Operator> O, ConstraintDatabase context, TypeManager tM ) {
//		if ( verbose ) {
//			System.out.println("Relational consistency:");
//		}
//				
//		for ( Operator o : O ) {
//			if ( verbose ) {
//				System.out.print(getDottedString(o.getName().toString(),numChars));
//			}
//			Map<Term,Collection<PrologConstraint>> query = PrologTools.getQueries(o);
//			
//			Map<Term,ConstraintCollection> programs = context.getIncludedPrograms(query.keySet());
//			
//			YapPrologAdapter yap = new YapPrologAdapter();
//			
//			for ( Term programID : query.keySet() ) {
//				Collection<Substitution> answer = yap.query(programs.get(programID), query.get(programID), programID, tM);
//				if ( answer == null ) {
//					if ( !yap.errorMessage.equals( "" )) {
//						if ( verbose ) {
//							System.out.println("[FAIL]");
//							System.err.println( "ERROR: Query '" + query + "' can not be satisfied. Calling YAP produced an error." );
//							System.err.println("YAP output:\n" + yap.errorMessage);
//						}
//						return false;
//					} else {
//						warnings++;
//						if ( ! ignoreWarnings ) {
//							System.err.println( "[WARNING] Query '" + query + "' can not be satisfied. This means the operator is never applicable given the background knowledge from domain and problem file. There was no error message by YAP prolog, so it may be the case that this is intentional.");
//						}
//					}
//				}
//			}
//			if ( verbose ) {
//				System.out.println("[OK]");
//			}
//		}
//		
//		return true;
//	}
		
	private String getDottedString(String s, int len) {
		String r = s;
		
		while ( r.length() < len ) {
			r += ".";
		}
		return r;
	}
}
