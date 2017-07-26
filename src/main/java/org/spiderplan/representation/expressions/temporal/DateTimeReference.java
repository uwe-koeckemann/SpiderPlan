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
package org.spiderplan.representation.expressions.temporal;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.interfaces.Unique;
import org.spiderplan.representation.logic.Term;

/**
 * Reference between internal time points and real date-time.
 * 
 * What this should do:
 * - Allow to take timestamps and convert them to internal time points
 * - Convert internal time points to timestamps
 * - Convert between internal and external time points
 * - Do offset calculations
 * - Convert timestamp and timespan terms to external time points 
 * 
 * @author Uwe Köckemann
 *
 */
public class DateTimeReference extends Expression implements Unique {
	
	Term dtRefTerm;
	
	Date t0; // Contains ms since 1970-01-01
	
	String t0Str;
	String dateFormatStr;
	
	
	DateFormat dateFormat;
	DateFormat deltaFormat;
	
	long d_days = 0;
	long d_hours = 0;
	long d_minutes = 0;
	long d_seconds = 0;
	long d_milliseconds = 0;
		
	long total_d_milliseconds; // How many milliseconds pass between internal time points
	
	private static List<String> YEAR = new ArrayList<String>();
	private static List<String> MONTH = new ArrayList<String>();
	private static List<String> DAY = new ArrayList<String>();
	private static List<String> HOUR = new ArrayList<String>();
	private static List<String> MINUTE = new ArrayList<String>();
	private static List<String> SECOND = new ArrayList<String>();
	private static List<String> MILLISECOND = new ArrayList<String>();
	{
		YEAR.add("y");
		YEAR.add("yyyy");
		YEAR.add("year");
		MONTH.add("M");
		MONTH.add("MM");
		MONTH.add("month");
		DAY.add("d");
		DAY.add("dd");
		DAY.add("day");
		DAY.add("days");
		HOUR.add("hour");
		HOUR.add("hours");
		HOUR.add("h");
		HOUR.add("hh");
		MINUTE.add("m");
		MINUTE.add("mm");
		MINUTE.add("min");		
		MINUTE.add("minute");
		MINUTE.add("minutes");
		SECOND.add("s");
		SECOND.add("ss");
		SECOND.add("second");
		SECOND.add("seconds");
		MILLISECOND.add("f");
		MILLISECOND.add("ms");
		MILLISECOND.add("fff");
		MILLISECOND.add("millisecond");
		MILLISECOND.add("milliseconds");
	}
	
	/**
	 * @param t
	 */
	public DateTimeReference( Term t ) {
		super(ExpressionTypes.Temporal);
		dtRefTerm = t;
		dateFormatStr = t.getArg(0).toString();

		Term initialDateTime = t.getArg(1);
		
		if ( initialDateTime.getArg(0).equals(Term.createConstant("now"))) {
			t0 = new Date();
		} else {
			t0 = this.term2date(initialDateTime);
		}
	
		try {
			Term deltaTimespan = t.getArg(2);
			for ( int i = 0 ; i < deltaTimespan.getNumArgs() ; i++ ) {
				Term arg = deltaTimespan.getArg(i);
				String name = arg.getName();
				long value = Long.valueOf(arg.getArg(0).toString());
				
				if ( DAY.contains(name)) {
					this.d_days = value;
				} else if ( HOUR.contains(name) ) {
					this.d_hours = value;
				} else if ( MINUTE.contains(name) ) {
					this.d_minutes = value;
				} else if ( SECOND.contains(name) ) {
					this.d_seconds = value;
				} else if ( MILLISECOND.contains(name) ) {
					this.d_milliseconds = value;
				}
			}
			
			total_d_milliseconds = TimeUnit.DAYS.toMillis(d_days);
			total_d_milliseconds += TimeUnit.HOURS.toMillis(d_hours);
			total_d_milliseconds += TimeUnit.MINUTES.toMillis(d_minutes);
			total_d_milliseconds += TimeUnit.SECONDS.toMillis(d_seconds);
			total_d_milliseconds += d_milliseconds;			
		} catch ( NumberFormatException e ) {
			System.err.println("Last 5 arguments of " +t.toString()+ " must be long values. Exiting...");
			e.printStackTrace();
			System.exit(1);
		} 
	}
	
