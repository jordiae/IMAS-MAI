/*
package behaviours;

import agents.FIPARequestAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.StaleProxyException;

import java.io.IOException;


enum Role {
    INITIATOR,
    RESPONDER
}

public class FIPAProtocolBehaviour extends CyclicBehaviour {
    private FIPARequestAgent agent;

    public FIPAProtocolBehaviour(FIPARequestAgent a) {
        super(a);
        agent = a;
    }


    public void action() {
        ACLMessage msg = agent.blockingReceive();
        if (msg != null)
            if (msg.getPerformative() == ACLMessage.AGREE) {
                if (nResponders == 0) {
                            role = Role.RESPONDER;
                        }
                    }
                    else if (msg.getPerformative() == ACLMessage.REFUSE) {
                        handleRefuse(msg);
                    }
                    else if (msg.getPerformative() == ACLMessage.FAILURE) {
                        handleFailure(msg);
                    }
                    else if (msg.getPerformative() == ACLMessage.INFORM) {
                        handleInform(msg);
                    }
                    break;

                case RESPONDER:
                    if (msg.getPerformative() == ACLMessage.REQUEST) {
                        try {
                            if (agent.checkAction(msg)) {
                                ACLMessage reply = msg.createReply();
                                reply.setPerformative(ACLMessage.AGREE);
                                agent.send(reply);

                                if (agent.performAction(msg)) {
                                    ACLMessage response = msg.createReply();
                                    response.setPerformative(ACLMessage.INFORM);
                                    agent.send(response);
                                }
                                else {
                                    ACLMessage response = msg.createReply();
                                    response.setPerformative(ACLMessage.FAILURE);
                                    agent.send(response);
                                }
                            }
                            else {
                                ACLMessage reply = msg.createReply();
                                reply.setPerformative(ACLMessage.REFUSE);
                                agent.send(reply);
                            }
                        }
                        catch (UnreadableException | IOException | StaleProxyException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
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
