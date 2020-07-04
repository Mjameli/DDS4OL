# Denoising Distant Supervision for Ontology Lexicalization using Semantic Similarity Measures

Ontology lexicalization aims to provide information about how the elements of an ontology are verbalized in a given language. Most ontology lexicalization techniques require labeled training data, which are usually generated automatically using the distant supervision technique. This technique is based upon the assumption that if a sentence contains two entities of a triple in a knowledge base, it expresses the relation stated in that triple. This assumption is very simplistic and would lead to generating wrong mappings between sentences and knowledge base triples. 

This repository implements a new method to denoising distant supervision techniquie by taking the semantic similarity between sentences and the label of triples’ predicate into account using different semantic similarity measures based on pre-trained word embeddings. This method is applied in the M-ATOLL framework. Details of the implemented approaches can be found in our paper: [Denoising Distant Supervision for Ontology Lexicalization using Semantic Similarity Measures](submitted to Expert System with Applications).






## Setup

1. Download Pretrained BERT Embeddings and put it in following directory:
```
Embeddings/bert
```

3. Download ontology lexicalization dataset from:
```
http://dblexipedia.org/public/Input_EN.tar.gz
```

4. Extract the ontology lexicalization dataset to:
```
resources/Mappings
```


##Getting Started
Before running the JAVA ontology lexicon generator, run the Python word embeddings server as:
```
python PythonServers/FTBERTServer.py
```

To gernerate ontology lexicon by default configuration, run the following MAVEN commands:
```
mvn clean && mvn install
mvn exec:java -Dexec.mainClass="process.MatollELMOandBERTAllthresholdAllMethod" -Dexec.args="--mode=train /path/to/inputMappings/ /path/to/config.xml"
```

You can change the default cofiguaration in config.xml file.


##Evaluation
To evalute the generated ontology lexicon, run:
```
mvn exec:java -e -Dexec.mainClass="evaluation.RunEvaluationAll" -Dexec.args="/path/to/output/Ontologylexicon/filePattern"
```
"filePattern" is the name of the generated ontology lexicon file in which the similaritymeasure and threshold are replaced by "#SIM_METHOD#" and "#SIM_THRESHOLD#", respectively(e.g. Autogen2020_BERT_#SIM_METHOD#_#SIM_THRESHOLD#.ttl)

##Other embeddings
Other word embeddings can be used instead of BERT. For each word embeddings, run it's specific Python server and JAVA program. See the following folders for more Python Server and Java processors, respectively:

```
PythonServers
```
and 
```
process
```


The main contributors of this repository is:
- [Mehdi Jabalameli](https://github.com/mjameli)


Contact: Mehdi Jabalameli, mjameli@yahoo.com


Don't hesitate to send me an e-mail or report an issue, if something is broken (and it shouldn't be) or if you have further questions.

> This repository contains experimental software and is published for the sole purpose of giving additional background details on the respective publication.







