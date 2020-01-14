package agents;
import behaviours.FIPAInitiatorReceiverBehaviour;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.wrapper.StaleProxyException;
import utils.Config;
import utils.Transformer;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;
import jade.wrapper.ContainerController;
import jade.wrapper.AgentController;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.converters.ArffLoader.ArffReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.Serializable;
import javafx.util.Pair;
import java.util.HashMap;


public class ManagerAgent extends Agent {
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
    private Instances data;
	private Instances testInstances;
    private String testData;
    private int numTestInstances;
    private int desiredNumClassifiers;
    private int actualNumClassifiers = 0;
    private int[] classifiersTrainInstances;
    private String[] classifierInstances;
    private String nameDatafile;
    private String algorithm;
	private Boolean TRAINED = false;
    private Config config;  // Class holding the simulation parameters

    // setup
    protected void setup() {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ManagerAgent");
        sd.setName(getName());
        sd.setOwnership("IMAS_group");
        dfd.setName(getAID());
        dfd.addServices(sd);
        try {
            DFService.register(this,dfd);
            addBehaviour(new FIPAInitiatorReceiverBehaviour(this));
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "[" + getLocalName() + "] - Cannot register with DF", e);
            doDelete();
        }
    }

    public String checkAction(Config config) throws UnreadableException, IOException {
        if (config.getAction().equals("T")) {
			try {
				desiredNumClassifiers = Integer.parseInt(config.getClassifiers());
				classifiersTrainInstances = Arrays.stream(
						config.getTrainingSettings().split(",")).mapToInt(Integer::parseInt).toArray();
				numTestInstances = Integer.parseInt(config.getClassifierInstances());
				nameDatafile = "src/data/" + config.getFile();
				algorithm = config.getAlgorithm();

				// load the arff file and separate the instances for the classifiers and for the test phase
				BufferedReader reader = new BufferedReader(new FileReader(nameDatafile));
				ArffReader arff = new ArffReader(reader);
				data = arff.getData();
				data.setClassIndex(data.numAttributes() - 1);
				classifierInstances = new String[desiredNumClassifiers];

				// convert the instances to strings using the Transformer class
				data.randomize(new java.util.Random(System.currentTimeMillis()));
				testInstances = new Instances(data, 0, numTestInstances);
				testData = Transformer.toString(testInstances);
				data = new Instances(data, numTestInstances, data.numInstances()-numTestInstances);
				for(int i = 0; i < desiredNumClassifiers; i++){
					data.randomize(new java.util.Random(System.currentTimeMillis()));
					classifierInstances[i] = Transformer.toString(new Instances(data, 0, classifiersTrainInstances[i]));
				}
			} catch (Exception e) {
				return e.getMessage();
			}
        } else if (config.getAction().equals("P") && !TRAINED) {
			return "Classifiers not ready";
		}
		return "";
    }

    public ACLMessage[] performAction(String action) throws UnreadableException, IOException, StaleProxyException {
        if (action.equals("T")) {
            if (desiredNumClassifiers > actualNumClassifiers) {
                ContainerController ManagerController = getContainerController();
                for(int i = actualNumClassifiers; i < desiredNumClassifiers; i++){
                    AgentController new_agent;
                    new_agent = ManagerController.createNewAgent("Classifier" + i,
                            this.getClass().getPackage().getName() + ".ClassifierAgent",
                            null);
                    new_agent.start();
                }
                actualNumClassifiers = desiredNumClassifiers;
            }

            ACLMessage[] messagesToClassifiers = new ACLMessage[desiredNumClassifiers];
            for(int i = 0; i < desiredNumClassifiers; i++) {
                ACLMessage msg_to_classifiers = new ACLMessage(ACLMessage.REQUEST);
                AID a = new AID("Classifier"+i, false);
                msg_to_classifiers.addReceiver(a);
                msg_to_classifiers.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                msg_to_classifiers.setContent("T_" + algorithm + "_" + classifierInstances[i]);
                messagesToClassifiers[i] = msg_to_classifiers;
            }
			TRAINED = false;
            return messagesToClassifiers;
        }
        else if (action.equals("P")) {
            ACLMessage[] messagesToClassifiers = new ACLMessage[desiredNumClassifiers];
            for(int i = 0; i < desiredNumClassifiers; i++) {
                ACLMessage msg_to_classifiers = new ACLMessage(ACLMessage.REQUEST);
                AID a = new AID("Classifier"+i, false);
                msg_to_classifiers.addReceiver(a);
                msg_to_classifiers.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                msg_to_classifiers.setContent("P_" + testData);
                messagesToClassifiers[i] = msg_to_classifiers;
            }
            return messagesToClassifiers;
        }
        return null;
    }

	public void setTrained(boolean trained) {
		TRAINED = trained;
	}

	private static String center(String s, int size, char pad) {
        if (s == null || size <= s.length())
            return s;

        String new_s = "";
        for (int i = 0; i < (size - s.length()) / 2; i++) {
            new_s += pad;
        }
        new_s += s;
        while (new_s.length() < size) {
            new_s += pad;
        }
        return new_s;
    }

    public Pair<Boolean, String> treatResults(ArrayList<Serializable> results) {
        if (!TRAINED) {
			try {
				Boolean results_ok = true;
				for (int i = 0; i < results.size(); i++){
					Pair<Boolean, String> deserialized = (Pair<Boolean, String>) results.get(i);
					if (deserialized.getKey() == false) {
						results_ok = false;
					}
				}
				if (results_ok) {
					TRAINED = true;
					return new Pair<> (true, "Classifiers have been trained");
				} else {
					return new Pair<> (false, "Classifiers not correctly trained");
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return new Pair<> (false, "At least one classifier said: Fuck off");
			}
        }
        else {
			try {
				int sp_data = 0;
				for (int i = 0; i < numTestInstances; i++) {
					int tmp = data.instance(i).toString().length();
					if (tmp > sp_data) sp_data = tmp;
				}
				int sp_ins = 10 + 2;
				sp_data += 12;
				int sp_agents = 20 + 2;
				int sp_prec = 20 + 2;
				int sp_final = 20 + 2;
				String final_Table = " Table with the results\n"+center("Instance", sp_ins, ' ') + center("Data", sp_data, ' ');
				for (int i = 0; i < desiredNumClassifiers; i++) {
					final_Table += center("Agent "+Integer.toString(i), sp_agents, ' ') + 
						center("Precission Agent "+Integer.toString(i), sp_prec, ' ');
				}
				final_Table += center("Final Decision", sp_final, ' ')+"\n";
				ArrayList<HashMap> arrayMap = new ArrayList<HashMap> (); // instance 0: N:2 O:0, instance 1: N:1 O:1 ...
				ArrayList<ArrayList<String>> classifiers_predict = new ArrayList<ArrayList<String>> (); // Classifier y in Instance x voted r
				for (int i = 0; i < results.size(); i++) { // for each classifier
					Pair<Boolean, ArrayList<String>> deserialized = (Pair<Boolean,  ArrayList<String>>) results.get(i);
					ArrayList<String> instance_results = deserialized.getValue();
					classifiers_predict.add(instance_results);
				}
				ArrayList<String> most_voted_classes = new ArrayList<String>();
				for (int i = 0; i < numTestInstances; i++) { // for each instance
					final_Table += center(Integer.toString(i), sp_ins, ' ');
					Instance inst = testInstances.instance(i);
					final_Table += center(inst.toString(), sp_data, ' ');
					HashMap map = new HashMap();
					for (int c = 0; c < classifiers_predict.size(); c++){ // for each classifier
						String pred = classifiers_predict.get(c).get(i);
						Float weight = (float) classifiersTrainInstances[c]/(data.size()+numTestInstances);
						if (map.containsKey(pred) == true) map.replace(pred, (Float) map.get(pred)+weight);
						else map.put(pred, weight);
						final_Table += center(pred, sp_agents, ' ') + 
							center(String.format("%.4f", weight*100), sp_prec, ' ');
					}
					HashMap.Entry<String, Float> most_voted = null;
					for (Object o : map.entrySet()) { // for each key in the HashMap
						HashMap.Entry<String, Float> e = (HashMap.Entry<String, Float>) o;
						if (most_voted == null || e.getValue().compareTo(most_voted.getValue()) > 0) most_voted = e;
					}
					
					final_Table += center(most_voted.getKey(), sp_final, ' ');
					final_Table += "\n";
					most_voted_classes.add(most_voted.getKey());
				}
				// Return RESULT
				return new Pair<> (true, final_Table);

			} catch (Exception e) {
				System.out.println(e.getMessage());
				return new Pair<> (false, "At least one classifier said: Fuck off");
			}

        }
    }
}

