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
package org.spiderplan.prolog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.spiderplan.representation.Operator;
import org.spiderplan.representation.constraints.Asserted;
import org.spiderplan.representation.constraints.IncludedProgram;
import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.constraints.ConstraintCollection;
import org.spiderplan.representation.constraints.InteractionConstraint;
import org.spiderplan.representation.constraints.PrologConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.EnumType;
import org.spiderplan.representation.types.IntegerType;
import org.spiderplan.representation.types.Type;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.ExecuteSystemCommand;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.Loop;
import org.spiderplan.tools.SimpleParsing;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.stopWatch.StopWatch;


/**
 * Calls YaProlog to answer set of prolog queries.
 * 
 * Note: works by creating files and executing command line call 
 * 		 rather than using direct Prolog interface.
 * 
 * Input: Set of prolog statements and set of queries.
 * Output: True/False & set of answer substitutions
 */
public class YapPrologAdapter {

	public String name = "YapPrologAdapter";
	public enum FailBehavior { Warning, Exit, Ignore };
	public FailBehavior failBehavior = FailBehavior.Warning;
	
	public String errorMessage = "";
	public static String uniqueFileNamePart = "";
	public boolean debug = false;

	private ArrayList<String> qVars = new ArrayList<String> ();
	private String qPred = "";
		
	private Map<String,Term> prologCompatibilityMap = new HashMap<String,Term>();
	
	private boolean verbose = false;
	private int verbosity = 0;
	private boolean keepTimes = false;
	
	private String yapBinaryLocation = "yap";
	
	public YapPrologAdapter() {
		
	}			
	
	public YapPrologAdapter( String binaryLocation ) {
		this.yapBinaryLocation = binaryLocation;
	}
	
	public Collection<Substitution> query( ConstraintCollection kbIn, Collection<PrologConstraint> q, Term programID , TypeManager tM ) {
		prologCompatibilityMap = new HashMap<String, Term>();
		if ( keepTimes ) StopWatch.start("[Prolog] Query");
		if ( q.isEmpty() ) {
			if ( keepTimes ) StopWatch.stop("[Prolog] Query");
			return new ArrayList<Substitution>();
		}

		qPred = createQueryPredicate(q);

		ArrayList<String> B = new ArrayList<String>();
		
		for ( PrologConstraint rC : this.getTypeDomainsAsPrologConstraints(programID, tM) ) {
			B.add(rC.getRelation().getPrologStyleString()+".");
		}		
		if ( kbIn != null ) {
			
			for ( IncludedProgram pC : kbIn.get(IncludedProgram.class) ) {
				B.add(pC.getCode());
			}		
			for ( PrologConstraint rC : kbIn.get(PrologConstraint.class) ) {
				if ( rC.isAsserted() ) {
					B.add(rC.getRelation().getPrologStyleString()+".");
				}
			}
		}
		
		B.add("\n% Automatically generated query clause:");
		B.add(qPred);

		if ( keepTimes ) StopWatch.start("[Prolog] Create kb file");
		this.dumpKB2PrologFile(B);
		if ( keepTimes ) StopWatch.stop("[Prolog] Create kb file");

		String bagOfStr = this.createBagofTerm(qPred);

		if ( keepTimes ) StopWatch.start("[Prolog] Create query file");
		this.createQueryFile(bagOfStr);
		if ( keepTimes ) StopWatch.stop("[Prolog] Create query file");

		if ( keepTimes ) StopWatch.start("[Prolog] Run Prolog");
		this.runProlog();
		if ( keepTimes ) StopWatch.stop("[Prolog] Run Prolog");
		
		Collection<Substitution> r = this.getResults();
		
		if ( keepTimes ) StopWatch.stop("[Prolog] Query");
		
		return  r;
	}
	
