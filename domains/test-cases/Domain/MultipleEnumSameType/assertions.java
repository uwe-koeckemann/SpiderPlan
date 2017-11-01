TypeManager tM = resultCore.getTypeManager();
assertTrue(tM.getTypeNames().size() == 3);
assertTrue(tM.getTypeByName(Term.createConstant("t")).generateDomain(tM).size() == 4);
