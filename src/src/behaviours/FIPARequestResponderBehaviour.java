/*
package behaviours;

import agents.FIPARequestAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREResponder;
import jade.wrapper.StaleProxyException;

import java.io.IOException;

public class FIPARequestResponderBehaviour extends CyclicBehaviour {
    private FIPARequestAgent agent;
    private boolean preparedResultNotification = false;

    public FIPARequestResponderBehaviour(FIPARequestAgent a) {
        super(a);
        agent = a;
    }

    public void action() {
        if (!preparedResultNotification) {
            ACLMessage msg = agent.blockingReceive();
            if(msg != null) {
                System.out.println("----- RECEIVED: " + agent.getLocalName() + " from: " + msg.getSender().getLocalName());
                if (msg.getPerformative() == ACLMessage.REQUEST) {
                    try {
                        ACLMessage reply = prepareResponse(msg);
                        System.out.println("----- RESPONSE: " + agent.getLocalName() + " to: " + msg.getSender().getLocalName());
                        agent.send(reply);

                        if (reply.getPerformative() == ACLMessage.AGREE) {
                            prepareResultNotification(msg);
                            preparedResultNotification = true;
                        }
                    } catch (NotUnderstoodException e) {
                        e.printStackTrace();
                    } catch (RefuseException e) {
                        e.printStackTrace();
                    } catch (FailureException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
            }
        }
    }

    protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
        //System.out.println("Agent "+agent.getLocalName()+": REQUEST received from "+request.getSender().getName());
        //System.out.println("Action is "+request.getContent());
        try {
            if (agent.checkAction(request)) {
                // We agree to perform the action. Note that in the FIPA-Request
                // protocol the AGREE message is optional. Return null if you
                // don't want to send it.
                //System.out.println("Agent "+ agent.getLocalName() +": Agree");
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }
            else {
                // We refuse to perform the action
                //System.out.println("Agent "+agent.getLocalName()+": Refuse");
                throw new RefuseException("check-failed");
            }
        } catch (UnreadableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected ACLMessage prepareResultNotification(ACLMessage request) throws FailureException {
        try {
            if (agent.performAction(request)) {
                //System.out.println("Agent "+agent.getLocalName()+": Action successfully performed");
                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }
            else {
                //System.out.println("Agent "+agent.getLocalName()+": Action failed");
                throw new FailureException("unexpected-error");
            }
        } catch (UnreadableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        return null;
    }
}
*/
