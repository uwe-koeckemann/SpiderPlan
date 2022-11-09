# Spiderplan

Spiderplan is a constraint-based automated planner. 

## Getting Started

### Installation

    cd spiderplan
    sbt clean; compile; test; publishM2

### Running an Example

    cd spiderplan
    sbt run test/robot-move-pick-place/domain.aiddl test/robot-move-pick-place/problem-01.aiddl

## Important Terms and Concepts

### Temporal Intervals

Temporal intervals consist of two time points for their start-time and end-time.
SpiderPlan uses flexible temporal intervals which means that start- and end-time
both use a range of possible values from an earliest to a latest value. As a
result we have four relevant values for each time point: the **Earliest Start
Time (EST)**, **Latest Start Time (LST)**, **Earliest End Time (EET)**, and
**Latest End Time (LET)**.



## Constraints

Spider plan represents both problems and solutions as **Constraint Databases
(CDBs)**.  A constraint database is a set of constraints grouped by type.  It
has a form

    CDB = {
        t1:{a b c}
        t2:{e f g}
    }
    
As an AIDDL term, a CDB is a set of key-value pairs whose keys are types and
whose values are sets of constraints of the given type.

### Statements

A statement

    (I x:v)
    
links a state-variable `x` with value `v` to a temporal interval `I`.  As an
AIDDL term, a statement is a tuple consistng of two elements: the interval and a
key-value pair whose key is `x` and whose value us `v`.

In a CDB, statements use the key `statement`.

    statement:{
      (s0_1 (at r1):loc1)
      (s0_2 (object-at o1):loc2)
      (s0_3 (adjacent loc1 loc2):true)
      (s0_4 (adjacent loc2 loc1):true)
    }


### Temporal Constraints

Temporal constraints restrict temporal intervals by imposing limitations on
their start- and end-times. These constraints mainly come in two flavors: unary
(e.g., `duration`, `release`, `deadline`) and binary (e.g., `before`, `equals`,
`overlaps`, `during`).  In addition, there is one special temporal constraint
`intersectio-possible` which is satisfied if a set of intervals may have a
non-empty intersection.

In a CDB, temporal constraints use the key `temporal`.

    temporal:{
      (duration s0_3 (+INF +INF))
      (duration s0_4 (+INF +INF))
      (duration s0_2 (1 +INF))
      (release s0_2 (0 0))
      (duration s0_1 (1 +INF))
      (release s0_3 (0 0))
      (equals s0_1 G1)
      (release s0_1 (0 0))
      (release s0_4 (0 0))
    }


#### Quantified Allen Constraints

#### Unary Constraints

#### Other Temporal Constraints

### Reusable Resource Constraints

### Open Goals & Operators

Open goals are statements that the planner should achieve. They have exactly the
same form as operators, but appear in their own constraint type `goal`.

Open goals can be satisfied by setting their interval equal to a statement in
the CDB which uses the same variable and value. If no such statement exists,
operators allow for more complex ways to change a CDB.

An operator is a tuple of key-value pairs:

- `name` name and possibly parameters of the operator
- `signature` type map for each parameter
- `id` variable used to make the operator unique where neede (e.g., making sure
  it uses unique temporal intervals)
- `interval` term that represents the interval of the operator itself (usually
  depends on the ID term)
- `preconditions` collection of statements that must exist in a CDB for the operator to be applicable
- `effects` collection of statements that will be added to a CDB if the operator is applied
- `constraints` a CDB that will be added to a CDB when the operator is
  applied. So adding an operator may lead to the addition of any type of
  constraint. Typically, constraints at least connect preconditions and effects
  to the operator's main interval

To signal that an open-goal has been achieved, its interval will be added to a
set under a constraint type `goal.sat`.


### Conditional Constraints

## Libraries

In addtition to the regular set of constraints, Spiderplan offers libraries for
constraints solved by external solvers. Licenses may be different from
Spiderplan due to the usage of external libraries. For this reason and to avoid
cluttering the main SpiderPlan library wth dependencies, these solvers are in
their own libraries. 

### Prolog

Allows to put constraints on Prolog background knowledge. In some cases, these
constraints can be removed during preprocessing. Operators, for instance, can be
replaced by operators that fulfill their Prolog constraints.

### Coordination ORU

Allows combined task and motion planning and multi-robot coordination by
attaching motion constraints to planning operators.

# Ackknowledgements

