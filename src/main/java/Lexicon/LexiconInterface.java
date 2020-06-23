/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lexicon;

import java.util.ArrayList;
import java.util.List;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import ir.ac.ui.firstqa.WordNet.WordNet;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.SimpleStemmer;
import edu.mit.jwi.morph.WordnetStemmer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Admin
 */
public class LexiconInterface {

    public static boolean usewordnet = false;
    //public static double superClassDecayFactor = 0.5;

    public static String ontLexServer = "http://127.0.0.1:8890/sparql";
    //public static String ontLexDefault_graph = "lemonwithextraWithMyAddition";
//    public static String ontLexDefault_graph = "lemonwithextraWithMyAdditionPlusAutoAdded";
    public static String ontLexDefault_graph = "DBpediaGoldLexicon";
    //   public static String ontLexDefault_graph = "dblexpedia";
//    public static String ontLexDefault_graph = "dblexpediaPlusWalterAdjective2017";

    public static String dbpedia_OWL_graph = "DBPediaOntology";

    public static List<StateVerb> getStateVerbs(String inp_verb) {
        List<StateVerb> resultSV = new ArrayList<>();

        String query;

        Map<String, Double> verbs;

        if (usewordnet) {
            verbs = WordNet.INSTANCE.getAllSynAndHomonymsWithSimiliarity(POS.VERB, inp_verb);

            if (verbs.isEmpty()) {
                System.out.println("Info:There is not any synonym for the verb:" + inp_verb);
            }

        } else {
            verbs = new HashMap<>();
        }

        verbs.put(inp_verb, 1.0);

        for (String verb : verbs.keySet()) {

            query = "select distinct ?v ?ref  ?subjP ?objP where {"
                    + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://lemon-model.net/lemon#LexicalEntry> ."
                    + " { ?s <http://www.lexinfo.net/ontology/2.0/lexinfo#partOfSpeech>  <http://www.lexinfo.net/ontology/2.0/lexinfo#verb> .}"
                    + " Union { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://www.lexinfo.net/ontology/2.0/lexinfo#VerbPhrase> .}"
                    + "  ?s <http://lemon-model.net/lemon#canonicalForm>  ?canonicalForm ."
                    + "  ?canonicalForm <http://lemon-model.net/lemon#writtenRep> ?v. "
                    + "?s <http://lemon-model.net/lemon#sense> ?sense."
                    + "?sense <http://lemon-model.net/lemon#objOfProp> ?oP."
                    + "?sense <http://lemon-model.net/lemon#subjOfProp> ?sP."
                    + "?sense <http://lemon-model.net/lemon#reference> ?ref."
                    + "?s <http://lemon-model.net/lemon#synBehavior> ?sb."
                    + "?sb ?objP ?oP."
                    + "?sb ?subjP ?sP."
                    + "optional{?s <http://lemon-model.net/lemon#language> \"en\".}"
                    + "filter (str(?v)='" + verb + "')"
                    + "}";

            QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, ontLexDefault_graph);
//            QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, ontLexDefault_graph);

            try {
                ResultSet results = qe.execSelect();       //Notice: for ASK queries, you must run qe.execASK();

                for (; results.hasNext();) {

                    QuerySolution sol = (QuerySolution) results.next();
                    /*
                     for (String var : results.getResultVars()) {
                     System.out.println(sol.get(var));
                     }
                     */
                    String resVerb = sol.get("?v").toString().replace("@en", "");
                    String resRef = sol.get("?ref").toString();
                    String subjStr = sol.get("?subjP").toString();
                    String objStr = sol.get("?objP").toString();

                     subjStr = subjStr.substring(Math.max(subjStr.indexOf("#"), subjStr.lastIndexOf("/")) + 1);
                                                objStr = objStr.substring(Math.max(objStr.indexOf("#"), objStr.lastIndexOf("/")) + 1);

                    GrammaticalType subj = null;
                    GrammaticalType obj = null;

                    subj = GrammaticalType.valueof(subjStr);
                    obj = GrammaticalType.valueof(objStr);

                    if (((subj == null)) || ((obj == null))) {
                        System.err.println("Can not detect subject or object of the verb: " + sol.get("?v") + "(" + sol.get("?ref") + ")");

                    } else {
                        StateVerb sv = new StateVerb(resVerb, resRef, subj, obj, verbs.get(verb));
                        if (sv.getVerb().contains("@de") || sv.getVerb().contains("@es")) {//language other than english}    
                            //there is a problem in sparql optional condition.   
                        }
                        if (!resultSV.contains(sv)) {
                            resultSV.add(sv);
                        }
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
        }
        return resultSV;

    }

    public static List<StateVerb> getAllStateVerbs() {
        List<StateVerb> resultSV = new ArrayList<>();

        String query;


        



            query = "select distinct ?v ?ref  ?subjP ?objP where {"
                    + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://lemon-model.net/lemon#LexicalEntry> ."
                    + " { ?s <http://www.lexinfo.net/ontology/2.0/lexinfo#partOfSpeech>  <http://www.lexinfo.net/ontology/2.0/lexinfo#verb> .}"
                    + " Union { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://www.lexinfo.net/ontology/2.0/lexinfo#VerbPhrase> .}"
                    + "  ?s <http://lemon-model.net/lemon#canonicalForm>  ?canonicalForm ."
                    + "  ?canonicalForm <http://lemon-model.net/lemon#writtenRep> ?v. "
                    + "?s <http://lemon-model.net/lemon#sense> ?sense."
                    + "?sense <http://lemon-model.net/lemon#objOfProp> ?oP."
                    + "?sense <http://lemon-model.net/lemon#subjOfProp> ?sP."
                    + "?sense <http://lemon-model.net/lemon#reference> ?ref."
                    + "?s <http://lemon-model.net/lemon#synBehavior> ?sb."
                    + "?sb ?objP ?oP."
                    + "?sb ?subjP ?sP."
                    + "optional{?s <http://lemon-model.net/lemon#language> \"en\".}"
                    + "}";

            QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, ontLexDefault_graph);
//            QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, ontLexDefault_graph);

            try {
                ResultSet results = qe.execSelect();       //Notice: for ASK queries, you must run qe.execASK();

                for (; results.hasNext();) {

                    QuerySolution sol = (QuerySolution) results.next();
                    /*
                     for (String var : results.getResultVars()) {
                     System.out.println(sol.get(var));
                     }
                     */
                    String resVerb = sol.get("?v").toString().replace("@en", "");
                    String resRef = sol.get("?ref").toString();
                    String subjStr = sol.get("?subjP").toString();
                    String objStr = sol.get("?objP").toString();

                     subjStr = subjStr.substring(Math.max(subjStr.indexOf("#"), subjStr.lastIndexOf("/")) + 1);
                                                objStr = objStr.substring(Math.max(objStr.indexOf("#"), objStr.lastIndexOf("/")) + 1);

                    GrammaticalType subj = null;
                    GrammaticalType obj = null;

                    subj = GrammaticalType.valueof(subjStr);
                    obj = GrammaticalType.valueof(objStr);

                    if (((subj == null)) || ((obj == null))) {
                        System.err.println("Can not detect subject or object of the verb: " + sol.get("?v") + "(" + sol.get("?ref") + ")");

                    } else {
                        StateVerb sv = new StateVerb(resVerb, resRef, subj, obj, 1.0);
                        if (sv.getVerb().contains("@de") || sv.getVerb().contains("@es")) {//language other than english}    
                            //there is a problem in sparql optional condition.   
                        }
                        if (!resultSV.contains(sv)) {
                            resultSV.add(sv);
                        }
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
        
        return resultSV;

    }

    public static List<RelationalAdjective> getRelationalAdjectives(String inp_adj) {
        List<RelationalAdjective> resultRA = new ArrayList<>();

        String query;
        Map<String, Double> adjs;

        if (usewordnet) {
            adjs = WordNet.INSTANCE.getAllSynAndHomonymsWithSimiliarity(POS.ADJECTIVE, inp_adj);

            if (adjs.isEmpty()) {
                System.out.println("Info:There is not any synonym for the adjective:" + inp_adj);
            }
        } else {
            adjs = new HashMap<>();
        }

        adjs.put(inp_adj, 1.0);

        for (String adj : adjs.keySet()) {

            query = "select distinct ?v ?ref  ?subjP ?objP where {"
                    + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://lemon-model.net/lemon#LexicalEntry> ."
                    + "  ?s <http://www.lexinfo.net/ontology/2.0/lexinfo#partOfSpeech>  <http://www.lexinfo.net/ontology/2.0/lexinfo#adjective> ."
                    + "  ?s <http://lemon-model.net/lemon#canonicalForm>  ?canonicalForm ."
                    + "  ?canonicalForm <http://lemon-model.net/lemon#writtenRep> ?v. "
                    + "?s <http://lemon-model.net/lemon#sense> ?sense."
                    + "?sense <http://lemon-model.net/lemon#objOfProp> ?oP."
                    + "?sense <http://lemon-model.net/lemon#subjOfProp> ?sP."
                    + "?sense <http://lemon-model.net/lemon#reference> ?ref."
                    + "?s <http://lemon-model.net/lemon#synBehavior> ?synBe."
                    + "?synBe ?objP ?oP."
                    + "?synBe ?subjP ?sP."
                    + "filter regex(?v, '" + adj + "', 'i')"
                    + "}";

            QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, ontLexDefault_graph);
            try {
                ResultSet results = qe.execSelect();       //Notice: for ASK queries, you must run qe.execASK();

                for (; results.hasNext();) {

                    QuerySolution sol = (QuerySolution) results.next();
                    /*                for (String var : results.getResultVars()) {
                     System.out.println(sol.get(var));
                     }
                     */
                    String resVerb = sol.get("?v").toString().replace("@en", "");
                    String resRef = sol.get("?ref").toString();
                    String subjStr = sol.get("?objP").toString();
                    String objStr = sol.get("?subjP").toString();

                            subjStr = subjStr.substring(Math.max(subjStr.indexOf("#"), subjStr.lastIndexOf("/")) + 1);
                                                objStr = objStr.substring(Math.max(objStr.indexOf("#"), objStr.lastIndexOf("/")) + 1);

                    GrammaticalType subj = null;
                    GrammaticalType obj = null;

                    subj = GrammaticalType.valueof(subjStr);
                    obj = GrammaticalType.valueof(objStr);

                    if (((subj == null)) || ((obj == null))) {
                        System.err.println("Can not detect subject or object of relational adjective: " + sol.get("?v") + "(" + sol.get("?ref") + ")");

                    } else {
                        RelationalAdjective ra = new RelationalAdjective(resVerb, resRef, subj, obj, adjs.get(adj));

                        if (!resultRA.contains(ra)) {
                            resultRA.add(ra);
                        }
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
        }
        return resultRA;

    }

    public static List<ClassNoun> getClassNouns(String inp_noun) {
        List<ClassNoun> resultCN = new ArrayList<>();

        String query;
        String lextypecond;

        for (String suffix : getOrderedSuffix(inp_noun)) {
            Map<String, Double> nouns;

            if (usewordnet) {
                nouns = WordNet.INSTANCE.getAllSynAndHomonymsWithSimiliarity(POS.NOUN, suffix);

                if (nouns.isEmpty()) {

                    System.out.println("Info:There is not any synonym for the noun:" + suffix);
                }
            } else {
                nouns = new HashMap<>();
            }

            nouns.put(suffix, 1.0);

            for (String noun : nouns.keySet()) {

                if (!noun.contains(" ")) //is not multipart
                {
                    lextypecond = "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://lemon-model.net/lemon#LexicalEntry> ."
                            + "  ?s <http://www.lexinfo.net/ontology/2.0/lexinfo#partOfSpeech>  <http://www.lexinfo.net/ontology/2.0/lexinfo#commonNoun> .";
                } else {
                    lextypecond = "?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.lexinfo.net/ontology/2.0/lexinfo#NounPhrase>.";
                }

                query = "select distinct ?v ?ref  where {"
                        + lextypecond
                        + "  ?s <http://lemon-model.net/lemon#canonicalForm>  ?canonicalForm ."
                        + "  ?canonicalForm <http://lemon-model.net/lemon#writtenRep> ?v. "
                        + "?s <http://lemon-model.net/lemon#sense> ?sense. "
                        + "?sense <http://lemon-model.net/lemon#reference> ?ref. "
                        + "?sense <http://lemon-model.net/lemon#isA> ?dummy1. "
                        + "filter (lcase(str(?v))='" + noun.toLowerCase() + "')."
                        + "filter NOT EXISTS { ?ref <http://www.w3.org/2002/07/owl#onProperty> ?dummy2. }"
                        + "}";       //not exists added for excluding object property nouns.

                QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, ontLexDefault_graph);
                try {
                    ResultSet results = qe.execSelect();       //Notice: for ASK queries, you must run qe.execASK();

                    for (; results.hasNext();) {

                        QuerySolution sol = (QuerySolution) results.next();
                        /*                for (String var : results.getResultVars()) {
                         System.out.println(sol.get(var));
                         }
                         */
                        String resNoun = sol.get("?v").toString().replace("@en", "");
                        String resRef = sol.get("?ref").toString();

                        ClassNoun cn = new ClassNoun(resNoun, resRef, nouns.get(noun));
                        if (!resultCN.contains(cn)) {
                            resultCN.add(cn);
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
            }
            if (!resultCN.isEmpty()) {
                break;
            }
        }

        //get all super types
        List<ClassNoun> finalresults = new ArrayList<>();
        finalresults.addAll(resultCN);

        //commented by Mehdi.
        //it causes the number of generated queries become large.
//        
//        for (ClassNoun res : resultCN) {
//            ClassNoun res2 = res;
//            String superClass;
//            while ((superClass = getSuperClass(res2)) != null) {
//                ClassNoun newres = new ClassNoun(res2.noun, superClass, res2.score * superClassDecayFactor);
//                if (!finalresults.contains(newres)) {
//                    finalresults.add(newres);
//                    res2 = newres;
//                } else {
//                    break;
//                }
//
//            }
//
//        }
        return finalresults;

    }

    public static List<Adjective> getAdjectives(String inp_adj) {
        List<Adjective> resultAdj = new ArrayList<>();

        String query;

        Map<String, Double> adjs;
        if (usewordnet) {
            adjs = WordNet.INSTANCE.getAllSynAndHomonymsWithSimiliarity(POS.ADJECTIVE, inp_adj);

            if (adjs.isEmpty()) {
                System.out.println("Info:There is not any synonym for the adjective:" + inp_adj);
            }
        } else {
            adjs = new HashMap<>();
        }

        adjs.put(inp_adj, 1.0);

        for (String adj : adjs.keySet()) {

            query = "select distinct ?v ?ref  ?subjP ?objP where {"
                    + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://lemon-model.net/lemon#Word> ."
                    + "  ?s <http://www.lexinfo.net/ontology/2.0/lexinfo#partOfSpeech>  <http://www.lexinfo.net/ontology/2.0/lexinfo#adjective> ."
                    + "  ?s <http://lemon-model.net/lemon#canonicalForm>  ?canonicalForm ."
                    + "  ?canonicalForm <http://lemon-model.net/lemon#writtenRep> ?v. "
                    + "?s <http://lemon-model.net/lemon#sense> ?sense."
                    + "?sense <http://lemon-model.net/lemon#objOfProp> ?oP."
                    + "?sense <http://lemon-model.net/lemon#subjOfProp> ?sP."
                    + "?sense <http://lemon-model.net/lemon#reference> ?ref."
                    + "?s <http://lemon-model.net/lemon#synBehavior> ?synBe."
                    + "?synBe ?objP ?oP."
                    + "?synBe ?subjP ?sP."
                    + "filter (lcase(str(?v))='" + adj.toLowerCase() + "')"
                    + "}";

            QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, ontLexDefault_graph);
            try {
                ResultSet results = qe.execSelect();       //Notice: for ASK queries, you must run qe.execASK();

                for (; results.hasNext();) {

                    QuerySolution sol = (QuerySolution) results.next();
                    /*                for (String var : results.getResultVars()) {
                     System.out.println(sol.get(var));
                     }
                     */
                    String resAdj = sol.get("?v").toString();
                    String resRef = sol.get("?ref").toString();

                    Adjective cn = new Adjective(resAdj, resRef, adjs.get(adj));
                    if (!resultAdj.contains(cn)) {
                        resultAdj.add(cn);
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
        }
        return resultAdj;

    }

    public static List<VerbPhrase> getVerbPhrases(String inp_verb) {
        List<VerbPhrase> resultVP = new ArrayList<>();

        String query;

        Map<String, Double> verbs;
        if (usewordnet) {
            verbs = WordNet.INSTANCE.getAllSynAndHomonymsWithSimiliarity(POS.VERB, inp_verb);

            if (verbs.isEmpty()) {
                System.out.println("Info:There is not any synonym for the verb:" + inp_verb);
            }

        } else {
            verbs = new HashMap<>();
        }

        verbs.put(inp_verb, 1.0);

        for (String phrase : verbs.keySet()) {

            query = "select distinct ?v ?ref  where {"
                    + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://lemon-model.net/lemon#Phrase> ."
                    //                + "  ?s <http://www.lexinfo.net/ontology/2.0/lexinfo#partOfSpeech>  <http://www.lexinfo.net/ontology/2.0/lexinfo#adjective> ."
                    + "  ?s <http://lemon-model.net/lemon#canonicalForm>  ?canonicalForm ."
                    + "  ?canonicalForm <http://lemon-model.net/lemon#writtenRep> ?v. "
                    + "?s <http://lemon-model.net/lemon#sense> ?sense."
                    //                + "?sense <http://lemon-model.net/lemon#objOfProp> ?oP."
                    //                + "?sense <http://lemon-model.net/lemon#subjOfProp> ?sP."
                    + "?sense <http://lemon-model.net/lemon#reference> ?ref."
                    //                + "?s <http://lemon-model.net/lemon#synBehavior> ?synBe."
                    //                + "?synBe ?objP ?oP."
                    //                + "?synBe ?subjP ?sP."
                    + "filter (lcase(str(?v))='" + phrase.toLowerCase() + "')"
                    + "}";

            QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, ontLexDefault_graph);
            try {
                ResultSet results = qe.execSelect();       //Notice: for ASK queries, you must run qe.execASK();

                for (; results.hasNext();) {

                    QuerySolution sol = (QuerySolution) results.next();
                    /*                for (String var : results.getResultVars()) {
                     System.out.println(sol.get(var));
                     }
                     */
                    String resVerb = sol.get("?v").toString().replace("@en", "");
                    String resRef = sol.get("?ref").toString();

                    VerbPhrase vp = new VerbPhrase(resVerb, resRef, verbs.get(phrase));
                    if (!resultVP.contains(vp)) {
                        resultVP.add(vp);
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
        }
        return resultVP;

    }

    public static List<RelationalNoun> getRelationalNouns(String inp_noun) {
        List<RelationalNoun> resultRN = new ArrayList<>();

        String query;

        inp_noun = inp_noun.replace("_", " ");

        for (String suffix : getOrderedSuffix(inp_noun)) {
            Map<String, Double> nouns;

            if (usewordnet) {
                nouns = WordNet.INSTANCE.getAllSynAndHomonymsWithSimiliarity(POS.NOUN, suffix);

                if (nouns.isEmpty()) {
                    System.out.println("Info:There is not any synonym for the noun:" + suffix);
                }
            } else {
                nouns = new HashMap<>();
            }

            nouns.put(inp_noun, 1.0);

            for (String noun : nouns.keySet()) {
                List<String> queries;
                queries = new ArrayList<>(2);
                if (noun.contains(" ")) //multipart noun
                {
                    query = "select distinct ?v ?ref  ?subjP ?objP where {"
                            + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://lemon-model.net/lemon#LexicalEntry> ."
                            + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://www.lexinfo.net/ontology/2.0/lexinfo#NounPhrase> ."
                            + "  ?s <http://lemon-model.net/lemon#canonicalForm>  ?canonicalForm ."
                            + "  ?canonicalForm <http://lemon-model.net/lemon#writtenRep> ?v. "
                            + "?s <http://lemon-model.net/lemon#sense> ?sense."
                            + "?sense <http://lemon-model.net/lemon#objOfProp> ?oP."
                            + "?sense <http://lemon-model.net/lemon#subjOfProp> ?sP."
                            + "?sense <http://lemon-model.net/lemon#reference> ?ref."
                            + "?s <http://lemon-model.net/lemon#synBehavior> ?synBe."
                            + "?synBe ?objP ?oP."
                            + "?synBe ?subjP ?sP."
                            + "filter (lcase(str(?v))='" + noun.toLowerCase() + "')"
                            + "}";
                    queries.add(query);
                }

                //some multiparts nouns also were stores as commonNoun e.g. vice president
                query = "select distinct ?v ?ref  ?subjP ?objP where {"
                        + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://lemon-model.net/lemon#LexicalEntry> ."
                        + "  ?s <http://www.lexinfo.net/ontology/2.0/lexinfo#partOfSpeech>  <http://www.lexinfo.net/ontology/2.0/lexinfo#commonNoun> ."
                        + "  ?s <http://lemon-model.net/lemon#canonicalForm>  ?canonicalForm ."
                        + "  ?canonicalForm <http://lemon-model.net/lemon#writtenRep> ?v. "
                        + "?s <http://lemon-model.net/lemon#sense> ?sense."
                        + "?sense <http://lemon-model.net/lemon#objOfProp> ?oP."
                        + "?sense <http://lemon-model.net/lemon#subjOfProp> ?sP."
                        + "?sense <http://lemon-model.net/lemon#reference> ?ref."
                        + "?s <http://lemon-model.net/lemon#synBehavior> ?synBe."
                        + "?synBe ?objP ?oP."
                        + "?synBe ?subjP ?sP."
                        + "filter (lcase(str(?v))='" + noun.toLowerCase() + "')"
                        + "}";

                queries.add(query);
                for (String q : queries) {
                    QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, q, ontLexDefault_graph);
                    try {
                        ResultSet results = qe.execSelect();       //Notice: for ASK queries, you must run qe.execASK();

                        for (; results.hasNext();) {

                            QuerySolution sol = (QuerySolution) results.next();
                            /*
                             for (String var : results.getResultVars()) {
                             System.out.println(sol.get(var));
                             }
                             */
                            String resNoun = sol.get("?v").toString().replace("@en", "");
                            String resRef = sol.get("?ref").toString();
                            String subjStr = sol.get("?subjP").toString();
                            String objStr = sol.get("?objP").toString();

                            subjStr = subjStr.substring(Math.max(subjStr.indexOf("#"), subjStr.lastIndexOf("/")) + 1);

                            objStr = objStr.substring(Math.max(objStr.indexOf("#"), objStr.lastIndexOf("/")) + 1);

                            GrammaticalType subj = null;
                            GrammaticalType obj = null;

                            subj = GrammaticalType.valueof(subjStr);
                            obj = GrammaticalType.valueof(objStr);

                            if (((subj == null)) || ((obj == null))) {
                                System.err.println("Can not detect subject or object of relational noun : " + sol.get("?v") + "(" + sol.get("?ref") + ")");

                            } else {

                                RelationalNoun rn = new RelationalNoun(resNoun, resRef, subj, obj, nouns.get(noun));
                                if (!resultRN.contains(rn)) {
                                    resultRN.add(rn);
                                }
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
                }
            }
            if (!resultRN.isEmpty()) {
                break;
            }
        }
        return resultRN;

    }

    public static List<ObjectPropertyNoun> getObjectPropertyNouns(String inp_noun) {
        List<ObjectPropertyNoun> resultOPN = new ArrayList<>();

        inp_noun = inp_noun.replace("_", " ");
        String query;
        Map<String, Double> nouns;
        if (usewordnet) {
            nouns = WordNet.INSTANCE.getAllSynAndHomonymsWithSimiliarity(POS.NOUN, inp_noun);

            if (nouns.isEmpty()) {
                System.out.println("Info:There is not any synonym for the noun:" + inp_noun);
            }
        } else {
            nouns = new HashMap<>();
        }

        nouns.put(inp_noun, 1.0);

        for (String noun : nouns.keySet()) {

            query = "select distinct ?v ?property ?value where {"
                    + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://lemon-model.net/lemon#LexicalEntry> ."
                    + "  ?s <http://www.lexinfo.net/ontology/2.0/lexinfo#partOfSpeech>  <http://www.lexinfo.net/ontology/2.0/lexinfo#commonNoun> ."
                    + "  ?s <http://lemon-model.net/lemon#canonicalForm>  ?canonicalForm ."
                    + "  ?canonicalForm <http://lemon-model.net/lemon#writtenRep> ?v. "
                    + "?s <http://lemon-model.net/lemon#sense> ?sense."
                    + "?sense <http://lemon-model.net/lemon#reference> ?ref."
                    + "?ref <http://www.w3.org/2002/07/owl#onProperty> ?property."
                    + "?ref <http://www.w3.org/2002/07/owl#hasValue> ?value."
                    + "filter (lcase(str(?v))='" + noun.toLowerCase() + "')"
                    + "}";

            QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, ontLexDefault_graph);
            try {
                ResultSet results = qe.execSelect();       //Notice: for ASK queries, you must run qe.execASK();

                for (; results.hasNext();) {

                    QuerySolution sol = (QuerySolution) results.next();
                    /*
                     for (String var : results.getResultVars()) {
                     System.out.println(sol.get(var));
                     }
                     */
                    String resNoun = sol.get("?v").toString().replace("@en", "");
                    //String resRef = sol.get("?ref").toString();
                    String prop = sol.get("?property").toString();
                    String val = sol.get("?value").toString();

                    ObjectPropertyNoun rn = new ObjectPropertyNoun(resNoun, prop, val, nouns.get(noun));
                    if (!resultOPN.contains(rn)) {
                        resultOPN.add(rn);
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

        }
        return resultOPN;

    }

    public static List<ObjectPropertyAdjective> getObjectPropertyAdjectives(String inp_adj) {
        List<ObjectPropertyAdjective> resultOPA = new ArrayList<>();

        String query;
        Map<String, Double> adjs;
        if (usewordnet) {
            adjs = WordNet.INSTANCE.getAllSynAndHomonymsWithSimiliarity(POS.ADJECTIVE, inp_adj);

            if (adjs.isEmpty()) {
                System.out.println("Info:There is not any synonym for the adjective:" + inp_adj);
            }
        } else {
            adjs = new HashMap<>();
        }

        adjs.put(inp_adj, 1.0);

        for (String adj : adjs.keySet()) {

            query = "select distinct ?v ?property ?value where {"
                    + "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://lemon-model.net/lemon#LexicalEntry> ."
                    + " { ?s <http://www.lexinfo.net/ontology/2.0/lexinfo#partOfSpeech>  <http://www.lexinfo.net/ontology/2.0/lexinfo#adjective> .}"
                    + "UNION {\n" + //for adjective phrase such as critically endangered
                    "?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://www.lexinfo.net/ontology/2.0/lexinfo#AdjectivePhrase> .  }"
                    + "  ?s <http://lemon-model.net/lemon#canonicalForm>  ?canonicalForm ."
                    + "  ?canonicalForm <http://lemon-model.net/lemon#writtenRep> ?v. "
                    + "?s <http://lemon-model.net/lemon#sense> ?sense."
                    + "?sense <http://lemon-model.net/lemon#reference> ?ref."
                    + "?ref <http://www.w3.org/2002/07/owl#onProperty> ?property."
                    + "?ref <http://www.w3.org/2002/07/owl#hasValue> ?value."
                    + "filter (lcase(str(?v))='" + adj.toLowerCase() + "')"
                    + "}";

            QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, ontLexDefault_graph);
            try {
                ResultSet results = qe.execSelect();       //Notice: for ASK queries, you must run qe.execASK();

                for (; results.hasNext();) {

                    QuerySolution sol = (QuerySolution) results.next();
                    /*
                     for (String var : results.getResultVars()) {
                     System.out.println(sol.get(var));
                     }
                     */
                    String resAdj = sol.get("?v").toString().replace("@en", "");
                    //String resRef = sol.get("?ref").toString();
                    String prop = sol.get("?property").toString();
                    String val = sol.get("?value").toString();

                    ObjectPropertyAdjective rn = new ObjectPropertyAdjective(resAdj, prop, val, adjs.get(adj));
                    if (!resultOPA.contains(rn)) {
                        resultOPA.add(rn);
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
        }
        return resultOPA;

    }

    public static boolean isAsTransitiveVerb(StateVerb st) {
        //transitive verb have both subject and direct object
        if (st.propSubj == GrammaticalType.Subject && st.propObj == GrammaticalType.DirectObj) {
            return true;
        }
        if (st.propSubj == GrammaticalType.DirectObj && st.propObj == GrammaticalType.Subject) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        WordnetStemmer wnStemmer = new WordnetStemmer(WordNet.INSTANCE.dict);
        System.out.println(wnStemmer.findStems("partner", null));
        SimpleStemmer sStemmer = new SimpleStemmer();
        System.out.println(sStemmer.findStems("partner", null));
//System.out.println(WordNet.INSTANCE.getAllSynAndHomonyms(POS.ADJECTIVE, "tall"));

        System.out.println(getObjectPropertyAdjectives("Swedish"));

        String noun = "cosmonaut";
        List<ClassNoun> resultsN1 = getClassNouns(noun);
        if (!resultsN1.isEmpty()) {
            for (ClassNoun rn : resultsN1) {
                System.out.println(rn);
            }
        } else {
            System.out.println("There is not any match for your noun:" + noun);
        }
        String verb = "manage";
        List<StateVerb> results1 = getStateVerbs(verb);
        if (!results1.isEmpty()) {
            for (StateVerb rn : results1) {
                System.out.println(rn);
            }
        } else {
            System.out.println("There is not any match for your verb:" + verb);
        }
        String adj = "born";
        List<RelationalAdjective> results2 = getRelationalAdjectives(adj);
        if (!results2.isEmpty()) {
            for (RelationalAdjective rn : results2) {
                System.out.println(rn);
            }
        } else {
            System.out.println("There is not any relational adjective match for your adj.:" + adj);
        }

        String opNoun = "surfer";
        List<ObjectPropertyNoun> results3 = getObjectPropertyNouns(opNoun);
        if (!results3.isEmpty()) {
            for (ObjectPropertyNoun rn : results3) {
                System.out.println(rn);
            }
        } else {
            System.out.println("There is not any object property noun match for your noun:" + opNoun);
        }

//        List<ObjectPropertyAdjective> results4 = getObjectPropertyAdjectives("pro-European");
        //     String obAdj = "critically endangered";
        String obAdj = "Argentine";
//        String obAdj = "married";
        List<ObjectPropertyAdjective> results4 = getObjectPropertyAdjectives(obAdj);
        if (!results4.isEmpty()) {
            for (ObjectPropertyAdjective rn : results4) {
                System.out.println(rn);
            }
        } else {
            System.out.println("There is not any object property adjective match for your adj.:" + obAdj);
        }
        String relNoun = "mayor";
        List<RelationalNoun> results = getRelationalNouns(relNoun);
        if (!results.isEmpty()) {
            for (RelationalNoun rn : results) {
                System.out.println(rn);
            }
        } else {
            System.out.println("There is not any match for your noun:" + relNoun);
        }
    }

    // get all suffixes of the inp_noun in descending order of the suffix length
    //e.g. all suffixes of "Grunge record label" are : "Grunge record label", "record label" , "label"
    private static List<String> getOrderedSuffix(String inp_noun) {
        List<String> result = new ArrayList<>();

        String[] parts = inp_noun.split(" ");
        for (int start = 0; start < parts.length; start++) {
            String suffix = "";
            for (int i = start; i < parts.length; i++) {
                suffix = suffix + " " + parts[i];
            }
            suffix = suffix.trim();
            result.add(suffix);
        }
        return result;
    }

    private static String getSuperClass(ClassNoun res) {
        String query = "select distinct ?super where { <" + res.ref + "> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?super} ";
        QueryExecution qe = QueryExecutionFactory.sparqlService(ontLexServer, query, dbpedia_OWL_graph);

        try {
            ResultSet results = qe.execSelect();       //Notice: for ASK queries, you must run qe.execASK();

            if (results.hasNext()) {

                QuerySolution sol = (QuerySolution) results.next();
                /*
                 for (String var : results.getResultVars()) {
                 System.out.println(sol.get(var));
                 }
                 */
                String superClass = sol.get("?super").toString();
                return superClass;
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
        return null;
    }

}
