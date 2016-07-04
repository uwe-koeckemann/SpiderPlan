;; Created by Uwe Köckemann

(define (domain household)
	(:requirements :typing :durative-actions :timed-initial-literals :universal-preconditions :negative-preconditions)
	(:types robot - object)
	(:predicates 	
		(ready ?r - robot)
		(task-is-done)
		(human-activity)
	)
	(:durative-action solve-task
		:parameters (?r - robot)
		:duration (= ?duration 100)
		:condition 
			(and 
				(over all (ready ?r))
				(over all (not (human-activity)))
			)
		:effect (and (at end (task-is-done)))
	)
)
