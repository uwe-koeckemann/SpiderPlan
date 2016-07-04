(define (domain future-events)
  (:requirements :typing :durative-actions :timed-initial-literals)
  (:types 	robot - object 
					location - object
         )

(:predicates 
	(robot-at ?r - robot ?l - location)
)

(:durative-action move
  :parameters (?r - robot ?l1 - location ?l2 - location)
  :duration (= ?duration 100)
  :condition (and 
			(at start (robot-at ?r ?l1)) 
	)
  :effect (and 
			(at start (not (robot-at ?r ?l1))) 
			(at end (robot-at ?r ?l2)))
)
)