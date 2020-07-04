#!/usr/bin/env bash
mvn install

#NOte: the code for CoreBert is same as ELmo. Just run different python server
mvn exec:java -e -Dexec.mainClass="process.MatollELMOandBERTAllthresholdAllMethod" -Dexec.args="--mode=train resources/Mappings
 config.xml "







