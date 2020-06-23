rem mvn install

mvn exec:java -e -Dexec.mainClass="de.citec.sc.matoll.process.MatollWithBertPreFilter" -Dexec.args="--mode=train D:\\Tools\\matoll-master\\Data\\Sample\\ config.xml bert_similiarity 0.4"
mvn exec:java -e -Dexec.mainClass="de.citec.sc.matoll.process.MatollWithBertPreFilter" -Dexec.args="--mode=train D:\\Tools\\matoll-master\\Data\\Sample\\ config.xml bert_similiarity 0.3"
mvn exec:java -e -Dexec.mainClass="de.citec.sc.matoll.process.MatollWithBertPreFilter" -Dexec.args="--mode=train D:\\Tools\\matoll-master\\Data\\Sample\\ config.xml bert_similiarity 0.5"
mvn exec:java -e -Dexec.mainClass="de.citec.sc.matoll.process.MatollWithBertPreFilter" -Dexec.args="--mode=train D:\\Tools\\matoll-master\\Data\\Sample\\ config.xml bert_similiarity 0.2"
mvn exec:java -e -Dexec.mainClass="de.citec.sc.matoll.process.MatollWithBertPreFilter" -Dexec.args="--mode=train D:\\Tools\\matoll-master\\Data\\Sample\\ config.xml bert_similiarity 0.1"
mvn exec:java -e -Dexec.mainClass="de.citec.sc.matoll.process.MatollWithBertPreFilter" -Dexec.args="--mode=train D:\\Tools\\matoll-master\\Data\\Sample\\ config.xml bert_similiarity 0.6"
mvn exec:java -e -Dexec.mainClass="de.citec.sc.matoll.process.MatollWithBertPreFilter" -Dexec.args="--mode=train D:\\Tools\\matoll-master\\Data\\Sample\\ config.xml bert_similiarity 0.7"
mvn exec:java -e -Dexec.mainClass="de.citec.sc.matoll.process.MatollWithBertPreFilter" -Dexec.args="--mode=train D:\\Tools\\matoll-master\\Data\\Sample\\ config.xml bert_similiarity 0.8"
mvn exec:java -e -Dexec.mainClass="de.citec.sc.matoll.process.MatollWithBertPreFilter" -Dexec.args="--mode=train D:\\Tools\\matoll-master\\Data\\Sample\\ config.xml bert_similiarity 0.9"
