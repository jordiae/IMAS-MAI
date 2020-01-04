package agents;

import behaviours.WaitUserInputBehaviour;
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

    protected void setup() {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("UserAgent");
        sd.setName(getName());
        sd.setOwnership("IMAS_group");
        dfd.setName(getAID());
        dfd.addServices(sd);

        createManagerAgent();

        try {
            DFService.register(this,dfd);
            WaitUserInputBehaviour behaviour = new  WaitUserInputBehaviour(this);
            addBehaviour(behaviour);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
            doDelete();
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

    private void createManagerAgent() {
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

    public String readUserInput() {
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