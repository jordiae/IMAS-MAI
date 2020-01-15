package behaviours;
import agents.ManagerAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.StaleProxyException;
import utils.Config;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import javafx.util.Pair;

enum InitiatorReceiverState {
    IDLE,
    WAITING,
    FAILED,
    SUCCESS
}

public class FIPAInitiatorReceiverBehaviour extends CyclicBehaviour {
    private ManagerAgent myAgent;
    private InitiatorReceiverState state = InitiatorReceiverState.IDLE;
    private int numResponders = 0;
    private int numResults = 0;
	private int sentRequests = 0;
    private ArrayList<Serializable> results;
    private ACLMessage requestMsg;
    private String finalResult;

    public FIPAInitiatorReceiverBehaviour(ManagerAgent a) {
        super(a);
        myAgent = a;
        state = InitiatorReceiverState.IDLE;
    }

    public void action() {
        ACLMessage msg;
        switch(state) {
            case IDLE:
                msg = myAgent.blockingReceive();
                if (msg != null) {
                    try {
                        if (msg.getPerformative() == ACLMessage.REQUEST) {
                            requestMsg = msg;
                            results = new ArrayList<>(0);
                            Config config = (Config) msg.getContentObject();
							String error = myAgent.checkAction(config);
                            if (error.equals("")) {
                                ACLMessage response = msg.createReply();
                                response.setPerformative(ACLMessage.AGREE);
                                myAgent.send(response);

                                ACLMessage[] messages = myAgent.performAction(config.getAction());
                                numResponders = 0;
                                numResults = 0;
								sentRequests = messages.length;
                                state = InitiatorReceiverState.WAITING;
                                for (ACLMessage message : messages) {
                                    myAgent.send(message);
                                }
                            }
                            else {
                                ACLMessage response = msg.createReply();
                                response.setPerformative(ACLMessage.REFUSE);
								response.setContent(error);
                                myAgent.send(response);
                            }
                        }
                    }
                    catch (UnreadableException | IOException | StaleProxyException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case WAITING:
                msg = myAgent.blockingReceive(3000);
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.AGREE) {
                        ++numResponders;
                    }
                    else if (msg.getPerformative() == ACLMessage.REFUSE) {
						myAgent.setTrained(false);
                        state = InitiatorReceiverState.FAILED;
                    } 
					else if (msg.getPerformative() == ACLMessage.INFORM) {
                        ++numResults;
                        try {
                            results.add(msg.getContentObject());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        if (numResults == sentRequests && numResponders == sentRequests) {
							Pair<Boolean, String> tmp = myAgent.treatResults(results);
							Boolean results_recieved = tmp.getKey();
                            finalResult = tmp.getValue();	
							if (results_recieved == true){
								state = InitiatorReceiverState.SUCCESS;
							} else {
								myAgent.setTrained(false);
								state = InitiatorReceiverState.FAILED;
							}
                        }
                    }
					else if (msg.getPerformative() == ACLMessage.FAILURE) {
						myAgent.setTrained(false);
                        state = InitiatorReceiverState.FAILED;
                    }
                }
                else {
					myAgent.setTrained(false);
                    state = InitiatorReceiverState.FAILED;
                }
                break;
            case FAILED:
                ACLMessage resultFailed = requestMsg.createReply();
                resultFailed.setPerformative(ACLMessage.FAILURE);
                resultFailed.setContent(finalResult);
                myAgent.send(resultFailed);
                state = InitiatorReceiverState.IDLE;
                break;

            case SUCCESS:
                ACLMessage resultSuccess = requestMsg.createReply();
                resultSuccess.setPerformative(ACLMessage.INFORM);
                resultSuccess.setContent(finalResult);
                myAgent.send(resultSuccess);
//                System.out.println("Manager - Sent INFORM");
                state = InitiatorReceiverState.IDLE;
                break;
        }
    }
}
