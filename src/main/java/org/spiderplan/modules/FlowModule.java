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
package org.spiderplan.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.FlowRule;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Control module that uses rules to decide how its sub-modules are executed.
 * This is usually the main-module that decides the overall behavior of a
 * planner. 
 * 
 * @author Uwe Köckemann
 * 
 */
public class FlowModule  extends Module {
	
	private List<String> moduleList = new ArrayList<String>();
	private Map<String,Module> modules = new HashMap<String, Module>();
	private List<FlowRule> rules = new ArrayList<FlowRule>();
	
	boolean firstRun = true;
	
	boolean overrideVerbose = false;
	
	private boolean done = false;
	private boolean success = false;
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public FlowModule(String name, ConfigurationManager cM) {
		super(name, cM);
		
		super.parameterDesc.add(  new ParameterDescription("modules", "String", "", "List of modules that belong to this flow.") );
		super.parameterDesc.add( new ParameterDescription("rules", "string", "", 
									"Setup of the flow of modules. " +
									"A comma separated list with elements of the format ModuleName->State->ModuleName." + 
									"ModuleName can be any element from the \"modules\" option. " +
									"Use START->ModuleName for initial module" +
									"and ModuleName->State->FAIL for failure of FlowModule." + 
									"State can be in {Success, Step, Fail}, where Step indicates that a module is not done, but allows to further process an intermediate solution.") );
				
		super.parameterDesc.add(  new ParameterDescription("overrideVerbose", "boolean", "false", "When set to true verbose and verbosity for all sub-modules in flow is overwritten with the settings of this module when calling them.") );		
		
		if ( cM.hasAttribute(name, "overrideVerbose")  ) {
			this.overrideVerbose = cM.getBoolean(name, "overrideVerbose");
		} 	
		
		if ( cM.hasAttribute(name, "modules")  ) {
			this.moduleList = cM.getStringList(name, "modules");
		} 		

		if ( cM.hasAttribute(name, "rules")) {
			String setupStr = cM.getString(name, "rules");
			setupStr = setupStr.replace(" ", "").replace("\t", "").replace("\n", "");
			
			String[] tmp = setupStr.replace("{", "").replace("}", "").split(";");
			
			for ( String s : tmp ) {
				rules.add(new FlowRule(s));
			}
		}

		for ( String mName : this.moduleList ) {
				modules.put(mName,ModuleFactory.initModule(mName, cM));
		}
	}
	
	@Override
	public Core run( Core core ) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
	 	Core currentCore = core;
		String currentModule = "Start";
						
		while ( !done || !success ) {		
			boolean foundRule = false;
			for ( FlowRule rule : this.rules ) {
				if ( rule.applies(currentModule, currentCore)) {
					currentModule = rule.getToModule();
					currentCore.setInSignals(rule.getInSignals());
					foundRule = true;
					if ( verbose ) Logger.msg(getName(), "Applying rule: " + rule, 1);
					break;
				}
			}
			
			if ( !foundRule ) {
				throw new IllegalStateException("No FlowRule defined for module " + currentModule + " with out signals: " + core.getOutSignalsString());
			}
			
			if ( keepStats ) stats.increment( msg("#Calls") );
			if ( keepStats ) stats.increment( msg("#Calls (" + currentModule + ")"));
			
			if ( currentModule.equals("Fail") ) {
				currentCore.setResultingState(this.getName(), State.Inconsistent);
				if ( verbose ) Logger.msg(getName(), currentCore.getResultingState(this.getName()).toString(), 0);
				if ( verbose ) Logger.msg(getName(), "Times:" + StopWatch.allSums2Str(), 4 );
									
				if ( verbose ) Logger.depth--;
				return currentCore;	
			} else if ( currentModule.equals("Success") ) {
				currentCore.setResultingState(this.getName(), State.Consistent);
				if ( verbose ) Logger.msg(getName(), currentCore.getResultingState(this.getName()).toString(), 0);				
				if ( verbose ) Logger.msg(getName(), "Times:" + StopWatch.allSums2Str(), 4 );
							
				
				
				if ( verbose ) Logger.depth--;
				return currentCore;
			}		
			
			if ( !modules.containsKey(currentModule)) {
				throw new IllegalArgumentException("Unknown module " + currentModule + " for FlowModule " + this.getName());
			}
			
			if ( keepTimes ) StopWatch.start("[" + this.getName() +"] Running " + currentModule);
			if ( overrideVerbose )
				currentCore = modules.get(currentModule).run( currentCore );
			else
				currentCore = modules.get(currentModule).run( currentCore );
			if ( keepTimes ) StopWatch.stop("[" + this.getName() +"] Running " + currentModule);
				
			if ( keepStats && keepTimes ) stats.setDouble(msg("Time "+currentModule+" [s]"), (StopWatch.getSum("[" + this.getName() +"] Running " + currentModule)/1000.0));
			
			
			if ( currentCore.getResultingState(currentModule) != null && currentCore.getResultingState(currentModule).equals("Killed")) {
				currentCore.setResultingState(this.getName(), State.Killed);
				if ( verbose ) Logger.msg(getName(), currentCore.getResultingState(this.getName()).toString(), 0);
				
				if ( verbose ) Logger.depth--;
				return currentCore;
			}			
		}
		
		if ( verbose ) Logger.msg(getName(), StopWatch.allSums2Str(), 3);
		if ( verbose ) Logger.msg(getName(), currentCore.getResultingState(this.getName()).toString(), 0);
		
		
		
		if ( verbose ) Logger.depth--;		
		return currentCore;		
	}

}
