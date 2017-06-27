#-------------------------------------------------------------------------------
# The MIT License (MIT)
# 
# Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#-------------------------------------------------------------------------------
# What is SpiderPlan?

SpiderPlan is a constraint-based planning framework that allows inclusion of many different types of knowledge 
into automated planning. It also features execution and comes with an interface to the Robot Operating System (ROS).

# Featured Constraint Types & Solvers

- Statements
  - State-variable assignment attached to flexible temporal interval
- Goals
- Domain constraints
- Temporal constraints
- Reusable resources
- Prolog 
- Costs
- Math constraints (simple calculations)
- Set constraints (simple)
- Interaction Constraints
  - Used for social acceptability, proactivity and context-awareness in Human-aware Planning
- ROS (Robot Operating System)
  - Publish variables to ROS
  - Subsribe to ROS topics
  - Make variables ROS goals (actionlib)
  - Use ROS service calls

# Dependencies & Requirements

The following libraries will be fetched from maven when running gradle:

- JUnit <a href="http://junit.org/">(link)</a>
- Jung Graph Library <a href="http://jung.sourceforge.net/">(link)</a>


Apart from that Python 2.x is required by some scripts. YAP Prolog <a href="http://www.dcc.fc.up.pt/~vsc/Yap/">(link)</a> is used to solve Prolog constraints. And ROS Hydro <a href="http://www.ros.org/">(link)</a> is required when ising ROS constraints.

# Installation & Test

All commands assume the current folder is the SpiderPlan root folder. The run.sh script will also build the project.

## Build: 

```
 ./gradlew build
```

## Run default test case:

```
 ./run.sh
```

## Running a single problem:

We need to provide a planner definition (.spider) as first argument followed by any number of domain description files (.uddl).

```
 ./run.sh ./domains/household/planner.spider ./domains/household/domain.uddl ./domains/household/test-cases/test01.uddl
```

## Running an experiment:

Planner definition, domain description and problem folder are specidied in the experiment definition (.experiment). Running the following line will solve all problems with the specified planner and domain and store the results in a .csv file.

```
 ./run.sh ./domains/household/u0-p1.experiment
```

# Sponsors

* <a href="http://www.oru.se/">&Ouml;rebro University</a>
* <a href="http://www.vr.se/inenglish">The Swedish Research Council (Project: Human-aware task planning for mobile robots)</a> 
* <a href="http://www.kks.se/">The Swedish Knowledge Foundation (Project: Ecare@Home)</a>











