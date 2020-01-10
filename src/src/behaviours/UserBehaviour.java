package behaviours;

import agents.UserAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import utils.Config;

import java.io.IOException;

public class UserBehaviour extends CyclicBehaviour {
    private UserAgent agent;
    private String CONFIG_FILE_PATH = "src/config/";
    private Config config;

    public UserBehaviour(UserAgent a) {
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

        agent.send(msg);
        System.out.println("User - Sent REQUEST");

        ACLMessage receivedResult = agent.blockingReceive();

        if (receivedResult.getPerformative()==ACLMessage.AGREE) {
            receivedResult = agent.blockingReceive();
            System.out.println("User - Received AGREE");

            if (receivedResult.getPerformative()==ACLMessage.INFORM) {
                System.out.println("User - Received INFORM");
            }

            else if (receivedResult.getPerformative()==ACLMessage.FAILURE) {
                System.out.println("User - Received FAILURE");
            }
        }

        else if (receivedResult.getPerformative()==ACLMessage.REFUSE) {
            System.out.println("User - Received REFUSE");
        }
    }
}