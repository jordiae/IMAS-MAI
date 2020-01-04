package behaviours;

import agents.FIPARequestAgent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class FIPARequestResponderBehaviour extends AchieveREResponder {
    private FIPARequestAgent agent;

    public FIPARequestResponderBehaviour(FIPARequestAgent a, MessageTemplate mt) {
        super(a, mt);
        agent = a;
    }

    protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
        System.out.println("Agent "+agent.getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());
        try {
            if (agent.checkAction(request)) {
                // We agree to perform the action. Note that in the FIPA-Request
                // protocol the AGREE message is optional. Return null if you
                // don't want to send it.
                System.out.println("Agent "+agent.getLocalName()+": Agree");
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }
            else {
                // We refuse to perform the action
                System.out.println("Agent "+agent.getLocalName()+": Refuse");
                ACLMessage refuse = request.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
                return refuse;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
        try {
            if (agent.performAction(request)) {
                System.out.println("Agent "+agent.getLocalName()+": Action successfully performed");
                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }
            else {
                System.out.println("Agent "+agent.getLocalName()+": Action failed");
                ACLMessage failure = request.createReply();
                failure.setPerformative(ACLMessage.FAILURE);
                return failure;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
