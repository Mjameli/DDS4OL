/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Preprocess;

import de.citec.sc.lemon.core.Lexicon;
import patterns.PatternLibrary;
import patterns.SparqlPattern;
import patterns.english.SparqlPattern_EN_Intransitive_PP;
import patterns.english.SparqlPattern_EN_Noun_PP_appos;
import patterns.english.SparqlPattern_EN_Noun_PP_copulative;
import patterns.english.SparqlPattern_EN_Noun_PP_possessive;
import patterns.english.SparqlPattern_EN_Predicative_Participle_copulative;
import patterns.english.SparqlPattern_EN_Predicative_Participle_passive;
import patterns.english.SparqlPattern_EN_Transitive_Passive;
import patterns.english.SparqlPattern_EN_Transitive_Verb;
import preprocessor.ModelPreprocessor;
import de.citec.sc.sentence.preprocessing.process.Language;
import de.citec.sc.sentence.preprocessing.process.OntologyImporter;
import java.io.FileInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
//import de.citec.sc.lemon.core.Language;

import Lexicon.LexiconInterface;
import Lexicon.StateVerb;
import static process.Matoll.getObject;
import static process.Matoll.getReference;
import static process.Matoll.getSentences;
import static process.Matoll.getSubject;
import utils.StanfordLemmatizer;
import utils.Stopwords;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;

/**
 *
 * @author Mehdi Jabalameli <Mehdi Jabalameli at ui.ac.ir>
 */
public class ExtLexiconSentences {

    public static String ontLexServer = "http://127.0.0.1:8890/sparql";

    public static String ontLexDefault_graph = "DBpediaGoldLexicon";
    public static String modelsdir = "J:\\wikipedia\\RDFModels\\";

