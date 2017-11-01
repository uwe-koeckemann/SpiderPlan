package org.spiderplan.ontology;

import java.util.ArrayList;
import java.util.List;

/**
 * An ontology triple (used to express queries).
 *
 * @author Marjan Alirezaie
 * @author Uwe Köckemann
 *
 */
public class NotTriple {
	private String subject = "";
	private String predicate = "";
	private String object = "";
	
	private List<String> variables = new ArrayList<String>();
	

	/**
	 * Create a new triple providing subject, predicate, and object strings
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	public NotTriple(String subject, String predicate, String object) {
		this.subject = getQueryTerm(subject);
		this.predicate = getQueryTerm(predicate);
		this.object = getQueryTerm(object);
	}
		
	/**
	 * Get the variables used in this triple
	 * @return a list of variables
	 */
	public List<String> getVariables() {
		return this.variables;
	}
		
	/**
	 * This method removes UDDL_STRING_SIGN from each given query term. It also checks if the given term is a variable or not
	 * @param term
	 * @return
	 */
	private String getQueryTerm(String term) {
		if (term.length() > 1) {
			if (term.startsWith(QueryManager.UDDL_STRING_SIGN) && term.endsWith(QueryManager.UDDL_STRING_SIGN))
				return term.substring(1, term.length() - 1);
			
			// check if the given term is variable or not
			else if (term.startsWith(QueryManager.UDDL_VARIABLE_SIGN))
				variables.add(term.substring(1)); // add the variable to the variable list without the preceding question mark
			
			return term;
		}
		else return "";
	}
	
	/**
	 * Turn this triple into SPARQL syntax.
	 * @return String in SPARQL that can be added to SPARQL query
	 */
	public String getSPARQLTriple() {
		StringBuilder sb = new StringBuilder();
		if (this.subject.length() > 0 && this.predicate.length() > 0 && this.object.length() > 0) {
			sb.append(this.subject);
			sb.append(" ");
			sb.append(this.predicate);
			sb.append(" ");
			sb.append(this.object);
			sb.append(".");
		}
		return sb.toString();
	}
}
