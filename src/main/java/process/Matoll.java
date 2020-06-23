package process;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import gnu.trove.map.hash.TObjectIntHashMap;

import javax.xml.parsers.ParserConfigurationException;

//import learning.SVMClassifier;
import de.citec.sc.lemon.core.LexicalEntry;
import de.citec.sc.lemon.core.Lexicon;
import de.citec.sc.lemon.core.Reference;
import io.Config;
import de.citec.sc.lemon.io.LexiconLoader;
import de.citec.sc.lemon.io.LexiconSerialization;
import patterns.PatternLibrary;
import preprocessor.ModelPreprocessor;
import utils.StanfordLemmatizer;
import de.citec.sc.lemon.core.Language;
import de.citec.sc.lemon.core.Provenance;
import de.citec.sc.lemon.core.Sense;
import de.citec.sc.lemon.core.SimpleReference;
import de.citec.sc.lemon.core.SyntacticBehaviour;
import de.citec.sc.lemon.io.CSV_LexiconSerialization;
import utils.Stopwords;
import de.jungblut.distance.CosineDistance;
import de.jungblut.glove.GloveRandomAccessReader;
//import de.jungblut.glove.examples.GloveLookup;

import de.jungblut.math.DoubleVector;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import org.xml.sax.SAXException;

import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

