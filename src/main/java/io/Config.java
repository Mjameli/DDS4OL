package io;


import de.citec.sc.lemon.core.Language;
import static de.citec.sc.lemon.core.Language.DE;
import static de.citec.sc.lemon.core.Language.EN;
import static de.citec.sc.lemon.core.Language.ES;
import static de.citec.sc.lemon.core.Language.JA;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import patterns.SparqlPattern;
import patterns.english.SparqlPattern_EN_Intransitive_PP;
import patterns.english.SparqlPattern_EN_Noun_PP_appos;
import patterns.english.SparqlPattern_EN_Noun_PP_copulative;
import patterns.english.SparqlPattern_EN_Noun_PP_possessive;
import patterns.english.SparqlPattern_EN_Predicative_Participle_passive;
import patterns.english.SparqlPattern_EN_Transitive_Verb;
import patterns.english.SparqlPattern_EN_Predicative_Participle_copulative;
import patterns.english.SparqlPattern_EN_Transitive_Passive;

public class Config {

	Logger logger = LogManager.getLogger(Config.class.getName());
	
	HashMap<String,String> params;
	
	String Model = "model";
	String GoldStandardLexicon = null;
	String OutputLexicon = "lexicon";
	String Output = "eval";
	Boolean Coreference = false;
        Boolean Statistics = true;
	String Classifier = "de.citec.sc.matoll.classifiers.FreqClassifier";
	Language Language = EN;
	Integer numItems;
	String Frequency;
        String BaseUri = "http://dblexipedia.org/";
        
        boolean RemoveStopwords = false;
        
        List<File> files = new ArrayList<>();

   

	
	List<SparqlPattern> Patterns = null;
	
	public Config()
	{
	}
	
