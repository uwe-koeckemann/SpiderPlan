;; Created by Uwe Köckemann

(define (domain configuration-planning)
	(:requirements :typing :durative-actions :action-costs :numeric-fluents)
	(:types 
		sensor - object
		concept - object
		thing - object
		location - object
		configuration - object
	)
	(:predicates
		(at sensor location)
		(capabilitySensorSimple sensor concept)
		(capabilitySensorTarget sensor concept object)
		(capabilitySensorConfig sensor concept configuration)
		(capabilitySensorLocation sensor concept location)
		
		(inferring concept)
	)
	(:functions
		(energy)
		(cost sensor concept)
	)

;	(:durative-action solve-task
;		:parameters (?r - robot)
;		:duration (= ?duration 100)
;		:condition 
;			(and (over all (ready ?r)))
;		:effect 
;			(and 
;				(at end (task-is-done))
;				(increase (energy) (* #t 10))
;			)
;	)
	
	(:durative-action infer-sensor 
		:parameters (?Sensor - sensor ?C - concept ?Cost - cost)
		:duration (> ?duration 1)
		:condition  (and 
			(capabilitySensorSimple ?Sensor ?C) )
		:effect (and
			(at start (inferring ?C))
			(at end (not (inferring ?C)))
			(?E2 (sensor-state ?Sensor) on)
			(increase (energy) (* #t (cost ?Sensor ?C))) )
	)
	
	(:durative-action infer-targeting-sensor 
		:parameters (?Sensor - sensor ?C - concept ?Object - entity ?L - location ?Cost - cost)
		:condition (and
			(?P1 (at ?Object) ?L)
			(?P2 (at ?Sensor) ?L)
			(?P3 (targeting ?Sensor) ?Object)
		)
		:effect (and
			(?E1 (inferring ?C))
			(?E3 (sensor-state ?Sensor) on)	
		)
		(:constraints
			(:temporal
				(equals ?THIS ?E1)
				(during ?THIS ?E3 [1 inf] [1 inf])

				(during ?THIS ?P1 [1 inf] [1 inf])
				(during ?THIS ?P2 [1 inf] [1 inf])
				(during ?THIS ?P3 [1 inf] [1 inf])
			)
			(:prolog kb
				(capabilitySensor ?Sensor (reqTargeting ?Object) ?C ?Cost)
			)
			(:graph
				(edge G ?Sensor ?C input-for)
			)
			(:math (eval-int (intervalCost ?E1) (mult (sub (EET ?E1) (LST ?E1)) ?Cost)))
			(:cost (add link-cost (intervalCost ?E1)))
		)
	)

	(:durative-action infer-at-location 
		:parameters (?Sensor - sensor ?C - concept ?L - location ?Cost - cost)
		:condition (and
			(?P1 (at ?Sensor) ?L)
		)
		:effect (and
			(?E1 (inferring ?C))
			(?E3 (sensor-state ?Sensor) on)	
		)
		(:constraints
			(:temporal
				(equals ?THIS ?E1)
				(during ?THIS ?E3 [1 inf] [1 inf])

				(during ?THIS ?P1 [1 inf] [1 inf])
			)
			(:prolog kb
				(capabilitySensor ?Sensor (atLocation ?L) ?C ?Cost)
			)
			(:graph
				(edge G ?Sensor ?C input-for)
			)
			(:math (eval-int (intervalCost ?E1) (mult (sub (EET ?E1) (LST ?E1)) ?Cost)))
			(:cost (add link-cost (intervalCost ?E1)))
		)
	)

	(:durative-action infer-with-config 
		:parameters (?Sensor - sensor ?C - concept ?Config - configuration ?Cost - cost)
		:condition (and
			(?P1 (config ?Sensor) ?Config)
		)
		:effect (and
			(?E1 (inferring ?C))
			(?E3 (sensor-state ?Sensor) on)	
		)
		(:constraints
			(:temporal
				(equals ?THIS ?E1)
				(during ?THIS ?E3 [1 inf] [1 inf])

				(during ?THIS ?P1 [1 inf] [1 inf])
			)
			(:prolog kb
				(capabilitySensor ?Sensor (hasConfig ?Config) ?C ?Cost)
			)
			(:graph
				(edge G ?Sensor ?C input-for)
			)
			(:math (eval-int (intervalCost ?E1) (mult (sub (EET ?E1) (LST ?E1)) ?Cost)))
			(:cost (add link-cost (intervalCost ?E1)))
		)
	)

	(:durative-action infer-from-1 
		:parameters (?C - concept ?Cbase - concept ?Cost - cost)
		(:preconditions
			(?P1 (inferring ?Cbase))
		)
		:effect (and
			(?E1 (inferring ?C))
		)
		(:constraints
			(:temporal
				(equals ?THIS ?E1)
				(during ?THIS ?P1 [1 inf] [1 inf])
			)
			(:prolog kb
				(capabilityOne ?Cbase ?C ?Cost)
			)
			(:graph
				(edge G ?Cbase ?C input-for)
			)
			(:math (eval-int (intervalCost ?E1) (mult (sub (EET ?E1) (LST ?E1)) ?Cost)))
			(:cost (add link-cost (intervalCost ?E1)))
		)
	)

	(:durative-action ;; infer a concept from two other concepts
		(infer-from-1 ?C - concept ?Cbase1 - concept ?Cost - cost)
		(:preconditions
			(?P1 (inferring ?Cbase1))
			(?P2 (inferring ?Cbase2))
		)
		:effect (and
			(?E1 (inferring ?C))
		)
		(:constraints
			(:temporal
				(equals ?THIS ?E1)
				(during ?THIS ?P1 [1 inf] [1 inf])
				(during ?THIS ?P2 [1 inf] [1 inf])
			)
			(:prolog kb
				(capability1 ?Cbase1 ?C ?Cost)
			)
			(:graph
				(edge G ?Cbase1 ?C input-for)
				(edge G ?Cbase2 ?C input-for)
			)
			(:math (eval-int (intervalCost ?E1) (mult (sub (EET ?E1) (LST ?E1)) ?Cost)))
			(:cost (add link-cost (intervalCost ?E1)))
		)
	)

	(:durative-action infer-from-2 
		:parameters (?C - concept ?Cbase1 - concept ?Cbase2 - concept ?Cost - cost)
		(:preconditions
			(?P1 (inferring ?Cbase1))
			(?P2 (inferring ?Cbase2))
		)
		:effect (and
			(?E1 (inferring ?C))
		)
		(:constraints
			(:temporal
				(equals ?THIS ?E1)
				(during ?THIS ?P1 [1 inf] [1 inf])
				(during ?THIS ?P2 [1 inf] [1 inf])
			)
			(:prolog kb
				(capability2 ?Cbase1 ?Cbase2 ?C ?Cost)
			)
			(:graph
				(edge G ?Cbase1 ?C input-for)
				(edge G ?Cbase2 ?C input-for)
			)
			(:math (eval-int (intervalCost ?E1) (mult (sub (EET ?E1) (LST ?E1)) ?Cost)))
			(:cost (add link-cost (intervalCost ?E1)))
		)
	)

	(:durative-action infer-from-3 
		:parameters (?C - concept ?Cbase1 - concept ?Cbase2 - concept ?Cbase3 - concept ?Cost - cost)
		(:preconditions
			(?P1 (inferring ?Cbase1))
			(?P2 (inferring ?Cbase2))
			(?P3 (inferring ?Cbase3))
		)
		:effect (and
			(?E1 (inferring ?C))
		)
		(:constraints
			(:temporal
				(equals ?THIS ?E1)
				(during ?THIS ?P1 [1 inf] [1 inf])
				(during ?THIS ?P2 [1 inf] [1 inf])
				(during ?THIS ?P3 [1 inf] [1 inf])
			)
			(:prolog kb
				;; node of specific class in current configuration can use the three base concepts to infer the target concept:
				(capability3 ?Cbase1 ?Cbase2 ?Cbase3 ?C ?Cost) 
			)
			(:graph
				(edge G ?Cbase1 ?C input-for)
				(edge G ?Cbase2 ?C input-for)
				(edge G ?Cbase3 ?C input-for)
			)
			(:math (eval-int (intervalCost ?E1) (mult (sub (EET ?E1) (LST ?E1)) ?Cost)))
			(:cost (add link-cost (intervalCost ?E1)))
		)
	)

	(:durative-action infer-from-4 
		:parameters (?C - concept ?Cbase1 - concept ?Cbase2 - concept ?Cbase3 - concept ?Cbase4 - concept ?Cost - cost)
		(:preconditions
			(?P1 (inferring ?Cbase1))
			(?P2 (inferring ?Cbase2))
			(?P3 (inferring ?Cbase3))
			(?P4 (inferring ?Cbase4))
		)
		:effect (and
			(?E1 (inferring ?C))
		)
		(:constraints
			(:temporal
				(equals ?THIS ?E1)
				(during ?THIS ?P1 [1 inf] [1 inf])
				(during ?THIS ?P2 [1 inf] [1 inf])
				(during ?THIS ?P3 [1 inf] [1 inf])
				(during ?THIS ?P4 [1 inf] [1 inf])
			)
			(:prolog kb
				;; node of specific class in current configuration can use the three base concepts to infer the target concept:
				(capability4 ?Cbase1 ?Cbase2 ?Cbase3 ?Cbase4 ?C ?Cost) 
			)
			(:graph
				(edge G ?Cbase1 ?C input-for)
				(edge G ?Cbase2 ?C input-for)
				(edge G ?Cbase3 ?C input-for)
				(edge G ?Cbase4 ?C input-for)
			)
			(:math (eval-int (intervalCost ?E1) (mult (sub (EET ?E1) (LST ?E1)) ?Cost)))
			(:cost (add link-cost (intervalCost ?E1)))
		)
	)

	(:durative-action infer-from-5 
		:parameters (?C - concept ?Cbase1 - concept ?Cbase2 - concept ?Cbase3 - concept ?Cbase4 - concept ?Cbase5 - concept ?Cost - cost)
		(:preconditions
			(?P1 (inferring ?Cbase1))
			(?P2 (inferring ?Cbase2))
			(?P3 (inferring ?Cbase3))
			(?P4 (inferring ?Cbase4))
			(?P5 (inferring ?Cbase5))
		)
		:effect (and
			(?E1 (inferring ?C))
		)
		(:constraints
			(:temporal
				(equals ?THIS ?E1)
				(during ?THIS ?P1 [1 inf] [1 inf])
				(during ?THIS ?P2 [1 inf] [1 inf])
				(during ?THIS ?P3 [1 inf] [1 inf])
				(during ?THIS ?P4 [1 inf] [1 inf])
				(during ?THIS ?P5 [1 inf] [1 inf])
			)
			(:prolog kb
				;; node of specific class in current configuration can use the three base concepts to infer the target concept:
				(capability5 ?Cbase1 ?Cbase2 ?Cbase3 ?Cbase4 ?Cbase5 ?C ?Cost) 
			)
			(:graph
				(edge G ?Cbase1 ?C input-for)
				(edge G ?Cbase2 ?C input-for)
				(edge G ?Cbase3 ?C input-for)
				(edge G ?Cbase4 ?C input-for)
				(edge G ?Cbase5 ?C input-for)
			)
			(:math (eval-int (intervalCost ?E1) (mult (sub (EET ?E1) (LST ?E1)) ?Cost)))
			(:cost (add link-cost (intervalCost ?E1)))
		)
	)

	(:durative-action infer-from-6 
		:parameters (?C - concept ?Cbase1 - concept ?Cbase2 - concept ?Cbase3 - concept ?Cbase4 - concept ?Cbase5 - concept ?Cbase6 - concept ?Cost - cost)
		(:preconditions
			(?P1 (inferring ?Cbase1))
			(?P2 (inferring ?Cbase2))
			(?P3 (inferring ?Cbase3))
			(?P4 (inferring ?Cbase4))
			(?P5 (inferring ?Cbase5))
			(?P6 (inferring ?Cbase6))
		)
		:effect (and
			(?E1 (inferring ?C))
		)
		(:constraints
			(:temporal
				(equals ?THIS ?E1)
				(during ?THIS ?P1 [1 inf] [1 inf])
				(during ?THIS ?P2 [1 inf] [1 inf])
				(during ?THIS ?P3 [1 inf] [1 inf])
				(during ?THIS ?P4 [1 inf] [1 inf])
				(during ?THIS ?P5 [1 inf] [1 inf])
				(during ?THIS ?P6 [1 inf] [1 inf])
			)
			(:prolog kb
				;; node of specific class in current configuration can use the three base concepts to infer the target concept:
				(capability6 ?Cbase1 ?Cbase2 ?Cbase3 ?Cbase3 ?Cbase4 ?Cbase5 ?Cbase6 ?C ?Cost) 
			)
			(:graph
				(edge G ?Cbase1 ?C input-for)
				(edge G ?Cbase2 ?C input-for)
				(edge G ?Cbase3 ?C input-for)
				(edge G ?Cbase4 ?C input-for)
				(edge G ?Cbase5 ?C input-for)
				(edge G ?Cbase6 ?C input-for)
			)
			(:math (eval-int (intervalCost ?E1) (mult (sub (EET ?E1) (LST ?E1)) ?Cost)))
			(:cost (add link-cost (intervalCost ?E1)))
		)
	)
)

