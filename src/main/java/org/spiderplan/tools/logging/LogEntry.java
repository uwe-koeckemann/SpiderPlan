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
package org.spiderplan.tools.logging;

/**
 * Simple class representing one log entry with source, message and level (0 being most important)
 * 
 * @author Uwe Köckemann
 *
 */
public class LogEntry {
	String source;
	String message;
	int level;	
	int depth;
	long ID;
	
	public static boolean addID2String = false;
	private static long nextID = 0;
	
	public LogEntry( String source, String message, int level ) {
		this.source = source;
		this.message = message;
		this.level = level;		
		this.depth = 0;
		this.ID = nextID++;		
	}
	
	public LogEntry( String source, String message, int level, int depth ) {
		this.source = source;
		this.message = message;
		this.level = level;
		this.depth = depth;
		this.ID = nextID++;
	}
	
	@Override
	public String toString() {
		String tabs = getTabbing( depth );
		String IDstr = "";
		if ( addID2String ) {
			 IDstr = "[" + addLeadingZeros(ID) + "]";
		}
		
		return IDstr+tabs+"[" + source + "] " + message.replace("\n", "\n"+IDstr+tabs+"[" + source + "] ");
		
	}
	
	private String getTabbing( int n ) {
		String r = "";
		for ( int i = 0 ; i < n ; i++ ) {
			r += "|    ";
		}
		return r;
	}
	
	private String addLeadingZeros( long ID ) {
		String r = String.valueOf(ID);
		while ( r.length() < 10 ) {
			r = "0" + r;
		}
		return r;
	}
	
	public int getLevel() {
		return level;
	}
	
	public static long getLastID() {
		return nextID-1;
	}
}