	public void loadFromFile(String configFile) throws ParserConfigurationException, SAXException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, DOMException, Exception {
	
		// add logger here...
		System.out.print("Reading configuration from: "+configFile+"\n");
		
		File fXmlFile = new File(configFile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
	 
		//optional, but recommended
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();
	 
		NodeList nList = doc.getDocumentElement().getChildNodes();
		
	
		System.out.println("----------------------------");
		
		for (int i = 0; i < nList.getLength(); i++) {
			 
			Node node = nList.item(i);
									
			if (node.getNodeName().equals("Language"))
			{
				this.Language = mapToLanguage(node.getTextContent());
				
				if (Language.equals(EN))
				{
					Patterns = new ArrayList<SparqlPattern>();
					
					Patterns.add(new SparqlPattern_EN_Intransitive_PP());
					Patterns.add(new SparqlPattern_EN_Noun_PP_appos());
					Patterns.add(new SparqlPattern_EN_Noun_PP_copulative());
					Patterns.add(new SparqlPattern_EN_Predicative_Participle_passive());
					Patterns.add(new SparqlPattern_EN_Transitive_Verb());
					Patterns.add(new SparqlPattern_EN_Predicative_Participle_copulative());
                                        Patterns.add(new SparqlPattern_EN_Transitive_Passive());
                                        Patterns.add(new SparqlPattern_EN_Noun_PP_possessive());
//                                        Patterns.add(new SparqlPattern_EN_DatatypeNoun());
//					Patterns.add(new SparqlPattern_EN_DatatypeNoun_2());
//                                        Patterns.add((new SparqlPattern_EN_Noun_PP_player()));
					logger.info("Adding patterns 1-9 (EN) to pattern library \n");
				}
				
				
				
			}
			
			if (node.getNodeName().equals("Coreference"))
			{				
				if (node.getTextContent().equals("True")) this.Coreference = true;
				if (node.getTextContent().equals("False")) this.Coreference = false;
			}
                        
                        if (node.getNodeName().equals("RemoveStopwords"))
			{				
				if (node.getTextContent().equals("True")) this.RemoveStopwords = true;
				if (node.getTextContent().equals("False")) this.RemoveStopwords = false;
			}
                        
                        if (node.getNodeName().equals("doStatistics"))
			{				
				if (node.getTextContent().equals("True")) this.Statistics = true;
				if (node.getTextContent().equals("False")) this.Statistics = false;
			}
			
			if (node.getNodeName().equals("GoldStandardLexicon"))
			{
				this.GoldStandardLexicon = node.getTextContent();
			}
			
			if (node.getNodeName().equals("Classifier"))
			{
				this.Classifier = node.getTextContent();
			}
                        
                        if (node.getNodeName().equals("BaseURI"))
			{
				this.BaseUri = node.getTextContent();
			}
			
			if (node.getNodeName().equals("OutputLexicon"))
			{
				this.OutputLexicon = node.getTextContent();
			}
			
			if (node.getNodeName().equals("MinFrequency"))
			{
				this.Frequency = node.getTextContent();
			}
			
			
			if (node.getNodeName().equals("Output"))
			{
				this.Output = node.getTextContent();
			}
			
			if (node.getNodeName().equals("NumLexItems"))
			{
				this.numItems = new Integer(node.getTextContent());
			}
			
			if (node.getNodeName().equals("Model"))
			{
				this.Model = node.getTextContent();
			}
			
			if (node.getNodeName().equals("Patterns"))
			{
				Patterns = new ArrayList<SparqlPattern>();
				
				NodeList patterns = node.getChildNodes();
				
				for (int j = 0; j <  patterns.getLength(); j++) {
			
					Node pattern = patterns.item(j);
					
					if (pattern.getNodeName().equals("Pattern"))
					{
						Patterns.add((SparqlPattern) Class.forName(pattern.getTextContent()).newInstance());
						
					}	
						
				}

			}
                        
                        if (node.getNodeName().equals("Files"))
			{				
				NodeList patterns = node.getChildNodes();
				
				for (int j = 0; j <  patterns.getLength(); j++) {
			
					Node pattern = patterns.item(j);
					
					if (pattern.getNodeName().equals("File"))
					{
						files.add(new File(pattern.getTextContent()));
						
					}	
						
				}

			}
                        
		}
		
	}

        
        private Language mapToLanguage(String s) throws Exception {
            
            if      (s.toLowerCase().equals("en") || s.toLowerCase().equals("eng")) return EN;
            else if (s.toLowerCase().equals("de") || s.toLowerCase().equals("ger")) return DE;
            else if (s.toLowerCase().equals("es") || s.toLowerCase().equals("spa")) return ES;
            else if (s.toLowerCase().equals("ja") || s.toLowerCase().equals("jpn")) return JA;
            else throw new Exception("Language '" + s + "' unknown.");
        }
	

	public String getModel() {
		return Model;
	}



	public void setModel(String model) {
		Model = model;
	}



	public String getGoldStandardLexicon() {
		return GoldStandardLexicon;
	}



	public void setGoldStandardLexicon(String goldStandardLexicon) {
		GoldStandardLexicon = goldStandardLexicon;
	}



	public String getOutputLexicon() {
		return OutputLexicon;
	}



	public void setOutputLexicon(String outputLexicon) {
		OutputLexicon = outputLexicon;
	}



	public String getOutput() {
		return Output;
	}



	public void setOutput(String output) {
		Output = output;
	}



	public Boolean getCoreference() {
		return Coreference;
	}
        
        public Boolean removeStopwords() {
		return RemoveStopwords;
	}

	public String getFrequency()
	{
		return Frequency;
	}

	public void setCoreference(Boolean coreference) {
		Coreference = coreference;
	}



	public Language getLanguage() {
		return Language;
	}



	public void setLanguage(Language language) {
		Language = language;
	}

	public String getClassifier()
	{
		return Classifier;
	}


	public List<SparqlPattern> getPatterns()
	{
		if (Patterns.size() > 0)
		
		return Patterns;
		
		return null;
	}
        
        public String getBaseUri() {
            return BaseUri;
        }
         public List<File> getFiles() {
            return files;
        }

    public boolean doStatistics() {
        return Statistics;
    }

}
