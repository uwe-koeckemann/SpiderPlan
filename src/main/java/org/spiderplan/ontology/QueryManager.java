package org.spiderplan.ontology;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.logic.Term;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * This class creates queries and executes them. 
 * 
 * @author Marjan Alirezaie
 * @author Uwe Köckemann
 * 
 */
public class QueryManager {
	protected final static String UDDL_STRING_SIGN = "\"";
	protected final static String UDDL_SPACE_SIGN = " ";
	protected final static String UDDL_VARIABLE_SIGN = "?";
	protected final static String SPARQL_VARIABLE_SIGN = "?";
	protected final static String SPARQL_PREFIX = "PREFIX";
	
	private List<Prefix> prefixes;
	private List<Triple> triples;
	
	private static String URI_ENTITY_SPLITTER = "#";
	
	protected OntModel model;	
	
	// variables are assumed to not be preceded by question marks
//	private List<String> variables;
	
	/**
	 * Create a new manager with a path to the ontology.
	 * 
	 * @param ontologyPath
	 */
	public QueryManager( String ontologyPath ) {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.DEBUG);
	
		model = ModelFactory.createOntologyModel();//new OntModelSpec(OntModelSpec.OWL_DL_MEM));
		
		try {
			if ( ontologyPath.contains("http") ) {
//				InputStream iS = new URL(ontologyPath).openStream();
//				
//				Scanner s = new java.util.Scanner(iS);
//			    s.useDelimiter("\\A");
//
//			    String streamString = s.hasNext() ? s.next() : "";
//			    s.close();
//				System.out.println( streamString ); 
				
//				model.read(iS, "RDF/XML-ABBREV");
				model.read(new URL(ontologyPath).openStream(), null);
			} else {
				model.read(new FileInputStream(new File(ontologyPath)), null);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
				
		this.prefixes = new ArrayList<>();
		this.triples = new ArrayList<>();
	}
	
	/**
	 * Add a prefix to the query.
	 * @param prefix
	 */
	public void addPrefix(Prefix prefix) {
		prefixes.add(prefix);
	}
	/**
	 * Add a triple to the query.
	 * @param triple
	 */
	public void addTriple(Triple triple) {
		triples.add(triple);
	}
	
	/**
	 * Run the query (i.e., all added prefixes and triples)
	 * and return an answer.
	 * 
	 * TODO: Verify that return works as described below.
	 * 
	 * @return the answer to the query. An empty list means the query could not be satisfied, 
	 * a single empty substitution means the query is true without substitution (i.e., the triples 
	 * formed a true sentence but did not contain variables).
	 */
	public List<Substitution> query() {
		// Collect variables from all triples:	
		List<String> variables = new ArrayList<String>();
			
		for (Triple triple : triples) {
			for ( String tripVariable : triple.getVariables() ) {
				if ( !variables.contains(tripVariable) ) {
					variables.add(tripVariable);
				}
			}
		}
		List<Substitution> queryResult = new ArrayList<Substitution>();
		String sparql = getSPARQLQuery(variables);
		
		System.out.println(sparql);
		
		List <QuerySolution> solutionList = getQueryResultSet(sparql);
		
		for (QuerySolution solution : solutionList) {
			Substitution singleSolution = new Substitution();
			for (String var : variables) {
				Term variableTerm = Term.createVariable(var);
				Term resourceTerm = Term.createConstant(solution.getResource(var).toString().split(URI_ENTITY_SPLITTER)[1]);
				
				singleSolution.add(variableTerm, resourceTerm);
			}
			queryResult.add(singleSolution);
		}

		return queryResult;
	}	

	
	/**
	 * Given the lists of prefixes, triples, and variables, this method generates a SPARQL query
	 * @return a SPARQL query string
	 */
	private String getSPARQLQuery( List<String> variables ) {
		StringBuilder sb = new StringBuilder();
		
		// generating the PREFIX phrase in SPARQL
		for (Prefix prefix : prefixes) {
			sb.append(prefix.getSPARQLPrefix());
			sb.append("\n");
		}
		
		sb.append("\n");
		
		// generating the SELECT phrase in SPARQL
		sb.append("SELECT DISTINCT ");
		for (String var : variables) {
			sb.append(SPARQL_VARIABLE_SIGN);
			sb.append(var);
			sb.append(" ");
		}
		
		sb.append("\n");
		
		// generating the WHERE phrase in SPARQL
		sb.append("WHERE \n{\n");
		
		for (Triple triple : triples) {
			sb.append(triple.getSPARQLTriple());
			sb.append("\n");
		}
		
		// this method return 
		sb.append("}");
		
		return sb.toString();
	}
		
	/**
	 * This method executes the given (sparql) queryString
	 * @param queryString
	 * @return
	 */
	private List <QuerySolution> getQueryResultSet(String queryString) {
		
		List <QuerySolution> resultList;
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		
		try {	
			ResultSet resultSet = qexec.execSelect();	
			resultList = ResultSetFormatter.toList(resultSet);
		}
		catch(Exception e) {
			resultList = null;
		}
		finally{
			qexec.close();
		}
		
		return resultList;
	}
}
