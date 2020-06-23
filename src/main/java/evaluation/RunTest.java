/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import de.citec.sc.lemon.core.LexicalEntry;
import de.citec.sc.lemon.core.Lexicon;
import de.citec.sc.lemon.core.Reference;
import de.citec.sc.lemon.io.LexiconLoader;
import java.io.File;
import java.nio.file.Files;

/**
 *
 * @author swalter
 */
public class RunTest {

    public static void main(String[] args) {
//this method is just for test the properties of a lexicon
        
        LexiconLoader loader = new LexiconLoader();
        Lexicon gold = loader.loadFromFile("dbpedia2014_EN_Walter_Result.ttl");
//        Lexicon gold = loader.loadFromFile("../lexica/dbpedia_en.rdf");
        
        System.out.println("Loaded gold");
        for (LexicalEntry entry:gold.getEntries())
        for (Reference ref:entry.getReferences())
        {    if (ref!=null && !ref.getURI().contains("dbpedia.org"))
            System.out.println(ref.getURI());
        }
        
           }
}
