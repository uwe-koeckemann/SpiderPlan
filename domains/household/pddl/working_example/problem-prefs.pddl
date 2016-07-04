
;; Created by Uwe Köckemann

;; Solution: 200.001: (solve-task r1)  [100.000]

(define (problem pfile0)
 (:domain household)
	(:objects r1 - robot)
	(:init 
		;(at 0.1 (human-activity))
		;(human-activity)
		;(at 200 (not (human-activity))) ;; Any valid plan has to start after this
		(at 0 (human-activity))
		(at 20 (not (human-activity)))
		(ready r1)
	)
        (:constraints
            (preference p1 
                (always
                        (or (and 
                              (human-activity)
															(not (making-noise))
                        )

                        (and 
                              (not (human-activity))
															(making-noise)
                        )
												(and 
                              (not (human-activity))
															(not (making-noise))
                        )
										)
                      
                )
            )
						)

	(:goal (task-is-done))
  (:metric minimize (* 10000 (is-violated p1)))
)

