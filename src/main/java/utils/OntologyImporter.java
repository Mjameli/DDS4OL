package utils;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;



public class OntologyImporter {
	
	final Model ontology_model = ModelFactory.createDefaultModel();
	
	public OntologyImporter(String path_to_ontology, String type){
		/*
		 * type could be for example "RDF/XML" or TURTLE
		 */
		ontology_model.read(path_to_ontology,type);
	}
	
	public Set<String> getProperties(){
		Set<String> properties = new HashSet<String>();
		QueryExecution qExec = QueryExecutionFactory.create("SELECT ?p WHERE{?p ?s ?o.}", ontology_model) ;
	    ResultSet rs = qExec.execSelect() ;
	    try {
	    	while ( rs.hasNext() ) {
	        	 QuerySolution qs = rs.next();
	        	 try{
	        		 String uri = qs.get("?p").toString();
	        		 String[] tmp = uri.split("/");
	        		 if(Character.isLowerCase(tmp[tmp.length-1].charAt(0))){
		        		 properties.add(uri);
	        		 }
	        		
	        	 }
	        	 catch(Exception e){
	        		 e.printStackTrace();
	        	 }
	    	}
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
	    qExec.close() ;
		
		
		
		return properties;
		
		
	}
	
	
	
	public Set<String> getClasses(){
		Set<String> properties = new HashSet<String>();
		
		QueryExecution qExec = QueryExecutionFactory.create("SELECT ?p WHERE{?p ?s ?o.}", ontology_model) ;
	    ResultSet rs = qExec.execSelect() ;
	    try {
	    	while ( rs.hasNext() ) {
	        	 QuerySolution qs = rs.next();
	        	 try{
	        		 String uri = qs.get("?p").toString();
	        		 String[] tmp = uri.split("/");
	        		 if(Character.isUpperCase(tmp[tmp.length-1].charAt(0))){
		        		 properties.add(uri);
	        		 }
	        		
	        	 }
	        	 catch(Exception e){
	        		 e.printStackTrace();
	        	 }
	    	}
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
	    qExec.close() ;
		
		
		
		return properties;
		
		
	}
	
	

}
