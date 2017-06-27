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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.spiderplan.executor.ExecutionManager;
import org.spiderplan.executor.ROS.ROSProxy;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.ExpressionTypes.ROSRelation;
import org.spiderplan.representation.expressions.ExpressionTypes.SocketRelation;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.execution.Observation;
import org.spiderplan.representation.expressions.execution.ros.ROSConstraint;
import org.spiderplan.representation.expressions.execution.ros.ROSGoal;
import org.spiderplan.representation.expressions.execution.sockets.SocketExpression;
import org.spiderplan.representation.expressions.interfaces.Unique;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.UniqueID;
import org.spiderplan.tools.logging.Logger;

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
