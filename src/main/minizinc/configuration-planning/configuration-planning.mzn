int: n;				% Number of concepts
int: m;				% Number of rules

set of int: Concepts    = 1..n;
set of int: Rules 			= 1..m;

array[Concepts] of string: conceptNames;
array[Rules] of string: ruleNames;

set of Concepts : targetConcepts ;
set of Concepts : unavailable;

var set of Concepts:  configuration ;
var set of Rules: usedRules ;

array[Rules] of  int : ruleTargets ;
array[Rules,Concepts] of  int : rules ;

array[Rules] of int: cost;

% Information goals need to be fulfilled:
constraint forall (i in 1..n) ( (bool2int(i in targetConcepts) = 0) \/ (bool2int(i in configuration) = 1) ) ;

% Do not use unavailable sensors:
constraint forall (i in 1..n) ( (bool2int(i in configuration) = 0) \/ (bool2int(i in unavailable) = 0) ) ;

% Do not use rules if not needed:
constraint forall (i in 1..n) ( (bool2int(i in configuration) = 1) \/ (bool2int( exists (j in 1..m) ((bool2int(j in usedRules) = 1) /\ ruleTargets[j] = i)) = 0) ) ;

% Use exactly one rule for each target concept
constraint forall (i in 1..n) ( (bool2int(i in configuration) = 0) \/ ((sum (j in 1..m) (bool2int( (j in usedRules) /\ ruleTargets[j] = i))) = 1)) ;


% Each concept needs one of its input possibilities satisfied
constraint (
	forall (i in 1..n) (
		(bool2int(i in configuration) = 0) \/
		exists (j in 1..m) (
			(ruleTargets[j] = i) 
			/\ (bool2int(j in usedRules) = 1)
			/\ forall (k in 1..n) (
				(rules[j,k] = 0) \/ (bool2int(k in configuration) = 1)
			)
		)
	)
);

solve minimize sum (i in 1..m) (bool2int(i in usedRules)*cost[i]);

output
["(list "] ++ [if fix(i in configuration) then show(conceptNames[i]) ++ " " else "" endif | i in 1..n] ++[");"]
++ ["(list "] ++ [if fix(i in usedRules) then show(ruleNames[i]) ++ " " else "" endif | i in 1..m] ++[")"]