    public static void main(String[] args) throws FileNotFoundException {
        
        //this method extracts sentences from ontology lexicon file.
        //Not COMPLETE!!!!!!!
        
        Stopwords stopwords = new Stopwords();
        de.citec.sc.lemon.core.Language language = de.citec.sc.lemon.core.Language.EN;
        String propertiesFile = "en_lexicalizedURIsByUngerWithPrefixes.txt";

        List<List<String>> properties = new ArrayList<>();
        try {
            if (propertiesFile.endsWith(".owl")) {
                loadOntology(propertiesFile, properties, Language.EN);
            } else {
                loadPropertyList(propertiesFile, properties, Language.EN);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(properties.size());
        
        Lexicon automatic_lexicon = new Lexicon();
//                automatic_lexicon.setBaseURI(config.getBaseUri());
        

        PatternLibrary library = new PatternLibrary();
        if (language == de.citec.sc.lemon.core.Language.EN) {
            StanfordLemmatizer sl = new StanfordLemmatizer(language);
            library.setLemmatizer(sl);
        }

        ArrayList<SparqlPattern> Patterns = new ArrayList<>();

        Patterns.add(new SparqlPattern_EN_Intransitive_PP());
        Patterns.add(new SparqlPattern_EN_Noun_PP_appos());
        Patterns.add(new SparqlPattern_EN_Noun_PP_copulative());
        Patterns.add(new SparqlPattern_EN_Predicative_Participle_passive());
        Patterns.add(new SparqlPattern_EN_Transitive_Verb());
        Patterns.add(new SparqlPattern_EN_Predicative_Participle_copulative());
        Patterns.add(new SparqlPattern_EN_Transitive_Passive());
        Patterns.add(new SparqlPattern_EN_Noun_PP_possessive());

        library.setPatterns(Patterns);

        List<StateVerb> stateverbs = LexiconInterface.getAllStateVerbs();

        if (stateverbs != null && !stateverbs.isEmpty()) {
            for (StateVerb stateverb : stateverbs) {
                System.out.println(stateverb);
                String pname = stateverb.getRef().substring(stateverb.getRef().lastIndexOf("/") + 1);
                
//                if (!pname.contains("distributor")) continue;
                
                //find model file 
                File dir = new File(modelsdir);

                File[] f = dir.listFiles((File dir1, String name) -> {
                    String lowercaseName = name.toLowerCase();
                    return lowercaseName.startsWith(pname) && lowercaseName.endsWith(".ttl");
                });
                File file;
                if (f.length == 0) {
                    //can not locate model file.
                    continue;
                }
                file = f[0];

                ModelPreprocessor preprocessor = new ModelPreprocessor(language);
                preprocessor.setCoreferenceResolution(false);
                Set<String> dep = new HashSet<>();
                dep.add("prep");
                dep.add("appos");
                dep.add("nn");
                dep.add("dobj");
                dep.add("pobj");
                dep.add("num");
                preprocessor.setDEP(dep);

                System.out.println("");
                List<Model> sentences = new ArrayList<>();

                Integer sentence_num_per_file = 0;
                System.out.println("Processing " + file.getName());
                Model model = RDFDataMgr.loadModel(file.toString());
                sentences.clear();
                sentences = getSentences(model);
                for (Model sentence : sentences) {
                    String obj = getObject(sentence);
                    String subj = getSubject(sentence);
                    if (!stopwords.isStopword(obj, language)
                            && !stopwords.isStopword(subj, language)
                            && !subj.equals(obj)
                            && !subj.contains(obj)
                            && !obj.contains(subj)) {
                        String reference = getReference(sentence);
                        if (!reference.equals("http://dbpedia.org/ontology/type") && !reference.equals("http://dbpedia.org/ontology/isPartOf")) {
                            preprocessor.preprocess(sentence, subj, obj, language);
//                            freq.adjustOrPutValue(reference, 1, 1);
//                            sentence_counter += 1;
                            sentence_num_per_file++;
                            library.extractLexicalEntries(sentence, automatic_lexicon);
                        }
                    }
                }
                model.close();
                System.out.println("Number of processed sentences:" + sentence_num_per_file);
                System.out.println(file.getName() + " was processed.\n");
            }

        }

    }

    public static void loadPropertyList(String pathToProperties,
            List<List<String>> properties, Language language) throws IOException {
        String properties_raw ;
        try ( /*
         * each line contains one property
         */ FileInputStream inputStream = new FileInputStream(pathToProperties)) {
            properties_raw = IOUtils.toString(inputStream);
        }

        for (String p : properties_raw.split("\n")) {
            p = p.trim();
            String ontologyName = findOntologyName(p);
            String[] tmp = p.split("/");
            String name = tmp[tmp.length - 1];
            String namespace = tmp[tmp.length - 2];
            List<String> property = new ArrayList<>();
            property.add(p);
            property.add(ontologyName);
            property.add(namespace);
            property.add(language.toString().toLowerCase());
            property.add(name);
            properties.add(property);
            //System.out.println(property.toString());

        }

    }

    public static void loadOntology(String pathToOntology,
            List<List<String>> properties, Language language) throws IOException {
        OntologyImporter importer = new OntologyImporter(pathToOntology, "RDF/XML");

        for (String p : importer.getProperties()) {
            String ontologyName = findOntologyName(p);
            String[] tmp = p.split("/");
            String name = tmp[tmp.length - 1];
            String namespace = tmp[tmp.length - 2];
            List<String> property = new ArrayList<>();
            property.add(p);
            property.add(ontologyName);
            property.add(namespace);
            property.add(language.toString().toLowerCase());
            property.add(name);
            properties.add(property);
            //System.out.println(property.toString());

        }

    }

    private static String findOntologyName(String p) {
        // String to be scanned to find the pattern.
        String pattern = "^http://(\\w*).*\\W.*";
        String ontologyName = "";
        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(p);
        if (m.find()) {
            ontologyName = m.group(1);
        } else {
            System.out.println("NO MATCH");
        }
        return ontologyName;
    }

}
