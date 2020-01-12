cp=src/out/production/IMAS_test
libs=lib/jade.jar:lib/weka.jar
classes=$(cp)/ClassifierAgent.class #$(cp)/ConfigReader.class $(cp)/ManagerAgent.class $(cp)/Transformer.class $(cp)/UserAgent.class

all: classes
	java -cp lib/jade.jar:lib/weka.jar:src/out/production/IMAS_test/ jade.Boot -gui -agents user:agents.UserAgent
classes:
	javac -classpath lib/jade.jar:lib/weka.jar -d src/out/production/IMAS_test/ src/src/agents/*.java src/src/behaviours/*.java src/src/utils/*.java
