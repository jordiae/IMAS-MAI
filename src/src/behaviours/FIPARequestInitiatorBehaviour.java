package behaviours;

import agents.FIPARequestAgent;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;

import java.util.Vector;

public class FIPARequestInitiatorBehaviour extends AchieveREInitiator {
    private FIPARequestAgent agent;

    public FIPARequestInitiatorBehaviour(FIPARequestAgent a, ACLMessage msg) {
        super(a, msg);
        agent = a;
    }

    protected void handleInform(ACLMessage inform) {
        System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
        agent.resultDone();
    }

    protected void handleAgree(ACLMessage agree) {
        System.out.println("Agent "+agree.getSender().getName()+" agreed the requested action");
        agent.agreed();
    }
    protected void handleRefuse(ACLMessage refuse) {
        System.out.println("Agent "+refuse.getSender().getName()+" refused to perform the requested action");
        agent.refused();
    }
    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            System.out.println("Responder does not exist");
        }
        else {
            System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
        }
        agent.failed();
    }
}
