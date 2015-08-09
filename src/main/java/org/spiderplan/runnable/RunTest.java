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
package org.spiderplan.runnable;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.ConstraintTypes.TemporalRelation;
import org.spiderplan.representation.constraints.Interval;
import org.spiderplan.representation.constraints.ReusableResourceCapacity;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.stopWatch.StopWatch;
//import stopWatch.StopWatch;

public class RunTest {

	/**
	 * @param args
	 * @throws UnknownThing 
	 * @throws NonGroundThing 
	 */
	public static void main(String[] args) {	
		TypeManager tM = new TypeManager();
			
		tM.addSimpleEnumType("executing","executing");
		tM.addSimpleEnumType("boolean", "true,false");
		tM.addSimpleEnumType("match", "match0,match1,match2,match3,match4,");
		tM.addSimpleEnumType("fuse", "fuse0,fuse1,fuse2,fuse3,fuse4,fuse5,fuse6,fuse7,fuse8,fuse9");
		
		tM.attachTypes("light(match)=boolean");
		tM.attachTypes("unused(match)=boolean");
		tM.attachTypes("mended(fuse)=boolean");
		tM.attachTypes("customOperator(fuse,match)=executing");
		tM.attachTypes("LightMatch(match)=executing");
		tM.attachTypes("MendFuse(fuse,match)=executing");
		
		ConstraintDatabase context = new ConstraintDatabase();
		
		context.add(new ReusableResourceCapacity(new Atomic("rightHandUse"), 1));
		context.add(new ReusableResourceCapacity(new Atomic("leftHandUse"), 1));
		
		context.add(new Statement("e1_13: <rightHandUse(),1"));
		context.add(new Statement("e1_5: <rightHandUse(),1"));
		context.add(new Statement("e1_6: <rightHandUse(),1"));
		
		context.add(new Statement("e1_11: <rightHandUse(),1"));
		context.add(new Statement("e1_12: <rightHandUse(),1"));
		context.add(new Statement("e1_2: <rightHandUse(),1"));
		context.add(new Statement("e1_3: <rightHandUse(),1"));
		context.add(new Statement("e1_8: <rightHandUse(),1"));
		context.add(new Statement("e1_9: <rightHandUse(),1"));
		
		context.add(new Statement("e3_1: <leftHandUse(),1"));
		context.add(new Statement("e3_10: <leftHandUse(),1"));
		context.add(new Statement("e3_4: <leftHandUse(),1"));
		context.add(new Statement("e3_7: <leftHandUse(),1"));
		
		
		context.add(new Statement("e1_1: <unused(match0),false"));
		context.add(new Statement("e1_10: <unused(match1),false"));
		context.add(new Statement("e1_4: <unused(match4),false"));
	
		context.add(new Statement("e1_7: <unused(match3),false"));
	
		context.add(new Statement("e2_1: <light(match0),true"));
		context.add(new Statement("e2_10: <light(match1),true"));
		context.add(new Statement("e2_11: <mended(fuse8),true"));
		context.add(new Statement("e2_12: <mended(fuse9),true"));
		context.add(new Statement("e2_13: <mended(fuse3),true"));
		context.add(new Statement("e2_2: <mended(fuse0),true"));
		context.add(new Statement("e2_3: <mended(fuse1),true"));
		context.add(new Statement("e2_4: <light(match4),true"));
		context.add(new Statement("e2_5: <mended(fuse6),true"));
		context.add(new Statement("e2_6: <mended(fuse4),true"));
		context.add(new Statement("e2_7: <light(match3),true"));
		context.add(new Statement("e2_8: <mended(fuse7),true"));
		context.add(new Statement("e2_9: <mended(fuse2),true"));
		context.add(new Statement("key1/s0: <light(match0),false"));
		context.add(new Statement("key10/s0: <unused(match4),true"));
		context.add(new Statement("key11/s0: <mended(fuse0),false"));
		context.add(new Statement("key12/s0: <mended(fuse1),false"));
		context.add(new Statement("key13/s0: <mended(fuse2),false"));
		context.add(new Statement("key14/s0: <mended(fuse3),false"));
		context.add(new Statement("key15/s0: <mended(fuse4),false"));
		context.add(new Statement("key16/s0: <mended(fuse5),false"));
		context.add(new Statement("key17/s0: <mended(fuse6),false"));
		context.add(new Statement("key18/s0: <mended(fuse7),false"));
		context.add(new Statement("key19/s0: <mended(fuse8),false"));
		context.add(new Statement("key2/s0: <light(match1),false"));
		context.add(new Statement("key20/s0: <mended(fuse9),false"));
		context.add(new Statement("key3/s0: <light(match2),false"));
		context.add(new Statement("key4/s0: <light(match3),false"));
		context.add(new Statement("key5/s0: <light(match4),false"));
		context.add(new Statement("key6/s0: <unused(match0),true"));
		context.add(new Statement("key7/s0: <unused(match1),true"));
		context.add(new Statement("key8/s0: <unused(match2),true"));
		context.add(new Statement("key9/s0: <unused(match3),true"));
		context.add(new Statement("p2_11: <light(match1),true"));
		context.add(new Statement("p2_12: <light(match1),true"));
		context.add(new Statement("p2_13: <light(match4),true"));
		context.add(new Statement("p2_2: <light(match0),true"));
		context.add(new Statement("p2_3: <light(match0),true"));
		context.add(new Statement("p2_5: <light(match4),true"));
		context.add(new Statement("p2_6: <light(match4),true"));
		context.add(new Statement("p2_8: <light(match3),true"));
		context.add(new Statement("p2_9: <light(match3),true"));
		context.add(new Statement("tHIS_1: <LightMatch(match0),executing"));
		context.add(new Statement("tHIS_10: <LightMatch(match1),executing"));
		context.add(new Statement("tHIS_11: <MendFuse(fuse8,match1),executing"));
		context.add(new Statement("tHIS_12: <MendFuse(fuse9,match1),executing"));
		context.add(new Statement("tHIS_13: <MendFuse(fuse3,match4),executing"));
		context.add(new Statement("tHIS_2: <MendFuse(fuse0,match0),executing"));
		context.add(new Statement("tHIS_3: <MendFuse(fuse1,match0),executing"));
		context.add(new Statement("tHIS_4: <LightMatch(match4),executing"));
		context.add(new Statement("tHIS_5: <MendFuse(fuse6,match4),executing"));
		context.add(new Statement("tHIS_6: <MendFuse(fuse4,match4),executing"));
		context.add(new Statement("tHIS_7: <LightMatch(match3),executing"));
		context.add(new Statement("tHIS_8: <MendFuse(fuse7,match3),executing"));
		context.add(new Statement("tHIS_9: <MendFuse(fuse2,match3),executing"));
	
		
		
		IncrementalSTPSolver csp = new IncrementalSTPSolver(0, 5000);
//		MetaCSPAdapterWithHistory csp = new MetaCSPAdapterWithHistory(5000,300);
		
		context.add(new AllenConstraint(Term.createConstant("e1_11"), TemporalRelation.Duration, new Interval(Term.createInteger(2), Term.createInteger(2))));
		context.add(new AllenConstraint(Term.createConstant("e1_11"), Term.createConstant("p2_11"), TemporalRelation.During, new Interval(Term.createInteger(1), Term.createConstant("inf")), new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e1_12"), TemporalRelation.Duration, new Interval(Term.createInteger(2), Term.createInteger(2))));
		context.add(new AllenConstraint(Term.createConstant("e1_12"), Term.createConstant("p2_12"), TemporalRelation.During, new Interval(Term.createInteger(1), Term.createConstant("inf")), new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e1_13"), TemporalRelation.Duration, new Interval(Term.createInteger(2), Term.createInteger(2))));
		context.add(new AllenConstraint(Term.createConstant("e1_13"), Term.createConstant("p2_13"), TemporalRelation.During, new Interval(Term.createInteger(1), Term.createConstant("inf")), new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e1_2"), TemporalRelation.Duration, new Interval(Term.createInteger(2), Term.createInteger(2))));
		context.add(new AllenConstraint(Term.createConstant("e1_2"), Term.createConstant("p2_2"), TemporalRelation.During, new Interval(Term.createInteger(1), Term.createConstant("inf")), new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e1_3"), TemporalRelation.Duration, new Interval(Term.createInteger(2), Term.createInteger(2))));
		context.add(new AllenConstraint(Term.createConstant("e1_3"), Term.createConstant("p2_3"), TemporalRelation.During, new Interval(Term.createInteger(1), Term.createConstant("inf")), new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e1_5"), TemporalRelation.Duration, new Interval(Term.createInteger(2), Term.createInteger(2))));
		context.add(new AllenConstraint(Term.createConstant("e1_5"), Term.createConstant("p2_5"), TemporalRelation.During, new Interval(Term.createInteger(1), Term.createConstant("inf")), new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e1_6"), TemporalRelation.Duration, new Interval(Term.createInteger(2), Term.createInteger(2))));
		context.add(new AllenConstraint(Term.createConstant("e1_6"), Term.createConstant("p2_6"), TemporalRelation.During, new Interval(Term.createInteger(1), Term.createConstant("inf")), new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e1_8"), TemporalRelation.Duration, new Interval(Term.createInteger(2), Term.createInteger(2))));
		context.add(new AllenConstraint(Term.createConstant("e1_8"), Term.createConstant("p2_8"), TemporalRelation.During, new Interval(Term.createInteger(1), Term.createConstant("inf")), new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e1_9"), TemporalRelation.Duration, new Interval(Term.createInteger(2), Term.createInteger(2))));
		context.add(new AllenConstraint(Term.createConstant("e1_9"), Term.createConstant("p2_9"), TemporalRelation.During, new Interval(Term.createInteger(1), Term.createConstant("inf")), new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e2_1"), TemporalRelation.Duration, new Interval(Term.createInteger(7), Term.createInteger(7))));
		context.add(new AllenConstraint(Term.createConstant("e2_1"), Term.createConstant("tHIS_1"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("e2_10"), TemporalRelation.Duration, new Interval(Term.createInteger(7), Term.createInteger(7))));
		context.add(new AllenConstraint(Term.createConstant("e2_10"), Term.createConstant("tHIS_10"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("e2_11"), TemporalRelation.Duration, new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e2_12"), TemporalRelation.Duration, new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e2_13"), TemporalRelation.Duration, new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e2_2"), TemporalRelation.Duration, new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e2_3"), TemporalRelation.Duration, new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e2_4"), TemporalRelation.Duration, new Interval(Term.createInteger(7), Term.createInteger(7))));
		context.add(new AllenConstraint(Term.createConstant("e2_4"), Term.createConstant("tHIS_4"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("e2_5"), TemporalRelation.Duration, new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e2_6"), TemporalRelation.Duration, new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e2_7"), TemporalRelation.Duration, new Interval(Term.createInteger(7), Term.createInteger(7))));
		context.add(new AllenConstraint(Term.createConstant("e2_7"), Term.createConstant("tHIS_7"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("e2_8"), TemporalRelation.Duration, new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e2_9"), TemporalRelation.Duration, new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		context.add(new AllenConstraint(Term.createConstant("e3_1"), Term.createConstant("tHIS_1"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("e3_10"), Term.createConstant("tHIS_10"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("e3_4"), Term.createConstant("tHIS_4"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("e3_7"), Term.createConstant("tHIS_7"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("key10"), Term.createConstant("tHIS_4"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("key6"), Term.createConstant("tHIS_1"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("key7"), Term.createConstant("tHIS_10"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("key9"), Term.createConstant("tHIS_7"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("p2_11"), Term.createConstant("e2_10"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("p2_12"), Term.createConstant("e2_10"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("p2_13"), Term.createConstant("e2_4"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("p2_2"), Term.createConstant("e2_1"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("p2_3"), Term.createConstant("e2_1"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("p2_5"), Term.createConstant("e2_4"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("p2_6"), Term.createConstant("e2_4"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("p2_8"), Term.createConstant("e2_7"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("p2_9"), Term.createConstant("e2_7"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("s0"), TemporalRelation.Release, new Interval(Term.createInteger(0), Term.createInteger(0))));
		context.add(new AllenConstraint(Term.createConstant("tHIS_1"), Term.createConstant("e1_1"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_10"), Term.createConstant("e1_10"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_11"), Term.createConstant("e1_11"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_11"), Term.createConstant("e2_11"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_12"), Term.createConstant("e1_12"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_12"), Term.createConstant("e2_12"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_13"), Term.createConstant("e1_13"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_13"), Term.createConstant("e2_13"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_2"), Term.createConstant("e1_2"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_2"), Term.createConstant("e2_2"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_3"), Term.createConstant("e1_3"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_3"), Term.createConstant("e2_3"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_4"), Term.createConstant("e1_4"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_5"), Term.createConstant("e1_5"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_5"), Term.createConstant("e2_5"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_6"), Term.createConstant("e1_6"), TemporalRelation.Equals)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_6"), Term.createConstant("e2_6"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_7"), Term.createConstant("e1_7"), TemporalRelation.Meets)); 
		context.add(new AllenConstraint(Term.createConstant("tHIS_8"), Term.createConstant("e1_8"), TemporalRelation.Equals)); 
//		StopWatch.verbose = true;
//		APSPKernel.sw.verbose = true;
//		APSPKernel.sw.keepAllTimes = true;
		
		
		StopWatch.start("run");
		System.out.println( csp.isConsistent(context, tM) );
		StopWatch.stop("run");
		
		System.out.println(StopWatch.getLastFormattedInSeconds("run"));
//		
//		TestMetaCSPAdapter tester = new TestMetaCSPAdapter();
//		
//		tester.run();
		
//		System.out.println(APSPKernel.sw.allSums2Str());
//		System.out.println(APSPKernel.sw.allLast2Str());
//		System.out.println(APSPSolverGPU.sw.allSums2Str());
		System.out.println(StopWatch.allSums2Str());
		
		
//		System.out.println(APSPKernel.sw.getCSVFormattedInSeconds("[APSPKernel] Write row and col"));
		
//		System.out.println(APSPKernel.sw.recordedTimes.get("[APSPKernel] Write row and col"));
	}
}
