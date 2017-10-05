package org.spiderplan.ontology;
import java.util.List;

import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.ontology.OntologyExpression;
import org.spiderplan.representation.logic.Term;


/**
 * Static class that takes {@link OntologyExpression} converts them and 
 * creates and runs a query through QueryManager. 
 * 
 * @author Marjan Alirezaie
 * @author Uwe Köckemann
 *
 */
public class OntologyAdapter {
	
	/**
	 * @param ontologyPath Path to main ontology
	 * @param prefixes Prefixes used by this query
	 * @param triples The query itself in form of triples
	 * @return List of possible substitutions of variables in the query 
	 */
	public static List<Substitution> query( String ontologyPath, List<OntologyExpression> prefixes, List<OntologyExpression> triples ) {
		QueryManager queryGenerator = new QueryManager(ontologyPath);
		
		for ( OntologyExpression oE : prefixes ) { 
			Term expTerm = oE.getExpression();
			String name = expTerm.getArg(0).toString();
			String uri = expTerm.getArg(1).toString().replace("\"", "");
			queryGenerator.addPrefix(new Prefix(name, uri));
		}
		
		for ( OntologyExpression oE : triples ) {
			Term expTerm = oE.getExpression();
			String subject = expTerm.getArg(0).toString().replace("\"", "");
			String predicate = expTerm.getArg(1).toString().replace("\"", "");
			String object = expTerm.getArg(2).toString().replace("\"", "");
			
			queryGenerator.addTriple(new Triple(subject, predicate, object));
		}
						
		return queryGenerator.query();
	}
}
