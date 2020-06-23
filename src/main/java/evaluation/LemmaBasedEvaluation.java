package evaluation;

import java.util.HashMap;

import de.citec.sc.lemon.core.LexicalEntry;
import de.citec.sc.lemon.core.Lexicon;
import de.citec.sc.lemon.core.Reference;
import de.citec.sc.lemon.core.Restriction;
import de.citec.sc.lemon.core.SimpleReference;
import de.citec.sc.lemon.io.LexiconLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LemmaBasedEvaluation {

//This is the latest Walter evaluation file (Mehdi)
    public static List<Double> evaluate(Lexicon automatic, Lexicon gold, boolean onlyProperties, boolean macro_averaged) {

        Map<String, List<List<String>>> hm_automatic = new HashMap<>();
        Map<String, List<List<String>>> hm_gold = new HashMap<>();

        /*
         First create map with reference as key and an array with pos,form and ref for each sese/ref in each entry
         */
        createMap(automatic, hm_automatic, onlyProperties);
        createMap(gold, hm_gold, onlyProperties);
        /*
         Then do evaluation based on the map
         */
        if (macro_averaged) {
            return macro_averagedEvaluation(hm_automatic, hm_gold);
        } else {
            return micro_averagedEvaluation(hm_automatic, hm_gold);
        }

    }

    public static List<Double> evaluate(Lexicon automatic, Lexicon gold, boolean onlyProperties, List<String> uris, boolean macro_averaged) {

        Map<String, List<List<String>>> hm_automatic = new HashMap<>();
        Map<String, List<List<String>>> hm_gold = new HashMap<>();

        /*
         First create map with reference as key and an array with pos,form and ref for each sese/ref in each entry
         */
        createMap(automatic, hm_automatic, onlyProperties);
        createMap(gold, hm_gold, onlyProperties);

        Map<String, List<List<String>>> hm_automatic_new = new HashMap<>();
        Map<String, List<List<String>>> hm_gold_new = new HashMap<>();

        for (String uri : hm_automatic.keySet()) {
            if (uris.contains(uri)) {
                hm_automatic_new.put(uri, hm_automatic.get(uri));
            }
        }

        for (String uri : hm_gold.keySet()) {
            if (uris.contains(uri)) {
                hm_gold_new.put(uri, hm_gold.get(uri));
            }
        }

//            for(String x : hm_automatic_new.keySet()){
//                System.out.println(x+":"+hm_automatic_new.get(x));
//            }
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            for(String x : hm_gold_new.keySet()){
//                System.out.println(x+":"+hm_gold_new.get(x));
//            }
        /*
         Then do evaluation based on the map
         */
        if (macro_averaged) {
            return macro_averagedEvaluation(hm_automatic_new, hm_gold_new);
        } else {
            return micro_averagedEvaluation(hm_automatic_new, hm_gold_new);
        }

    }

    public static void createMap(Lexicon lexicon, Map<String, List<List<String>>> hm, boolean onlyProperties) {
        for (LexicalEntry entry : lexicon.getEntries()) {
            String pos = entry.getPOS();
            String form = entry.getCanonicalForm();
            for (Reference ref : entry.getReferences()) {
                try {
                    if (onlyProperties) {
                         String uri = "";
                        if (ref instanceof de.citec.sc.lemon.core.SimpleReference) {
                            SimpleReference reference = (SimpleReference) ref;
                            uri = reference.getURI();
                        }
                        if (ref instanceof de.citec.sc.lemon.core.Restriction) {

                            Restriction reference = (Restriction) ref;
                            uri = reference.getProperty();
                        }
                        if (uri.contains("unger"))  //some refs may be outside of dbpedia. e.g. "http://github.com/cunger/lemon.dbpedia/target/dbpedia_all#batLeft"
                            continue;
                        String tmp_uri = uri.replace("http://dbpedia.org/ontology/", "");
                        if (!Character.isUpperCase(tmp_uri.charAt(0))) {
                            List<String> tmp = new ArrayList<>();
                            pos = pos.replace("http://www.lexinfo.net/ontology/2.0/lexinfo#noun", "http://www.lexinfo.net/ontology/2.0/lexinfo#commonNoun");
                            pos = pos.replace("http://www.lexinfo.net/ontology/2.0/lexinfo#", "");
                            tmp.add(pos);
                            tmp.add(form);
                            tmp.add(uri);
//                                System.out.println(uri);
                            if (hm.containsKey(uri)) {
                                List<List<String>> tmp_list = hm.get(uri);
                                tmp_list.add(tmp);
                                hm.put(uri, tmp_list);
                            } else {
                                List<List<String>> tmp_list = new ArrayList<>();
                                tmp_list.add(tmp);
                                hm.put(uri, tmp_list);
                            }
                        }
                    } else {
                        String uri = "";
                        if (ref instanceof de.citec.sc.lemon.core.SimpleReference) {
                            SimpleReference reference = (SimpleReference) ref;
                            uri = reference.getURI();
                        }
                        if (ref instanceof de.citec.sc.lemon.core.Restriction) {

                            Restriction reference = (Restriction) ref;
                            uri = reference.getProperty();
                        }
                        
                        if (uri.contains("unger"))  //some refs may be outside of dbpedia. e.g. "http://github.com/cunger/lemon.dbpedia/target/dbpedia_all#batLeft"
                            continue;
                        
                        List<String> tmp = new ArrayList<>();
                        pos = pos.replace("http://www.lexinfo.net/ontology/2.0/lexinfo#noun", "http://www.lexinfo.net/ontology/2.0/lexinfo#commonNoun");
                        pos = pos.replace("http://www.lexinfo.net/ontology/2.0/lexinfo#", "");
                        tmp.add(pos);
                        tmp.add(form);
                        tmp.add(uri);
                        if (hm.containsKey(uri)) {
                            List<List<String>> tmp_list = hm.get(uri);
                            tmp_list.add(tmp);
                            hm.put(uri, tmp_list);
                        } else {
                            List<List<String>> tmp_list = new ArrayList<>();
                            tmp_list.add(tmp);
                            hm.put(uri, tmp_list);
                        }
                    }

                } catch (Exception e) {
                    /*
                     Do nothing, some of the lexicon entries do not have a reference
                     */
                }

            }
        }
    }

    private static List<Double> macro_averagedEvaluation(Map<String, List<List<String>>> hm_automatic, Map<String, List<List<String>>> hm_gold) {
        /*
         Calculate Recall/Precision per Property(from gold) and average at the end over number properties gold
         */
        double recall = 0.0;
        double precision = 0.0;

        List<Double> overall_results = new ArrayList<Double>();

        for (String uri : hm_gold.keySet()) {
            List<List<String>> entries_gold = hm_gold.get(uri);
            if (hm_automatic.containsKey(uri)) {
                List<List<String>> entries_automatic = hm_automatic.get(uri);
                int correct_entries = 0;
                int lenght_gold = entries_gold.size();
                int lenght_automatic = entries_automatic.size();
                for (List<String> entry_gold : entries_gold) {
                    for (List<String> entry_automatic : entries_automatic) {
                        if (entry_gold.get(0).equals(entry_automatic.get(0))
                                && entry_gold.get(1).equals(entry_automatic.get(1))
                                && entry_gold.get(2).equals(entry_automatic.get(2))) {
                            correct_entries += 1;
                            break;
                        }
                    }
                }

                double local_recall = (correct_entries + 0.0) / lenght_gold;
                double local_precision = (correct_entries + 0.0) / lenght_automatic;
//                System.out.println(uri+":"+local_recall);
                recall += local_recall;
                precision += local_precision;

            } else {
                recall += 0.0;
                /*
                 if no entry for a uri from gold is found in automatic lexicon, set precision to 1.0
                 */
                precision += 1.0;
            }
        }

        Double global_recall = roundDown4(recall / hm_gold.size());
        Double global_precision = roundDown4(precision / hm_gold.size());
        overall_results.add(global_precision);
        overall_results.add(global_recall);
        overall_results.add(roundDown4((2 * global_recall * global_precision) / (global_recall + global_precision)));

        return overall_results;
    }

    private static List<Double> micro_averagedEvaluation(Map<String, List<List<String>>> hm_automatic, Map<String, List<List<String>>> hm_gold) {

        int global_correct_entries = 0;
        double precision = 0.0;

        List<Double> overall_results = new ArrayList<>();
        int number_entries_gold = 0;
        int number_entries_automatic = 0;

        for (String uri : hm_gold.keySet()) {
            List<List<String>> entries_gold = hm_gold.get(uri);
            number_entries_gold += entries_gold.size();
            if (hm_automatic.containsKey(uri)) {
                List<List<String>> entries_automatic = hm_automatic.get(uri);
                number_entries_automatic += entries_automatic.size();
                
                for (List<String> entry_gold : entries_gold) {
                    for (List<String> entry_automatic : entries_automatic) {
                        if (entry_gold.get(0).equals(entry_automatic.get(0))
                                && entry_gold.get(1).equals(entry_automatic.get(1))
                                && entry_gold.get(2).equals(entry_automatic.get(2))) {
                            global_correct_entries += 1;
                            break;
                        }
                    }
                }
                
                
                //Mehdi: Just for debug
               
//                for (List<String> entry_automatic : entries_automatic) {
//                boolean found = false;
//                    for (List<String> entry_gold : entries_gold) {
//                    
//                        if (entry_gold.get(0).equals(entry_automatic.get(0))
//                                && entry_gold.get(1).equals(entry_automatic.get(1))
//                                && entry_gold.get(2).equals(entry_automatic.get(2))) {
//                            found = true;
//                            break;
//                        }
//                    }
//                    if (!found)
//                        System.out.println(entry_automatic);
//                }
//                
                
                
//                global_correct_entries+=correct_entries;
            }
        }
        Double global_recall = roundDown4((global_correct_entries + 0.0) / number_entries_gold);
        Double global_precision = roundDown4((global_correct_entries + 0.0) / number_entries_automatic);
        overall_results.add(global_precision);
        overall_results.add(global_recall);
        overall_results.add(roundDown4((2 * global_recall * global_precision) / (global_recall + global_precision)));
        overall_results.add(global_correct_entries+0.0);
        overall_results.add(number_entries_automatic+0.0);
        overall_results.add(number_entries_gold+0.0);
        
        
        return overall_results;
    }

    
//    public static int getActualSize(Lexicon lex) {  //Mehdi: it returns the number of all complete entries .
//        //component entries will be dropped
//        int size = 0;
//
//        for (LexicalEntry entry : lex.getEntries()) {
//            for (Reference ref : entry.getReferences()) {
//                if (ref != null) {
//                    size++;
//                    break;
//                }
//            }
//
//        }
//
//        return size;
//    }
    
    
    public static double roundDown4(double d) {
        return (long) (d * 1e4) / 1e4;
    }

    public static void main(String[] args) {
        //This is the latest Walter evaluation file(Mehdi)

        //Do not remove commented lines at the end of the main . They are useful.
        String path_gold_english = "../lexica/dbpedia_en.rdf";
        String path_automatic_english = "dbpedia2014_EN_Walter_Result.ttl";
//        String path_automatic_english = "dbpedia2014Full_new_beforeTraining.ttl";

        LexiconLoader loader = new LexiconLoader();

        Lexicon gold_english = loader.loadFromFile(path_gold_english);

        Lexicon automatic_english = loader.loadFromFile(path_automatic_english);

        List<Double> result = LemmaBasedEvaluation.evaluate(automatic_english, gold_english, true, true);
        System.out.println("(Dependency Based Approach): Macro  P:" + result.get(0) + ", R:" + result.get(1) + ", F:" + result.get(2));

        result = LemmaBasedEvaluation.evaluate(automatic_english, gold_english, true, false);
        System.out.println("(Dependency Based Approach): Micro  P:" + result.get(0) + ", R:" + result.get(1) + ", F:" + result.get(2));

        
        
//        writer.println("English: Only properties");
//        result = LemmaBasedEvaluation.evaluate(automatic_english, gold_english,true,true);
//        writer.println("(Dependency Based Approach): Macro  P:"+result.get(0)+", R:"+result.get(1)+", F:"+result.get(2));
//        
//        result = LemmaBasedEvaluation.evaluate(automatic_english, gold_english,true,false);
//        writer.println("(Dependency Based Approach): Micro  P:"+result.get(0)+", R:"+result.get(1)+", F:"+result.get(2));
//    
//        writer.println("English: Properties and Classes");
//        result = LemmaBasedEvaluation.evaluate(automatic_label_based_approach, gold_english,false,true);
//        writer.println("(Label Based Approach): Macro  P:"+result.get(0)+", R:"+result.get(1)+", F:"+result.get(2));
//    
//        Set<String> set_gold = new HashSet<>();
//        Set<String> set_automatic = new HashSet<>();
//        for(LexicalEntry entry:gold_english.getEntries()){
//            try{
//                for(Sense sense:entry.getSenseBehaviours().keySet()){
//                    set_gold.add(sense.getReference().getURI());
//                }
//            }
//            catch(Exception e){}
//        }
//        
//        for(LexicalEntry entry:automatic_english.getEntries()){
//            try{
//                for(Sense sense:entry.getSenseBehaviours().keySet()){
//                    set_automatic.add(sense.getReference().getURI());
//                }
//            }
//            catch(Exception e){}
//        }
//        set_automatic.retainAll(set_gold);
//        List<String> uris = new ArrayList<>();
//        for(String x: set_automatic) uris.add(x);
//        
//    result = LemmaBasedEvaluation.evaluate(joined, gold_english,true,uris,true);
//        writer.println("(Only automatic properties): Macro  P:"+result.get(0)+", R:"+result.get(1)+", F:"+result.get(2));
    }

}