public class Matoll {

//	public static Logger logger = LogManager.getLogger(Matoll.class.getName());
    /**
     *
     * @param args
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, Exception {

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //Mehdi: Note: it just extract lexical entries for the properties in the gold lexicon!!!!!!!!!!!!!
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        String directory;
        String mode;
        String gold_standard_lexicon;
        String output_lexicon;
        String configFile;
        Language language;
        boolean coreference;
        String output;

        // System.out.println(similiarty("SimpleReference", "CompoundReference", new GloveRandomAccessReader("F:/glove")));
        Stopwords stopwords = new Stopwords();

        HashMap<String, Double> maxima;
        maxima = new HashMap<>();

        //Provenance provenance;
        Pattern p = Pattern.compile("^--(\\w+)=(\\w+|\\d+)$");

        Matcher matcher;

        if (args.length < 3) {
            System.out.print("Usage: Matoll --mode=train/test <DIRECTORY> <CONFIG>\n");
            return;

        }

//		Classifier classifier;
        directory = args[1].replace("\\\\", "/").replace("\\", "/");
        configFile = args[2];

        final Config config = new Config();

        config.loadFromFile(configFile);

        gold_standard_lexicon = config.getGoldStandardLexicon();

        String model_file = config.getModel();

        output_lexicon = config.getOutputLexicon();
        output = config.getOutput();
        coreference = config.getCoreference();

        language = config.getLanguage();

        final StanfordLemmatizer sl = new StanfordLemmatizer(language);

        for (int i = 0; i < args.length; i++) {
            matcher = p.matcher(args[i]);

            if (matcher.matches()) {
                if (i == 0 && matcher.group(1).equals("mode")) {
                    mode = matcher.group(2);
                    System.out.print("Starting MATOLL with mode: " + mode + "\n");
                    System.out.print("Language: " + language + "\n");
                    System.out.print("Processing directory: " + directory + "\n");
                    System.out.print("Using gold standard: " + gold_standard_lexicon + "\n");
                    System.out.print("Using model file: " + model_file + "\n");
                    System.out.print("Output lexicon: " + output_lexicon + "\n");
                    System.out.print("Output: " + output + "\n");
                    System.out.print("Using coreference: " + coreference + "\n");

                } else {
                    System.out.print("Usage: Matoll --mode=train/test <DIRECTORY> <CONFIG>\n");
                    return;
                }
            }
        }

//		
        LexiconLoader loader = new LexiconLoader();
//		
//		logger.info("Loading lexicon from: "+gold_standard_lexicon+"\n");
//		
        Lexicon gold = loader.loadFromFile(gold_standard_lexicon);

        Set<String> goldRefURIs = new HashSet<>();

        gold.getReferences().stream().forEach((ref) -> {
            if (ref != null) {
                goldRefURIs.add(ref.getURI());
            }
        });

        // Creating preprocessor
//		
        ModelPreprocessor preprocessor = new ModelPreprocessor(language);
        preprocessor.setCoreferenceResolution(coreference);
        switch (language) {

            case EN:
                Set<String> dep = new HashSet<>();
                dep.add("prep");
                dep.add("appos");
                dep.add("nn");
                dep.add("dobj");
                dep.add("pobj");
                dep.add("num");
                preprocessor.setDEP(dep);
                break;

            case DE:
                dep = new HashSet<>();
                dep.add("pp");
                dep.add("pn");
                dep.add("obja");
                dep.add("objd");
                dep.add("app");
                preprocessor.setDEP(dep);
                break;

            case ES:
                dep = new HashSet<>();
                dep.add("MOD");
                dep.add("COMP");
                dep.add("DO");
                dep.add("OBLC");
                dep.add("BYAG");
                preprocessor.setDEP(dep);
                break;

            case JA:
                break;
            //TODO

        }

        Lexicon automatic_lexicon = new Lexicon();
        automatic_lexicon.setBaseURI(config.getBaseUri());

        PatternLibrary library = new PatternLibrary();
        if (language == Language.EN) {
            //sl = new StanfordLemmatizer(language);
            library.setLemmatizer(sl);
        }

        library.setPatterns(config.getPatterns());

        String subj;
        String obj;

        String reference;

        List<Model> sentences = new ArrayList<>();

        List<File> list_files = new ArrayList<>();

        if (config.getFiles().isEmpty()) {
            File folder = new File(directory);
            File[] files = folder.listFiles();
            //   System.out.println("Just for test: Directory is "+directory);
            for (File file : files) {
                if (file.toString().contains(".ttl")) {
                    list_files.add(file);
                }
            }
        } else {
            list_files.addAll(config.getFiles());
        }

        int sentence_counter = 0;
        TObjectIntHashMap<String> freq = new TObjectIntHashMap<>();

        int filecount = 0;
        int list_files_count = list_files.size();
        for (File file : list_files) {
            filecount++;
            Integer sentence_num_per_file = 0;
            System.out.println("Processing " + file.getName());
            File temp= new File("resources/temp.ttl");
            Files.copy(file, temp);
            System.out.println("temp file:"+temp.getAbsoluteFile());
            Model model = RDFDataMgr.loadModel(temp.toString());
            temp.delete();
            sentences.clear();
            sentences = getSentences(model);
            for (Model sentence : sentences) {
                obj = getObject(sentence);
                subj = getSubject(sentence);
                if (!stopwords.isStopword(obj, language)
                        && !stopwords.isStopword(subj, language)
                        && !subj.equals(obj)
                        && !subj.contains(obj)
                        && !obj.contains(subj)) {
                    reference = getReference(sentence);
                    if (!reference.equals("http://dbpedia.org/ontology/type") && !reference.equals("http://dbpedia.org/ontology/isPartOf")) {
                        if (goldRefURIs.contains(reference)) {
                            preprocessor.preprocess(sentence, subj, obj, language);
                            freq.adjustOrPutValue(reference, 1, 1);
                            sentence_counter += 1;
                            sentence_num_per_file++;
                            library.extractLexicalEntries(sentence, automatic_lexicon);
                        }
                    }
                }
            }
            model.close();
            System.out.println("Number of processed sentences:" + sentence_num_per_file);
            System.out.println(file.getName() + " was processed.\n");
            System.out.println(filecount + " of " + list_files_count + " was processed.\n");
        }

        System.out.println("Extracted entries");
        if (config.doStatistics()) {
            System.out.println("do some statistics now");
            absolutNumberEntriesByReference(automatic_lexicon, freq, language);
            Map<String, Double> overall_pattern_distribution = getOverallDistributionPattern(automatic_lexicon);
            for (String key : overall_pattern_distribution.keySet()) {
                System.out.println(key + ":" + overall_pattern_distribution.get(key));
            }
            for (Reference ref : automatic_lexicon.getReferences()) {
                getDistributionPatternPerPreference(automatic_lexicon, ref.getURI(), language);
            }
        }

        System.out.println("Calculate normalized confidence");
        calculateConfidence(automatic_lexicon, freq);
        normalizeConfidence(automatic_lexicon);

        lexicon2file(automatic_lexicon, output_lexicon);

        //Added by Mehdi
//        Lexicon automatic_lexicon_refined = new Lexicon();
//        automatic_lexicon_refined.setBaseURI(automatic_lexicon.getBaseURI());
//
//        GloveRandomAccessReader gl = new GloveRandomAccessReader("F:/glove");
//
//        for (float threshold = 0.1f; threshold <= 0.9f; threshold += 0.1f) {
//            threshold = Math.round(threshold * 10) / 10;  //JAVA floating point rep. is not so exact.
//            for (LexicalEntry entry : automatic_lexicon.getEntries()) {
//                String can_form = entry.getCanonicalForm();
//                for (Reference ref : entry.getReferences()) {
//                    if (ref instanceof SimpleReference) {
//                        String ref_string = ref.getURI().substring(ref.getURI().lastIndexOf("/") + 1, ref.getURI().length());
//                        double sim = similiarty(can_form, ref_string, gl);
//                        if (sim < threshold) {
//                            HashMap<Sense, HashSet<SyntacticBehaviour>> sb = entry.getSenseBehaviours();
//
//                            sb.entrySet().removeIf(e -> e.getKey().getReference().equals(ref)); //remove the sense 
//                        }
//                    }
//                }
//                if (entry.getReferences() != null && !entry.getReferences().isEmpty()) {
//                    automatic_lexicon_refined.addEntry(entry);
//                }
//            }
//
//            lexicon2file(automatic_lexicon_refined, output_lexicon.replace(".lex", "_Glove_" + threshold + ".lex"));
//        }
        System.out.println("Actual number used sentences:" + Integer.toString(sentence_counter));
        System.out.println("The size of automatic lexicon:" + automatic_lexicon.size());
        //     System.out.println("The size of refined automatic lexicon:" + automatic_lexicon_refined.size());

    }

    public static void lexicon2file(Lexicon automatic_lexicon, String output_lexicon) throws FileNotFoundException, IOException {
        //		LexiconSerialization serializer = new LexiconSerialization(library.getPatternSparqlMapping(),config.removeStopwords());

        LexiconSerialization serializer = new LexiconSerialization();
        //TODO: Add stopword removel, calculating the sameAs Link, as well as ass the Name of the Pattern.

        CSV_LexiconSerialization csv_serialiser = new CSV_LexiconSerialization();

        csv_serialiser.serialize(automatic_lexicon, output_lexicon.replace(".lex", ".tsv"));

        Model model = ModelFactory.createDefaultModel();

        serializer.serialize(automatic_lexicon, model);

        FileOutputStream out = new FileOutputStream(new File(output_lexicon.replace(".lex", "_beforeTraining.ttl")));

        RDFDataMgr.write(out, model, RDFFormat.TURTLE);

        out.close();
    }

    /**
     *
     * @param lexicon
     * @throws IOException
     */
    public static void writeByReference(Lexicon lexicon, Language language) throws IOException {
        List<LexicalEntry> entries;
        FileWriter writer;
        Set<Reference> references = lexicon.getReferences();

        for (Reference ref : references) {
            String filename = language.toString() + "_" + ref.toString().replaceAll("http:\\/\\/", "").replaceAll("\\/", "_").replaceAll("\\.", "_") + ".lex";
            System.out.println("Write lexicon for reference " + ref.toString() + " to " + filename);
            writer = new FileWriter(filename);
            entries = lexicon.getEntriesForReference(ref.toString());

            for (LexicalEntry entry : entries) {
                writer.write(entry.toString() + "\n");
                writer.flush();
            }

            writer.close();

        }
    }

