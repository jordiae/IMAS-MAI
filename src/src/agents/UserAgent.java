package agents;

import behaviours.FIPAInitiatorBehaviour;
import behaviours.ReadUserInputBehaviour;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import utils.Config;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class UserAgent extends Agent {
    private String CONFIG_FILE_PATH = "src/config/";
    private Logger myLogger = Logger.getMyLogger(getClass().getName());
    private Config config = null;
    private boolean actionPending = false;

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
            DFService.register(this, dfd);
            addBehaviour(new ReadUserInputBehaviour(this));
            addBehaviour(new FIPAInitiatorBehaviour(this));
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Cannot register with DF", e);
            doDelete();
        }
    }

    protected void takeDown() {
        //DF unregistration
        //Close any open/required resources
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        super.takeDown();
    }

    private void createManagerAgent() {
        // Dynamic creation of the Manager Agent
        ContainerController cc = getContainerController();
        try {
            AgentController ac = cc.createNewAgent("manager",
                    this.getClass().getPackage().getName() + ".ManagerAgent",
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
        String instructions =
                "\n\n********** Please input one of the following action request **********\n" +
                        "'T' : For simulating with the 'imas.settings' configuration file\n" +
                        "'T <config_file_name> : For simulating with another configuration file\n" +
                        "'P' : To predict with the already train models.\n\n";
        String action = "";
//        myLogger.log(Logger.INFO, instructions);
        try {
            while (!validAction) {
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

    public void startAction(Config config) {
        actionPending = true;
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public void actionFinished() {
        actionPending = false;
    }

    public boolean isActionPending() {
        return actionPending;
    }

    public void receivedAgree() {}

    public void receivedRefuse(String info) {
        myLogger.log(Logger.WARNING, "REFUSED \n" + info);
    }

    public void receivedFailure(String info) {
        myLogger.log(Logger.WARNING, "FAILURE\n" + info);
    }

    public void receivedInform(String result) {
        myLogger.log(Logger.INFO, result);
    }
}
