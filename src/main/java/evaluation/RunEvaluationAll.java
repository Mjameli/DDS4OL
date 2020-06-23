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
public class RunEvaluationAll {

    public static void main(String[] args) {
        //calculate Macro measures

        String embedding = "CoreBert";     //"Glove" or "Word2vec"

        LexiconLoader loader = new LexiconLoader();
        Lexicon gold = loader.loadFromFile("../lexica/dbpedia_en.rdf");
        System.out.println("Number of dbpedia properties in gold lexicon is ");

        String autolex_path_baseform;

        List<String> similiarity_methods = new ArrayList<>();
        //similiarity_methods.add("similiarity_sum");
        similiarity_methods.add("similiarity_mean");
        similiarity_methods.add("max_similiarity");
        similiarity_methods.add("mean_similiarity");
        similiarity_methods.add("mas_asym_similiarity");
        similiarity_methods.add("mas_sym_similiarity");

//autogen2020_04_01_GoldPropOnly_CoreBertLayer12_withStopwords_mas_sym_similiarity_0.9_beforeTraining
        if (args.length > 0) {
            autolex_path_baseform = args[0];
        } else {

            autolex_path_baseform = "/media/Volume3/Jabalameli/M-Atoll/Outputs/autogen2020_04_01_GoldPropOnly_CoreBertLayer12_shortestPathOnly_withStopwords_#SIM_METHOD#_#SIM_THRESHOLD#_beforeTraining.ttl";
        }

		//autogen2020_04_01_GoldPropOnly_ELMO_shortestPathOnly_mas_asym_similiarity_0.1
        String autolex_path_base;
        for (String sim_method : similiarity_methods) {
            autolex_path_base = autolex_path_baseform.replace("#SIM_METHOD#", sim_method);

            System.out.println("Autolex path: " + autolex_path_base);
            System.out.println("Embedding = " + embedding);

            System.out.println("Macro  Result");
            System.out.println("Thrshld\tPrecisn\tRecall\tF-Measr");
            for (float threshold = 0.0f; threshold <= 0.91f; threshold += 0.1f) {
                threshold = Math.round(threshold * 10) / 10.0f;  //JAVA floating point rep. is not so exact.

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
            System.out.println("Thrshld\tPrecisn\tRecall\tF-Measr\t#corrct\t#auto\t#gold");
            for (float threshold = 0.0f; threshold <= 0.91f; threshold += 0.1f) {
                threshold = Math.round(threshold * 10) / 10.0f;  //JAVA floating point rep. is not so exact.

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