    /**
     *
     * @param lexicon
     * @throws IOException
     */
    public static void absolutNumberEntriesByReference(Lexicon lexicon, TObjectIntHashMap<String> freq, Language language) throws IOException {
        Map<String, Integer> absolut_nubers = new HashMap<>();
        for (LexicalEntry entry : lexicon.getEntries()) {
            for (Sense sense : entry.getSenseBehaviours().keySet()) {
                Provenance prov = entry.getProvenance(sense);
                String uri = sense.getReference().getURI();
                if (absolut_nubers.containsKey(uri)) {
                    int value = absolut_nubers.get(uri);
                    absolut_nubers.put(uri, value + prov.getFrequency());
                } else {
                    absolut_nubers.put(uri, prov.getFrequency());
                }
            }
        }
        String filename = language.toString() + "_statistics.tsv";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("uri\t#entries\t#numberSentences\n");

            for (String key : absolut_nubers.keySet()) {
                if (freq.contains(key)) {
                    int number_sentences = freq.get(key);
                    writer.write(key + "\t" + absolut_nubers.get(key) + "\t" + number_sentences + "\n");
                    writer.flush();
                }

            }
        }
    }

    /**
     *
     * @param model
     * @return
     */
    public static String getReference(Model model) {
        StmtIterator iter = model.listStatements(null, model.getProperty("conll:reference"), (RDFNode) null);
        Statement stmt;
        while (iter.hasNext()) {
            stmt = iter.next();
            return stmt.getObject().toString();
        }

        return null;
    }

    public static Map<String, Double> getOverallDistributionPattern(Lexicon lexicon) {
        Map<String, Double> results = new HashMap<>();
        Map<String, Integer> tmp_results = new HashMap<>();
        int absolut_number = 0;

        for (LexicalEntry entry : lexicon.getEntries()) {
            for (Sense sense : entry.getSenseBehaviours().keySet()) {
                Provenance prov = entry.getProvenance(sense);
                absolut_number += prov.getFrequency();
                for (String pattern_name : prov.getPatternset()) {
                    if (tmp_results.containsKey(pattern_name)) {
                        int value = tmp_results.get(pattern_name);
                        tmp_results.put(pattern_name, value + prov.getFrequency());
                    } else {
                        tmp_results.put(pattern_name, prov.getFrequency());
                    }
                    /*
                     numbers are not 100%correct, as one provenance can be created by different pattern and the distinction of the contribution of each pattern is then lost.
                     */
                }
            }
        }
        for (String key : tmp_results.keySet()) {
            double value = (tmp_results.get(key) + 0.0) / absolut_number;
            results.put(key, value);
        }

        return results;
    }

    public static void getDistributionPatternPerPreference(Lexicon lexicon, String uri, Language language) throws IOException {
        Map<String, Double> results = new HashMap<>();
        Map<String, Integer> tmp_results = new HashMap<>();
        int absolut_number = 0;

        for (LexicalEntry entry : lexicon.getEntriesForReference(uri)) {
            for (Sense sense : entry.getSenseBehaviours().keySet()) {
                Provenance prov = entry.getProvenance(sense);
                if (uri.equals(sense.getReference().getURI())) {
                    absolut_number += prov.getFrequency();
                    for (String pattern_name : prov.getPatternset()) {
                        if (tmp_results.containsKey(pattern_name)) {
                            int value = tmp_results.get(pattern_name);
                            tmp_results.put(pattern_name, value + prov.getFrequency());
                        } else {
                            tmp_results.put(pattern_name, prov.getFrequency());
                        }
                        /*
                         numbers are not 100%correct, as one provenance can be created by different pattern and the distinction of the contribution of each pattern is then lost.
                         */
                    }
                }
            }
        }
        for (String key : tmp_results.keySet()) {
            double value = (tmp_results.get(key) + 0.0) / absolut_number;
            results.put(key, value);
        }

        String filename = uri.replaceAll("http:\\/\\/", "").replaceAll("\\/", "_").replaceAll("\\.", "_") + "_" + language.toString() + "_statistics.tsv";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("pattern_name\t#distribution\n");

            for (String key : results.keySet()) {
                writer.write(key + "\t" + results.get(key) + "\n");
                writer.flush();

            }
        }

    }

    /**
     *
     * @param model
     * @return
     * @throws FileNotFoundException
     */
    public static List<Model> getSentences(Model model) throws FileNotFoundException {

        // get all ?res <conll:sentence> 
        List<Model> sentences = new ArrayList<>();

        StmtIterator iter, iter2, iter3;

        Statement stmt, stmt2, stmt3;

        Resource resource;

        Resource token;

        iter = model.listStatements(null, model.getProperty("conll:language"), (RDFNode) null);

        while (iter.hasNext()) {

            Model sentence = ModelFactory.createDefaultModel();

            stmt = iter.next();

            resource = stmt.getSubject();

            iter2 = model.listStatements(resource, null, (RDFNode) null);

            while (iter2.hasNext()) {
                stmt2 = iter2.next();

                sentence.add(stmt2);

            }

            iter2 = model.listStatements(null, model.getProperty("own:partOf"), (RDFNode) resource);

            while (iter2.hasNext()) {
                stmt2 = iter2.next();

                token = stmt2.getSubject();

                iter3 = model.listStatements(token, null, (RDFNode) null);

                while (iter3.hasNext()) {
                    stmt3 = iter3.next();

                    sentence.add(stmt3);

                }
            }

            sentences.add(sentence);

            // RDFDataMgr.write(new FileOutputStream(new File(resource+".ttl")), sentence, RDFFormat.TURTLE) ;
        }

        return sentences;

    }

    /**
     *
     * @param model
     * @return
     */
    public static String getSubject(Model model) {

        StmtIterator iter = model.listStatements(null, model.getProperty("own:subj"), (RDFNode) null);

        Statement stmt;

        while (iter.hasNext()) {

            stmt = iter.next();

            return stmt.getObject().toString();
        }

        return null;
    }

    /**
     *
     * @param model
     * @return
     */
    public static String getObject(Model model) {
        StmtIterator iter = model.listStatements(null, model.getProperty("own:obj"), (RDFNode) null);

        Statement stmt;

        while (iter.hasNext()) {

            stmt = iter.next();

            return stmt.getObject().toString();
        }

        return null;
    }

    public static void calculateConfidence(Lexicon automatic_lexicon, TObjectIntHashMap<String> freq) {
        for (LexicalEntry entry : automatic_lexicon.getEntries()) {
            int value_2 = getFrequencySameCanonicalForm(entry.getCanonicalForm(), automatic_lexicon);
            for (Sense sense : entry.getSenseBehaviours().keySet()) {
                Provenance prov = entry.getProvenance(sense);
                String uri = sense.getReference().getURI();
                double value_1 = (prov.getFrequency() + 0.0) / freq.get(uri);
                double value_3 = (prov.getFrequency() + 0.0) / value_2;
                /*
                 Harmonic mean between the ratio of Frequency of the sense over the number of sentences in the property 
                 and the ratio of frequency of the sense over the number of senses of other entries with the same cannonical form
                 */
                prov.setConfidence((2 * value_1 * value_3) / (value_1 + value_3));
            }
        }
    }

    public static int getFrequencySameCanonicalForm(String canonicalForm, Lexicon automatic_lexicon) {
        int value = 0;
        for (LexicalEntry entry : automatic_lexicon.getEntriesWithCanonicalForm(canonicalForm)) {
            for (Sense sense : entry.getSenseBehaviours().keySet()) {
                Provenance prov = entry.getProvenance(sense);
                value += prov.getFrequency();
            }
        }
        return value;
    }

    /*
     normalizes all confidence values by the highest confidence value in the lexicon
     */
    public static void normalizeConfidence(Lexicon automatic_lexicon) {
        double highestConfidence = 0.0;
        for (LexicalEntry entry : automatic_lexicon.getEntries()) {
            for (Sense sense : entry.getSenseBehaviours().keySet()) {
                Provenance prov = entry.getProvenance(sense);
                if (prov.getConfidence() > highestConfidence) {
                    highestConfidence = prov.getConfidence();
                }
            }
        }

        System.out.println("Greatest confidence is:" + highestConfidence);

        for (LexicalEntry entry : automatic_lexicon.getEntries()) {
            for (Sense sense : entry.getSenseBehaviours().keySet()) {
                Provenance prov = entry.getProvenance(sense);
                double confidence = prov.getConfidence();
                double normalised_confidence = confidence / highestConfidence;
                prov.setConfidence(normalised_confidence);
            }
        }

    }