	/**
	 * Returns all true ground atomics of relational constraints in O.
	 * Better than saturate, because it may use math in clauses, but
	 * never try to saturate math (or any other unnecessary) formulas.
	 * 
	 *  Add result to initial state of causal planner, so that it can
	 *  work with relational constraints as preconditions.
	 * @param O 
	 * @param B 
	 * @param tM 
	 */
	public void saturateConstraints( Collection<Operator> O, ConstraintCollection B, Term programID, TypeManager tM ) {		
		Set<Operator> remList = new HashSet<Operator>();
		Set<Operator> addList = new HashSet<Operator>();

		for ( Operator o : O ) {
			remList.add(o);
	
			ArrayList<PrologConstraint> query = new ArrayList<PrologConstraint>();
			ArrayList<Atomic> qLits = new ArrayList<Atomic>();
			
			for ( Constraint c : o.getConstraints() ) {
				if ( c instanceof PrologConstraint ) {	
					if ( ((PrologConstraint) c).getProgramID().equals(programID) ) {
						query.add((PrologConstraint)c);
						qLits.add(((PrologConstraint)c).getRelation());
					}
				}
			}
			
			if ( query.isEmpty() ) {
//				System.out.println("Empty query");
				addList.add(o);
			} else {					
				Collection<Substitution> qResult = query(B, query, programID, tM);
				
				
				if ( qResult != null ) {
					ArrayList<Substitution> appliedSubst = new ArrayList<>();
					/*
					 * Check all Terms in substituted Atomics for Prolog don't care variables (e.g. _1234).
					 * If one is found, new Atomics are created for every value in the domain of this variable
					 * and pushed onto the stack. Only ground Atomics for which all Terms are inside their Type's
					 * domains are removed from stack and added to results. 
					 */
					Map<Term,Type> varTypes = new HashMap<Term,Type>();
					for ( Atomic a : qLits ) {
						for ( int i = 0 ; i < a.getNumArgs() ; i++ ) {
							Type t = tM.getPredicateTypes(a.getUniqueName(), i);
							varTypes.put(a.getArg(i), t);
						}
					}
					
					Stack<Substitution> workStack = new Stack<Substitution>();
					workStack.addAll(qResult);
					while ( !workStack.empty() ) {
						Substitution q = workStack.pop();
						boolean isGround = true;
						boolean isInDomain = true;
						for ( Term from : q.getMap().keySet() ) {
							if ( q.getMap().get(from).toString().substring(0, 1).equals("_") ) {	
								isGround = false;
								ArrayList<Term> domain = varTypes.get(from).getDomain();
								for ( Term val : domain ) {
									Substitution qNew = q.copy();
									qNew.getMap().put(from,val);
									workStack.push(qNew);
								}
								break;
							} else {
								if ( !varTypes.get(from).contains(q.getMap().get(from)) ) {
									isInDomain = false;
									break;
								}
							}
						}		
						
						if ( isGround && isInDomain ) {  // has no Terms starting with _ (prolog don't care values)
							appliedSubst.add(q);	
						} 
					}
					
					/*
					 * Substitute
					 */
					for ( Substitution theta  : appliedSubst ) {
						Operator oCopy = o.copy();
						oCopy.substitute(theta);
						Collection<Constraint> conAddList = new ArrayList<Constraint>();
						for ( Constraint c : oCopy.getConstraints() ) {
							if ( c instanceof PrologConstraint ) {
								conAddList.add(new Asserted(c));
							}
						}
						oCopy.addConstraints(conAddList);
						addList.add(oCopy);
					}	
				} else { 
					//TODO: Use Logger.err() once that exists...
					if ( failBehavior == FailBehavior.Warning ) {
						Logger.msg(this.name, "[WARNING] Query failed! " + query, 0);
					} else if ( failBehavior == FailBehavior.Exit ) {
						Logger.msg(this.name, "[WARNING] Query failed! " + query, 0);
						Logger.msg(this.name, "Exiting because failBehvaior is set to Exit.", 0);
						System.err.println("Exiting because failBehvaior is set to Exit.");
						Loop.start();
					}
				}
			}
//			if (o.getName().toString().contains("pick")) {
//				Loop.start();
//			}
		}
		
		O.removeAll(remList);
		O.addAll(addList);
	}
		
