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
package org.spiderplan.minizinc;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.ExecuteSystemCommand;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Provides a set of methods to run MiniZinc and get result substitutions.
 * 
 * @author Uwe Koeckemann
 *
 */
public class MiniZincAdapter {

	private static boolean keepTimes = false;
	
	/**
	 * Run minizinc.
	 * 
	 * @param minizincBinaryLocation location of minizinc binary (use "minizinc" when its in PATH)
	 * @param program string representation of program to run
	 * @param allSolutions if <code>true</code> all solutions will be generated
	 * @return all substitutions that satisfy minizinc constraints
	 */
	public static Collection<Substitution> runMiniZinc( String minizincBinaryLocation, String program, boolean allSolutions ) {
		return runMiniZinc(minizincBinaryLocation, program, null,allSolutions, -1);
	}
	
	/**
	 * Run minizinc.
	 * 
	 * @param minizincBinaryLocation location of minizinc binary (use "minizinc" when its in PATH)
	 * @param program string representation of program to run
	 * @param data string representation of data to use for program
	 * @param allSolutions if <code>true</code> all solutions will be generated
	 * @return all substitutions that satisfy minizinc constraints
	 */
	public static Collection<Substitution> runMiniZinc( String minizincBinaryLocation, String program, String data, boolean allSolutions ) {
		return runMiniZinc(minizincBinaryLocation, program, null,allSolutions, -1);
	}
	
	/**
	 * Run minizinc.
	 * 
	 * @param minizincBinaryLocation location of minizinc binary (use "minizinc" when its in PATH)
	 * @param program string representation of program to run
	 * @param data string representation of data to use for program
	 * @param allSolutions if <code>true</code> all solutions will be generated
	 * @param nthSolution index of solution that should be returned
	 * @return all substitutions that satisfy minizinc constraints
	 */
	public static Collection<Substitution> runMiniZinc( String minizincBinaryLocation, String program, String data, boolean allSolutions, int nthSolution ) {		
		String problemFileName = Global.workingDir+"csp"+Global.UniqueFilenamePart+".mzn";
		String dataFileName = Global.workingDir+"csp"+Global.UniqueFilenamePart+".dzn";
				
		try {
			FileWriter fstream = new FileWriter(problemFileName);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write( program );
			out.close();
			fstream.close();
			
			if ( data != null ) {
				fstream = new FileWriter(dataFileName);
				out = new BufferedWriter(fstream);
				out.write( data );
				out.close();
				fstream.close();
			}
			
			String cmd = minizincBinaryLocation + " ";
			if ( allSolutions ) {
				cmd += "--all-solutions ";
			} else if ( nthSolution != -1 ) {
				cmd += "-i " + ((nthSolution-1)*2) + " -n " + nthSolution + " ";
			}
			cmd += problemFileName;
			
			if ( data != null ) {
				cmd += " " + dataFileName;
			}
					
			if ( keepTimes ) StopWatch.start("[MiniZinc] Starting");
			String ret[] = ExecuteSystemCommand.call("/tmp/", cmd);
			if ( keepTimes ) StopWatch.stop("[MiniZinc] Starting");

			ret[0] = ret[0].replace("==========", "").replace("\n", "");
				
			if ( !ret[1].equals("") ) {
				Logger.msg("MiniZinkAdapter", ret[1], 0); //TODO: Use Logger.err
			}
			
			if ( ret[0].contains("=====UNSATISFIABLE=====") ) {
				return null;
			} else {
				ArrayList<Substitution> subst = new ArrayList<Substitution>();
				for ( String s : ret[0].split("----------") ) {
					s = s.replace("{", "").replace("}", "");
					Substitution theta = new Substitution();
					for ( String ft : s.split(",") ) {
						String[] tmp = ft.split("/");
						theta.add(Term.createVariable(tmp[0]), Term.parse(tmp[1]));
					}
					subst.add(theta);
				}
				return subst;
			}
						
		} catch (IOException e) {  
			e.printStackTrace();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Make a term into a minizinc compatible string.
	 * 
	 * @param t term to convert
	 * @return minizinc compatibly string
	 */
	public static String makeMiniZincCompatible( Term t ) {
		return t.toString().replace("?", "");
	}
	
	/**
	 * If <code>true</code> this class will use {@link StopWatch} commands that 
	 * are ignored otherwise.
	 * @param keepTimes A flag that decides whether the {@link StopWatch} is used
	 * or not.
	 */
	public static void setKeepTimes( boolean keepTimes ) {
		MiniZincAdapter.keepTimes = keepTimes;
	}
}
