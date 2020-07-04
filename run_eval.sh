#!/usr/bin/env bash
mvn install

mvn exec:java -e -Dexec.mainClass="evaluation.RunEvaluationAll" -Dexec.args="resources/resources/OutputLexicon/autogen2020_04_01_FTBert_#SIM_METHOD#_#SIM_THRESHOLD#_beforeTraining.ttl"