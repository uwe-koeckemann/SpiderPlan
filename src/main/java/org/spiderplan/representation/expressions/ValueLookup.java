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
package org.spiderplan.representation.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Unique;
import org.spiderplan.representation.logic.Term;


/**
 * Represents all kinds of values that are computed by different solvers.
 * Implemented uses so far include: Storing integer and float results of evaluated equations (:math constraints).
 * <p>
 * <b>Note:</b> Implements {@link Unique} interface so adding more than one instance of this constraint to a {@link ConstraintDatabase} 
 * will overwrite the existing one. 
 * 
 * @author Uwe Köckemann
 * 
 */
public class ValueLookup extends Expression implements Unique, Mutable {
	
	Map<Term,Long> intValues;
	Map<Term,Double> floatValues;
	Map<Term,Long[]> intervals;
		
	/**
	 * Create a new lookup.
	 */
	public ValueLookup( ) {
		super(ExpressionTypes.Math);
		this.intValues = new HashMap<Term, Long>();
		this.floatValues = new HashMap<Term, Double>();
		this.intervals = new HashMap<Term, Long[]>();
	}
	
	/**
	 * Get value from a variable
	 * @param variable 
	 * @return the value or <code>null</code> if variable does not exist
	 */
	public Long getInt( Term variable ) {
		Long r = this.intValues.get(variable);
		if ( r != null ) {
			return r.longValue();
		} else {
			Double rDouble = this.floatValues.get(variable);
			if ( rDouble != null )
				return rDouble.longValue();
		}
		return null;
	}
	
	/**
	 * Get value from a variable
	 * @param variable 
	 * @return the value or <code>null</code> if variable does not exist
	 */
	public Double getFloat( Term variable ) {
		Double r = this.floatValues.get(variable);
		if ( r != null ) {
			return r.doubleValue();
		} else {
			Long rLong = this.intValues.get(variable);
			if ( rLong != null )
				return rLong.doubleValue();
		}
		return null;
	}
	
	/**
	 * Get earliest start time (EST) of an interval.
	 * @param interval
	 * @return EST
	 */
	public long getEST( Term interval ) {
		try {
			return this.intervals.get(interval)[0];
		} catch ( NullPointerException e ) {
			System.err.println(String.format("Interval %s not found in value lookup! It might have been added after the last temporal propagation.", interval.toString()));
			throw e;
		}
	}
	/**
	 * Get latest start time (LST) of an interval.
	 * @param interval
	 * @return LST
	 */
	public long getLST( Term interval ) {
		try {
			return this.intervals.get(interval)[1];
		} catch ( NullPointerException e ) {
			System.err.println(String.format("Interval %s not found in value lookup! It might have been added after the last temporal propagation.", interval.toString()));
			throw e;
		}
	}
	/**
	 * Get earliest end time (EET) of an interval.
	 * @param interval
	 * @return EET
	 */
	public long getEET( Term interval ) {
		try {
			return this.intervals.get(interval)[2];
		} catch ( NullPointerException e ) {
			System.err.println(String.format("Interval %s not found in value lookup! It might have been added after the last temporal propagation.", interval.toString()));
			throw e;
		}
	}
	/**
	 * Get latest end time (LET) of an interval.
	 * @param interval
	 * @return LET
	 */
	public long getLET( Term interval ) {
		try {
			return this.intervals.get(interval)[3];
		} catch ( NullPointerException e ) {
			System.err.println(String.format("Interval %s not found in value lookup! It might have been added after the last temporal propagation.", interval.toString()));
			throw e;
		}
	}
	
	/**
	 * Get bounds as long array
	 * @param interval 
	 * @return bounds array
	 */
	public long[] getBoundsArray( Term interval ) {
		long[] r = new long[4];
		r[0] = this.intervals.get(interval)[0];
		r[1] = this.intervals.get(interval)[1];
		r[2] = this.intervals.get(interval)[2];
		r[3] = this.intervals.get(interval)[3];
		return r;
	}
	

	
	/**
	 * Add an integer value.
	 * @param variable
	 * @param value
	 */
	public void putInt( Term variable, Long value ) {
		this.intValues.put(variable, value);
	}
	/**
	 * Add a float value.
	 * @param variable
	 * @param value
	 */
	public void putFloat( Term variable, double value ) {
		this.floatValues.put(variable, value);
	}
	
	/**
	 * Add an interval.
	 * @param intervalTerm term representing interval 
	 * @param bounds bounds of the interval
	 *  
	 */
	public void putInterval( Term intervalTerm, Long[] bounds ) {
		this.intervals.put(intervalTerm, bounds);
	}
	
	/**
	 * Check if variable is known.
	 * @param variable
	 * @return <code>true</code> if variable is known, <code>false</code> otherwise
	 */
	public boolean hasVariable( Term variable ) {
		return intValues.containsKey(variable) || floatValues.containsKey(variable);
	}
	
	/**
	 * Check if integer variable is known.
	 * @param variable
	 * @return <code>true</code> if variable is known, <code>false</code> otherwise
	 */
	public boolean hasIntVariable( Term variable ) {
		return intValues.containsKey(variable);
	}
	
