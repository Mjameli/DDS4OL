/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package process;
//import com.robrua.nlp.bert.Bert;

import com.robrua.nlp.bert.FullTokenizer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Mehdi Jabalameli <Mehdi Jabalameli at ui.ac.ir>
 */
public class BertTest {

    public static void main(String[] args) {
        try (Bert bert = Bert.load("com/robrua/nlp/easy-bert/bert-uncased-L-12-H-768-A-12")) {

            long startTime = System.nanoTime();

           // String sent1 = "stringmatching is a text";
            String sent1 = "This semester is spring.";

            
            float[][] embeddings1 = bert.embedWords(sent1);
            String[] tokens = bert.getTokenizer().tokenize(sent1);
            System.out.println("# of tokens =" + tokens.length);
            System.out.println("# of embedded words = " + embeddings1.length);
            
            //List<Pair<String, float[]>> embeddings2 = bert.embedWords(sent1);
            
            for (int i=0; i<embeddings1.length; i++)
            {
                for (int j=0; j<30; j++)
                    System.out.print(embeddings1[i][j]);
                System.out.println("");
            }
           
//            for (Pair<String, float[]> elem:embeddings2){
//            for (int j=0; j<30; j++)
//                    System.out.print(elem.getRight()[j]);
//                System.out.println("");
//            }
//                
            
            
            
            System.out.println(embeddings1);
            

            //double sim = de.citec.sc.matoll.process.MatollWithBertPreFilter.calculate_similiarty("esst iss ffor tesdt ansd edxamplde", "isd thdis judst fodr fudn", bert, "bert_similiarity");

            long endTime = System.nanoTime();

            long durationInNano = (endTime - startTime);

            long durationInMillis = TimeUnit.NANOSECONDS.toMillis(durationInNano);  //Total execution time in nano seconds

            System.out.println("duration(mili) = " + durationInMillis);

            //System.out.println("The similarity is " + sim);

        }

        System.out.println("test");
    }

    

    

}
