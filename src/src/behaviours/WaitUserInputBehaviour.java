package behaviours;

import agents.UserAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.util.Logger;
import utils.Config;

import java.io.IOException;

public class WaitUserInputBehaviour extends CyclicBehaviour {
    private UserAgent agent;
    private String CONFIG_FILE_PATH = "src/config/";
    private Config config;

    public WaitUserInputBehaviour(UserAgent a) {
        super(a);
        agent = a;
    }

    public void action() {
        String userInput = agent.readUserInput();
        String[] words = userInput.split(" ");

        String configFile = "imas.settings";

        String action = words[0];
        if (words.length == 2){
            configFile = words[1];
        }

        // Create message
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("manager", AID.ISLOCALNAME));
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

        // Load the simulation parameters
        config = new Config("T", CONFIG_FILE_PATH + "/" + configFile);
        // Add Serializable object to message
        try {
            msg.setContentObject(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        agent.startProcess(msg);
        block();
    }
}