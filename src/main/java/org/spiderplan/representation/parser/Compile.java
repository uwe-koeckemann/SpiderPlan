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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.parser.domain_v4.DomainParser_v4;
import org.spiderplan.representation.parser.pddl.PDDLParser;
import org.spiderplan.representation.parser.planner.ParseException;
import org.spiderplan.representation.parser.planner.PlannerParser;
import org.spiderplan.representation.types.IntervalType;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Parses domain and planner definitions.
 * 
 * @author Uwe Köckemann
 *
 */
public class Compile {

	/**
	 * Different domain versions.
	 * @author Uwe Köckemann
	 */
	public static enum DomainVersion { 
		/**
		 * Old version
		 */
		v2,  
		/**
		 * Older version
		 */
		v3,
		/**
		 * Most recent version
		 */
		v4
		};
	
	/**
	 * Change to switch domain version for old syntax.
	 */
	public static DomainVersion domainVersion = DomainVersion.v4;
	
	static java.io.FileInputStream fInStream; 

	private static Core c;
	private static ConfigurationManager oM;
	
	private static HashMap<String,String> bkbLookUp;
	private static int nextBKB_ID = 0;
	
	/**
	 * Toggle console output
	 */
	public static boolean verbose = false;
	/**
	 * Toggle stop-watch to measure compile times
	 */
	public static boolean keepTimes = false;
	/**
	 * Print times measured by stop-watch directly
	 */
	public static boolean printTimes = false;
	
	/**
	 * Get the compiled {@link Core}
	 * @return the core
	 */
	public static Core getCore() {
		return c;
	}
	
	/**
	 * Get planner definition
	 * @return configuration of the planner
	 */
	public static ConfigurationManager getPlannerConfig() {
		return oM;
	}
	
	/**
	 * Compile a single file
	 * @param fileName name of the file
	 * @return {@link Core} containing all compiled information
	 */
	public static Core compile( String fileName ) {
		bkbLookUp = new HashMap<String, String>();
		Core c = new Core();
		c.setTypeManager(new TypeManager());
		Compile.compileDomainAndProblem(fileName, c);
		return c;
	}
	 
