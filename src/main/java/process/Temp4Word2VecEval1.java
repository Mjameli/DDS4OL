/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process;

import de.citec.sc.lemon.core.LexicalEntry;
import de.citec.sc.lemon.core.Lexicon;
import de.citec.sc.lemon.core.Reference;
import de.citec.sc.lemon.core.Sense;
import de.citec.sc.lemon.core.SimpleReference;
import de.citec.sc.lemon.core.SyntacticBehaviour;
import de.citec.sc.lemon.io.LexiconLoader;
import static process.Matoll.lexicon2file;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
//import de.citec.sc.matoll.evaluation.LexiconEvaluationMacroNew;
import java.io.File;
import java.util.Arrays;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
//import org.deeplearning4j.berkeley.StringUtils;
//import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author Mehdi Jabalameli <Mehdi Jabalameli at ui.ac.ir>
 */
public class Temp4Word2VecEval1 {

    public static void main(String[] args) throws IOException {

        String word2vecpath = "/media/ubuntu/NewVolume/Jabalameli/googleword2vec/GoogleNews-vectors-negative300.bin";
        File gModel = new File(word2vecpath);
//        Nd4j.factory().setOrder('f');
        // Note: it is a very heavy process!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //do not run over laptop!!!!!!!!!!!!!!!!!!!!1
        // it needs about 10G RAM memory
        System.out.println("Loading Word2vec....(it may takes some minutes)");
        WordVectors vec = WordVectorSerializer.readWord2VecModel(gModel);
        System.out.println("Finished.");
        
        
        String input_lexicon = "autogen19_04_2019_beforeTraining.ttl";

        LexiconLoader loader = new LexiconLoader();
        Lexicon automatic_lexicon;
        automatic_lexicon = loader.loadFromFile(input_lexicon);
        System.out.println("Loaded " + input_lexicon);

        Lexicon automatic_lexicon_refined = new Lexicon();
        automatic_lexicon_refined.setBaseURI(automatic_lexicon.getBaseURI());

        for (float threshold = 0.1f; threshold <= 0.9f; threshold += 0.1f) {
            threshold = Math.round(threshold * 10) / 10.0f;  //JAVA floating point rep. is not so exact.
            System.out.println("threshold =" + threshold + ")");
            for (LexicalEntry entry : automatic_lexicon.getEntries()) {
                String can_form = entry.getCanonicalForm();
                for (Reference ref : entry.getReferences()) {
                    if (ref instanceof SimpleReference) {
                        String ref_string = ref.getURI().substring(ref.getURI().lastIndexOf("/") + 1, ref.getURI().length());
                        double sim = similiarty(can_form, ref_string, vec);
                        if (sim < threshold) {
                            HashMap<Sense, HashSet<SyntacticBehaviour>> sb = entry.getSenseBehaviours();

                            sb.entrySet().removeIf(e -> e.getKey().getReference().equals(ref)); //remove the sense 
                        }
                    }
                }
                if (entry.getReferences() == null || entry.getReferences().isEmpty()) {
                    //automatic_lexicon.getEntries().remove(entry); !!!not worked

                } else {
                    automatic_lexicon_refined.addEntry(entry);
                }
            }

            System.out.println("Writing lexicon to file:(threshold =" + threshold + ")");
            System.out.println("Size of lexicon:" + automatic_lexicon_refined.size());
            lexicon2file(automatic_lexicon_refined, input_lexicon + "_Word2vec_" + threshold + ".lex");
            System.out.println("saved.");

        }
        //  System.out.println("Actual number used sentences:" + Integer.toString(sentence_counter));
        System.out.println("The size of automatic lexicon:" + automatic_lexicon.size());
        // System.out.println("The size of refined automatic lexicon:" + automatic_lexicon_refined.size());

    }

    public static double similiarty(String word1, String word2, WordVectors vec) {
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
            double[] w1;
            if (word1parts.length == 1) {
                w1 = vec.getWordVector(word1.toLowerCase());
            } else {
                w1 = vec.getWordVector(word1parts[0].toLowerCase());
                if (w1 == null) {
                    if (word1parts[0].contains("-")) {
                        String[] wps = word1parts[0].split("-");

                        int len = wps.length;
                        String[] temp = new String[len + word1parts.length - 1];
                        System.arraycopy(wps, 0, temp, 0, len);

                        System.arraycopy(word1parts, 1, temp, len, word1parts.length - 1);
                        word1parts = temp;
                        w1 = vec.getWordVector(word1parts[0].toLowerCase());

                        for (int i = 1; i < word1parts.length; i++) {
                            double[] pvec = vec.getWordVector(word1parts[i].toLowerCase());
                            if (pvec != null && w1 != null) {
                                add(w1, pvec);
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
            double[] w2;
            if (word2parts.length == 1) {
                w2 = vec.getWordVector(word2.toLowerCase());
            } else {
                w2 = vec.getWordVector(word2parts[0].toLowerCase());
                for (int i = 1; i < word2parts.length; i++) {
                    double[] pvec = vec.getWordVector(word2parts[i].toLowerCase());
                    if (pvec != null) {
                        add(w2, pvec);
                    }
                }
            }

            if ((w1 == null) || (w2 == null)) {
                return 0;
            }
            
            return cosineSimilarity(w1, w2);
        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return 0;

    }

    private static void add(double[] vector1, double[] pvec) {
        //vector1 = vector1 +pvec
        for (int i = 0; i < vector1.length; i++) {
            vector1[i] += pvec[i];
        }
    }

    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
