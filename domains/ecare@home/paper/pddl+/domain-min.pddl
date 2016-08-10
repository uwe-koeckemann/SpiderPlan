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
		(at ?s - sensor ?l - location)
		(capabilitySensorSimple ?s - sensor ?c - concept)
		(capabilitySensorTarget ?s - sensor ?c - concept ?o - object)
		(capabilitySensorConfig ?s - sensor ?c - concept ?conf - configuration)
		(capabilitySensorLocation ?s - sensor ?c - concept ?l - location)
		
		(inferring ?c - concept)
	)
	(:constants
		s1 - sensor
		c1 - concept
	)
	(:functions
		(energy)
		(cost ?s - sensor ?c - concept)
	)
	
	(:durative-action infer-sensor 
		:parameters (?Sensor - sensor ?C - concept)
		:duration (= ?duration 1)
		:condition (and 
			(over all (capabilitySensorSimple ?Sensor ?C) ) )
		:effect (and
			(at start (inferring ?C))
			(at end (not (inferring ?C)))
			(increase (energy) (* #t (cost ?Sensor ?C))) )
	)	
)