	/**
	 * Get the earliest time in the reference. 
	 * @return t0
	 */
	public Date getTime0() {
		return t0;
	}
		
//	private long internalDateTime2external( long tInternal ) {
//		return t0.getTime() + tInternal * total_d_milliseconds;
//	}	
	
	
	/**
	 * Convert time in millis since 1970-01-01 to internal planner time.
	 * @param tReal
	 * @return planning time step
	 */
	public long externalDateTime2internal( long tReal ) {
		return (tReal-t0.getTime())/total_d_milliseconds;
	}	
	private long externalTimeSpan2internal( long tReal ) {
		return tReal/total_d_milliseconds;
	}
	
	public String internalDateTimeToString( long tInternal ) {
		long external = this.t0.getTime() + tInternal*total_d_milliseconds;
		Date d = new Date(external);
		return d.toString();
	}
		
	/**
	 * Convert a term to internal time point. This term 
	 * may describe a date-time (datetime (y 2017) (M 7) (d 20) (h 10) (m 30) (s 20) (f 100))
	 * or a time span (timespan (d 10)) or an offset (offset (datetime (y 2017) (M 7) (d 10)) (timespan (d 1))).
	 * @param t A term as described abode
	 * @return internal time point representation of the planner
	 */
	public long term2internal( Term t ) {
		if ( t.getName().equals("datetime") ) {
			return externalDateTime2internal(term2date(t).getTime());
		} else if ( t.getName().equals("timespan") ) {
			return externalTimeSpan2internal(term2delta(t));
		} else if ( t.getName().equals("offset") ) {
			return term2internal(t.getArg(0)) + term2internal(t.getArg(1));
		} else {
			throw new IllegalArgumentException("Illegal term for time reference: " + t + " use (datetime (year 2017) (month 1) (day 1) ...), (timespan (days 1) (hours ...)), or (offset (datetime ...) (timespan ...))");
		}
	}
	
	private Date term2date( Term dtTerm ) {
		Calendar c = Calendar.getInstance();
		
		if ( dtTerm.getArg(0).toString().equals("now") ) {
			return Date.from(c.toInstant());
		}
		
		c.clear();
		
		for ( int i = 0 ; i < dtTerm.getNumArgs() ; i++ ) {
			Term arg = dtTerm.getArg(i);
			String name = arg.getName();
			int value = Integer.valueOf(arg.getArg(0).toString());
			
			
			if ( YEAR.contains(name) ) {
				c.set(Calendar.YEAR, value);
			} else if ( MONTH.contains(name) ) {
				c.set(Calendar.MONTH, value-1); // Calendar starts counting month at 0
			} else if ( DAY.contains(name) ) {
				c.set(Calendar.DAY_OF_MONTH, value);
			} else if ( HOUR.contains(name) ) {
				c.set(Calendar.HOUR_OF_DAY, value);
			} else if ( MINUTE.contains(name) ) {
				c.set(Calendar.MINUTE, value);
			} else if ( SECOND.contains(name) ) {
				c.set(Calendar.SECOND, value);
			} else if ( MILLISECOND.contains(name) ) {
				c.set(Calendar.MILLISECOND, value);
			}
		}
				
		Date r = Date.from(c.toInstant());
		
		return r;
	}
	
	private long term2delta( Term deltaTerm ) {
		long total_d_ms = 0;
		
		for ( int i = 0 ; i < deltaTerm.getNumArgs() ; i++ ) {
			Term arg = deltaTerm.getArg(i);
			String name = arg.getName();
			int value = Integer.valueOf(arg.getArg(0).toString());
						
			if ( DAY.contains(name) ) {
				total_d_ms += TimeUnit.DAYS.toMillis(value);
			} else if ( HOUR.contains(name) ) {
				total_d_ms += TimeUnit.HOURS.toMillis(value);
			} else if ( MINUTE.contains(name) ) {
				total_d_ms += TimeUnit.MINUTES.toMillis(value);
			} else if ( SECOND.contains(name) ) {
				total_d_ms += TimeUnit.SECONDS.toMillis(value);
			} else if ( MILLISECOND.contains(name) ) {
				total_d_ms += value;
			}
		}
					
		return total_d_ms;
	}
	
	@Override
	public boolean isUnique() {
		return true;
	}
	
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex); //TODO: Maybe this should change?
	}
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof DateTimeReference ) {
			DateTimeReference pI = (DateTimeReference)o;
			return this.toString().equals(pI.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return dtRefTerm.toString();
	}
}
