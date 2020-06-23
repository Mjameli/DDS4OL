/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import org.apache.jena.rdf.model.Model;

import de.citec.sc.lemon.core.LexicalEntry;
import de.citec.sc.lemon.core.Lexicon;
import de.citec.sc.lemon.io.LexiconLoader;
import de.citec.sc.lemon.io.LexiconSerialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

/**
 *
 * @author swalter
 */
public class RunEvaluation {

    public static void main(String[] args) {
        //calculate Macro measures

        double[] thresholds = new double[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 0.95f};


        String embedding; //  = "Bert";   

        LexiconLoader loader = new LexiconLoader();
        Lexicon gold = loader.loadFromFile("../lexica/dbpedia_en.rdf");
//        Lexicon gold = loader.loadFromFile("../lexica/dbpedia_en_Unger.nt");

        System.out.println("Number of dbpedia properties in gold lexicon is ");

        String autolex_path_base;

        if (args.length > 0) {
            embedding = args[0];
        } else {
            embedding = "Bert";
        }

        if (args.length > 1) {
            autolex_path_base = args[1];
        } else {
            autolex_path_base = "/media/Volume3/Jabalameli/M-Atoll/Outputs/autogen2020_04_01_GoldPropOnly_BertPreFilter_shortestPathOnly_withStopwords_sentence_bert_similiarity_threshold__beforeTraining.ttl#SIM_THRESHOLD#_beforeTraining.ttl";
        }

        System.out.println("Autolex path: " + autolex_path_base);
        System.out.println("Embedding = " + embedding);

        System.out.println("Macro  Result");
        System.out.println("Thrshld\tPrecisn\tRecall\tF-Measr");
        
        for (double threshold : thresholds) {
            threshold = Math.round(threshold * 100) / 100.0;  //JAVA floating point rep. is not so exact.
            String autolex_path;

            autolex_path = autolex_path_base.replace("#SIM_THRESHOLD#", String.valueOf(threshold));

//            Lexicon automatic_reduced = new Lexicon();
            File lex_file = new File(autolex_path);
            if (!lex_file.exists()) {
                continue;
            }
            Lexicon automatic = loader.loadFromFile(autolex_path);

            List<Double> result = LemmaBasedEvaluation.evaluate(automatic, gold, true, true);

            System.out.print(threshold);
//            System.out.print("\t" + LemmaBasedEvaluation.getActualSize(automatic)); //actual size available in Micro evaluations
            System.out.println("\t" + result.get(0) + "\t" + result.get(1) + "\t" + result.get(2));

        }

        System.out.println("Micro  Result");
        System.out.println("Thrshld\tPrecisn\tRecall\tF-Measr\t#correct\t#auto\t#gold");
        for (double threshold : thresholds) {
            threshold = Math.round(threshold * 100) / 100.0;  //JAVA floating point rep. is not so exact.

            String autolex_path;

            autolex_path = autolex_path_base.replace("#SIM_THRESHOLD#", String.valueOf(threshold));

//            Lexicon automatic_reduced = new Lexicon();
            File lex_file = new File(autolex_path);
            if (!lex_file.exists()) {
                continue;
            }

            Lexicon automatic = loader.loadFromFile(autolex_path);

            List<Double> result = LemmaBasedEvaluation.evaluate(automatic, gold, true, false);

            System.out.print(threshold);
//            System.out.print("\t" + LemmaBasedEvaluation.getActualSize(automatic));
            System.out.print("\t" + result.get(0) + "\t" + result.get(1) + "\t" + result.get(2));
            System.out.println("\t" + Math.round(result.get(3)) + "\t" + Math.round(result.get(4)) + "\t" + Math.round(result.get(5)));

        }

    }

    private static List<String> loadDataset(String file) {
        List<String> properties = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(file)));
            String[] lines = content.split("\n");
            for (String l : lines) {
                properties.add(l);
            }

        } catch (IOException ex) {
            System.out.println("Did not find file: " + file);
        }
        return properties;
    }

    private static void writeLexicon(Lexicon lexicon, String name) throws FileNotFoundException {
        LexiconSerialization serializer = new LexiconSerialization();

        Model model = ModelFactory.createDefaultModel();

        serializer.serialize(lexicon, model);

        FileOutputStream out = new FileOutputStream(new File(name + ".ttl"));

        RDFDataMgr.write(out, model, RDFFormat.TURTLE);
    }

    private static void writeLex(Lexicon lexicon, String filename) throws IOException {
        FileWriter writer = new FileWriter(filename);

        for (LexicalEntry entry : lexicon.getEntries()) {
            writer.write(entry.toString() + "\n");
            writer.flush();
        }

        writer.close();
    }

}
