package preprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import de.citec.sc.lemon.core.Language;
import static de.citec.sc.lemon.core.Language.JA;
import coreference.Coreference;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ModelPreprocessor {

	HashMap<String,String> Resource2Lemma;
	HashMap<String,String> Resource2Head;
	HashMap<String,String> Resource2Dependency;
	
	HashMap<Integer,String> Int2NodeMapping;
	HashMap<String,Integer> Node2IntMapping;
	
	HashMap<String,String> senseArgs;

    
        boolean     doCoref;
        Coreference coreference = new Coreference();
	
        Language    language;
        
	Set<String> DEP;

	
	public ModelPreprocessor(Language language)
	{
                this.language = language; 
		DEP = new HashSet<String>();
                doCoref = false;
	}
	        
	/**
         * @param model
         * @param subjectEntity
         * @param objectEntity 
         */
	public void preprocess(Model model, String subjectEntity,
			String objectEntity, Language language) {
		
		Resource2Lemma = getResource2Lemma(model);
		Resource2Head = getResource2Head(model);
		Resource2Dependency = getResource2Dependency(model);
		
		Int2NodeMapping = new HashMap<Integer,String>();
		Node2IntMapping = new HashMap<String,Integer>();
		
		senseArgs = new HashMap<String,String>();
		
		getMappings(Int2NodeMapping,Node2IntMapping,model);
		
		List<Hypothesis> hypotheses;
		
		String root;
		
		if (objectEntity != null)
		{
			List<List<String>> objectResources = getResources(model,objectEntity);
			hypotheses = getHypotheses(objectResources);
                        if(language.equals(JA)){
                            for(Hypothesis tmp : hypotheses){
                                for( String tmp2 : tmp.getNodes()){
                                    model.add(model.getResource(tmp2), model.createProperty("own:senseArg"), model.createResource("http://lemon-model.net/lemon#objOfProp"));

                                    senseArgs.put(tmp2, "http://lemon-model.net/lemon#objOfProp");
                                }
                            }
                        }
                        
                        else{
                            for (Hypothesis hypo: hypotheses)
                            {
                                    // System.out.print("Final hypo: "+hypo.toString());

                                    root = hypo.checkValidAndReturnRoot(Resource2Head,Resource2Dependency,DEP);

                                    if (root != null) 
                                    {
                                            model.add(model.getResource(root), model.createProperty("own:senseArg"), model.createResource("http://lemon-model.net/lemon#objOfProp"));
                                            senseArgs.put(root, "http://lemon-model.net/lemon#objOfProp");
                                    }
                            }
                        }
			
		}
		
		if (subjectEntity != null)
		{
			List<List<String>> subjectResources = getResources(model,subjectEntity);
			 hypotheses = getHypotheses(subjectResources);
                         if(language.equals(JA)){
                            for(Hypothesis tmp : hypotheses){
                                for( String tmp2 : tmp.getNodes()){
                                    model.add(model.getResource(tmp2), model.createProperty("own:senseArg"), model.createResource("http://lemon-model.net/lemon#subjOfProp"));

                                    senseArgs.put(tmp2, "http://lemon-model.net/lemon#subjOfProp");
                                }
                            }
                        }
                        else{
                             for (Hypothesis hypo: hypotheses)
                            {

                                    // System.out.print("Final hypo: "+hypo.toString());

                                    root = hypo.checkValidAndReturnRoot(Resource2Head,Resource2Dependency,DEP);

                                    if (root != null) 
                                    {	
                                            model.add(model.getResource(root), model.createProperty("own:senseArg"), model.createResource("http://lemon-model.net/lemon#subjOfProp"));
                                            senseArgs.put(root, "http://lemon-model.net/lemon#subjOfProp");
                                    }	
                            }
                        }
			
		}
		
                if (doCoref) try {
                    coreference.computeCoreference(model,language);
                } catch (Exception ex) {
                    Logger.getLogger(ModelPreprocessor.class.getName()).log(Level.SEVERE, null, ex);
                }
				
	}
        /**
         * 
         * @param int2NodeMapping
         * @param node2IntMapping
         * @param model 
         */
	private void getMappings(HashMap<Integer, String> int2NodeMapping,
			HashMap<String, Integer> node2IntMapping, Model model) {
		
		StmtIterator iter;
		
		Statement stmt;
		
		String node;
		
		String number;
		
		iter = model.listStatements(null,model.getProperty("conll:wordnumber"), (RDFNode) null);
		
		while (iter.hasNext()) {
					
			stmt = iter.next();
			
			node = stmt.getSubject().toString();
			number = stmt.getObject().toString();
			
			int2NodeMapping.put(new Integer(number), node);
			node2IntMapping.put(node, new Integer(number));
		}
		
	}

        /**
         * 
         * @param resources
         * @return 
         */
	private List<Hypothesis> getHypotheses(List<List<String>> resources) {
		
		List<Hypothesis> hypotheses = new ArrayList<Hypothesis>();
		
		List<Hypothesis> expanded_hypotheses;
		
		hypotheses.add(new Hypothesis());
				
		for (List<String> nodes: resources)
		{
			// System.out.print("Checking nodes: "+nodes+"\n");
			
			expanded_hypotheses = new ArrayList<Hypothesis>();
			
			
			for (Hypothesis hypo: hypotheses)
			{
				// System.out.print("Expanding: "+hypo.toString());
				
				for (Hypothesis hypot: hypo.expand(nodes))
				{
			
					// System.out.print("Adding: "+hypot.toString());
					expanded_hypotheses.add(hypot);
				}
				
			}
			
			hypotheses = expanded_hypotheses;
		}
		
		return hypotheses;
		
	}
        /**
         * 
         * @param model
         * @param string
         * @return 
         */
	private List<List<String>> getResources(Model model,
			String string) {
		
		String[] tokens = string.split(" ");
		
		ArrayList<List<String>> resourceList = new ArrayList<List<String>>();
		
		ArrayList<String> wordResources;
		
		StmtIterator iter;
		
		Statement stmt;
		
		for (int i=0; i < tokens.length; i++)
		{
			wordResources = new ArrayList<String>();
			
			iter = model.listStatements(null,model.getProperty("conll:form"), (RDFNode) null);
		
			while (iter.hasNext()) {
						
				stmt = iter.next();
				
				if (stmt.getObject().toString().equals(tokens[i]))
				{
					wordResources.add(stmt.getSubject().toString());
					// System.out.println(stmt.getSubject().toString()+" has form "+stmt.getObject().toString());
				}
				
			}
			
			resourceList.add(wordResources);
							
		}	

		 return resourceList;
	}
        /**
         * 
         * @param model
         * @return 
         */
	private HashMap<String, String> getResource2Dependency(Model model) {
		
		HashMap<String,String> resource2Dep = new HashMap<String,String>();
		
		StmtIterator iter;
		
		Statement stmt;
		
		iter = model.listStatements(null,model.getProperty("conll:deprel"), (RDFNode) null);
		
		while (iter.hasNext()) {
					
			stmt = iter.next();
			
			resource2Dep.put(stmt.getSubject().toString(), stmt.getObject().toString());
			
			// System.out.println(stmt.getSubject().toString()+" has dependency "+stmt.getObject().toString());
		
			
		}
		
		return resource2Dep;
	   
	}
/**
 * 
 * @param model
 * @return 
 */
	private HashMap<String, String> getResource2Head(Model model) {
		
		HashMap<String,String> resource2Head = new HashMap<String,String>();
		
		StmtIterator iter;
		
		Statement stmt;
		
		iter = model.listStatements(null,model.getProperty("conll:head"), (RDFNode) null);
		
		while (iter.hasNext()) {
					
			stmt = iter.next();
			
			resource2Head.put(stmt.getSubject().toString(), stmt.getObject().toString());
			
			// System.out.println(stmt.getSubject().toString()+" has head "+stmt.getObject().toString());
						
		}
		
		return resource2Head;
	}
        /**
         * 
         * @param model
         * @return 
         */
	private HashMap<String, String> getResource2Lemma(Model model) {
		
		HashMap<String,String> resource2Lemma = new HashMap<String,String>();
		
		StmtIterator iter;
		
		Statement stmt;
		
		iter = model.listStatements(null,model.getProperty("conll:form"), (RDFNode) null);
		
		while (iter.hasNext()) {
					
			stmt = iter.next();
			
			resource2Lemma.put(stmt.getSubject().toString(), stmt.getObject().toString());
			
			// System.out.println(stmt.getSubject().toString()+" has lemma "+stmt.getObject().toString());
			
		}
		
		return resource2Lemma;
	}

	public void setCoreferenceResolution(boolean b) {
		doCoref = b;	
	}

	public void setDEP(Set<String> dep) {
		DEP = dep;		
	}
        
        public void setLanguage(Language l) {
               language = l;
        }

}
