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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.interfaces.Unique;
import org.spiderplan.representation.logic.Term;

/**
 * Reference between internal time points and real date-time.
 * 
 * @author Uwe Köckemann
 *
 */
public class DateTimeReference extends Expression implements Unique {
	
	String timeFormatStr;
	String t0Str;
	
	long d_days;
	long d_hours;
	long d_minutes;
	long d_seconds;
	long d_milliseconds;
	
	DateFormat timeFormat;
	Date t0;
	DateFormat deltaFormat;
	long total_d_milliseconds;

	/**
	 * Create new time reference.
	 * 
	 * @param timeFormatStr
	 * @param t0Str
	 * @param d_days 
	 * @param d_hours 
	 * @param d_minutes 
	 * @param d_seconds 
	 * @param d_milliseconds 
	 */
	public DateTimeReference( String timeFormatStr, String t0Str, long d_days, long d_hours, long d_minutes, long d_seconds, long d_milliseconds ) {
		super(ExpressionTypes.Temporal);
		try {
			this.timeFormatStr = timeFormatStr;
			this.t0Str = t0Str;

			this.timeFormat = new SimpleDateFormat(timeFormatStr);
			this.t0 = this.timeFormat.parse(t0Str);
			
			this.d_days = d_days;
			this.d_hours = d_hours;
			this.d_minutes = d_minutes;
			this.d_seconds = d_seconds;
			this.d_milliseconds = d_milliseconds;
			
			total_d_milliseconds = TimeUnit.DAYS.toMillis(d_days);
			total_d_milliseconds += TimeUnit.HOURS.toMillis(d_hours);
			total_d_milliseconds += TimeUnit.MINUTES.toMillis(d_minutes);
			total_d_milliseconds += TimeUnit.SECONDS.toMillis(d_seconds);
			total_d_milliseconds += d_milliseconds;			
		} catch (ParseException e) {
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
	
	/**
	 * Get the time in miliseconds that passes between two timepoints.
	 * @return time delta in miliseconds
	 */
	public long getTotalDelta() {
		return total_d_milliseconds;
	}
	
	/**
	 * Convert internal time point to date time in milliseconds.
	 * 
	 * @param tInternal Internal time point (e.g., from temporal propagation)
	 * @return time point that corresponds to real date time
	 */
	public long internal2external( long tInternal ) {

		return t0.getTime() + tInternal * total_d_milliseconds;
	}
	
	/**
	 * Convert real date time (in milliseconds) to internal time point.
	 * 
	 * @param tReal current real time in milliseconds
	 * @return internal time step that corresponds to tReal
	 */
	public long external2internal( long tReal ) {
		return (tReal-t0.getTime())/total_d_milliseconds;
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
		StringBuilder sb = new StringBuilder();
		sb.append("(date-time-format ");
		
		sb.append(this.timeFormatStr);
		sb.append(" ");
		sb.append(this.t0Str);
		sb.append(" ");
		sb.append(this.d_days);
		sb.append(" ");
		sb.append(this.d_hours);
		sb.append(" ");
		sb.append(this.d_minutes);
		sb.append(" ");
		sb.append(this.d_seconds);
		sb.append(" ");
		sb.append(this.d_milliseconds);
		sb.append(")");
		return sb.toString();
	}
}
