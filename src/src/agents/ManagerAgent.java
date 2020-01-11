package agents;
import behaviours.ManagerBehaviour;
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
import weka.core.converters.ArffLoader.ArffReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;



public class ManagerAgent extends Agent {
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
    private Instances data;
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
            addBehaviour(new ManagerBehaviour(this));
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
				data.randomize(new java.util.Random(0));
				testData = Transformer.toString(new Instances(data, 0, numTestInstances));
				data = new Instances(data, numTestInstances, data.numInstances()-numTestInstances);
				for(int i = 0; i < desiredNumClassifiers; i++){
					data.randomize(new java.util.Random(0));
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

    public String treatResults(ArrayList<Serializable> results) {
        if (!TRAINED) {
            TRAINED = true;
            return "Classifiers have been trained";
        }
        else {
            TRAINED = false;
            // VOTING
            // Return RESULT
            return "Classifier 1 says: A B A, Classifier 2 says: B B A, Classifier 3 says: B A B " +
                    "\n  Final result after voting: B B A ";
        }
    }
}