	/**
	 * Replace all {@link InteractionConstraint}s in <i>C</i> with partial ground versions
	 * that are consistent with their relational constraints.
	 */
	public void saturateInteractionConstraints( ConstraintCollection C, ConstraintCollection B, Term programID, TypeManager tM ) {
		ArrayList<InteractionConstraint> remList = new ArrayList<InteractionConstraint>();
		ArrayList<InteractionConstraint> addList = new ArrayList<InteractionConstraint>();
		
		for ( InteractionConstraint iC : C.get(InteractionConstraint.class) ) {
			remList.add(iC);
			ArrayList<PrologConstraint> query = new ArrayList<PrologConstraint>();
			
			for ( Constraint c : iC.getCondition().getConstraints() ) {
				if ( c instanceof PrologConstraint ) {	
					query.add((PrologConstraint)c);
				}
			}
			
			
			if ( !query.isEmpty() ) {				
				Collection<Substitution> qResult = query(B, query, programID, tM);
				
//				System.out.println(qResult);

				if ( qResult != null ) {
					for ( Substitution theta  : qResult ) {
						InteractionConstraint cCopy = iC.copy();
						cCopy.substitute(theta);
						addList.add(cCopy);
					}
				}
			} else {
				addList.add(iC);
			}
		}

		C.removeAll(remList);
		
		for ( InteractionConstraint ic : addList ) {
			boolean r = C.add(ic);
		}
	}
	
	/**
	 * Replace all {@link Operator}s in <i>O</i> with partial ground versions
	 * that are consistent with their relational constraints.
	 * 
	 * (Not yet working...)
	 */
	public void saturateOperatorConstraints( Collection<Operator> O, ConstraintCollection B, Term programID, TypeManager tM ) {
		ArrayList<Operator> remList = new ArrayList<Operator>();
		ArrayList<Operator> addList = new ArrayList<Operator>();
		
		for ( Operator o : O ) {
			remList.add(o);
			ArrayList<PrologConstraint> query = new ArrayList<PrologConstraint>();
			
			for ( Constraint c : o.getConstraints() ) {
				if ( c instanceof PrologConstraint ) {	
					query.add((PrologConstraint)c);
					
				}
			}
			
			if ( !query.isEmpty() ) {
				Collection<Substitution> qResult = query(B, query, programID, tM);				
				if ( qResult != null ) {
					for ( Substitution theta  : qResult ) {
						Operator oCopy = o.copy();
						oCopy.substitute(theta);
						addList.add(oCopy);
					}
				}
			}
		}
		O.removeAll(remList);
		O.addAll(addList);
	}
	
