import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import jade.util.Logger;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import javafx.util.Pair;

// XML required imports
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class UserAgent extends Agent {
    private String CONFIG_FILE_PATH = "../config/";
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
    private ConfigReader configReader;

    private class WaitUserInputBehaviour extends CyclicBehaviour {

        public WaitUserInputBehaviour(Agent a) {
            super(a);
        }

        public void action() {
            Pair<String, String> user_input = read_user_input();
            if (user_input!= null) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                if (user_input.getKey().equals("T")) {
                    // Load the simulation parameters
                    boolean paramsReady = readConfigFile(user_input.getValue());

                    // Pass the simulation settings to the manager agent on creation, with a format:
                    // Title, algorithm, classifiers, training settings, classifier instances and database file.
                    // e.g. 'T_IMAS_complete_simulation@J48@5@100@200@300@400@500@10@segment-test.arff'
                    String arguments = user_input.getKey()+"_"+configReader.getTitle() + '@' + configReader.getAlgorithm() + '@' +
                            configReader.getClassifiers() + '@' +
                            configReader.getTrainingSettings().replace(',', '@') + '@' +
                            configReader.getClassifierInstances() + "@" +
                            configReader.getFile();
                    msg.setContent(arguments);
                }
                else {
                    msg.setContent(user_input.getKey());
                }
                msg.addReceiver(new AID("manager", AID.ISLOCALNAME));
                send(msg);
            }
        }
    }

    protected void setup() {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("UserAgent");
        sd.setName(getName());
        sd.setOwnership("IMAS_group");
        dfd.setName(getAID());
        dfd.addServices(sd);

        try {
            DFService.register(this,dfd);
            WaitUserInputBehaviour UserBehaviour = new  WaitUserInputBehaviour(this);
            addBehaviour(UserBehaviour);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
            doDelete();
        }

        // Dynamic creation of the Manager Agent
        ContainerController cc = getContainerController();
        try {
            AgentController ac = cc.createNewAgent("manager", "ManagerAgent", null);
            try {
                ac.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    protected void takeDown() {
        //DF unregistration
        //Close any open/required resources
    }

    private Pair<String, String> read_user_input() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String action = reader.readLine();
            String[] words = action.split(" ");
            if(words[0].equals("T")) {
                return new Pair<String, String>("T", words[1]);
            }
            else if (words[0].equals("P")) {
                return new Pair<String, String>("P", null);
            }

            else {
                myLogger.log(Logger.INFO, "Wrong action");
                myLogger.log(Logger.INFO, "USAGE: T <config_file> | P");
                return null;
            }
        }
        catch (IOException e) {
            myLogger.log(Logger.INFO, "Wrong action");
            myLogger.log(Logger.INFO, "USAGE: T <config_file> | P");
            e.printStackTrace();
            return null;
        }
    }

    private boolean readConfigFile(String filename) {
        myLogger.log(Logger.INFO, "Loading simulation parameters from: ", CONFIG_FILE_PATH + filename);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ConfigReader.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            configReader = (ConfigReader) jaxbUnmarshaller.unmarshal(new File(CONFIG_FILE_PATH + filename));
            myLogger.log(Logger.INFO, "Simulation parameters loaded SUCCESSFULLY");
            return true;
        }catch(JAXBException e ){
            myLogger.log(Logger.SEVERE, "Error parsing simulation settings: " + e.toString());
        }
        // To-Do: Something went wrong, evaluate what to do here...
        return false;
    }
}