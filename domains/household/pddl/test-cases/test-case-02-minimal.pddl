(define (problem pfile0)
 (:domain household)
 (:objects 
		t1 t2 t3 t4 - robot-task
	)
	(:init 
		;; Initial state
		(at r1 bedroomParents)
		(state t1 vaccuum bedroomParents waiting)
		(at 40 (human-activity mother bedroomParents reading))
		(at 200 (not (human-activity mother bedroomParents reading)))

		(= (execution-time r1 vaccuum) 30)
	)

	;; Preferences
	(:constraints
		;; IC ( LetMeRead ?H ?L ?T ?I1 ?I2 )
		(forall (?H - human)
			(forall (?L - location)
				(forall (?T - robot-task)
					(preference LetMeRead-1
						(always
							(or 
								(and 
									(not (human-activity ?H ?L reading))
									(state ?T vaccuum ?L processing)
								)
								(and 
									(human-activity ?H ?L reading)
									(not (state ?T vaccuum ?L processing))
								)
								(and 
									(not (human-activity ?H ?L reading))
									(not (state ?T vaccuum ?L processing))
								)
							)	
						)
					)
				)
			)
		)
	)
	(:metric minimize (* 100000000 (is-violated LetMeRead-1)))

	(:goal (and
		(state t1 vaccuum bedroomParents finished )
	))
)
