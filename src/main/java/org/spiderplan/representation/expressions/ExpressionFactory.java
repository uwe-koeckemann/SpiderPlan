//package org.spiderplan.representation.expressions;
//package org.spiderplan.representation.constraints;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//import java.util.List;
//
//import org.spiderplan.modules.configuration.ConfigurationManager;
//import org.spiderplan.modules.solvers.Module;
//import org.spiderplan.representation.constraints.SupportedExpressions.SupportedEntry;
//import org.spiderplan.representation.logic.Term;
//import org.spiderplan.tools.Loop;
//
///*
// * TODO: Term constructor for all constraints 
// * TODO: look-up type to get SupportedExpressions
// * TODO: get class and constructor
// * TODO: call default constructor with Term
// * TODO: distinction between constraints, queries, programs, etc. 
// * 			what would be the common name? "Information"?
// */
///**
// * Create constraints given their term representation and type.
// * 
// * @author Uwe Köckemann
// */
//public class ExpressionFactory {
//	
//	@SuppressWarnings("rawtypes")
//	public static Expression createConstraint( Term conType, List<Term> termRep ) {
//		Expression c = null;
//
//		try {
//			String uName = termRep.getUniqueName();
//			
//			SupportedExpression entry = ExpressionTypes.getSupportedExpressions(conType).getSupportedEntry(uName);
//			
//			if ( entry == null ) {
//				throw new IllegalArgumentException("Term " + termRep + " not supported by type " + conType + "\nSupported expressions are:\n" + ConstraintTypes.getSupportedExpressions(conType).toString());
//			}
//			
//			Class constraintClass = entry.getImplementingClass();
//			
//			Class termClass = Class.forName("org.spiderplan.representation.logic.Term");
//	
//			@SuppressWarnings("unchecked")
//			Constructor constructor = constraintClass.getConstructor(termClass);
//			c = (Expression)constructor.newInstance(termRep);
//						
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//			Loop.start();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//			Loop.start();	
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//			Loop.start();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//			Loop.start();
//		} catch (InvocationTargetException e) {
//			e.getTargetException().printStackTrace();
//			e.printStackTrace();
//			Loop.start();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//			Loop.start();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//			Loop.start();
//		} 
//		
//		return c;
//	}
//}
