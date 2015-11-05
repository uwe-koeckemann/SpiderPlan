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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

/**
 * Simple Logger class that keeps history and allows to set landmarks. 
 * Log history can be accessed based on landmarks.
 * Sources are registered with a name and a maximum message level that this source will output. 
 * 
 * To use this:
 * 1) Call Logger.init()
 * 2) Create source(s) 
 * 		a) add PrintStreams if not using GUI and live output is needed
 * 3) Optional: Start GUI(s) with Logger.draw() (for multiple GUIs just call multiple times)
 * 4) Create logs/landmarks
 * 
 * Use keepAllLogs to store messages regardless of their level (to access them later).
 * <i>broadcast</i> is a default source that will forward messages to all other streams.  
 * Log level 0 is highest priority and should contain least number of {@link LogEntry}s.
 *   
 * @author Uwe Köckemann
 */
public class Logger {

	public static int depth = 0;
	
	private static Vector<Vector<LogEntry>> history = new Vector<Vector<LogEntry>>();
	private static HashMap<String,Integer> logLevelMap = new HashMap<String, Integer>(); 
	
	private static int currentLandmark = 0;
	private static int defaultLvl = 10;
	private static boolean isInitialized = false;
	
	protected static HashMap<String,Vector<PrintStream>> outStreamMap = new HashMap<String, Vector<PrintStream>>();
		
	public static boolean keepAllLogs = true;
	public static boolean stop = false;
	
	private Logger() {}
	
	/**
	 * Initialize logger
	 */
	public static void init() {
		if ( !isInitialized ) {
			isInitialized = true;
			history.add( new Vector<LogEntry>() );
			Logger.registerSource("broadcast", 5);			
		}
	}
	
	public static void reset() {
		history = new Vector<Vector<LogEntry>>();
		logLevelMap = new HashMap<String, Integer>(); 
		outStreamMap = new HashMap<String, Vector<PrintStream>>();
		keepAllLogs = true;
		currentLandmark = 0;
		isInitialized = false;
		stop = false;
		
		init();
	}
	
	/**
	 * Register a source
	 * @param sourceName	Name of source
	 * @param maxlevel		Maximum level of source that is printed/stored.
	 */
	public static void registerSource( String sourceName, int maxlevel ) {
		init();
		if ( !logLevelMap.containsKey(sourceName) ) {
			logLevelMap.put(sourceName, new Integer(maxlevel));
			outStreamMap.put(sourceName, new Vector<PrintStream>());	
		}		
	}
		
//	/**
//	 * Create log from a source.
//	 * @param source
//	 * @param msg
//	 * @param level Level of this message. Lower levels are considered more important.
//	 */
//	public static void msg( String source, String msg, int level ) {
//		init();
//		if ( !outStreamMap.containsKey(source) ) {
//			registerSource(source, level);
//		}
//		if ( !Logger.stop ) {
//			try {
//				boolean hasMinLevel = level <= logLevelMap.get(source);
//				if ( keepAllLogs || hasMinLevel ) {
//					LogEntry le = new LogEntry(source, msg, level);
//					history.get(currentLandmark).add(le);
//					
//					if ( hasMinLevel ) {
//						for ( PrintStream  out : outStreamMap.get(source) ) {
//							out.println(le.toString());
//						}
//					}
//				}
//			} catch ( NullPointerException e ) {
//				System.err.println("Unknown source: " + source);
//				e.printStackTrace();
//			}
//		}
//	}
//	
	/**
	 * Create log from a source.
	 * @param source
	 * @param msg
	 * @param level Level of this message. Lower levels are considered more important.
	 * @param depth Depth of the tabbing of this message
	 */
	public static void msg( String source, String msg, int level ) {
		init();
		if ( !outStreamMap.containsKey(source) ) {
			registerSource(source, level);
		}
		if ( !Logger.stop ) {
			try {
				boolean hasMinLevel = level <= logLevelMap.get(source);
				if ( keepAllLogs || hasMinLevel ) {
					LogEntry le = new LogEntry(source, msg, level, depth);
					history.get(currentLandmark).add(le);
					if ( hasMinLevel ) {
						for ( PrintStream  out : outStreamMap.get(source) ) {
							out.println(le.toString());
						}
					}
				}
			} catch ( NullPointerException e ) {
				System.err.println("Unknown source: " + source);
				e.printStackTrace();
			}
		}
	}
		
	/**
	 * Create a landmark from source and broadcast it to all streams.
	 * @param source
	 */
	public static void landmarkMsg( String source  ) {
		history.add( new Vector<LogEntry>() );
		currentLandmark++;
		msg("broadcast", "================================" , 0);
		msg("broadcast", "= Landmark " + currentLandmark + " (by " +  source + ")", 0);
		msg("broadcast", "================================" , 0);
	}
	
