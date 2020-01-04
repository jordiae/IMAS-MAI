package behaviours;

import agents.UserAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.util.Logger;
import utils.SimulationConfig;

import java.io.IOException;

public class WaitUserInputBehaviour extends CyclicBehaviour {
    private UserAgent agent;
    private String CONFIG_FILE_PATH = "src/config/";
    private SimulationConfig simulationConfig;

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

        agent.addBehaviour(new AchieveREInitiator(agent, msg) {
            protected void handleInform(ACLMessage inform) {
                System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
            }
            protected void handleRefuse(ACLMessage refuse) {
                System.out.println("Agent "+refuse.getSender().getName()+" refused to perform the requested action");
            }
            protected void handleFailure(ACLMessage failure) {
                if (failure.getSender().equals(myAgent.getAMS())) {
                    System.out.println("Responder does not exist");
                }
                else {
                    System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
                }
            }
        } );


    }
}