	private void createQueryFile(String bagOfString) {
		try{
			FileWriter fstream = new FileWriter(Global.workingDir+"query"+uniqueFileNamePart+".prolog");
			BufferedWriter out = new BufferedWriter(fstream);

			out.write( ":- ['kb"+uniqueFileNamePart+".prolog'], " + bagOfString + " -> tell('answer"+uniqueFileNamePart+".prolog'), write(QueryResult) ; tell('answer"+uniqueFileNamePart+".prolog'), write('[-]') ." );
			out.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String createBagofTerm( String qClause ) {
		String b = "bagof( ";
		String qPred = qClause.split(" :- ")[0];	// take head of clause

		String varStr;
		if ( qPred.contains("(") ) {
			varStr = qPred.split("\\(")[1].split("\\)")[0].replace(",", "/").replace(" ","");
		} else {
			varStr = qPred;
		}
		b += varStr + " , " + qPred + " , " + " QueryResult )";

		return b;
	}

	private String createQueryPredicate( Collection<PrologConstraint> q ) {
		ArrayList<String> vars = new ArrayList<String>();
		ArrayList<Atomic> qLits = new ArrayList<Atomic>();
		
		Map<String,String> alreadyMapped = new HashMap<String, String>();
		int varIdx = 0;

		for ( PrologConstraint rC : q ) {
			Substitution theta = new Substitution();
			for ( int i = 0 ; i < rC.getRelation().getNumArgs() ; i++ ) {
				Term t = rC.getRelation().getArg(i);
				ArrayList<String> tmp = getAllVars(t);

				for ( String tStr : tmp ) {
					String newVarStr;
					newVarStr = alreadyMapped.get(tStr);
					if ( newVarStr == null ) {
						String pComp = makePrologCompatible(tStr);
						
						String newVarName;
						if ( pComp.equals(tStr) ) {
							newVarName = "Var" + tStr;
						} else {
							newVarName = "Var" + (varIdx++) + pComp.replace("?", "");
						}
						
						alreadyMapped.put(tStr, newVarName);
						prologCompatibilityMap.put(newVarName, t);
						newVarStr = newVarName;
					}
					theta.add(Term.createVariable(tStr), Term.createVariable(newVarStr));
					if ( ! vars.contains(newVarStr) ) {
						vars.add(newVarStr);
					}
				}
			}

			qLits.add(rC.getRelation().substitute(theta));
		}

		qVars.removeAll(qVars);
		for ( String v : vars ) {
			qVars.add(v);
		}

		String qPred = "queryPred";
		if ( vars.size() > 0 ) {
			qPred += "( " + vars.get(0);
			for ( int i = 1 ; i < vars.size() ; i++ ) {
				qPred += ", " + vars.get(i);
			}
			qPred += " )";
		} 	

		qPred += " :- " + qLits.get(0).getPrologStyleString();

		for ( int i = 1 ; i < qLits.size(); i++ ) {
			qPred += ", " + qLits.get(i).getPrologStyleString();
		}
		qPred += ".";

		return qPred;
	}

	private ArrayList<String> getAllVars( Term t ) {
		ArrayList<String> r = new ArrayList<String>();

		if ( t.isVariable() ) {
			r.add(t.getPrologStyleString());
		} else if ( t.isComplex() ) {
			for ( int i = 0 ; i < t.getNumArgs() ; i++ ) {
				r.addAll( getAllVars( t.getArg(i) ) );
			}
		}
		return r;
	}

	private void dumpKB2PrologFile( ArrayList<String> kb ) {
		try{
			FileWriter fstream = new FileWriter(Global.workingDir+"kb"+uniqueFileNamePart+".prolog");
			BufferedWriter out = new BufferedWriter(fstream);
			for ( String l : kb ) {
				out.write( l + "\n");
			}
			out.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void runProlog() {
		try {  
			/* Just create answer file to overwrite old one if exists  */
			@SuppressWarnings("unused")
			FileWriter fAnswer = new FileWriter(Global.workingDir+"answer"+uniqueFileNamePart+".prolog");

			String cmd = yapBinaryLocation + " -L "+Global.workingDir+"query"+uniqueFileNamePart+".prolog";
			
			if ( keepTimes ) StopWatch.start("[Prolog] Running");
			String[] r = ExecuteSystemCommand.call("/tmp/", cmd);
			if ( keepTimes ) StopWatch.stop("[Prolog] Running");
 
			errorMessage = r[1];  
			
			if ( ! errorMessage.equals("") ) {
				throw new Error("YAP error on query " + qPred + ":\n" + errorMessage);
			}
			
		} catch (IOException e) {  
			e.printStackTrace();  
			Loop.start();
		} 
	}

	private Collection<Substitution> getResults() {
		ArrayList<Substitution> resultingSubs = new ArrayList<Substitution>();

		try {
			FileInputStream fstream = new FileInputStream(Global.workingDir+"answer"+uniqueFileNamePart+".prolog");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			ArrayList<String> allSubstitutions;
			
			
			while ((strLine = br.readLine()) != null)   {
				if ( strLine.trim().equals("[queryPred]") || strLine.trim().equals("[queryPred,queryPred]") ) {
					return new ArrayList<Substitution>();
				} else if ( strLine.trim().equals("[-]")) {
					return null;
				}

				allSubstitutions = SimpleParsing.complexSplit(strLine.substring(1,strLine.length()-1), ","); // strLine.substring(1,strLine.length()-1).split(",");
								
				for ( int i = 0 ; i < allSubstitutions.size(); i++ ) {
					String[] constants = allSubstitutions.get(i).replace("/", "<####>").split("<####>");

						
					Substitution theta = new Substitution();

					for ( int k = 0 ; k < constants.length; k++ ) {		
						Term realVal = prologCompatibilityMap.get(constants[k]);
						if ( realVal == null ) {
							realVal = Term.createConstant(constants[k]);
						}
						
						theta.add( prologCompatibilityMap.get(qVars.get(k)), realVal );
					}
					resultingSubs.add(theta);
				}
			}
			
			br.close();
			in.close();

		} catch (Exception e){
			e.printStackTrace();
			System.exit(0);
		}

		if ( resultingSubs.size() > 0 ) {
			return resultingSubs;
		} else {
			return null;
		}
	}
	
	
	/**
	 * Get {@link PrologConstraint}s for generic type definitions based on the domains stored in 
	 * {@link TypeManager} tM. Also makes sure all constants are Prolog compatible and maintains a 
	 * mapping so that the original names can be retrieved later.
	 * 
	 * @param tM {@link TypeManager} containing all typing information.
	 * @return
	 */
	private Collection<PrologConstraint> getTypeDomainsAsPrologConstraints( Term programID, TypeManager tM ) {
		ArrayList<PrologConstraint> r = new ArrayList<PrologConstraint>();
		
		int valIdx = 0;
			
		for ( Term tName  : tM.getTypeNames() ) {
			Type t = tM.getTypeByName(tName); 
			if ( t instanceof IntegerType ) {
				IntegerType iT = (IntegerType)t;
						
				for ( long i = iT.min ; i <= iT.max ; i++ ) {
					prologCompatibilityMap.put(String.valueOf(i), Term.createInteger(i));
					
					PrologConstraint rC = new PrologConstraint(new Atomic(iT.getName().toString(), Term.createInteger(i)) , programID );
					rC.setAsserted(true);
					r.add( rC );
				}
		
			} else if ( t instanceof EnumType ) {
				EnumType iT = (EnumType)t;
						
				for ( Term value : iT.getDomain() ) {
					String newValueName;
					
					String pComp = makePrologCompatible(value.getPrologStyleString());
					
					if ( pComp.equals(value) ) {
						newValueName = value.getPrologStyleString();
					} else {
						newValueName = "val" + (valIdx++) + pComp;
					}
					prologCompatibilityMap.put(newValueName, value);
					
					
					PrologConstraint rC = new PrologConstraint(new Atomic(iT.getName().toString(),  value) , programID );
					rC.setAsserted(true);
					r.add( rC );
				}
			}
		}
		return r;
	}	
	
	private String makePrologCompatible( String s ) {
		return s.replace("-", "").replace("?", "Prolog");
	}
	
	/**
	 * If <code>true</code> this class will use {@link StopWatch} commands that 
	 * are ignored otherwise.
	 * @param keepTimes A flag that decides whether the {@link StopWatch} is used
	 * or not.
	 */
	public void setKeepTimes( boolean keepTimes ) {
		this.keepTimes = keepTimes;
	}
	/**
	 * Change verbose settings
	 * @param verbose Use <code>true</code> if {@link Logger} should be used, <code>false</code> otherwise.
	 * @param verbosity Message granularity level.
	 */
	public void setVerbose( boolean verbose, int verbosity ) {
		this.verbose = verbose;
		this.verbosity = verbosity;
	}
}