	/**
	 * Compile a list of domain definition files and a single planner definition
	 * @param domainFilenames list of filenames of domain files
	 * @param plannerFilename planner definition filename
	 */
	public static void compile( ArrayList<String> domainFilenames, String plannerFilename ) {
		boolean isPDDL = false;
		
		if ( keepTimes || printTimes ) StopWatch.start("Compiling");
		try {
				
			ModuleFactory.forgetStaticModules();
			
			c = new Core();
			oM = new ConfigurationManager();
			bkbLookUp = new HashMap<String, String>();
			nextBKB_ID = 0;
			
			c.setTypeManager(new TypeManager());
			/**
			 * Add "executing" Type with one value "executing" which is used to
			 * assert that an action is being executed.
			 */
			c.getTypeManager().addSimpleEnumType("executing","executing");
			/**
			 * Another default EnumType: boolean
			 */
			c.getTypeManager().addSimpleEnumType("boolean", "true,false");
			
			c.getTypeManager().addNewType(new IntervalType());
			
			for ( String filename : domainFilenames ) {
				File file = new File(filename);
				String path = file.getParentFile().getAbsolutePath().toString() + "/";
				path = path.replace("/./", "/");
				DomainParser_v4.sourceDirectory = path;
				
				if ( keepTimes || printTimes ) StopWatch.start("Compiling domain");
				Compile.compileDomainAndProblem(filename, c);
				if ( keepTimes || printTimes ) StopWatch.stop("Compiling domain");
				
				if ( filename.endsWith(".pddl") ) {
					if ( keepTimes || printTimes ) StopWatch.start("PDDL post processing");
//					PDDLpostProcessing();
					isPDDL = true;
					if ( keepTimes || printTimes ) StopWatch.stop("PDDL post processing");
				}
			}
			
			if ( keepTimes || printTimes ) StopWatch.start("Updating type domains");
			c.getTypeManager().updateTypeDomains();
			if ( keepTimes || printTimes ) StopWatch.stop("Updating type domains");
			
			
			/**
			 * Make all keys in initial context ground
			 */
			Substitution theta = new Substitution();
			for ( Statement s : c.getContext().get(Statement.class) ) {
				if ( s.getKey().isVariable() ) {
					Term newKey = s.getKey().makeConstant();
					theta.add(s.getKey(),newKey);
				}
			}
			c.getContext().substitute(theta);
					
			if ( domainVersion.equals(DomainVersion.v4) ) {
				c.getTypeManager().collectTypeInformation(c.getContext());
			}
			
			if ( isPDDL ) {
				c.getContext().export(Global.workingDir + "dump.uddl");
			}
			
//			Global.initialContext = c.getContext().copy();
			
//			System.out.println(c.getTypeManager());
//			
//			for ( Operator o : c.getOperators() ) {
//				System.out.println(o);
//			}
//			
//			System.out.println(c .getContext());
			

			
//			c.getInitialContext().addConstraints(c.getConstraints());
//			c.getConstraints().clear();
			
//			for ( Operator o : c.getOperators() ) {
//				System.out.println(o);
//			}
//			
//			System.out.println(c.getInitialContext());
//			System.exit(0);
			
//			
//			System.out.println(c.getGoalContext());
//			
//			System.out.println(c.getTypeManager());
//			System.exit(0);
//			
//			c.getTypeManager().updateTypeDomains();
//			
//			System.out.println(c.getTypeManager());
						
//			System.out.println(c.getTypeManager());
//			if ( c.getTypeManager().hasTypeWithName("number")) {
//				Type t = c.getTypeManager().getTypeByName("number");
//				t.setName("myNumber");
//			}
//			

			
//			/**
//			 * Replace non-ground statements in initial context with all
//			 * possible ground statements.
//			 */
//			ArrayList<Statement> addList = new ArrayList<Statement>();
//			ArrayList<Statement> remList = new ArrayList<Statement>();
//			for ( Statement s : c.getInitialContext().getStatements() ) {
//				if ( !s.isGround() ) {
//					remList.add(s);
//										
//					Collection<Substitution> allCombos = c.getTypeManager().getAllGroundSubstitutions(s.getVariable());
//					
//					for ( Substitution sub : allCombos ) {
//						Statement sCopy = s.copy();
//						sCopy.substitute(sub);
//						addList.add(sCopy);
//						sCopy.setKey( new Term("key"+UniqueID.getID()) );
//					}
//					
//				}
//			}
//			for ( Statement s : remList ) {
//				c.getInitialContext().remove(s);
//			}
//			c.getInitialContext().addStatements(addList);	
				
			if ( keepTimes || printTimes ) StopWatch.start("Consistency check");
			ConsistencyChecker cC = new ConsistencyChecker();
			ConsistencyChecker.verbose = Compile.verbose;
			if ( !cC.check( c ) ) {
				System.err.println("ERROR: Found inconsistencies! Switching to verbose and checking again...\n");
				ConsistencyChecker.verbose = true;
				cC.check( c );
				System.exit(1);
			}
			if ( keepTimes || printTimes ) StopWatch.stop("Consistency check");
		
			if ( keepTimes || printTimes ) StopWatch.start("Compiling planner");
			Compile.compilePlanner(plannerFilename);
			if ( keepTimes || printTimes ) StopWatch.stop("Compiling planner");
			
			if ( verbose || printTimes ) {
				System.out.println(StopWatch.allSums2Str());
			}

		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(0);
		}
		if ( keepTimes || printTimes ) StopWatch.stop("Compiling");
	}
		
