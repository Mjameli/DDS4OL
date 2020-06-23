/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lexicon;

import static Lexicon.LexiconInterface.ontLexDefault_graph;
import static Lexicon.LexiconInterface.ontLexServer;
import static Lexicon.LexiconInterface.usewordnet;
import ir.ac.ui.firstqa.WordNet.WordNet;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import edu.mit.jwi.item.POS;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import sparql.SPARQL_ElementSimple;

/**
 *
 * @author Admin
 */
public class Enricher {

    public static void main(String[] args) {

//Generate the ClassNoun entries for all Dbpedia ontology classes.
        Map<String, String> classesList;
        classesList = getOntologyClasses();
        System.out.println(classesList);
        writeClassesToFile(classesList, "resources/DbpediaOntologyClasses.ldp");
        
        
        //Duplicate triples that their property has an equivalent in dbpedia properties.
        //e.g. dbo:spouse  --> dbp:spouse
        Map<String, String> rpts = getReferencedPropertyTriples();
        System.out.println(rpts);

         generateTripleFile(rpts,"resources/referencedProperty.nt" );
    }

    //extract all ontology classes from the datastroe
    public static Map<String, String> getOntologyClasses() {
        Map<String, String> resultclasses = new HashMap<>();

        String query = "select distinct * where {?cl a <http://www.w3.org/2002/07/owl#Class>}";

        QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, "");

        try {
            ResultSet results = qe.execSelect();

            for (; results.hasNext();) {

                QuerySolution sol = (QuerySolution) results.next();

                String classStr = sol.get("?cl").toString();

                if (!classStr.contains("dbpedia.org")) {
                    continue;
                }

                resultclasses.put(classStr, getClassPrettyString(classStr));

            }

        } catch (Exception e) {
            System.out.println("Mehdi: Error in executing the query");
            System.out.println("Mehdi: Most probly, Sparql Endpoint is down.");
            System.out.println("Mehdi: Try your query in following URL(JUST THIS URL)");
            System.out.println("Mehdi: " + ontLexServer);
            System.out.println("your query is:\n" + query);
            e.printStackTrace();

        } finally {

            qe.close();
        }

        return resultclasses;
    }

    private static String getClassPrettyString(String classStr) {
        String result = "";
        classStr = classStr.substring(classStr.lastIndexOf("/") + 1);
        for (String part : classStr.split("(?=[A-Z])")) //split over capital letters
        {
            result += part.toLowerCase() + " ";

        }
        result = result.trim();
        return result;
    }

    private static void writeClassesToFile(Map<String, String> classesList, String filename) {
        try {
            FileWriter fw = new FileWriter(filename);
            fw.append("@prefix dbpedia: <http://dbpedia.org/ontology/> .\n"
                    + "@prefix lex: <http://github.com/cunger/lemon.dbpedia/target/dbpedia_all#> .\n"
                    + "\n"
                    + "Lexicon(<http://github.com/cunger/lemon.dbpedia/target/dbpedia_en_22#>,\"en\"," + "\n");

            for (String key : classesList.keySet()) {
                String classPath = "dbpedia:" + key.substring(key.lastIndexOf("/") + 1);
                fw.append("ClassNoun(\"" + classesList.get(key) + "\", " + classPath + "),\n");
            }

            fw.append(")");
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(Enricher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Map<String, String> getReferencedPropertyTriples() {
        Map<String, String> resultPairs = new HashMap<>();

        String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "select distinct * where {?sense <http://lemon-model.net/lemon#reference> ?ref.\n"
                + "?ref a rdf:Property.\n"
                + "filter regex(?ref , \"dbpedia.org/ontology\", \"i\").\n"
                + "filter regex(?sense, \"github.com\", \"i\").\n"
                + "}";

        QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, "");

        try {
            ResultSet results = qe.execSelect();

            for (; results.hasNext();) {

                QuerySolution sol = (QuerySolution) results.next();

                String senseStr = sol.get("?sense").toString();
                String refStr = sol.get("?ref").toString();

                refStr = refStr.replace("/ontology/", "/property/");
                
                if (resultPairs.containsValue(refStr)) // it's existance has been checked before
                {
                    resultPairs.put(senseStr, refStr);
                } else if (existInDbpediaProperties(refStr)) {
                    resultPairs.put(senseStr, refStr);
                }

            }

        } catch (Exception e) {
            System.out.println("Mehdi: Error in executing the query");
            System.out.println("Mehdi: Most probly, Sparql Endpoint is down.");
            System.out.println("Mehdi: Try your query in following URL(JUST THIS URL)");
            System.out.println("Mehdi: " + ontLexServer);
            System.out.println("your query is:\n" + query);
            e.printStackTrace();

        } finally {

            qe.close();
        }

        return resultPairs;
    }

    private static boolean existInDbpediaProperties(String refStr) {
        String endpoint = "http://dbpedia.org/sparql";
        String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "ask where {<" + refStr + "> a rdf:Property.}";
        QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, queryString);
        boolean answer = true;
        try {
            answer = qe.execAsk();
        } catch (Exception e) {
            System.out.println("Mehdi: Error in executing the query");
            System.out.println("Mehdi: Most probably, Sparql Endpoint is down.");
            System.out.println("Mehdi: Try your query in following URL(JUST THIS URL)");
            System.out.println("Mehdi: " + endpoint);
            System.out.println("your query is:\n" + queryString);
            e.printStackTrace();

        } finally {

            qe.close();
        }
        return answer;
    }

    private static void generateTripleFile(Map<String, String> rpts, String filename) {
        try {
            FileWriter fw = new FileWriter(filename);
           
            for (String key : rpts.keySet()) {
                String tripleStr = "<"+key+"> <http://lemon-model.net/lemon#reference> <" + rpts.get(key)+">.\r\n";
                fw.append(tripleStr);
            }

            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(Enricher.class.getName()).log(Level.SEVERE, null, ex);
        }}

}
