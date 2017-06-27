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
package org.spiderplan.tools.statistics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple class for collecting information for experiments.
 * 
 * @author Uwe Köckemann
 *
 */
public class Statistics {
	
	private static int numEntries = 0;

	private static Map<String,ArrayList<Long>> longValues = new HashMap<String, ArrayList<Long>>();
	private static Map<String,ArrayList<Double>> doubleValues = new HashMap<String, ArrayList<Double>>();
	private static Map<String,Long> longValue = new HashMap<String, Long>();
	private static Map<String,Double> doubleValue = new HashMap<String, Double>();
	private static Map<String,Long> counters = new HashMap<String, Long>();
	private static Map<String,String> strings = new HashMap<String, String>();
	
	private static ArrayList<Map<String,ArrayList<Long>>> allLongValues = new ArrayList<Map<String,ArrayList<Long>>>();
	private static ArrayList<Map<String,ArrayList<Double>>> allDoubleValues = new ArrayList<Map<String,ArrayList<Double>>>();
	private static ArrayList<Map<String,Long>> allLongValue = new ArrayList<Map<String,Long>>();
	private static ArrayList<Map<String,Double>> allDoubleValue = new ArrayList<Map<String,Double>>();
	private static ArrayList<Map<String,Long>> allCounters = new ArrayList<Map<String,Long>>();
	private static ArrayList<Map<String,String>> allStrings = new ArrayList<Map<String,String>>();
	
	private static Map<String,String> renameRules = new HashMap<String, String>();
	
	private Statistics() {}
	
	/**
	 * Add a long value
	 * @param key
	 * @param val
	 */
	public static void addLong( String key, Long val ) {
		if ( !longValues.containsKey(key) ) {
			longValues.put(key,new ArrayList<Long>());
		}
		longValues.get(key).add(val);
	}
	
	/**
	 * Add a double value
	 * @param key
	 * @param val
	 */
	public static void addDouble( String key, Double val ) {
		if ( !doubleValues.containsKey(key) ) {
			doubleValues.put(key,new ArrayList<Double>());
		}
		doubleValues.get(key).add(val);
	}
	
	/**
	 * Create a new counter
	 * @param key
	 */
	public static void creatCounter( String key ) {
		counters.put(key,Long.valueOf(0));
	}
	
	/**
	 * Increment a counter
	 * @param key
	 */
	public static void increment( String key ) {
		if ( !counters.containsKey(key) ) {
			counters.put(key,Long.valueOf(1));
		} else {
			counters.put(key, Long.valueOf(counters.get(key).longValue()+1));
		}
	}
	/**
	 * Set a long value
	 * @param key
	 * @param val
	 */
	public static void setLong( String key, Long val ) {
		longValue.put(key,val);
	}
	/**
	 * Set a double value.
	 * @param key
	 * @param val
	 */
	public static void setDouble( String key, Double val ) {
		doubleValue.put(key,val);
	}
	/**
	 * Add to existing long value.
	 * @param key
	 * @param val
	 */
	public static void addToLong( String key, Long val ) {
		if ( longValue.containsKey(key) ) {
			longValue.put(key,Long.valueOf(longValue.get(key).longValue()+val));
		} else {
			longValue.put(key,val);
		}
	}
	/**
	 * Add to existing double value.
	 * @param key
	 * @param val
	 */
	public static void addToDouble( String key, Double val ) {
		if ( doubleValue.containsKey(key) ) {
			doubleValue.put(key,Double.valueOf(doubleValue.get(key).doubleValue()+val));
		} else {
			doubleValue.put(key,val);
		}
	}
	
	/**
	 * Set string value.
	 * @param key
	 * @param val
	 */
	public static void setString( String key, String val ) {
		strings.put(key,val);
	}
	
	/**
	 * Get all long values from a list.
	 * @param key
	 * @return list of long values
	 */
	public static List<Long> getAllLong( String key ) {
		return longValues.get(key);
	}
	/**
	 * Get all double values from a list.
	 * @param key
	 * @return list of double values
	 */
	public static List<Double> getAllDouble( String key ) {
		return doubleValues.get(key);
	}
	
