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
package org.spiderplan.tools.profiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple class for creating caller profiles.
 * <p>
 * Usage: Call probe() method anywhere in your code 
 * and execute. This will increment a counter
 * for the origin of calls to this point in your code. 
 * Call getString() at any time to get a {@link String}
 * representation containing the total number of calls,
 * their origins and the percentage of calls from one
 * source. 
 * 
 * @author Uwe Köckemann
 */
public class Profiler {
	private static Map<String,Long> counters = new HashMap<String, Long>();
	
	/**
	 * Increments a counter for the stack trace from which the method that calls
	 * the probe method is called from. The counter will contain class name, 
	 * method name and line number for the previous <code>depth</code> classes.
	 * @param depth How far back the stack trace is analyzed. Use <code>0</code> to
	 * only show the caller.
	 */
	public static void probe( int depth ) {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		StringBuilder origin = new StringBuilder();
		
		origin.append(getShortClassName(stElements[2].getClassName()));
		origin.append(".");
		origin.append(stElements[2].getMethodName());
		origin.append("@");
		origin.append(stElements[2].getLineNumber());
		
		origin.append(" <CALLED-BY> ");
		
		for ( int i = 0 ; i <= depth ; i++ ) {
			if ( stElements.length > (3+depth-i) ) {
				origin.append(getShortClassName(stElements[3+depth-i].getClassName()));
				origin.append(".");
				origin.append(stElements[3+depth-i].getMethodName());
				origin.append("@");
				origin.append(stElements[3+depth-i].getLineNumber());
				if ( i < depth ) {
					origin.append("->");
				}
			}
		}
		increment(origin.toString());
		
		StringBuilder total = new StringBuilder();
		total.append(getShortClassName(stElements[2].getClassName()));
		total.append(".");
		total.append(stElements[2].getMethodName());
		total.append("@");
		total.append(stElements[2].getLineNumber());
		
		increment(total.toString());
	}
	
	/**
	 * Returns a {@link String} representation of all recorded data.
	 * @return A {@link String} containing all traces and their counters
	 */
	public static String getString() {
		ArrayList<String> lines = new ArrayList<String>();
		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(counters.keySet());
		Collections.sort(keys);
		
		ArrayList<String> linesForOne = new ArrayList<String>();
		
		for ( String key : keys ) {
			StringBuilder r = new StringBuilder();
			r.append(key);
			r.append(" -> ");
			r.append(counters.get(key));
			r.append("\n");
			String l = r.toString();
			
			if ( key.contains("<CALLED-BY>") ) {
				String caller = key.split(" <CALLED-BY>")[0];
				double part = counters.get(key).doubleValue();
				double total = counters.get(caller).doubleValue();
				double percentage = part/(total/100.0);
				String pStr = String.format("%.3f", percentage);
				while ( pStr.length() < 7 ) 
					pStr = "0" + pStr;
				l = l.replace("<CALLED-BY>", "<CALLED-BY> (" + pStr + "%)");
			} else {
				Collections.sort(linesForOne);
				Collections.reverse(linesForOne);
				lines.addAll(linesForOne);
				linesForOne = new ArrayList<String>();				
				l = l.replace("->", "<CALLED-TOTAL>");
			}
			
			linesForOne.add(l);
		}
		
		Collections.sort(linesForOne);
		Collections.reverse(linesForOne);
		lines.addAll(linesForOne);
		linesForOne = new ArrayList<String>();	
			
		StringBuilder rB = new StringBuilder();
		for ( String line : lines ) {
			rB.append(line);
		}
		return rB.toString();
	}
	
	private static void increment( String key ) {
		if ( !counters.containsKey(key) ) {
			counters.put(key, Long.valueOf(1));
		} else {
			counters.put(key, Long.valueOf(counters.get(key).longValue()+1));
		}
	}
	
	private static String getShortClassName( String s ) {
		String r = s.split("\\.")[s.split("\\.").length-1];
		return r;
	}
}
