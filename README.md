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
- ROS
  - Publish variables to ROS
  - Subsribe to ROS topics
  - Make variables ROS goals (actionlib)
  - Use ROS service calls

# Dependencies & Requirements

The following libraries will be fetched from maven when running gradle:

- JUnit <a href="http://junit.org/">(link)</a>
- Jung Graph Library <a href="http://jung.sourceforge.net/">(link)</a>

Apart from that Python 2.x is required by some scripts (e.g. for ROS)
and YAP Prolog <a href="http://www.dcc.fc.up.pt/~vsc/Yap/">(link)</a> is used to solve Prolog constraints.

# Installation & Test

1) Compile with gradle (will download all maven dependencies):

```
gradle build
```

2) Run a basic example:

```
gradle run
```

# Solving a problem

To solve a problem three things need to be provided:

- A domain definition (.uddl)
- A problem definition (.uddl)
- A planner definition (.spider)

# Sponsors

* <a href="http://www.oru.se/">&Ouml;rebro University</a>
* <a href="http://www.vr.se/inenglish">The Swedish Research Council (Project: Human-aware task planning for mobile robots)</a> 
* <a href="http://www.kk-stiftelsen.org/">The Swedish Knowledge Foundation (Project: ecare@home)</a>











