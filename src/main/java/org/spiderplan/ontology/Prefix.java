package org.spiderplan.ontology;


/**
 * An ontology prefix. These determine the vocabulary that can be used
 * in prefixes.
 * 
 * @author Marjan Alirezaie
 * @author Uwe Köckemann
 *
 */
public class Prefix {
	private String name;
	private String uri;
	
	/**
	 * Create a new prefix by providing its short name
	 * and a URI.
	 * @param name
	 * @param uri
	 */
	public Prefix( String name, String uri ) {
		this.name = name;
		this.uri = uri;
	}
		
	/**
	 * Turn this prefix into SPARQL syntax.
	 * @return String in SPARQL that can be added to SPARQL query
	 */
	public String getSPARQLPrefix() {
		StringBuilder sb = new StringBuilder();
		sb.append(QueryManager.SPARQL_PREFIX);
		sb.append(" ");
		sb.append(this.name);
		sb.append(":");
		sb.append(this.uri);
		return sb.toString();
	}
}
