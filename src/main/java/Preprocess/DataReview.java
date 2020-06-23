/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Preprocess;

import ir.ac.ui.ontolex.CleanAndParse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Mehdi Jabalameli <Mehdi Jabalameli at ui.ac.ir>
 */
public class DataReview {

    public static void main(String[] args) throws FileNotFoundException, IOException {

        File inputFile = new File("J:\\wikipedia\\dblexpedia_verb_Sentences.tsv");
        File outputFile = new File("J:\\wikipedia\\dblexpedia_verb_Sentences_Expert_Evaluated.tsv");

        BufferedReader r;

        try {
            r = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            FileWriter fw = new FileWriter(outputFile, true);//append to the file
            BufferedWriter w = new BufferedWriter(fw); 
            String line;
            String propname = "";
int line_num =0;
            while ((line = r.readLine()) != null) {

                line_num++;
                String[] sentence = line.split("\t");
                String plainSentence;

                if (!sentence[3].equals(propname)) {
                    JOptionPane.showMessageDialog(null, "The following entries are for " + sentence[3]);
                    propname = sentence[3];
                }

                int result;
                int dialogButton = JOptionPane.YES_NO_OPTION;
                int dialogResult = JOptionPane.showConfirmDialog(null, sentence[0] + "\n" + sentence[4] + "\t" + sentence[5] + "\n" + sentence[6], sentence[3], dialogButton);
                if (dialogResult == 0) {
                    // System.out.println("Yes option");
                    result = 1;
                } else {
                    //System.out.println("No Option");
                    result = 0;
                }

                //writing to output file
                w.write(line + "\t" + result + "\n");
                
                if ( line_num % 20==0) {
                    w.flush();
                    fw.flush();
                } 
            }

            w.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CleanAndParse.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CleanAndParse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
