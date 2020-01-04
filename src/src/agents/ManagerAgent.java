package agents;
//~ package examples.Manager;
import behaviours.FIPAProtocolBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
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
import java.util.Arrays;

enum State {
    IDLE,
    TRAINING,
    TRAINED,
    PREDICTING,
}

public class ManagerAgent extends FIPARequestAgent {
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
    private State state = State.IDLE;
    private Instances data;
    private int numTestInstances;
    private int desiredNumClassifiers;
    private int actualNumClassifiers = 0;
    private int[] classifiersTrainInstances;
    private String[] classifierInstances;
    private String nameDatafile;
    private Config config;  // Class holding the simulation parameters

    private int classifiersAgreed = 0;

    private FIPAProtocolBehaviour fipaBehaviour;

    private boolean correctState = true;

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
            fipaBehaviour = new FIPAProtocolBehaviour(this);
            addBehaviour(fipaBehaviour);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "[" + getLocalName() + "] - Cannot register with DF", e);
            doDelete();
        }
    }

    public boolean checkAction(ACLMessage msg) throws UnreadableException, IOException {
        config = (Config) msg.getContentObject();
        if (config.getAction().equals("T")) {
            desiredNumClassifiers = Integer.parseInt(config.getClassifiers());
            classifiersTrainInstances = Arrays.stream(
                    config.getTrainingSettings().split(",")).mapToInt(Integer::parseInt).toArray();
            numTestInstances = Integer.parseInt(config.getClassifierInstances());
            nameDatafile = "src/data/" + config.getFile();

            // load the arff file and separate the instances for the classifiers and for the test phase
            BufferedReader reader = new BufferedReader(new FileReader(nameDatafile));
            ArffReader arff = new ArffReader(reader);
            data = arff.getData();
            data.setClassIndex(data.numAttributes() - 1);
            classifierInstances = new String[desiredNumClassifiers];

            // convert the instances to strings using the Transformer class
            for(int i = 0; i < desiredNumClassifiers; i++){
                data.randomize(new java.util.Random(0));
                classifierInstances[i] = Transformer.toString(new Instances(data, 0, classifiersTrainInstances[i]));
            }
        }
        return true;
    }

    public boolean performAction(ACLMessage msg) throws UnreadableException, IOException, StaleProxyException {
        config = (Config) msg.getContentObject();
        if (config.getAction().equals("T")) {
            if (desiredNumClassifiers > actualNumClassifiers) {
                ContainerController ManagerController = getContainerController();
                for(int i = actualNumClassifiers; i < desiredNumClassifiers; i++){
                    AgentController new_agent;
                    new_agent = ManagerController.createNewAgent("Classifier" + i,
                            this.getClass().getPackageName() + ".ClassifierAgent",
                            null);
                    new_agent.start();
                }
                actualNumClassifiers = desiredNumClassifiers;
            }

            classifiersAgreed = 0;
            state = State.TRAINING;
            for(int i = 0; i < desiredNumClassifiers; i++) {
                ACLMessage msg_to_classifiers = new ACLMessage(ACLMessage.REQUEST);
                AID a = new AID("Classifier"+i, false);
                msg_to_classifiers.addReceiver(a);
                msg_to_classifiers.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                msg_to_classifiers.setContent("T_" + classifierInstances[i]);

                send(msg_to_classifiers);
            }
        }

        else if (msg.getContent().equals("P")) {

        }
        return correctState;
    }

    public boolean waitAllResponses() {
        return classifiersAgreed == desiredNumClassifiers;
    }

    public void agreed() {
        ++classifiersAgreed;
        System.out.println("AGREED");
    }

    public void refused() {
        System.out.println("REFUSED");
    }

    public void resultDone() {
        System.out.println("INFORM");
    }

    public void failed() {
        correctState = false;
        System.out.println("FAILED");
    }


}

