ConstraintDatabase resultCDB = resultCore.getContext();
ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 35);
assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 40);
assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 35);
assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 100);
