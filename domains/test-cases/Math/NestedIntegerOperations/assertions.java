ValueLookup values = resultCore.getContext().getUnique(ValueLookup.class);;
assertTrue(values.getInt(Term.createConstant("x")) == 42);	
