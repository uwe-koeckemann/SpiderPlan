;; Created by Uwe Köckemann

(define (domain household)
	(:requirements :typing :durative-actions :constraints :timed-initial-literals :universal-preconditions :negative-preconditions)
	(:types robot - object)
	(:predicates
		(ready ?r - robot)
		(task-is-done)
		(not-human-activity)
		(making-noise)
	)

	(:durative-action solve-task
		:parameters (?r - robot)
		:duration (= ?duration 100)
		:condition 
			(and (over all (ready ?r)))
		:effect 
			(and 
				(at start (making-noise))
				(at end (not (making-noise)))
				(at end (task-is-done))
			)
	)
)