	/**
	 * Get counter value.
	 * @param key
	 * @return counter value
	 */
	public static Long getCounter( String key ) {
		return counters.get(key);
	}
	
	/**
	 * Get long value
	 * @param key
	 * @return long value
	 */
	public static Long getLong( String key ) {
		return longValue.get(key);
	}
	/**
	 * Get double value.
	 * @param key
	 * @return double value
	 */
	public static Double getDouble( String key ) {
		return doubleValue.get(key);
	}
	/**
	 * Get string value
	 * @param key
	 * @return string value
	 */
	public static String getString( String key ) {
		return strings.get(key);
	}
	
	/**
	 * Will change name of attribute <i>from</i> to <i>to</i>. 
	 * @param from Name of an attribute.
	 * @param to How <i>from</i> should appear in CSV file.
	 */
	public static void renameKey( String from, String to ) {
		renameRules.put(from, to);
	}
	
	/**
	 * Store all data in history and reset all statistics.
	 * Used to keep data inbetween experiments.
	 */
	public static void store() {
		numEntries++;
		
		allLongValues.add(longValues);
		allDoubleValues.add(doubleValues);
		allLongValue.add(longValue);
		allDoubleValue.add(doubleValue);
		allCounters.add(counters);
		allStrings.add(strings);
		
		longValues = new HashMap<String, ArrayList<Long>>();
		doubleValues = new HashMap<String, ArrayList<Double>>();
		longValue = new HashMap<String, Long>();
		doubleValue = new HashMap<String, Double>();
		counters = new HashMap<String, Long>();
		strings = new HashMap<String, String>();
	}
	
	/**
	 * Reset everything.
	 */
	public static void reset() {
		longValues.clear();
		doubleValues.clear();
		longValue.clear();
		doubleValue.clear();
		counters.clear();
		strings.clear();
		
		allLongValues.clear();
		allDoubleValues.clear();
		allLongValue.clear();
		allDoubleValue.clear();
		allCounters.clear();
		allStrings.clear();
		
		renameRules.clear();
		
		numEntries = 0;
	}
	
