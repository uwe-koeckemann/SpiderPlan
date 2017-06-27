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
package org.spiderplan.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.spiderplan.causal.forwardPlanning.StateVariableOperator;
import org.spiderplan.representation.logic.Term;



/**
 * Collection of static factory methods that do some simple parsing.
 * Used to create test cases in an easy way.
 * @author Uwe Köckemann
 *
 */
public class SimpleParsing {

	/**
	 * Split String around comma that are not inside parenthesis.
	 * @param resultsLine
	 * @param separator 
	 * @return list of strings
	 */
	public static ArrayList<String> complexSplit( String resultsLine, String separator ) {
		ArrayList<String> results = new ArrayList<String>();
				
		String tmp = "";
		int parenthesisDepth = 0;
		
		for ( int i = 0 ; i < resultsLine.length() ; i++ ) {
			String currentChar = String.valueOf(resultsLine.charAt(i));
			if ( currentChar.equals(separator) && parenthesisDepth == 0 ) {
				results.add(tmp);
				tmp = "";
	
				continue;
			} else if ( currentChar.equals("(") ) {
				parenthesisDepth++;
			} else if ( currentChar.equals(")") ) {
				parenthesisDepth--;
			}
			tmp += currentChar;
		}
		results.add(tmp);
		
		return results;
	}
	
	/**
	 * Converts String representation of term in format f(x,y,g(z)) to 
	 * s-expression format: (f x y (g z))
	 * @param in
	 * @return String in s-expression format
	 */
	public static String convertTermFormat( String in ) {
		String tmp = " " + in.replace(",", " ");
		
		int lastSpaceAt = 0;
		
		for ( int i = 0 ; i < tmp.length(); i++ ) {
			if ( tmp.charAt(i) == ' ' ) {
				lastSpaceAt = i;
			} else if ( tmp.charAt(i) == '(' ) {
				tmp = tmp.substring(0, lastSpaceAt) + " (" + tmp.substring(lastSpaceAt+1,i) + " " + tmp.substring(i+1);  
			}
		}
		return tmp.substring(1);
	}
	
	/**
	 * Create a map of assignments from {@link Term} to {@link Term} from
	 * a {@link String}.
	 * @param s {@link String} of the form "p(a,b)<-c;p(b,c)<-a;..."
	 * @return map from state-variables to values
	 */
	public static Map<Term,Term> createMap( String s ) {
		Map<Term,Term> r = new HashMap<Term, Term>();
		s = s.replace("\n", "").replace("\t", "");
		String[] tmp = s.split(";");
		for ( String assignment : tmp ) {
			Term var = Term.parse(assignment.split("<-")[0]);
			Term val = Term.parse(assignment.split("<-")[1]);
			r.put(var,val);
		}
		return r;
	}
	
	/**
	 * Create a {@link StateVariableOperator} from a {@link String}
	 * @param s {@link String} of the form name(narg1,...,nargn)<p>p1(p1arg1...);---;pn(pnarg1...)<e>e1(e1arg1,...);...;en(enarg1,...)
	 * @return state-variable operator
	 */
	public static StateVariableOperator createSVO( String s ) {
		Term name = Term.parse(s.split("<p>")[0]);
		Map<Term,Term> preconditions = SimpleParsing.createMap(s.split("<p>")[1].split("<e>")[0]);
		Map<Term,Term> effects = SimpleParsing.createMap(s.split("<e>")[1]);
		
		StateVariableOperator svo = new StateVariableOperator();
		
		svo.setName(name);
		svo.getPreconditions().putAll(preconditions);
		svo.getEffects().putAll(effects);
		
		return svo;
	}
	
}