//    public static void extportTSV(Lexicon lexicon, String path){
//        Map<String,Double> hm_double = new HashMap<>();
//        Map<String,Integer> hm_int = new HashMap<>();
//        for(LexicalEntry entry : lexicon.getEntries()){
//            for(Sense sense:entry.getSenseBehaviours().keySet()){
//                Reference ref = sense.getReference();
//                if (ref instanceof de.citec.sc.matoll.core.SimpleReference){
//                    SimpleReference reference = (SimpleReference) ref;
//                    String preposition = "";
//                    if(entry.getPreposition()!=null) preposition = entry.getPreposition().getCanonicalForm();
//                    String input = entry.getCanonicalForm()+"\t"+preposition+"\t"+reference.getURI()+"\t";
//                    if(hm_int.containsKey(input)){
//                            int freq = hm_int.get(input);
//                             hm_int.put(input, entry.getProvenance(sense).getFrequency()+freq);
//                        }
//                        else{
//                            hm_int.put(input, entry.getProvenance(sense).getFrequency());
//                        }
//                }
//                else if (ref instanceof de.citec.sc.matoll.core.Restriction){
//                    Restriction reference = (Restriction) ref;
//                    String input = entry.getCanonicalForm()+"\t"+reference.getValue()+"\t"+reference.getProperty()+"\t";
//                    if(entry.getProvenance(sense).getConfidence()!=null){
//                        if(hm_double.containsKey(input)){
//                            double value = hm_double.get(input);
//                             hm_double.put(input, entry.getProvenance(sense).getConfidence()+value);
//                        }
//                        else{
//                            hm_double.put(input, entry.getProvenance(sense).getConfidence());
//                        }
//                    }
//                    
//                }
//            }
//        }
//        
//        PrintWriter writer;
//        try {
//                writer = new PrintWriter(path+"_restriction.tsv");
//                for(String key:hm_double.keySet()){
//                    writer.write(key+Double.toString(hm_double.get(key))+"\n");
//                }
//                writer.close();
//        } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//        }
//
//        try {
//                writer = new PrintWriter(path+"_simple.tsv");
//                for(String key:hm_int.keySet()){
//                    if(hm_int.get(key)>1)
//                        writer.write(key+Integer.toString(hm_int.get(key))+"\n");
//                }
//                writer.close();
//        } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//        }
//        
//    }
    public static double similiarty(String word1, String word2, GloveRandomAccessReader gl) {
        //Note(Mehdi) word1 may contains spaces and word2 may be a multiword string        
        //canonical forms may contain spaces 
        //reference property may be a multiword string e.g. "MergedIntoParty"
        try {
            String[] word1parts;
            if (word1.contains(" ")) {
                word1parts = word1.split(" ");
            } else {
                word1parts = word1.split("(?=\\p{Upper})"); //split over capital letters
            }
            DoubleVector w1;
            if (word1parts.length == 1) {
                w1 = gl.get(word1.toLowerCase());
            } else {
                w1 = gl.get(word1parts[0].toLowerCase());
                if (w1 == null) {
                    if (word1parts[0].contains("-")) {
                        String[] wps = word1parts[0].split("-");

                        int len = wps.length;
                        String[] temp = new String[len + word1parts.length - 1];
                        System.arraycopy(wps, 0, temp, 0, len);

                        System.arraycopy(word1parts, 1, temp, len, word1parts.length - 1);
                        word1parts = temp;
                        w1 = gl.get(word1parts[0].toLowerCase());

                        for (int i = 1; i < word1parts.length; i++) {
                            DoubleVector pvec = gl.get(word1parts[i].toLowerCase());
                            if (pvec != null && w1 != null) {
                                w1 = w1.add(pvec);
                            }
                        }
                    } else {
                        System.out.println("Null pointer.Can not find " + word1parts[0]);
                    }
                }
            }

            String[] word2parts;
            if (word2.contains(" ")) {
                word2parts = word2.split(" ");
            } else {
                word2parts = word2.split("(?=\\p{Upper})");
            }
            DoubleVector w2;
            if (word2parts.length == 1) {
                w2 = gl.get(word2.toLowerCase());
            } else {
                w2 = gl.get(word2parts[0].toLowerCase());
                for (int i = 1; i < word2parts.length; i++) {
                    DoubleVector pvec = gl.get(word2parts[i].toLowerCase());
                    if (pvec != null) {
                       w2 =  w2.add(pvec);
                    }
                }
            }

            if ((w1 == null) || (w2 == null)) {
                return 0;
            }
            CosineDistance cos = new CosineDistance();
            return 1 - cos.measureDistance(w1, w2);
        } catch (IOException ex) {
            Logger.getLogger(GloveRandomAccessReader.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Can not open Glove bin directory");
        }

        return 0;

    }

}
