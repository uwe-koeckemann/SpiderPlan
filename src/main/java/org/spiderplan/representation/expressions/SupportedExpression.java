package org.spiderplan.representation.expressions;

import java.util.ArrayList;
import java.util.List;

/**
 * A single entry for a supported expression.
 * 
 * @author Uwe Köckemann
 *
 * @param <T> enumeration of relations used by the entries
 */
public class SupportedExpression<T> {
	
	private List<String> names = new ArrayList<String>();
	private String exampleUsage;
	private String helpText;
	private T relation;
	private Class<? extends Expression> c;
	
	/**
	 * Create a new entry.
	 * @param uniqueName string of the form <code>name/arity</code>
	 * @param exampleUsage example usage of this expression
	 * @param helpText explanation of the meaning of this expression 
	 * @param relation internally used symbol to represent this expression
	 * @param c class that is used to represent objects of this expression
	 */
	public SupportedExpression( String uniqueName, String exampleUsage, String helpText, T relation, Class<? extends Expression> c ) {
		this.names.add(uniqueName);
		this.helpText = helpText;
		this.relation = relation;
		this.c = c;
	}
	
	/**
	 * Add alternative name to this entry.
	 * @param name the name to be added
	 */
	public void addName( String name ) {
		if ( !this.names.contains(name) ) {
			this.names.add(name);	
		}
	}
	
	/**
	 * Get all names of this entry.
	 * @return list of names
	 */
	public List<String> getNames() {
		return names;
	}
	
	/**
	 * Get relation used by this entry.
	 * @return the relation
	 */
	public T getRelation() {
		return relation;
	}
	
	/**
	 * Get class that represents this entry.
	 * @return the class
	 */
	public Class<? extends Expression> getImplementingClass() {
		return c;
	}
	
	@Override
	public String toString() {
		StringBuilder sB = new StringBuilder();
		sB.append(names.toString());
		sB.append("\n\tUsage: ");
		sB.append(exampleUsage);
		sB.append("\n\tMeaning: ");
		sB.append(helpText);
		sB.append("\n\tInternal representation: ");
		sB.append(relation.getClass().getSimpleName()); 
		sB.append(".");
		sB.append(relation.toString());
		sB.append("\n\tImplemented by: ");
		sB.append(c.toString());
		sB.append("\n");
		return sB.toString();
	}
}