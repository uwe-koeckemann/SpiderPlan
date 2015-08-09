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

/**
 * Contains a description of parameters used by the {@link ConfigurationManager}.
 * 
 * @author Uwe Köckemann
 *
 */
public class ParameterDescription {
	private String name;
	private String type;
	private String defaultValue;
	private String helpText;
	
	private boolean optional = true;
	
	/**
	 * Create a new optional parameter.
	 * @param name Parameter name
	 * @param type Data type
	 * @param defaultValue Default value
	 * @param helpText Help message explaining the meaning of the paramter
	 */
	public ParameterDescription( String name, String type, String defaultValue, String helpText ) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.helpText = helpText;
	}
	
	/**
	 * Create a new parameter.
	 * @param name Parameter name
	 * @param type Data type
	 * @param defaultValue Default value
	 * @param optional Use <code>true</code> if this parameter is optional, <code>false</code> otherwise.
	 * @param helpText Help message explaining the meaning of the paramter
	 */
	public ParameterDescription( String name, String type, String defaultValue, boolean optional, String helpText ) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.optional = optional;
		this.helpText = helpText;
	}
	
	/**
	 * Get the name of the parameter
	 * @return The name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Check if this parameter is optional
	 * @return <code>true</code> if this parameter is optional, <code>false</code> otherwise.
	 */
	public boolean isOptional() {
		return optional;
	}
	
	@Override
	public String toString() {
		String opt = "";
		if ( !optional ) 
			opt = "(required) ";
		return name + " (" +type+")" + " ["+defaultValue+"] " + opt + helpText;
	}
}
