package behaviours;

import agents.UserAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Config;

import java.io.IOException;

public class FIPAInitiatorBehaviour extends CyclicBehaviour {
    private UserAgent myAgent;

    public FIPAInitiatorBehaviour(UserAgent a, Config config) {
        super(a);
        myAgent = a;

        // Create message
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("manager", AID.ISLOCALNAME));
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

        // Add Serializable object to message
        try {
            msg.setContentObject(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        myAgent.send(msg);
    }

    public void action() {
        ACLMessage msg = myAgent.blockingReceive();

        if (msg.getPerformative()==ACLMessage.AGREE) {
            msg = myAgent.blockingReceive();
            myAgent.receivedAgree();

            if (msg.getPerformative()==ACLMessage.INFORM) {
                try {
                    myAgent.receivedInform(msg.getContentObject());
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }

            else if (msg.getPerformative()==ACLMessage.FAILURE) {
                myAgent.receivedFailure(msg.getContent());
            }
        }

        else if (msg.getPerformative()==ACLMessage.REFUSE) {
            myAgent.receivedRefuse(msg.getContent());
        }
    }
}