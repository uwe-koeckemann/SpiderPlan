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
package org.spiderplan.modules.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.tools.Loop;

/**
 * Creates {@link Module}s using the default constructor given their name (and package).
 * The default package is "modules"
 * @author uwe
 *
 */
public class ModuleFactory {
	
	private static HashMap<String,Module> staticModules = new HashMap<String, Module>(); 
	
	/**
	 * Creates a {@link Module} given its name
	 * @param mName The name of the {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 * @return The instantiated {@link Module}
	 */
	@SuppressWarnings("rawtypes")
	public static Module initModule( String mName, ConfigurationManager cM ) {
		Module m = null;
		boolean isStaticModule = false;		
		
		try {
			mName = mName.trim();
			
			if ( cM.hasAttribute(mName, "static")) {
				if ( cM.getBoolean(mName, "static")) {
					isStaticModule = cM.getBoolean(mName, "static");
					if ( isStaticModule && staticModules.containsKey(mName) ) {
						return staticModules.get(mName);
					}
				}
			}

			String moduleClassStr = cM.getString( mName, "class");
			if ( moduleClassStr == null ) {
				throw new IllegalArgumentException("Module " + mName + " does not exist.");
			}
			Class sClass = Class.forName("java.lang.String");
			Class cClass = Class.forName("org.spiderplan.modules.configuration.ConfigurationManager");
			Class moduleClass = null;
			boolean foundClass = false;
			
			// Try default location of modules
			try {
				moduleClass = Class.forName("org.spiderplan.modules."+moduleClassStr);
				foundClass = true;
			} catch ( ClassNotFoundException e ) { }	// We still got options:
			// Try external module 
			if ( !foundClass ) {
				moduleClass = Class.forName(moduleClassStr);
			}
			@SuppressWarnings("unchecked")
			Constructor c = moduleClass.getConstructor(sClass,cClass);
			m = (Module)c.newInstance(mName, cM);
			
			if ( isStaticModule ) {
				staticModules.put(mName, m);
			}
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Loop.start();
		} catch (SecurityException e) {
			e.printStackTrace();
			Loop.start();	
		} catch (InstantiationException e) {
			e.printStackTrace();
			Loop.start();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			Loop.start();
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			e.printStackTrace();
			Loop.start();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Loop.start();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			Loop.start();
		} 
		
		return m;
	}
	
	/**
	 * Clear static modules stored in the factory
	 */
	public static void forgetStaticModules() {
		ModuleFactory.staticModules = new HashMap<String, Module>();
	}
}
