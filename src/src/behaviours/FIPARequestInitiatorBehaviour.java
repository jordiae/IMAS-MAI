/*
package behaviours;

import agents.FIPARequestAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

import java.util.Iterator;


public class FIPARequestInitiatorBehaviour extends CyclicBehaviour {
    private FIPARequestAgent agent;

    public FIPARequestInitiatorBehaviour(FIPARequestAgent a, ACLMessage msg) {
        super(a);
        agent = a;
        System.out.println("----- REQUEST: " + agent.getLocalName() + " to: ");
        Iterator receivers = msg.getAllIntendedReceiver();
        while(receivers.hasNext()) {
            System.out.print(((AID)receivers.next()).getLocalName() + ", ");
        }
        System.out.println("");
        agent.send(msg);
        agent.blockingReceive();
    }

    public void action() {
        ACLMessage msg = agent.blockingReceive();
        if(msg != null) {
            System.out.println("----- RECEIVED: " + agent.getLocalName() + " from: " + msg.getSender().getLocalName());
            if (msg.getPerformative() == ACLMessage.AGREE) {
                handleAgree(msg);
            }
            else if (msg.getPerformative() == ACLMessage.REFUSE){
                handleRefuse(msg);
            }
            else if (msg.getPerformative() == ACLMessage.INFORM) {
                handleInform(msg);
            }
            else if (msg.getPerformative() == ACLMessage.FAILURE) {
                handleFailure(msg);
            }
        }
    }

    private void handleInform(ACLMessage inform) {
        //System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
        agent.resultDone();
    }

    private void handleAgree(ACLMessage agree) {
        //System.out.println("Agent "+agree.getSender().getName()+" agreed the requested action");
        agent.agreed();
    }

    private void handleRefuse(ACLMessage refuse) {
        //System.out.println("Agent "+refuse.getSender().getName()+" refused to perform the requested action");
        agent.refused();
    }

    private void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            //System.out.println("Responder does not exist");
        }
        else {
            //System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
        }
        agent.failed();
    }
}
*/
