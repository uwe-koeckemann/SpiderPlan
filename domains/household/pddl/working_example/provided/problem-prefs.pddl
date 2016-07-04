
;; Created by Uwe Köckemann

;; Solution: 200.001: (solve-task r1)  [100.000]

(define (problem pfile0)
 (:domain household)
	(:objects r1 - robot)
	(:init 
		;(at 0.1 (human-activity))
		;(human-activity)
		;(at 200 (not (human-activity))) ;; Any valid plan has to start after this
		(at 200 (not-human-activity))
		(ready r1)
	)
        (:constraints
            (preference p1 
                (always
                        (not 
                                (and 
                                        (not (not-human-activity))
                                        (making-noise)
                                )
                        )	
                )
            )
        )

	(:goal (task-is-done))
(:metric minimize (is-violated p1))
)

