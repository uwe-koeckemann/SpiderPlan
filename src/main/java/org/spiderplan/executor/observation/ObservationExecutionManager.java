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
package org.spiderplan.executor.observation;

import org.spiderplan.executor.ExecutionManager;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.execution.Observation;

/**
 * @author Uwe Köckemann
 *
 */
public class ObservationExecutionManager extends ExecutionManager {
	
	/**
	 * Initialize with name.
	 * @param name
	 */
	public ObservationExecutionManager( String name ) {
		super(name);
	}
	
	@Override
	public void initialize( ConstraintDatabase cdb ) {
		for ( Observation obs : cdb.get(Observation.class) ) {
//			if ( !obs.isAsserted() ) {
				ObservationReactor r = new ObservationReactor(obs.getStatement(), cdb);
				execList.add(obs.getStatement());
				hasReactorList.add(obs.getStatement());
				this.reactors.add(r);
//			}
		}
	}
	

	@Override
	public boolean update(long t, ConstraintDatabase cdb) {
		/**
		 * What this should do:
		 * 
		 * - Add reactors for new observations
		 * - Call super.update() to update all existing reactors
		 */			
		for ( Observation obs : cdb.get(Observation.class) ) {
//			if ( !obs.isAsserted() ) {
				if ( !super.hasReactorList.contains(obs.getStatement()) ) {
					ObservationReactor r = new ObservationReactor(obs.getStatement(), cdb);
					execList.add(obs.getStatement());
					hasReactorList.add(obs.getStatement());
					this.reactors.add(r);
				}
//			}
		}

		
		
		super.update(t, cdb);
		
		return true;
	}
}
