package user_agent;

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

// XML required imports
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class UserAgent extends Agent {
    private String CONFIG_FILE_PATH = "../config/imas.settings";
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
    private user_agent.SimulationSettings simulationSettings;

    private class WaitUserInputBehaviour extends CyclicBehaviour {

        public WaitUserInputBehaviour(Agent a) {
            super(a);
        }

        public void action() {
            String action = read_user_input();
            if (action != null) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setContent(action);
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

        // Load the simulation parameters
        boolean paramsReady = this.readConfigFile();

        try {
            DFService.register(this, dfd);
            WaitUserInputBehaviour UserBehaviour = new WaitUserInputBehaviour(this);
            addBehaviour(UserBehaviour);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
            doDelete();
        }

        // Dynamic creation of the Manager Agent
        ContainerController cc = getContainerController();
        try {
            // Pass the simulation settings to the manager agent on creation, with a format:
            // Title, algorithm, classifiers, training settings, classifier instances and database file.
            // i.g. 'T_IMAS_complete_simulation@J48@5@100@200@300@400@500@10@segment-test.arff'
            String[] arguments = {simulationSettings.getTitle() + '@' + simulationSettings.getAlgorithm() + '@' +
                                  simulationSettings.getClassifiers() + '@' +
                                  simulationSettings.getTrainingSettings().replace(',', '@') + '@' +
                                  simulationSettings.getClassifierInstances() + "@" +
                                  simulationSettings.getFile()};
            AgentController ac = cc.createNewAgent("manager", "ManagerAgent", arguments);
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

    protected String read_user_input() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String action = reader.readLine();
            if(action.equals("T")) {
                //read_config_file();
                //TO-DO: return also the content of the configuration file
                return "T";
            }
            else if (action.equals("P")) {
                return "P";
            }

            else {
                System.out.println("Wrong action");
                System.out.println("USAGE: T <config_file> | P");
                return null;
            }
        }
        catch (IOException e) {
            System.out.println("Wrong action");
            System.out.println("USAGE: T <config_file> | P");
            e.printStackTrace();
            return null;
        }
    }

    private boolean readConfigFile() {
        myLogger.log(Logger.INFO, "Loading simulation parameters from: ", this.CONFIG_FILE_PATH);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SimulationSettings.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            this.simulationSettings = (SimulationSettings) jaxbUnmarshaller.unmarshal(new File(this.CONFIG_FILE_PATH));
            myLogger.log(Logger.INFO, "Simulation parameters loaded SUCCESSFULLY");
            return true;
        }catch(JAXBException e ){
            myLogger.log(Logger.SEVERE, "Error parsing simulation settings: " + e.toString());
        }
        // To-Do: Something went wrong, evaluate what to do here...
        return false;
    }
}