	private static void compilePlanner( String fileName ) throws ParseException {
		if ( Compile.verbose ) {
			System.out.println(fileName);
		}

		oM = new ConfigurationManager();
		
		try {
			fInStream = new java.io.FileInputStream(fileName);
			PlannerParser parserVarTypes = new PlannerParser(fInStream);
			PlannerParser.verbose = Compile.verbose;
			parserVarTypes.CompilationUnit(oM);
			fInStream.close();	
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (java.io.IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void compileDomainAndProblem( String fileName, Core c )  {
		if ( Compile.verbose ) {
			System.out.println(fileName);
		}

		try {

			InputStream inStream = replaceKnowledgeBases(fileName);

			if ( !fileName.endsWith(".pddl")) {
				if ( domainVersion.equals(DomainVersion.v4)) {
					DomainParser_v4 domainParser_v4 = new DomainParser_v4(inStream);
					DomainParser_v4.verbose = Compile.verbose;
					domainParser_v4.CompilationUnit(c, bkbLookUp);
				}   
			} else {
				PDDLParser pddlParser = new PDDLParser(inStream);
				PDDLParser.verbose = Compile.verbose;
				pddlParser.CompilationUnit(c);	
			}

		} catch (org.spiderplan.representation.parser.pddl.ParseException e) {
			System.err.println("In file " + fileName);
			e.printStackTrace();
			System.exit(1);
		} catch (org.spiderplan.representation.parser.domain_v4.ParseException e) {
			System.err.println("In file " + fileName);
			e.printStackTrace();
			System.exit(1);
		}  
	}

	/**
	 * Takes a file and replaces foreign code with a key and adds it to a look up table.
	 * If the code comes from a file the file in question will be opened and added to the look up
	 * instead.
	 * @param fName
	 * @return A new {@link InputStream} providing the input to the parser.
	 */
	private static InputStream replaceKnowledgeBases( String fName ) {
		try {
			String beginEscape = "begin-escape-syntax";
			String endEscape = "end-escape-syntax";
			
			String beginFileEscape = "begin-include-file";
			String endFileEscape = "end-include-file";
			
			if ( !domainVersion.equals(DomainVersion.v4) ) {
				beginEscape = "begin_inline_kb";
				endEscape = "end_inline_kb";
			}
			
			if ( !domainVersion.equals(DomainVersion.v4) ) {
				beginFileEscape = "begin_file_kb";
				endFileEscape = "end_file_kb";
			}
			
			java.io.File file = new java.io.File(fName);
			Scanner scanner = new Scanner(file);
			scanner.useDelimiter("\\Z");			
			String content = scanner.next();
			scanner.close();
			
			
			String[] tmp = content.split("\\<"+beginEscape+"\\>");
			
			for ( int i = 1 ; i < tmp.length ; i++ ) {
				String s = tmp[i];
				String bkbStr = s.split("\\<"+endEscape+"\\>")[0];
				String key = "bkbKey" + nextBKB_ID++;
				
				/**
				 * Replace bkb with look up key
				 * Also adds newlines equal to the number of newlines in original file so
				 * that line numbers in parser errors remain meaningful even if we change the file.
				 */
				content = content.replaceFirst(Pattern.quote("<"+beginEscape+">" + bkbStr + "<"+endEscape+">"), key+newLines(bkbStr));
				bkbLookUp.put(key, bkbStr);
			}
			
			tmp = content.split("\\<"+beginFileEscape+"\\>");
			 
			for ( int i = 1 ; i < tmp.length ; i++ ) {
				String s = tmp[i];
				String fNameKBStr = s.split("\\<"+endFileEscape+"\\>")[0];

				String key = "bkbKey" + nextBKB_ID++;
				
				/**
				 * Get the "real" file name:
				 * Relative file names are relative to the file they are referenced in
				 * and not to the project root
				 */
				String fNameKBreal;
				if ( !fNameKBStr.trim().startsWith("/") ) {  // file path is relative to the file its referenced in
					File f = new File(fName);
					if ( !fNameKBStr.trim().startsWith("."))
						fNameKBreal = f.getParent() + "/" + fNameKBStr.trim();
					else
						fNameKBreal = f.getParent() + "/" + fNameKBStr.trim();
				} else {	// file path is absolute
					fNameKBreal = fNameKBStr;
				}
				
				scanner = new Scanner(new java.io.File(fNameKBreal));
				scanner.useDelimiter("\\Z");			
				String contentFileKB = scanner.next();
				scanner.close();
				
				//String contentFileKB = new Scanner(new java.io.File(fNameKBreal)).useDelimiter("\\Z").next();
				

				content = content.replaceFirst(Pattern.quote("<"+beginFileEscape+">" + fNameKBStr + "<"+endFileEscape+">"), key+newLines(fNameKBStr));
				bkbLookUp.put(key, contentFileKB);
			}
						
			return new ByteArrayInputStream(content.getBytes());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	 private static String newLines(String str){
	    String[] lines = str.split("\r\n|\r|\n");
	    String r = "";
	    for ( int i = 0 ; i < lines.length ; i++ ) {
	    	r += "\n";
	    }
	    return r;
	 }

	 /**
	  * Automatically detects unchangeable facts and makes them background knowledge and constraints.
	  * Adds temporal Meets constraints between changes of the same variable to avoid symbolic value 
	  * scheduling conflicts.
	  */
//	 private static void PDDLpostProcessing() {
//		 Term programID = Term.createConstant("pddlKB");
//		 ArrayList<Atomic> moveToBK = new ArrayList<Atomic>();
//		for ( Statement s : c.getContext().get(Statement.class) ) {
//			boolean canChange = false;
//			
//			for ( Statement s2 : c.getContext().get(Statement.class) ) {
//				if ( !s.equals(s2) && s.getVariable().equals(s2.getVariable()) && !s.getValue().equals(s2.getValue())) {
//					canChange = true;
//				}
//			}
//			
//			if ( !canChange ) {
//				for ( Operator o : c.getOperators() ) {
//					for ( Statement e : o.getEffects() ) {
//						canChange = (s.getVariable().match(e.getVariable()) != null);
//						if ( canChange ) {
//							break;
//						}
//					}
//					if ( canChange ) {
//						break;
//					}
//				}
//				
//				if ( !canChange ) {
//					if ( !moveToBK.contains(s.getVariable())) {
//						moveToBK.add(s.getVariable());
//					}
//				}
//			}
//		}
//		
//		for ( Atomic var : moveToBK ) {
//			/**
//			 * Move static facts from statements to asserted constraints
//			 */
//			ArrayList<Statement> remList = new ArrayList<Statement>();
//			ArrayList<Term> remKeys = new ArrayList<Term>();
//			for ( Statement s : c.getContext().get(Statement.class) ) {
//				if ( s.getVariable().equals(var) ) {
//					remList.add(s);
//					remKeys.add(s.getKey());
//					if ( s.getValue().toString().equals("true")) {
//						PrologConstraint rC = new PrologConstraint(var,programID);
//						rC.setAsserted(true);
//						c.getContext().add(rC);
//						if ( verbose ) {
//							System.out.println("Moving unchangable fact " + s + " to constraint " + rC);
//						}
//					}
//				}
//			}
//			c.getContext().removeAll(remList);			
//			HashSet<Expression> remSet = new HashSet<Expression>();
//			for ( AllenConstraint tC : c.getContext().get(AllenConstraint.class) ) {
//				if ( remKeys.contains(tC.getFrom()) || remKeys.contains(tC.getTo()) ) {
//					remSet.add(tC);
//				}				
//			}
//			c.getContext().removeAll(remSet);
//
//			/**
//			 * Move preconditions on static facts from preconditions to constraints
//			 * and remove associated temporal constraints.
//			 */
//			for ( Operator o : c.getOperators() ) {
//				remList = new ArrayList<Statement>();
//				ArrayList<Expression> remListCon = new ArrayList<Expression>();
//				for ( Statement p : o.getPreconditions() ) {
//					if ( p.getVariable().match(var) != null ) {
//						PrologConstraint rC = new PrologConstraint(p.getVariable(),programID);
//						remList.add(p);
//						
//						for ( Expression con : o.getConstraints() ) {
//							if ( con instanceof AllenConstraint ) {
//								AllenConstraint tC = (AllenConstraint)con;
//								if ( tC.getFrom().equals(p.getKey()) || ( tC.isBinary() && tC.getTo().equals(p.getKey())) ) {
//									remListCon.add(tC);
//								}
//							}
//							if ( con instanceof SimpleDistanceConstraint ) {
//								SimpleDistanceConstraint tC = (SimpleDistanceConstraint)con;
//								if ( tC.getFrom().equals(p.getKey()) || tC.getTo().equals(p.getKey()) ) {
//									remListCon.add(tC);
//								}
//							}
//						}
//						
//						o.addConstraint(rC);
//						
//						if ( verbose ) {
//							System.out.println("Replacing precondition " + p + " by constraint " + rC);
//						}
//						
//						if ( p.getValue().equals("false")) {
//							throw new IllegalStateException("Negative precondition "+p+" on unchangable variable "+p.getVariable()+". Not supported and probably not necessary.");
//						}																
//					}
//				}
//		
//				o.getPreconditions().removeAll(remList);
//				o.getConstraints().removeAll(remListCon);
//			}
//		}
//		
//		/**
//		 * Add Meets constraints to chains of changes to avoid scheduling where possible:
//		 */
//		for ( Operator o : c.getOperators() ) {
//			HashMap<Atomic,ArrayList<Term>> usages = new HashMap<Atomic, ArrayList<Term>>();
//			
//			for ( Statement p : o.getPreconditions() ) {
//				if ( !usages.containsKey(p.getVariable())) {
//					usages.put(p.getVariable(), new ArrayList<Term>());
//				} 
//				usages.get(p.getVariable()).add(p.getKey());
//			}
//			String prevConType = "";
//			String conType = "";
//			for ( Statement e : o.getEffects() ) {
//				if ( !usages.containsKey(e.getVariable())) {
//					usages.put(e.getVariable(), new ArrayList<Term>());
//				} 
//				for ( Expression con : o.getConstraints() ) {
//					if ( con instanceof AllenConstraint ) {
//						AllenConstraint tC = (AllenConstraint)con;
//						conType = tC.getRelation().toString();
//						break;
//					}
//				}
//				/**
//				 * If an "at end" (Meets) constraint comes before "at start"
//				 * we switch the order here so we can add Meets constraints
//				 * along the sequence of changes.
//				 */
//				if ( prevConType.equals("Meets") ) {
//					int numUsages = usages.get(e.getVariable()).size();
//					usages.get(e.getVariable()).add(numUsages-1, e.getKey());
//				} else {
//					usages.get(e.getVariable()).add(e.getKey());
//				}
//				prevConType = conType;
//			}
//				
//			/**
//			 * Each assignment meets the next one:
//			 */
//			if ( verbose ) {
//				System.out.println("Chaining changes:");
//			}
//			for ( Atomic k : usages.keySet() ) {
//				ArrayList<Term> keySequence = usages.get(k);	 					
//				if ( keySequence.size() > 1 ) {
//					for ( int i = 0 ; i < keySequence.size()-1 ; i++ ) {
//						AllenConstraint tC = new AllenConstraint(keySequence.get(i), keySequence.get(i+1), org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation.Meets );
//						o.addConstraint(tC);
//						if ( verbose ) {
//							System.out.println("    " + tC);
//						}
//					}
//				}
//			}
//		}	
//		
//		/**
//		 * Get all ground occurrences of all integer types so we can fix the max value.
//		 */
//		TypeManager tM = c.getTypeManager();
//		ArrayList<Type> intTypes = new ArrayList<Type>();
//		HashMap<Type,ArrayList<Term>> allGroundOccurences = new HashMap<Type, ArrayList<Term>>();
//		
//		for ( Term tName : tM.getTypeNames() ) {
//			Type t = tM.getTypeByName(tName);
//			if ( t instanceof IntegerType ) {
//				allGroundOccurences.put(t, new ArrayList<Term>());
//				intTypes.add(t);
//			}
//		}
//		
//		ArrayList<PrologConstraint> allRelCons = new ArrayList<PrologConstraint>();
//		allRelCons.addAll(c.getContext().get(PrologConstraint.class));
////		allRelCons.addAll(c.getGoalContext().get(RelationalConstraint.class));
//		//allRelCons.addAll(c.get(RelationalConstraint.class));
//		for ( Operator o : c.getOperators() ) {
//			for ( Expression con : o.getConstraints() ) {
//				if ( con instanceof PrologConstraint ) {
//					allRelCons.add((PrologConstraint)con);
//				}
//			}
//		}
//		
//		Type t;
//		
//		for ( PrologConstraint rC : allRelCons ) {
//			Atomic rel = rC.getRelation();
//			for ( int i = 0 ; i < rel.getNumArgs() ; i++ ) {
////				System.out.println("Rel: " + rel + " at " + i);
//				t = tM.getPredicateTypes(rel.getUniqueName(), i);
//				if ( allGroundOccurences.containsKey(t) ) { 
//					if ( rel.getArg(i).isGround() ) {
//						allGroundOccurences.get(t).add(rel.getArg(i));
//					}
//				}
//			}
//		}
//		
//		
//		for ( Statement s : c.getContext().get(Statement.class) ) {
//			for ( int i = 0 ; i < s.getVariable().getNumArgs() ; i++ ) {
//				t = tM.getPredicateTypes(s.getVariable().getUniqueName(), i);
//				if ( allGroundOccurences.containsKey(t) ) { 
//					if ( s.getVariable().getArg(i).isGround() ) {
//						allGroundOccurences.get(t).add(s.getVariable().getArg(i));
//					}
//				}
//			}
//			t = tM.getPredicateTypes(s.getVariable().getUniqueName(), -1);
//			if ( allGroundOccurences.containsKey(t) ) { 
//				if ( s.getValue().isGround() ) {
//					allGroundOccurences.get(t).add(s.getValue());
//				}
//			}
//		}
////		for ( Statement s : c.getGoalContext().getStatements() ) {
////			for ( int i = 0 ; i < s.getVariable().getTerms().length ; i++ ) {
////				t = tM.getPredicateTypes(s.getVariable().getUniqueName(), i);
////				if ( allGroundOccurences.containsKey(t) ) { 
////					if ( s.getVariable().getTerms()[i].isGround() ) {
////						allGroundOccurences.get(t).add(s.getVariable().getTerms()[i]);
////					}
////				}
////			}
////			t = tM.getPredicateTypes(s.getVariable().getUniqueName(), -1);
////			if ( allGroundOccurences.containsKey(t) ) { 
////				if ( s.getValue().isGround() ) {
////					allGroundOccurences.get(t).add(s.getValue());
////				}
////			}
////		}
//		
//		for ( Operator o : c.getOperators() ) {
//			for ( Statement s : o.getPreconditions() ) {
//				for ( int i = 0 ; i < s.getVariable().getNumArgs() ; i++ ) {
//					t = tM.getPredicateTypes(s.getVariable().getUniqueName(), i);
//					if ( allGroundOccurences.containsKey(t) ) { 
//						if ( s.getVariable().getArg(i).isGround() ) {
//							allGroundOccurences.get(t).add(s.getVariable().getArg(i));
//						}
//					}
//				}
//				t = tM.getPredicateTypes(s.getVariable().getUniqueName(), -1);
//				if ( allGroundOccurences.containsKey(t) ) { 
//					if ( s.getValue().isGround() ) {
//						allGroundOccurences.get(t).add(s.getValue());
//					}
//				}				
//			}
//			for ( Statement s : o.getEffects() ) {
//				for ( int i = 0 ; i < s.getVariable().getNumArgs() ; i++ ) {
//					t = tM.getPredicateTypes(s.getVariable().getUniqueName(), i);
//					if ( allGroundOccurences.containsKey(t) ) { 
//						if ( s.getVariable().getArg(i).isGround() ) {
//							allGroundOccurences.get(t).add(s.getVariable().getArg(i));
//						}
//					}
//				}
//				t = tM.getPredicateTypes(s.getVariable().getUniqueName(), -1);
//				if ( allGroundOccurences.containsKey(t) ) { 
//					if ( s.getValue().isGround() ) {
//						allGroundOccurences.get(t).add(s.getValue());
//					}
//				}
//			}
//		}	
//		
//		long maxVal = 0;
//		long usedVal;
//		for ( Type type : intTypes ) {
//			IntegerType tInt = (IntegerType)type;
//			maxVal = 0;
//			for ( Term val : allGroundOccurences.get(type) ) {
//				usedVal = Long.valueOf(val.toString()).longValue();
//				if ( usedVal > maxVal ) {
//					maxVal = usedVal;
//				}
//			}
//			
//			if ( verbose ) {
//				System.out.println("Updating max. value of " + tInt.getName() + " to " + maxVal);
//			}
//			
//			tInt.max = maxVal;			
//		}		
//	 }

}