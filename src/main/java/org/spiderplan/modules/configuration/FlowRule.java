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
package org.spiderplan.modules.configuration;

import java.util.ArrayList;
import java.util.Collection;

import org.spiderplan.modules.FlowModule;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;

/**
 * A rule to guide the {@link FlowModule}, allowing it to decide which {@link Module}
 * is called next given other {@link Module}'s outputs. Also includes input signals
 * that change the behavior of the called {@link Module}.
 * 
 * @author Uwe Köckemann
 */
public class FlowRule {
	String fromModule;
	String toModule;
	ArrayList<String> conditions;
	ArrayList<String> inSignals;
	
	/**
	 * Create a new rule from a string of the form:
	 * 
	 * prevModule => [conditions =>] toModule [: inSignals]
	 * 
	 * where conditions is a comma-separated list of modules and their
	 * output signals of the form:
	 * 
	 * module_1.signal_1, ... , module_n.signal_n
	 * 
	 * and inSignals is a comma-separated list of input signals for
	 * the execution of toModule.
	 * 
	 * @param s
	 */
	public FlowRule( String s ) {
		s = s.replace("\n", "").replace(" ", "").replace("\t", "");
		String[] parts = s.split("=>"); 
		
		this.fromModule = parts[0];
		String toPart;
		String conditionPart;
		
		if ( parts.length == 2 ) {
			toPart = parts[1];
			conditionPart = null;	
		} else {
			conditionPart = parts[1];
			toPart = parts[2];
		}
		
		this.inSignals = new ArrayList<String>();
		this.conditions = new ArrayList<String>();
		
		toModule = toPart.split(":")[0];
		if ( toPart.contains(":") ) {
			String[] inSigTmp = toPart.split(":")[1].split(",");
			for ( String sig : inSigTmp ) {
				inSignals.add(sig);
			}
		}
		
		if ( conditionPart != null ) {
			String[] conditionsTmp = conditionPart.split(",");
			for ( String condition : conditionsTmp ) {
				conditions.add(condition);
			}
		}
	}
	
	/**
	 * Check if this {@link FlowRule} applies to a {@link Core} given that name of the
	 * previous {@link Module}
	 * @param prevModule Name of the previous {@link Module}
	 * @param c A {@link Core}
	 * @return <code>true</code> if this {@link FlowRule} applies to <code>c</code> 
	 * given {@link Module} <code>prevModule</code> was called last, <code>false</code> otherwise.
	 */
	public boolean applies( String prevModule, Core c ) {
		if ( this.fromModule.equals(prevModule) ) {
			for ( String conditionStr : this.conditions ) {
				String conditionModule = conditionStr.split("\\.")[0];
				String conditionSignal = conditionStr.split("\\.")[1];
				if ( 	 c.getResultingState(conditionModule) == null ||
						!c.getResultingState(conditionModule).equals(Core.State.valueOf(conditionSignal))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Get name of {@link Module} that will be called if this rule applies.
	 * @return Name of target {@link Module}.
	 */
	public String getToModule() { return toModule; }
	
	/**
	 * Get input signals that will be send to the module
	 * if this rule applies.
	 * @return A {@link Collection} of {@link String}s that are input
	 * signals for the target {@link Module}. 
	 */
	public Collection<String> getInSignals() {
		ArrayList<String> inSignals = new ArrayList<String>();
		for ( String sig: this.inSignals ) {
			inSignals.add(sig);
		}
		return inSignals; 
	}
	
	@Override
	public String toString() {
		String r = fromModule + " => ";
		if ( !conditions.isEmpty() ) {
			r += conditions.toString().replace("[", "").replace("]", "") + " => ";
		}
		r += toModule;
		if ( !inSignals.isEmpty() ) {
			r += ": " + inSignals.toString().replace("[", "").replace("]", "");
		}
		return r; 
	}
}
