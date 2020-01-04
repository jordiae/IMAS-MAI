package behaviours;

import agents.FIPARequestAgent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;


public class FIPAProtocolBehaviour extends CyclicBehaviour {
    private FIPARequestAgent agent;

    public FIPAProtocolBehaviour(FIPARequestAgent a) {
        super(a);
        agent = a;
    }

    public void action() {
        ACLMessage msg = agent.receive();

        if (msg != null) {
            switch (msg.getPerformative()) {
                case ACLMessage.REQUEST:
                    handleRequest(msg);
                    break;
                case ACLMessage.AGREE:
                    handleAgree(msg);
                    break;
                case ACLMessage.REFUSE:
                    handleRefuse(msg);
                    break;
                case ACLMessage.INFORM:
                    handleInform(msg);
                    break;
                case ACLMessage.FAILURE:
                    handleFailure(msg);
                    break;
            }
        }
        else {
            block();
        }
    }

    private void handleRequest(ACLMessage request) {
        System.out.println("Agent "+agent.getLocalName()+": REQUEST received from "+request.getSender().getName()+". ");
        //System.out.println("Action is "+request.getContent());
        try {
            if (agent.checkAction(request)) {
                // We agree to perform the action. Note that in the FIPA-Request
                // protocol the AGREE message is optional. Return null if you
                // don't want to send it.
                System.out.println("Agent "+agent.getLocalName()+": Agree");
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                agent.agreed();

            }
            else {
                // We refuse to perform the action
                System.out.println("Agent "+agent.getLocalName()+": Refuse");
                ACLMessage refuse = request.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
                agent.refused();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleInform(ACLMessage inform) {
        System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
        agent.resultDone();
    }

    private void handleAgree(ACLMessage agree) {
        System.out.println("Agent "+agree.getSender().getName()+" agreed the requested action");
        agent.agreed();
    }

    private void handleRefuse(ACLMessage refuse) {
        System.out.println("Agent "+refuse.getSender().getName()+" refused to perform the requested action");
        agent.refused();
    }

    private void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            System.out.println("Responder does not exist");
        }
        else {
            System.out.println("Agent "+failure.getSender().getName()+" failed to perform the requested action");
        }
        agent.failed();
    }
}
