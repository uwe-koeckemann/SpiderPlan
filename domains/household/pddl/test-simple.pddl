(define (problem problem1)
	(:domain household)
	(:init
		(at r1 robotroom)
		(adjacent robotroom basement )

		(= (movement-time robotRoom basement) 1)
	)
	(:goal (and
		(at r1 basement)
	))
)
