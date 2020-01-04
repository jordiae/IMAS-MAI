package agents;

import jade.domain.FIPANames;
import jade.proto.AchieveREInitiator;
import utils.SimulationConfig;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jade.util.Logger;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class UserAgent extends Agent {
    private String CONFIG_FILE_PATH = "src/config/";
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
    private SimulationConfig simulationConfig;

    private class WaitUserInputBehaviour extends CyclicBehaviour {

        public WaitUserInputBehaviour(Agent a) {
            super(a);
        }

        public void action() {
            String user_input = readUserInput();
            String[] words = user_input.split(" ");

            String configFile = "imas.settings";

            String action = words[0];
            if (words.length == 2){
                configFile = words[1];
            }

            // Create message
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("manager", AID.ISLOCALNAME));
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            msg.setContent(action);
            if (action.equals("T")) {
                // Load the simulation parameters
                simulationConfig = SimulationConfig.fromXML(CONFIG_FILE_PATH + "/" + configFile);
                // Add Serializable object to message
                try {
                    msg.setContentObject(simulationConfig);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            send(msg);

            // Await confirmation from action performed
//            ACLMessage response = myAgent.receive();  // Wait for message
//            processActionResponse(response);
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
            AgentController ac = cc.createNewAgent("manager",
                    this.getClass().getPackageName() + ".ManagerAgent",
                    null);
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

    private void processActionResponse(ACLMessage response) {
//        To-Do: Process response from training and prediction separately... when state is idle enable user interaction
//        again
    }

    private String readUserInput() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean validAction = false;
        String instructions = "\n\n****** Please input one of the following action request *************\n" +
                "'T' : For simulating with the 'imas.settings' configuration file\n" +
                "'T <config_file_name> : For simulating with another configuration file\n" +
                "'P' : To predict with the already train models.\n\n";
        String action = "";
        System.out.println(instructions);
        try {
            while(!validAction) {
                // Read user input
                action = reader.readLine();
                if (action != null && (action.startsWith("T") || action.startsWith("P"))) {
                    validAction = true;
                } else {
                    myLogger.log(Logger.WARNING, "Wrong action" + instructions);
                }
            }
        } catch (Exception e) {
            myLogger.log(Logger.SEVERE, "Error during user interaction");
            e.printStackTrace();
            return "";
        }
        return action;

    }
}