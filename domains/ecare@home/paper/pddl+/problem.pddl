(define (problem p01)
 (:domain configuration-planning)
	(:objects r1 - robot)
	(:init 
		(capabilitySensorSimple s1 c1)
		(= (cost s1 c1) 10)
	)
	(:goal 
		(and
			(hold-during 10 20 (inferring c1))
		)
	)
	
	(:metric minimize (energy))
)
