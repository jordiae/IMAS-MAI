# IMAS-MAI
## Introduction to Multiagent Systems project (Master in Artificial Intelligence)
### Instructions for executing our system in Windows:
1. Compile the agents:  
```javac -classpath lib\jade.jar;lib\weka.jar -d src\out\production\IMAS_test\ src\src\agents\*.java src\src\behaviours\*.java src\src\utils\*.java```

2. Execute Jade with User Agent (the rest will be created dynamically):  
```java -cp lib\jade.jar;lib\weka.jar;src\out\production\IMAS_test\ jade.Boot -gui -agents user:agents.UserAgent```

3. Use the command line to train or predict:  
```USAGE: T <config_file> | P```   
```Example: T imas.settings```


### Instructions for executing our system in Linux:
1. Compile the agents from the folder IMAS-MAI:  
```javac -cp lib/jade.jar:lib/weka.jar -d src/out/production/IMAS_test/ src/src/agents/*.java src/src/behaviours/*.java src/src/utils/*.java```

2. Execute Jade with User Agent (the rest will be created dynamically):  
```java -cp lib/jade.jar:lib/weka.jar:src/out/production/IMAS_test/ jade.Boot -gui -agents user:agents.UserAgent```

3. Use the command line to train or predict:  
```USAGE: T <config_file> | P```   
```Example: T imas.settings```
