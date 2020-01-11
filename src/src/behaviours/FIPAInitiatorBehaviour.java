package behaviours;

import agents.UserAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import utils.Config;

import java.io.IOException;

enum InitiatorState {
    IDLE,
    WAITING
}

public class FIPAInitiatorBehaviour extends CyclicBehaviour {
    private UserAgent myAgent;
    private InitiatorState state;

    public FIPAInitiatorBehaviour(UserAgent a) {
        super(a);
        myAgent = a;
        state = InitiatorState.IDLE;
    }

    public void action() {
        if (state == InitiatorState.IDLE) {
            if (myAgent.isActionPending()) {
                Config config = myAgent.getConfig();
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
                state = InitiatorState.WAITING;
                myAgent.send(msg);
            }
        }
        if (state == InitiatorState.WAITING) {
            ACLMessage msg = myAgent.blockingReceive();

            state = InitiatorState.IDLE;
            myAgent.actionFinished();
            if (msg.getPerformative() == ACLMessage.AGREE) {
                msg = myAgent.blockingReceive();
                myAgent.receivedAgree();

                if (msg.getPerformative() == ACLMessage.INFORM) {
                    myAgent.receivedInform(msg.getContent());
                } else if (msg.getPerformative() == ACLMessage.FAILURE) {
                    myAgent.receivedFailure(msg.getContent());
                }
            } else if (msg.getPerformative() == ACLMessage.REFUSE) {
                myAgent.receivedRefuse(msg.getContent());
            }
        }
    }
}