	/**
	 * Put one landmark on a {@link PrintStream} 
	 * @param landmarkID
	 * @param ps
	 */
	public static void streamLandmark( int landmarkID, PrintStream ps ) {
		for ( LogEntry le : history.get(landmarkID)) {
			if ( le.level < logLevelMap.get(le.source) ) {
				ps.println(le.toString());
			}
		}
	}
	
	/**
	 * Put sequence of landmarks on {@link PrintStream}
	 * @param fromLandmarkID
	 * @param toLandmarkID
	 * @param ps
	 */
	public static void streamLandmarks( int fromLandmarkID, int toLandmarkID, PrintStream ps ) {
		for ( int i = fromLandmarkID; i <= toLandmarkID; i++ ) {
			for ( LogEntry le : history.get(i) ) {
				if ( le.level < logLevelMap.get(le.source) ) {
					ps.println(le.toString());
				}
			}
		}
	}

	/**
	 * Put sequence of landmarks on {@link PrintStream} and override logLevel
	 * @param landmarkID
	 * @param ps
	 * @param customLevel
	 */
	public static void streamLandmarks( int fromLandmarkID, int toLandmarkID, PrintStream ps, int customLevel ) {
		for ( int i = fromLandmarkID; i <= toLandmarkID; i++ ) {
			if ( i >= history.size() ) {
				break;
			}
			for ( LogEntry le : history.get(i) ) {
				if ( le.level < customLevel ) {
					ps.println(le.toString());
				}
			}
		}
	}
	
	/**
	 * Put sequence of landmarks from list of sources on {@link PrintStream} overriding logLevel and printing only logs that contain given {@link String}
	 * @param selectedSources
	 * @param fromLandmarkID
	 * @param toLandmarkID
	 * @param ps
	 * @param customLevel
	 * @param containsStr
	 */
	public static void streamLandmarks( ArrayList<String> selectedSources, int fromLandmarkID, int toLandmarkID, PrintStream ps, int customLevel, String containsStr ) {
		for ( int i = fromLandmarkID; i <= toLandmarkID; i++ ) {
			if ( i >= history.size() ) {
				break;
			}
			for ( LogEntry le : history.get(i) ) {
				String leStr = le.toString();
				if ( (selectedSources.contains(le.source) || le.source.equals("broadcast")) && le.level <= customLevel && ( containsStr.equals("") || leStr.contains(containsStr) ) ) {
					ps.println(le.toString());
				}
			}
		}
	}

	/**
	 * Add a {@link PrintStream} to a source (for multiple outputs)
	 * @param sourceName
	 * @param ps
	 */
	public static void addPrintStream( String sourceName, PrintStream ps ) {
		if ( !outStreamMap.containsKey(sourceName) ) {
			Logger.registerSource(sourceName, defaultLvl);
		}
		outStreamMap.get(sourceName).add(ps);
		if ( !outStreamMap.get("broadcast").contains(ps) ) {
			outStreamMap.get("broadcast").add(ps);
		}
	}
	
	/**
	 * Get all registered sources
	 * @return
	 */
	public static ArrayList<String> getAllSources() {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll( logLevelMap.keySet() );
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Changes the default message level used in cases where sources
	 * are created automatically (i.e. they are used without being 
	 * explicitly created.
	 * @param lvl
	 */
	public void setDefaultLvl( int lvl ) {
		Logger.defaultLvl = lvl;
	}
	
	/**
	 * Create a GUI frame that streams all available sources 
	 */
	public static void drawWithName( String name ) {
		ArrayList<String> sources = getAllSources();
		sources.remove("broadcast");
		new LoggerFrame( name, sources );
	}
	
	/**
	 * Create a GUI frame that streams all available sources 
	 */
	public static void draw() {
		ArrayList<String> sources = getAllSources();
		sources.remove("broadcast");
		new LoggerFrame( sources );
	}
	
	/**
	 * Create a GUI frame that streams sources from CSV string
	 * @param source
	 */
	public static void draw( String source ) {
		ArrayList<String> sources = new ArrayList<String>();
		String[] sourceInput = source.split(",");
		for ( String s : sourceInput ) {
			sources.add(s);
		}
		new LoggerFrame( sources );
	}
	
	/**
	 * Create a GUI frame that streams all sources in list
	 * @param sources
	 */
	public static void draw( ArrayList<String> sources ) {
		new LoggerFrame( sources );
	}
	

}
