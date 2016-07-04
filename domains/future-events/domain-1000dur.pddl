(define (domain future-events)
  (:requirements :typing :durative-actions :timed-initial-literals)
  (:types 	robot - object 
					location - object
					door - object
					key - object
         )

(:predicates 
	(robot-at ?r - robot ?l - location)
	(is-open ?d - door)
	(door-between ?l1 - location ?l2 - location ?d - door )
	(door-at ?d - door ?l - location )
	(key-at ?k - key ?l - location )
	(needs-key ?d - door ?k - key)
	(holding ?r - robot ?k - key )
)

(:durative-action move
  :parameters (?r - robot ?l1 - location ?l2 - location ?d - door)
  :duration (= ?duration 1000)
  :condition (and 
			(at start (robot-at ?r ?l1)) 
			(at start (is-open ?d)) 
			(at end (is-open ?d)) 
			(over all (is-open ?d)) 
			(at start (door-between ?l1 ?l2 ?d)) 
	)
  :effect (and 
			(at start (not (robot-at ?r ?l1))) 
			(at end (robot-at ?r ?l2)))
)

(:durative-action open-door
  :parameters (?r - robot ?l - location ?d - door ?k - key)
	:duration (=  ?duration 10)
  :condition (and 
			(over all (door-at ?d ?l)) 
			(over all (robot-at ?r ?l))
			(over all (holding ?r ?k))  
			(over all (needs-key ?d ?k))
	)
  :effect (and 
			(at end (is-open ?d))
	)
)


(:durative-action pickup-key
  :parameters (?r - robot ?k - key ?l - location)
	:duration (=  ?duration 10)
  :condition (and 
			(at start(key-at ?k ?l)) 
			(over all (robot-at ?r ?l))
	)
  :effect (and 
			(at end (holding ?r ?k))
			(at end (not (key-at ?k ?l)))
	)
)
)