	/**
	 * All attributes are written to a CSV file. 
	 * @param fName target filename 
	 * @param attOrder order of attributes
	 */
	public static void dumpCSVAllAtts( String fName, List<String> attOrder ) {
		Set<String> allAtts = new HashSet<String>();
		for ( Map<String,?> m : allLongValue ) {
			allAtts.addAll(m.keySet());
		}
		for ( Map<String,?> m : allDoubleValue ) {
			allAtts.addAll(m.keySet());
		}
		for ( Map<String,?> m : allCounters ) {
			allAtts.addAll(m.keySet());
		}
		for ( Map<String,?> m : allStrings ) {
			allAtts.addAll(m.keySet());
		}
		for ( String a : attOrder ) {
			allAtts.remove(a);	
		}
		
		ArrayList<String> sortedAtts = new ArrayList<String>();
		sortedAtts.addAll(allAtts);
		Collections.sort(sortedAtts);
		
		attOrder.addAll(sortedAtts);
		
		ArrayList<String> usedAtts = new ArrayList<String>();
		usedAtts.addAll(attOrder);
		usedAtts.addAll(sortedAtts);
				
		BufferedWriter out = null;
		try  {
		    FileWriter fstream = new FileWriter(fName); //true tells to append data.
		    out = new BufferedWriter(fstream);
		    
		    String line = usedAtts.toString();
		    line = line.substring(1,line.length()-1);
		    
		    out.write(line+"\n");
	    
		    for ( int i = 0 ; i < numEntries ; i++ ) {
		    	ArrayList<String> dataLine = new ArrayList<String>();
		    	for ( String att : usedAtts ) {
		    		dataLine.add(Statistics.getStringValue(att, i));
		    	}
		    	
		    	line = dataLine.toString();
			    line = line.substring(1,line.length()-1);
		    	
		    	out.write(line+"\n");
//		    	System.out.println(line);
		    }
		    
		    
		    out.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
	}
	
	/**
	 * Dump data for some attributes to a comma-separated value (.csv) file.
	 * 
	 * @param fName Target filename
	 * @param attOrder Attributes to include in file
	 */
	public static void dumpCSV( String fName, List<String> attOrder ) {
		
		BufferedWriter out = null;
		try  {
		    FileWriter fstream = new FileWriter(fName); //true tells to append data.
		    out = new BufferedWriter(fstream);
		    
		    ArrayList<String> printedAtts = new ArrayList<String>();
		    
		    for ( int i = 0 ; i < attOrder.size() ; i++ ) {
		    	String to = renameRules.get(attOrder.get(i));
		    	if ( to != null ) {
		    		printedAtts.add(to);
		    	} else {
		    		printedAtts.add(attOrder.get(i));
		    	}
		    }
		    
		    String line = printedAtts.toString();
		    line = line.substring(1,line.length()-1);
		    out.write("# " + line+"\n");
		    
		    for ( int i = 0 ; i < numEntries ; i++ ) {
		    	ArrayList<String> dataLine = new ArrayList<String>();
		    	for ( String att : attOrder ) {
		    		dataLine.add(Statistics.getStringValue(att, i));
		    	}
		    	
		    	line = dataLine.toString();
			    line = line.substring(1,line.length()-1);
		    	
		    	out.write(line+"\n");
//		    	System.out.println(line);
		    }
		    
		    
		    out.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		
	}
	
	private static String getStringValue( String att, int index ) {
		if ( allLongValue.get(index).containsKey(att) ) {
			return allLongValue.get(index).get(att).toString();
		}
		if ( allLongValues.get(index).containsKey(att) ) {
			return allLongValues.get(index).get(att).toString();
		}
		if ( allDoubleValue.get(index).containsKey(att) ) {
			return allDoubleValue.get(index).get(att).toString();
		}
		if ( allDoubleValues.get(index).containsKey(att) ) {
			return allDoubleValues.get(index).get(att).toString();
		}
		if ( allStrings.get(index).containsKey(att) ) {
			return "\"" + allStrings.get(index).get(att).toString() + "\"";
		}
		if ( allCounters.get(index).containsKey(att) ) {
			return allCounters.get(index).get(att).toString();
		}
		return "?";
	}
	
	/**
	 * Get a string representation of all stored data.
	 * @return a string
	 */
	public static String getString() {
		String r = "";
		if ( !counters.isEmpty() ) {
			ArrayList<String> keys = new ArrayList<String>();
			keys.addAll(counters.keySet());
			Collections.sort(keys);
			for ( String key : keys ) {
				r += key + " -> " + counters.get(key) + "\n";
			}
		}
		if ( !longValues.isEmpty() ) {
			ArrayList<String> keys = new ArrayList<String>();
			keys.addAll(longValues.keySet());
			Collections.sort(keys);
			for ( String key : keys ) {
				r += key + " -> " + longValues.get(key) + "\n";
			}
		}
		if ( !doubleValues.isEmpty() ) {
			ArrayList<String> keys = new ArrayList<String>();
			keys.addAll(doubleValues.keySet());
			Collections.sort(keys);
			for ( String key : keys ) {
				r += key + " -> " + doubleValues.get(key) + "\n";
			}
		}
		if ( !longValue.isEmpty() ) {
			ArrayList<String> keys = new ArrayList<String>();
			keys.addAll(longValue.keySet());
			Collections.sort(keys);
			for ( String key : keys ) {
				r += key + " -> " + longValue.get(key) + "\n";
			}
		}
		if ( !doubleValue.isEmpty() ) {
			ArrayList<String> keys = new ArrayList<String>();
			keys.addAll(doubleValue.keySet());
			Collections.sort(keys);
			for ( String key : keys ) {
				r += key + " -> " + doubleValue.get(key) + "\n";
			}
		}
		return r;
	}
}
