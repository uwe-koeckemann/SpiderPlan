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
package org.spiderplan.modules.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.spiderplan.modules.solvers.Module;

/**
 * Manages the configuration of each {@link Module}.
 *  
 * @author Uwe Köckemann
 *
 */
public class ConfigurationManager {
	private ArrayList<String> modules = new ArrayList<String>();
	
	private HashMap<String,HashMap<String,String>> moduleConfigs = new HashMap<String, HashMap<String,String>>();
	private HashMap<String,String> options = new HashMap<String, String>();
	
	/**
	 * Create a new {@link ConfigurationManager}
	 */
	public ConfigurationManager() { }
	
	/**
	 * Create a new {@link ConfigurationManager}
	 * @param mNames Comma-separated {@link String} providing {@link Module} names initially added
	 */
	public ConfigurationManager( String mNames ) {
		for ( String mName : mNames.split(",")) {
			modules.add(mName);
			moduleConfigs.put(mName, new HashMap<String, String>());
		}
	}
	
	/**
	 * Add a {@link Module}
	 * @param mName Name of the {@link Module} to be added.
	 */
	public void add( String mName ) {
		modules.add( mName );
		moduleConfigs.put(mName, new HashMap<String,String>());
	}
	/**
	 * Set a parameter of a {@link Module}.
	 * @param mName {@link Module} name
	 * @param att Attribute of parameter 
	 * @param val Value of parameter
	 */
	public void set( String mName, String att, String val ) {
		if ( !moduleConfigs.containsKey(mName) ) {
			moduleConfigs.put(mName, new HashMap<String, String>());
		}
		moduleConfigs.get(mName).put(att, val);
	}
	/**
	 * Set a global parameter
	 * @param att Attribute of parameter 
	 * @param val Value of parameter
	 */
	public void setOptions( String att, String val ) {
		options.put(att, val);
	}
	
	/**
	 * Change setting globally and for all modules.
	 * 
	 * @param att Attribute name
	 * @param val Value name
	 */
	public void overrideOption( String att, String val ) {
		for ( String moduleName : moduleConfigs.keySet() ) {
			moduleConfigs.get(moduleName).put(att, val);
		}
		options.put(att, val);
	}
	
	/**
	 * Check if parameter is set by a {@link Module}
	 * @param mName {@link Module} name
	 * @param att Attribute of parameter 
	 * @return <code>true</code> if parameter is set my {@link Module}, <code>false</code> otherwise,
	 */
	public boolean hasAttribute( String mName, String att ) {
		if ( !this.moduleConfigs.containsKey(mName) ) {
			throw new IllegalArgumentException("Module with name: "+ mName + " does not exist. Check planner definition.");
		}
		return this.moduleConfigs.get(mName).containsKey(att) || this.options.containsKey(att);
	}
	
	/**
	 * Get {@link String} value of a parameter of a {@link Module}
	 * @param mName {@link Module} name
	 * @param att Attribute of parameter 
	 * @return Value of parameter
	 */
	public String getString( String mName, String att ) {
		if ( !moduleConfigs.containsKey(mName) ) {
			throw new IllegalArgumentException("Module " + mName + " does not exist.");
		}
		
		if ( moduleConfigs.get(mName).containsKey(att) ) {
			return moduleConfigs.get(mName).get(att);
		} else if ( options.containsKey(att) ) {
			return options.get(att);
		} else {
			throw new IllegalArgumentException("Module Attribute " + mName + "." + att + " does not exist.");
		}
		
	}
	/**
	 * Get {@link List} of {@link String} of a parameter of a {@link Module}
	 * @param mName {@link Module} name
	 * @param att Attribute of parameter 
	 * @return Value of parameter
	 */
	public List<String> getStringList( String mName, String att ) {
		String s;
		if ( moduleConfigs.get(mName).containsKey(att) ) {
			s = moduleConfigs.get(mName).get(att);
		} else if ( options.containsKey(att) ) {
			s = options.get(att);
		} else {
			throw new IllegalArgumentException("Module Attribute " + mName + "." + att + " does not exist.");
		}
		ArrayList<String> r = new ArrayList<String>();
		for ( String p : s.split(",")) {
			r.add(p);
		}
		return r;
	}
	
	/**
	 * Get integer value of a parameter of a {@link Module}
	 * @param mName {@link Module} name
	 * @param att Attribute of parameter 
	 * @return Value of parameter
	 */
	public int getInt( String mName, String att ) {
		if ( moduleConfigs.get(mName).containsKey(att) ) {
			return Integer.valueOf(moduleConfigs.get(mName).get(att)).intValue();
		} else if ( options.containsKey(att) ) {
			return Integer.valueOf(options.get(att)).intValue();
		} else {
			throw new IllegalArgumentException("Module Attribute " + mName + "." + att + " does not exist.");
		}
	}
	
	/**
	 * Get long value of a parameter of a {@link Module}
	 * @param mName {@link Module} name
	 * @param att Attribute of parameter 
	 * @return Value of parameter
	 */
	public long getLong( String mName, String att ) {
		if ( moduleConfigs.get(mName).containsKey(att) ) {
			return Long.valueOf(moduleConfigs.get(mName).get(att)).longValue();
		} else if ( options.containsKey(att) ) {
			return Long.valueOf(options.get(att)).longValue();
		} else {
			throw new IllegalArgumentException("Module Attribute " + mName + "." + att + " does not exist.");
		}
	}
	/**
	 * Get boolean value of a parameter of a {@link Module}
	 * @param mName {@link Module} name
	 * @param att Attribute of parameter 
	 * @return Value of parameter
	 */
	public boolean getBoolean( String mName, String att ) {
		if ( moduleConfigs.get(mName).containsKey(att) ) {
			return moduleConfigs.get(mName).get(att).equals("true");
		} else if ( options.containsKey(att) ) {
			return options.get(att).equals("true");
		} else {
			throw new IllegalArgumentException("Module Attribute " + mName + "." + att + " does not exist.");
		}
	}
	/**
	 * Get double value of a parameter of a {@link Module}
	 * @param mName {@link Module} name
	 * @param att Attribute of parameter 
	 * @return Value of parameter
	 */
	public double getDouble( String mName, String att ) {
		if ( moduleConfigs.get(mName).containsKey(att) ) {
			return Double.valueOf(moduleConfigs.get(mName).get(att)).doubleValue();
		} else if ( options.containsKey(att) ) {
			return Double.valueOf(options.get(att)).doubleValue();
		} else {
			throw new IllegalArgumentException("Module Attribute " + mName + "." + att + " does not exist.");
		}
	}
	
	@Override
	public String toString() {
		String s = "Modules configs:\n";
		for ( String k : moduleConfigs.keySet() ) {
			for ( String k2 : moduleConfigs.get(k).keySet() ) {
				s += k + " -> " + k2 + " -> " + moduleConfigs.get(k).get(k2) + "\n";
			}
		}		
		return s;
	}
}