	/**
	 * Check if float variable is known.
	 * @param variable
	 * @return <code>true</code> if variable is known, <code>false</code> otherwise
	 */
	public boolean hasFloatVariable( Term variable ) {
		return floatValues.containsKey(variable);
	}
	
	/**
	 * Check if interval is known.
	 * @param interval
	 * @return <code>true</code> if interval is known, <code>false</code> otherwise
	 */
	public boolean hasInterval( Term interval ) {
		return intervals.containsKey(interval);
	}
	
	@Override
	public ValueLookup copy() {
		ValueLookup copy = new ValueLookup();
		for ( Term k : this.intValues.keySet() ) {
			copy.putInt(k, intValues.get(k));
		}
		for ( Term k : this.floatValues.keySet() ) {
			copy.putFloat(k, floatValues.get(k));
		}
		for ( Term k : this.intervals.keySet() ) {
			copy.putInterval(k, this.intervals.get(k));
		}
		return copy;
	}
	
	@Override
	public boolean isUnique() { return true; }
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		//TODO: Should this change?
	}
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof ValueLookup ) {
			ValueLookup oTI = (ValueLookup)o;
			return oTI.intValues.equals(this.intValues); 
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.intValues.hashCode();
	}
	
	@Override
	public String toString() {	
		List<List<String>> table = new ArrayList<List<String>>();
		
		int maxLenType = 8;
		int maxLenKey = 8;
		int maxLenVal = 5;
		int maxLenAfterDecimalPoint = 0;
		for ( Term k : this.intValues.keySet() ) {
			String strKey = k.toString(); 
			String strVal = intValues.get(k).toString();
			
			if ( strKey.length() > maxLenKey ) {
				maxLenKey = strKey.length();
			}	
			if ( strVal.length() > maxLenVal ) {
				maxLenVal = strVal.length();
			}	
			
			List<String> row = new ArrayList<String>();
			row.add("int");
			row.add(strKey);
			row.add(strVal);
			table.add(row);
		}
		for ( Term k : this.floatValues.keySet() ) {
			String strKey = k.toString(); 
			String strVal = floatValues.get(k).toString();
			
			if ( strKey.length() > maxLenKey ) {
				maxLenKey = strKey.length();
			}	
			
			String[] tmp = strVal.split("\\.");
			
			if ( tmp[0].length() > maxLenVal ) {
				maxLenVal = tmp[0].length();
			}	
			if ( tmp[1].length() > maxLenAfterDecimalPoint ) {
				maxLenAfterDecimalPoint = tmp[1].length();
			}	
			
			List<String> row = new ArrayList<String>();
			row.add("float");
			row.add(strKey);
			row.add(strVal);
			table.add(row);
		}
		for ( Term k : this.intervals.keySet() ) {
			String strKey = k.toString(); 
			
			String strVal = "[" + intervals.get(k)[0] + " " + intervals.get(k)[1] + "] [" + intervals.get(k)[2] + " " + intervals.get(k)[3] + "]";
			
			if ( strKey.length() > maxLenKey ) {
				maxLenKey = strKey.length();
			}	
			if ( strVal.length() > maxLenVal ) {
				maxLenVal = strVal.length();
			}	
			
			List<String> row = new ArrayList<String>();
			row.add("interval");
			row.add(strKey);
			row.add(strVal);
			table.add(row);
		}
		maxLenAfterDecimalPoint += 1;
		
		StringBuilder sb = new StringBuilder();
		sb.append(";; Computed value lookup table:");
		
		sb.append("\n;; | ");
		sb.append(addSpace("Type", maxLenType, true));
		sb.append(" | ");
		sb.append(addSpace("Variable", maxLenKey, true));
		sb.append(" | ");
		sb.append(addSpace("Value", maxLenVal+maxLenAfterDecimalPoint, true));
		sb.append(" |");
		
		sb.append("\n;; ");
		for ( int i = 0; i < (maxLenKey + maxLenVal + maxLenAfterDecimalPoint + maxLenType + 10) ; i++ ) {
			sb.append("=");
		}
				
		for ( List<String> row : table ) {
			sb.append("\n;; | ");
			sb.append(addSpace(row.get(0), maxLenType, true));
			sb.append(" | ");
			sb.append(addSpace(row.get(1), maxLenKey, true));
			sb.append(" | ");
			if ( !row.get(2).contains(".") ) {
				sb.append(addSpace(addSpace(row.get(2), maxLenVal, false), maxLenAfterDecimalPoint+maxLenVal, true));	
			} else {
				sb.append(addSpace(row.get(2).split("\\.")[0], maxLenVal, false));
				sb.append(".");
				sb.append(addSpace(row.get(2).split("\\.")[1], maxLenAfterDecimalPoint-1, true));
			}
			
			sb.append(" |");
		}

		return sb.toString();
	}
	
	private String addSpace( String s, int maxLen, boolean addRight ) {
		while ( s.length() < maxLen ) {
			if ( addRight ) {
				s = s + " ";
			} else {
				s = " " + s;
			}
		}
		return s;
	}
}
